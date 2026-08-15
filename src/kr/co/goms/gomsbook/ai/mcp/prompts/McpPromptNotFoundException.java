/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

/**
 * Exception thrown when a requested MCP prompt
 * cannot be found.
 */
public final class McpPromptNotFoundException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String promptName;

    public McpPromptNotFoundException(
            String promptName
    ) {

        super(
                createMessage(
                        promptName
                )
        );

        this.promptName =
                normalize(
                        promptName
                );
    }

    public McpPromptNotFoundException(
            String promptName,
            Throwable cause
    ) {

        super(
                createMessage(
                        promptName
                ),
                cause
        );

        this.promptName =
                normalize(
                        promptName
                );
    }

    public String getPromptName() {

        return promptName;
    }

    private static String createMessage(
            String promptName
    ) {

        String normalized =
                normalize(
                        promptName
                );

        if (normalized == null) {

            return "MCP prompt was not found.";
        }

        return "MCP prompt was not found: "
                + normalized;
    }

    private static String normalize(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}