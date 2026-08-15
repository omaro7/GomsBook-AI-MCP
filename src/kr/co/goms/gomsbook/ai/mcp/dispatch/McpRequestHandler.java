/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.dispatch;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;

/**
 * Handles a single MCP request method.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * A request handler is responsible for processing one MCP method
 * using the current request-scoped context and returning an
 * {@link McpResult}.
 * </p>
 *
 * <p>
 * Implementations must not depend on protocol-level session or
 * initialization state.
 * </p>
 */
public interface McpRequestHandler {

    /**
     * Returns the MCP method handled by this handler.
     *
     * <p>
     * Examples:
     * </p>
     *
     * <pre>
     * server/discover
     * tools/list
     * tools/call
     * resources/list
     * resources/read
     * prompts/list
     * prompts/get
     * completion/complete
     * </pre>
     *
     * @return MCP method name
     */
    String getMethod();

    /**
     * Handles the current MCP request.
     *
     * @param context request-scoped context
     * @return MCP result
     */
    McpResult handle(
            McpRequestContext context
    );
}