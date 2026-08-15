/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;

/**
 * MCP tool service contract.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * The service provides the server-side implementation for:
 * </p>
 *
 * <ul>
 *     <li>{@code tools/list}</li>
 *     <li>{@code tools/call}</li>
 * </ul>
 */
public interface McpToolService {

    /**
     * Returns the tools exposed by the MCP server.
     *
     * @param params tools/list parameters
     * @return tool list result
     */
    McpListToolsResult listTools(
            McpListToolsParams params
    );

    /**
     * Executes an MCP tool.
     *
     * <p>
     * MCP 2026-07-28 allows tools/call to return either a normal
     * {@link McpToolResult} or an input-required result such as
     * {@code McpInputRequiredResult}.
     * </p>
     *
     * @param params tools/call parameters
     * @return MCP result
     */
    McpResult callTool(
            McpCallToolParams params
    );
}