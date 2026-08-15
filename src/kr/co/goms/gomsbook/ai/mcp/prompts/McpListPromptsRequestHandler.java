/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodec;
import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodecException;
import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestContext;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpMethod;

/**
 * Handles the MCP {@code prompts/list} request.
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
 *     <li>convert raw request params to {@link McpListPromptsParams}</li>
 *     <li>delegate prompt listing to {@link McpPromptService}</li>
 *     <li>return {@link McpListPromptsResult}</li>
 * </ul>
 *
 * <p>
 * JSON-RPC response construction and protocol-level error
 * mapping are handled by the server runtime.
 * </p>
 */
public final class McpListPromptsRequestHandler
        implements McpRequestHandler {

    private final McpJsonCodec codec;

    private final McpPromptService promptService;


    /**
     * Creates a prompts/list request handler.
     *
     * @param codec JSON codec
     * @param promptService MCP prompt service
     */
    public McpListPromptsRequestHandler(
            McpJsonCodec codec,
            McpPromptService promptService
    ) {

        this.codec =
                Objects.requireNonNull(
                        codec,
                        "MCP JSON codec must not be null."
                );

        this.promptService =
                Objects.requireNonNull(
                        promptService,
                        "MCP prompt service must not be null."
                );
    }


    /*
     * ------------------------------------------------------------
     * McpRequestHandler
     * ------------------------------------------------------------
     */

    @Override
    public String getMethod() {

        return McpMethod.PROMPTS_LIST;
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

        McpListPromptsParams params =
                convertParams(
                        context
                );

        McpListPromptsResult result =
                promptService.listPrompts(
                        params
                );

        if (result == null) {

            throw new IllegalStateException(
                    "MCP prompt service returned null "
                            + "for prompts/list."
            );
        }

        return result;
    }


    /*
     * ------------------------------------------------------------
     * Params conversion
     * ------------------------------------------------------------
     */

    private McpListPromptsParams convertParams(
            McpRequestContext context
    ) {

        try {

            return codec.convertParams(
                    context.getParams(),
                    McpListPromptsParams.class
            );

        } catch (McpJsonCodecException exception) {

            /*
             * Method-specific parameter conversion failure
             * is mapped by DefaultMcpServerRuntime to:
             *
             * -32602 Invalid params
             */
            throw new IllegalArgumentException(
                    "Invalid MCP prompts/list params: "
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

        if (!McpMethod.PROMPTS_LIST.equals(
                method
        )) {

            throw new IllegalArgumentException(
                    "Invalid MCP method for prompts/list handler: "
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


    public McpPromptService getPromptService() {
        return promptService;
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

        return "McpListPromptsRequestHandler{"
                + "method='"
                + McpMethod.PROMPTS_LIST
                + '\''
                + '}';
    }
}