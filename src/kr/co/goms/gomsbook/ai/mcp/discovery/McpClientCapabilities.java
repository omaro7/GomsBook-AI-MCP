/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.discovery;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCP client capabilities.
 *
 * <p>
 * Represents capabilities declared by an MCP client
 * in request metadata:
 * </p>
 *
 * <pre>
 * params._meta
 *   └─ io.modelcontextprotocol/clientCapabilities
 * </pre>
 *
 * <p>
 * Known MCP client capabilities include:
 * </p>
 *
 * <ul>
 *     <li>elicitation</li>
 *     <li>sampling</li>
 *     <li>roots</li>
 *     <li>extensions</li>
 *     <li>experimental</li>
 * </ul>
 *
 * <p>
 * Roots and sampling remain supported for compatibility,
 * but are deprecated in protocol version 2026-07-28.
 * </p>
 */
public final class McpClientCapabilities {

    private Map<String, Object> roots;

    private SamplingCapability sampling;

    private ElicitationCapability elicitation;

    private Map<String, Map<String, Object>> extensions;

    private Map<String, Map<String, Object>> experimental;


    /**
     * Constructor for Gson deserialization.
     */
    public McpClientCapabilities() {

        this.extensions =
                new LinkedHashMap<>();

        this.experimental =
                new LinkedHashMap<>();
    }


    private McpClientCapabilities(
            Builder builder) {

        this.roots =
                copyMap(
                        builder.roots
                );

        this.sampling =
                builder.sampling;

        this.elicitation =
                builder.elicitation;

        this.extensions =
                copyNestedMap(
                        builder.extensions
                );

        this.experimental =
                copyNestedMap(
                        builder.experimental
                );

        validate();
    }


    public static Builder builder() {

        return new Builder();
    }


    /**
     * Creates an empty capability declaration.
     *
     * <p>
     * The empty object is valid when the client does not
     * expose any optional MCP client feature.
     * </p>
     */
    public static McpClientCapabilities empty() {

        return builder()
                .build();
    }


    public Map<String, Object> getRoots() {

        if (roots == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                roots
        );
    }


    public SamplingCapability getSampling() {

        return sampling;
    }


    public ElicitationCapability getElicitation() {

        return elicitation;
    }


    public Map<String, Map<String, Object>>
            getExtensions() {

        return unmodifiableNestedMap(
                extensions
        );
    }


    public Map<String, Map<String, Object>>
            getExperimental() {

        return unmodifiableNestedMap(
                experimental
        );
    }


    public boolean supportsRoots() {

        return roots != null;
    }


    public boolean supportsSampling() {

        return sampling != null;
    }


    public boolean supportsElicitation() {

        return elicitation != null;
    }


    public boolean supportsElicitationForm() {

        return elicitation != null
                && elicitation.supportsForm();
    }


    public boolean supportsElicitationUrl() {

        return elicitation != null
                && elicitation.supportsUrl();
    }


    public boolean supportsSamplingTools() {

        return sampling != null
                && sampling.supportsTools();
    }


    public boolean supportsSamplingContext() {

        return sampling != null
                && sampling.supportsContext();
    }


    public boolean supportsExtension(
            String extensionId) {

        if (extensionId == null
                || extensionId.isBlank()) {

            return false;
        }

        return extensions != null
                && extensions.containsKey(
                        extensionId.trim()
                );
    }


    public Map<String, Object> getExtension(
            String extensionId) {

        if (!supportsExtension(
                extensionId)) {

            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                extensions.get(
                        extensionId.trim()
                )
        );
    }


    /**
     * Validates capability declarations.
     */
    public void validate() {

        if (sampling != null) {

            sampling.validate();
        }

        if (elicitation != null) {

            elicitation.validate();
        }

        validateCapabilityMap(
                extensions,
                "MCP extension"
        );

        validateCapabilityMap(
                experimental,
                "MCP experimental capability"
        );
    }


