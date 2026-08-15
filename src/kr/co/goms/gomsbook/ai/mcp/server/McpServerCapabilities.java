/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Capabilities supported by an MCP server.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Standard server capabilities:
 * </p>
 *
 * <ul>
 *     <li>{@code completions}</li>
 *     <li>{@code prompts}</li>
 *     <li>{@code resources}</li>
 *     <li>{@code tools}</li>
 *     <li>{@code extensions}</li>
 *     <li>{@code experimental}</li>
 * </ul>
 *
 * <p>
 * The deprecated {@code logging} capability is intentionally
 * omitted from this implementation.
 * </p>
 */
public final class McpServerCapabilities {

    private final Completions completions;

    private final Prompts prompts;

    private final Resources resources;

    private final Tools tools;

    private final Map<String, Map<String, Object>> extensions;

    private final Map<String, Map<String, Object>> experimental;


    private McpServerCapabilities(
            Builder builder
    ) {

        this.completions =
                builder.completions;

        this.prompts =
                builder.prompts;

        this.resources =
                builder.resources;

        this.tools =
                builder.tools;

        this.extensions =
                immutableNestedMap(
                        builder.extensions
                );

        this.experimental =
                immutableNestedMap(
                        builder.experimental
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
     * Creates an empty capability set.
     *
     * @return empty server capabilities
     */
    public static McpServerCapabilities empty() {

        return builder()
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public Completions getCompletions() {
        return completions;
    }

    public Prompts getPrompts() {
        return prompts;
    }

    public Resources getResources() {
        return resources;
    }

    public Tools getTools() {
        return tools;
    }

    public Map<String, Map<String, Object>> getExtensions() {
        return extensions;
    }

    public Map<String, Map<String, Object>> getExperimental() {
        return experimental;
    }


    /*
     * ------------------------------------------------------------
     * Capability predicates
     * ------------------------------------------------------------
     */

    public boolean supportsCompletions() {
        return completions != null;
    }

    public boolean supportsPrompts() {
        return prompts != null;
    }

    public boolean supportsResources() {
        return resources != null;
    }

    public boolean supportsTools() {
        return tools != null;
    }

    public boolean supportsPromptListChanged() {

        return prompts != null
                && prompts.supportsListChanged();
    }

    public boolean supportsResourceSubscribe() {

        return resources != null
                && resources.supportsSubscribe();
    }

    public boolean supportsResourceListChanged() {

        return resources != null
                && resources.supportsListChanged();
    }

    public boolean supportsToolListChanged() {

        return tools != null
                && tools.supportsListChanged();
    }

    public boolean hasExtensions() {

        return !extensions.isEmpty();
    }

    public boolean supportsExtension(
            String extensionId
    ) {

        if (extensionId == null) {
            return false;
        }

        return extensions.containsKey(
                extensionId
        );
    }

    public boolean hasExperimentalCapabilities() {

        return !experimental.isEmpty();
    }


    /*
     * ------------------------------------------------------------
     * Completions
     * ------------------------------------------------------------
     */

    /**
     * Completion capability.
     *
     * <p>
     * The presence of an empty object indicates that the server
     * supports {@code completion/complete}.
     * </p>
     */
    public static final class Completions {

        private static final Completions INSTANCE =
                new Completions();

        private Completions() {
        }

        public static Completions supported() {
            return INSTANCE;
        }

        @Override
        public boolean equals(
                Object object
        ) {

            return object instanceof Completions;
        }

        @Override
        public int hashCode() {
            return Completions.class.hashCode();
        }

        @Override
        public String toString() {
            return "Completions{}";
        }
    }


    /*
     * ------------------------------------------------------------
     * Prompts
     * ------------------------------------------------------------
     */

    /**
     * Prompt capability.
     */
    public static final class Prompts {

        private final boolean listChanged;

        private Prompts(
                boolean listChanged
        ) {

            this.listChanged =
                    listChanged;
        }

        /**
         * Declares baseline prompt support.
         *
         * @return prompt capability
         */
        public static Prompts supported() {

            return new Prompts(
                    false
            );
        }

        /**
         * Declares prompt support including list-change
         * notifications.
         *
         * @return prompt capability
         */
        public static Prompts withListChanged() {

            return new Prompts(
                    true
            );
        }

        public boolean supportsListChanged() {
            return listChanged;
        }

        @Override
        public boolean equals(
                Object object
        ) {

            if (this == object) {
                return true;
            }

            if (!(object instanceof Prompts)) {
                return false;
            }

            Prompts other =
                    (Prompts) object;

            return listChanged
                    == other.listChanged;
        }

        @Override
        public int hashCode() {

            return Boolean.hashCode(
                    listChanged
            );
        }

        @Override
        public String toString() {

            return "Prompts{"
                    + "listChanged="
                    + listChanged
                    + '}';
        }
    }


    /*
     * ------------------------------------------------------------
     * Resources
     * ------------------------------------------------------------
     */

    /**
     * Resource capability.
     */
    public static final class Resources {

        private final boolean subscribe;

        private final boolean listChanged;

        private Resources(
                boolean subscribe,
                boolean listChanged
        ) {

            this.subscribe =
                    subscribe;

            this.listChanged =
                    listChanged;
        }

        /**
         * Declares baseline resource support.
         *
         * @return resource capability
         */
        public static Resources supported() {

            return new Resources(
                    false,
                    false
            );
        }

        /**
         * Declares resource subscription support.
         *
         * @return resource capability
         */
        public static Resources withSubscribe() {

            return new Resources(
                    true,
                    false
            );
        }

        /**
         * Declares resource list-change notification support.
         *
         * @return resource capability
         */
        public static Resources withListChanged() {

            return new Resources(
                    false,
                    true
            );
        }

        /**
         * Declares full resource notification support.
         *
         * @return resource capability
         */
        public static Resources withNotifications() {

            return new Resources(
                    true,
                    true
            );
        }

        public boolean supportsSubscribe() {
            return subscribe;
        }

        public boolean supportsListChanged() {
            return listChanged;
        }

        @Override
        public boolean equals(
                Object object
        ) {

            if (this == object) {
                return true;
            }

            if (!(object instanceof Resources)) {
                return false;
            }

            Resources other =
                    (Resources) object;

            return subscribe
                    == other.subscribe
                    && listChanged
                    == other.listChanged;
        }

        @Override
        public int hashCode() {

            return Objects.hash(
                    subscribe,
                    listChanged
            );
        }

        @Override
        public String toString() {

            return "Resources{"
                    + "subscribe="
                    + subscribe
                    + ", listChanged="
                    + listChanged
                    + '}';
        }
    }


    /*
     * ------------------------------------------------------------
     * Tools
     * ------------------------------------------------------------
     */

    /**
     * Tool capability.
     */
    public static final class Tools {

        private final boolean listChanged;

        private Tools(
                boolean listChanged
        ) {

            this.listChanged =
                    listChanged;
        }

        /**
         * Declares baseline tool support.
         *
         * @return tool capability
         */
        public static Tools supported() {

            return new Tools(
                    false
            );
        }

        /**
         * Declares tool support including list-change
         * notifications.
         *
         * @return tool capability
         */
        public static Tools withListChanged() {

            return new Tools(
                    true
            );
        }

        public boolean supportsListChanged() {
            return listChanged;
        }

        @Override
        public boolean equals(
                Object object
        ) {

            if (this == object) {
                return true;
            }

            if (!(object instanceof Tools)) {
                return false;
            }

            Tools other =
                    (Tools) object;

            return listChanged
                    == other.listChanged;
        }

        @Override
        public int hashCode() {

            return Boolean.hashCode(
                    listChanged
            );
        }

        @Override
        public String toString() {

            return "Tools{"
                    + "listChanged="
                    + listChanged
                    + '}';
        }
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private Completions completions;

        private Prompts prompts;

        private Resources resources;

        private Tools tools;

        private final Map<String, Map<String, Object>> extensions =
                new LinkedHashMap<>();

        private final Map<String, Map<String, Object>> experimental =
                new LinkedHashMap<>();

        private Builder() {
        }

        public Builder completions(
                Completions completions
        ) {

            this.completions =
                    completions;

            return this;
        }

        public Builder prompts(
                Prompts prompts
        ) {

            this.prompts =
                    prompts;

            return this;
        }

        public Builder resources(
                Resources resources
        ) {

            this.resources =
                    resources;

            return this;
        }

        public Builder tools(
                Tools tools
        ) {

            this.tools =
                    tools;

            return this;
        }

        public Builder extension(
                String extensionId,
                Map<String, Object> configuration
        ) {

            String normalizedId =
                    requireText(
                            extensionId,
                            "MCP extension id"
                    );

            validateExtensionId(
                    normalizedId
            );

            extensions.put(
                    normalizedId,
                    copyConfiguration(
                            configuration
                    )
            );

            return this;
        }

        public Builder experimental(
                String name,
                Map<String, Object> configuration
        ) {

            String capabilityName =
                    requireText(
                            name,
                            "Experimental capability name"
                    );

            experimental.put(
                    capabilityName,
                    copyConfiguration(
                            configuration
                    )
            );

            return this;
        }

        public McpServerCapabilities build() {

            return new McpServerCapabilities(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

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

    private static void validateExtensionId(
            String extensionId
    ) {

        if (!extensionId.contains("/")) {

            throw new IllegalArgumentException(
                    "MCP extension id must be namespaced: "
                            + extensionId
            );
        }
    }

    private static Map<String, Object> copyConfiguration(
            Map<String, Object> configuration
    ) {

        if (configuration == null
                || configuration.isEmpty()) {

            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        configuration
                )
        );
    }

    private static Map<String, Map<String, Object>> immutableNestedMap(
            Map<String, Map<String, Object>> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyMap();
        }

        Map<String, Map<String, Object>> copy =
                new LinkedHashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry
                : source.entrySet()) {

            copy.put(
                    entry.getKey(),
                    copyConfiguration(
                            entry.getValue()
                    )
            );
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

        if (!(object instanceof McpServerCapabilities)) {
            return false;
        }

        McpServerCapabilities other =
                (McpServerCapabilities) object;

        return Objects.equals(
                completions,
                other.completions
        )
                && Objects.equals(
                        prompts,
                        other.prompts
                )
                && Objects.equals(
                        resources,
                        other.resources
                )
                && Objects.equals(
                        tools,
                        other.tools
                )
                && Objects.equals(
                        extensions,
                        other.extensions
                )
                && Objects.equals(
                        experimental,
                        other.experimental
                );
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                completions,
                prompts,
                resources,
                tools,
                extensions,
                experimental
        );
    }

    @Override
    public String toString() {

        return "McpServerCapabilities{"
                + "completions="
                + completions
                + ", prompts="
                + prompts
                + ", resources="
                + resources
                + ", tools="
                + tools
                + ", extensions="
                + extensions
                + ", experimental="
                + experimental
                + '}';
    }
}