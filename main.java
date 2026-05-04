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