    private static void validateCapabilityMap(
            Map<String, Map<String, Object>> capabilities,
            String fieldName) {

        if (capabilities == null) {
            return;
        }

        for (Map.Entry<String, Map<String, Object>> entry
                : capabilities.entrySet()) {

            String key =
                    entry.getKey();

            if (key == null
                    || key.isBlank()) {

                throw new IllegalArgumentException(
                        fieldName
                                + " identifier must not be blank."
                );
            }

            if (entry.getValue() == null) {

                throw new IllegalArgumentException(
                        fieldName
                                + " settings must not be null: "
                                + key
                );
            }
        }
    }


    private static Map<String, Object> copyMap(
            Map<String, Object> source) {

        if (source == null) {
            return null;
        }

        return new LinkedHashMap<>(
                source
        );
    }


    private static Map<String, Map<String, Object>>
            copyNestedMap(
                    Map<String, Map<String, Object>> source) {

        Map<String, Map<String, Object>> result =
                new LinkedHashMap<>();

        if (source == null) {
            return result;
        }

        for (Map.Entry<String, Map<String, Object>> entry
                : source.entrySet()) {

            result.put(
                    entry.getKey(),
                    entry.getValue() == null
                            ? null
                            : new LinkedHashMap<>(
                                    entry.getValue()
                            )
            );
        }

        return result;
    }


