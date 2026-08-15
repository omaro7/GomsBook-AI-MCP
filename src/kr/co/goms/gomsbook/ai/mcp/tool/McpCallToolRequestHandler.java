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
 * Handles the MCP {@code tools/call} request.
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
 *     <li>convert raw request params to {@link McpCallToolParams}</li>
 *     <li>validate required tool call parameters</li>
 *     <li>delegate execution to {@link McpToolService}</li>
 *     <li>return {@link McpToolResult}</li>
 * </ul>
 *
 * <p>
 * JSON-RPC envelope creation and protocol-level error mapping
 * are handled by the server runtime.
 * </p>
 */
public final class McpCallToolRequestHandler
        implements McpRequestHandler {

    private final McpJsonCodec codec;

    private final McpToolService toolService;


    /**
     * Creates a tools/call request handler.
     *
     * @param codec JSON codec
     * @param toolService MCP tool service
     */
    public McpCallToolRequestHandler(
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

        return McpMethod.TOOLS_CALL;
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

        McpCallToolParams params =
                convertParams(
                        context
                );

        validateParams(
                params
        );

        McpResult result =
                toolService.callTool(
                        params
                );

        if (result == null) {

            throw new IllegalStateException(
                    "MCP tool service returned null "
                            + "for tools/call."
            );
        }

        return result;
    }


    /*
     * ------------------------------------------------------------
     * Params conversion
     * ------------------------------------------------------------
     */

    private McpCallToolParams convertParams(
            McpRequestContext context
    ) {

        try {

            return codec.convertParams(
                    context.getParams(),
                    McpCallToolParams.class
            );

        } catch (McpJsonCodecException exception) {

            /*
             * Method-specific parameter conversion failures are
             * converted by DefaultMcpServerRuntime to:
             *
             * -32602 Invalid params
             */
            throw new IllegalArgumentException(
                    "Invalid MCP tools/call params: "
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

        if (!McpMethod.TOOLS_CALL.equals(
                method
        )) {

            throw new IllegalArgumentException(
                    "Invalid MCP method for tools/call handler: "
                            + method
            );
        }
    }


    private static void validateParams(
            McpCallToolParams params
    ) {

        if (params == null) {

            throw new IllegalArgumentException(
                    "MCP tools/call params must not be null."
            );
        }

        String name =
                params.getName();

        if (name == null
                || name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP tools/call requires a tool name."
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
            return "Unknown tool parameter error.";
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

        return "McpCallToolRequestHandler{"
                + "method='"
                + McpMethod.TOOLS_CALL
                + '\''
                + '}';
    }
}