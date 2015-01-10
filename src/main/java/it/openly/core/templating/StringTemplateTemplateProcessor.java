package it.openly.core.templating;

import java.util.Map;
import java.util.Map.Entry;

import org.stringtemplate.v4.ST;

/**
 * ITemplateProcessor implementation backed by StringTemplate.
 * 
 * TODO: this is new stuff. Test it.
 * 
 * @author Filippo
 * 
 */
public class StringTemplateTemplateProcessor implements ITemplateProcessor {

	@Override
	public String processTemplate(String templateText, Map<String, ?>... contexts) {
		ST template = new ST(templateText);
		for (int i = 0; i < contexts.length; i++) {
			Map<String, ?> ctx = contexts[i];
			for (Entry<String, ?> entry : ctx.entrySet()) {
				template.add(entry.getKey(), entry.getValue());
			}
		}
		return template.render();
	}
}
