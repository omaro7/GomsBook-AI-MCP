/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MCP completion context.
 *
 * <p>
 * Provides additional argument values that may be used
 * by a completion provider to generate more relevant
 * completion candidates.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * {
 *   "arguments": {
 *     "framework": "django",
 *     "version": "5"
 *   }
 * }
 * </pre>
 */
public final class McpCompletionContext {

    private final Map<String, String> arguments;

    /**
     * Creates an empty completion context.
     */
    public McpCompletionContext() {

        this(Collections.emptyMap());
    }

    /**
     * Creates a completion context.
     *
     * @param arguments additional argument values
     */
    public McpCompletionContext(
            Map<String, String> arguments) {

        Objects.requireNonNull(
                arguments,
                "arguments must not be null."
        );

        Map<String, String> normalized =
                new LinkedHashMap<>();

        for (Map.Entry<String, String> entry
                : arguments.entrySet()) {

            String name =
                    requireText(
                            entry.getKey(),
                            "argument name"
                    );

            String value =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "argument value must not be null."
                    );

            normalized.put(
                    name,
                    value
            );
        }

        this.arguments =
                Collections.unmodifiableMap(
                        normalized
                );
    }

    /**
     * Returns additional argument values.
     *
     * @return immutable argument map
     */
    public Map<String, String> getArguments() {

        return arguments;
    }

    /**
     * Returns whether this context contains no arguments.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {

        return arguments.isEmpty();
    }

    /**
     * Returns an argument value by name.
     *
     * @param name argument name
     * @return argument value, or {@code null}
     */
    public String getArgument(
            String name) {

        if (name == null) {
            return null;
        }

        return arguments.get(
                name
        );
    }

    private static String requireText(
            String value,
            String fieldName) {

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

        return "McpCompletionContext{" +
                "arguments=" + arguments +
                '}';
    }
}