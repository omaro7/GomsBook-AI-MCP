/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.Objects;

/**
 * Parameters for the MCP {@code resources/read} operation.
 */
public final class McpReadResourceParams {

    private final String uri;

    private McpReadResourceParams(
            Builder builder) {

        this.uri =
                requireText(
                        builder.uri,
                        "uri");
    }

    /**
     * Returns the resource URI to read.
     *
     * @return resource URI
     */
    public String getUri() {

        return uri;
    }

    public static Builder builder() {

        return new Builder();
    }

    public static McpReadResourceParams of(
            String uri) {

        return builder()
                .uri(
                        uri)
                .build();
    }

    private static String requireText(
            String value,
            String fieldName) {

        if (value == null) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must not be null.");
        }

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must not be blank.");
        }

        return normalized;
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                uri);
    }

    @Override
    public boolean equals(
            Object object) {

        if (this == object) {

            return true;
        }

        if (!(object instanceof McpReadResourceParams)) {

            return false;
        }

        McpReadResourceParams other =
                (McpReadResourceParams) object;

        return Objects.equals(
                uri,
                other.uri);
    }

    @Override
    public String toString() {

        return "McpReadResourceParams{"
                + "uri='"
                + uri
                + '\''
                + '}';
    }

    public static final class Builder {

        private String uri;

        private Builder() {
        }

        public Builder uri(
                String uri) {

            this.uri =
                    uri;

            return this;
        }

        public McpReadResourceParams build() {

            return new McpReadResourceParams(
                    this);
        }
    }
}