/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.core;

import java.util.Objects;

/**
 * Represents the MCP result type.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Core MCP result types:
 * </p>
 *
 * <ul>
 *     <li>{@code complete}</li>
 *     <li>{@code input_required}</li>
 * </ul>
 *
 * <p>
 * MCP extensions may define additional result types, therefore
 * this class is implemented as a string-based value object rather
 * than a closed enum.
 * </p>
 */
public final class McpResultType {

    /**
     * Indicates that the request completed and the returned result
     * is final.
     */
    public static final String COMPLETE_VALUE =
            "complete";

    /**
     * Indicates that additional input is required before the
     * original request can be completed.
     */
    public static final String INPUT_REQUIRED_VALUE =
            "input_required";

    /**
     * Core complete result type.
     */
    public static final McpResultType COMPLETE =
            new McpResultType(
                    COMPLETE_VALUE
            );

    /**
     * Core multi round-trip interim result type.
     */
    public static final McpResultType INPUT_REQUIRED =
            new McpResultType(
                    INPUT_REQUIRED_VALUE
            );

    private final String value;

    private McpResultType(
            String value
    ) {

        this.value =
                requireValue(
                        value
                );
    }

    /*
     * ------------------------------------------------------------
     * Factory
     * ------------------------------------------------------------
     */

    /**
     * Creates a result type.
     *
     * <p>
     * Core values return their shared constants. Other values are
     * preserved for extension-defined result types.
     * </p>
     *
     * @param value result type value
     * @return result type
     */
    public static McpResultType of(
            String value
    ) {

        String normalized =
                requireValue(
                        value
                );

        if (COMPLETE_VALUE.equals(
                normalized
        )) {

            return COMPLETE;
        }

        if (INPUT_REQUIRED_VALUE.equals(
                normalized
        )) {

            return INPUT_REQUIRED;
        }

        return new McpResultType(
                normalized
        );
    }

    /*
     * ------------------------------------------------------------
     * Accessor
     * ------------------------------------------------------------
     */

    public String value() {
        return value;
    }

    /*
     * ------------------------------------------------------------
     * Core type predicates
     * ------------------------------------------------------------
     */

    public boolean isComplete() {

        return COMPLETE_VALUE.equals(
                value
        );
    }

    public boolean isInputRequired() {

        return INPUT_REQUIRED_VALUE.equals(
                value
        );
    }

    /**
     * Returns whether this value is defined by the MCP core
     * protocol.
     *
     * @return true for complete or input_required
     */
    public boolean isCoreType() {

        return isComplete()
                || isInputRequired();
    }

    /**
     * Returns whether this value represents an extension-defined
     * result type.
     *
     * @return true if not a core type
     */
    public boolean isExtensionType() {

        return !isCoreType();
    }

    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    /**
     * Returns whether the given raw value is a core MCP result
     * type.
     *
     * @param value raw result type
     * @return true if core result type
     */
    public static boolean isCoreType(
            String value
    ) {

        if (value == null) {
            return false;
        }

        return COMPLETE_VALUE.equals(
                value
        )
                || INPUT_REQUIRED_VALUE.equals(
                        value
                );
    }

    /**
     * Requires a core result type.
     *
     * <p>
     * This method is intended for contexts where no negotiated
     * extension result types are allowed.
     * </p>
     *
     * @param value result type
     * @return result type
     */
    public static McpResultType requireCoreType(
            String value
    ) {

        McpResultType resultType =
                of(
                        value
                );

        if (!resultType.isCoreType()) {

            throw new IllegalArgumentException(
                    "Unsupported MCP core result type: "
                            + resultType.value
            );
        }

        return resultType;
    }

    private static String requireValue(
            String value
    ) {

        Objects.requireNonNull(
                value,
                "MCP result type must not be null."
        );

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP result type must not be blank."
            );
        }

        return normalized;
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

        if (!(object instanceof McpResultType)) {
            return false;
        }

        McpResultType other =
                (McpResultType) object;

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
        return value;
    }
}