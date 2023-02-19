package it.openly.core.data.support;

import it.openly.core.data.ITemplateProcessor;
import it.openly.core.data.ProcessedTemplate;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * A simple Sql Template Processor implementing some very basic and yet flexible processing.
 * The objective is to allow the creation of simple parametric queries, allowing to use one query file for multiple sets
 * of parameters. This is very useful when showing grid results that the user can filter upon on arbitrary columns.<br/>
 * <br/>
 * A query meant for this template will look something like:<br/>
 * <br/>
 * <code>
 * select <br/>
 *     * <br/>
 * from <br/>
 *     mytable <br/>
 * where 1=1 <br/>
 *     -- key1 -- and mycol = :key1 <br/>
 *     -- key3 is null -- and mycol2 is null <br/>
 *     -- !key4 -- and mycol3 = 'something' <br/>
 *     -- key5 -- and mycol4 in (:key5) <br/>
 * order by <br/>
 *     -- key2:value_asc -- mycol2 asc, <br/>
 *     -- key2:value_desc -- mycol2 desc, <br/>
 *     id desc
 *     -- =key3 -- <br/>
 * -- page_size -- limit :page_size
 * -- page_offset -- offset :page_offset
 * </code>
 * <br/>
 * Supported processing features examples:<br/>
 * <br/>
 * <b>Comment elision</b><br/>
 * -- key1 --<br/>
 * When the query is passed a Map containing "key1", the corresponding block (-- key1 --) is removed, thus enabling
 * what follows in the same line of the query.<br/>
 * <br/>
 * -- key3 is null --<br/>
 * When the query is passed a Map containing a key "key3" set to null, the corresponding block is removed, thus enabling
 * what follows in the same line of the query.<br/>
 * <br/>
 * -- !key4 --<br/>
 * When the query is passed a Map that DOES NOT contain a key "key4", the corresponding block is removed, thus enabling
 * what follows in the same line of the query.<br/>
 * <br/>
 * -- key2:value_asc --<br/>
 * When the query is passed a Map that contains a key "key2" with value matching "value_asc", the corresponding block is
 * removed, thus enabling what follows in the same line of the query.<br/>
 * <br/>
 * <b>Variable injection</b><br/>
 * -- =key3 --<br/>
 * When the query is passed a Map that contains a key "key3", the comment block is removed and replaced with the value
 * of such key, therefore injecting whichever query portion the developer wishes.<br/>
 * Note that this feature may lead to SQL Injection and is disabled by default. In order to enable it, the query must
 * contain a -- PRAGMA:ENABLE_INJECT -- block.<br/>
 * <br/>
 * <b>Array expansion</b><br/>
 * :key5<br/>
 * When the query is passed a Map that contains a key "key5" and its value is an array or a list, the variable is
 * replaced with one key for each value contained in the array/list, concatenated through a comma. The corresponding
 * keys are automatically added to the resulting context.<br/>
 * For example, if the array contained two elements, the replaced value would be ":key5_0, key5_1".<br/>
 *
 * @author filippo.possenti
 */
public class SimpleQueryTemplateProcessor implements ITemplateProcessor {
    private static final String COMMENT_START = "-- ";
    private static final String COMMENT_START_NEGATIVE = "-- !";
    private static final String COMMENT_START_INJECT = "-- =";
    private static final String COMMENT_END = " --";
    private static final String NAMED_PARAMETER_START = ":";

    private static final int MAX_REPLACEMENTS = 10;

    private final boolean removeEmptyLines;

    public SimpleQueryTemplateProcessor(boolean removeEmptyLines) {
        this.removeEmptyLines = removeEmptyLines;
    }

    /**
     * Processes a template
     * @param templateText The template text
     * @param context A {@link java.util.Map Map} object containing the variables that will drive the template's processing
     * @return The processed text
     */
    @Override
    public final ProcessedTemplate processTemplate(@NonNull String templateText, Map<String, Object> context) {
        ProcessedTemplate processedTemplate = new ProcessedTemplate();
        context = new HashMap<>(context);
        String[] queryLines = templateText.split("\n");
        boolean enableInject = templateText.contains("-- PRAGMA:ENABLE_INJECT --");
        for(int i = 0; i <queryLines.length; i++) {
            queryLines[i] = replaceTokenFromAllContexts(enableInject, queryLines[i], context);
        }
        String sql = String.join("\n", queryLines);
        if (removeEmptyLines) {
            sql = removeEmptyLines(sql);
        }
        processedTemplate.setSql(sql);
        processedTemplate.setContext(context);
        return processedTemplate;
    }

