/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for the MCP {@code prompts/get} request.
 *
 * <p>
 * A prompt is identified by its name and may receive
 * optional string arguments used to resolve the prompt.
 * </p>
 */
public final class McpGetPromptParams {

    private final String name;

    private final Map<String, String> arguments;

    private McpGetPromptParams(
            Builder builder
    ) {

        this.name =
                requireText(
                        builder.name,
                        "name"
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

    public Map<String, String> getArguments() {

        return arguments;
    }

    public boolean hasArguments() {

        return !arguments.isEmpty();
    }

    public String getArgument(
            String name
    ) {

        if (name == null) {

            return null;
        }

        return arguments.get(
                name
        );
    }

    private static Map<String, String> immutableArguments(
            Map<String, String> arguments
    ) {

        if (arguments == null
                || arguments.isEmpty()) {

            return Collections.emptyMap();
        }

        Map<String, String> copy =
                new LinkedHashMap<>();

        for (Map.Entry<String, String> entry
                : arguments.entrySet()) {

            String key =
                    requireText(
                            entry.getKey(),
                            "argument name"
                    );

            String value =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "Prompt argument value must not be null."
                    );

            copy.put(
                    key,
                    value
            );
        }

        return Collections.unmodifiableMap(
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

    @Override
    public String toString() {

        return "McpGetPromptParams{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                '}';
    }

    public static final class Builder {

        private String name;

        private final Map<String, String> arguments =
                new LinkedHashMap<>();

        private Builder() {
        }

        public Builder name(
                String name
        ) {

            this.name =
                    name;

            return this;
        }

        public Builder argument(
                String name,
                String value
        ) {

            this.arguments.put(
                    requireText(
                            name,
                            "argument name"
                    ),
                    Objects.requireNonNull(
                            value,
                            "Prompt argument value must not be null."
                    )
            );

            return this;
        }

        public Builder arguments(
                Map<String, String> arguments
        ) {

            this.arguments.clear();

            if (arguments != null) {

                for (Map.Entry<String, String> entry
                        : arguments.entrySet()) {

                    argument(
                            entry.getKey(),
                            entry.getValue()
                    );
                }
            }

            return this;
        }

        public McpGetPromptParams build() {

            return new McpGetPromptParams(
                    this
            );
        }
    }
}