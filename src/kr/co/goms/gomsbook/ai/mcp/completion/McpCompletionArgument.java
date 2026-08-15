/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.Objects;

/**
 * MCP completion argument.
 *
 * <p>
 * Represents the argument currently being completed in a
 * {@code completion/complete} request.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * {
 *   "name": "language",
 *   "value": "py"
 * }
 * </pre>
 */
public final class McpCompletionArgument {

    private final String name;

    private final String value;

    /**
     * Creates a completion argument.
     *
     * @param name  argument name
     * @param value current argument value
     */
    public McpCompletionArgument(
            String name,
            String value) {

        this.name =
                requireText(
                        name,
                        "name"
                );

        this.value =
                Objects.requireNonNull(
                        value,
                        "value must not be null."
                );
    }

    /**
     * Returns the argument name.
     *
     * @return argument name
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the current argument value.
     *
     * <p>
     * The value may be an empty string when completion
     * is requested before the user has entered text.
     * </p>
     *
     * @return current argument value
     */
    public String getValue() {

        return value;
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

        return "McpCompletionArgument{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}