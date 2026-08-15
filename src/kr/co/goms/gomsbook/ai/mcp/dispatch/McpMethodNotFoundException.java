/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.dispatch;

import java.util.Objects;

/**
 * Thrown when no MCP request handler is registered for
 * a requested method.
 */
public final class McpMethodNotFoundException
        extends RuntimeException {

    private static final long serialVersionUID =
            1L;

    private final String method;


    public McpMethodNotFoundException(
            String method
    ) {

        super(
                "MCP method not found: "
                        + requireMethod(method)
        );

        this.method =
                method.trim();
    }


    public String getMethod() {
        return method;
    }


    private static String requireMethod(
            String method
    ) {

        Objects.requireNonNull(
                method,
                "MCP method must not be null."
        );

        String normalized =
                method.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP method must not be blank."
            );
        }

        return normalized;
    }
}