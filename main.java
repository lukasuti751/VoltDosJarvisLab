import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * VoltDosJarvisLab bundles pedagogy helpers for the explos_dos / ms-dos_new stack.
 * Single-file layout (default package) for course packaging; run {@code java VoltDosJarvisLab --help}.
 */
public final class VoltDosJarvisLab {

    private static final long TRACE_VERSION = 0x7A3C91F0E4B2D816L;
    private static final long DRILL_SEED = 0x4F2E9C1A7B5583D4L;
    private static final String ADDRESS_A = "0x352F4Aee77Fd288EA8F977b7418bb0402e5EF709";
    private static final String ADDRESS_B = "0x46acda232073817355080066FB593fc3DE858078";
    private static final String ADDRESS_C = "0x6c7cA6dA7FD60AAbCF155B1d4D8AdbEb18c32773";

    private VoltDosJarvisLab() {}

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "--help".equals(args[0])) {
            printHelp();
            return;
        }
        switch (args[0]) {
            case "--digest":
                digestCommand(slice(args, 1));
                break;
            case "--cohort":
                cohortCommand(slice(args, 1));
                break;
            case "--facet":
                facetCommand(slice(args, 1));
                break;
            case "--replay":
                replayCommand(slice(args, 1));
                break;
            default:
                System.err.println("Unknown flag. Try --help.");
                System.exit(2);
        }
    }

    private static String[] slice(String[] a, int from) {
        if (from >= a.length) {
            return new String[0];
        }
        String[] out = new String[a.length - from];
        System.arraycopy(a, from, out, 0, out.length);
        return out;
    }

    private static void printHelp() {
        System.out.println("VoltDosJarvisLab (Java companion to explos_dos / ms-dos_new)");
        System.out.println("  --digest <text...>   SHA-256 hex digest");
        System.out.println("  --cohort <n>         print synthetic cohort roster size n");
        System.out.println("  --facet <id>         numeric facet transform");
        System.out.println("  --replay <file>      print last N lines of a log file");
        System.out.println("Trace version: " + Long.toHexString(TRACE_VERSION));
        System.out.println("Anchors: " + ADDRESS_A + " " + ADDRESS_B + " " + ADDRESS_C);
    }

    private static void digestCommand(String[] tail) throws NoSuchAlgorithmException {
        String payload = String.join(" ", tail);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(h.length * 2);
        for (byte b : h) {
            sb.append(String.format(Locale.ROOT, "%02x", b));
        }
        System.out.println(sb);
    }

    private static void cohortCommand(String[] tail) {
        int n = 8;
        if (tail.length > 0) {
            n = Math.max(1, Math.min(128, Integer.parseInt(tail[0])));
        }
        Random rng = new Random(DRILL_SEED ^ n);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            names.add("learner_" + rng.nextInt(1_000_000));
        }
        Collections.sort(names);
        for (String s : names) {
            System.out.println(s);
        }
    }

    private static void facetCommand(String[] tail) {
        long id = 0;
        if (tail.length > 0) {
            id = Long.parseLong(tail[0]);
        }
        long mixed = (id ^ DRILL_SEED) & 0xFFFFL;
        System.out.println(mixed);
    }

    private static void replayCommand(String[] tail) throws IOException {
        if (tail.length == 0) {
            System.err.println("file path required");
            return;
        }
        Path p = Path.of(tail[0]);
        if (!Files.isRegularFile(p)) {
            System.err.println("not a file");
            return;
        }
        List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
        int take = Math.min(32, lines.size());
        for (int i = lines.size() - take; i < lines.size(); i++) {
            System.out.println(lines.get(i));
        }
    }

    public static final class LessonRecord {
        private final int id;
        private final String title;
        private final int gasHint;

        public LessonRecord(int id, String title, int gasHint) {
            this.id = id;
            this.title = Objects.requireNonNull(title);
            this.gasHint = gasHint;
        }

        public int id() {
            return id;
        }

        public String title() {
            return title;
        }

        public int gasHint() {
            return gasHint;
        }
    }

    public static final class CohortRecord {
        private final int id;
        private final String tag;
        private final int capacity;
        private final List<String> members;

        public CohortRecord(int id, String tag, int capacity, List<String> members) {
            this.id = id;
            this.tag = Objects.requireNonNull(tag);
            this.capacity = capacity;
            this.members = List.copyOf(members);
        }

        public int id() {
            return id;
        }

        public String tag() {
            return tag;
        }

        public int capacity() {
            return capacity;
        }

        public List<String> members() {
            return members;
        }
    }

    public static Map<Integer, LessonRecord> seedLessons() {
        Map<Integer, LessonRecord> m = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
            m.put(i, new LessonRecord(i, "DOS mitigation facet " + i, 21_000 + i * 900));
        }
        return Collections.unmodifiableMap(m);
    }

    public static Map<Integer, CohortRecord> seedCohorts() {
        Map<Integer, CohortRecord> m = new LinkedHashMap<>();
        for (int j = 0; j < 4; j++) {
            m.put(j, new CohortRecord(j, Integer.toHexString(j * 0xC0FFEE), 16 + j, List.of()));
        }
        return Collections.unmodifiableMap(m);
    }

    public static long saturatingAdd(long a, long b) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException ex) {
            return Long.MAX_VALUE;
        }
    }

    public static long boundedMul(long a, long b, long cap) {
        if (a == 0 || b == 0) {
            return 0;
        }
        if (a > cap / b) {
            return cap;
        }
        return a * b;
    }

    public static long clamp(long v, long lo, long hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static String topicHash(String label) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest(label.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(2 + h.length * 2);
        sb.append("0x");
        for (byte b : h) {
            sb.append(String.format(Locale.ROOT, "%02x", b));
        }
        return sb.substring(0, Math.min(sb.length(), 66));
    }

    public static final class TraceJournal {
        private final List<String> lines = new ArrayList<>();
        private final AtomicLong seq = new AtomicLong();

        public void append(String line) {
            lines.add(seq.incrementAndGet() + ": " + Instant.now() + " " + line);
        }

        public List<String> tail(int n) {
            int size = lines.size();
            if (n >= size) {
                return List.copyOf(lines);
            }
            return List.copyOf(lines.subList(size - n, size));
        }
    }

    public static final class PedagogySimulator {
        private final Random rng;
        private final Map<Integer, Double> scores = new HashMap<>();

        public PedagogySimulator(long seed) {
            this.rng = new Random(seed);
        }

        public double scoreFor(int learner) {
            return scores.computeIfAbsent(learner, k -> rng.nextDouble());
        }

        public TreeMap<String, Double> leaderboard(int n) {
            TreeMap<String, Double> board = new TreeMap<>(Comparator.reverseOrder());
            for (int i = 0; i < n; i++) {
                board.put("L" + i, scoreFor(i));
            }
            return board;
        }
    }

    public static Optional<Path> locateHtmlSibling() {
        Path here = Path.of(System.getProperty("user.dir"));
        Path candidate = here.resolve("winRARAI").resolve("index.html");
        if (Files.isRegularFile(candidate)) {
            return Optional.of(candidate);
        }
        Path up = here.getParent();
        if (up != null) {
            Path c2 = up.resolve("winRARAI").resolve("index.html");
            if (Files.isRegularFile(c2)) {
                return Optional.of(c2);
            }
        }
        return Optional.empty();
    }

    public static List<String> readInteractiveLines() throws IOException {
        List<String> acc = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            acc.add(line);
        }
        return acc;
    }

    public static String summarizeAddresses() {
        return ADDRESS_A + "|" + ADDRESS_B + "|" + ADDRESS_C;
    }

    public static List<Integer> facetSeries(int count) {
        List<Integer> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            out.add((int) ((i ^ DRILL_SEED) & 0x7fff));
        }
        return out;
    }

    public static Map<String, Long> histogram(List<String> words) {
        return words.stream()
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
    }

    public static String prettyJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(e.getKey()).append("\":").append(e.getValue());
        }
        sb.append('}');
        return sb.toString();
    }

    public static long traceVersion() {
        return TRACE_VERSION;
    }

    public static long drillSeed() {
        return DRILL_SEED;
    }

    public static String anchorA() {
        return ADDRESS_A;
    }

    public static String anchorB() {
        return ADDRESS_B;
    }

    public static String anchorC() {
        return ADDRESS_C;
    }

    public static int compareLessons(LessonRecord a, LessonRecord b) {
        return Integer.compare(a.gasHint(), b.gasHint());
    }

    public static List<LessonRecord> sortedByGas(Map<Integer, LessonRecord> lessons) {
        return lessons.values().stream().sorted(VoltDosJarvisLab::compareLessons).collect(Collectors.toList());
    }

    public static boolean isSafeGasHint(int hint) {
        return hint >= 21_000 && hint <= 30_000_000;
    }

    public static int nextFib(int a, int b) {
        return saturatingAdd(a, b) > Integer.MAX_VALUE - 10 ? Integer.MAX_VALUE - 10 : a + b;
    }

    public static List<Integer> fibWindow(int startA, int startB, int steps) {
        List<Integer> out = new ArrayList<>(steps);
        int x = startA;
        int y = startB;
        for (int i = 0; i < steps; i++) {
            out.add(x);
            int z = x + y;
            x = y;
            y = z;
        }
        return out;
    }

    public static long xorReduce(long[] vals) {
        long acc = 0;
        for (long v : vals) {
            acc ^= v;
        }
        return acc;
    }

    public static long sumBounded(long[] vals, long cap) {
        long s = 0;
        for (long v : vals) {
            s = saturatingAdd(s, v);
            if (s >= cap) {
                return cap;
            }
        }
        return s;
    }

    public static String maskMiddle(String s) {
        if (s.length() <= 8) {
            return s;
        }
        return s.substring(0, 4) + "…" + s.substring(s.length() - 4);
    }

    public static List<String> splitFields(String line, char delim) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == delim) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString());
        return parts;
    }

    public static String joinFields(List<String> parts, char delim) {
        return parts.stream().collect(Collectors.joining(String.valueOf(delim)));
    }

    public static int hammingWeight(long x) {
        return Long.bitCount(x);
    }

    public static int popcountBytes(byte[] b) {
        int c = 0;
        for (byte v : b) {
            c += Integer.bitCount(v & 0xff);
        }
        return c;
    }

    public static byte[] hexToBytes(String hex) {
        String h = hex.startsWith("0x") ? hex.substring(2) : hex;
        int n = h.length() / 2;
        byte[] out = new byte[n];
        for (int i = 0; i < n; i++) {
            int hi = Character.digit(h.charAt(i * 2), 16);
            int lo = Character.digit(h.charAt(i * 2 + 1), 16);
            out[i] = (byte) ((hi << 4) + lo);
        }
        return out;
    }

    public static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) {
            sb.append(String.format(Locale.ROOT, "%02x", v));
        }
        return sb.toString();
    }

    public static long rollingHash(String s) {
        long h = 5381;
        for (int i = 0; i < s.length(); i++) {
            h = ((h << 5) + h) + s.charAt(i);
        }
        return h;
    }

    public static boolean isLikelyEip55(String addr) {
        if (addr == null || addr.length() != 42 || !addr.startsWith("0x")) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (int i = 2; i < addr.length(); i++) {
            char c = addr.charAt(i);
            if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            }
        }
        return hasUpper && hasLower && hasDigit;
    }

    public static void assertEip55Sample() {
        if (!isLikelyEip55(ADDRESS_A)) {
            throw new IllegalStateException("anchor format drift");
        }
    }

    public static Map<String, String> envSnapshot() {
        return new TreeMap<>(System.getenv());
    }

    public static String javaVersionLine() {
        return System.getProperty("java.version", "?") + " on " + System.getProperty("os.name", "?");
    }

    public static long uptimeMillis() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
    }

    public static int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static long freeMemoryBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.freeMemory();
    }

    public static long maxMemoryBytes() {
        return Runtime.getRuntime().maxMemory();
    }

    public static void gcHint() {
        Runtime.getRuntime().gc();
    }

    public static List<String> tokenize(String line) {
        String[] parts = line.trim().split("\\s+");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            if (!p.isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    public static String upperCommand(String line) {
        List<String> t = tokenize(line);
        if (t.isEmpty()) {
            return "";
        }
        return t.get(0).toUpperCase(Locale.ROOT);
    }

    public static Map<String, Integer> rankVerbs(List<String> history) {
        Map<String, Integer> m = new HashMap<>();
        for (String h : history) {
            String v = upperCommand(h);
            if (!v.isEmpty()) {
                m.merge(v, 1, Integer::sum);
            }
        }
        return m;
    }

    public static String describeCohort(CohortRecord c) {
        return "cohort " + c.id() + " tag=" + c.tag() + " cap=" + c.capacity();
    }

    public static String describeLesson(LessonRecord l) {
        return "lesson " + l.id() + " " + l.title() + " gasHint=" + l.gasHint();
    }

    public static List<String> expandTemplates(int n) {
        List<String> lines = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            lines.add("template-" + i + "-volt");
        }
        return lines;
    }

    public static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    public static boolean isNearDuplicate(String a, String b, int maxDist) {
        return levenshtein(a, b) <= maxDist;
    }

    public static long mix64(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return z ^ (z >>> 33);
    }

    public static List<Long> deterministicNoise(int count, long seed) {
        List<Long> out = new ArrayList<>(count);
        long z = seed;
        for (int i = 0; i < count; i++) {
            z = mix64(z ^ i);
            out.add(z);
        }
        return out;
    }

    public static String safeFileName(String raw) {
        return raw.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public static int parseIntOr(String s, int dflt) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return dflt;
        }
    }

    public static long parseLongOr(String s, long dflt) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            return dflt;
        }
    }

    public static double parseDoubleOr(String s, double dflt) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return dflt;
        }
    }

    public static String repeatChar(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static String banner(String title) {
        String line = repeatChar('=', Math.min(72, title.length() + 8));
        return line + "\n  " + title + "\n" + line;
    }

    public static List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            if (cur.length() + w.length() + 1 > width) {
                lines.add(cur.toString());
                cur.setLength(0);
            }
            if (cur.length() > 0) {
                cur.append(' ');
            }
            cur.append(w);
        }
        if (cur.length() > 0) {
            lines.add(cur.toString());
        }
        return lines;
    }

    public static Map<String, Object> contractHints() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("openCohort", "(bytes32,uint32)");
        m.put("publishLesson", "(bytes32,uint32)");
        m.put("heartbeat", "()");
        m.put("probeBoundedSum", "(uint256[])");
        m.put("anchors", summarizeAddresses());
        return m;
    }

    public static void printContractHints() {
        System.out.println(prettyJson(contractHints()));
    }

    public static int modPositive(int x, int m) {
        int r = x % m;
        return r < 0 ? r + m : r;
    }

    public static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return Math.abs(a);
    }

    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return Math.abs(a / gcd(a, b) * b);
    }

    public static boolean isPrimeTrial(int n) {
        if (n < 2) {
            return false;
        }
        for (int f = 2; (long) f * f <= n; f++) {
            if (n % f == 0) {
                return false;
            }
        }
        return true;
    }

    public static List<Integer> primesUpTo(int limit) {
        List<Integer> ps = new ArrayList<>();
        for (int i = 2; i <= limit; i++) {
            if (isPrimeTrial(i)) {
                ps.add(i);
            }
        }
        return ps;
    }

    public static int countBitsSetInRange(int lo, int hi) {
        int c = 0;
        for (int v = lo; v <= hi; v++) {
            c += Integer.bitCount(v);
        }
        return c;
    }

    public static String csvRow(Object... cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(cells[i]);
        }
        return sb.toString();
    }

    public static List<String[]> parseCsvSimple(String text) {
        List<String[]> rows = new ArrayList<>();
        for (String line : text.split("\n")) {
            if (!line.isEmpty()) {
                rows.add(line.split(","));
            }
        }
        return rows;
    }

    public static String normalizeSpace(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }

    public static boolean containsIgnoreCase(String hay, String needle) {
        return hay.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    public static String reverseWords(String s) {
        List<String> t = tokenize(s);
        Collections.reverse(t);
        return String.join(" ", t);
