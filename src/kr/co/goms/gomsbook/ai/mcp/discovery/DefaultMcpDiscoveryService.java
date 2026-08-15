/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.protocol.McpProtocolVersion;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpServerInfo;
import kr.co.goms.gomsbook.ai.mcp.server.McpServerCapabilities;

/**
 * Default implementation of {@link McpDiscoveryService}.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This service is stateless. Discovery information is configured
 * when the service is created and a new {@link McpDiscoverResult}
 * is produced for every {@code server/discover} request.
 * </p>
 */
public final class DefaultMcpDiscoveryService
        implements McpDiscoveryService {

    /**
     * Default discovery cache lifetime.
     *
     * <p>
     * One hour.
     * </p>
     */
    public static final long DEFAULT_TTL_MS =
            3_600_000L;

    private final McpServerInfo serverInfo;

    private final McpServerCapabilities capabilities;

    private final List<String> supportedVersions;

    private final String instructions;

    private final long ttlMs;

    private final String cacheScope;


    private DefaultMcpDiscoveryService(
            Builder builder
    ) {

        this.serverInfo =
                Objects.requireNonNull(
                        builder.serverInfo,
                        "MCP server info must not be null."
                );

        this.capabilities =
                Objects.requireNonNull(
                        builder.capabilities,
                        "MCP server capabilities must not be null."
                );

        this.supportedVersions =
                immutableVersions(
                        builder.supportedVersions
                );

        this.instructions =
                normalizeOptional(
                        builder.instructions
                );

        this.ttlMs =
                requireTtlMs(
                        builder.ttlMs
                );

        this.cacheScope =
                requireCacheScope(
                        builder.cacheScope
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
     * Creates a discovery service for the current MCP protocol
     * version using default cache settings.
     *
     * @param serverInfo server information
     * @param capabilities server capabilities
     * @return discovery service
     */
    public static DefaultMcpDiscoveryService create(
            McpServerInfo serverInfo,
            McpServerCapabilities capabilities
    ) {

        return builder()
                .serverInfo(
                        serverInfo
                )
                .capabilities(
                        capabilities
                )
                .supportedVersion(
                        McpProtocolVersion.CURRENT
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * McpDiscoveryService
     * ------------------------------------------------------------
     */

    @Override
    public McpDiscoverResult discover(
            McpDiscoverParams params
    ) {

        Objects.requireNonNull(
                params,
                "MCP discover params must not be null."
        );

        return McpDiscoverResult.builder()
                .supportedVersions(
                        supportedVersions
                )
                .capabilities(
                        capabilities
                )
                .instructions(
                        instructions
                )
                .ttlMs(
                        ttlMs
                )
                .cacheScope(
                        cacheScope
                )
                .serverInfo(
                        serverInfo
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public McpServerInfo getServerInfo() {
        return serverInfo;
    }

    public McpServerCapabilities getCapabilities() {
        return capabilities;
    }

    public List<String> getSupportedVersions() {
        return supportedVersions;
    }

    public String getInstructions() {
        return instructions;
    }

    public long getTtlMs() {
        return ttlMs;
    }

    public String getCacheScope() {
        return cacheScope;
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private McpServerInfo serverInfo;

        private McpServerCapabilities capabilities =
                McpServerCapabilities.empty();

        private final List<String> supportedVersions =
                new ArrayList<>();

        private String instructions;

        private long ttlMs =
                DEFAULT_TTL_MS;

        private String cacheScope =
                McpDiscoverResult.CACHE_SCOPE_PUBLIC;


        private Builder() {

            supportedVersions.add(
                    McpProtocolVersion.CURRENT
            );
        }


        public Builder serverInfo(
                McpServerInfo serverInfo
        ) {

            this.serverInfo =
                    serverInfo;

            return this;
        }


        public Builder capabilities(
                McpServerCapabilities capabilities
        ) {

            this.capabilities =
                    capabilities;

            return this;
        }


        public Builder supportedVersion(
                String version
        ) {

            String normalized =
                    requireVersion(
                            version
                    );

            if (!supportedVersions.contains(
                    normalized
            )) {

                supportedVersions.add(
                        normalized
                );
            }

            return this;
        }


        public Builder supportedVersions(
                List<String> versions
        ) {

            supportedVersions.clear();

            if (versions == null
                    || versions.isEmpty()) {

                supportedVersions.add(
                        McpProtocolVersion.CURRENT
                );

                return this;
            }

            for (String version : versions) {

                supportedVersion(
                        version
                );
            }

            return this;
        }


        public Builder instructions(
                String instructions
        ) {

            this.instructions =
                    instructions;

            return this;
        }


        public Builder ttlMs(
                long ttlMs
        ) {

            this.ttlMs =
                    ttlMs;

            return this;
        }


        public Builder cacheScope(
                String cacheScope
        ) {

            this.cacheScope =
                    cacheScope;

            return this;
        }


        public Builder publicCache() {

            this.cacheScope =
                    McpDiscoverResult.CACHE_SCOPE_PUBLIC;

            return this;
        }


        public Builder privateCache() {

            this.cacheScope =
                    McpDiscoverResult.CACHE_SCOPE_PRIVATE;

            return this;
        }


        public DefaultMcpDiscoveryService build() {

            return new DefaultMcpDiscoveryService(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static List<String> immutableVersions(
            List<String> source
    ) {

        if (source == null
                || source.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP discovery service must support at least "
                            + "one protocol version."
            );
        }

        List<String> copy =
                new ArrayList<>();

        for (String version : source) {

            String normalized =
                    requireVersion(
                            version
                    );

            if (!copy.contains(
                    normalized
            )) {

                copy.add(
                        normalized
                );
            }
        }

        return Collections.unmodifiableList(
                copy
        );
    }


    private static String requireVersion(
            String version
    ) {

        Objects.requireNonNull(
                version,
                "MCP protocol version must not be null."
        );

        String normalized =
                version.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP protocol version must not be blank."
            );
        }

        return normalized;
    }


    private static long requireTtlMs(
            long ttlMs
    ) {

        if (ttlMs < 0L) {

            throw new IllegalArgumentException(
                    "MCP discovery ttlMs must not be negative: "
                            + ttlMs
            );
        }

        return ttlMs;
    }


    private static String requireCacheScope(
            String cacheScope
    ) {

        Objects.requireNonNull(
                cacheScope,
                "MCP discovery cacheScope must not be null."
        );

        String normalized =
                cacheScope.trim();

        if (McpDiscoverResult.CACHE_SCOPE_PUBLIC.equals(
                normalized
        )) {

            return McpDiscoverResult.CACHE_SCOPE_PUBLIC;
        }

        if (McpDiscoverResult.CACHE_SCOPE_PRIVATE.equals(
                normalized
        )) {

            return McpDiscoverResult.CACHE_SCOPE_PRIVATE;
        }

        throw new IllegalArgumentException(
                "Unsupported MCP discovery cacheScope: "
                        + normalized
        );
    }


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


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "DefaultMcpDiscoveryService{"
                + "serverInfo="
                + serverInfo
                + ", capabilities="
                + capabilities
                + ", supportedVersions="
                + supportedVersions
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
}