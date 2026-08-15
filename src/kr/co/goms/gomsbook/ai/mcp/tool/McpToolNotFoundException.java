/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.Objects;

/**
 * Thrown when an MCP tools/call request references an unknown
 * tool.
 */
public final class McpToolNotFoundException
        extends IllegalArgumentException {

    private static final long serialVersionUID =
            1L;

    private final String toolName;


    public McpToolNotFoundException(
            String toolName
    ) {

        super(
                "MCP tool not found: "
                        + requireToolName(
                                toolName
                        )
        );

        this.toolName =
                toolName.trim();
    }


    public String getToolName() {
        return toolName;
    }


    private static String requireToolName(
            String toolName
    ) {

        Objects.requireNonNull(
                toolName,
                "MCP tool name must not be null."
        );

        String normalized =
                toolName.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP tool name must not be blank."
            );
        }

        return normalized;
    }
}