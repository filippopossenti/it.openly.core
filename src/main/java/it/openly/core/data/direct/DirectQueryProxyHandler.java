package it.openly.core.data.direct;

import it.openly.core.data.IQuery;
import it.openly.core.data.IQueryFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

public class DirectQueryProxyHandler implements InvocationHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
	
	private IQueryFactory queryFactory = null;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String[] parameterNames = new String[args.length];
		String[] discoveredParamNames = null;
		Annotation[][] annotations = method.getParameterAnnotations();
		for(int i = 0; i < annotations.length; i++) {
			String paramName = extractQueryParamName(annotations[i]);
			if(paramName == null) {
				logger.debug("No QueryParam annotation found for method {}.{}. Extraction will be attempted but you should consider adding the QueryParam annotation instead.", method.getClass().getName(), method.getName());
				if(discoveredParamNames == null) {
					discoveredParamNames = parameterNameDiscoverer.getParameterNames(method);
				}
				paramName = discoveredParamNames[i];
				if(paramName == null) {
					logger.error("No name could be discovered for argument {} of method {}.{}.", i, method.getClass().getName(), method.getName());
					throw new Exception("No name could be discovered for argument " + i);
				}
			}
			parameterNames[i] = paramName;
			
		}

		Map<String, Object> context = new HashMap<>();
		for(int i = 0; i < args.length; i++) {
			context.put(parameterNames[i], args[i]);
		}
		
		String queryPath = method.getClass().getSimpleName() + "/" + method.getName() + ".sql";
		
		IQuery query = queryFactory.createQuery(queryPath, context);
		
		if(method.getReturnType().equals(Void.TYPE)) {
			query.update(context);
			return null;
		}
		else if(List.class.isAssignableFrom(method.getReturnType())) {
			return query.queryForList(context);
		}
		else {
			return query.queryForObject(method.getReturnType(), context);
		}
	}
	
	private String extractQueryParamName(Annotation[] annotations) {
		for(int i = 0; i < annotations.length; i++) {
			Annotation annot = annotations[i];
			if(annot.annotationType().isAssignableFrom(QueryParam.class)) {
				QueryParam qp = annot.annotationType().getAnnotation(QueryParam.class);
				return qp.value();
			}
		}
		return null;
	}
}
