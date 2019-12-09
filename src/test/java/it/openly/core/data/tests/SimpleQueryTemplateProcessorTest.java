package it.openly.core.data.tests;

import it.openly.core.data.ProcessedTemplate;
import it.openly.core.data.support.SimpleQueryTemplateProcessor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class SimpleQueryTemplateProcessorTest {

    SimpleQueryTemplateProcessor simpleQueryTemplateProcessor = new SimpleQueryTemplateProcessor(true);

    @Parameterized.Parameters(name="{index}: {0}")
    @SneakyThrows
    public static Iterable<Object[]> data() {
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

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/simplequerytemplateprocessortest/templatequeries/*.txt");
        List<Object[]> data = new ArrayList<>();
        for(Resource r : resources) {
            System.out.println("Preparing scenario for query: " + r.getFilename());
            Resource[] exp = resolver.getResources("classpath*:/simplequerytemplateprocessortest/templatequeries/"+ r.getFilename() + ".expectation");
            String template = IOUtils.toString(r.getInputStream());
            String expectation = IOUtils.toString(exp[0].getInputStream());
            data.add(new Object[] { r.getFilename(), template, expectation, context });
        }
        return data;
    }

    private final String inputTemplate;
    private final String expectedOutputString;
    private final Map<String, Object> context;

    public SimpleQueryTemplateProcessorTest(String filename, String input, String output, Map<String, Object> ctx) {
        inputTemplate = input;
        expectedOutputString = output;
        context = ctx;
    }

    @Test
    public void testProcessTemplate() {
        // given

        // when
        ProcessedTemplate processedTemplate = simpleQueryTemplateProcessor.processTemplate(inputTemplate, context);

        // then
        assertThat(processedTemplate.getSql(), equalTo(expectedOutputString));
    }
}
