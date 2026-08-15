/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.Objects;

/**
 * Parameters for the MCP {@code resources/list} operation.
 */
public final class McpListResourcesParams {

    private final String cursor;

    private McpListResourcesParams(
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

    public static McpListResourcesParams empty() {

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

        if (!(object instanceof McpListResourcesParams)) {

            return false;
        }

        McpListResourcesParams other =
                (McpListResourcesParams) object;

        return Objects.equals(
                cursor,
                other.cursor);
    }

    @Override
    public String toString() {

        return "McpListResourcesParams{"
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

        public McpListResourcesParams build() {

            return new McpListResourcesParams(
                    this);
        }
    }
}