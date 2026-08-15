/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MCP Resource contents.
 *
 * <p>
 * Represents the actual contents returned by
 * {@code resources/read}.
 * </p>
 *
 * <p>
 * A resource content is either text-based or binary-based.
 * Text resources use {@code text}, while binary resources
 * use Base64 encoded {@code blob}.
 * </p>
 */
public final class McpResourceContents {

    private final String uri;

    private final String mimeType;

    private final String text;

    private final String blob;

    private final Map<String, Object> meta;

    private McpResourceContents(
            Builder builder) {

        this.uri =
                requireText(
                        builder.uri,
                        "uri");

        this.mimeType =
                normalizeText(
                        builder.mimeType);

        /*
         * Resource contents must preserve the original value.
         *
         * In particular, an empty text resource ("") or
         * an empty Base64 blob ("") may still be valid.
         */
        this.text =
                builder.text;

        this.blob =
                builder.blob;

        validateContent(
                text,
                blob);

        this.meta =
                immutableCopy(
                        builder.meta);
    }

    public String getUri() {

        return uri;
    }

    public String getMimeType() {

        return mimeType;
    }

    public String getText() {

        return text;
    }

    public String getBlob() {

        return blob;
    }

    /**
     * MCP JSON field: "_meta".
     */
    public Map<String, Object> getMeta() {

        return meta;
    }

    public boolean hasMimeType() {

        return mimeType != null;
    }

    /**
     * Returns whether this content contains a text field.
     *
     * <p>
     * An empty string is still considered valid text content.
     * </p>
     */
    public boolean hasText() {

        return text != null;
    }

    /**
     * Returns whether this content contains a blob field.
     *
     * <p>
     * An empty string is still considered a valid blob value.
     * </p>
     */
    public boolean hasBlob() {

        return blob != null;
    }

    public boolean hasMeta() {

        return !meta.isEmpty();
    }

    public boolean isText() {

        return text != null;
    }

    public boolean isBinary() {

        return blob != null;
    }

    public static Builder builder() {

        return new Builder();
    }

    /**
     * Creates text resource contents.
     *
     * @param uri resource URI
     * @param mimeType MIME type
     * @param text text contents
     *
     * @return resource contents
     */
    public static McpResourceContents text(
            String uri,
            String mimeType,
            String text) {

        return builder()
                .uri(
                        uri)
                .mimeType(
                        mimeType)
                .text(
                        text)
                .build();
    }

    /**
     * Creates binary resource contents.
     *
     * <p>
     * The blob value must be Base64 encoded by the caller.
     * </p>
     *
     * @param uri resource URI
     * @param mimeType MIME type
     * @param blob Base64 encoded contents
     *
     * @return resource contents
     */
    public static McpResourceContents blob(
            String uri,
            String mimeType,
            String blob) {

        return builder()
                .uri(
                        uri)
                .mimeType(
                        mimeType)
                .blob(
                        blob)
                .build();
    }

    private static String requireText(
            String value,
            String fieldName) {

        String normalized =
                normalizeText(
                        value);

        if (normalized == null) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must not be null or blank.");
        }

        return normalized;
    }

    private static String normalizeText(
            String value) {

        if (value == null) {

            return null;
        }

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {

            return null;
        }

        return normalized;
    }

    /**
     * Validates that exactly one content representation
     * is present.
     *
     * <p>
     * Empty strings are allowed. Only {@code null} means that
     * the field was not provided.
     * </p>
     */
    private static void validateContent(
            String text,
            String blob) {

        if (text == null
                && blob == null) {

            throw new IllegalArgumentException(
                    "Either text or blob must be provided.");
        }

        if (text != null
                && blob != null) {

            throw new IllegalArgumentException(
                    "text and blob must not be provided together.");
        }
    }

    private static Map<String, Object> immutableCopy(
            Map<String, Object> source) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        source));
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                uri,
                mimeType,
                text,
                blob,
                meta);
    }

    @Override
    public boolean equals(
            Object object) {

        if (this == object) {

            return true;
        }

        if (!(object instanceof McpResourceContents)) {

            return false;
        }

        McpResourceContents other =
                (McpResourceContents) object;

        return Objects.equals(
                        uri,
                        other.uri)
                && Objects.equals(
                        mimeType,
                        other.mimeType)
                && Objects.equals(
                        text,
                        other.text)
                && Objects.equals(
                        blob,
                        other.blob)
                && Objects.equals(
                        meta,
                        other.meta);
    }

    @Override
    public String toString() {

        return "McpResourceContents{"
                + "uri='"
                + uri
                + '\''
                + ", mimeType='"
                + mimeType
                + '\''
                + ", text="
                + (text != null)
                + ", blob="
                + (blob != null)
                + '}';
    }

    public static final class Builder {

        private String uri;

        private String mimeType;

        private String text;

        private String blob;

        private Map<String, Object> meta;

        private Builder() {
        }

        public Builder uri(
                String uri) {

            this.uri =
                    uri;

            return this;
        }

        public Builder mimeType(
                String mimeType) {

            this.mimeType =
                    mimeType;

            return this;
        }

        public Builder text(
                String text) {

            this.text =
                    text;

            return this;
        }

        public Builder blob(
                String blob) {

            this.blob =
                    blob;

            return this;
        }

        public Builder meta(
                Map<String, Object> meta) {

            this.meta =
                    meta;

            return this;
        }

        public Builder meta(
                String key,
                Object value) {

            if (key == null
                    || key.isBlank()) {

                throw new IllegalArgumentException(
                        "meta key must not be null or blank.");
            }

            if (this.meta == null) {

                this.meta =
                        new LinkedHashMap<>();
            }

            this.meta.put(
                    key,
                    value);

            return this;
        }

        public McpResourceContents build() {

            return new McpResourceContents(
                    this);
        }
    }
}