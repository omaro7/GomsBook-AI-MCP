/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for the MCP {@code tools/call} request.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * A tool call requires a tool name and may contain an
 * arguments object.
 * </p>
 */
public final class McpCallToolParams {

    private final String name;

    private final Map<String, Object> arguments;


    /*
     * ------------------------------------------------------------
     * Constructor
     * ------------------------------------------------------------
     */

    private McpCallToolParams(
            Builder builder
    ) {

        this.name =
                requireName(
                        builder.name
                );

        this.arguments =
                immutableArguments(
                        builder.arguments
                );
    }


    /*
     * ------------------------------------------------------------
     * Factory
     * ------------------------------------------------------------
     */

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Creates a tool call without arguments.
     *
     * @param name tool name
     * @return tool call params
     */
    public static McpCallToolParams of(
            String name
    ) {

        return builder()
                .name(
                        name
                )
                .build();
    }


    /**
     * Creates a tool call with arguments.
     *
     * @param name tool name
     * @param arguments tool arguments
     * @return tool call params
     */
    public static McpCallToolParams of(
            String name,
            Map<String, Object> arguments
    ) {

        return builder()
                .name(
                        name
                )
                .arguments(
                        arguments
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public String getName() {
        return name;
    }


    public Map<String, Object> getArguments() {
        return arguments;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasArguments() {
        return !arguments.isEmpty();
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private String name;

        private final Map<String, Object> arguments =
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


        /**
         * Adds a single tool argument.
         *
         * @param name argument name
         * @param value argument value
         * @return builder
         */
        public Builder argument(
                String name,
                Object value
        ) {

            String normalizedName =
                    requireArgumentName(
                            name
                    );

            if (value == null) {

                arguments.remove(
                        normalizedName
                );

            } else {

                arguments.put(
                        normalizedName,
                        value
                );
            }

            return this;
        }


        /**
         * Replaces all tool arguments.
         *
         * @param arguments arguments
         * @return builder
         */
        public Builder arguments(
                Map<String, Object> arguments
        ) {

            this.arguments.clear();

            if (arguments == null
                    || arguments.isEmpty()) {

                return this;
            }

            for (Map.Entry<String, Object> entry
                    : arguments.entrySet()) {

                argument(
                        entry.getKey(),
                        entry.getValue()
                );
            }

            return this;
        }


        public McpCallToolParams build() {

            return new McpCallToolParams(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static String requireName(
            String name
    ) {

        Objects.requireNonNull(
                name,
                "MCP tool name must not be null."
        );

        String normalized =
                name.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP tool name must not be blank."
            );
        }

        return normalized;
    }


    private static String requireArgumentName(
            String name
    ) {

        Objects.requireNonNull(
                name,
                "MCP tool argument name must not be null."
        );

        String normalized =
                name.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP tool argument name must not be blank."
            );
        }

        return normalized;
    }


    private static Map<String, Object> immutableArguments(
            Map<String, Object> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyMap();
        }

        Map<String, Object> copy =
                new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry
                : source.entrySet()) {

            String name =
                    requireArgumentName(
                            entry.getKey()
                    );

            if (entry.getValue() != null) {

                copy.put(
                        name,
                        entry.getValue()
                );
            }
        }

        if (copy.isEmpty()) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                copy
        );
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpCallToolParams)) {
            return false;
        }

        McpCallToolParams other =
                (McpCallToolParams) object;

        return Objects.equals(
                name,
                other.name
        )
                && Objects.equals(
                        arguments,
                        other.arguments
                );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                name,
                arguments
        );
    }


    @Override
    public String toString() {

        return "McpCallToolParams{"
                + "name='"
                + name
                + '\''
                + ", arguments="
                + arguments
                + '}';
    }
}