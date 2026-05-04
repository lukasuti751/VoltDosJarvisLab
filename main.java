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
