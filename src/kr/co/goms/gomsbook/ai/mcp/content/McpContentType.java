/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.content;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * MCP content block type.
 *
 * <p>
 * Represents the discriminator value used by MCP content blocks.
 * </p>
 *
 * <ul>
 *     <li>{@link #TEXT} - text content</li>
 *     <li>{@link #IMAGE} - image content</li>
 *     <li>{@link #AUDIO} - audio content</li>
 *     <li>{@link #RESOURCE_LINK} - resource link content</li>
 *     <li>{@link #RESOURCE} - embedded resource content</li>
 * </ul>
 */
public enum McpContentType {

    /**
     * Text content.
     */
    @SerializedName("text")
    TEXT("text"),

    /**
     * Image content.
     */
    @SerializedName("image")
    IMAGE("image"),

    /**
     * Audio content.
     */
    @SerializedName("audio")
    AUDIO("audio"),

    /**
     * Resource link content.
     */
    @SerializedName("resource_link")
    RESOURCE_LINK("resource_link"),

    /**
     * Embedded resource content.
     */
    @SerializedName("resource")
    RESOURCE("resource");

    private final String value;

    McpContentType(
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
     * Resolves a content type from an MCP protocol value.
     *
     * @param value protocol value
     * @return resolved content type
     * @throws IllegalArgumentException if the value is unknown
     */
    public static McpContentType fromValue(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    "MCP content type must not be blank."
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
                .filter(type ->
                        type.value.equals(
                                normalized
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported MCP content type: "
                                        + value
                        )
                );
    }

    /**
     * Checks whether the specified protocol value is supported.
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
                .anyMatch(type ->
                        type.value.equals(
                                normalized
                        )
                );
    }

    @Override
    public String toString() {
        return value;
    }
}