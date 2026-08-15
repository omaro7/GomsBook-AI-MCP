/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.content;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

import kr.co.goms.gomsbook.ai.mcp.common.McpAnnotations;

/**
 * MCP text content block.
 *
 * <p>
 * Represents textual content transported through MCP.
 * </p>
 *
 * <pre>
 * {
 *     "type": "text",
 *     "text": "Hello",
 *     "_meta": {
 *         "kr.co.goms.gomsbook/contentId": "content-001"
 *     }
 * }
 * </pre>
 */
public final class McpTextContent
        implements McpContent {

    /**
     * MCP content discriminator.
     */
    private final McpContentType type;

    /**
     * Text payload.
     */
    private final String text;

    /**
     * Optional MCP annotations.
     */
    private final McpAnnotations annotations;

    /**
     * Optional MCP metadata.
     */
    @SerializedName("_meta")
    private final Map<String, Object> meta;

    private McpTextContent(
            Builder builder
    ) {

        this.type =
                McpContentType.TEXT;

        this.text =
                requireText(
                        builder.text
                );

        this.annotations =
                normalizeAnnotations(
                        builder.annotations
                );

        this.meta =
                normalizeMeta(
                        builder.meta
                );
    }

    /**
     * Returns the MCP content type.
     *
     * @return {@link McpContentType#TEXT}
     */
    @Override
    public McpContentType getType() {
        return type;
    }

    /**
     * Returns the text payload.
     *
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns optional annotations.
     *
     * @return annotations or {@code null}
     */
    @Override
    public McpAnnotations getAnnotations() {
        return annotations;
    }

    /**
     * Returns optional MCP metadata.
     *
     * @return immutable metadata map or {@code null}
     */
    public Map<String, Object> getMeta() {
        return meta;
    }

    /**
     * Returns whether MCP metadata exists.
     *
     * @return {@code true} if metadata exists
     */
    public boolean hasMeta() {

        return meta != null
                && !meta.isEmpty();
    }

    /**
     * Returns a metadata value.
     *
     * @param key metadata key
     * @return metadata value or {@code null}
     */
    public Object getMeta(
            String key
    ) {

        if (meta == null
                || key == null) {

            return null;
        }

        return meta.get(
                key
        );
    }

    /**
     * Creates a builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates text content.
     *
     * @param text text payload
     * @return text content
     */
    public static McpTextContent of(
            String text
    ) {

        return builder()
                .text(
                        text
                )
                .build();
    }

    /**
     * Creates text content with annotations.
     *
     * @param text text payload
     * @param annotations annotations
     * @return text content
     */
    public static McpTextContent of(
            String text,
            McpAnnotations annotations
    ) {

        return builder()
                .text(
                        text
                )
                .annotations(
                        annotations
                )
                .build();
    }

    private static String requireText(
            String text
    ) {

        return Objects.requireNonNull(
                text,
                "MCP text content must not be null."
        );
    }

    private static McpAnnotations normalizeAnnotations(
            McpAnnotations annotations
    ) {

        if (annotations == null
                || annotations.isEmpty()) {

            return null;
        }

        return annotations;
    }

    private static Map<String, Object> normalizeMeta(
            Map<String, Object> meta
    ) {

        if (meta == null
                || meta.isEmpty()) {

            return null;
        }

        Map<String, Object> normalized =
                new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry
                : meta.entrySet()) {

            String key =
                    normalizeMetaKey(
                            entry.getKey()
                    );

            normalized.put(
                    key,
                    entry.getValue()
            );
        }

        if (normalized.isEmpty()) {
            return null;
        }

        return Collections.unmodifiableMap(
                normalized
        );
    }

    private static String normalizeMetaKey(
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

    /**
     * Builder for {@link McpTextContent}.
     */
    public static final class Builder {

        private String text;

        private McpAnnotations annotations;

        private Map<String, Object> meta;

        private Builder() {
        }

        /**
         * Sets the text payload.
         *
         * @param text text
         * @return this builder
         */
        public Builder text(
                String text
        ) {

            this.text =
                    text;

            return this;
        }

        /**
         * Sets optional annotations.
         *
         * @param annotations annotations
         * @return this builder
         */
        public Builder annotations(
                McpAnnotations annotations
        ) {

            this.annotations =
                    annotations;

            return this;
        }

        /**
         * Sets MCP metadata.
         *
         * @param meta metadata map
         * @return this builder
         */
        public Builder meta(
                Map<String, Object> meta
        ) {

            this.meta =
                    meta == null
                            ? null
                            : new LinkedHashMap<>(
                                    meta
                            );

            return this;
        }

        /**
         * Adds MCP metadata.
         *
         * @param key metadata key
         * @param value metadata value
         * @return this builder
         */
        public Builder putMeta(
                String key,
                Object value
        ) {

            String normalizedKey =
                    normalizeMetaKey(
                            key
                    );

            if (this.meta == null) {

                this.meta =
                        new LinkedHashMap<>();
            }

            this.meta.put(
                    normalizedKey,
                    value
            );

            return this;
        }

        /**
         * Builds the text content.
         *
         * @return text content
         */
        public McpTextContent build() {

            return new McpTextContent(
                    this
            );
        }
    }
}