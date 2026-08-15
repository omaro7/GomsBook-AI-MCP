/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.Collections;
import java.util.Map;

/**
 * MCP(Model Context Protocol) tool abstraction.
 *
 * <p>
 * Represents an executable tool exposed through
 * the MCP {@code tools/list} and {@code tools/call}
 * methods.
 * </p>
 *
 * <p>
 * Implementations provide a tool definition and
 * execute tool calls using a map of arguments.
 * </p>
 */
public interface McpTool {

    /**
     * Returns the MCP tool definition.
     *
     * @return tool definition
     */
    McpToolDefinition getDefinition();


    /**
     * Executes this MCP tool.
     *
     * @param arguments tool arguments
     * @return tool execution result
     */
    McpToolResult execute(
            Map<String, Object> arguments
    );


    /**
     * Returns the unique tool name.
     *
     * <p>
     * By default the name is obtained from
     * {@link #getDefinition()}.
     * </p>
     *
     * @return tool name
     */
    default String getName() {

        McpToolDefinition definition =
                getDefinition();

        if (definition == null) {

            throw new IllegalStateException(
                    "MCP tool definition must not be null."
            );
        }

        return definition.getName();
    }


    /**
     * Checks whether this tool has the given name.
     *
     * @param name tool name
     * @return {@code true} when the name matches
     */
    default boolean matches(
            String name) {

        if (name == null
                || name.isBlank()) {

            return false;
        }

        return getName().equals(
                name.trim()
        );
    }


    /**
     * Executes this tool without arguments.
     *
     * @return tool execution result
     */
    default McpToolResult execute() {

        return execute(
                Collections.emptyMap()
        );
    }


    /**
     * Validates the tool definition.
     *
     * <p>
     * Tool-specific argument validation should normally
     * be performed by the underlying tool implementation.
     * </p>
     */
    default void validate() {

        McpToolDefinition definition =
                getDefinition();

        if (definition == null) {

            throw new IllegalStateException(
                    "MCP tool definition must not be null."
            );
        }

        definition.validate();
    }
}