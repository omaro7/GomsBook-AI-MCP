/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server.runtime;

import kr.co.goms.gomsbook.ai.mcp.core.McpRequest;
import kr.co.goms.gomsbook.ai.mcp.core.McpResponse;

/**
 * Runtime abstraction for a GomsBook MCP Server.
 *
 * <p>
 * Manages the lifecycle of a single MCP Server instance.
 * </p>
 *
 * <pre>
 * GomsBook AI
 *      ↓
 * McpServerRuntime
 *      ↓
 * McpServer
 *      ↓
 * McpTransport
 * </pre>
 */
public interface McpServerRuntime {

    /**
     * Processes an MCP request.
     *
     * <p>
     * JSON-RPC notifications may return {@code null} because
     * notifications do not receive responses.
     * </p>
     *
     * @param request MCP request
     * @return MCP response, or {@code null} for notifications
     */
    McpResponse handle(
            McpRequest request
    );
}