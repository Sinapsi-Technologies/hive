package io.sinapsi.hive.mcp.index;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@FunctionalInterface
public interface ProjectClasspathResolver {

    List<Path> resolve(Path projectRoot) throws IOException, InterruptedException;
}
