import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CourseDatabase.java
 * ---------------------------------------------------------
 * Responsibility (single, focused):
 *   - Define the Course data model
 *   - Hold the in-memory course dataset
 *   - Provide simple retrieval operations (by id, get all)
 *
 * No DSA search/recommendation logic lives here on purpose -
 * this file is pure data + data access.
 */
public class CourseDatabase {

    /** Course model: one record of the dataset. */
    public static class Course {
        private final int id;
        private final String name;
        private final String category;
        private final String description;
        private final List<String> prerequisites;   // e.g. ["Basic Programming", "Statistics"]
        private final List<String> requiredSkills;   // e.g. ["Python", "Linear Algebra"]
        private final List<String> careerPaths;      // e.g. ["Data Scientist", "ML Engineer"]
        private final List<String> interestTags;     // e.g. ["Machine Learning", "AI", "Data"]

        public Course(int id, String name, String category, String description,
                      List<String> prerequisites, List<String> requiredSkills,
                      List<String> careerPaths, List<String> interestTags) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.description = description;
            this.prerequisites = prerequisites;
            this.requiredSkills = requiredSkills;
            this.careerPaths = careerPaths;
            this.interestTags = interestTags;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getDescription() { return description; }
        public List<String> getPrerequisites() { return prerequisites; }
        public List<String> getRequiredSkills() { return requiredSkills; }
        public List<String> getCareerPaths() { return careerPaths; }
        public List<String> getInterestTags() { return interestTags; }

        /** Short one-line summary, used in list views. */
        public String toBriefString() {
            return String.format("[%02d] %-28s | %-20s", id, name, category);
        }

