import java.util.ArrayList;
import java.util.List;

/**
 * SearchEngine.java
 * ---------------------------------------------------------
 * Responsibility (single, focused):
 *   - Keyword search over the course dataset
 *   - KMP        -> exact pattern search in course NAME / CATEGORY
 *   - Rabin-Karp -> hashing-based keyword search in DESCRIPTION / SKILLS
 *   - Edit Distance (DP) -> fuzzy fallback when no exact/keyword match found
 *
 * These algorithm names are NEVER shown to the end user - only clean
 * "search results" leave this class via Main.java.
 */
public class SearchEngine {

    private final CourseDatabase db;

    // Below this similarity %, a fuzzy match is not considered useful.
    private static final double FUZZY_RESULT_THRESHOLD = 50.0;

    public SearchEngine(CourseDatabase db) {
        this.db = db;
    }

    /** One search hit: course + confidence (100 = exact, else similarity %). */
    public static class SearchResult {
        public final CourseDatabase.Course course;
        public final double confidence;

        public SearchResult(CourseDatabase.Course course, double confidence) {
            this.course = course;
            this.confidence = confidence;
        }

        public double getConfidence() { return confidence; }
    }

    /**
     * Search workflow:
     *   query -> KMP (name/category) + Rabin-Karp (description/skills)
     *         -> if nothing found -> Edit Distance fuzzy fallback
     */
    public List<SearchResult> search(String query) {
        String q = query.trim().toLowerCase();
        List<SearchResult> results = new ArrayList<>();

        for (CourseDatabase.Course course : db.getAllCourses()) {
            boolean strongMatch = kmpContains(course.getName().toLowerCase(), q)
                    || kmpContains(course.getCategory().toLowerCase(), q);

            boolean keywordMatch = rabinKarpContains(course.getDescription().toLowerCase(), q)
                    || matchesAnySkill(course, q);

            if (strongMatch) {
                results.add(new SearchResult(course, 100.0));
            } else if (keywordMatch) {
                results.add(new SearchResult(course, 90.0));
            }
        }

        if (!results.isEmpty()) {
            return RecommendationEngine.mergeSortDescending(results, SearchResult::getConfidence);
        }

        // Nothing found via exact/keyword search -> fall back to fuzzy matching.
        return fuzzySearch(q);
    }

    /** Rabin-Karp keyword check against a course's required skills list. */
    private boolean matchesAnySkill(CourseDatabase.Course course, String query) {
        for (String skill : course.getRequiredSkills()) {
            if (rabinKarpContains(skill.toLowerCase(), query)) return true;
        }
        return false;
    }

    /** Fuzzy fallback: compares query against name, category and skills via Edit Distance similarity. */
    private List<SearchResult> fuzzySearch(String query) {
        List<SearchResult> fuzzyResults = new ArrayList<>();

        for (CourseDatabase.Course course : db.getAllCourses()) {
            double best = similarityPercent(query, course.getName());
            best = Math.max(best, similarityPercent(query, course.getCategory()));
            for (String skill : course.getRequiredSkills()) {
                best = Math.max(best, similarityPercent(query, skill));
            }
            if (best >= FUZZY_RESULT_THRESHOLD) {
                fuzzyResults.add(new SearchResult(course, best));
            }
        }

        return RecommendationEngine.mergeSortDescending(fuzzyResults, SearchResult::getConfidence);
    }

    // ==================================================================
    // DSA ALGORITHM 1: KMP (Knuth-Morris-Pratt) pattern search.
    // Purpose: fast exact substring search in course name/category.
    // Time complexity: O(n + m) where n = text length, m = pattern length.
    // ==================================================================
    public static boolean kmpContains(String text, String pattern) {
        if (pattern.isEmpty()) return true;
        int[] lps = computeLPSArray(pattern);
        int i = 0, j = 0;
        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++; j++;
                if (j == pattern.length()) return true;
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return false;
    }

    /** Builds the "Longest Prefix which is also Suffix" table used by KMP. */
    private static int[] computeLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0, i = 1;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    // ==================================================================
    // DSA ALGORITHM 2: Rabin-Karp pattern search (hashing-based).
    // Purpose: alternative keyword search across description/skills fields,
    // using a rolling hash instead of character-by-character backtracking.
    // Time complexity: O(n + m) average case.
    // ==================================================================
    public static boolean rabinKarpContains(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        if (m == 0) return true;
        if (m > n) return false;

        final int base = 256;
        final long mod = 1_000_000_007L;

        long patternHash = 0, windowHash = 0, highOrder = 1;
        for (int i = 0; i < m - 1; i++) {
            highOrder = (highOrder * base) % mod;
        }
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % mod;
            windowHash = (windowHash * base + text.charAt(i)) % mod;
        }

        for (int i = 0; i <= n - m; i++) {
            if (patternHash == windowHash && text.regionMatches(i, pattern, 0, m)) {
                return true; // hash matched AND verified -> real match (handles collisions)
            }
            if (i < n - m) {
                windowHash = (base * (windowHash - text.charAt(i) * highOrder) + text.charAt(i + m)) % mod;
                if (windowHash < 0) windowHash += mod;
            }
        }
        return false;
    }

    // ==================================================================
    // DSA ALGORITHM 3: Edit Distance (Levenshtein) via Dynamic Programming.
    // Purpose: fuzzy matching for misspelled/partial queries.
    // Time complexity: O(n * m), Space complexity: O(n * m).
    // ==================================================================
    public static int editDistance(String a, String b) {
        String s1 = a.toLowerCase();
        String s2 = b.toLowerCase();
        int n = s1.length(), m = s2.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // no edit needed
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],      // substitution
                                    Math.min(dp[i - 1][j],          // deletion
                                             dp[i][j - 1]));        // insertion
                }
            }
        }
        return dp[n][m];
    }

    /** Converts raw edit distance into a 0-100% similarity score, normalized by the longer string's length. */
    public static double similarityPercent(String a, String b) {
        if (a == null || b == null) return 0.0;
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 100.0;
        int distance = editDistance(a, b);
        return Math.max(0.0, (1.0 - ((double) distance / maxLen)) * 100.0);
    }
}
