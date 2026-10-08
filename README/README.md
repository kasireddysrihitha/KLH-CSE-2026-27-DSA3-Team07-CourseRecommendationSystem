# Course Recommendation System

A Java-based course recommendation and planning tool built for **Data Structures and Algorithms - 3 (25CS2103E)**. It loads a course catalogue from a text file and helps a student search courses, get ranked recommendations, check prerequisites, compare courses and plan a learning path. The main features are written with hand-implemented data structures and algorithms rather than external libraries.

The project has a console menu and a web interface (HTML/CSS/JavaScript) served by a small Java HTTP server.

## Key Features

- Course data loaded from an external file (`data/courses.txt`) with validation
- Search by course name, category, description or skill, with spelling-error tolerance
- Prefix auto-suggestions while typing (e.g. "Mach" suggests *Machine Learning Fundamentals*)
- Personalised recommendations from interests, skills, qualifications and career goals
- Prerequisite / eligibility check with the list of missing prerequisites
- Course details, category and text filtering, and side-by-side comparison of two courses
- Prerequisite dependency graph with cycle detection
- Learning path to a target course and a semester plan
- "What this course unlocks" analysis

## Data Structures and Algorithms Used

| Topic | Where | Used for |
|---|---|---|
| File handling | `FileHandler.java` | Reads and validates `data/courses.txt` line by line; skips bad lines with a warning |
| KMP | `SearchEngine.java` | Exact substring search in course name and category |
| Rabin-Karp | `SearchEngine.java` | Keyword search in description and required skills (rolling hash, verified on match) |
| Edit Distance (dynamic programming) | `SearchEngine.java` | Fuzzy search fallback, fuzzy matching of profile terms and known topics |
| Merge Sort | `RecommendationEngine.java` | Sorting search results by confidence and recommendations by score |
| Trie | `CourseTrie.java` | Prefix suggestions on course names (and on the start of later words in a name) |
| Graph (adjacency lists) | `CourseGraph.java` | Courses and prerequisite topics as vertices, prerequisites as directed edges |
| Topological sort (Kahn's algorithm) | `CourseGraph.java` | Valid study order, levels and cycle detection |
| BFS | `CourseGraph.java` | Weak components, course unlocks, and the set of courses needed for a target |
| Learning path / semester planning | `CourseGraph.java` | Greedy scheduling of the needed courses with a courses-per-semester limit |

## How It Works

1. On start-up, `CourseDatabase` asks `FileHandler` to load `data/courses.txt`.
2. `SearchEngine` builds its Trie, and `CourseGraph` builds the prerequisite graph, from the loaded courses.
3. **Search:** KMP on name/category (confidence 100), then Rabin-Karp on description/skills (confidence 90). If nothing is found, Edit Distance is used as a fuzzy fallback. Results are ordered with Merge Sort.
4. **Recommendation:** each course gets a score out of 100 from weighted matches of interests (30), skills (35), career goals (25) and prerequisite eligibility (10). Courses are ranked with Merge Sort.
5. **Learning path:** a reverse BFS from the target collects the needed prerequisites (skipping topics the student already knows), and a greedy scheduler assigns them to semesters.
6. `WebServer` exposes all of this as JSON and also serves the web pages.

## Technology Stack

- Java (plain Java, no frameworks or external libraries)
- JDK built-in HTTP server (`com.sun.net.httpserver`)
- HTML, CSS and JavaScript (no front-end framework)
- Plain text file as the data source

## Project Structure

```
CourseRecommendationSystem/
├── src/
│   ├── Main.java                  # console application
│   ├── WebServer.java             # HTTP server + JSON API
│   ├── JsonUtil.java              # converts objects to JSON text
│   ├── FileHandler.java           # reads/validates the data file
│   ├── CourseDatabase.java        # Course model and in-memory list
│   ├── SearchEngine.java          # KMP, Rabin-Karp, Edit Distance, search
│   ├── CourseTrie.java            # Trie
│   ├── RecommendationEngine.java  # scoring, eligibility, Merge Sort
│   └── CourseGraph.java           # prerequisite graph algorithms
├── public/                        # web front end
│   ├── index.html, recommend.html, search.html,
│   │   explore.html, course.html, compare.html
│   ├── css/style.css
│   └── js/ (api.js, components.js, nav.js, recommend.js,
│            search.js, explore.js, course.js, compare.js)
├── data/courses.txt               # course dataset
├── test/                          # Java, Python and JavaScript tests
├── GRAPH_DESIGN.md
└── FINAL_TEST_REPORT.md
```

## Dataset

Courses are stored in `data/courses.txt` (UTF-8), one course per line, with 8 fields separated by `|`:

```
id|name|category|description|prerequisites|requiredSkills|careerPaths|interestTags
```

List fields use `;` between items. Only `prerequisites` may be empty. Blank lines and lines starting with `#` are ignored. The supplied file has 14 courses. To use another file:

```
java -Dcourses.file=path/to/file.txt -cp bin WebServer
```

## How to Run

**Requirements:** JDK 17 or later with `javac` and `java`. No other dependencies are needed.

From the `CourseRecommendationSystem` folder:

```
javac -d bin src/*.java
java -cp bin WebServer
```

Then open **http://localhost:8080** in a browser. Press `Ctrl+C` to stop the server.

To use the console version instead:

```
java -cp bin Main
```

## Web Application Pages

| Page | What it does |
|---|---|
| Home | Entry page with links to the main features |
| Get Recommendations | Enter interests, skills, qualifications and career goals to get ranked courses with eligibility |
| Search | Search with live prefix suggestions and typo tolerance |
| Explore Courses | Browse all courses, filter by category or text |
| Course details | Description, prerequisites, skills, careers, what the course unlocks, and a learning path / semester plan builder |
| Compare | Two courses side by side |

## API Endpoints

All endpoints are `GET` and return JSON.

| Endpoint | Parameters |
|---|---|
| `/api/status` | – |
| `/api/courses` | – |
| `/api/course` | `id` |
| `/api/search` | `q` |
| `/api/suggest` | `q` |
| `/api/categories` | – |
| `/api/recommend` | `interests`, `skills`, `qualifications`, `careerGoals` (comma-separated) |
| `/api/compare` | `id1`, `id2` |
| `/api/graph` | – |
| `/api/path` | `target`, `known`, `perSemester` |
| `/api/unlocks` | `id` |

## Testing

Test files are in the `test/` folder: `TrieTest`, `GraphTest`, `FileHandlerTest` and `IntegrationTest` (Java), `api_test.py`, `fuzz_check.py` and `edge_dataset_check.py` (Python, standard library only), `frontend_jsdom_check.js` (Node.js with jsdom) and `run_with_server.sh`. Results from the final test round are recorded in `FINAL_TEST_REPORT.md`.

```
javac -d bin src/*.java && javac -cp bin -d bin test/*.java
for t in TrieTest GraphTest FileHandlerTest IntegrationTest; do java -cp bin $t; done
sh test/run_with_server.sh -- python3 test/api_test.py
```

## Team Members

- Kasireddy Srihitha – 2520030153
- Rishitha – 2520030101
- Shravya – 2520030322

Team 07, Section 08  
Guide: Sireesha Vikkurty

## Course Details

- **Course:** Data Structures and Algorithms - 3 (25CS2103E)
- **Department:** Computer Science and Engineering
- **Academic Year:** 2026-27

## Future Scope

- Larger course catalogue or a real database
- Storing student profiles and history
- A graph visualisation page using the existing `/api/graph` endpoint
- A better semester planner that minimises the number of semesters
- Automated tests in a real browser
