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
 * Handles the MCP {@code resources/templates/list} request.
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
 *     <li>convert request params to
 *         {@link McpListResourceTemplatesParams}</li>
 *     <li>delegate resource-template listing to
 *         {@link McpResourceService}</li>
 *     <li>return {@link McpListResourceTemplatesResult}</li>
 * </ul>
 *
 * <p>
 * JSON-RPC response construction and protocol-level error
 * mapping are handled by the server runtime.
 * </p>
 */
public final class McpListResourceTemplatesRequestHandler
        implements McpRequestHandler {

    private final McpJsonCodec codec;

    private final McpResourceService resourceService;


    /**
     * Creates a resources/templates/list request handler.
     *
     * @param codec JSON codec
     * @param resourceService MCP resource service
     */
    public McpListResourceTemplatesRequestHandler(
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

        return McpMethod.RESOURCES_TEMPLATES_LIST;
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

        McpListResourceTemplatesParams params =
                convertParams(
                        context
                );

        McpListResourceTemplatesResult result =
                resourceService.listResourceTemplates(
                        params
                );

        if (result == null) {

            throw new IllegalStateException(
                    "MCP resource service returned null "
                            + "for resources/templates/list."
            );
        }

        return result;
    }


    /*
     * ------------------------------------------------------------
     * Params conversion
     * ------------------------------------------------------------
     */

    private McpListResourceTemplatesParams convertParams(
            McpRequestContext context
    ) {

        try {

            return codec.convertParams(
                    context.getParams(),
                    McpListResourceTemplatesParams.class
            );

        } catch (McpJsonCodecException exception) {

            throw new IllegalArgumentException(
                    "Invalid MCP resources/templates/list params: "
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

        if (!McpMethod.RESOURCES_TEMPLATES_LIST.equals(
                method
        )) {

            throw new IllegalArgumentException(
                    "Invalid MCP method for "
                            + "resources/templates/list handler: "
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

        return "McpListResourceTemplatesRequestHandler{"
                + "method='"
                + McpMethod.RESOURCES_TEMPLATES_LIST
                + '\''
                + '}';
    }
}