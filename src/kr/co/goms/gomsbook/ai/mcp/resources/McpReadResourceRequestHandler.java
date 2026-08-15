/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodec;
import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodecException;
import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestContext;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpMethod;

/**
 * Handles the MCP {@code resources/read} request.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Responsibilities:
 * </p>
 *
 * <ul>
 *     <li>validate the dispatched MCP method</li>
 *     <li>convert raw request params to {@link McpReadResourceParams}</li>
 *     <li>validate the requested resource URI</li>
 *     <li>delegate resource reading to {@link McpResourceService}</li>
 *     <li>return {@link McpReadResourceResult}</li>
 * </ul>
 *
 * <p>
 * Invalid parameters and unknown resource URIs are propagated as
 * {@link IllegalArgumentException} so that the upper runtime can
 * map them to JSON-RPC {@code -32602 Invalid params}.
 * </p>
 */
public final class McpReadResourceRequestHandler
        implements McpRequestHandler {

    private final McpJsonCodec codec;

    private final McpResourceService resourceService;


    /**
     * Creates a resources/read request handler.
     *
     * @param codec JSON codec
     * @param resourceService MCP resource service
     */
    public McpReadResourceRequestHandler(
            McpJsonCodec codec,
            McpResourceService resourceService
    ) {

        this.codec =
                Objects.requireNonNull(
                        codec,
                        "MCP JSON codec must not be null."
                );

        this.resourceService =
                Objects.requireNonNull(
                        resourceService,
                        "MCP resource service must not be null."
                );
    }


    /*
     * ------------------------------------------------------------
     * McpRequestHandler
     * ------------------------------------------------------------
     */

    @Override
    public String getMethod() {

        return McpMethod.RESOURCES_READ;
    }


    @Override
    public McpResult handle(
            McpRequestContext context
    ) {

        McpReadResourceParams params =
                codec.convertParams(
                        context.getRequest().getParams(),
                        McpReadResourceParams.class
                );

        McpResult result =
                resourceService.readResource(
                        params
                );

        if (result == null) {

            throw new IllegalStateException(
                    "MCP resource service returned null "
                            + "for resources/read."
            );
        }

        return result;
    }


    /*
     * ------------------------------------------------------------
     * Params conversion
     * ------------------------------------------------------------
     */

    private McpReadResourceParams convertParams(
            McpRequestContext context
    ) {

        try {

            return codec.convertParams(
                    context.getParams(),
                    McpReadResourceParams.class
            );

        } catch (McpJsonCodecException exception) {

            throw new IllegalArgumentException(
                    "Invalid MCP resources/read params: "
                            + safeMessage(
                                    exception
                            ),
                    exception
            );
        }
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

        if (!McpMethod.RESOURCES_READ.equals(
                method
        )) {

            throw new IllegalArgumentException(
                    "Invalid MCP method for resources/read handler: "
                            + method
            );
        }
    }


    private static void validateParams(
            McpReadResourceParams params
    ) {

        if (params == null) {

            throw new IllegalArgumentException(
                    "MCP resources/read params must not be null."
            );
        }

        String uri =
                params.getUri();

        if (uri == null
                || uri.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP resources/read requires a resource URI."
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public McpJsonCodec getCodec() {
        return codec;
    }


    public McpResourceService getResourceService() {
        return resourceService;
    }


    /*
     * ------------------------------------------------------------
     * Utility
     * ------------------------------------------------------------
     */

    private static String safeMessage(
            Throwable throwable
    ) {

        if (throwable == null) {
            return "Unknown resource read error.";
        }

        String message =
                throwable.getMessage();

        if (message == null
                || message.trim().isEmpty()) {

            return throwable
                    .getClass()
                    .getSimpleName();
        }

        return message.trim();
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "McpReadResourceRequestHandler{"
                + "method='"
                + McpMethod.RESOURCES_READ
                + '\''
                + '}';
    }
}