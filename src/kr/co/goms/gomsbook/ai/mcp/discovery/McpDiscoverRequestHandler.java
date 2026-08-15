/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.discovery;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestContext;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpMethod;

/**
 * Handles the MCP {@code server/discover} request.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This handler delegates server discovery to
 * {@link McpDiscoveryService}.
 * </p>
 */
public final class McpDiscoverRequestHandler
        implements McpRequestHandler {

    private final McpDiscoveryService discoveryService;


    /**
     * Creates a discovery request handler.
     *
     * @param discoveryService discovery service
     */
    public McpDiscoverRequestHandler(
            McpDiscoveryService discoveryService
    ) {

        this.discoveryService =
                Objects.requireNonNull(
                        discoveryService,
                        "MCP discovery service must not be null."
                );
    }


    /*
     * ------------------------------------------------------------
     * McpRequestHandler
     * ------------------------------------------------------------
     */

    @Override
    public String getMethod() {

        return McpMethod.SERVER_DISCOVER;
    }


    @Override
    public McpResult handle(
            McpRequestContext context
    ) {

        Objects.requireNonNull(
                context,
                "MCP request context must not be null."
        );

        validateMethod(
                context
        );

        McpDiscoverParams params =
                McpDiscoverParams.create();

        return discoveryService.discover(
                params
        );
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static void validateMethod(
            McpRequestContext context
    ) {

        String method =
                context.getMethod();

        if (!McpMethod.SERVER_DISCOVER.equals(
                method
        )) {

            throw new IllegalArgumentException(
                    "Invalid MCP method for discovery handler: "
                            + method
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Accessor
     * ------------------------------------------------------------
     */

    public McpDiscoveryService getDiscoveryService() {
        return discoveryService;
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "McpDiscoverRequestHandler{"
                + "method='"
                + McpMethod.SERVER_DISCOVER
                + '\''
                + '}';
    }
}