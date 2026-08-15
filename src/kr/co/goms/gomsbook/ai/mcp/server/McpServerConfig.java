/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.co.goms.gomsbook.ai.mcp.protocol.McpProtocolVersion;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpServerInfo;

/**
 * Configuration for an MCP(Model Context Protocol) server.
 *
 * <p>
 * Contains server identity, supported protocol versions,
 * capabilities, instructions, and optional discovery
 * cache settings.
 * </p>
 */
public final class McpServerConfig {

    private static final String DEFAULT_SERVER_NAME =
            "gomsbook-ai-mcp";

    private static final String DEFAULT_SERVER_VERSION =
            "1.0.0";

    private static final long DEFAULT_DISCOVERY_TTL_MS =
            3600000L;


    private McpServerInfo serverInfo;

    private List<String> supportedVersions;

    private McpServerCapabilities capabilities;

    private String instructions;

    private Long discoveryTtlMs;

    private String discoveryCacheScope;


    /**
     * Constructor for Gson deserialization.
     */
    public McpServerConfig() {

        this.serverInfo =
                createDefaultServerInfo();

        this.supportedVersions =
                new ArrayList<>();

        this.supportedVersions.add(
                McpProtocolVersion.CURRENT
        );

        this.capabilities = McpServerCapabilities.builder().build();

        this.discoveryTtlMs =
                Long.valueOf(
                        DEFAULT_DISCOVERY_TTL_MS
                );

        this.discoveryCacheScope =
                McpServerDiscoveryResult
                        .CACHE_SCOPE_PUBLIC;
    }


    private McpServerConfig(
            Builder builder) {

        this.serverInfo =
                builder.serverInfo == null
                        ? createDefaultServerInfo()
                        : builder.serverInfo;

        this.supportedVersions =
                normalizeVersions(
                        builder.supportedVersions
                );

        this.capabilities =
                builder.capabilities == null
                        ? McpServerCapabilities.builder().build()
                        : builder.capabilities;

        this.instructions =
                normalizeOptional(
                        builder.instructions
                );

        this.discoveryTtlMs =
                builder.discoveryTtlMs;

        this.discoveryCacheScope =
                normalizeOptional(
                        builder.discoveryCacheScope
                );

        validate();
    }


    public static Builder builder() {
        return new Builder();
    }


    public static McpServerConfig defaultConfig() {
        return builder()
                .build();
    }


    public McpServerInfo getServerInfo() {
        return serverInfo;
    }


    public String getServerName() {

        return serverInfo == null
                ? null
                : serverInfo.getName();
    }


    public String getServerVersion() {

        return serverInfo == null
                ? null
                : serverInfo.getVersion();
    }


    /**
     * Returns the preferred/current protocol version.
     *
     * <p>
     * Kept for compatibility with existing code.
     * </p>
     */
    public String getProtocolVersion() {

        if (supportedVersions == null
                || supportedVersions.isEmpty()) {

            return null;
        }

        return supportedVersions.get(
                0
        );
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


    public String getInstructions() {

        return instructions;
    }


    public Long getDiscoveryTtlMs() {

        return discoveryTtlMs;
    }


    public String getDiscoveryCacheScope() {

        return discoveryCacheScope;
    }


    public boolean hasInstructions() {

        return instructions != null
                && !instructions.isBlank();
    }


    public boolean hasDiscoveryTtl() {

        return discoveryTtlMs != null;
    }


    public boolean hasDiscoveryCacheScope() {

        return discoveryCacheScope != null
                && !discoveryCacheScope.isBlank();
    }


    /**
     * Creates the server/discover result represented
     * by this configuration.
     */
    public McpServerDiscoveryResult
            createDiscoveryResult() {

        McpServerDiscoveryResult.Builder builder =
                McpServerDiscoveryResult.builder()
                        .supportedVersions(
                                supportedVersions
                        )
                        .capabilities(
                                capabilities
                        )
                        .serverInfo(
                                serverInfo
                        )
                        .instructions(
                                instructions
                        );

        if (discoveryTtlMs != null) {

            builder.ttlMs(
                    discoveryTtlMs.longValue()
            );
        }

        if (discoveryCacheScope != null) {

            builder.cacheScope(
                    discoveryCacheScope
            );
        }

        return builder.build();
    }


    /**
     * Validates this server configuration.
     */
    public void validate() {

        if (serverInfo == null) {

            throw new IllegalArgumentException(
                    "MCP serverInfo must not be null."
            );
        }

        if (supportedVersions == null
                || supportedVersions.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP supported protocol versions "
                            + "must not be empty."
            );
        }

        for (String version
                : supportedVersions) {

            if (version == null
                    || version.isBlank()) {

                throw new IllegalArgumentException(
                        "MCP supported protocol version "
                                + "must not be blank."
                );
            }

            if (!McpProtocolVersion.isSupported(
                    version)) {

                throw new IllegalArgumentException(
                        "Unsupported MCP protocol version: "
                                + version
                );
            }
        }


        if (capabilities == null) {

            throw new IllegalArgumentException(
                    "MCP server capabilities "
                            + "must not be null."
            );
        }

        if (discoveryTtlMs != null
                && discoveryTtlMs.longValue() < 0L) {

            throw new IllegalArgumentException(
                    "MCP discovery ttlMs "
                            + "must not be negative."
            );
        }


        if (discoveryCacheScope != null
                && !McpServerDiscoveryResult
                        .CACHE_SCOPE_PUBLIC
                        .equals(
                                discoveryCacheScope
                        )
                && !McpServerDiscoveryResult
                        .CACHE_SCOPE_PRIVATE
                        .equals(
                                discoveryCacheScope
                        )) {

            throw new IllegalArgumentException(
                    "Unsupported MCP discovery "
                            + "cacheScope: "
                            + discoveryCacheScope
            );
        }
    }


