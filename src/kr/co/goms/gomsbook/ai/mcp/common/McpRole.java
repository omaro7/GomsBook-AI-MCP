/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.common;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * MCP role.
 *
 * <p>
 * Represents a role used by MCP protocol models such as
 * annotations and prompt messages.
 * </p>
 *
 * <ul>
 *     <li>{@link #USER} - user role</li>
 *     <li>{@link #ASSISTANT} - assistant role</li>
 * </ul>
 */
public enum McpRole {

    /**
     * User role.
     */
    @SerializedName("user")
    USER("user"),

    /**
     * Assistant role.
     */
    @SerializedName("assistant")
    ASSISTANT("assistant");

    private final String value;

    McpRole(
            String value
    ) {

        this.value =
                Objects.requireNonNull(
                        value,
                        "value must not be null."
                );
    }

    /**
     * Returns the MCP protocol value.
     *
     * @return protocol value
     */
    public String getValue() {
        return value;
    }

    /**
     * Resolves an MCP role from a protocol value.
     *
     * @param value protocol value
     * @return resolved role
     * @throws IllegalArgumentException if the value is unsupported
     */
    public static McpRole fromValue(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    "MCP role must not be blank."
            );
        }

        String normalized =
                value
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return Arrays.stream(
                        values()
                )
                .filter(role ->
                        role.value.equals(
                                normalized
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported MCP role: "
                                        + value
                        )
                );
    }

    /**
     * Checks whether the specified protocol value
     * represents a supported MCP role.
     *
     * @param value protocol value
     * @return {@code true} if supported
     */
    public static boolean supports(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return false;
        }

        String normalized =
                value
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return Arrays.stream(
                        values()
                )
                .anyMatch(role ->
                        role.value.equals(
                                normalized
                        )
                );
    }

    /**
     * Returns whether this is the user role.
     *
     * @return {@code true} for {@link #USER}
     */
    public boolean isUser() {
        return this == USER;
    }

    /**
     * Returns whether this is the assistant role.
     *
     * @return {@code true} for {@link #ASSISTANT}
     */
    public boolean isAssistant() {
        return this == ASSISTANT;
    }

    @Override
    public String toString() {
        return value;
    }
}