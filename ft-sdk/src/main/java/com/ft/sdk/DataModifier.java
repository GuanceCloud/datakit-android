package com.ft.sdk;

/**
 * Field replacement, suitable for global field replacement scenarios.
 * If you expect line-by-line analysis and implement per-line data replacement, please use {@link LineDataModifier }
 * In terms of performance, {@link DataModifier} > {@link LineDataModifier }
 */
public interface DataModifier {

    /**
     * Modify a specific field
     *
     * @param key   Field name
     * @param value Field value (original value)
     * @return New value, return null to indicate no change
     */
    Object modify(String key, Object value);
}