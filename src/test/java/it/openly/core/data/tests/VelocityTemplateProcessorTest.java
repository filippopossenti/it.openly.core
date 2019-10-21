package it.openly.core.data.tests;

import it.openly.core.data.support.VelocityTemplateProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static it.openly.core.data.tests.TestUtils.map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class VelocityTemplateProcessorTest {

    @Test
    public void testProcessTemplate() {
        // given
        VelocityTemplateProcessor processor = new VelocityTemplateProcessor();

        // when
        String result = processor.processTemplate("Hello ${name}", map("name", "world!"));

        // then
        assertThat(result, is("Hello world!"));

    }
}