    private String replaceTokenFromAllContexts(boolean enableInject, String line, Map<String, Object> context) {
        final TokenDeactivator td = new TokenDeactivator(line);
        context.forEach(td::evaluate);
        td.evaluateNegative(context);
        td.evaluateArrayExpansion(context);
        if(enableInject) {
            td.evaluateInjection(context);
        }
        return td.getResult();
    }

    @SneakyThrows
    private String removeEmptyLines(String instr) {
        BufferedReader rdr = new BufferedReader(new StringReader(instr));
        StringWriter wtr = new StringWriter();
        String line;
        boolean line1 = true;
        while ((line = rdr.readLine()) != null) {
            String l = line.trim();
            if (!"".equals(l)) {
                if(!line1) {
                    wtr.write(System.lineSeparator());
                }
                line1 = false;
                wtr.write(line);
            }
        }
        return wtr.toString();
    }

    private static class TokenDeactivator {
        private String result;
        TokenDeactivator(String result) {
            this.result = result;
        }

        void evaluateNegative(Map<String, Object> context) {
            int start = result.indexOf(COMMENT_START_NEGATIVE);
            if(start < 0) {
                return;
            }
            int end = result.indexOf(COMMENT_END, start + COMMENT_START_NEGATIVE.length());
            String key = result.substring(start + COMMENT_START_NEGATIVE.length(), end).trim();
            if(!context.containsKey(key)) {
                result = result.substring(0, start) + result.substring(end + COMMENT_END.length());
            }
        }

        void evaluateInjection(Map<String, Object> context) {
            int remaining = MAX_REPLACEMENTS;
            while(--remaining >= 0) {
                int start = result.indexOf(COMMENT_START_INJECT);
                if (start < 0) {
                    return;
                }
                int end = result.indexOf(COMMENT_END, start + COMMENT_START_INJECT.length());
                String key = result.substring(start + COMMENT_START_INJECT.length(), end).trim();
                if (context.containsKey(key)) {
                    result = result.substring(0, start) + context.get(key) + result.substring(end + COMMENT_END.length());
                }
            }
        }

        void evaluateArrayExpansion(Map<String, Object> context) {
            if(!result.contains(NAMED_PARAMETER_START)) {
                return;
            }
            Map<String, Object> ctx = new HashMap<>();
            context.forEach((key, value) -> {
                if(result.contains(NAMED_PARAMETER_START + key)) {
                    getArrayExpansionSourceCollection(value).ifPresent(list -> applyArrayExpansionReplacement(ctx, key, list));
                }
            });
            context.putAll(ctx);
        }

        private void applyArrayExpansionReplacement(Map<String, Object> ctx, String key, List<?> list) {
            StringBuilder replacement = new StringBuilder();
            int count = list.size();
            for(int i = 0; i < count; i++) {
                String indexedKey = key + "_" + i;
                ctx.put(indexedKey, list.get(i));
                if(i > 0) {
                    replacement.append(", ");
                }
                replacement.append(NAMED_PARAMETER_START);
                replacement.append(indexedKey);
            }

            result = result.replace(NAMED_PARAMETER_START + key, replacement.toString());
        }

        void evaluate(String key, Object value) {
            if(value != null) {
                result = result.replace(COMMENT_START + key + NAMED_PARAMETER_START + value + COMMENT_END, "");
            }
            else {
                result = result.replace(COMMENT_START + key + " is null" + COMMENT_END, "");
            }
            result = result.replace(COMMENT_START + key + COMMENT_END, "");
        }

        String getResult() {
            return result;
        }

        private Optional<List<?>> getArrayExpansionSourceCollection(Object value) {
            if(value == null) {
                return Optional.empty();
            }
            else if(value.getClass().isArray()) {
                return Optional.of(Arrays.asList((Object[]) value));
            }
            else if(value instanceof List<?> list) {
                return Optional.of(list);
            }
            else if(value instanceof Collection<?> collection) {
                return Optional.of(collection.stream().toList());
            }
            else {
                return Optional.of(List.of(value));
            }
        }


    }
}
