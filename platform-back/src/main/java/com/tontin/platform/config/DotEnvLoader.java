package com.tontin.platform.config;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads a {@code .env} file into {@link System#setProperty} so Spring can resolve
 * {@code ${VAR}} placeholders when the process was not started with exported variables.
 * Does not override existing environment variables or system properties.
 */
public final class DotEnvLoader {

    private DotEnvLoader() {}

    public static void loadIfPresent() {
        String[] dirs = {".", "platform-back", "../platform-back"};
        for (String dir : dirs) {
            Path envFile = Path.of(dir, ".env");
            if (!Files.isRegularFile(envFile)) {
                continue;
            }
            try {
                for (String raw : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
                    String line = raw.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eq = line.indexOf('=');
                    if (eq <= 0) {
                        continue;
                    }
                    String key = line.substring(0, eq).trim();
                    if (key.isEmpty()) {
                        continue;
                    }
                    String value = line.substring(eq + 1).trim();
                    if (
                        value.length() >= 2 &&
                        ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'")))
                    ) {
                        value = value.substring(1, value.length() - 1);
                    }
                    if (System.getenv(key) == null && System.getProperty(key) == null) {
                        System.setProperty(key, value);
                    }
                }
                return;
            } catch (Exception ignored) {
                // try next candidate directory
            }
        }
    }
}
