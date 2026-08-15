/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.Objects;

/**
 * MCP completion reference for a resource or resource template.
 *
 * <p>
 * Represents a {@code ResourceTemplateReference} used by the
 * {@code completion/complete} request.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * {
 *   "type": "ref/resource",
 *   "uri": "file:///{path}"
 * }
 * </pre>
 */
public final class McpResourceTemplateReference
        implements McpCompletionReference {

    /**
     * MCP resource reference type.
     */
    public static final String TYPE = "ref/resource";

    private final String type;

    private final String uri;

    /**
     * Creates a resource template reference.
     *
     * @param uri resource template URI
     */
    public McpResourceTemplateReference(
            String uri) {

        this.type = TYPE;

        this.uri =
                requireText(
                        uri,
                        "uri"
                );
    }

    /**
     * Returns the MCP reference type.
     *
     * @return {@code ref/resource}
     */
    @Override
    public String getType() {

        return type;
    }

    /**
     * Returns the resource template URI.
     *
     * @return resource template URI
     */
    public String getUri() {

        return uri;
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

        return "McpResourceTemplateReference{" +
                "type='" + type + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}