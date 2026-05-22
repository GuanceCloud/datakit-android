package com.ft.sdk.garble.filter;

import com.ft.sdk.garble.bean.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataKit-compatible point filter. A matched condition means the point should be dropped.
 */
class FTDataFilter {

    static final String CATEGORY_LOGGING = "logging";
    static final String CATEGORY_RUM = "rum";

    private final Map<String, List<FTFilterParser.Expression>> conditions;
    private final Map<String, String> rawConditions;

    FTDataFilter(Map<String, List<FTFilterParser.Expression>> conditions,
                 Map<String, String> rawConditions) {
        this.conditions = conditions;
        this.rawConditions = rawConditions;
    }

    static FTDataFilter empty() {
        return new FTDataFilter(new HashMap<String, List<FTFilterParser.Expression>>(),
                new HashMap<String, String>());
    }

    static FTDataFilter compile(Map<String, String[]> filters) {
        Map<String, List<FTFilterParser.Expression>> compiled =
                new HashMap<String, List<FTFilterParser.Expression>>();
        Map<String, String> raw = new HashMap<>();
        if (filters == null || filters.isEmpty()) {
            return new FTDataFilter(compiled, raw);
        }

        for (Map.Entry<String, String[]> entry : filters.entrySet()) {
            String category = normalizeCategory(entry.getKey());
            if (category == null) {
                continue;
            }

            List<FTFilterParser.Expression> categoryConditions = new ArrayList<>();
            StringBuilder rawBuilder = new StringBuilder();
            String[] rules = entry.getValue();
            if (rules == null) {
                continue;
            }

            for (String rule : rules) {
                if (rule == null || rule.trim().isEmpty()) {
                    continue;
                }
                List<FTFilterParser.Expression> expressions = FTFilterParser.parseConditions(rule);
                if (!expressions.isEmpty()) {
                    categoryConditions.addAll(expressions);
                    if (rawBuilder.length() > 0) {
                        rawBuilder.append(' ');
                    }
                    rawBuilder.append(rule);
                }
            }

            if (!categoryConditions.isEmpty()) {
                compiled.put(category, categoryConditions);
                raw.put(category, rawBuilder.toString());
            }
        }
        return new FTDataFilter(compiled, raw);
    }

    boolean isEmpty() {
        return conditions.isEmpty();
    }

    String getRawConditions(String category) {
        return rawConditions.get(category);
    }

    boolean isFiltered(DataType dataType, String measurement,
                       Map<String, Object> tags, Map<String, Object> fields) {
        String category = categoryOf(dataType);
        if (category == null) {
            return false;
        }

        List<FTFilterParser.Expression> expressions = conditions.get(category);
        if (expressions == null || expressions.isEmpty()) {
            return false;
        }

        FilterValues values = new FilterValues(category, measurement, tags, fields);
        for (FTFilterParser.Expression expression : expressions) {
            if (expression.eval(values)) {
                return true;
            }
        }
        return false;
    }

    static String categoryOf(DataType dataType) {
        if (dataType == null) {
            return null;
        }
        switch (dataType) {
            case LOG:
                return CATEGORY_LOGGING;
            case RUM_APP:
            case RUM_APP_ERROR_SAMPLED:
            case RUM_WEBVIEW:
            case RUM_WEBVIEW_ERROR_SAMPLED:
                return CATEGORY_RUM;
            default:
                return null;
        }
    }

    private static String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String normalized = category.trim().toLowerCase();
        if (CATEGORY_LOGGING.equals(normalized) || CATEGORY_RUM.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private static class FilterValues implements FTFilterParser.Values {
        private final String category;
        private final String measurement;
        private final Map<String, Object> tags;
        private final Map<String, Object> fields;

        FilterValues(String category, String measurement,
                     Map<String, Object> tags, Map<String, Object> fields) {
            this.category = category;
            this.measurement = measurement;
            this.tags = tags;
            this.fields = fields;
        }

        @Override
        public Object get(String key) {
            if (key == null) {
                return FTFilterParser.MISSING;
            }
            if (tags != null && tags.containsKey(key)) {
                return tags.get(key);
            }
            if (fields != null && fields.containsKey(key)) {
                return fields.get(key);
            }
            if ("category".equals(key)) {
                return category;
            }
            if ("source".equals(key)) {
                return measurement;
            }
            if ("measurement".equals(key) || "class".equals(key)) {
                return measurement;
            }
            return FTFilterParser.MISSING;
        }
    }
}
