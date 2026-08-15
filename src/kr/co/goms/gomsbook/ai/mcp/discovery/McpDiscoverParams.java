/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.discovery;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpRequestMetadata;

/**
 * Parameters for the MCP {@code server/discover} request.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * The {@code server/discover} request does not define any
 * method-specific parameters. It contains only the standard
 * MCP request metadata in {@code _meta}.
 * </p>
 *
 * <pre>
 * {
 *   "_meta": {
 *     "io.modelcontextprotocol/protocolVersion": "2026-07-28",
 *     "io.modelcontextprotocol/clientInfo": {
 *       "name": "ExampleClient",
 *       "version": "1.0.0"
 *     },
 *     "io.modelcontextprotocol/clientCapabilities": {}
 *   }
 * }
 * </pre>
 */
public final class McpDiscoverParams {

    private final McpRequestMetadata metadata;

    private McpDiscoverParams(
            Builder builder
    ) {

        this.metadata =
                Objects.requireNonNull(
                        builder.metadata,
                        "MCP discover metadata must not be null."
                );
    }

    /*
     * ------------------------------------------------------------
     * Factory
     * ------------------------------------------------------------
     */

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates discover params using minimum valid MCP metadata.
     *
     * @return discover params
     */
    public static McpDiscoverParams create() {

        return builder()
                .metadata(
                        McpRequestMetadata.create()
                )
                .build();
    }

    /**
     * Creates discover params using the supplied request metadata.
     *
     * @param metadata request metadata
     * @return discover params
     */
    public static McpDiscoverParams of(
            McpRequestMetadata metadata
    ) {

        return builder()
                .metadata(metadata)
                .build();
    }

    /*
     * ------------------------------------------------------------
     * Accessor
     * ------------------------------------------------------------
     */

    public McpRequestMetadata getMetadata() {
        return metadata;
    }

    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private McpRequestMetadata metadata =
                McpRequestMetadata.create();

        private Builder() {
        }

        public Builder metadata(
                McpRequestMetadata metadata
        ) {

            this.metadata =
                    metadata;

            return this;
        }

        public McpDiscoverParams build() {

            return new McpDiscoverParams(
                    this
            );
        }
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

        if (!(object instanceof McpDiscoverParams)) {
            return false;
        }

        McpDiscoverParams other =
                (McpDiscoverParams) object;

        return Objects.equals(
                metadata,
                other.metadata
        );
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                metadata
        );
    }

    @Override
    public String toString() {

        return "McpDiscoverParams{"
                + "metadata="
                + metadata
                + '}';
    }
}