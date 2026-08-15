/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.core.McpResultType;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpProtocolVersion;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpServerInfo;

/**
 * Result returned by the MCP server/discover method.
 *
 * <p>
 * Represents MCP server discovery information including
 * supported protocol versions, server capabilities,
 * server implementation information and optional
 * caching metadata.
 * </p>
 */
public final class McpServerDiscoveryResult extends McpResult {

    public static final String META_SERVER_INFO =
            "io.modelcontextprotocol/serverInfo";

    public static final String CACHE_SCOPE_PUBLIC =
            "public";

    public static final String CACHE_SCOPE_PRIVATE =
            "private";


    private McpResultType resultType;

    private List<String> supportedVersions;

    private McpServerCapabilities capabilities;

    private Map<String, Object> _meta;

    private String instructions;

    private Long ttlMs;

    private String cacheScope;


    /**
     * Constructor for Gson deserialization.
     */
    public McpServerDiscoveryResult() {

        super(
                McpResultType.COMPLETE
        );

        this.supportedVersions =
                new ArrayList<>();

        this.capabilities =
                McpServerCapabilities.empty();

        this.instructions =
                null;

        this.ttlMs =
                0L;

        this.cacheScope =
                null;
    }

    private McpServerDiscoveryResult(
            Builder builder) {

        super(
                McpResultType.COMPLETE,
                builder.serverInfo,
                builder.meta
        );

        this.supportedVersions =
                builder.supportedVersions == null
                        ? new ArrayList<>()
                        : new ArrayList<>(
                                builder.supportedVersions
                        );

        this.capabilities =
                builder.capabilities == null
                        ? McpServerCapabilities.empty()
                        : builder.capabilities;

        this.instructions =
                normalizeOptional(
                        builder.instructions
                );

        this.ttlMs =
                builder.ttlMs;

        this.cacheScope =
                normalizeOptional(
                        builder.cacheScope
                );
    }


    public static Builder builder() {

        return new Builder();
    }


    /**
     * Creates a default discovery result for the
     * current GomsBook MCP server.
     *
     * @param capabilities server capabilities
     * @param serverInfo server implementation information
     * @return discovery result
     */
    public static McpServerDiscoveryResult createDefault(
            McpServerCapabilities capabilities,
            McpServerInfo serverInfo) {

        return builder()
                .supportedVersion(
                        McpProtocolVersion.CURRENT
                )
                .capabilities(
                        capabilities
                )
                .serverInfo(
                        serverInfo
                )
                .build();
    }

    public List<String> getSupportedVersions() {

        if (supportedVersions == null) {

            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                supportedVersions
        );
    }


    public McpServerCapabilities getCapabilities() {

        return capabilities;
    }


    public Map<String, Object> getMeta() {

        if (_meta == null
                || _meta.isEmpty()) {

            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                _meta
        );
    }


    /**
     * Returns server implementation information
     * stored in discovery result metadata.
     *
     * @return server implementation or {@code null}
     */
    public McpServerInfo getServerInfo() {

        if (_meta == null) {

            return null;
        }

        Object value =
                _meta.get(
                        META_SERVER_INFO
                );

        if (value instanceof McpServerInfo) {

            return (McpServerInfo) value;
        }

        /*
         * After Gson generic deserialization the value
         * may be represented as a Map.
         *
         * Do not attempt conversion here because this
         * core model must remain independent from Gson.
         */
        return null;
    }


    public String getInstructions() {

        return instructions;
    }


    public Long getTtlMs() {

        return ttlMs;
    }


    public String getCacheScope() {

        return cacheScope;
    }


    public boolean hasInstructions() {

        return instructions != null
                && !instructions.isBlank();
    }


    public boolean hasServerInfo() {

        return _meta != null
                && _meta.containsKey(
                        META_SERVER_INFO
                );
    }


    public boolean hasTtl() {

        return ttlMs != null;
    }


    public boolean hasCacheScope() {

        return cacheScope != null
                && !cacheScope.isBlank();
    }

    private static String normalizeOptional(
            String value) {

        if (value == null) {

            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }


    @Override
    public String toString() {

        return "McpServerDiscoveryResult{"
                + "resultType="
                + resultType
                + ", supportedVersions="
                + supportedVersions
                + ", capabilities="
                + capabilities
                + ", _meta="
                + _meta
                + ", instructions='"
                + instructions
                + '\''
                + ", ttlMs="
                + ttlMs
                + ", cacheScope='"
                + cacheScope
                + '\''
                + '}';
    }


    /**
     * Builder for {@link McpServerDiscoveryResult}.
     */
    public static final class Builder {

        private McpResultType resultType = McpResultType.COMPLETE;

        private List<String> supportedVersions =
                new ArrayList<>();

        private McpServerCapabilities capabilities;

        private McpServerInfo serverInfo;

        private Map<String, Object> meta =
                new LinkedHashMap<>();

        private String instructions;

        private Long ttlMs;

        private String cacheScope;


        private Builder() {
        }


        public Builder resultType(
                McpResultType resultType) {

            this.resultType =
                    resultType;

            return this;
        }


        public Builder supportedVersion(
                String protocolVersion) {

            if (protocolVersion == null
                    || protocolVersion.isBlank()) {

                throw new IllegalArgumentException(
                        "MCP supported protocol version "
                                + "must not be blank."
                );
            }

            String normalized =
                    protocolVersion.trim();

            if (!this.supportedVersions.contains(
                    normalized)) {

                this.supportedVersions.add(
                        normalized
                );
            }

            return this;
        }


        public Builder supportedVersions(
                List<String> supportedVersions) {

            this.supportedVersions =
                    new ArrayList<>();

            if (supportedVersions == null) {

                return this;
            }

            for (String version
                    : supportedVersions) {

                if (version == null
                        || version.isBlank()) {

                    continue;
                }

                String normalized =
                        version.trim();

                if (!this.supportedVersions.contains(
                        normalized)) {

                    this.supportedVersions.add(
                            normalized
                    );
                }
            }

            return this;
        }


        public Builder capabilities(
                McpServerCapabilities capabilities) {

            this.capabilities =
                    capabilities;

            return this;
        }


        public Builder serverInfo(
        		McpServerInfo serverInfo) {

            this.serverInfo =
                    serverInfo;

            return this;
        }


        public Builder meta(
                String key,
                Object value) {

            if (key == null
                    || key.isBlank()) {

                throw new IllegalArgumentException(
                        "MCP discovery metadata key "
                                + "must not be blank."
                );
            }

            this.meta.put(
                    key.trim(),
                    value
            );

            return this;
        }


        public Builder meta(
                Map<String, Object> meta) {

            this.meta =
                    meta == null
                            ? new LinkedHashMap<>()
                            : new LinkedHashMap<>(
                                    meta
                            );

            return this;
        }


        public Builder instructions(
                String instructions) {

            this.instructions =
                    instructions;

            return this;
        }


        public Builder ttlMs(
                long ttlMs) {

            this.ttlMs =
                    Long.valueOf(
                            ttlMs
                    );

            return this;
        }


        public Builder cacheScope(
                String cacheScope) {

            this.cacheScope =
                    cacheScope;

            return this;
        }


        public McpServerDiscoveryResult build() {

            return new McpServerDiscoveryResult(
                    this
            );
        }
    }
}