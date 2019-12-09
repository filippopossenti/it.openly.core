package it.openly.core.data.tests;

import it.openly.core.data.ProcessedTemplate;
import it.openly.core.data.support.VelocityQueryTemplateProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static it.openly.core.data.tests.TestUtils.map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class VelocityQueryTemplateProcessorTest {

    @Test
    public void testProcessTemplate() {
        // given
        VelocityQueryTemplateProcessor processor = new VelocityQueryTemplateProcessor();

        // when
        ProcessedTemplate processedTemplate = processor.processTemplate("Hello ${name}", map("name", "world!"));

        // then
        assertThat(processedTemplate.getSql(), is("Hello world!"));

    }
}
