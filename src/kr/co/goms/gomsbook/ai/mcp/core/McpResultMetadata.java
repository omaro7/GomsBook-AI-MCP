/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

import kr.co.goms.gomsbook.ai.mcp.protocol.McpServerInfo;

/**
 * MCP 2026-07-28 result metadata.
 *
 * <p>
 * Represents the {@code ResultMetaObject} used by
 * {@code Result._meta}.
 * </p>
 *
 * <p>
 * Standard MCP field:
 * </p>
 *
 * <pre>
 * {
 *   "io.modelcontextprotocol/serverInfo": {
 *     "name": "GomsBook-AI-Agent",
 *     "version": "1.0.0"
 *   }
 * }
 * </pre>
 */
public final class McpResultMetadata {

    public static final String SERVER_INFO_KEY =
            "io.modelcontextprotocol/serverInfo";

    /**
     * Identifies the server implementation that produced
     * the result.
     */
    @SerializedName(SERVER_INFO_KEY)
    private final McpServerInfo serverInfo;

    /**
     * Optional extension metadata.
     *
     * <p>
     * MCP MetaObject allows additional namespaced properties.
     * This map is intended for application-specific metadata.
     * </p>
     */
    private final Map<String, Object> extensions;

    /**
     * Creates empty result metadata.
     */
    public McpResultMetadata() {

        this(
                null,
                Collections.emptyMap()
        );
    }

    /**
     * Creates result metadata containing server information.
     *
     * @param serverInfo server implementation information
     */
    public McpResultMetadata(
    		McpServerInfo serverInfo) {

        this(
                serverInfo,
                Collections.emptyMap()
        );
    }

    /**
     * Creates result metadata.
     *
     * @param serverInfo optional server implementation information
     * @param extensions optional extension metadata
     */
    public McpResultMetadata(
    		McpServerInfo serverInfo,
            Map<String, Object> extensions) {

        this.serverInfo =
                serverInfo;

        Objects.requireNonNull(
                extensions,
                "extensions must not be null."
        );

        Map<String, Object> copy =
                new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry
                : extensions.entrySet()) {

            String key =
                    requireText(
                            entry.getKey(),
                            "extension key"
                    );

            if (SERVER_INFO_KEY.equals(key)) {

                throw new IllegalArgumentException(
                        "extensions must not contain reserved key '" +
                                SERVER_INFO_KEY +
                                "'."
                );
            }

            copy.put(
                    key,
                    entry.getValue()
            );
        }

        this.extensions =
                Collections.unmodifiableMap(
                        copy
                );
    }

    public McpServerInfo getServerInfo() {

        return serverInfo;
    }

    public Map<String, Object> getExtensions() {

        return extensions;
    }

    public boolean hasServerInfo() {

        return serverInfo != null;
    }

    public boolean hasExtensions() {

        return !extensions.isEmpty();
    }

    private static String requireText(
            String value,
            String fieldName) {

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

    @Override
    public String toString() {

        return "McpResultMetadata{" +
                "serverInfo=" + serverInfo +
                ", extensions=" + extensions +
                '}';
    }
}