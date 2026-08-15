/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

/**
 * Service for MCP completion requests.
 *
 * <p>
 * Resolves an appropriate {@link McpCompletionProvider},
 * executes completion, and returns the MCP completion result.
 * </p>
 */
public interface McpCompletionService {

    /**
     * Executes an MCP completion request.
     *
     * @param params completion request parameters
     * @return completion result
     *
     * @throws McpCompletionNotFoundException
     *         if no completion provider can handle the request
     */
    McpCompleteResult complete(
            McpCompleteParams params);
}