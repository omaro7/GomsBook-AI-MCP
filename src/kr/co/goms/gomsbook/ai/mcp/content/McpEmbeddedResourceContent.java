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
import kr.co.goms.gomsbook.ai.mcp.resources.McpResourceContents;

/**
 * MCP embedded resource content block.
 *
 * <p>
 * Represents resource contents embedded directly
 * inside an MCP content block.
 * </p>
 *
 * <pre>
 * {
 *     "type": "resource",
 *     "resource": {
 *         "uri": "file:///project/chapter01.xhtml",
 *         "mimeType": "application/xhtml+xml",
 *         "text": "..."
 *     },
 *     "_meta": {
 *         "kr.co.goms.gomsbook/source": "epub"
 *     }
 * }
 * </pre>
 */
public final class McpEmbeddedResourceContent
        implements McpContent {

    /**
     * MCP content discriminator.
     */
    private final McpContentType type;

    /**
     * Embedded resource contents.
     */
    private final McpResourceContents resource;

    /**
     * Optional MCP annotations.
     */
    private final McpAnnotations annotations;

    /**
     * Optional MCP metadata.
     */
    @SerializedName("_meta")
    private final Map<String, Object> meta;

    private McpEmbeddedResourceContent(
            Builder builder
    ) {

        this.type =
                McpContentType.RESOURCE;

        this.resource =
                requireResource(
                        builder.resource
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
     * @return {@link McpContentType#RESOURCE}
     */
    @Override
    public McpContentType getType() {
        return type;
    }

    /**
     * Returns embedded resource contents.
     *
     * @return resource contents
     */
    public McpResourceContents getResource() {
        return resource;
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
     * @return value or {@code null}
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
     * Creates a new builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates embedded resource content.
     *
     * @param resource resource contents
     * @return embedded resource content
     */
    public static McpEmbeddedResourceContent of(
            McpResourceContents resource
    ) {

        return builder()
                .resource(
                        resource
                )
                .build();
    }

    /**
     * Creates embedded resource content with annotations.
     *
     * @param resource resource contents
     * @param annotations annotations
     * @return embedded resource content
     */
    public static McpEmbeddedResourceContent of(
            McpResourceContents resource,
            McpAnnotations annotations
    ) {

        return builder()
                .resource(
                        resource
                )
                .annotations(
                        annotations
                )
                .build();
    }

    private static McpResourceContents requireResource(
            McpResourceContents resource
    ) {

        return Objects.requireNonNull(
                resource,
                "MCP embedded resource must not be null."
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
     * Builder for {@link McpEmbeddedResourceContent}.
     */
    public static final class Builder {

        private McpResourceContents resource;

        private McpAnnotations annotations;

        private Map<String, Object> meta;

        private Builder() {
        }

        /**
         * Sets embedded resource contents.
         *
         * @param resource resource contents
         * @return this builder
         */
        public Builder resource(
                McpResourceContents resource
        ) {

            this.resource =
                    resource;

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
         * Builds embedded resource content.
         *
         * @return embedded resource content
         */
        public McpEmbeddedResourceContent build() {

            return new McpEmbeddedResourceContent(
                    this
            );
        }
    }
}