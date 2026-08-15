/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

/**
 * Marker interface for MCP completion references.
 *
 * <p>
 * A completion request can target either:
 * </p>
 *
 * <ul>
 *     <li>a prompt ({@code ref/prompt})</li>
 *     <li>a resource or resource template ({@code ref/resource})</li>
 * </ul>
 *
 * <p>
 * Concrete implementations:
 * </p>
 *
 * <ul>
 *     <li>{@link McpPromptReference}</li>
 *     <li>{@link McpResourceTemplateReference}</li>
 * </ul>
 */
public interface McpCompletionReference {

    /**
     * MCP reference type.
     *
     * <p>
     * Expected values:
     * </p>
     *
     * <ul>
     *     <li>{@code ref/prompt}</li>
     *     <li>{@code ref/resource}</li>
     * </ul>
     *
     * @return reference type
     */
    String getType();
}