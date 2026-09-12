import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Main.java
 * ---------------------------------------------------------
 * Responsibility (single, focused):
 *   - Start the application
 *   - Show the user-facing menu (NO algorithm names here)
 *   - Read input, delegate work to RecommendationEngine / SearchEngine
 *   - Print results
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CourseDatabase database = new CourseDatabase();
    private static final RecommendationEngine recommendationEngine = new RecommendationEngine(database);
    private static final SearchEngine searchEngine = new SearchEngine(database);

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("     COURSE RECOMMENDATION SYSTEM");
        System.out.println("========================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": handleRecommend(); break;
                case "2": handleSearch(); break;
                case "3": handleViewAll(); break;
                case "4": handleViewDetails(); break;
                case "5": handleCompare(); break;
                case "6": running = false; System.out.println("\nThank you for using the Course Recommendation System!"); break;
                default: System.out.println("\nInvalid choice. Please enter a number between 1 and 6.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n----------------------------------------");
        System.out.println("1. Get Course Recommendations");
        System.out.println("2. Search Courses");
        System.out.println("3. View All Courses");
        System.out.println("4. View Course Details");
        System.out.println("5. Compare Courses");
        System.out.println("6. Exit");
        System.out.print("Enter your choice: ");
    }

    // ------------------------------------------------------------
    // 1. Recommendation
    // ------------------------------------------------------------
    private static void handleRecommend() {
        System.out.println("\n--- Get Course Recommendations ---");
        List<String> interests = readList("Enter your interests (comma separated, e.g. Machine Learning, Web Development): ");
        List<String> skills = readList("Enter your current skills (comma separated, e.g. Python, SQL): ");
        List<String> qualifications = readList("Enter your qualifications (comma separated, e.g. Basic Programming, Statistics): ");
        List<String> careerGoals = readList("Enter your career goals (comma separated, e.g. Data Scientist): ");

        RecommendationEngine.StudentProfile profile =
                new RecommendationEngine.StudentProfile(interests, skills, qualifications, careerGoals);

        List<RecommendationEngine.ScoredCourse> results = recommendationEngine.recommend(profile);

        System.out.println("\nTop Recommended Courses:");
        System.out.println("----------------------------------------");
        int shown = 0;
        for (RecommendationEngine.ScoredCourse sc : results) {
            if (shown >= 5) break; // top 5
            if (sc.score <= 0.0) continue;
            shown++;
            System.out.printf("%d. %s%n", shown, sc.course.getName());
            System.out.printf("   Category      : %s%n", sc.course.getCategory());
            System.out.printf("   Match Score   : %.1f / 100%n", sc.score);
            System.out.printf("   Eligible      : %s%n", sc.eligible ? "Yes" : "No");
            if (!sc.eligible) {
                System.out.printf("   Missing Prereqs: %s%n", String.join(", ", sc.missingPrerequisites));
            }
            System.out.println();
        }
        if (shown == 0) {
            System.out.println("No matching courses found. Try adding more interests or skills.");
        }
    }

    // ------------------------------------------------------------
    // 2. Search
    // ------------------------------------------------------------
    private static void handleSearch() {
        System.out.println("\n--- Search Courses ---");
        System.out.print("Enter search query: ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("Please enter a non-empty search query.");
            return;
        }

        List<SearchEngine.SearchResult> results = searchEngine.search(query);

        System.out.println("\nSearch Results for: \"" + query + "\"");
        System.out.println("----------------------------------------");
        if (results.isEmpty()) {
            System.out.println("No matching or similar courses found.");
            return;
        }
        int rank = 1;
        for (SearchEngine.SearchResult r : results) {
            System.out.printf("%d. %s%n", rank++, r.course.getName());
            System.out.printf("   Category   : %s%n", r.course.getCategory());
            System.out.printf("   Similarity : %.0f%%%n%n", r.confidence);
        }
    }

    // ------------------------------------------------------------
    // 3. View All
    // ------------------------------------------------------------
    private static void handleViewAll() {
        System.out.println("\n--- All Courses ---");
        System.out.println("----------------------------------------");
        for (CourseDatabase.Course c : database.getAllCourses()) {
            System.out.println(c.toBriefString());
        }
    }

    // ------------------------------------------------------------
    // 4. View Details
    // ------------------------------------------------------------
    private static void handleViewDetails() {
        System.out.println("\n--- View Course Details ---");
        int id = readCourseId("Enter Course ID: ");
        CourseDatabase.Course course = database.getCourseById(id);
        if (course == null) {
            System.out.println("No course found with ID " + id + ".");
            return;
        }
        System.out.println("\n" + course.toDetailString());
    }

    // ------------------------------------------------------------
    // 5. Compare
    // ------------------------------------------------------------
    private static void handleCompare() {
        System.out.println("\n--- Compare Courses ---");
        int id1 = readCourseId("Enter first Course ID: ");
        int id2 = readCourseId("Enter second Course ID: ");

        CourseDatabase.Course c1 = database.getCourseById(id1);
        CourseDatabase.Course c2 = database.getCourseById(id2);

        if (c1 == null || c2 == null) {
            System.out.println("One or both course IDs are invalid.");
            return;
        }

        System.out.println();
        printComparisonRow("Name", c1.getName(), c2.getName());
        printComparisonRow("Category", c1.getCategory(), c2.getCategory());
        printComparisonRow("Prerequisites", joinOrNone(c1.getPrerequisites()), joinOrNone(c2.getPrerequisites()));
        printComparisonRow("Required Skills", String.join(", ", c1.getRequiredSkills()), String.join(", ", c2.getRequiredSkills()));
        printComparisonRow("Career Paths", String.join(", ", c1.getCareerPaths()), String.join(", ", c2.getCareerPaths()));
    }

    private static void printComparisonRow(String label, String v1, String v2) {
        System.out.printf("%-16s | %-30s | %-30s%n", label, v1, v2);
    }

    private static String joinOrNone(List<String> list) {
        return list.isEmpty() ? "None" : String.join(", ", list);
    }

    // ------------------------------------------------------------
    // Input helpers
    // ------------------------------------------------------------
    private static List<String> readList(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (String part : line.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    private static int readCourseId(String prompt) {
        System.out.print(prompt);
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid numeric Course ID: ");
            }
        }
    }
}
