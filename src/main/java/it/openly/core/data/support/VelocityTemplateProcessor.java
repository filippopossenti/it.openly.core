package it.openly.core.data.support;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import it.openly.core.data.ITemplateProcessor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * ITemplateProcessor implementation backed by Velocity
 *
 * @author filippo.possenti
 */
public class VelocityTemplateProcessor implements ITemplateProcessor {
	private VelocityEngine velocityEngine;

	public VelocityTemplateProcessor(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}
	
	public VelocityTemplateProcessor() {
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
	public String processTemplate(@NonNull String templateText, Map<String, Object> context) {
		try(StringWriter writer = new StringWriter();
			StringReader reader = new StringReader(templateText)
		) {
			velocityEngine.evaluate(new VelocityContext(context), writer, "template", reader);
			return writer.toString();
		}
	}
}
