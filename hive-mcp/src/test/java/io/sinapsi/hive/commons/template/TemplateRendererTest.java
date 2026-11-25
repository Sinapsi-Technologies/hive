package io.sinapsi.hive.commons.template;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateRendererTest {

    @Test
    void rendersNamedTemplateWithParameters() throws Exception {

        String rendered = new TemplateRenderer().render(
                "domain/value-object.java.ftl",
                Map.of(
                        "packageName", "com.example.domain",
                        "className", "Email",
                        "valueType", "String",
                        "imports", List.of(),
                        "notNull", false,
                        "notBlank", false,
                        "factoryMethod", ""
                )
        );

        assertEquals("""
                package com.example.domain;


                public record Email(String value) {

                }
                """, rendered);
    }
}
