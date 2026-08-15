/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

/**
 * Service for MCP prompt operations.
 *
 * <p>
 * Provides operations corresponding to the MCP
 * {@code prompts/list} and {@code prompts/get} methods.
 * </p>
 */
public interface McpPromptService {

    /**
     * Lists available MCP prompts.
     *
     * @param params list request parameters
     * @return available prompts
     */
    McpListPromptsResult listPrompts(
            McpListPromptsParams params
    );

    /**
     * Resolves a prompt by name and arguments.
     *
     * @param params get request parameters
     * @return resolved prompt result
     *
     * @throws McpPromptNotFoundException
     *         if the requested prompt does not exist
     */
    McpGetPromptResult getPrompt(
            McpGetPromptParams params
    );
}