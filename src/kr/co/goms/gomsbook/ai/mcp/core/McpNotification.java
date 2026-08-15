/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCP(Model Context Protocol) JSON-RPC notification.
 *
 * <p>
 * A notification is a JSON-RPC 2.0 message that does
 * not contain an id and does not expect a response.
 * </p>
 *
 * <p>
 * Typical MCP notifications include:
 * </p>
 *
 * <ul>
 *     <li>notifications/cancelled</li>
 *     <li>notifications/progress</li>
 *     <li>notifications/tools/list_changed</li>
 *     <li>notifications/resources/list_changed</li>
 *     <li>notifications/resources/updated</li>
 *     <li>notifications/prompts/list_changed</li>
 * </ul>
 */
public final class McpNotification {

    /**
     * JSON-RPC protocol version.
     */
    public static final String JSON_RPC_VERSION =
            "2.0";

    private String jsonrpc;

    private String method;

    private Map<String, Object> params;


    /**
     * Constructor for Gson deserialization.
     */
    public McpNotification() {

        this.jsonrpc =
                JSON_RPC_VERSION;

        this.params =
                new LinkedHashMap<>();
    }


    private McpNotification(
            Builder builder) {

        this.jsonrpc =
                JSON_RPC_VERSION;

        this.method =
                normalizeRequired(
                        builder.method,
                        "MCP notification method"
                );

        this.params =
                builder.params == null
                        ? new LinkedHashMap<>()
                        : new LinkedHashMap<>(
                                builder.params
                        );

        validate();
    }


    public static Builder builder() {

        return new Builder();
    }


    public String getJsonrpc() {

        return jsonrpc;
    }


    public String getMethod() {

        return method;
    }


    public Map<String, Object> getParams() {

        if (params == null) {

            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                params
        );
    }


    /**
     * Returns a notification parameter.
     *
     * @param name parameter name
     * @return parameter value or {@code null}
     */
    public Object getParam(
            String name) {

        if (name == null
                || params == null) {

            return null;
        }

        return params.get(
                name
        );
    }


    /**
     * Checks whether a parameter exists.
     *
     * @param name parameter name
     * @return {@code true} when parameter exists
     */
    public boolean hasParam(
            String name) {

        return name != null
                && params != null
                && params.containsKey(
                        name
                );
    }


    /**
     * Checks whether this notification contains parameters.
     */
    public boolean hasParams() {

        return params != null
                && !params.isEmpty();
    }


    /**
     * Validates this MCP notification.
     */
    public void validate() {

        if (!JSON_RPC_VERSION.equals(
                jsonrpc)) {

            throw new IllegalArgumentException(
                    "Invalid JSON-RPC version: "
                            + jsonrpc
            );
        }

        if (method == null
                || method.isBlank()) {

            throw new IllegalArgumentException(
                    "MCP notification method "
                            + "must not be blank."
            );
        }
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


    @Override
    public String toString() {

        return "McpNotification{"
                + "jsonrpc='"
                + jsonrpc
                + '\''
                + ", method='"
                + method
                + '\''
                + ", params="
                + params
                + '}';
    }


    /**
     * Builder for {@link McpNotification}.
     */
    public static final class Builder {

        private String method;

        private Map<String, Object> params =
                new LinkedHashMap<>();


        private Builder() {
        }


        public Builder method(
                String method) {

            this.method =
                    method;

            return this;
        }


        public Builder params(
                Map<String, Object> params) {

            this.params =
                    params == null
                            ? new LinkedHashMap<>()
                            : new LinkedHashMap<>(
                                    params
                            );

            return this;
        }


        public Builder param(
                String name,
                Object value) {

            if (name == null
                    || name.isBlank()) {

                throw new IllegalArgumentException(
                        "MCP notification parameter name "
                                + "must not be blank."
                );
            }

            this.params.put(
                    name.trim(),
                    value
            );

            return this;
        }


        public McpNotification build() {

            return new McpNotification(
                    this
            );
        }
    }
}