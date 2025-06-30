package org.example;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

public class SqlLineageMain {
    public static void main(String[] argz) throws JSQLParserException {
        String plainSql = """
                SELECT pid, cid,
                    CASE cid
                        WHEN 1 THEN 'Food'
                        WHEN 2 THEN 'Beverage'
                        ELSE 'Other'
                    END AS catItem,
                    COALESCE(phone, alt_phone, 'N/A') AS contact
                FROM Customers
                """;

//        String encodedSql = """
//                SELECT {{pid::132}},
//                       {{cid::1}},
//                      CASE {{cid::1}}
//                        WHEN 1 THEN 'Food'
//                        WHEN 2 THEN 'Beverage'
//                        ELSE 'Other'
//                      END AS {{catItem::Food}},
//                      COALESCE({{phone::}}, {{alt_phone::(479) 555-1234}}, 'N/A') AS {{contact::(479) 555-1234}}
//                FROM Customers
//                """;

        String encodedSql = """
                SELECT {{OrderID::123}}, {{Quantity::30}},
                CASE
                    WHEN {{Quantity::30}} > 30 THEN 'The quantity is greater than 30'
                    WHEN {{Quantity::30}} = 30 THEN 'The quantity is 30'
                    ELSE 'The quantity is under 30'
                END AS {{QuantityText::The quantity is 30}}
                FROM OrderDetails
                """;

        Map<String, String> decodedQueryMap = decode(encodedSql);
        String decodedQueryString = toJson(decodedQueryMap);

        String encodedSqlToQuery = toQuery(encodedSql);
        System.out.println(encodedSqlToQuery);

        Select select = (Select) CCJSqlParserUtil.parse(encodedSqlToQuery);

        PlainSelect body = (PlainSelect) select.getSelectBody();

        for (SelectItem item : body.getSelectItems()) {
            if (item instanceof SelectExpressionItem exprItem) {
                Expression expr = exprItem.getExpression();

                String alias = exprItem.getAlias() != null ? exprItem.getAlias().getName() : null;

                if (expr instanceof Column col) {
                    String key = col.getColumnName();

                    String encoded = getEncodedStringValue(key, decodedQueryMap).toString();

                    exprItem.setExpression(new StringValue(encoded));
                }
                else if (expr instanceof CaseExpression caseExpr) {
                    Expression switchExpr = caseExpr.getSwitchExpression();

                    if (switchExpr instanceof Column colExpr) {
                        String key = colExpr.getColumnName();
                        caseExpr.setSwitchExpression(getEncodedStringValue(key, decodedQueryMap));
                    }

                    if (alias != null) {
                        exprItem.setAlias(new Alias(getEncodedStringValue(alias, decodedQueryMap).toString()));
                    }

                    if (Objects.nonNull(caseExpr.getWhenClauses())) {
                        for (WhenClause whenClause : caseExpr.getWhenClauses()) {
                            if(whenClause.getWhenExpression() instanceof GreaterThan greaterThanExpr) {
                                Expression leftExpr = greaterThanExpr.getLeftExpression();

                                decodedQueryMap.forEach((k, v) -> {
                                    if(leftExpr.toString().contains(k)) {
                                        String str = leftExpr.toString().replace(k, getEncodedStringValue(k, decodedQueryMap).toString());
                                        greaterThanExpr.setLeftExpression(new StringValue(str));
                                    }
                                });
                            }

                            if(whenClause.getWhenExpression() instanceof GreaterThanEquals greaterThanEqualsExpr) {

                            }

                            if(whenClause.getWhenExpression() instanceof EqualsTo equalsToExpr) {
                                Expression leftExpr = equalsToExpr.getLeftExpression();

                                decodedQueryMap.forEach((k, v) -> {
                                    if(leftExpr.toString().contains(k)) {
                                        String str = leftExpr.toString().replace(k, getEncodedStringValue(k, decodedQueryMap).toString());
                                        equalsToExpr.setLeftExpression(new StringValue(str));
                                    }
                                });
                            }
                        }
                    }
                }
                else if (expr instanceof Function func && func.getName().equalsIgnoreCase("COALESCE")) {
                    ExpressionList args = func.getParameters();

                    for (int i = 0; i < args.getExpressions().size(); i++) {
                        Expression arg = args.getExpressions().get(i);

                        if (arg instanceof Column colArg) {
                            String key = colArg.getColumnName();
                            args.getExpressions().set(i, getEncodedStringValue(key, decodedQueryMap));
                        }
                    }

                    if (alias != null) {
                        exprItem.setAlias(new Alias(getEncodedStringValue(alias, decodedQueryMap).toString()));
                    }
                }
            }
        }

        System.out.println(encodedSql.replaceAll("\\s+", " ").trim());

        String st = select.toString().replace("'{{", "{{").replace("}}'", "}}");
        System.out.println(st);

        System.out.println(st.equals(encodedSql.replaceAll("\\s+", " ").trim()));
    }

    static StringValue getEncodedStringValue(String key, Map<String, String> decodedQueryMap) {
        return new StringValue("{{" + key + "::" + decodedQueryMap.get(key) + "}}");
    }

    private static Map<String, String> decode(String encodedSql) {
        // Regex to match {{key::value}} patterns
        Pattern pattern = Pattern.compile("\\{\\{(.*?)::(.*?)}}");
        Matcher matcher = pattern.matcher(encodedSql);

        // Flat map: key → first value only and keeps insertion order
        Map<String, String> resultMap = new LinkedHashMap<>();

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();

            // Only store the first value for a key
            resultMap.putIfAbsent(key, value);
        }

        // Print as JSON-style output
        System.out.println(toJson(resultMap));

        return resultMap;
    }

    public static String toQuery(String encodedSql) throws JSQLParserException {
        // Regex to match {{key::value}} and capture the key
        Pattern pattern = Pattern.compile("\\{\\{(.*?)::(.*?)}}");
        Matcher matcher = pattern.matcher(encodedSql);

        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1).trim(); // Extract the key
            matcher.appendReplacement(result, Matcher.quoteReplacement(key));
        }

        matcher.appendTail(result);

        return result.toString();
    }

    // Utility to print map as JSON
    public static String toJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");

        map.forEach((k, v) ->
                sb.append("\"")
                  .append(k)
                  .append("\":\"")
                  .append(v)
                  .append("\", "));

        if (!map.isEmpty())
            sb.setLength(sb.length() - 2); // remove trailing comma

        sb.append("}");

        return sb.toString();
    }
}
