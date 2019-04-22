package it.openly.core.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.map.CaseInsensitiveMap;

public class ContextUtils {
	private Map<String, Object> context;
	private Map<String, Object> usedParameters = new HashMap<>();
	private String concatOperator = globalStringConcatOperator;
	private boolean isNative = false;
	private String returnedType = null;
	
	
	private static String globalStringConcatOperator = "+";	// the plus is for MS-SQL: change it to || for Oracle compatibility.
	
	@SuppressWarnings("unchecked")
	public ContextUtils(Map<String, ?> context) {
		this.context = new CaseInsensitiveMap(context);	// sql queries should be case-insensitive (even if named parameters not always are)... so we try to mimic that for subsequent use in velocity
	}

	private void setUsedInternal(String key) {
		usedParameters.put(key, context.get(key));
	}
	
	private String addUsedInternal(String originalKey, Object value) {
		String key = originalKey + "_" + UUID.randomUUID().toString().replace("-", "");
		usedParameters.put(key, value);
		return key;
	}
	
	/**
	 * Used to indicate the underlying factory that the query is to be considered as native.
	 * @return True if the underlying factory is to manage the query as native, false otherwise.
	 */
	public boolean getNative() {
		return isNative;
	}
	
	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}

	/**
	 * Used to indicate the underlying factory which type will be returned.
	 * @return The type that will be returned.
	 */
	public String getReturnedType() {
		return returnedType;
	}
	
	public void setReturnedType(String returnedType) {
		this.returnedType = returnedType;
	}
	
	/**
	 * Use it to set the string concatenation operator. You should do this only once, when your application gets initialized.
	 * @param operator The string representing the string concatenation operator to use throughout the application
	 */
	public static void setGlobalStringConcatOperator(String operator) {
		globalStringConcatOperator = operator;
	}

	/**
	 * Use it if you want to change the operator only for this specific context.
	 * @param operator The string representing the string concatenation operator to use
	 */
	public void setStringConcatOperator(String operator) {
		concatOperator = operator;
	}
	
	
	/**
	 * Use this to set a parameter as used. It's necessary in Hibernate, as it expects a perfect match between the parameters you provide and the ones inside the query.
	 * @param key Marks the key as a used parameter
	 */
	public void setUsed(String key) {
		if(isBound(key))
			setUsedInternal(key);
	}
	
	/**
	 * Meant for internal use. Will retrieve the list of parameters that have been processed by this utilities class.
	 * This is used to avoid errors in Hibernate, as it expects to receive exactly the number of parameters that are
	 *  written inside the query.
	 * @return The list of parameters that have been processed by this utility class
	 */
	public Map<String, Object> getUsedParametersMap() {
		return usedParameters;
	}
	
	/**
	 * Returns true if the specified key is present inside the context.
	 * @param key The key to check
	 * @return True if the key is present inside the context, false otherwise
	 */
	public boolean isBound(String key) {
		return context.containsKey(key);
	}
	
	/**
	 * Returns the value of the specified key. Use with caution as it can expose queries to SQL injection.
	 * @param key The key for which the value is to be retrieved
	 * @return The value associated with the key
	 */
	public Object get(String key) {
		return context.get(key);
	}
	
	public void put(String key, Object value) {
		context.put(key, value);
	}
	
	/**
	 * Write an equality condition if and only if the specified key is present in context
	 * @param operator Prepend the specified operator
	 * @param alias The alias to check against equality
	 * @param key The key to check from the context
	 * @return The sql fragment of the condition or an empty string if the key is not bound to the context
	 */
	public String equal(String operator, String alias, String key) {
		if(isBound(key)) {
			setUsedInternal(key);
			return operator + " " + alias + " = :" + key + " ";
		}
		else {
			return "";
		}
	}
	
	/**
	 * wib stands for "Write If Bound"
	 * 
	 * Write value if and only if the specified key is present in context.
	 * Can be used to write "smart" insert querys.
	 * @param key The key to check
	 * @param value The value to write if the key is present
	 * @return The value
	 */
	public String wib(String key, String value) {
		return wb(key, value, "");
	}

	/**
	 * Writes the first value if bound, the second if not
	 * @param key The key to check
	 * @param valueIfBound The value to write if the key is present
	 * @param valueIfNotBound The value to write if the key is not present
	 * @return The value
	 */
	public String wb(String key, String valueIfBound, String valueIfNotBound) {
		if(isBound(key))
			return valueIfBound;
		else
			return valueIfNotBound;
	}
	
	
	/**
	 * Write a like condition if and only if the specified key is present in context.
	 * @param operator The operator to prepend
	 * @param alias The name of the column
	 * @param key The key to check
	 * @return The sql clause
	 */
	public String like(String operator, String alias, String key) {
		if(isBound(key)) {
			setUsedInternal(key);
			return operator + " " + alias + " like '%' " + concatOperator + " :" + key + " " + concatOperator + " '%' ";
		}
		else
			return "";
	}
	
	/**
	 * Write an "in" condition if and only if the specified key is present and it is an array or list
	 *  with one elements or more.
	 * @param operator The operator to prepend
	 * @param alias The name of the column
	 * @param key The key to check
	 * @return The sql clause
	 */
	public String in(String operator, String alias, String key) {
		if(isBound(key)) {
			StringBuilder condition = new StringBuilder("");
			condition.append(operator);
			condition.append(" ");
			condition.append(alias);
			condition.append(" in (");
			boolean hasElements;
			Object val = get(key);
			if((val instanceof Object[]) || val.getClass().isArray()) {
				Object[] arrayValue = (Object[])val;
				hasElements = (arrayValue.length > 0);
				for(int i = 0; i < arrayValue.length; i++) {
					String newKey = addUsedInternal(key, arrayValue[i]);
					if(i > 0)
						condition.append(", ");
					condition.append(":");
					condition.append(newKey);
				}
			}
			else if(val instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> listValue = (List<Object>)val;
				hasElements = !listValue.isEmpty();
				for(int i = 0; i < listValue.size(); i++) {
					String newKey = addUsedInternal(key, listValue.get(i));
					if(i > 0)
						condition.append(", ");
					condition.append(":");
					condition.append(newKey);
				}
			}
			else {
				throw new RuntimeException("Parameter type not supported. You must supply either a list or an array for key " + key);
			}
			condition.append(") ");
			
			
			if(!hasElements)
				condition = new StringBuilder("");
			return condition.toString();
		}
		else
			return "";
	}
	
	/**
	 * Write inclusive greater-than and less-than conditions.
	 * Greater-than condition is applied if keyLow is present into the context.
	 * Lower-than condition is applied if keyHigh is present into the context.
	 * Conditions are enveloped between parenthesis to avoid interference with other conditions.
	 * @param operator The operator to prepend
	 * @param alias The name of the column
	 * @param keyLow The key that will contain the lower value
	 * @param keyHigh The key that will contain the higher value
	 * @return The sql clause
	 */
	public String between(String operator, String alias, String keyLow, String keyHigh) {
		String rv = "";
		if(isBound(keyLow) || isBound(keyHigh))
			rv += " " + operator + " (";
		if(isBound(keyLow)) {
			setUsedInternal(keyLow);
			rv += alias + " >= " + ":" + keyLow + " ";
			if(isBound(keyHigh))
				rv += " and ";
		}
		if(isBound(keyHigh)) {
			setUsedInternal(keyHigh);
			rv += alias + " <= " + ":" + keyHigh + " ";
		}

		if(isBound(keyLow) || isBound(keyHigh))
			rv += ") ";
	
		return rv;
	}
	
	/**
	 * Write an order-by condition if and only if the specified key is present into the context.
	 * An additional coma is appended to the end, as we cannot know if there are other order-by conditions. This will enforce you to add an additional order-by condition on the end.
	 * @param alias The column
	 * @param key The key to check
	 * @param nulls A suffix clause used to tell whether nulls go first or last
	 * @return The sql clause
	 */
	public String orderby(String alias, String key, String nulls) {
		if(isBound(key))
			return " " + alias + " " + ("DESC".equals(context.get(key)) ? "DESC" : "ASC") + " " + nulls + ", ";
		else
			return "";
	}
}
