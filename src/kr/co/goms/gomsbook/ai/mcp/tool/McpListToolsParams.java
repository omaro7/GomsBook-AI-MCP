/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.Objects;

/**
 * Parameters for the MCP {@code tools/list} request.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * The cursor is opaque to MCP clients and is used by the server
 * to continue a paginated tool listing.
 * </p>
 */
public final class McpListToolsParams {

    private final String cursor;


    /*
     * ------------------------------------------------------------
     * Constructor
     * ------------------------------------------------------------
     */

    private McpListToolsParams(
            Builder builder
    ) {

        this.cursor =
                normalizeCursor(
                        builder.cursor
                );
    }


    /*
     * ------------------------------------------------------------
     * Factory
     * ------------------------------------------------------------
     */

    /**
     * Creates empty tools/list parameters.
     *
     * @return empty parameters
     */
    public static McpListToolsParams create() {

        return builder()
                .build();
    }


    /**
     * Creates tools/list parameters with a cursor.
     *
     * @param cursor pagination cursor
     * @return parameters
     */
    public static McpListToolsParams ofCursor(
            String cursor
    ) {

        return builder()
                .cursor(
                        cursor
                )
                .build();
    }


    public static Builder builder() {
        return new Builder();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public String getCursor() {
        return cursor;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasCursor() {
        return cursor != null;
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

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


        public McpListToolsParams build() {

            return new McpListToolsParams(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Normalization
     * ------------------------------------------------------------
     */

    private static String normalizeCursor(
            String cursor
    ) {

        if (cursor == null) {
            return null;
        }


        String normalized =
                cursor.trim();


        if (normalized.isEmpty()) {
            return null;
        }


        return normalized;
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpListToolsParams)) {
            return false;
        }


        McpListToolsParams other =
                (McpListToolsParams) object;


        return Objects.equals(
                cursor,
                other.cursor
        );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                cursor
        );
    }


    @Override
    public String toString() {

        return "McpListToolsParams{"
                + "cursor='"
                + cursor
                + '\''
                + '}';
    }
}