    private static McpServerInfo
            createDefaultServerInfo() {

        return McpServerInfo.builder()
                .name(
                        DEFAULT_SERVER_NAME
                )
                .version(
                        DEFAULT_SERVER_VERSION
                )
                .title(
                        "GomsBook AI MCP"
                )
                .description(
                        "GomsBook AI MCP Server"
                )
                .build();
    }


    private static List<String> normalizeVersions(
            List<String> versions) {

        List<String> result =
                new ArrayList<>();

        if (versions == null
                || versions.isEmpty()) {

            result.add(
                    McpProtocolVersion.CURRENT
            );

            return result;
        }

        for (String version
                : versions) {

            if (version == null
                    || version.isBlank()) {

                continue;
            }

            String normalized =
                    version.trim();

            if (!result.contains(
                    normalized)) {

                result.add(
                        normalized
                );
            }
        }

        if (result.isEmpty()) {

            result.add(
                    McpProtocolVersion.CURRENT
            );
        }

        return result;
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

    public boolean supportsVersion(
            String protocolVersion
    ) {

        if (protocolVersion == null
                || protocolVersion.isBlank()) {

            return false;
        }

        return supportedVersions.contains(
                protocolVersion.trim()
        );
    }

    @Override
    public String toString() {

        return "McpServerConfig{"
                + "serverInfo="
                + serverInfo
                + ", supportedVersions="
                + supportedVersions
                + ", capabilities="
                + capabilities
                + ", instructions='"
                + instructions
                + '\''
                + ", discoveryTtlMs="
                + discoveryTtlMs
                + ", discoveryCacheScope='"
                + discoveryCacheScope
                + '\''
                + '}';
    }


    public static final class Builder {

        private McpServerInfo serverInfo = createDefaultServerInfo();

        private List<String> supportedVersions =
                new ArrayList<>(
                        Collections.singletonList(
                                McpProtocolVersion.CURRENT
                        )
                );

        private McpServerCapabilities capabilities =  McpServerCapabilities.builder().build();

        private String instructions;

        private Long discoveryTtlMs =
                Long.valueOf(
                        DEFAULT_DISCOVERY_TTL_MS
                );

        private String discoveryCacheScope =
                McpServerDiscoveryResult
                        .CACHE_SCOPE_PUBLIC;


        private Builder() {
        }


        public Builder serverInfo(
        		McpServerInfo serverInfo) {

            this.serverInfo =
                    serverInfo;

            return this;
        }


        /**
         * Compatibility helper.
         */
        public Builder serverName(
                String serverName) {

        	McpServerInfo current =
                    serverInfo == null
                            ? createDefaultServerInfo()
                            : serverInfo;

            this.serverInfo =
            		McpServerInfo.builder()
                            .name(
                                    serverName
                            )
                            .version(
                                    current.getVersion()
                            )
                            .title(
                                    current.getTitle()
                            )
                            .description(
                                    current.getDescription()
                            )
                            .websiteUrl(
                                    current.getWebsiteUrl()
                            )
                            .build();

            return this;
        }


        /**
         * Compatibility helper.
         */
        public Builder serverVersion(
                String serverVersion) {

        	McpServerInfo current =
                    serverInfo == null
                            ? createDefaultServerInfo()
                            : serverInfo;

            this.serverInfo =
            		McpServerInfo.builder()
                            .name(
                                    current.getName()
                            )
                            .version(
                                    serverVersion
                            )
                            .title(
                                    current.getTitle()
                            )
                            .description(
                                    current.getDescription()
                            )
                            .websiteUrl(
                                    current.getWebsiteUrl()
                            )
                            .build();

            return this;
        }


        /**
         * Compatibility helper.
         *
         * <p>
         * Replaces supportedVersions with a single
         * preferred protocol version.
         * </p>
         */
        public Builder protocolVersion(
                String protocolVersion) {

            this.supportedVersions =
                    new ArrayList<>();

            if (protocolVersion != null) {

                this.supportedVersions.add(
                        protocolVersion
                );
            }

            return this;
        }


        public Builder supportedVersion(
                String protocolVersion) {

            if (protocolVersion == null
                    || protocolVersion.isBlank()) {

                return this;
            }

            if (this.supportedVersions == null) {

                this.supportedVersions =
                        new ArrayList<>();
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
                    supportedVersions == null
                            ? new ArrayList<>()
                            : new ArrayList<>(
                                    supportedVersions
                            );

            return this;
        }


        public Builder capabilities(
                McpServerCapabilities capabilities) {

            this.capabilities =
                    capabilities;

            return this;
        }


        public Builder instructions(
                String instructions) {

            this.instructions =
                    instructions;

            return this;
        }


        public Builder discoveryTtlMs(
                long discoveryTtlMs) {

            this.discoveryTtlMs =
                    Long.valueOf(
                            discoveryTtlMs
                    );

            return this;
        }


        public Builder noDiscoveryCache() {

            this.discoveryTtlMs =
                    null;

            this.discoveryCacheScope =
                    null;

            return this;
        }


        public Builder discoveryCacheScope(
                String discoveryCacheScope) {

            this.discoveryCacheScope =
                    discoveryCacheScope;

            return this;
        }


        public McpServerConfig build() {

            return new McpServerConfig(
                    this
            );
        }
    }
}