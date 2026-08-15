/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.protocol.McpServerInfo;

/**
 * Base class for all MCP result objects.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Every MCP result MUST contain a {@code resultType}.
 * Result metadata is optional and is serialized as {@code _meta}.
 * </p>
 *
 * <p>
 * Standard result metadata may contain:
 * </p>
 *
 * <pre>
 * _meta["io.modelcontextprotocol/serverInfo"]
 * </pre>
 *
 * <p>
 * Servers SHOULD include server information on every response
 * unless explicitly configured not to do so.
 * </p>
 */
public abstract class McpResult {

    /**
     * Standard metadata key identifying the server implementation
     * that produced this result.
     */
    public static final String KEY_SERVER_INFO =
            "io.modelcontextprotocol/serverInfo";

    private final McpResultType resultType;

    private final McpServerInfo serverInfo;

    private final Map<String, Object> additionalMetadata;

    /**
     * Creates an MCP result.
     *
     * @param resultType result type
     * @param serverInfo server implementation information
     * @param additionalMetadata additional result metadata
     */
    protected McpResult(
            McpResultType resultType,
            McpServerInfo serverInfo,
            Map<String, Object> additionalMetadata
    ) {

        this.resultType =
                Objects.requireNonNull(
                        resultType,
                        "MCP result type must not be null."
                );

        this.serverInfo =
                serverInfo;

        this.additionalMetadata =
                immutableMetadata(
                        additionalMetadata
                );
    }

    /**
     * Convenience constructor for a result without metadata.
     *
     * @param resultType result type
     */
    protected McpResult(
            McpResultType resultType
    ) {

        this(
                resultType,
                null,
                Collections.emptyMap()
        );
    }

    /**
     * Convenience constructor for a result containing server info.
     *
     * @param resultType result type
     * @param serverInfo server information
     */
    protected McpResult(
            McpResultType resultType,
            McpServerInfo serverInfo
    ) {

        this(
                resultType,
                serverInfo,
                Collections.emptyMap()
        );
    }

    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    /**
     * Returns the MCP result type.
     *
     * @return result type
     */
    public final McpResultType getResultType() {
        return resultType;
    }

    /**
     * Returns the server information attached to this result.
     *
     * @return server information or {@code null}
     */
    public McpServerInfo getServerInfo() {
        return serverInfo;
    }

    /**
     * Returns additional result metadata.
     *
     * @return immutable metadata
     */
    public final Map<String, Object> getAdditionalMetadata() {
        return additionalMetadata;
    }

    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public final boolean isComplete() {
        return resultType.isComplete();
    }

    public final boolean isInputRequired() {
        return resultType.isInputRequired();
    }

    public boolean hasServerInfo() {
        return serverInfo != null;
    }

    public final boolean hasAdditionalMetadata() {
        return !additionalMetadata.isEmpty();
    }

    public final boolean hasMetadata(
            String key
    ) {

        if (key == null) {
            return false;
        }

        if (KEY_SERVER_INFO.equals(key)) {
            return serverInfo != null;
        }

        return additionalMetadata.containsKey(
                key
        );
    }

    /**
     * Returns a metadata value.
     *
     * @param key metadata key
     * @return metadata value or {@code null}
     */
    public final Object getMetadata(
            String key
    ) {

        if (key == null) {
            return null;
        }

        if (KEY_SERVER_INFO.equals(key)) {
            return serverInfo;
        }

        return additionalMetadata.get(
                key
        );
    }

    /*
     * ------------------------------------------------------------
     * Metadata conversion
     * ------------------------------------------------------------
     */

    /**
     * Creates the MCP {@code _meta} representation.
     *
     * <p>
     * An empty map is returned when this result does not carry
     * metadata.
     * </p>
     *
     * @return immutable metadata map
     */
    public final Map<String, Object> metadataAsMap() {

        if (serverInfo == null
                && additionalMetadata.isEmpty()) {

            return Collections.emptyMap();
        }

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        if (serverInfo != null) {

            metadata.put(
                    KEY_SERVER_INFO,
                    serverInfo
            );
        }

        metadata.putAll(
                additionalMetadata
        );

        return Collections.unmodifiableMap(
                metadata
        );
    }

    /*
     * ------------------------------------------------------------
     * Metadata validation
     * ------------------------------------------------------------
     */

    /**
     * Validates and copies additional metadata.
     *
     * @param source metadata
     * @return immutable metadata
     */
    private static Map<String, Object> immutableMetadata(
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

            String key =
                    requireMetadataKey(
                            entry.getKey()
                    );

            if (KEY_SERVER_INFO.equals(key)) {

                throw new IllegalArgumentException(
                        "Reserved MCP result metadata key cannot "
                                + "be set through additional metadata: "
                                + KEY_SERVER_INFO
                );
            }

            if (entry.getValue() != null) {

                copy.put(
                        key,
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

    private static String requireMetadataKey(
            String key
    ) {

        Objects.requireNonNull(
                key,
                "MCP result metadata key must not be null."
        );

        String normalized =
                key.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP result metadata key must not be blank."
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
    public int hashCode() {

        return Objects.hash(
                resultType,
                serverInfo,
                additionalMetadata
        );
    }

    @Override
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpResult)) {
            return false;
        }

        McpResult other =
                (McpResult) object;

        return Objects.equals(
                resultType,
                other.resultType
        )
                && Objects.equals(
                        serverInfo,
                        other.serverInfo
                )
                && Objects.equals(
                        additionalMetadata,
                        other.additionalMetadata
                );
    }

    @Override
    public String toString() {

        return "McpResult{"
                + "resultType="
                + resultType
                + ", serverInfo="
                + serverInfo
                + ", additionalMetadata="
                + additionalMetadata
                + '}';
    }
}