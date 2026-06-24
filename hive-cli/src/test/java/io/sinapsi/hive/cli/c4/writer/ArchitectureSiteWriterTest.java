package io.sinapsi.hive.cli.c4.writer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureSiteWriterTest {
    private final ArchitectureSiteWriter writer = new ArchitectureSiteWriter();

    @Test
    void rendersHeaderMetadataAndHexagonFlow() {
        String html = writer.render("com.example.app", "com.example.app", "2026-06-24 10:00", List.of());

        assertTrue(html.contains("<title>com.example.app — Hive Architecture Map</title>"), html);
        assertTrue(html.contains("com.example.app"), html);
        assertTrue(html.contains("2026-06-24 10:00"), html);
        assertTrue(html.contains("Inbound Adapter"), html);
        assertTrue(html.contains("Outbound Adapter"), html);
        assertTrue(html.contains("→"), html);
    }

    @Test
    void inlinesSvgWhenAvailable() {
        ArchitectureSiteWriter.DiagramEntry entry = new ArchitectureSiteWriter.DiagramEntry(
                "Todo Module — C3 Component Diagram",
                "c3-todo.puml",
                "c3-todo.svg",
                "<svg id=\"diagram\"></svg>");

        String html = writer.render("com.example.app", "com.example.app", "now", List.of(entry));

        assertTrue(html.contains("<svg id=\"diagram\"></svg>"), html);
        assertTrue(html.contains("c3-todo.puml"), html);
    }

    @Test
    void showsPlaceholderWhenNotRendered() {
        ArchitectureSiteWriter.DiagramEntry entry = new ArchitectureSiteWriter.DiagramEntry(
                "Todo Module — C3 Component Diagram", "c3-todo.puml", null, null);

        String html = writer.render("com.example.app", "com.example.app", "now", List.of(entry));

        assertTrue(html.contains("placeholder"), html);
        assertTrue(html.contains("--render"), html);
    }

    @Test
    void linksImageWhenPngOnly() {
        ArchitectureSiteWriter.DiagramEntry entry = new ArchitectureSiteWriter.DiagramEntry(
                "Todo Module — C3 Component Diagram", "c3-todo.puml", "c3-todo.png", null);

        String html = writer.render("com.example.app", "com.example.app", "now", List.of(entry));

        assertTrue(html.contains("src=\"c3-todo.png\""), html);
    }

    @Test
    void stripsXmlPrologFromSvg() {
        String svg = "<?xml version=\"1.0\"?>\n<!DOCTYPE svg>\n<svg><rect/></svg>";
        assertEquals("<svg><rect/></svg>", ArchitectureSiteWriter.stripSvgProlog(svg));
    }

    @Test
    void keepsSvgWhenAlreadyClean() {
        assertEquals("<svg></svg>", ArchitectureSiteWriter.stripSvgProlog("<svg></svg>"));
    }
}
