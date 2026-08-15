/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

/**
 * Parameters for the MCP {@code prompts/list} request.
 *
 * <p>
 * The cursor is used for pagination. A {@code null} cursor means
 * that the first page should be requested.
 * </p>
 */
public final class McpListPromptsParams {

    private final String cursor;

    private McpListPromptsParams(
            Builder builder
    ) {

        this.cursor =
                normalize(
                        builder.cursor
                );
    }

    public static Builder builder() {

        return new Builder();
    }

    public static McpListPromptsParams empty() {

        return builder()
                .build();
    }

    public String getCursor() {

        return cursor;
    }

    public boolean hasCursor() {

        return cursor != null;
    }

    private static String normalize(
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

    @Override
    public String toString() {

        return "McpListPromptsParams{" +
                "cursor='" + cursor + '\'' +
                '}';
    }

    public static final class Builder {

        private String cursor;

        private Builder() {
        }

        public Builder cursor(
                String cursor
        ) {

            this.cursor =
                    cursor;

            return this;
        }

        public McpListPromptsParams build() {

            return new McpListPromptsParams(
                    this
            );
        }
    }
}