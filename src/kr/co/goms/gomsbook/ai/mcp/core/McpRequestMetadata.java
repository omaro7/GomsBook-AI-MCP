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
import kr.co.goms.gomsbook.ai.mcp.protocol.McpProtocolVersion;

/**
 * Metadata attached to MCP request parameters.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * MCP 2026-07-28 is stateless. Every client-to-server request
 * carries protocol and client capability information through
 * the {@code _meta} object.
 * </p>
 *
 * <p>
 * This class validates only the structural validity of metadata.
 * Protocol-version support is validated by the server runtime.
 * </p>
 */
public final class McpRequestMetadata {

    /*
     * ------------------------------------------------------------
     * Standard MCP metadata keys
     * ------------------------------------------------------------
     */

    public static final String KEY_PROTOCOL_VERSION =
            "io.modelcontextprotocol/protocolVersion";

    public static final String KEY_CLIENT_CAPABILITIES =
            "io.modelcontextprotocol/clientCapabilities";

    public static final String KEY_CLIENT_INFO =
            "io.modelcontextprotocol/clientInfo";

    public static final String KEY_LOG_LEVEL =
            "io.modelcontextprotocol/logLevel";


    /*
     * ------------------------------------------------------------
     * Fields
     * ------------------------------------------------------------
     */

    private final String protocolVersion;

    private final McpClientCapabilities clientCapabilities;

    private final McpClientInfo clientInfo;

    private final String logLevel;

    private final Map<String, Object> additionalMetadata;


