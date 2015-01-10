package it.openly.core.templating;

import java.util.Map;

public interface ITemplateProcessor {
	String processTemplate(String templateText, Map<String, ?>... contexts);
}
