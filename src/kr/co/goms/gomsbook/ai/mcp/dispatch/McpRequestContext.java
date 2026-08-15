/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.dispatch;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpClientInfo;
import kr.co.goms.gomsbook.ai.mcp.core.McpRequest;
import kr.co.goms.gomsbook.ai.mcp.core.McpRequestMetadata;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpClientCapabilities;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpRequestId;

/**
 * Request-scoped context used while dispatching an MCP request.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This context is intentionally stateless across requests.
 * It contains only information belonging to the current request.
 * </p>
 */
public final class McpRequestContext {

    private final McpRequest request;

    private final Map<String, Object> attributes;


    private McpRequestContext(
            Builder builder
    ) {

        this.request =
                Objects.requireNonNull(
                        builder.request,
                        "MCP request must not be null."
                );

        this.attributes =
                immutableAttributes(
                        builder.attributes
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

    public static McpRequestContext of(
            McpRequest request
    ) {

        return builder()
                .request(request)
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Request access
     * ------------------------------------------------------------
     */

    public McpRequest getRequest() {
        return request;
    }

    public McpRequestId getRequestId() {
        return (McpRequestId)request.getId();
    }

    public String getMethod() {
        return request.getMethod();
    }

    public Map<String, Object> getParams() {
        return request.getParams();
    }

    public Map<String, Object> getMetadata() {
        return request.getMetadataMap();
    }


    /*
     * ------------------------------------------------------------
     * Metadata shortcuts
     * ------------------------------------------------------------
     */

    public String getProtocolVersion() {

        return request.getProtocolVersion();
    }


    public Object getClientCapabilities() {

        return request.getClientCapabilities();
    }


    public Object getClientInfo() {

        return request.getClientInfo();
    }


    public String getLogLevel() {

        Object value =
                request.getMetadataMap()
                        .get(
                                McpRequestMetadata.KEY_LOG_LEVEL
                        );

        if (!(value instanceof String)) {
            return null;
        }

        String logLevel =
                ((String) value).trim();

        return logLevel.isEmpty()
                ? null
                : logLevel;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasRequestId() {

        return request.getId() != null;
    }


    public boolean isNotification() {

        return request.getId() == null;
    }


    public boolean hasClientInfo() {

        return request.getClientInfo() != null;
    }


    public boolean hasLogLevel() {

        Object value =
                request.getMetadataMap()
                        .get(
                                McpRequestMetadata.KEY_LOG_LEVEL
                        );

        return value instanceof String
                && !((String) value).isBlank();
    }


    /*
     * ------------------------------------------------------------
     * Request parameters
     * ------------------------------------------------------------
     */

    public boolean hasParam(
            String name
    ) {

        return request.hasParam(
                name
        );
    }

    public Object getParam(
            String name
    ) {

        return request.getParam(
                name
        );
    }


    /*
     * ------------------------------------------------------------
     * Context attributes
     * ------------------------------------------------------------
     */

    /**
     * Returns immutable request-scoped attributes.
     *
     * <p>
     * Attributes are internal runtime values and are never
     * serialized into MCP messages.
     * </p>
     *
     * @return attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public boolean hasAttribute(
            String name
    ) {

        if (name == null) {
            return false;
        }

        return attributes.containsKey(
                name
        );
    }

    public Object getAttribute(
            String name
    ) {

        if (name == null) {
            return null;
        }

        return attributes.get(
                name
        );
    }

    /**
     * Returns a context attribute using an expected Java type.
     *
     * @param name attribute name
     * @param type expected type
     * @param <T> attribute type
     * @return attribute value, or {@code null}
     */
    public <T> T getAttribute(
            String name,
            Class<T> type
    ) {

        Objects.requireNonNull(
                type,
                "Attribute type must not be null."
        );

        Object value =
                getAttribute(
                        name
                );

        if (value == null) {
            return null;
        }

        if (!type.isInstance(value)) {

            throw new IllegalStateException(
                    "MCP request context attribute '"
                            + name
                            + "' is not of type "
                            + type.getName()
                            + ". Actual type: "
                            + value.getClass().getName()
            );
        }

        return type.cast(
                value
        );
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private McpRequest request;

        private final Map<String, Object> attributes =
                new LinkedHashMap<>();


        private Builder() {
        }


        public Builder request(
                McpRequest request
        ) {

            this.request =
                    request;

            return this;
        }


        /**
         * Adds an internal request-scoped runtime attribute.
         *
         * @param name attribute name
         * @param value attribute value
         * @return builder
         */
        public Builder attribute(
                String name,
                Object value
        ) {

            String normalizedName =
                    requireAttributeName(
                            name
                    );

            if (value == null) {

                attributes.remove(
                        normalizedName
                );

            } else {

                attributes.put(
                        normalizedName,
                        value
                );
            }

            return this;
        }


        public Builder attributes(
                Map<String, Object> attributes
        ) {

            this.attributes.clear();

            if (attributes == null
                    || attributes.isEmpty()) {

                return this;
            }

            for (Map.Entry<String, Object> entry
                    : attributes.entrySet()) {

                attribute(
                        entry.getKey(),
                        entry.getValue()
                );
            }

            return this;
        }


        public McpRequestContext build() {

            return new McpRequestContext(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static String requireAttributeName(
            String name
    ) {

        Objects.requireNonNull(
                name,
                "MCP request context attribute name must not be null."
        );

        String normalized =
                name.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP request context attribute name must not be blank."
            );
        }

        return normalized;
    }


    private static Map<String, Object> immutableAttributes(
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
                    requireAttributeName(
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

        if (!(object instanceof McpRequestContext)) {
            return false;
        }

        McpRequestContext other =
                (McpRequestContext) object;

        return Objects.equals(
                request,
                other.request
        )
                && Objects.equals(
                        attributes,
                        other.attributes
                );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                request,
                attributes
        );
    }


    @Override
    public String toString() {

        return "McpRequestContext{"
                + "request="
                + request
                + ", attributes="
                + attributes
                + '}';
    }
}