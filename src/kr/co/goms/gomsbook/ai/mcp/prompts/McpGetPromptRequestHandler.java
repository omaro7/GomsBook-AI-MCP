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
 * Handles the MCP {@code prompts/get} request.
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
 *     <li>convert raw request params to {@link McpGetPromptParams}</li>
 *     <li>validate the requested prompt name</li>
 *     <li>delegate prompt resolution to {@link McpPromptService}</li>
 *     <li>return {@link McpGetPromptResult}</li>
 * </ul>
 *
 * <p>
 * Invalid prompt names, missing required arguments, and unknown
 * prompts are propagated as {@link IllegalArgumentException} so
 * the server runtime can map them to JSON-RPC
 * {@code -32602 Invalid params}.
 * </p>
 */
public final class McpGetPromptRequestHandler
        implements McpRequestHandler {

    private final McpJsonCodec codec;

    private final McpPromptService promptService;


    /**
     * Creates a prompts/get request handler.
     *
     * @param codec JSON codec
     * @param promptService MCP prompt service
     */
    public McpGetPromptRequestHandler(
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

        return McpMethod.PROMPTS_GET;
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

        McpGetPromptParams params =
                convertParams(
                        context
                );

        validateParams(
                params
        );

        try {

            McpGetPromptResult result =
                    promptService.getPrompt(
                            params
                    );

            if (result == null) {

                throw new IllegalStateException(
                        "MCP prompt service returned null "
                                + "for prompts/get."
                );
            }

            return result;

        } catch (McpPromptNotFoundException exception) {

            throw new IllegalArgumentException(
                    safeMessage(
                            exception
                    ),
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Params conversion
     * ------------------------------------------------------------
     */

    private McpGetPromptParams convertParams(
            McpRequestContext context
    ) {

        try {

            return codec.convertParams(
                    context.getParams(),
                    McpGetPromptParams.class
            );

        } catch (McpJsonCodecException exception) {

            throw new IllegalArgumentException(
                    "Invalid MCP prompts/get params: "
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

        if (!McpMethod.PROMPTS_GET.equals(
                method
        )) {

            throw new IllegalArgumentException(
                    "Invalid MCP method for prompts/get handler: "
                            + method
            );
        }
    }


    private static void validateParams(
            McpGetPromptParams params
    ) {

        if (params == null) {

            throw new IllegalArgumentException(
                    "MCP prompts/get params must not be null."
            );
        }

        String name =
                params.getName();

        if (name == null
                || name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP prompts/get requires a prompt name."
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

            return "Unknown prompt resolution error.";
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

        return "McpGetPromptRequestHandler{"
                + "method='"
                + McpMethod.PROMPTS_GET
                + '\''
                + '}';
    }
}