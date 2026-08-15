/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * MCP icon definition.
 *
 * <p>
 * Represents an optionally sized icon that may be
 * displayed in an MCP client UI.
 * </p>
 */
public final class McpIcon {

    /**
     * Icon URI.
     */
    private final String src;

    /**
     * Optional MIME type.
     */
    private final String mimeType;

    /**
     * Optional supported sizes.
     */
    private final List<String> sizes;

    /**
     * Optional target UI theme.
     */
    private final Theme theme;

    private McpIcon(
            Builder builder
    ) {

        this.src =
                requireSrc(
                        builder.src
                );

        this.mimeType =
                normalizeOptional(
                        builder.mimeType
                );

        this.sizes =
                normalizeSizes(
                        builder.sizes
                );

        this.theme =
                builder.theme;
    }

    public String getSrc() {
        return src;
    }

    public String getMimeType() {
        return mimeType;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public Theme getTheme() {
        return theme;
    }

    public boolean hasSizes() {
        return !sizes.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static McpIcon of(
            String src
    ) {

        return builder()
                .src(
                        src
                )
                .build();
    }

    private static String requireSrc(
            String src
    ) {

        Objects.requireNonNull(
                src,
                "MCP icon src must not be null."
        );

        String normalized =
                src.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "MCP icon src must not be blank."
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

    private static List<String> normalizeSizes(
            List<String> sizes
    ) {

        if (sizes == null
                || sizes.isEmpty()) {

            return Collections.emptyList();
        }

        List<String> normalized =
                new ArrayList<>();

        for (String size : sizes) {

            if (size == null
                    || size.isBlank()) {

                continue;
            }

            String value =
                    size.trim();

            if (!normalized.contains(
                    value
            )) {

                normalized.add(
                        value
                );
            }
        }

        return Collections.unmodifiableList(
                normalized
        );
    }

    public static final class Builder {

        private String src;

        private String mimeType;

        private List<String> sizes;

        private Theme theme;

        private Builder() {
        }

        public Builder src(
                String src
        ) {

            this.src =
                    src;

            return this;
        }

        public Builder mimeType(
                String mimeType
        ) {

            this.mimeType =
                    mimeType;

            return this;
        }

        public Builder sizes(
                List<String> sizes
        ) {

            this.sizes =
                    sizes == null
                            ? null
                            : new ArrayList<>(
                                    sizes
                            );

            return this;
        }

        public Builder addSize(
                String size
        ) {

            if (this.sizes == null) {
                this.sizes =
                        new ArrayList<>();
            }

            this.sizes.add(
                    size
            );

            return this;
        }

        public Builder theme(
                Theme theme
        ) {

            this.theme =
                    theme;

            return this;
        }

        public McpIcon build() {

            return new McpIcon(
                    this
            );
        }
    }

    /**
     * MCP icon target theme.
     */
    public enum Theme {

        @SerializedName("light")
        LIGHT,

        @SerializedName("dark")
        DARK
    }
}