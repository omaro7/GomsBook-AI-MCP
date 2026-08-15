/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

import java.util.Map;

/**
 * Provides a single MCP prompt definition and resolves
 * prompt messages from request arguments.
 */
public interface McpPromptProvider {

    /**
     * Returns the prompt definition exposed through
     * {@code prompts/list}.
     *
     * @return prompt definition
     */
    McpPrompt getPrompt();

    /**
     * Resolves the prompt using the supplied arguments.
     *
     * @param arguments prompt arguments
     * @return resolved prompt result
     */
    McpGetPromptResult get(
            Map<String, String> arguments
    );

    /**
     * Returns the unique prompt name.
     *
     * @return prompt name
     */
    default String getName() {

        return getPrompt()
                .getName();
    }

    /**
     * Checks whether this provider matches the supplied
     * prompt name.
     *
     * @param name prompt name
     * @return {@code true} if this provider supports the name
     */
    default boolean supports(
            String name
    ) {

        if (name == null) {

            return false;
        }

        return getName()
                .equals(
                        name
                );
    }
}