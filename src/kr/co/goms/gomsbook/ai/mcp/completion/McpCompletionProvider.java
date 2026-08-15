/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

/**
 * Provider for MCP completion requests.
 *
 * <p>
 * Implementations generate completion candidates for
 * prompt arguments or resource-template arguments.
 * </p>
 */
public interface McpCompletionProvider {

    /**
     * Returns whether this provider supports the given request.
     *
     * @param params completion request parameters
     * @return {@code true} if this provider can handle the request
     */
    boolean supports(
            McpCompleteParams params);

    /**
     * Generates completion candidates.
     *
     * @param params completion request parameters
     * @return completion payload
     */
    McpCompletion complete(
            McpCompleteParams params);
}