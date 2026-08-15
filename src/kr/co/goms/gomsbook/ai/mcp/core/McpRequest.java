/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpClientCapabilities;

/**
 * MCP(Model Context Protocol) JSON-RPC request.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Request-level MCP metadata is carried on the wire inside
 * {@code params._meta}, while the Java model also retains the
 * typed {@link McpRequestMetadata} representation.
 * </p>
 */
public final class McpRequest {

    public static final String JSON_RPC_VERSION =
            "2.0";

    public static final String PARAM_META =
            "_meta";


    private String jsonrpc;

    private Object id;

    private String method;

    private Map<String, Object> params;

    /**
     * Typed Java representation of params._meta.
     *
     * <p>
     * The actual wire representation remains inside params._meta.
     * </p>
     */
    private transient McpRequestMetadata metadata;


    /*
     * ------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------
     */

    /**
     * Constructor for Gson deserialization.
     */
    public McpRequest() {

        this.jsonrpc =
                JSON_RPC_VERSION;

        this.params =
                new LinkedHashMap<>();

        this.metadata =
                null;
    }


    private McpRequest(
            Builder builder
    ) {

        this.jsonrpc =
                JSON_RPC_VERSION;

        this.id =
                builder.id;

        this.method =
                normalizeRequired(
                        builder.method,
                        "MCP request method"
                );

        this.params =
                builder.params == null
                        ? new LinkedHashMap<>()
                        : new LinkedHashMap<>(
                                builder.params
                        );

        /*
         * Do not allow params(...) to inject _meta directly.
         * Metadata has its own typed path.
         */
        this.params.remove(
                PARAM_META
        );

        this.metadata =
                builder.metadata;

        if (metadata != null) {

            this.params.put(
                    PARAM_META,
                    metadata.toMap()
            );
        }

        validate();
    }


    /*
     * ------------------------------------------------------------
     * Factory
     * ------------------------------------------------------------
     */

    public static Builder builder() {

        return new Builder();
    }


    /*
     * ------------------------------------------------------------
     * Basic accessors
     * ------------------------------------------------------------
     */

    public String getJsonrpc() {

        return jsonrpc;
    }


    public Object getId() {

        return id;
    }


    public String getMethod() {

        return method;
    }


    public Map<String, Object> getParams() {

        if (params == null
                || params.isEmpty()) {

            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                params
        );
    }


    public Object getParam(
            String name
    ) {

        if (name == null
                || params == null) {

            return null;
        }

        return params.get(
                name
        );
    }


    /*
     * ------------------------------------------------------------
     * Typed metadata
     * ------------------------------------------------------------
     */

    /**
     * Returns typed MCP request metadata.
     *
     * @return metadata, or {@code null}
     */
    public McpRequestMetadata getMetadata() {

        return metadata;
    }


    /**
     * Returns request metadata in its wire-map representation.
     *
     * @return immutable metadata map
     */
    public Map<String, Object> getMetadataMap() {

        if (metadata != null) {

            Map<String, Object> map =
                    metadata.toMap();

            if (map == null
                    || map.isEmpty()) {

                return Collections.emptyMap();
            }

            return Collections.unmodifiableMap(
                    new LinkedHashMap<>(
                            map
                    )
            );
        }


        /*
         * Defensive fallback for objects populated from raw JSON.
         */
        if (params == null) {

            return Collections.emptyMap();
        }

        Object rawMetadata =
                params.get(
                        PARAM_META
                );

        if (!(rawMetadata instanceof Map)) {

            return Collections.emptyMap();
        }

        Map<?, ?> source =
                (Map<?, ?>) rawMetadata;

        Map<String, Object> result =
                new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry
                : source.entrySet()) {

            if (entry.getKey()
                    instanceof String) {

                result.put(
                        (String) entry.getKey(),
                        entry.getValue()
                );
            }
        }

