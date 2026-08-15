/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

/**
 * Exception thrown when no MCP completion provider
 * can handle a completion request.
 */
public class McpCompletionNotFoundException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a completion-not-found exception.
     *
     * @param message exception message
     */
    public McpCompletionNotFoundException(
            String message) {

        super(message);
    }

    /**
     * Creates a completion-not-found exception.
     *
     * @param message exception message
     * @param cause   underlying cause
     */
    public McpCompletionNotFoundException(
            String message,
            Throwable cause) {

        super(
                message,
                cause
        );
    }

    /**
     * Creates an exception for completion parameters
     * for which no provider could be resolved.
     *
     * @param params completion request parameters
     * @return exception
     */
    public static McpCompletionNotFoundException forParams(
            McpCompleteParams params) {

        if (params == null) {

            return new McpCompletionNotFoundException(
                    "No MCP completion provider found."
            );
        }

        McpCompletionReference ref =
                params.getRef();

        McpCompletionArgument argument =
                params.getArgument();

        String refDescription =
                describeReference(
                        ref
                );

        String argumentName =
                argument != null
                        ? argument.getName()
                        : null;

        return new McpCompletionNotFoundException(
                "No MCP completion provider found for " +
                        refDescription +
                        ", argument='" +
                        argumentName +
                        "'."
        );
    }

    private static String describeReference(
            McpCompletionReference ref) {

        if (ref == null) {
            return "reference=null";
        }

        if (ref instanceof McpPromptReference) {

            McpPromptReference promptRef =
                    (McpPromptReference) ref;

            return "prompt='" +
                    promptRef.getName() +
                    "'";
        }

        if (ref instanceof McpResourceTemplateReference) {

            McpResourceTemplateReference resourceRef =
                    (McpResourceTemplateReference) ref;

            return "resource='" +
                    resourceRef.getUri() +
                    "'";
        }

        return "referenceType='" +
                ref.getType() +
                "'";
    }
}