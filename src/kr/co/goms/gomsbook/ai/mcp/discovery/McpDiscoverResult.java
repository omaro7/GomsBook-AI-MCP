/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.core.McpResultType;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpProtocolVersion;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpServerInfo;
import kr.co.goms.gomsbook.ai.mcp.server.McpServerCapabilities;

/**
 * Result returned by the MCP {@code server/discover} method.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * A discover result advertises:
 * </p>
 *
 * <ul>
 *     <li>supported protocol versions</li>
 *     <li>server capabilities</li>
 *     <li>optional server instructions</li>
 *     <li>cache lifetime</li>
 *     <li>cache scope</li>
 * </ul>
 */
public final class McpDiscoverResult
        extends McpResult {

    /**
     * Public/shared caching is allowed.
     */
    public static final String CACHE_SCOPE_PUBLIC =
            "public";

    /**
     * The result is specific to the requesting client/context.
     */
    public static final String CACHE_SCOPE_PRIVATE =
            "private";


    private final List<String> supportedVersions;

    private final McpServerCapabilities capabilities;

    private final String instructions;

    private final long ttlMs;

    private final String cacheScope;


    private McpDiscoverResult(
            Builder builder
    ) {

        super(
                McpResultType.COMPLETE,
                builder.serverInfo,
                builder.additionalMetadata
        );

        this.supportedVersions =
                immutableVersions(
                        builder.supportedVersions
                );

        this.capabilities =
                Objects.requireNonNull(
                        builder.capabilities,
                        "MCP server capabilities must not be null."
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
     * Creates a discover result for the current protocol version.
     *
     * @param capabilities server capabilities
     * @param serverInfo server implementation information
     * @return discover result
     */
    public static McpDiscoverResult create(
            McpServerCapabilities capabilities,
            McpServerInfo serverInfo
    ) {

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


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public List<String> getSupportedVersions() {
        return supportedVersions;
    }

    public McpServerCapabilities getCapabilities() {
        return capabilities;
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
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasInstructions() {
        return instructions != null;
    }

    public boolean supportsVersion(
            String version
    ) {

        if (version == null) {
            return false;
        }

        return supportedVersions.contains(
                version.trim()
        );
    }

    public boolean isPublicCache() {

        return CACHE_SCOPE_PUBLIC.equals(
                cacheScope
        );
    }

    public boolean isPrivateCache() {

        return CACHE_SCOPE_PRIVATE.equals(
                cacheScope
        );
    }

    public boolean isImmediatelyStale() {
        return ttlMs == 0L;
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private final List<String> supportedVersions =
                new ArrayList<>();

        private McpServerCapabilities capabilities;

        private String instructions;

        /*
         * A reasonable default for server discovery.
         *
         * 1 hour.
         */
        private long ttlMs =
                3_600_000L;

        private String cacheScope =
                CACHE_SCOPE_PUBLIC;

        private McpServerInfo serverInfo;

        private Map<String, Object> additionalMetadata =
                Collections.emptyMap();


        private Builder() {
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

            if (versions == null) {
                return this;
            }

            for (String version : versions) {

                supportedVersion(
                        version
                );
            }

            return this;
        }


        public Builder capabilities(
                McpServerCapabilities capabilities
        ) {

            this.capabilities =
                    capabilities;

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
                    CACHE_SCOPE_PUBLIC;

            return this;
        }


        public Builder privateCache() {

            this.cacheScope =
                    CACHE_SCOPE_PRIVATE;

            return this;
        }


        public Builder serverInfo(
                McpServerInfo serverInfo
        ) {

            this.serverInfo =
                    serverInfo;

            return this;
        }


        public Builder additionalMetadata(
                Map<String, Object> additionalMetadata
        ) {

            this.additionalMetadata =
                    additionalMetadata == null
                            ? Collections.emptyMap()
                            : additionalMetadata;

            return this;
        }


        public McpDiscoverResult build() {

            return new McpDiscoverResult(
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
                    "MCP discover result must contain at least "
                            + "one supported protocol version."
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
                    "MCP ttlMs must not be negative: "
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
                "MCP cacheScope must not be null."
        );

        String normalized =
                cacheScope.trim();

        if (CACHE_SCOPE_PUBLIC.equals(
                normalized
        )) {

            return CACHE_SCOPE_PUBLIC;
        }

        if (CACHE_SCOPE_PRIVATE.equals(
                normalized
        )) {

            return CACHE_SCOPE_PRIVATE;
        }

        throw new IllegalArgumentException(
                "Unsupported MCP cacheScope: "
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
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpDiscoverResult)) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        McpDiscoverResult other =
                (McpDiscoverResult) object;

        return ttlMs == other.ttlMs
                && Objects.equals(
                        supportedVersions,
                        other.supportedVersions
                )
                && Objects.equals(
                        capabilities,
                        other.capabilities
                )
                && Objects.equals(
                        instructions,
                        other.instructions
                )
                && Objects.equals(
                        cacheScope,
                        other.cacheScope
                );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                super.hashCode(),
                supportedVersions,
                capabilities,
                instructions,
                ttlMs,
                cacheScope
        );
    }


    @Override
    public String toString() {

        return "McpDiscoverResult{"
                + "resultType="
                + getResultType()
                + ", supportedVersions="
                + supportedVersions
                + ", capabilities="
                + capabilities
                + ", instructions='"
                + instructions
                + '\''
                + ", ttlMs="
                + ttlMs
                + ", cacheScope='"
                + cacheScope
                + '\''
                + ", serverInfo="
                + getServerInfo()
                + '}';
    }
}