/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

import kr.co.goms.gomsbook.ai.mcp.common.McpAnnotations;
import kr.co.goms.gomsbook.ai.mcp.common.McpIcon;

/**
 * MCP resource link content block.
 *
 * <p>
 * Represents a resource that the MCP server is capable of reading.
 * </p>
 *
 * <pre>
 * {
 *     "type": "resource_link",
 *     "name": "chapter01.xhtml",
 *     "title": "Chapter 01",
 *     "uri": "file:///project/OEBPS/Text/chapter01.xhtml",
 *     "mimeType": "application/xhtml+xml",
 *     "size": 12540,
 *     "icons": [
 *         {
 *             "src": "https://example.com/xhtml.png",
 *             "mimeType": "image/png"
 *         }
 *     ],
 *     "_meta": {
 *         "kr.co.goms.gomsbook/source": "epub"
 *     }
 * }
 * </pre>
 */
public final class McpResourceLinkContent
        implements McpContent {

    /**
     * MCP content discriminator.
     */
    private final McpContentType type;

    /**
     * Optional resource icons.
     */
    private final List<McpIcon> icons;

    /**
     * Logical or programmatic resource name.
     */
    private final String name;

    /**
     * Optional human-readable title.
     */
    private final String title;

    /**
     * Resource URI.
     */
    private final String uri;

    /**
     * Optional resource description.
     */
    private final String description;

    /**
     * Optional MIME type.
     */
    private final String mimeType;

    /**
     * Optional MCP annotations.
     */
    private final McpAnnotations annotations;

    /**
     * Optional raw resource size in bytes.
     */
    private final Long size;

    /**
     * Optional MCP metadata.
     */
    @SerializedName("_meta")
    private final Map<String, Object> meta;

    private McpResourceLinkContent(
            Builder builder
    ) {

        this.type =
                McpContentType.RESOURCE_LINK;

        this.icons =
                normalizeIcons(
                        builder.icons
                );

        this.name =
                requireName(
                        builder.name
                );

        this.title =
                normalizeOptional(
                        builder.title
                );

        this.uri =
                requireUri(
                        builder.uri
                );

        this.description =
                normalizeOptional(
                        builder.description
                );

        this.mimeType =
                normalizeOptional(
                        builder.mimeType
                );

        this.annotations =
                normalizeAnnotations(
                        builder.annotations
                );

        this.size =
                validateSize(
                        builder.size
                );

        this.meta =
                normalizeMeta(
                        builder.meta
                );
    }

    @Override
    public McpContentType getType() {
        return type;
    }

    public List<McpIcon> getIcons() {
        return icons;
    }

    public boolean hasIcons() {
        return icons != null
                && !icons.isEmpty();
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public String getDescription() {
        return description;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public McpAnnotations getAnnotations() {
        return annotations;
    }

    public Long getSize() {
        return size;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public boolean hasMeta() {
        return meta != null
                && !meta.isEmpty();
    }

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

    public static Builder builder() {
        return new Builder();
    }

    public static McpResourceLinkContent of(
            String name,
            String uri
    ) {

        return builder()
                .name(
                        name
                )
                .uri(
                        uri
                )
                .build();
    }

    private static List<McpIcon> normalizeIcons(
            List<McpIcon> icons
    ) {

        if (icons == null
                || icons.isEmpty()) {

            return null;
        }

        List<McpIcon> normalized =
                new ArrayList<>();

        for (McpIcon icon : icons) {

            McpIcon safeIcon =
                    Objects.requireNonNull(
                            icon,
                            "MCP resource link icon must not be null."
                    );

            normalized.add(
                    safeIcon
            );
        }

        return Collections.unmodifiableList(
                normalized
        );
    }

    private static String requireName(
            String name
    ) {

        Objects.requireNonNull(
                name,
                "MCP resource link name must not be null."
        );

        String normalized =
                name.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "MCP resource link name must not be blank."
            );
        }

        return normalized;
    }

    private static String requireUri(
            String uri
    ) {

        Objects.requireNonNull(
                uri,
                "MCP resource link URI must not be null."
        );

        String normalized =
                uri.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "MCP resource link URI must not be blank."
            );
        }

        return normalized;
    }

    private static String normalizeOptional(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
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

    private static Long validateSize(
            Long size
    ) {

        if (size == null) {
            return null;
        }

        if (size < 0L) {
            throw new IllegalArgumentException(
                    "MCP resource link size must not be negative."
            );
        }

        return size;
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
     * Builder for {@link McpResourceLinkContent}.
     */
    public static final class Builder {

        private List<McpIcon> icons;

        private String name;

        private String title;

        private String uri;

        private String description;

        private String mimeType;

        private McpAnnotations annotations;

        private Long size;

        private Map<String, Object> meta;

        private Builder() {
        }

        public Builder icons(
                List<McpIcon> icons
        ) {

            this.icons =
                    icons == null
                            ? null
                            : new ArrayList<>(
                                    icons
                            );

            return this;
        }

        public Builder addIcon(
                McpIcon icon
        ) {

            Objects.requireNonNull(
                    icon,
                    "MCP resource link icon must not be null."
            );

            if (this.icons == null) {

                this.icons =
                        new ArrayList<>();
            }

            this.icons.add(
                    icon
            );

            return this;
        }

        public Builder name(
                String name
        ) {

            this.name =
                    name;

            return this;
        }

        public Builder title(
                String title
        ) {

            this.title =
                    title;

            return this;
        }

        public Builder uri(
                String uri
        ) {

            this.uri =
                    uri;

            return this;
        }

        public Builder description(
                String description
        ) {

            this.description =
                    description;

            return this;
        }

        public Builder mimeType(
                String mimeType
        ) {

            this.mimeType =
                    mimeType;

            return this;
        }

        public Builder annotations(
                McpAnnotations annotations
        ) {

            this.annotations =
                    annotations;

            return this;
        }

        public Builder size(
                Long size
        ) {

            this.size =
                    size;

            return this;
        }

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

        public McpResourceLinkContent build() {

            return new McpResourceLinkContent(
                    this
            );
        }
    }
}