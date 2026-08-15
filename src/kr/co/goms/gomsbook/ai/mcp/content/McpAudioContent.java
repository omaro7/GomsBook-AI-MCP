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
 * MCP audio content block.
 *
 * <p>
 * Represents Base64 encoded audio data transported through MCP.
 * </p>
 *
 * <pre>
 * {
 *     "type": "audio",
 *     "data": "...base64...",
 *     "mimeType": "audio/wav",
 *     "_meta": {
 *         "kr.co.goms.gomsbook/source": "narration"
 *     }
 * }
 * </pre>
 */
public final class McpAudioContent
        implements McpContent {

    /**
     * MCP content discriminator.
     */
    private final McpContentType type;

    /**
     * Base64 encoded audio data.
     */
    private final String data;

    /**
     * Audio MIME type.
     */
    private final String mimeType;

    /**
     * Optional MCP annotations.
     */
    private McpAnnotations annotations;

    /**
     * Optional MCP metadata.
     */
    @SerializedName("_meta")
    private final Map<String, Object> meta;

    private McpAudioContent(
            Builder builder
    ) {

        this.type =
                McpContentType.AUDIO;

        this.data =
                requireData(
                        builder.data
                );

        this.mimeType =
                requireMimeType(
                        builder.mimeType
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
     * @return {@link McpContentType#AUDIO}
     */
    @Override
    public McpContentType getType() {
        return type;
    }

    /**
     * Returns Base64 encoded audio data.
     *
     * @return audio data
     */
    public String getData() {
        return data;
    }

    /**
     * Returns the audio MIME type.
     *
     * @return MIME type
     */
    public String getMimeType() {
        return mimeType;
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
     * Returns whether metadata exists.
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
     * Creates a new builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates audio content.
     *
     * @param data Base64 encoded audio data
     * @param mimeType audio MIME type
     * @return audio content
     */
    public static McpAudioContent of(
            String data,
            String mimeType
    ) {

        return builder()
                .data(
                        data
                )
                .mimeType(
                        mimeType
                )
                .build();
    }

    /**
     * Creates audio content with annotations.
     *
     * @param data Base64 encoded audio data
     * @param mimeType audio MIME type
     * @param annotations annotations
     * @return audio content
     */
    public static McpAudioContent of(
            String data,
            String mimeType,
            McpAnnotations annotations
    ) {

        return builder()
                .data(
                        data
                )
                .mimeType(
                        mimeType
                )
                .annotations(
                        annotations
                )
                .build();
    }

    private static String requireData(
            String data
    ) {

        Objects.requireNonNull(
                data,
                "MCP audio data must not be null."
        );

        if (data.isBlank()) {
            throw new IllegalArgumentException(
                    "MCP audio data must not be blank."
            );
        }

        return data;
    }

    private static String requireMimeType(
            String mimeType
    ) {

        Objects.requireNonNull(
                mimeType,
                "MCP audio mimeType must not be null."
        );

        String normalized =
                mimeType.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "MCP audio mimeType must not be blank."
            );
        }

        if (!normalized.startsWith(
                "audio/"
        )) {

            throw new IllegalArgumentException(
                    "MCP audio mimeType must start with 'audio/': "
                            + mimeType
            );
        }

        return normalized;
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
     * Builder for {@link McpAudioContent}.
     */
    public static final class Builder {

        private String data;

        private String mimeType;

        private McpAnnotations annotations;

        private Map<String, Object> meta;

        private Builder() {
        }

        /**
         * Sets Base64 encoded audio data.
         *
         * @param data audio data
         * @return this builder
         */
        public Builder data(
                String data
        ) {

            this.data =
                    data;

            return this;
        }

        /**
         * Sets audio MIME type.
         *
         * @param mimeType MIME type
         * @return this builder
         */
        public Builder mimeType(
                String mimeType
        ) {

            this.mimeType =
                    mimeType;

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
         * Builds audio content.
         *
         * @return audio content
         */
        public McpAudioContent build() {

            return new McpAudioContent(
                    this
            );
        }
    }
}