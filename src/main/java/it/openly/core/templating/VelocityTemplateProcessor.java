package it.openly.core.templating;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * ITemplateProcessor implementation backed by Velocity
 * @author Filippo
 *
 */
public class VelocityTemplateProcessor implements ITemplateProcessor {
	private VelocityEngine velocityEngine = new VelocityEngine();
	
	private String helperKey = null;
	private Object helperObject = null;
	
	public VelocityTemplateProcessor() {
	}
	
	public VelocityTemplateProcessor(String helperKey, Object helperObject) {
		this.helperKey = helperKey;
		this.helperObject = helperObject;
	}
	
	@Override
	public String processTemplate(String templateText, Map<String, ?>... contexts) {
		Map<String, Object> context = prepareSingleContext(contexts);
		if(helperKey != null) {
			context.put(helperKey, helperObject);
		}
		
		VelocityContext vcontext = new VelocityContext(context);
		StringWriter writer = new StringWriter();
		StringReader reader = new StringReader(templateText);
		velocityEngine.evaluate(vcontext, writer, "template", reader);
		return writer.toString();
	}
	
	private Map<String, Object> prepareSingleContext(Map<String, ?>[] contexts) {
		Map<String, Object> rv = new HashMap<String, Object>();
		for(int i = 0; i < contexts.length; i++) {
			rv.putAll(contexts[i]);
		}
		return rv;
	}

}
