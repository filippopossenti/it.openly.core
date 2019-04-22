package it.openly.core.spring.factory.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * A specialized PropertyPlaceholderConfigurer meant to provide values that will
 * be different either for each initialization or for each use. Useful to assign
 * unique identifiers for a given run, for example.<br/>
 * <br/>
 * Currently provided placeholders are:<br/>
 * <ul>
 * <li><strong>now</strong> : the date and time the property was retrieved
 * first. Format can be changed by using mutableprops.now.format</li>
 * <li><strong>uuid</strong> : the UUID the property was retrieved first.</li>
 * <li><strong>newid</strong> : an UUID that changes every time the property is
 * retrieved</li>
 * </ul>
 * 
 * @author Filippo
 * 
 */
public class MutablePlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private String propertiesPrefix = "mutableprops";
	private Map<String, Expression> mutableProperties = null;

	public MutablePlaceholderConfigurer() {
		mutableProperties = new HashMap<String, Expression>();
		mutableProperties.put("now", new NowExpression("now", true));
		mutableProperties.put("uuid", new UUIDExpression("uuid", true));
		mutableProperties.put("newid", new UUIDExpression("newid", false));
	}

	@Override
	protected String resolvePlaceholder(String placeholder, Properties props) {
		if (mutableProperties.containsKey(placeholder)) {
			return mutableProperties.get(placeholder).evaluate(getPropertiesPrefix(), props);
		}
		return super.resolvePlaceholder(placeholder, props);
	}

	public String getPropertiesPrefix() {
		return propertiesPrefix;
	}

	public void setPropertiesPrefix(String value) {
		propertiesPrefix = value;
	}

	private static abstract class Expression {
		private String name;
		private String value = null;
		private boolean staticValue;

		public Expression(String name, boolean staticValue) {
			this.name = name;
			this.staticValue = staticValue;
		}

		public String evaluate(String prefix, Properties props) {
			if (!staticValue || value == null) {
				String format = (String) props.get(prefix + "." + name + ".format");
				value = evaluateInternal(format);
			}
			return value;
		}

		public abstract String evaluateInternal(String format);

	}

	private static class NowExpression extends Expression {

		public NowExpression(String name, boolean staticValue) {
			super(name, staticValue);
		}

		@Override
		public String evaluateInternal(String format) {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			formatter.setLenient(false);
			return formatter.format(new Date());
		}
	}

	private static class UUIDExpression extends Expression {

		public UUIDExpression(String name, boolean staticValue) {
			super(name, staticValue);
		}

		@Override
		public String evaluateInternal(String format) {
			return UUID.randomUUID().toString();
		}

	}
}
