package io.sinapsi.hive.commons.template;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/** Renders named FreeMarker templates stored under {@code src/main/resources/templates}. */
public final class TemplateRenderer {

    private final Configuration configuration;

    public TemplateRenderer() {

        configuration = new Configuration(Configuration.VERSION_2_3_35);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateLoader(new ClassTemplateLoader(getClass(), "/templates"));
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
    }

    public String render(String templateName, Map<String, ?> model) throws IOException {

        Template template = configuration.getTemplate(templateName);
        StringWriter output = new StringWriter();

        try {
            template.process(model, output);
        } catch (TemplateException exception) {
            throw new IOException("Unable to render template: " + templateName, exception);
        }

        return output.toString();
    }
}
