package it.openly.core.data;

import java.util.Map;

/**
 * @author filippo.possenti
 */
public interface ITemplateProcessor {
	ProcessedTemplate processTemplate(String templateText, Map<String, Object> context);
}