        /** Full detail view. */
        public String toDetailString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Course ID       : ").append(id).append("\n");
            sb.append("Name            : ").append(name).append("\n");
            sb.append("Category        : ").append(category).append("\n");
            sb.append("Description     : ").append(description).append("\n");
            sb.append("Prerequisites   : ").append(prerequisites.isEmpty() ? "None" : String.join(", ", prerequisites)).append("\n");
            sb.append("Required Skills : ").append(String.join(", ", requiredSkills)).append("\n");
            sb.append("Career Paths    : ").append(String.join(", ", careerPaths)).append("\n");
            return sb.toString();
        }
    }

    private final List<Course> courses;

    public CourseDatabase() {
        courses = new ArrayList<>();
        loadDataset();
    }

    public List<Course> getAllCourses() {
        return courses;
    }

    public Course getCourseById(int id) {
        for (Course c : courses) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    /** Populates a reasonable, meaningful dataset covering multiple domains. */
    private void loadDataset() {
        courses.add(new Course(1, "Machine Learning Fundamentals", "AI/ML",
                "Introduction to supervised and unsupervised learning, model training and evaluation.",
                Arrays.asList("Basic Programming", "Statistics"),
                Arrays.asList("Python", "Linear Algebra", "Probability"),
                Arrays.asList("ML Engineer", "Data Scientist", "AI Researcher"),
                Arrays.asList("Machine Learning", "Artificial Intelligence", "Data")));

        courses.add(new Course(2, "Data Science Essentials", "Data Science",
                "Data cleaning, exploratory analysis, visualization and statistical inference.",
                Arrays.asList("Basic Programming", "Statistics"),
                Arrays.asList("Python", "SQL", "Statistics"),
                Arrays.asList("Data Scientist", "Data Analyst", "Business Analyst"),
                Arrays.asList("Data Analysis", "Statistics", "Visualization")));

        courses.add(new Course(3, "Deep Learning with Neural Networks", "AI/ML",
                "Feedforward networks, CNNs and RNNs for vision and sequence tasks.",
                Arrays.asList("Machine Learning Fundamentals", "Linear Algebra"),
                Arrays.asList("Python", "TensorFlow", "Linear Algebra"),
                Arrays.asList("AI Researcher", "ML Engineer", "Computer Vision Engineer"),
                Arrays.asList("Deep Learning", "Neural Networks", "Artificial Intelligence")));

        courses.add(new Course(4, "Full Stack Web Development", "Web Development",
                "Building end-to-end web applications using front-end and back-end technologies.",
                Arrays.asList("Basic Programming"),
                Arrays.asList("JavaScript", "HTML/CSS", "Node.js", "SQL"),
                Arrays.asList("Web Developer", "Full Stack Engineer", "Software Engineer"),
                Arrays.asList("Web Development", "Frontend", "Backend")));

        courses.add(new Course(5, "Cybersecurity Fundamentals", "Cybersecurity",
                "Core concepts of network security, cryptography and ethical hacking.",
                Arrays.asList("Networking Basics"),
                Arrays.asList("Networking", "Linux", "Cryptography"),
                Arrays.asList("Security Analyst", "Penetration Tester", "SOC Analyst"),
                Arrays.asList("Cybersecurity", "Networking", "Ethical Hacking")));

        courses.add(new Course(6, "Cloud Computing with AWS", "Cloud Computing",
                "Designing and deploying scalable applications using cloud infrastructure.",
                Arrays.asList("Basic Programming", "Networking Basics"),
                Arrays.asList("Linux", "Networking", "AWS"),
                Arrays.asList("Cloud Engineer", "DevOps Engineer", "Solutions Architect"),
                Arrays.asList("Cloud Computing", "Infrastructure", "DevOps")));

        courses.add(new Course(7, "Mobile App Development with Android", "Mobile Development",
                "Building native Android applications using Java/Kotlin and Android SDK.",
                Arrays.asList("Basic Programming"),
                Arrays.asList("Java", "Kotlin", "Android SDK"),
                Arrays.asList("Mobile App Developer", "Android Developer", "Software Engineer"),
                Arrays.asList("Mobile Development", "Android", "App Design")));

        courses.add(new Course(8, "Software Engineering Principles", "Software Engineering",
                "Design patterns, software architecture, testing and version control practices.",
                Arrays.asList("Basic Programming"),
                Arrays.asList("Java", "Git", "OOP Design"),
                Arrays.asList("Software Engineer", "Software Architect", "Backend Developer"),
                Arrays.asList("Software Engineering", "Design Patterns", "Architecture")));

        courses.add(new Course(9, "Database Management Systems", "Database Systems",
                "Relational database design, normalization, indexing and query optimization.",
                Arrays.asList("Basic Programming"),
                Arrays.asList("SQL", "Database Design"),
                Arrays.asList("Database Administrator", "Backend Developer", "Data Engineer"),
                Arrays.asList("Databases", "SQL", "Data Modeling")));

        courses.add(new Course(10, "Computer Networking", "Networking",
                "Network protocols, TCP/IP model, routing and network troubleshooting.",
                new ArrayList<>(),
                Arrays.asList("Networking", "Linux"),
                Arrays.asList("Network Engineer", "Systems Administrator", "Security Analyst"),
                Arrays.asList("Networking", "Protocols", "Infrastructure")));

        courses.add(new Course(11, "UI/UX Design Principles", "UI/UX Design",
                "User-centered design, wireframing, prototyping and usability testing.",
                new ArrayList<>(),
                Arrays.asList("Figma", "Design Thinking"),
                Arrays.asList("UI Designer", "UX Designer", "Product Designer"),
                Arrays.asList("Design", "User Experience", "Prototyping")));

        courses.add(new Course(12, "Blockchain and Smart Contracts", "Blockchain",
                "Distributed ledger concepts, consensus algorithms and smart contract development.",
                Arrays.asList("Basic Programming"),
                Arrays.asList("Solidity", "Cryptography", "JavaScript"),
                Arrays.asList("Blockchain Developer", "Smart Contract Engineer"),
                Arrays.asList("Blockchain", "Cryptography", "Decentralized Systems")));

        courses.add(new Course(13, "Game Development with Unity", "Game Development",
                "2D/3D game design, physics, scripting and building playable prototypes.",
                Arrays.asList("Basic Programming"),
                Arrays.asList("C#", "Unity", "Game Design"),
                Arrays.asList("Game Developer", "Game Designer"),
                Arrays.asList("Game Development", "Graphics", "Interactive Design")));

        courses.add(new Course(14, "Data Structures and Algorithms", "Software Engineering",
                "Core data structures, algorithm design paradigms and complexity analysis.",
                Arrays.asList("Basic Programming"),
                Arrays.asList("Java", "Problem Solving", "Mathematics"),
                Arrays.asList("Software Engineer", "Competitive Programmer", "Backend Developer"),
                Arrays.asList("Algorithms", "Problem Solving", "Data Structures")));
    }
}
