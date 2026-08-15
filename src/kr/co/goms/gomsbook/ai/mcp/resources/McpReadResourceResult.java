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
 * Result returned by the MCP {@code resources/read} method.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * A resource read result contains one or more
 * {@link McpResourceContents} objects and cache metadata.
 * </p>
 */
public final class McpReadResourceResult
        extends McpResult {

    public static final String CACHE_SCOPE_PUBLIC =
            "public";

    public static final String CACHE_SCOPE_PRIVATE =
            "private";

    public static final long DEFAULT_TTL_MS =
            3_600_000L;


    private final List<McpResourceContents> contents;

    private final long ttlMs;

    private final String cacheScope;


    private McpReadResourceResult(
            Builder builder
    ) {

        super(
                McpResultType.COMPLETE,
                builder.serverInfo,
                builder.additionalMetadata
        );

        this.contents =
                immutableContents(
                        builder.contents
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


    public static McpReadResourceResult of(
            List<McpResourceContents> contents
    ) {

        return builder()
                .contents(
                        contents
                )
                .build();
    }


    public static McpReadResourceResult of(
            McpResourceContents content
    ) {

        return builder()
                .content(
                        content
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public List<McpResourceContents> getContents() {
        return contents;
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

    public boolean hasContents() {
        return !contents.isEmpty();
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

        private final List<McpResourceContents> contents =
                new ArrayList<>();

        private long ttlMs =
                DEFAULT_TTL_MS;

        /*
         * Resource reads are commonly authorization / project
         * specific, so private is the safer default.
         */
        private String cacheScope =
                CACHE_SCOPE_PRIVATE;

        private McpServerInfo serverInfo;

        private final Map<String, Object> additionalMetadata =
                new LinkedHashMap<>();


        private Builder() {
        }


        public Builder content(
                McpResourceContents content
        ) {

            if (content != null) {

                contents.add(
                        content
                );
            }

            return this;
        }


        public Builder contents(
                List<McpResourceContents> contents
        ) {

            this.contents.clear();

            if (contents == null
                    || contents.isEmpty()) {

                return this;
            }

            for (McpResourceContents content : contents) {

                if (content != null) {

                    this.contents.add(
                            content
                    );
                }
            }

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


        public McpReadResourceResult build() {

            return new McpReadResourceResult(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static List<McpResourceContents> immutableContents(
            List<McpResourceContents> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyList();
        }

        List<McpResourceContents> copy =
                new ArrayList<>();

        for (McpResourceContents content : source) {

            if (content != null) {

                copy.add(
                        content
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
                    "MCP resources/read ttlMs must not be negative: "
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
                "MCP resources/read cacheScope must not be null."
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
                "Unsupported MCP resources/read cacheScope: "
                        + normalized
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
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpReadResourceResult)) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        McpReadResourceResult other =
                (McpReadResourceResult) object;

        return ttlMs == other.ttlMs
                && Objects.equals(
                        contents,
                        other.contents
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
                contents,
                ttlMs,
                cacheScope
        );
    }


    @Override
    public String toString() {

        return "McpReadResourceResult{"
                + "resultType="
                + getResultType()
                + ", contents="
                + contents
                + ", ttlMs="
                + ttlMs
                + ", cacheScope='"
                + cacheScope
                + '\''
                + '}';
    }
}