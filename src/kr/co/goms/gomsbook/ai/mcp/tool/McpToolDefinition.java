/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCP(Model Context Protocol) tool definition.
 *
 * <p>
 * Represents metadata exposed through the MCP
 * {@code tools/list} method.
 * </p>
 *
 * <p>
 * A tool definition contains:
 * </p>
 *
 * <ul>
 *     <li>name - unique tool name</li>
 *     <li>title - optional human-readable title</li>
 *     <li>description - optional tool description</li>
 *     <li>inputSchema - JSON Schema describing arguments</li>
 *     <li>outputSchema - optional JSON Schema describing output</li>
 * </ul>
 */
public final class McpToolDefinition {

    private String name;

    private String title;

    private String description;

    private Map<String, Object> inputSchema;

    private Map<String, Object> outputSchema;


    /**
     * Constructor for Gson deserialization.
     */
    public McpToolDefinition() {

        this.inputSchema =
                createDefaultInputSchema();
    }


    private McpToolDefinition(
            Builder builder) {

        this.name =
                normalizeRequired(
                        builder.name,
                        "MCP tool name"
                );

        this.title =
                normalizeOptional(
                        builder.title
                );

        this.description =
                normalizeOptional(
                        builder.description
                );

        this.inputSchema =
                builder.inputSchema == null
                        ? createDefaultInputSchema()
                        : new LinkedHashMap<>(
                                builder.inputSchema
                        );

        this.outputSchema =
                builder.outputSchema == null
                        ? null
                        : new LinkedHashMap<>(
                                builder.outputSchema
                        );

        validate();
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


    public Map<String, Object> getInputSchema() {

        if (inputSchema == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                inputSchema
        );
    }


    public Map<String, Object> getOutputSchema() {

        if (outputSchema == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                outputSchema
        );
    }


    public boolean hasTitle() {

        return title != null
                && !title.isBlank();
    }


    public boolean hasDescription() {

        return description != null
                && !description.isBlank();
    }


    public boolean hasOutputSchema() {

        return outputSchema != null
                && !outputSchema.isEmpty();
    }


    /**
     * Validates this MCP tool definition.
     */
    public void validate() {

        if (name == null
                || name.isBlank()) {

            throw new IllegalArgumentException(
                    "MCP tool name must not be blank."
            );
        }

        if (inputSchema == null) {

            throw new IllegalArgumentException(
                    "MCP tool inputSchema must not be null."
            );
        }

        Object schemaType =
                inputSchema.get(
                        "type"
                );

        if (schemaType == null
                || !"object".equals(
                        String.valueOf(
                                schemaType
                        )
                )) {

            throw new IllegalArgumentException(
                    "MCP tool inputSchema type "
                            + "must be 'object'."
            );
        }
    }


    /**
     * Creates the minimal valid input schema.
     *
     * @return default input schema
     */
    private static Map<String, Object>
            createDefaultInputSchema() {

        Map<String, Object> schema =
                new LinkedHashMap<>();

        schema.put(
                "type",
                "object"
        );

        schema.put(
                "properties",
                new LinkedHashMap<String, Object>()
        );

        return schema;
    }


    private static String normalizeRequired(
            String value,
            String fieldName) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must not be blank."
            );
        }

        return value.trim();
    }


    private static String normalizeOptional(
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

        return "McpToolDefinition{"
                + "name='"
                + name
                + '\''
                + ", title='"
                + title
                + '\''
                + ", description='"
                + description
                + '\''
                + ", inputSchema="
                + inputSchema
                + ", outputSchema="
                + outputSchema
                + '}';
    }


    /**
     * Builder for {@link McpToolDefinition}.
     */
    public static final class Builder {

        private String name;

        private String title;

        private String description;

        private Map<String, Object> inputSchema;

        private Map<String, Object> outputSchema;


        private Builder() {
        }


        public Builder name(
                String name) {

            this.name =
                    name;

            return this;
        }


        public Builder title(
                String title) {

            this.title =
                    title;

            return this;
        }


        public Builder description(
                String description) {

            this.description =
                    description;

            return this;
        }


        public Builder inputSchema(
                Map<String, Object> inputSchema) {

            this.inputSchema =
                    inputSchema == null
                            ? null
                            : new LinkedHashMap<>(
                                    inputSchema
                            );

            return this;
        }


        public Builder outputSchema(
                Map<String, Object> outputSchema) {

            this.outputSchema =
                    outputSchema == null
                            ? null
                            : new LinkedHashMap<>(
                                    outputSchema
                            );

            return this;
        }


        public McpToolDefinition build() {

            return new McpToolDefinition(
                    this
            );
        }
    }
}