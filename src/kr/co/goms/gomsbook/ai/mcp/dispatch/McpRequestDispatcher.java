/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.dispatch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpRequest;
import kr.co.goms.gomsbook.ai.mcp.core.McpResult;

/**
 * Dispatches MCP requests to registered request handlers.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 */
public final class McpRequestDispatcher {

    private final Map<String, McpRequestHandler> handlers;


    public McpRequestDispatcher() {

        this.handlers =
                new LinkedHashMap<>();
    }


    /**
     * Registers a request handler.
     */
    public McpRequestDispatcher register(
            McpRequestHandler handler
    ) {

        Objects.requireNonNull(
                handler,
                "MCP request handler must not be null."
        );

        String method =
                normalizeMethod(
                        handler.getMethod()
                );

        McpRequestHandler previous =
                handlers.putIfAbsent(
                        method,
                        handler
                );

        if (previous != null) {

            throw new IllegalStateException(
                    "MCP request handler already registered: "
                            + method
            );
        }

        return this;
    }


    /**
     * Checks whether a handler is registered.
     */
    public boolean supports(
            String method
    ) {

        if (method == null
                || method.isBlank()) {

            return false;
        }

        return handlers.containsKey(
                method.trim()
        );
    }


    /**
     * Dispatches an MCP request.
     */
    public McpResult dispatch(
            McpRequestContext context
    ) {

        Objects.requireNonNull(
                context,
                "MCP request context must not be null."
        );

        McpRequest request =
                context.getRequest();

        Objects.requireNonNull(
                request,
                "MCP request must not be null."
        );

        String method =
                normalizeMethod(
                        request.getMethod()
                );

        McpRequestHandler handler =
                handlers.get(
                        method
                );

        if (handler == null) {

            throw new McpMethodNotFoundException(
                    method
            );
        }

        McpResult result =
                handler.handle(
                        context
                );

        if (result == null) {

            throw new IllegalStateException(
                    "MCP request handler returned null result: "
                            + method
            );
        }

        return result;
    }


    private static String normalizeMethod(
            String method
    ) {

        Objects.requireNonNull(
                method,
                "MCP request method must not be null."
        );

        String normalized =
                method.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP request method must not be blank."
            );
        }

        return normalized;
    }
}