package it.openly.core.data.support;

import it.openly.core.data.ProcessedTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static it.openly.core.test.TestUtils.map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VelocityQueryTemplateProcessorTest {

    @Test
    @DisplayName("Processing of queries using Velocity")
    void testProcessTemplate() {
        // Given: a template to process, an expected output and some input arguments
        String template = "Hello ${name}";
        String expectation = "Hello world!";
        Map<String, Object> context = map("name", "world!");

        // When: I use the VelocityQueryTemplateProcessor class to process the template
        VelocityQueryTemplateProcessor processor = new VelocityQueryTemplateProcessor();
        ProcessedTemplate processedTemplate = processor.processTemplate(template, context);

        // Then: the resulting query string matches the expectation
        assertEquals(expectation, processedTemplate.getSql());

    }
}
