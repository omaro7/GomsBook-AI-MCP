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
 * Handles the MCP {@code resources/list} request.
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
 *     <li>convert raw request params to {@link McpListResourcesParams}</li>
 *     <li>delegate resource listing to {@link McpResourceService}</li>
 *     <li>return {@link McpListResourcesResult}</li>
 * </ul>
 *
 * <p>
 * JSON-RPC envelope creation and protocol-level error mapping are
 * handled by the server runtime.
 * </p>
 */
public final class McpListResourcesRequestHandler
        implements McpRequestHandler {

    private final McpJsonCodec codec;

    private final McpResourceService resourceService;


    /**
     * Creates a resources/list request handler.
     *
     * @param codec JSON codec
     * @param resourceService MCP resource service
     */
    public McpListResourcesRequestHandler(
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

        return McpMethod.RESOURCES_LIST;
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

        McpListResourcesParams params =
                convertParams(
                        context
                );

        McpListResourcesResult result =
                resourceService.listResources(
                        params
                );

        if (result == null) {

            throw new IllegalStateException(
                    "MCP resource service returned null "
                            + "for resources/list."
            );
        }

        return result;
    }


    /*
     * ------------------------------------------------------------
     * Params conversion
     * ------------------------------------------------------------
     */

    private McpListResourcesParams convertParams(
            McpRequestContext context
    ) {

        try {

            return codec.convertParams(
                    context.getParams(),
                    McpListResourcesParams.class
            );

        } catch (McpJsonCodecException exception) {

            throw new IllegalArgumentException(
                    "Invalid MCP resources/list params: "
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

        if (!McpMethod.RESOURCES_LIST.equals(
                method
        )) {

            throw new IllegalArgumentException(
                    "Invalid MCP method for resources/list handler: "
                            + method
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

            return "Unknown parameter conversion error.";
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

        return "McpListResourcesRequestHandler{"
                + "method='"
                + McpMethod.RESOURCES_LIST
                + '\''
                + '}';
    }
}