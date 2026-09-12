import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * RecommendationEngine.java
 * ---------------------------------------------------------
 * Responsibility (single, focused):
 *   - Represent a student's profile
 *   - Check eligibility / prerequisite satisfaction
 *   - Match profile against courses and compute a matching score
 *   - Rank recommendations (Merge Sort - a real DSA sorting routine,
 *     not just Collections.sort, written here because "Sorting" is
 *     this module's DSA responsibility)
 *
 * Fuzzy string comparisons (Edit Distance / DP) are delegated to
 * SearchEngine.similarityPercent(), so the Dynamic Programming
 * implementation lives in exactly one place and is reused here.
 */
public class RecommendationEngine {

    private final CourseDatabase db;

    // Similarity threshold (%) above which two terms are considered "the same"
    // e.g. student types "ML" vs course tag "Machine Learning" style typos.
    private static final double FUZZY_MATCH_THRESHOLD = 65.0;

    // Scoring weights - sum to 100. Kept simple and explainable for a viva.
    private static final double WEIGHT_INTEREST = 30.0;
    private static final double WEIGHT_SKILL = 35.0;
    private static final double WEIGHT_CAREER = 25.0;
    private static final double WEIGHT_ELIGIBILITY = 10.0;

    public RecommendationEngine(CourseDatabase db) {
        this.db = db;
    }

    /** Student profile: interests, skills, qualifications, career goals. */
    public static class StudentProfile {
        private final List<String> interests;
        private final List<String> skills;
        private final List<String> qualifications;
        private final List<String> careerGoals;

        public StudentProfile(List<String> interests, List<String> skills,
                               List<String> qualifications, List<String> careerGoals) {
            this.interests = interests;
            this.skills = skills;
            this.qualifications = qualifications;
            this.careerGoals = careerGoals;
        }

        public List<String> getInterests() { return interests; }
        public List<String> getSkills() { return skills; }
        public List<String> getQualifications() { return qualifications; }
        public List<String> getCareerGoals() { return careerGoals; }
    }

    /** A course plus its computed recommendation score and eligibility detail. */
    public static class ScoredCourse {
        public final CourseDatabase.Course course;
        public final double score;
        public final boolean eligible;
        public final List<String> missingPrerequisites;

        public ScoredCourse(CourseDatabase.Course course, double score,
                             boolean eligible, List<String> missingPrerequisites) {
            this.course = course;
            this.score = score;
            this.eligible = eligible;
            this.missingPrerequisites = missingPrerequisites;
        }

        public double getScore() { return score; }
    }

    /**
     * Core recommendation workflow:
     * profile -> per-course eligibility check -> matching score -> ranking.
     */
    public List<ScoredCourse> recommend(StudentProfile profile) {
        List<ScoredCourse> scored = new ArrayList<>();

        for (CourseDatabase.Course course : db.getAllCourses()) {
            List<String> missing = findMissingPrerequisites(profile, course);
            boolean eligible = missing.isEmpty();

            double interestFraction = fuzzyOverlapFraction(profile.getInterests(), course.getInterestTags());
            double skillFraction = fuzzyOverlapFraction(profile.getSkills(), course.getRequiredSkills());
            double careerFraction = fuzzyOverlapFraction(profile.getCareerGoals(), course.getCareerPaths());

            double prereqTotal = course.getPrerequisites().size();
            double eligibilityFraction = prereqTotal == 0
                    ? 1.0
                    : (prereqTotal - missing.size()) / prereqTotal;

            double score = (WEIGHT_INTEREST * interestFraction)
                    + (WEIGHT_SKILL * skillFraction)
                    + (WEIGHT_CAREER * careerFraction)
                    + (WEIGHT_ELIGIBILITY * eligibilityFraction);

            scored.add(new ScoredCourse(course, score, eligible, missing));
        }

        // Rank using hand-written Merge Sort - O(n log n) - by descending score.
        return mergeSortDescending(scored, ScoredCourse::getScore);
    }

    /** Returns the list of prerequisites the student does NOT satisfy (fuzzy-checked against skills+qualifications). */
    private List<String> findMissingPrerequisites(StudentProfile profile, CourseDatabase.Course course) {
        List<String> missing = new ArrayList<>();
        List<String> studentKnown = new ArrayList<>();
        studentKnown.addAll(profile.getSkills());
        studentKnown.addAll(profile.getQualifications());

        for (String prereq : course.getPrerequisites()) {
            if (!fuzzyContains(studentKnown, prereq)) {
                missing.add(prereq);
            }
        }
        return missing;
    }

    /**
     * Fraction of courseTerms that the student's list "covers" via fuzzy matching.
     * Example: studentTerms=["ML","Python"], courseTerms=["Machine Learning","Python","Statistics"]
     * -> "ML" fuzzy-matches "Machine Learning" only weakly (may or may not pass threshold),
     *    "Python" matches exactly -> fraction reflects genuine overlap.
     */
    private double fuzzyOverlapFraction(List<String> studentTerms, List<String> courseTerms) {
        if (courseTerms.isEmpty()) return 0.0;
        int matched = 0;
        for (String courseTerm : courseTerms) {
            if (fuzzyContains(studentTerms, courseTerm)) matched++;
        }
        return (double) matched / courseTerms.size();
    }

    /** True if any term in studentTerms fuzzy-matches (>= threshold) the target term. */
    private boolean fuzzyContains(List<String> studentTerms, String target) {
        for (String term : studentTerms) {
            if (SearchEngine.similarityPercent(term, target) >= FUZZY_MATCH_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Generic Merge Sort utility (DSA: Sorting).
    // Reused by SearchEngine to rank search results by similarity score,
    // so the algorithm is implemented once and shared across modules.
    // Time complexity: O(n log n), Space complexity: O(n).
    // ------------------------------------------------------------------
    public static <T> List<T> mergeSortDescending(List<T> list, ToDoubleFunction<T> scorer) {
        if (list.size() <= 1) {
            return new ArrayList<>(list);
        }
        int mid = list.size() / 2;
        List<T> left = mergeSortDescending(new ArrayList<>(list.subList(0, mid)), scorer);
        List<T> right = mergeSortDescending(new ArrayList<>(list.subList(mid, list.size())), scorer);
        return merge(left, right, scorer);
    }

    private static <T> List<T> merge(List<T> left, List<T> right, ToDoubleFunction<T> scorer) {
        List<T> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (scorer.applyAsDouble(left.get(i)) >= scorer.applyAsDouble(right.get(j))) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }
}
