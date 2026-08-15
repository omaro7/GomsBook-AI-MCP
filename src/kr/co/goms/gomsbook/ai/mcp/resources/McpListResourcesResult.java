/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.core.McpResultType;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpServerInfo;

/**
 * Result returned by the MCP {@code resources/list} method.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This result contains the resources exposed by the server and
 * supports pagination and cache metadata.
 * </p>
 */
public final class McpListResourcesResult
        extends McpResult {

    /**
     * Public/shared caching is allowed.
     */
    public static final String CACHE_SCOPE_PUBLIC =
            "public";

    /**
     * The result must only be reused within the same
     * authorization context.
     */
    public static final String CACHE_SCOPE_PRIVATE =
            "private";

    /**
     * Default cache lifetime.
     *
     * <p>
     * One hour.
     * </p>
     */
    public static final long DEFAULT_TTL_MS =
            3_600_000L;


    private final List<McpResource> resources;

    private final String nextCursor;

    private final long ttlMs;

    private final String cacheScope;


    private McpListResourcesResult(
            Builder builder
    ) {

        super(
                McpResultType.COMPLETE,
                builder.serverInfo,
                builder.additionalMetadata
        );

        this.resources =
                immutableResources(
                        builder.resources
                );

        this.nextCursor =
                normalizeOptional(
                        builder.nextCursor
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
     * Creates a non-paginated resource list result.
     *
     * @param resources resources
     * @return resource list result
     */
    public static McpListResourcesResult of(
            List<McpResource> resources
    ) {

        return builder()
                .resources(
                        resources
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public List<McpResource> getResources() {
        return resources;
    }


    public String getNextCursor() {
        return nextCursor;
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

    public boolean hasResources() {
        return !resources.isEmpty();
    }


    public boolean hasNextCursor() {
        return nextCursor != null;
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

        private final List<McpResource> resources =
                new ArrayList<>();

        private String nextCursor;

        private long ttlMs =
                DEFAULT_TTL_MS;

        private String cacheScope =
                CACHE_SCOPE_PUBLIC;

        private McpServerInfo serverInfo;

        private final Map<String, Object> additionalMetadata =
                new LinkedHashMap<>();


        private Builder() {
        }


        public Builder resource(
                McpResource resource
        ) {

            if (resource != null) {

                resources.add(
                        resource
                );
            }

            return this;
        }


        public Builder resources(
                List<McpResource> resources
        ) {

            this.resources.clear();

            if (resources == null
                    || resources.isEmpty()) {

                return this;
            }

            for (McpResource resource : resources) {

                if (resource != null) {

                    this.resources.add(
                            resource
                    );
                }
            }

            return this;
        }


        public Builder nextCursor(
                String nextCursor
        ) {

            this.nextCursor =
                    nextCursor;

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


        public Builder metadata(
                String key,
                Object value
        ) {

            String normalizedKey =
                    requireMetadataKey(
                            key
                    );

            if (McpResult.KEY_SERVER_INFO.equals(
                    normalizedKey
            )) {

                throw new IllegalArgumentException(
                        "Reserved MCP metadata key cannot be "
                                + "set through additional metadata: "
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


        public McpListResourcesResult build() {

            return new McpListResourcesResult(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static List<McpResource> immutableResources(
            List<McpResource> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyList();
        }

        List<McpResource> copy =
                new ArrayList<>();

        for (McpResource resource : source) {

            if (resource != null) {

                copy.add(
                        resource
                );
            }
        }

        if (copy.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                copy
        );
    }


    private static long requireTtlMs(
            long ttlMs
    ) {

        if (ttlMs < 0L) {

            throw new IllegalArgumentException(
                    "MCP resources/list ttlMs must not be negative: "
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
                "MCP resources/list cacheScope must not be null."
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
                "Unsupported MCP resources/list cacheScope: "
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
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpListResourcesResult)) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        McpListResourcesResult other =
                (McpListResourcesResult) object;

        return ttlMs == other.ttlMs
                && Objects.equals(
                        resources,
                        other.resources
                )
                && Objects.equals(
                        nextCursor,
                        other.nextCursor
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
                resources,
                nextCursor,
                ttlMs,
                cacheScope
        );
    }


    @Override
    public String toString() {

        return "McpListResourcesResult{"
                + "resultType="
                + getResultType()
                + ", resources="
                + resources
                + ", nextCursor='"
                + nextCursor
                + '\''
                + ", ttlMs="
                + ttlMs
                + ", cacheScope='"
                + cacheScope
                + '\''
                + '}';
    }
}