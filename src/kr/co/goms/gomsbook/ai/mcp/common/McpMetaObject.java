/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MCP metadata object.
 *
 * <p>
 * Represents the contents of an MCP {@code _meta} field.
 * </p>
 *
 * <p>
 * MCP protocol version: 2026-07-28
 * </p>
 */
public final class McpMetaObject {

    private final Map<String, Object> values;

    private McpMetaObject(
            Builder builder
    ) {

        this.values =
                Collections.unmodifiableMap(
                        new LinkedHashMap<>(
                                builder.values
                        )
                );
    }

    /**
     * Returns all metadata values.
     *
     * @return immutable metadata map
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Returns a metadata value.
     *
     * @param key metadata key
     * @return value or {@code null}
     */
    public Object get(
            String key
    ) {

        return values.get(
                key
        );
    }

    /**
     * Returns whether a metadata key exists.
     *
     * @param key metadata key
     * @return true if present
     */
    public boolean containsKey(
            String key
    ) {

        return values.containsKey(
                key
        );
    }

    /**
     * Returns whether this metadata object is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static McpMetaObject empty() {
        return builder().build();
    }

    public static McpMetaObject of(
            String key,
            Object value
    ) {

        return builder()
                .put(
                        key,
                        value
                )
                .build();
    }

    /**
     * Builder for {@link McpMetaObject}.
     */
    public static final class Builder {

        private final Map<String, Object> values =
                new LinkedHashMap<>();

        private Builder() {
        }

        public Builder put(
                String key,
                Object value
        ) {

            String normalizedKey =
                    validateKey(
                            key
                    );

            values.put(
                    normalizedKey,
                    value
            );

            return this;
        }

        public Builder putAll(
                Map<String, ?> values
        ) {

            if (values == null
                    || values.isEmpty()) {

                return this;
            }

            values.forEach(
                    this::put
            );

            return this;
        }

        public Builder remove(
                String key
        ) {

            if (key != null) {
                values.remove(
                        key.trim()
                );
            }

            return this;
        }

        public McpMetaObject build() {

            return new McpMetaObject(
                    this
            );
        }

        private static String validateKey(
                String key
        ) {

            Objects.requireNonNull(
                    key,
                    "MCP metadata key must not be null."
            );

            String normalized =
                    key.trim();

            if (normalized.isEmpty()) {
                throw new IllegalArgumentException(
                        "MCP metadata key must not be blank."
                );
            }

            return normalized;
        }
    }
}