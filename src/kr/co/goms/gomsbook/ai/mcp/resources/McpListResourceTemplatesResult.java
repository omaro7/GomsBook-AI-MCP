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
 * Result returned by the MCP
 * {@code resources/templates/list} method.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This result contains the resource templates exposed by the
 * server and supports pagination and cache metadata.
 * </p>
 */
public final class McpListResourceTemplatesResult
        extends McpResult {

    public static final String CACHE_SCOPE_PUBLIC =
            "public";

    public static final String CACHE_SCOPE_PRIVATE =
            "private";

    public static final long DEFAULT_TTL_MS =
            3_600_000L;


    private final List<McpResourceTemplate> resourceTemplates;

    private final String nextCursor;

    private final long ttlMs;

    private final String cacheScope;


    private McpListResourceTemplatesResult(
            Builder builder
    ) {

        super(
                McpResultType.COMPLETE,
                builder.serverInfo,
                builder.additionalMetadata
        );

        this.resourceTemplates =
                immutableResourceTemplates(
                        builder.resourceTemplates
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


    public static McpListResourceTemplatesResult of(
            List<McpResourceTemplate> resourceTemplates
    ) {

        return builder()
                .resourceTemplates(
                        resourceTemplates
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public List<McpResourceTemplate> getResourceTemplates() {
        return resourceTemplates;
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

    public boolean hasResourceTemplates() {
        return !resourceTemplates.isEmpty();
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

        private final List<McpResourceTemplate> resourceTemplates =
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


        public Builder resourceTemplate(
                McpResourceTemplate resourceTemplate
        ) {

            if (resourceTemplate != null) {

                resourceTemplates.add(
                        resourceTemplate
                );
            }

            return this;
        }


        public Builder resourceTemplates(
                List<McpResourceTemplate> resourceTemplates
        ) {

            this.resourceTemplates.clear();

            if (resourceTemplates == null
                    || resourceTemplates.isEmpty()) {

                return this;
            }

            for (McpResourceTemplate resourceTemplate
                    : resourceTemplates) {

                if (resourceTemplate != null) {

                    this.resourceTemplates.add(
                            resourceTemplate
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


        public McpListResourceTemplatesResult build() {

            return new McpListResourceTemplatesResult(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static List<McpResourceTemplate> immutableResourceTemplates(
            List<McpResourceTemplate> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyList();
        }

        List<McpResourceTemplate> copy =
                new ArrayList<>();

        for (McpResourceTemplate resourceTemplate : source) {

            if (resourceTemplate != null) {

                copy.add(
                        resourceTemplate
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
                    "MCP resources/templates/list ttlMs "
                            + "must not be negative: "
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
                "MCP resources/templates/list cacheScope "
                        + "must not be null."
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
                "Unsupported MCP resources/templates/list "
                        + "cacheScope: "
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

        if (!(object instanceof McpListResourceTemplatesResult)) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        McpListResourceTemplatesResult other =
                (McpListResourceTemplatesResult) object;

        return ttlMs == other.ttlMs
                && Objects.equals(
                        resourceTemplates,
                        other.resourceTemplates
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
                resourceTemplates,
                nextCursor,
                ttlMs,
                cacheScope
        );
    }


    @Override
    public String toString() {

        return "McpListResourceTemplatesResult{"
                + "resultType="
                + getResultType()
                + ", resourceTemplates="
                + resourceTemplates
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