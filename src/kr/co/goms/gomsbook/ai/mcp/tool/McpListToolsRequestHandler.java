/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodec;
import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodecException;
import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestContext;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpMethod;

/**
 * Handles the MCP {@code tools/list} request.
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
 *     <li>convert raw request params to {@link McpListToolsParams}</li>
 *     <li>delegate tool listing to {@link McpToolService}</li>
 *     <li>return the resulting {@link McpListToolsResult}</li>
 * </ul>
 *
 * <p>
 * Protocol-version validation, client capability validation,
 * JSON-RPC response construction, and error-envelope creation
 * are handled by the upper runtime layer.
 * </p>
 */
public final class McpListToolsRequestHandler
        implements McpRequestHandler {

    private final McpJsonCodec codec;

    private final McpToolService toolService;


    /**
     * Creates a tools/list request handler.
     *
     * @param codec JSON codec
     * @param toolService MCP tool service
     */
    public McpListToolsRequestHandler(
            McpJsonCodec codec,
            McpToolService toolService
    ) {

        this.codec =
                Objects.requireNonNull(
                        codec,
                        "MCP JSON codec must not be null."
                );

        this.toolService =
                Objects.requireNonNull(
                        toolService,
                        "MCP tool service must not be null."
                );
    }


    /*
     * ------------------------------------------------------------
     * McpRequestHandler
     * ------------------------------------------------------------
     */

    @Override
    public String getMethod() {

        return McpMethod.TOOLS_LIST;
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

        McpListToolsParams params =
                convertParams(
                        context
                );

        McpListToolsResult result =
                toolService.listTools(
                        params
                );

        if (result == null) {

            throw new IllegalStateException(
                    "MCP tool service returned null "
                            + "for tools/list."
            );
        }

        return result;
    }


    /*
     * ------------------------------------------------------------
     * Params conversion
     * ------------------------------------------------------------
     */

    private McpListToolsParams convertParams(
            McpRequestContext context
    ) {

        try {

            return codec.convertParams(
                    context.getParams(),
                    McpListToolsParams.class
            );

        } catch (McpJsonCodecException exception) {

            /*
             * Method-specific parameter conversion failure
             * maps to JSON-RPC -32602 Invalid params through
             * DefaultMcpServerRuntime.
             */
            throw new IllegalArgumentException(
                    "Invalid MCP tools/list params: "
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

        if (!McpMethod.TOOLS_LIST.equals(
                method
        )) {

            throw new IllegalArgumentException(
                    "Invalid MCP method for tools/list handler: "
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


    public McpToolService getToolService() {
        return toolService;
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

        return "McpListToolsRequestHandler{"
                + "method='"
                + McpMethod.TOOLS_LIST
                + '\''
                + '}';
    }
}