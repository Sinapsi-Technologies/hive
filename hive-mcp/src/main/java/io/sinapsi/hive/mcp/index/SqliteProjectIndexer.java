package io.sinapsi.hive.mcp.index;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Component
public final class SqliteProjectIndexer {

    private final ProjectClasspathResolver classpathResolver;

    @Autowired
    public SqliteProjectIndexer() {
        this(new MavenProjectClasspathResolver());
    }

    public SqliteProjectIndexer(ProjectClasspathResolver classpathResolver) {
        this.classpathResolver = classpathResolver;
    }

    public ProjectIndexSummary index(Path projectRoot) throws IOException, SQLException {
        Path root = projectRoot.toAbsolutePath().normalize();
        Path database = root.resolve(".hive/index/hive.db");
        Files.createDirectories(database.getParent());

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            connection.setAutoCommit(false);
            createSchema(connection);
            ScanCounts counts = scanSources(connection, root);
            connection.commit();
            return summary(connection, database);
        }
    }

    public ProjectIndexSummary indexFiles(Path projectRoot, List<String> relativePaths) throws IOException, SQLException {
        Path root = projectRoot.toAbsolutePath().normalize();
        Path database = root.resolve(".hive/index/hive.db");
        Files.createDirectories(database.getParent());

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            connection.setAutoCommit(false);
            createSchema(connection);
            JavaParser parser = parser(root);
            ScanCounts counts = new ScanCounts();
            for (String relativePath : relativePaths == null ? List.<String>of() : relativePaths) {
                if (relativePath == null || relativePath.isBlank()) {
                    continue;
                }
                Path source = root.resolve(relativePath).normalize();
                if (!source.startsWith(root) || !isJavaSource(source)) {
                    throw new IllegalArgumentException("Indexed path must be a Java source inside the project root: " + relativePath);
                }
                if (Files.exists(source)) {
                    indexSource(connection, parser, root, source, counts);
                } else {
                    deleteFileIndex(connection, root.relativize(source).toString());
                }
            }
            connection.commit();
            return summary(connection, database);
        }
    }

    public ProjectIndexSummary summary(Path projectRoot) throws SQLException {
        Path database = projectRoot.toAbsolutePath().normalize().resolve(".hive/index/hive.db");
        if (!Files.exists(database)) {
            return new ProjectIndexSummary(database.toString(), 0, 0, 0, 0, 0);
        }
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            return summary(connection, database);
        }
    }

    private ProjectIndexSummary summary(Connection connection, Path database) throws SQLException {
        return new ProjectIndexSummary(database.toString(), count(connection, "files"), count(connection, "symbols"),
                count(connection, "methods"), count(connection, "imports"), count(connection, "relations"));
    }

    public List<ProjectRelation> relations(Path projectRoot, String symbolName) throws SQLException {
        Path database = projectRoot.toAbsolutePath().normalize().resolve(".hive/index/hive.db");
        if (!Files.exists(database)) {
            return List.of();
        }
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                PreparedStatement statement = connection.prepareStatement(
                        "select s.qualified_name, r.relation_type, r.target_name "
                                + "from relations r join symbols s on s.id = r.source_symbol_id "
                                + "where s.simple_name = ? order by r.relation_type, r.target_name")) {
            statement.setString(1, symbolName);
            try (ResultSet result = statement.executeQuery()) {
                List<ProjectRelation> relations = new ArrayList<>();
                while (result.next()) {
                    relations.add(new ProjectRelation(result.getString(1), result.getString(2), result.getString(3)));
                }
                return List.copyOf(relations);
            }
        }
    }

    public List<ProjectRelation> usedBy(Path projectRoot, String targetName) throws SQLException {
        Path database = projectRoot.toAbsolutePath().normalize().resolve(".hive/index/hive.db");
        if (!Files.exists(database)) {
            return List.of();
        }
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                PreparedStatement statement = connection.prepareStatement(
                        "select s.qualified_name, r.relation_type, r.target_name "
                                + "from relations r join symbols s on s.id = r.source_symbol_id "
                                + "where r.relation_type = 'CALLS_RESOLVED' "
                                + "and (r.target_name = ? or r.target_name like ?) "
                                + "order by s.qualified_name")) {
            statement.setString(1, targetName);
            statement.setString(2, "%" + targetName + "(%");
            try (ResultSet result = statement.executeQuery()) {
                List<ProjectRelation> relations = new ArrayList<>();
                while (result.next()) {
                    relations.add(new ProjectRelation(result.getString(1), result.getString(2), result.getString(3)));
                }
                return List.copyOf(relations);
            }
        }
    }

    public List<ProjectRelation> importedBy(Path projectRoot, String targetName) throws SQLException {
        Path database = projectRoot.toAbsolutePath().normalize().resolve(".hive/index/hive.db");
        if (!Files.exists(database)) {
            return List.of();
        }
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                PreparedStatement statement = connection.prepareStatement(
                        "select s.qualified_name, r.relation_type, r.target_name "
                                + "from relations r join symbols s on s.id = r.source_symbol_id "
                                + "where r.relation_type = 'IMPORTS' "
                                + "and (r.target_name = ? or r.target_name like ?) "
                                + "order by s.qualified_name")) {
            statement.setString(1, targetName);
            statement.setString(2, "%." + targetName);
            try (ResultSet result = statement.executeQuery()) {
                List<ProjectRelation> relations = new ArrayList<>();
                while (result.next()) {
                    relations.add(new ProjectRelation(result.getString(1), result.getString(2), result.getString(3)));
                }
                return List.copyOf(relations);
            }
        }
    }

    public SourceContext sourceContext(Path projectRoot, String symbolName, int linesAround) throws IOException, SQLException {
        Path root = projectRoot.toAbsolutePath().normalize();
        Path database = root.resolve(".hive/index/hive.db");
        if (!Files.exists(database)) {
            return null;
        }
        String simpleName = symbolName.contains(".")
                ? symbolName.substring(symbolName.lastIndexOf('.') + 1)
                : symbolName;
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                PreparedStatement statement = connection.prepareStatement(
                        "select f.path from symbols s join files f on f.id=s.file_id where s.simple_name=? limit 1")) {
            statement.setString(1, simpleName);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                Path file = root.resolve(result.getString(1));
                List<String> lines = Files.readAllLines(file);
                int line = findSymbolLine(lines, simpleName);
                int radius = Math.max(0, linesAround);
                int start = Math.max(0, line - 1 - radius);
                int end = Math.min(lines.size(), line + radius);
                return new SourceContext(root.relativize(file).toString(), start + 1, end,
                        String.join("\n", lines.subList(start, end)));
            }
        }
    }

    private int findSymbolLine(List<String> lines, String symbolName) {
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.matches(".*\\b(class|interface|record|enum)\\s+" + java.util.regex.Pattern.quote(symbolName) + "\\b.*")
                    || line.matches(".*\\b" + java.util.regex.Pattern.quote(symbolName) + "\\s*\\(.*")) {
                return index + 1;
            }
        }
        return 1;
    }

    private void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("create table if not exists files (id integer primary key, path text not null unique, sha256 text not null)");
            statement.executeUpdate("create table if not exists symbols (id integer primary key, file_id integer not null, qualified_name text not null, simple_name text not null, kind text not null)");
            statement.executeUpdate("create table if not exists methods (id integer primary key, symbol_id integer not null, name text not null, signature text not null)");
            statement.executeUpdate("create table if not exists imports (id integer primary key, file_id integer not null, imported_name text not null)");
            statement.executeUpdate("create table if not exists relations (id integer primary key, source_symbol_id integer not null, target_name text not null, relation_type text not null)");
            statement.executeUpdate("create index if not exists idx_symbols_name on symbols(simple_name)");
            statement.executeUpdate("create index if not exists idx_methods_name on methods(name)");
            statement.executeUpdate("create index if not exists idx_relations_source on relations(source_symbol_id)");
        }
    }

    private ScanCounts scanSources(Connection connection, Path root) throws IOException, SQLException {
        ScanCounts counts = new ScanCounts();
        JavaParser parser = parser(root);
        Set<String> currentPaths = new HashSet<>();
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path source : paths.filter(this::isJavaSource).toList()) {
                String relativePath = root.relativize(source).toString();
                currentPaths.add(relativePath);
                String hash = sha256(source);
                if (hash.equals(existingHash(connection, relativePath))) {
                    counts.skipped++;
                    continue;
                }
                indexSource(connection, parser, root, source, counts);
            }
        }
        removeDeletedFiles(connection, currentPaths);
        return counts;
    }

    private void indexSource(Connection connection, JavaParser parser, Path root, Path source, ScanCounts counts)
            throws IOException, SQLException {
        String relativePath = root.relativize(source).toString();
        String hash = sha256(source);
        if (hash.equals(existingHash(connection, relativePath))) {
            counts.skipped++;
            return;
        }
        deleteFileIndex(connection, relativePath);
        CompilationUnit unit;
        try {
            unit = parser.parse(source).getResult().orElseThrow();
        } catch (RuntimeException | IOException ignored) {
            return;
        }

        long fileId = insertFile(connection, source.getParent() == null ? source.getFileName() : root.relativize(source), hash);
        counts.files++;
        for (var importDeclaration : unit.getImports()) {
            insertImport(connection, fileId, importDeclaration.getNameAsString());
            counts.imports++;
        }
        for (TypeDeclaration<?> type : unit.findAll(TypeDeclaration.class)) {
            long symbolId = insertSymbol(connection, fileId, unit, type);
            counts.symbols++;
            for (var importDeclaration : unit.getImports()) {
                insertRelation(connection, symbolId, importDeclaration.getNameAsString(), "IMPORTS");
                counts.relations++;
            }
            for (var relation : inheritanceRelations(type)) {
                insertRelation(connection, symbolId, relation.target(), relation.type());
                counts.relations++;
            }
            for (MethodDeclaration method : type.findAll(MethodDeclaration.class)) {
                if (method.findAncestor(TypeDeclaration.class).orElse(null) == type) {
                    insertMethod(connection, symbolId, method);
                    counts.methods++;
                    for (MethodCallExpr call : method.findAll(MethodCallExpr.class)) {
                        Relation relation = resolveCall(call);
                        insertRelation(connection, symbolId, relation.target(), relation.type());
                        counts.relations++;
                    }
                }
            }

            if (type.getNameAsString().endsWith("Test")) {
                String covered = type.getNameAsString().substring(0, type.getNameAsString().length() - 4);
                insertRelation(connection, symbolId, covered, "TEST_COVERS");
                counts.relations++;
            }
        }
    }

    private String existingHash(Connection connection, String relativePath) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select sha256 from files where path = ?")) {
            statement.setString(1, relativePath);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        }
    }

    private void deleteFileIndex(Connection connection, String relativePath) throws SQLException {
        try (PreparedStatement file = connection.prepareStatement("select id from files where path = ?")) {
            file.setString(1, relativePath);
            try (ResultSet result = file.executeQuery()) {
                if (!result.next()) {
                    return;
                }
                long fileId = result.getLong(1);
                try (PreparedStatement symbols = connection.prepareStatement("select id from symbols where file_id = ?")) {
                    symbols.setLong(1, fileId);
                    try (ResultSet symbolRows = symbols.executeQuery()) {
                        List<Long> symbolIds = new ArrayList<>();
                        while (symbolRows.next()) {
                            symbolIds.add(symbolRows.getLong(1));
                        }
                        for (long symbolId : symbolIds) {
                            try (PreparedStatement deleteRelations = connection.prepareStatement("delete from relations where source_symbol_id = ?")) {
                                deleteRelations.setLong(1, symbolId);
                                deleteRelations.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement deleteMethods = connection.prepareStatement("delete from methods where symbol_id in (select id from symbols where file_id = ?)")) {
                    deleteMethods.setLong(1, fileId);
                    deleteMethods.executeUpdate();
                }
                try (PreparedStatement deleteSymbols = connection.prepareStatement("delete from symbols where file_id = ?")) {
                    deleteSymbols.setLong(1, fileId);
                    deleteSymbols.executeUpdate();
                }
                try (PreparedStatement deleteImports = connection.prepareStatement("delete from imports where file_id = ?")) {
                    deleteImports.setLong(1, fileId);
                    deleteImports.executeUpdate();
                }
                try (PreparedStatement deleteFile = connection.prepareStatement("delete from files where id = ?")) {
                    deleteFile.setLong(1, fileId);
                    deleteFile.executeUpdate();
                }
            }
        }
    }

    private void removeDeletedFiles(Connection connection, Set<String> currentPaths) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("select path from files")) {
            List<String> deleted = new ArrayList<>();
            while (result.next()) {
                if (!currentPaths.contains(result.getString(1))) {
                    deleted.add(result.getString(1));
                }
            }
            for (String path : deleted) {
                deleteFileIndex(connection, path);
            }
        }
    }

    private JavaParser parser(Path root) throws IOException {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        Path mainSources = root.resolve("src/main/java");
        Path testSources = root.resolve("src/test/java");
        if (Files.isDirectory(mainSources)) {
            typeSolver.add(new JavaParserTypeSolver(mainSources));
        }
        if (Files.isDirectory(testSources)) {
            typeSolver.add(new JavaParserTypeSolver(testSources));
        }
        try {
            for (Path jar : classpathResolver.resolve(root)) {
                try {
                    typeSolver.add(new JarTypeSolver(jar));
                } catch (IOException ignored) {
                    // Keep indexing source even when one external JAR cannot be read.
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        ParserConfiguration configuration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(typeSolver));
        return new JavaParser(configuration);
    }

    private Relation resolveCall(MethodCallExpr call) {
        try {
            return new Relation("CALLS_RESOLVED", call.resolve().getQualifiedSignature());
        } catch (RuntimeException exception) {
            String target = call.getScope()
                    .map(scope -> scope.toString() + "." + call.getNameAsString())
                    .orElse(call.getNameAsString());
            return new Relation("CALLS_UNRESOLVED", target);
        }
    }

    private boolean isJavaSource(Path path) {
        String value = path.toString();
        return value.endsWith(".java")
                && !value.contains("/target/")
                && !value.contains("/.git/")
                && !value.contains("/.hive/");
    }

    private long insertFile(Connection connection, Path path, String sha256) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into files(path, sha256) values (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, path.toString());
            statement.setString(2, sha256);
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    private long insertSymbol(Connection connection, long fileId, CompilationUnit unit, TypeDeclaration<?> type)
            throws SQLException {
        String packageName = unit.getPackageDeclaration().map(value -> value.getNameAsString() + ".").orElse("");
        String qualifiedName = packageName + type.getNameAsString();
        String kind = type instanceof ClassOrInterfaceDeclaration classOrInterface
                ? (classOrInterface.isInterface() ? "INTERFACE" : "CLASS")
                : type.getClass().getSimpleName().replace("Declaration", "").toUpperCase();
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into symbols(file_id, qualified_name, simple_name, kind) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, fileId);
            statement.setString(2, qualifiedName);
            statement.setString(3, type.getNameAsString());
            statement.setString(4, kind);
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    private void insertMethod(Connection connection, long symbolId, MethodDeclaration method) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into methods(symbol_id, name, signature) values (?, ?, ?)")) {
            statement.setLong(1, symbolId);
            statement.setString(2, method.getNameAsString());
            statement.setString(3, method.getDeclarationAsString().replace("\n", " ").trim());
            statement.executeUpdate();
        }
    }

    private void insertImport(Connection connection, long fileId, String importedName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into imports(file_id, imported_name) values (?, ?)")) {
            statement.setLong(1, fileId);
            statement.setString(2, importedName);
            statement.executeUpdate();
        }
    }

    private void insertRelation(Connection connection, long sourceSymbolId, String targetName, String relationType)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into relations(source_symbol_id, target_name, relation_type) values (?, ?, ?)")) {
            statement.setLong(1, sourceSymbolId);
            statement.setString(2, targetName);
            statement.setString(3, relationType);
            statement.executeUpdate();
        }
    }

    private List<Relation> inheritanceRelations(TypeDeclaration<?> type) {
        List<Relation> relations = new ArrayList<>();
        if (type instanceof ClassOrInterfaceDeclaration declaration) {
            declaration.getExtendedTypes().forEach(value -> relations.add(new Relation("EXTENDS", value.getNameAsString())));
            declaration.getImplementedTypes().forEach(value -> relations.add(new Relation("IMPLEMENTS", value.getNameAsString())));
        } else if (type instanceof EnumDeclaration declaration) {
            declaration.getImplementedTypes().forEach(value -> relations.add(new Relation("IMPLEMENTS", value.getNameAsString())));
        } else if (type instanceof RecordDeclaration declaration) {
            declaration.getImplementedTypes().forEach(value -> relations.add(new Relation("IMPLEMENTS", value.getNameAsString())));
        }
        return relations;
    }

    private long generatedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("SQLite did not return a generated key");
            }
            return keys.getLong(1);
        }
    }

    private long count(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("select count(*) from " + table)) {
            result.next();
            return result.getLong(1);
        }
    }

    private String sha256(Path path) throws IOException {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
        } catch (Exception exception) {
            throw new IOException("Unable to hash " + path, exception);
        }
    }

    private static final class ScanCounts {
        private long files;
        private long symbols;
        private long methods;
        private long imports;
        private long relations;
        private long skipped;
    }

    private record Relation(String type, String target) {}
}
