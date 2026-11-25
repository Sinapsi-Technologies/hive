package io.sinapsi.hive.mcp.index;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public final class MavenProjectClasspathResolver implements ProjectClasspathResolver {
    private static final Logger log = LoggerFactory.getLogger(MavenProjectClasspathResolver.class);

    @Override
    public List<Path> resolve(Path projectRoot) throws IOException, InterruptedException {
        Path output = projectRoot.resolve(".hive/index/maven-classpath.txt");
        Path pom = projectRoot.resolve("pom.xml");
        Path marker = projectRoot.resolve(".hive/index/maven-classpath.pom-mtime");
        Files.createDirectories(output.getParent());
        if (Files.exists(output) && Files.exists(marker) && Files.exists(pom)
                && Files.readString(marker, StandardCharsets.UTF_8).trim()
                .equals(Long.toString(Files.getLastModifiedTime(pom).toMillis()))) {
            log.debug("Using cached Maven classpath for {}", projectRoot);
            return readClasspath(output);
        }
        log.info("Resolving Maven classpath for {} (timeout=120s)", projectRoot);
        Process process = new ProcessBuilder(
                "mvn", "-q", "dependency:build-classpath", "-Dmdep.outputFile=" + output)
                .directory(projectRoot.toFile())
                .redirectErrorStream(true)
                .start();
        if (!process.waitFor(120, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            log.warn("Maven classpath resolution timed out for {}", projectRoot);
            throw new IOException("Maven classpath resolution timed out");
        }
        if (process.exitValue() != 0 || !Files.exists(output)) {
            return List.of();
        }
        if (Files.exists(pom)) {
            Files.writeString(marker, Long.toString(Files.getLastModifiedTime(pom).toMillis()), StandardCharsets.UTF_8);
        }
        return readClasspath(output);
    }

    private List<Path> readClasspath(Path output) throws IOException {
        String classpath = Files.readString(output, StandardCharsets.UTF_8).trim();
        if (classpath.isBlank()) {
            return List.of();
        }
        return Arrays.stream(classpath.split(java.util.regex.Pattern.quote(java.io.File.pathSeparator)))
                .map(Path::of)
                .filter(Files::isRegularFile)
                .toList();
    }
}
