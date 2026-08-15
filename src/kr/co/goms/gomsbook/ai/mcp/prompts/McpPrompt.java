/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an MCP prompt definition.
 *
 * <p>
 * A prompt defines a reusable prompt template that can be
 * discovered through {@code prompts/list} and resolved through
 * {@code prompts/get}.
 * </p>
 */
public final class McpPrompt {

    private final String name;

    private final String title;

    private final String description;

    private final List<McpPromptArgument> arguments;

    private McpPrompt(
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

        this.arguments =
                immutableArguments(
                        builder.arguments
                );
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

    public List<McpPromptArgument> getArguments() {

        return arguments;
    }

    public boolean hasArguments() {

        return !arguments.isEmpty();
    }

    private static List<McpPromptArgument> immutableArguments(
            List<McpPromptArgument> arguments
    ) {

        if (arguments == null
                || arguments.isEmpty()) {

            return Collections.emptyList();
        }

        List<McpPromptArgument> copy =
                new ArrayList<>(
                        arguments.size()
                );

        for (McpPromptArgument argument : arguments) {

            copy.add(
                    Objects.requireNonNull(
                            argument,
                            "Prompt argument must not be null."
                    )
            );
        }

        return Collections.unmodifiableList(
                copy
        );
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

        return "McpPrompt{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", arguments=" + arguments +
                '}';
    }

    public static final class Builder {

        private String name;

        private String title;

        private String description;

        private final List<McpPromptArgument> arguments =
                new ArrayList<>();

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

        public Builder argument(
                McpPromptArgument argument
        ) {

            this.arguments.add(
                    Objects.requireNonNull(
                            argument,
                            "argument must not be null."
                    )
            );

            return this;
        }

        public Builder arguments(
                List<McpPromptArgument> arguments
        ) {

            this.arguments.clear();

            if (arguments != null) {

                for (McpPromptArgument argument : arguments) {

                    this.arguments.add(
                            Objects.requireNonNull(
                                    argument,
                                    "argument must not be null."
                            )
                    );
                }
            }

            return this;
        }

        public McpPrompt build() {

            return new McpPrompt(
                    this
            );
        }
    }
}