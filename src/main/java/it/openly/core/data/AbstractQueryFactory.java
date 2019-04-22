package it.openly.core.data;

import it.openly.core.Maps;
import it.openly.core.Resources;
import it.openly.core.Strings;
import it.openly.core.resources.IResourceResolver;
import it.openly.core.templating.ITemplateProcessor;
import it.openly.core.templating.VelocityTemplateProcessor;

import java.util.List;
import java.util.Map;

/**
 * Abstract factory object for queries. Create query objects taking care of
 * adding an additional parsing layer.
 * 
 */
public abstract class AbstractQueryFactory implements IQueryFactory {
	private ITemplateProcessor templateProcessor = new VelocityTemplateProcessor();
	private List<IResourceResolver> resourceResolvers = Resources.getResourceResolvers();
	private boolean removeEmptyLines = true;

	public List<IResourceResolver> getResourceResolvers() {
		return resourceResolvers;
	}

	public void setResourceResolvers(List<IResourceResolver> value) {
		resourceResolvers = value;
	}

	public boolean getRemoveEmptyLines() {
		return removeEmptyLines;
	}

	public void setRemoveEmptyLines(boolean value) {
		removeEmptyLines = value;
	}

	public ITemplateProcessor getTemplateProcessor() {
		return templateProcessor;
	}

	public void setTemplateProcessor(ITemplateProcessor value) {
		templateProcessor = value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IQuery createQuery(String namedQuery, Map<String, ?>... contexts) {
		String sqlStatementTemplate = Resources.resolveStringResource(getResourceResolvers(), namedQuery);
		return createQueryFromTemplate(sqlStatementTemplate, contexts);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public IQuery createQueryFromTemplate(String sqlStatementTemplate, Map<String, ?>... contexts) {
		ContextUtils ctx = null;

		Map<String, Object> context = Maps.merge(contexts);

		ctx = createPreprocessingContextUtils(context);
		context.put("CTX", ctx);
		String sql = templateProcessor.processTemplate(sqlStatementTemplate, context);
		if (getRemoveEmptyLines())
			sql = Strings.removeEmptyLines(sql);

		return createQueryObject(sql, context, ctx);
	}

	protected abstract IQuery createQueryObject(String sql, Map<String, ?> context, ContextUtils ctx);

	/**
	 * Override this method if you need to change the contents of your context
	 * utils (or its implementation) before the query get parsed. You can use
	 * it, for example, if you need to support both Spring and Hibernate querys
	 * 
	 * @param context
	 * @return
	 */
	protected ContextUtils createPreprocessingContextUtils(Map<String, ?> context) {
		return new ContextUtils(context);
	}
	
	@Override
	public <T> List<T> queryForList(String namedQuery, Map<String, ?>... contexts) {
		return createQuery(namedQuery, contexts).queryForList(contexts);
	}

	@Override
	public int queryForInt(String namedQuery, Map<String, ?>... contexts) {
		return createQuery(namedQuery, contexts).queryForInt(contexts);
	}

	@Override
	public long queryForLong(String namedQuery, Map<String, ?>... contexts) {
		return createQuery(namedQuery, contexts).queryForLong(contexts);
	}

	@Override
	public <T> T queryForObject(String namedQuery, Class<T> clazz, Map<String, ?>... contexts) {
		return createQuery(namedQuery, contexts).queryForObject(clazz, contexts);
	}

	@Override
	public int update(String namedQuery, Map<String, ?>... contexts) {
		return createQuery(namedQuery, contexts).update(contexts);
	}

	@Override
	public <T> void iterate(String namedQuery, IRowHandlerCallback<T> callback, Map<String, ?>... contexts) {
		createQuery(namedQuery, contexts).iterate(callback, contexts);
	}
}
