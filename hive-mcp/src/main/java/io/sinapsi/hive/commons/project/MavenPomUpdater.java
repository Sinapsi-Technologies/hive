package io.sinapsi.hive.commons.project;

import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.support.HiveVersions;
import io.sinapsi.hive.commons.template.TemplateRenderer;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public final class MavenPomUpdater {
    private static final String HIVE_GROUP_ID = "io.sinapsi.hive";
    private static final String ARCHUNIT_GROUP_ID = "com.tngtech.archunit";

    private final TemplateRenderer templates = new TemplateRenderer();

    public Path ensureBasePom(Path projectRoot, HiveConfig config) throws IOException {
        Path pom = projectRoot.resolve("pom.xml");
        if (!Files.exists(pom)) {
            Files.writeString(pom, minimalPom(config));
        }
        ensureDependency(projectRoot, HIVE_GROUP_ID, "hive-core", HiveVersions.hive(), null);
        return pom;
    }

    public void ensureValidatorDependency(Path projectRoot, HiveConfig config) throws IOException {
        ensureBasePom(projectRoot, config);
        ensureDependency(projectRoot, HIVE_GROUP_ID, "hive-validator", HiveVersions.hive(), null);
    }

    public void ensureArchUnitDependencies(Path projectRoot, HiveConfig config) throws IOException {
        ensureBasePom(projectRoot, config);
        ensureDependency(projectRoot, HIVE_GROUP_ID, "hive-archunit", HiveVersions.hive(), "test");
        ensureDependency(projectRoot, ARCHUNIT_GROUP_ID, "archunit-junit5", HiveVersions.archUnit(), "test");
    }

    public boolean ensureDependency(Path projectRoot, String groupId, String artifactId, String version, String scope)
            throws IOException {
        Path pom = projectRoot.resolve("pom.xml");
        Document document = parse(pom);
        if (containsDependency(document, groupId, artifactId)) {
            return false;
        }

        Element project = document.getDocumentElement();
        NodeList dependencyContainers = project.getElementsByTagName("dependencies");
        Element dependencies = dependencyContainers.getLength() > 0
                ? (Element) dependencyContainers.item(0)
                : document.createElement("dependencies");
        if (dependencyContainers.getLength() == 0) {
            project.appendChild(dependencies);
        }

        Element dependency = document.createElement("dependency");
        appendElement(document, dependency, "groupId", groupId);
        appendElement(document, dependency, "artifactId", artifactId);
        appendElement(document, dependency, "version", version);
        if (scope != null && !scope.isBlank()) {
            appendElement(document, dependency, "scope", scope);
        }
        dependencies.appendChild(dependency);
        writeDocument(pom, document);
        return true;
    }

    private boolean containsDependency(Document document, String groupId, String artifactId) {
        NodeList dependencies = document.getElementsByTagName("dependency");
        for (int index = 0; index < dependencies.getLength(); index++) {
            Element dependency = (Element) dependencies.item(index);
            if (text(dependency, "groupId").equals(groupId) && text(dependency, "artifactId").equals(artifactId)) {
                return true;
            }
        }
        return false;
    }

    private Document parse(Path pom) throws IOException {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom.toFile());
        } catch (Exception exception) {
            throw new IOException("Unable to parse pom.xml", exception);
        }
    }

    private void appendElement(Document document, Element parent, String name, String value) {
        Element element = document.createElement(name);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    private String text(Element parent, String name) {
        NodeList nodes = parent.getElementsByTagName(name);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent().trim();
    }

    private void writeDocument(Path pom, Document document) throws IOException {
        try {
            removeWhitespaceNodes(document.getDocumentElement());
            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter output = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(output));
            Files.writeString(pom, output.toString());
        } catch (Exception exception) {
            throw new IOException("Unable to write pom.xml", exception);
        }
    }

    private void removeWhitespaceNodes(Node node) {
        for (Node child = node.getFirstChild(); child != null; ) {
            Node next = child.getNextSibling();
            if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().isBlank()) {
                node.removeChild(child);
            } else if (child.hasChildNodes()) {
                removeWhitespaceNodes(child);
            }
            child = next;
        }
    }

    private String minimalPom(HiveConfig config) throws IOException {
        return templates.render(
                "project/pom.xml.ftl",
                java.util.Map.of(
                        "groupId", config.basePackage(),
                        "artifactId", "hive-app",
                        "version", "0.1.0-SNAPSHOT",
                        "javaVersion", "21"
                )
        );
    }
}