    private static Map<String, Map<String, Object>>
            unmodifiableNestedMap(
                    Map<String, Map<String, Object>> source) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyMap();
        }

        Map<String, Map<String, Object>> result =
                new LinkedHashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry
                : source.entrySet()) {

            result.put(
                    entry.getKey(),
                    Collections.unmodifiableMap(
                            entry.getValue()
                    )
            );
        }

        return Collections.unmodifiableMap(
                result
        );
    }


    @Override
    public String toString() {

        return "McpClientCapabilities{"
                + "roots="
                + roots
                + ", sampling="
                + sampling
                + ", elicitation="
                + elicitation
                + ", extensions="
                + extensions
                + ", experimental="
                + experimental
                + '}';
    }


    /**
     * Builder for {@link McpClientCapabilities}.
     */
    public static final class Builder {

        private Map<String, Object> roots;

        private SamplingCapability sampling;

        private ElicitationCapability elicitation;

        private Map<String, Map<String, Object>> extensions =
                new LinkedHashMap<>();

        private Map<String, Map<String, Object>> experimental =
                new LinkedHashMap<>();


        private Builder() {
        }


        /**
         * Enables legacy roots capability.
         */
        public Builder roots() {

            this.roots =
                    new LinkedHashMap<>();

            return this;
        }


        public Builder roots(
                Map<String, Object> settings) {

            this.roots =
                    settings == null
                            ? new LinkedHashMap<>()
                            : new LinkedHashMap<>(
                                    settings
                            );

            return this;
        }


        public Builder sampling(
                SamplingCapability sampling) {

            this.sampling =
                    sampling;

            return this;
        }


        public Builder elicitation(
                ElicitationCapability elicitation) {

            this.elicitation =
                    elicitation;

            return this;
        }


        public Builder extension(
                String extensionId,
                Map<String, Object> settings) {

            validateIdentifier(
                    extensionId,
                    "MCP extension"
            );

            this.extensions.put(
                    extensionId.trim(),
                    settings == null
                            ? new LinkedHashMap<>()
                            : new LinkedHashMap<>(
                                    settings
                            )
            );

            return this;
        }


        public Builder experimental(
                String capabilityName,
                Map<String, Object> settings) {

            validateIdentifier(
                    capabilityName,
                    "MCP experimental capability"
            );

            this.experimental.put(
                    capabilityName.trim(),
                    settings == null
                            ? new LinkedHashMap<>()
                            : new LinkedHashMap<>(
                                    settings
                            )
            );

            return this;
        }


        public McpClientCapabilities build() {

            return new McpClientCapabilities(
                    this
            );
        }


        private static void validateIdentifier(
                String value,
                String fieldName) {

            if (value == null
                    || value.isBlank()) {

                throw new IllegalArgumentException(
                        fieldName
                                + " identifier must not be blank."
                );
            }
        }
    }


    /**
     * MCP sampling capability.
     *
     * <p>
     * Sampling remains in the 2026-07-28 schema for
     * backwards compatibility, but is deprecated.
     * </p>
     */
    public static final class SamplingCapability {

        private Map<String, Object> context;

        private Map<String, Object> tools;


        /**
         * Constructor for Gson.
         */
        public SamplingCapability() {
        }


        private SamplingCapability(
                SamplingBuilder builder) {

            this.context =
                    copyMap(
                            builder.context
                    );

            this.tools =
                    copyMap(
                            builder.tools
                    );

            validate();
        }


        public static SamplingBuilder builder() {

            return new SamplingBuilder();
        }


        public static SamplingCapability basic() {

            return builder()
                    .build();
        }


        public boolean supportsContext() {

            return context != null;
        }


        public boolean supportsTools() {

            return tools != null;
        }


        public Map<String, Object> getContext() {

            if (context == null) {
                return Collections.emptyMap();
            }

            return Collections.unmodifiableMap(
                    context
            );
        }


        public Map<String, Object> getTools() {

            if (tools == null) {
                return Collections.emptyMap();
            }

            return Collections.unmodifiableMap(
                    tools
            );
        }


        public void validate() {
            // No required sub-capabilities.
        }


        @Override
        public String toString() {

            return "SamplingCapability{"
                    + "context="
                    + context
                    + ", tools="
                    + tools
                    + '}';
        }
    }


    /**
     * Builder for sampling capability.
     */
    public static final class SamplingBuilder {

        private Map<String, Object> context;

        private Map<String, Object> tools;


        private SamplingBuilder() {
        }


        public SamplingBuilder context() {

            this.context =
                    new LinkedHashMap<>();

            return this;
        }


        public SamplingBuilder tools() {

            this.tools =
                    new LinkedHashMap<>();

            return this;
        }


        public SamplingCapability build() {

            return new SamplingCapability(
                    this
            );
        }
    }


    /**
     * MCP elicitation capability.
     */
    public static final class ElicitationCapability {

        private Map<String, Object> form;

        private Map<String, Object> url;


        /**
         * Constructor for Gson.
         */
        public ElicitationCapability() {
        }


        private ElicitationCapability(
                ElicitationBuilder builder) {

            this.form =
                    copyMap(
                            builder.form
                    );

            this.url =
                    copyMap(
                            builder.url
                    );

            validate();
        }


        public static ElicitationBuilder builder() {

            return new ElicitationBuilder();
        }


        /**
         * Minimum elicitation capability.
         *
         * <p>
         * An empty elicitation object implies form mode
         * support according to MCP.
         * </p>
         */
        public static ElicitationCapability form() {

            return builder()
                    .form()
                    .build();
        }


        public boolean supportsForm() {

            /*
             * Explicit form declaration is supported.
             *
             * An entirely empty elicitation capability
             * also represents implicit form support.
             */
            return form != null
                    || (form == null
                    && url == null);
        }


        public boolean supportsUrl() {

            return url != null;
        }


        public Map<String, Object> getForm() {

            if (form == null) {
                return Collections.emptyMap();
            }

            return Collections.unmodifiableMap(
                    form
            );
        }


        public Map<String, Object> getUrl() {

            if (url == null) {
                return Collections.emptyMap();
            }

            return Collections.unmodifiableMap(
                    url
            );
        }


        public void validate() {
            // No required sub-capabilities.
        }


        @Override
        public String toString() {

            return "ElicitationCapability{"
                    + "form="
                    + form
                    + ", url="
                    + url
                    + '}';
        }
    }


    /**
     * Builder for elicitation capability.
     */
    public static final class ElicitationBuilder {

        private Map<String, Object> form;

        private Map<String, Object> url;


        private ElicitationBuilder() {
        }


        public ElicitationBuilder form() {

            this.form =
                    new LinkedHashMap<>();

            return this;
        }


        public ElicitationBuilder url() {

            this.url =
                    new LinkedHashMap<>();

            return this;
        }


        public ElicitationCapability build() {

            return new ElicitationCapability(
                    this
            );
        }
    }
}