    private McpRequestMetadata(
            Builder builder
    ) {

        /*
         * IMPORTANT:
         *
         * Do NOT call McpProtocolVersion.requireSupported(...)
         * here.
         *
         * An unsupported protocol version must still be decoded
         * and preserved so DefaultMcpServerRuntime can return the
         * protocol-defined UnsupportedProtocolVersion error.
         */
        this.protocolVersion =
                requireText(
                        builder.protocolVersion,
                        "MCP protocol version"
                );

        this.clientCapabilities =
                Objects.requireNonNull(
                        builder.clientCapabilities,
                        "MCP client capabilities must not be null."
                );

        this.clientInfo =
                builder.clientInfo;

        this.logLevel =
                normalizeOptional(
                        builder.logLevel
                );

        this.additionalMetadata =
                immutableCopy(
                        builder.additionalMetadata
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


    /**
     * Creates minimum valid request metadata for the current
     * supported protocol version.
     *
     * @return request metadata
     */
    public static McpRequestMetadata create() {

        return builder()
                .protocolVersion(
                        McpProtocolVersion.CURRENT
                )
                .clientCapabilities(
                        McpClientCapabilities.empty()
                )
                .build();
    }


    /**
     * Creates minimum valid request metadata with client info.
     *
     * @param clientInfo client information
     * @return request metadata
     */
    public static McpRequestMetadata create(
            McpClientInfo clientInfo
    ) {

        return builder()
                .protocolVersion(
                        McpProtocolVersion.CURRENT
                )
                .clientCapabilities(
                        McpClientCapabilities.empty()
                )
                .clientInfo(
                        clientInfo
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public String getProtocolVersion() {
        return protocolVersion;
    }


    public McpClientCapabilities getClientCapabilities() {
        return clientCapabilities;
    }


    public McpClientInfo getClientInfo() {
        return clientInfo;
    }


    public String getLogLevel() {
        return logLevel;
    }


    public Map<String, Object> getAdditionalMetadata() {
        return additionalMetadata;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasClientInfo() {
        return clientInfo != null;
    }


    public boolean hasLogLevel() {
        return logLevel != null;
    }


    public boolean hasAdditionalMetadata() {
        return !additionalMetadata.isEmpty();
    }


    public boolean hasMetadata(
            String key
    ) {

        if (key == null) {
            return false;
        }

        return additionalMetadata.containsKey(
                key
        );
    }


    public Object getMetadata(
            String key
    ) {

        if (key == null) {
            return null;
        }

        return additionalMetadata.get(
                key
        );
    }


    /*
     * ------------------------------------------------------------
     * Protocol-version helpers
     * ------------------------------------------------------------
     */

    /**
     * Returns whether the protocol version contained in this
     * metadata is supported by the current implementation.
     *
     * <p>
     * This helper must not be used during JSON decoding to reject
     * a request. Runtime validation is responsible for producing
     * the proper MCP protocol error response.
     * </p>
     *
     * @return true if supported
     */
    public boolean isCurrentProtocolVersion() {

        return McpProtocolVersion.isSupported(
                protocolVersion
        );
    }


    /*
     * ------------------------------------------------------------
     * Conversion
     * ------------------------------------------------------------
     */

    /**
     * Converts this metadata object to the MCP wire-format
     * {@code _meta} representation.
     *
     * @return immutable metadata map
     */
    public Map<String, Object> toMap() {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                KEY_PROTOCOL_VERSION,
                protocolVersion
        );

        metadata.put(
                KEY_CLIENT_CAPABILITIES,
                clientCapabilities
        );

        if (clientInfo != null) {

            metadata.put(
                    KEY_CLIENT_INFO,
                    clientInfo
            );
        }

        if (logLevel != null) {

            metadata.put(
                    KEY_LOG_LEVEL,
                    logLevel
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
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private String protocolVersion =
                McpProtocolVersion.CURRENT;

        private McpClientCapabilities clientCapabilities =
                McpClientCapabilities.empty();

        private McpClientInfo clientInfo;

        private String logLevel;

        private final Map<String, Object> additionalMetadata =
                new LinkedHashMap<>();


        private Builder() {
        }


        public Builder protocolVersion(
                String protocolVersion
        ) {

            this.protocolVersion =
                    protocolVersion;

            return this;
        }


        public Builder clientCapabilities(
                McpClientCapabilities clientCapabilities
        ) {

            this.clientCapabilities =
                    clientCapabilities;

            return this;
        }


        public Builder clientInfo(
                McpClientInfo clientInfo
        ) {

            this.clientInfo =
                    clientInfo;

            return this;
        }


        public Builder logLevel(
                String logLevel
        ) {

            this.logLevel =
                    logLevel;

            return this;
        }


        /**
         * Adds custom request metadata.
         *
         * @param key metadata key
         * @param value metadata value
         * @return builder
         */
        public Builder metadata(
                String key,
                Object value
        ) {

            String normalizedKey =
                    requireMetadataKey(
                            key
                    );

            if (isReservedKey(
                    normalizedKey
            )) {

                throw new IllegalArgumentException(
                        "Reserved MCP metadata key cannot be "
                                + "set as custom metadata: "
                                + normalizedKey
                );
            }

            if (value == null) {

                additionalMetadata.remove(
                        normalizedKey
                );

            } else {

                additionalMetadata.put(
                        normalizedKey,
                        value
                );
            }

            return this;
        }


        public McpRequestMetadata build() {

            return new McpRequestMetadata(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Reserved metadata
     * ------------------------------------------------------------
     */

    public static boolean isReservedKey(
            String key
    ) {

        if (key == null) {
            return false;
        }

        return KEY_PROTOCOL_VERSION.equals(key)
                || KEY_CLIENT_CAPABILITIES.equals(key)
                || KEY_CLIENT_INFO.equals(key)
                || KEY_LOG_LEVEL.equals(key);
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static String requireText(
            String value,
            String fieldName
    ) {

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


    private static String requireMetadataKey(
            String key
    ) {

        String normalized =
                requireText(
                        key,
                        "MCP metadata key"
                );

        validateMetadataKey(
                normalized
        );

        return normalized;
    }


    /**
     * Performs basic MCP metadata-key validation.
     *
     * <p>
     * Unprefixed keys are permitted. Prefixed keys use a
     * namespace followed by {@code /}.
     * </p>
     *
     * @param key metadata key
     */
    private static void validateMetadataKey(
            String key
    ) {

        int separator =
                key.indexOf('/');

        /*
         * Unprefixed metadata key.
         */
        if (separator < 0) {
            return;
        }

        if (separator == 0
                || separator == key.length() - 1) {

            throw new IllegalArgumentException(
                    "Invalid MCP metadata key: "
                            + key
            );
        }

        String prefix =
                key.substring(
                        0,
                        separator
                );

        String name =
                key.substring(
                        separator + 1
                );

        if (name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Invalid MCP metadata key: "
                            + key
            );
        }

        String[] labels =
                prefix.split(
                        "\\."
                );

        for (String label : labels) {

            if (!isValidPrefixLabel(
                    label
            )) {

                throw new IllegalArgumentException(
                        "Invalid MCP metadata namespace: "
                                + key
                );
            }
        }
    }


    private static boolean isValidPrefixLabel(
            String label
    ) {

        if (label == null
                || label.isEmpty()) {

            return false;
        }

        char first =
                label.charAt(0);

        char last =
                label.charAt(
                        label.length() - 1
                );

        if (!Character.isLetter(first)) {
            return false;
        }

        if (!Character.isLetterOrDigit(last)) {
            return false;
        }

        for (int i = 1;
                i < label.length() - 1;
                i++) {

            char character =
                    label.charAt(i);

            if (!Character.isLetterOrDigit(character)
                    && character != '-') {

                return false;
            }
        }

        return true;
    }


    /*
     * ------------------------------------------------------------
     * Utility
     * ------------------------------------------------------------
     */

    private static String normalizeOptional(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }


    private static Map<String, Object> immutableCopy(
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

            if (isReservedKey(key)) {

                throw new IllegalArgumentException(
                        "Reserved MCP metadata key cannot be "
                                + "stored as additional metadata: "
                                + key
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

        if (!(object instanceof McpRequestMetadata)) {
            return false;
        }

        McpRequestMetadata other =
                (McpRequestMetadata) object;

        return Objects.equals(
                protocolVersion,
                other.protocolVersion
        )
                && Objects.equals(
                        clientCapabilities,
                        other.clientCapabilities
                )
                && Objects.equals(
                        clientInfo,
                        other.clientInfo
                )
                && Objects.equals(
                        logLevel,
                        other.logLevel
                )
                && Objects.equals(
                        additionalMetadata,
                        other.additionalMetadata
                );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                protocolVersion,
                clientCapabilities,
                clientInfo,
                logLevel,
                additionalMetadata
        );
    }


    @Override
    public String toString() {

        return "McpRequestMetadata{"
                + "protocolVersion='"
                + protocolVersion
                + '\''
                + ", clientCapabilities="
                + clientCapabilities
                + ", clientInfo="
                + clientInfo
                + ", logLevel='"
                + logLevel
                + '\''
                + ", additionalMetadata="
                + additionalMetadata
                + '}';
    }
}