        if (result.isEmpty()) {

            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                result
        );
    }


    /*
     * ------------------------------------------------------------
     * Metadata convenience accessors
     * ------------------------------------------------------------
     */

    public String getProtocolVersion() {

        return metadata == null
                ? null
                : metadata.getProtocolVersion();
    }


    public McpClientCapabilities getClientCapabilities() {

        return metadata == null
                ? null
                : metadata.getClientCapabilities();
    }


    public McpClientInfo getClientInfo() {

        return metadata == null
                ? null
                : metadata.getClientInfo();
    }


    public String getLogLevel() {

        return metadata == null
                ? null
                : metadata.getLogLevel();
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasId() {

        return id != null;
    }


    public boolean isNotification() {

        return id == null;
    }


    public boolean hasParams() {

        return params != null
                && !params.isEmpty();
    }


    public boolean hasParam(
            String name
    ) {

        return name != null
                && params != null
                && params.containsKey(
                        name
                );
    }


    public boolean hasMetadata() {

        return metadata != null
                || (params != null
                && params.get(
                        PARAM_META
                ) instanceof Map);
    }


    public boolean hasClientInfo() {

        return metadata != null
                && metadata.getClientInfo() != null;
    }


    public boolean hasClientCapabilities() {

        return metadata != null
                && metadata.getClientCapabilities() != null;
    }


    public boolean hasLogLevel() {

        if (metadata == null) {
            return false;
        }

        String logLevel =
                metadata.getLogLevel();

        return logLevel != null
                && !logLevel.isBlank();
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    public void validate() {

        if (!JSON_RPC_VERSION.equals(
                jsonrpc
        )) {

            throw new IllegalArgumentException(
                    "Invalid JSON-RPC version: "
                            + jsonrpc
            );
        }

        validateId(
                id
        );

        if (method == null
                || method.isBlank()) {

            throw new IllegalArgumentException(
                    "MCP request method must not be blank."
            );
        }

        validateMetadataShape();
    }


    /**
     * Validates the structural relationship between the typed
     * metadata model and params._meta.
     *
     * <p>
     * Mandatory protocol-version and capability validation remains
     * the responsibility of the runtime.
     * </p>
     */
    private void validateMetadataShape() {

        if (params == null
                || !params.containsKey(
                        PARAM_META
                )) {

            return;
        }

        Object rawMetadata =
                params.get(
                        PARAM_META
                );

        if (!(rawMetadata instanceof Map)) {

            throw new IllegalArgumentException(
                    "MCP request params._meta must be an object."
            );
        }
    }


    private static void validateId(
            Object id
    ) {

        /*
         * null id means JSON-RPC notification.
         */
        if (id == null) {
            return;
        }

        if (id instanceof String) {

            if (((String) id).isBlank()) {

                throw new IllegalArgumentException(
                        "MCP request id must not be blank."
                );
            }

            return;
        }

        if (id instanceof Byte
                || id instanceof Short
                || id instanceof Integer
                || id instanceof Long) {

            return;
        }

        throw new IllegalArgumentException(
                "MCP request id must be "
                        + "a string or integer: "
                        + id.getClass().getName()
        );
    }


    private static String normalizeRequired(
            String value,
            String fieldName
    ) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must not be blank."
            );
        }

        return value.trim();
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private Object id;

        private String method;

        private Map<String, Object> params =
                new LinkedHashMap<>();

        private McpRequestMetadata metadata;


        private Builder() {
        }


        public Builder id(
                String id
        ) {

            this.id =
                    id;

            return this;
        }


        public Builder id(
                long id
        ) {

            this.id =
                    Long.valueOf(
                            id
                    );

            return this;
        }


        public Builder id(
                Object id
        ) {

            this.id =
                    id;

            return this;
        }


        public Builder method(
                String method
        ) {

            this.method =
                    method;

            return this;
        }


        public Builder params(
                Map<String, Object> params
        ) {

            this.params =
                    new LinkedHashMap<>();

            if (params == null
                    || params.isEmpty()) {

                return this;
            }

            for (Map.Entry<String, Object> entry
                    : params.entrySet()) {

                String name =
                        requireParamName(
                                entry.getKey()
                        );

                if (PARAM_META.equals(
                        name
                )) {

                    /*
                     * Metadata must be supplied through
                     * metadata(...).
                     */
                    continue;
                }

                this.params.put(
                        name,
                        entry.getValue()
                );
            }

            return this;
        }


        public Builder param(
                String name,
                Object value
        ) {

            String normalizedName =
                    requireParamName(
                            name
                    );

            if (PARAM_META.equals(
                    normalizedName
            )) {

                throw new IllegalArgumentException(
                        "Use metadata(...) to set params._meta."
                );
            }

            if (value == null) {

                params.remove(
                        normalizedName
                );

            } else {

                params.put(
                        normalizedName,
                        value
                );
            }

            return this;
        }


        public Builder metadata(
                McpRequestMetadata metadata
        ) {

            this.metadata =
                    metadata;

            return this;
        }


        public McpRequest build() {

            return new McpRequest(
                    this
            );
        }


        private static String requireParamName(
                String name
        ) {

            Objects.requireNonNull(
                    name,
                    "MCP parameter name must not be null."
            );

            String normalized =
                    name.trim();

            if (normalized.isEmpty()) {

                throw new IllegalArgumentException(
                        "MCP parameter name must not be blank."
                );
            }

            return normalized;
        }
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "McpRequest{"
                + "jsonrpc='"
                + jsonrpc
                + '\''
                + ", id="
                + id
                + ", method='"
                + method
                + '\''
                + ", params="
                + params
                + '}';
    }
}