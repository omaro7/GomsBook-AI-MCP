/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

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
 * Result returned by the MCP {@code prompts/list} method.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This result contains the prompts exposed by the server and
 * supports pagination and cache metadata.
 * </p>
 */
public final class McpListPromptsResult
        extends McpResult {

    public static final String CACHE_SCOPE_PUBLIC =
            "public";

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


    private final List<McpPrompt> prompts;

    private final String nextCursor;

    private final long ttlMs;

    private final String cacheScope;


    private McpListPromptsResult(
            Builder builder
    ) {

        super(
                McpResultType.COMPLETE,
                builder.serverInfo,
                builder.additionalMetadata
        );

        this.prompts =
                immutablePrompts(
                        builder.prompts
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
     * Creates a non-paginated prompts result.
     *
     * @param prompts prompts
     * @return result
     */
    public static McpListPromptsResult of(
            List<McpPrompt> prompts
    ) {

        return builder()
                .prompts(
                        prompts
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public List<McpPrompt> getPrompts() {
        return prompts;
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

    public boolean hasPrompts() {
        return !prompts.isEmpty();
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

        private final List<McpPrompt> prompts =
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


        public Builder prompt(
                McpPrompt prompt
        ) {

            if (prompt != null) {

                prompts.add(
                        prompt
                );
            }

            return this;
        }


        public Builder prompts(
                List<McpPrompt> prompts
        ) {

            this.prompts.clear();

            if (prompts == null
                    || prompts.isEmpty()) {

                return this;
            }

            for (McpPrompt prompt : prompts) {

                if (prompt != null) {

                    this.prompts.add(
                            prompt
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


        public McpListPromptsResult build() {

            return new McpListPromptsResult(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static List<McpPrompt> immutablePrompts(
            List<McpPrompt> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyList();
        }

        List<McpPrompt> copy =
                new ArrayList<>();

        for (McpPrompt prompt : source) {

            if (prompt != null) {

                copy.add(
                        prompt
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
                    "MCP prompts/list ttlMs must not be negative: "
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
                "MCP prompts/list cacheScope must not be null."
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
                "Unsupported MCP prompts/list cacheScope: "
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

        if (!(object instanceof McpListPromptsResult)) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        McpListPromptsResult other =
                (McpListPromptsResult) object;

        return ttlMs == other.ttlMs
                && Objects.equals(
                        prompts,
                        other.prompts
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
                prompts,
                nextCursor,
                ttlMs,
                cacheScope
        );
    }


    @Override
    public String toString() {

        return "McpListPromptsResult{"
                + "resultType="
                + getResultType()
                + ", prompts="
                + prompts
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