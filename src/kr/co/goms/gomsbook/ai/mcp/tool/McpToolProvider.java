/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.Map;

/**
 * Provides an MCP tool descriptor and executes the tool.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * {@link McpTool} represents the protocol descriptor only.
 * Actual execution is delegated to this provider.
 * </p>
 */
public interface McpToolProvider {

    /**
     * Returns the MCP tool descriptor exposed through tools/list.
     *
     * @return tool descriptor
     */
    McpTool getTool();


    /**
     * Executes the MCP tool.
     *
     * @param arguments tool invocation arguments
     * @return tool result
     */
    McpToolResult call(
            Map<String, Object> arguments
    );


    /**
     * Returns the tool name.
     *
     * @return tool name
     */
    default String getName() {

        McpTool tool =
                getTool();

        if (tool == null) {
            return null;
        }

        return tool.getName();
    }
}