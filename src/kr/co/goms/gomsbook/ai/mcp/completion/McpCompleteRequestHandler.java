/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodec;
import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodecException;
import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestContext;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpMethod;

/**
 * Handles the MCP {@code completion/complete} request.
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
 *     <li>convert raw request params to {@link McpCompleteParams}</li>
 *     <li>validate completion request parameters</li>
 *     <li>delegate completion processing to
 *         {@link McpCompletionService}</li>
 *     <li>return {@link McpCompleteResult}</li>
 * </ul>
 *
 * <p>
 * JSON-RPC response construction and protocol-level error
 * mapping are handled by the server runtime.
 * </p>
 */
public final class McpCompleteRequestHandler
        implements McpRequestHandler {

    private final McpJsonCodec codec;

    private final McpCompletionService completionService;


    /**
     * Creates a completion/complete request handler.
     *
     * @param codec JSON codec
     * @param completionService completion service
     */
    public McpCompleteRequestHandler(
            McpJsonCodec codec,
            McpCompletionService completionService
    ) {

        this.codec =
                Objects.requireNonNull(
                        codec,
                        "MCP JSON codec must not be null."
                );

        this.completionService =
                Objects.requireNonNull(
                        completionService,
                        "MCP completion service must not be null."
                );
    }


    /*
     * ------------------------------------------------------------
     * McpRequestHandler
     * ------------------------------------------------------------
     */

    @Override
    public String getMethod() {

        return McpMethod.COMPLETION_COMPLETE;
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

        McpCompleteParams params =
                convertParams(
                        context
                );

        validateParams(
                params
        );

        McpCompleteResult result =
                completionService.complete(
                        params
                );

        if (result == null) {

            throw new IllegalStateException(
                    "MCP completion service returned null "
                            + "for completion/complete."
            );
        }

        return result;
    }


    /*
     * ------------------------------------------------------------
     * Params conversion
     * ------------------------------------------------------------
     */

    private McpCompleteParams convertParams(
            McpRequestContext context
    ) {

        try {

            return codec.convertParams(
                    context.getParams(),
                    McpCompleteParams.class
            );

        } catch (McpJsonCodecException exception) {

            /*
             * Method-specific parameter conversion failure
             * is mapped by DefaultMcpServerRuntime to:
             *
             * -32602 Invalid params
             */
            throw new IllegalArgumentException(
                    "Invalid MCP completion/complete params: "
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

        if (!McpMethod.COMPLETION_COMPLETE.equals(
                method
        )) {

            throw new IllegalArgumentException(
                    "Invalid MCP method for "
                            + "completion/complete handler: "
                            + method
            );
        }
    }


    private static void validateParams(
            McpCompleteParams params
    ) {

        if (params == null) {

            throw new IllegalArgumentException(
                    "MCP completion/complete params "
                            + "must not be null."
            );
        }

        if (params.getRef() == null) {

            throw new IllegalArgumentException(
                    "MCP completion/complete requires ref."
            );
        }

        if (params.getArgument() == null) {

            throw new IllegalArgumentException(
                    "MCP completion/complete requires argument."
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


    public McpCompletionService getCompletionService() {
        return completionService;
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

            return "Unknown completion parameter error.";
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

        return "McpCompleteRequestHandler{"
                + "method='"
                + McpMethod.COMPLETION_COMPLETE
                + '\''
                + '}';
    }
}