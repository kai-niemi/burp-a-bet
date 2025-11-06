package io.cockroachdb.betting.common.util;

public abstract class AsciiArt {
    private AsciiArt() {
    }

    public static String happy() {
        return "٩(^‿^)۶";
    }

    public static String shrug() {
        return "¯\\_(ツ)_/¯";
    }

    public static String flipTableGently() {
        return "(╯°□°)╯︵ ┻━┻";
    }

    public static String flipTableRoughly() {
        return "(ノಠ益ಠ)ノ彡┻━┻";
    }
}
