package it.openly.core.data.support;

import it.openly.core.data.ProcessedTemplate;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class SimpleQueryTemplateProcessorTest {

    SimpleQueryTemplateProcessor simpleQueryTemplateProcessor = new SimpleQueryTemplateProcessor(true);

    @SneakyThrows
    private static Stream<Arguments> provideArguments() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/simplequerytemplateprocessortest/templatequeries/*.txt");
        return Stream.of(resources).map(Arguments::of);
    }

    private Map<String, Object> prepareContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("key01", "value01");
        context.put("key02", "value02");
        context.put("key03", 3);
        context.put("key04", true);
        context.put("key05", "value05");
        context.put("key06", "value06");
        context.put("key07", 6);
        context.put("key08", false);
        context.put("key10", "value10");
        context.put("nvkey", null);
        context.put("key13", "value13");
        context.put("key14", "value14");
        context.put("key15", 5345L);
        context.put("key16", false);
        context.put("key17", new Object[] { "value17a", "value17b" });
        context.put("key18", Arrays.asList("value18a", "value18b", "value18c"));
        return context;
    }

    @SneakyThrows
    private String loadInputTemplateText(Resource resource) {
        return IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
    }

    @SneakyThrows
    private String loadExpectationText(Resource resource) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] exp = resolver.getResources("classpath*:/simplequerytemplateprocessortest/templatequeries/"+ resource.getFilename() + ".expectation");
        return IOUtils.toString(exp[0].getInputStream(), Charset.defaultCharset());
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    @DisplayName("Processing of queries using the simple embedded engine")
    void testProcessTemplate(Resource inputTemplateResource) {
        // Given: an input template and an expected result as well as a bunch key/values as context for the query
        String inputTemplate = loadInputTemplateText(inputTemplateResource);
        String expectedOutputString = loadExpectationText(inputTemplateResource);
        Map<String, Object> context = prepareContext();

        // When: I process the template
        ProcessedTemplate processedTemplate = simpleQueryTemplateProcessor.processTemplate(inputTemplate, context);

        // Then: the produced template text matches the expectation
        assertEquals(expectedOutputString, processedTemplate.getSql());
    }
}
