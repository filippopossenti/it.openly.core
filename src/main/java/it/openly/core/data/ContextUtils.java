package it.openly.core.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.map.CaseInsensitiveMap;

public class ContextUtils {
	private Map<String, Object> context = null;
	private Map<String, Object> usedParameters = new HashMap<String, Object>();
	private String concatOperator = STRING_CONCAT_OPERATOR;
	private boolean isNative = false;
	private String returnedType = null;
	
	
	private static String STRING_CONCAT_OPERATOR = "+";	// the plus is for MS-SQL: change it to || for Oracle compatibility.
	
	@SuppressWarnings("unchecked")
	public ContextUtils(Map<String, ?> context) {
		this.context = new CaseInsensitiveMap(context);	// sql querys should be case-insensitive (even if named parameters not always are)... so we try to mimic that for subsequent use in velocity
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
	 * @return
	 */
	public boolean getNative() {
		return isNative;
	}
	
	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}

	/**
	 * Used to indicate the underlying factory which type will be returned.
	 * @return
	 */
	public String getReturnedType() {
		return returnedType;
	}
	
	public void setReturnedType(String returnedType) {
		this.returnedType = returnedType;
	}
	
	/**
	 * Use it to set the string concatenation operator. You should do this only once, when your application gets initialized.
	 * @param operator
	 */
	public static void setGlobalStringConcatOperator(String operator) {
		STRING_CONCAT_OPERATOR = operator;
	}

	/**
	 * Use it if you want to change the operator only for this specific context.
	 * @param operator
	 */
	public void setStringConcatOperator(String operator) {
		concatOperator = operator;
	}
	
	
	/**
	 * Use this to set a parameter as used. It's necessary in Hibernate, as it expects a perfect match between the parameters you provide and the ones inside the query.
	 * @param key
	 */
	public void setUsed(String key) {
		if(isBound(key))
			setUsedInternal(key);
	}
	
	/**
	 * Meant for internal use. Will retrieve the list of parameters that have been processed by this utilities class.
	 * This is used to avoid errors in Hibernate, as it expects to receive exactly the number of parameters that are
	 *  written inside the query.
	 * @return
	 */
	public Map<String, Object> getUsedParametersMap() {
		return usedParameters;
	}
	
	/**
	 * Returns true if the specified key is present inside the context.
	 * @param key
	 * @return
	 */
	public boolean isBound(String key) {
		return context.containsKey(key);
	}
	
	/**
	 * Returns the value of the specified key. Use with caution as it can expose querys to SQL injection.
	 * @param key
	 * @return
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
	 * @return
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
	 * @param key
	 * @param value
	 * @return
	 */
	public String wib(String key, String value) {
		return wb(key, value, "");
	}

	/**
	 * Writes the first value if bound, the second if not
	 * @param key
	 * @param value
	 * @return
	 */
	public String wb(String key, String valueIfBound, String valueIfNotBound) {
		if(isBound(key))
			return valueIfBound;
		else
			return valueIfNotBound;
	}
	
	
	/**
	 * Write a like condition if and only if the specified key is present in context.
	 * @param operator
	 * @param alias
	 * @param key
	 * @return
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
	 * @param operator
	 * @param alias
	 * @param key
	 * @return
	 */
	public String in(String operator, String alias, String key) {
		if(isBound(key)) {
			String condition = operator + " " + alias + " in (";
			boolean hasElements = false;
			Object val = get(key);
			if((val instanceof Object[]) || val.getClass().isArray()) {
				Object[] arrayValue = (Object[])val;
				hasElements = (arrayValue.length > 0);
				for(int i = 0; i < arrayValue.length; i++) {
					String newKey = addUsedInternal(key, arrayValue[i]);
					if(i > 0)
						condition += ", ";
					condition += ":" + newKey;
				}
			}
			else if(val instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> listValue = (List<Object>)val;
				hasElements = (listValue.size() > 0);
				for(int i = 0; i < listValue.size(); i++) {
					String newKey = addUsedInternal(key, listValue.get(i));
					if(i > 0)
						condition += ", ";
					condition += ":" + newKey;
				}
			}
			else {
				throw new RuntimeException("Parameter type not supported. You must supply either a list or an array for key " + key);
			}
			condition += ") ";
			
			
			if(!hasElements)
				condition = "";
			return condition;
		}
		else
			return "";
	}
	
	/**
	 * Write inclusive greater-than and less-than conditions.
	 * Greater-than condition is applied if keyLow is present into the context.
	 * Lower-than condition is applied if keyHigh is present into the context.
	 * Conditions are enveloped between parenthesis to avoid interference with other conditions.
	 * @param operator
	 * @param alias
	 * @param keyLow
	 * @param keyHigh
	 * @return
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
	 * @param alias
	 * @param key
	 * @param nulls
	 * @return
	 */
	public String orderby(String alias, String key, String nulls) {
		if(isBound(key))
			return " " + alias + " " + ("DESC".equals(context.get(key)) ? "DESC" : "ASC") + " " + nulls + ", ";
		else
			return "";
	}
}
