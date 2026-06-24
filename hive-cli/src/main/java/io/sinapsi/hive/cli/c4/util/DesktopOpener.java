package io.sinapsi.hive.cli.c4.util;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Opens a generated file in the OS default application for {@code --open}. Tries AWT
 * {@link Desktop} first, then falls back to the platform command ({@code open}, {@code xdg-open},
 * {@code cmd /c start}). Failures are reported, never fatal.
 */
public final class DesktopOpener {
    private DesktopOpener() {
    }

    /** @return empty on success, or a human-readable reason it could not open. */
    public static Optional<String> open(Path file) {
        Path target = file.toAbsolutePath();
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(target.toFile());
                return Optional.empty();
            }
        } catch (IOException | UnsupportedOperationException ignored) {
            // fall through to the platform command
        }
        return openWithCommand(target);
    }

    private static Optional<String> openWithCommand(Path target) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        List<String> command;
        if (os.contains("mac")) {
            command = List.of("open", target.toString());
        } else if (os.contains("win")) {
            command = List.of("cmd", "/c", "start", "", target.toString());
        } else {
            command = List.of("xdg-open", target.toString());
        }
        try {
            new ProcessBuilder(command).inheritIO().start();
            return Optional.empty();
        } catch (IOException exception) {
            return Optional.of("Could not open " + target.getFileName() + ": " + exception.getMessage());
        }
    }
}
