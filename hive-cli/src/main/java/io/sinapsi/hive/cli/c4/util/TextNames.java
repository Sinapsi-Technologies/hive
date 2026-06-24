package io.sinapsi.hive.cli.c4.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Small, deterministic name transformations shared by the scanner, writers, and command:
 * a module name like {@code orderFulfilment} or {@code order-fulfilment} becomes the slug
 * {@code order-fulfilment} (filenames), the Pascal id {@code OrderFulfilment} (diagram id),
 * or the title {@code Order Fulfilment} (human text).
 */
public final class TextNames {
    private TextNames() {
    }

    /** Filesystem- and URL-safe lower-kebab form, e.g. {@code c3-order-fulfilment.puml}. */
    public static String slug(String raw) {
        List<String> words = words(raw);
        return words.isEmpty() ? "module" : String.join("-", words);
    }

    /** PascalCase form used inside PlantUML ids, e.g. {@code @startuml C3_OrderFulfilment}. */
    public static String pascalCase(String raw) {
        List<String> words = words(raw);
        if (words.isEmpty()) {
            return "Module";
        }
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(capitalize(word));
        }
        return builder.toString();
    }

    /** Human title, e.g. {@code Order Fulfilment}. */
    public static String title(String raw) {
        List<String> words = words(raw);
        if (words.isEmpty()) {
            return "Module";
        }
        List<String> capitalized = new ArrayList<>(words.size());
        for (String word : words) {
            capitalized.add(capitalize(word));
        }
        return String.join(" ", capitalized);
    }

    private static String capitalize(String word) {
        if (word.isEmpty()) {
            return word;
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    private static List<String> words(String raw) {
        List<String> words = new ArrayList<>();
        if (raw == null) {
            return words;
        }
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                flush(words, current);
                continue;
            }
            if (Character.isUpperCase(c) && current.length() > 0) {
                char prev = raw.charAt(i - 1);
                if (Character.isLowerCase(prev) || Character.isDigit(prev)) {
                    flush(words, current);
                }
            }
            current.append(Character.toLowerCase(c));
        }
        flush(words, current);
        return words;
    }

    private static void flush(List<String> words, StringBuilder current) {
        if (current.length() > 0) {
            words.add(current.toString());
            current.setLength(0);
        }
    }
}
