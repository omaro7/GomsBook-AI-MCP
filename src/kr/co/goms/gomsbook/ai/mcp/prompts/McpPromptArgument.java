/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

import java.util.Objects;

/**
 * Represents a single argument definition for an MCP prompt.
 */
public final class McpPromptArgument {

    private final String name;

    private final String title;

    private final String description;

    private final boolean required;

    private McpPromptArgument(
            Builder builder
    ) {

        this.name =
                requireText(
                        builder.name,
                        "name"
                );

        this.title =
                normalize(
                        builder.title
                );

        this.description =
                normalize(
                        builder.description
                );

        this.required =
                builder.required;
    }

    public static Builder builder() {

        return new Builder();
    }

    public String getName() {

        return name;
    }

    public String getTitle() {

        return title;
    }

    public String getDescription() {

        return description;
    }

    public boolean isRequired() {

        return required;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {

        Objects.requireNonNull(
                value,
                fieldName + " must not be null."
        );

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return normalized;
    }

    private static String normalize(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    @Override
    public String toString() {

        return "McpPromptArgument{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", required=" + required +
                '}';
    }

    public static final class Builder {

        private String name;

        private String title;

        private String description;

        private boolean required;

        private Builder() {
        }

        public Builder name(
                String name
        ) {

            this.name =
                    name;

            return this;
        }

        public Builder title(
                String title
        ) {

            this.title =
                    title;

            return this;
        }

        public Builder description(
                String description
        ) {

            this.description =
                    description;

            return this;
        }

        public Builder required(
                boolean required
        ) {

            this.required =
                    required;

            return this;
        }

        public McpPromptArgument build() {

            return new McpPromptArgument(
                    this
            );
        }
    }
}