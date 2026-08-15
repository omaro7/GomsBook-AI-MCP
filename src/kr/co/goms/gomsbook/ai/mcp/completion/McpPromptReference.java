/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.Objects;

/**
 * MCP completion reference for a prompt.
 *
 * <p>
 * Represents a {@code PromptReference} used by the
 * {@code completion/complete} request.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * {
 *   "type": "ref/prompt",
 *   "name": "code_review"
 * }
 * </pre>
 */
public final class McpPromptReference
        implements McpCompletionReference {

    /**
     * MCP prompt reference type.
     */
    public static final String TYPE = "ref/prompt";

    private final String type;

    private final String name;

    private final String title;

    /**
     * Creates a prompt reference.
     *
     * @param name prompt name
     */
    public McpPromptReference(
            String name) {

        this(
                name,
                null
        );
    }

    /**
     * Creates a prompt reference.
     *
     * @param name  prompt name
     * @param title optional human-readable prompt title
     */
    public McpPromptReference(
            String name,
            String title) {

        this.type = TYPE;

        this.name =
                requireText(
                        name,
                        "name"
                );

        this.title =
                normalizeOptionalText(
                        title
                );
    }

    /**
     * Returns the MCP reference type.
     *
     * @return {@code ref/prompt}
     */
    @Override
    public String getType() {

        return type;
    }

    /**
     * Returns the prompt name.
     *
     * @return prompt name
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the optional human-readable title.
     *
     * @return prompt title, or {@code null}
     */
    public String getTitle() {

        return title;
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

    private static String normalizeOptionalText(
            String value) {

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

        return "McpPromptReference{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}