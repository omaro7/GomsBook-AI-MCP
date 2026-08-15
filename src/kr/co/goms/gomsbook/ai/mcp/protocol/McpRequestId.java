/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.protocol;

import java.util.Objects;

/**
 * Represents a JSON-RPC request identifier used by MCP.
 *
 * <p>
 * A request identifier may be either a string or a number.
 * Notifications do not have an identifier and therefore should
 * not use this class.
 * </p>
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 */
public final class McpRequestId {

    private final Object value;

    private McpRequestId(
            Object value
    ) {

        this.value =
                validate(
                        value
                );
    }

    /**
     * Creates a request identifier from a string.
     *
     * @param value identifier value
     * @return request identifier
     */
    public static McpRequestId of(
            String value
    ) {

        Objects.requireNonNull(
                value,
                "MCP request id must not be null."
        );

        if (value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP request id must not be blank."
            );
        }

        return new McpRequestId(
                value
        );
    }

    /**
     * Creates a request identifier from an integer.
     *
     * @param value identifier value
     * @return request identifier
     */
    public static McpRequestId of(
            int value
    ) {

        return new McpRequestId(
                Integer.valueOf(value)
        );
    }

    /**
     * Creates a request identifier from a long.
     *
     * @param value identifier value
     * @return request identifier
     */
    public static McpRequestId of(
            long value
    ) {

        return new McpRequestId(
                Long.valueOf(value)
        );
    }

    /**
     * Creates a request identifier from a supported raw value.
     *
     * @param value string or number
     * @return request identifier
     */
    public static McpRequestId from(
            Object value
    ) {

        return new McpRequestId(
                value
        );
    }

    /**
     * Returns the raw JSON-RPC identifier value.
     *
     * @return identifier value
     */
    public Object value() {
        return value;
    }

    /**
     * Returns whether this identifier is a string.
     *
     * @return true if string identifier
     */
    public boolean isString() {
        return value instanceof String;
    }

    /**
     * Returns whether this identifier is numeric.
     *
     * @return true if numeric identifier
     */
    public boolean isNumber() {
        return value instanceof Number;
    }

    /**
     * Returns this identifier as a string.
     *
     * @return string identifier
     *
     * @throws IllegalStateException
     *         if this identifier is not a string
     */
    public String stringValue() {

        if (!isString()) {

            throw new IllegalStateException(
                    "MCP request id is not a string."
            );
        }

        return (String) value;
    }

    /**
     * Returns this identifier as a number.
     *
     * @return numeric identifier
     *
     * @throws IllegalStateException
     *         if this identifier is not numeric
     */
    public Number numberValue() {

        if (!isNumber()) {

            throw new IllegalStateException(
                    "MCP request id is not numeric."
            );
        }

        return (Number) value;
    }

    private static Object validate(
            Object value
    ) {

        Objects.requireNonNull(
                value,
                "MCP request id must not be null."
        );

        if (value instanceof String) {

            String stringValue =
                    (String) value;

            if (stringValue.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "MCP request id must not be blank."
                );
            }

            return stringValue;
        }

        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long) {

            return value;
        }

        throw new IllegalArgumentException(
                "Unsupported MCP request id type: "
                        + value.getClass().getName()
        );
    }

    @Override
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpRequestId)) {
            return false;
        }

        McpRequestId other =
                (McpRequestId) object;

        return value.equals(
                other.value
        );
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}