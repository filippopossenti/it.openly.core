package it.openly.core.data.support;

import it.openly.core.data.ITemplateProcessor;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * A simple Sql Template Processor implementing some very basic and yet flexible processing.
 * The objective is to allow the creation of simple parametric queries, allowing to use one query file for multiple sets
 * of parameters. This is very useful when showing grid results that the user can filter upon on arbitrary columns.
 *
 * A query meant for this template will look something like:
 *
 * <code>
 * select
 *     *
 * from
 *     mytable
 * where 1=1
 *     -- key1 -- and mycol = :key1
 *     -- key3 is null -- and mycol2 is null
 *     -- !key4 -- and mycol3 = 'something'
 * order by
 *     -- key2:value_asc -- mycol2 asc
 *     -- key2:value_desc -- mycol2 desc
 *     -- =key3 --
 * </code>
 *
 * Supported processing features examples:
 *
 * -- key1 --
 * When the query is passed a Map containing "key1", the corresponding block (-- key1 --) is removed, thus enabling
 * what follows in the same line of the query.
 *
 * -- key3 is null --
 * When the query is passed a Map containing a key "key3" set to null, the corresponding block is removed, thus enabling
 * what follows in the same line of the query.
 *
 * -- !key4 --
 * When the query is passed a Map that DOES NOT contain a key "key4", the corresponding block is removed, thus enabling
 * what follows in the same line of the query
 *
 * -- key2:value_asc --
 * When the query is passed a Map that contains a key "key2" with value matching "value_asc", the corresponding block is
 * removed, thus enabling what follows in the same line of the query.
 *
 * -- =key3 --
 * When the query is passed a Map that contains a key "key3", the comment block is removed and replaced with the value
 * of such key, therefore injecting whichever query portion the developer wishes.
 * Note that this feature may lead to SQL Injection and is disabled by default. In order to enable it, the query must
 * contain a -- PRAGMA:ENABLE_INJECT -- block.
 *
 * @author filippo.possenti
 */
public class SimpleQueryTemplateProcessor implements ITemplateProcessor {
    private static final String COMMENT_START = "-- ";
    private static final String COMMENT_START_NEGATIVE = "-- !";
    private static final String COMMENT_START_INJECT = "-- =";
    private static final String COMMEND_END = " --";

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
    public final String processTemplate(@NonNull String templateText, Map<String, Object> context) {
        String[] queryLines = templateText.split("\n");
        boolean enableInject = templateText.contains("-- PRAGMA:ENABLE_INJECT --");
        for(int i = 0; i <queryLines.length; i++) {
            queryLines[i] = replaceTokenFromAllContexts(enableInject, queryLines[i], context);
        }
        String sql = String.join("\n", queryLines);
        if (removeEmptyLines) {
            sql = removeEmptyLines(sql);
        }
        return sql;
    }

    private String replaceTokenFromAllContexts(boolean enableInject, String line, Map<String, Object> context) {
        final TokenDeactivator td = new TokenDeactivator(line);
        context.forEach(td::evaluate);
        td.evaluateNegative(context);
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
        boolean line1 = false;
        while ((line = rdr.readLine()) != null) {
            String l = line.trim();
            if(!line1) {
                line1 = true;
            }
            else {
                wtr.write(System.lineSeparator());
            }
            if (!"".equals(l))
                wtr.write(line);
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
            int end = result.indexOf(COMMEND_END, start + COMMENT_START_NEGATIVE.length());
            String key = result.substring(start + COMMENT_START_NEGATIVE.length(), end).trim();
            if(!context.containsKey(key)) {
                result = result.substring(0, start) + result.substring(end + COMMEND_END.length());
            }
        }

        void evaluateInjection(Map<String, Object> context) {
            int start = result.indexOf(COMMENT_START_INJECT);
            if(start < 0) {
                return;
            }
            int end = result.indexOf(COMMEND_END, start + COMMENT_START_INJECT.length());
            String key = result.substring(start + COMMENT_START_INJECT.length(), end).trim();
            if(context.containsKey(key)) {
                result = result.substring(0, start) + context.get(key) + result.substring(end + COMMEND_END.length());
            }
        }

        void evaluate(String key, Object value) {
            if(value != null) {
                result = result.replace(COMMENT_START + key + ":" + value + COMMEND_END, "");
            }
            else {
                result = result.replace(COMMENT_START + key + " is null" + COMMEND_END, "");
            }
            result = result.replace(COMMENT_START + key + COMMEND_END, "");
        }

        String getResult() {
            return result;
        }
    }
}
