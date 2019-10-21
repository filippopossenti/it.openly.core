package it.openly.core.data;

import java.util.Map;

/**
 * @author filippo.possenti
 */
public interface ITemplateProcessor {
	String processTemplate(String templateText, Map<String, Object> context);
}
