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
 * MCP Resource metadata.
 *
 * <p>
 * Represents a resource exposed by an MCP server through
 * the {@code resources/list} operation.
 * </p>
 *
 * <p>
 * The actual resource contents are represented separately
 * by {@link McpResourceContents}.
 * </p>
 */
public final class McpResource {

    private final String uri;

    private final String name;

    private final String title;

    private final String description;

    private final String mimeType;

    private final Map<String, Object> annotations;

    private final Long size;

    private final Map<String, Object> meta;

    private McpResource(
            Builder builder) {

        this.uri =
                requireText(
                        builder.uri,
                        "uri");

        this.name =
                requireText(
                        builder.name,
                        "name");

        this.title =
                normalizeText(
                        builder.title);

        this.description =
                normalizeText(
                        builder.description);

        this.mimeType =
                normalizeText(
                        builder.mimeType);

        this.annotations =
                immutableCopy(
                        builder.annotations);

        this.size =
                validateSize(
                        builder.size);

        this.meta =
                immutableCopy(
                        builder.meta);
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public Long getSize() {
        return size;
    }

    /**
     * MCP JSON field: "_meta".
     */
    public Map<String, Object> getMeta() {
        return meta;
    }

    public boolean hasTitle() {
        return title != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasMimeType() {
        return mimeType != null;
    }

    public boolean hasAnnotations() {
        return !annotations.isEmpty();
    }

    public boolean hasSize() {
        return size != null;
    }

    public boolean hasMeta() {
        return !meta.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
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

    private static Long validateSize(
            Long size) {

        if (size == null) {
            return null;
        }

        if (size.longValue() < 0L) {
            throw new IllegalArgumentException(
                    "size must not be negative.");
        }

        return size;
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
                name,
                title,
                description,
                mimeType,
                annotations,
                size,
                meta);
    }

    @Override
    public boolean equals(
            Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpResource)) {
            return false;
        }

        McpResource other =
                (McpResource) object;

        return Objects.equals(
                        uri,
                        other.uri)
                && Objects.equals(
                        name,
                        other.name)
                && Objects.equals(
                        title,
                        other.title)
                && Objects.equals(
                        description,
                        other.description)
                && Objects.equals(
                        mimeType,
                        other.mimeType)
                && Objects.equals(
                        annotations,
                        other.annotations)
                && Objects.equals(
                        size,
                        other.size)
                && Objects.equals(
                        meta,
                        other.meta);
    }

    @Override
    public String toString() {

        return "McpResource{"
                + "uri='"
                + uri
                + '\''
                + ", name='"
                + name
                + '\''
                + ", title='"
                + title
                + '\''
                + ", description='"
                + description
                + '\''
                + ", mimeType='"
                + mimeType
                + '\''
                + ", size="
                + size
                + '}';
    }

    public static final class Builder {

        private String uri;

        private String name;

        private String title;

        private String description;

        private String mimeType;

        private Map<String, Object> annotations;

        private Long size;

        private Map<String, Object> meta;

        private Builder() {
        }

        public Builder uri(
                String uri) {

            this.uri = uri;

            return this;
        }

        public Builder name(
                String name) {

            this.name = name;

            return this;
        }

        public Builder title(
                String title) {

            this.title = title;

            return this;
        }

        public Builder description(
                String description) {

            this.description =
                    description;

            return this;
        }

        public Builder mimeType(
                String mimeType) {

            this.mimeType =
                    mimeType;

            return this;
        }

        public Builder annotations(
                Map<String, Object> annotations) {

            this.annotations =
                    annotations;

            return this;
        }

        public Builder annotation(
                String key,
                Object value) {

            if (key == null
                    || key.isBlank()) {

                throw new IllegalArgumentException(
                        "annotation key must not be null or blank.");
            }

            if (this.annotations == null) {

                this.annotations =
                        new LinkedHashMap<>();
            }

            this.annotations.put(
                    key,
                    value);

            return this;
        }

        public Builder size(
                Long size) {

            this.size = size;

            return this;
        }

        public Builder size(
                long size) {

            this.size =
                    Long.valueOf(
                            size);

            return this;
        }

        public Builder meta(
                Map<String, Object> meta) {

            this.meta = meta;

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

        public McpResource build() {

            return new McpResource(
                    this);
        }
    }
}