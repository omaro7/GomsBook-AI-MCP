/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.Objects;

/**
 * Parameters for the MCP
 * {@code resources/templates/list} operation.
 */
public final class McpListResourceTemplatesParams {

    private final String cursor;

    private McpListResourceTemplatesParams(
            Builder builder) {

        this.cursor =
                normalizeText(
                        builder.cursor);
    }

    /**
     * Returns the pagination cursor.
     *
     * @return cursor or {@code null}
     */
    public String getCursor() {

        return cursor;
    }

    public boolean hasCursor() {

        return cursor != null;
    }

    public static Builder builder() {

        return new Builder();
    }

    public static McpListResourceTemplatesParams empty() {

        return builder()
                .build();
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

    @Override
    public int hashCode() {

        return Objects.hash(
                cursor);
    }

    @Override
    public boolean equals(
            Object object) {

        if (this == object) {

            return true;
        }

        if (!(object instanceof McpListResourceTemplatesParams)) {

            return false;
        }

        McpListResourceTemplatesParams other =
                (McpListResourceTemplatesParams) object;

        return Objects.equals(
                cursor,
                other.cursor);
    }

    @Override
    public String toString() {

        return "McpListResourceTemplatesParams{"
                + "cursor='"
                + cursor
                + '\''
                + '}';
    }

    public static final class Builder {

        private String cursor;

        private Builder() {
        }

        public Builder cursor(
                String cursor) {

            this.cursor =
                    cursor;

            return this;
        }

        public McpListResourceTemplatesParams build() {

            return new McpListResourceTemplatesParams(
                    this);
        }
    }
}