/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.common.McpRole;
import kr.co.goms.gomsbook.ai.mcp.content.McpContent;

/**
 * Represents a single message returned by an MCP prompt.
 *
 * <p>
 * Each prompt message consists of a role and an MCP content block.
 * </p>
 */
public final class McpPromptMessage {

    private final McpRole role;

    private final McpContent content;

    private McpPromptMessage(
            Builder builder
    ) {

        this.role =
                Objects.requireNonNull(
                        builder.role,
                        "role must not be null."
                );

        this.content =
                Objects.requireNonNull(
                        builder.content,
                        "content must not be null."
                );
    }

    public static Builder builder() {

        return new Builder();
    }

    public McpRole getRole() {

        return role;
    }

    public McpContent getContent() {

        return content;
    }

    @Override
    public String toString() {

        return "McpPromptMessage{" +
                "role=" + role +
                ", content=" + content +
                '}';
    }

    public static final class Builder {

        private McpRole role;

        private McpContent content;

        private Builder() {
        }

        public Builder role(
                McpRole role
        ) {

            this.role =
                    role;

            return this;
        }

        public Builder content(
                McpContent content
        ) {

            this.content =
                    content;

            return this;
        }

        public McpPromptMessage build() {

            return new McpPromptMessage(
                    this
            );
        }
    }
}