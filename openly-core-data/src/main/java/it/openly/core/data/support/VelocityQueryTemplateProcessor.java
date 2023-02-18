package it.openly.core.data.support;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import it.openly.core.data.ITemplateProcessor;
import it.openly.core.data.ProcessedTemplate;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * ITemplateProcessor implementation backed by Velocity
 *
 * @author filippo.possenti
 */
public class VelocityQueryTemplateProcessor implements ITemplateProcessor {
	private VelocityEngine velocityEngine;

	public VelocityQueryTemplateProcessor(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}
	
	public VelocityQueryTemplateProcessor() {
		this(new VelocityEngine());
	}

	/**
	 * Processes the template
	 * @param templateText The template text
	 * @param context A {@link java.util.Map Map} containing variables and methods that will be used by Velocity during processing.
	 * @return The processed template
	 */
	@Override
	@SneakyThrows
	public ProcessedTemplate processTemplate(@NonNull String templateText, Map<String, Object> context) {
		ProcessedTemplate processedTemplate = new ProcessedTemplate();
		try(StringWriter writer = new StringWriter();
			StringReader reader = new StringReader(templateText)
		) {
			velocityEngine.evaluate(new VelocityContext(context), writer, "template", reader);
			processedTemplate.setSql(writer.toString());
			processedTemplate.setContext(context);
			return processedTemplate;
		}
	}
}
