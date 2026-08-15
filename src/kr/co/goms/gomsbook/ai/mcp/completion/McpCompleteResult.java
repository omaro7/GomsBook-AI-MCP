/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.core.McpResultType;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpServerInfo;

/**
 * Result returned by the MCP {@code completion/complete} method.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * The result contains the completion values produced for the
 * requested prompt or resource-template argument.
 * </p>
 */
public final class McpCompleteResult
        extends McpResult {

    private final McpCompletion completion;


    private McpCompleteResult(
            Builder builder
    ) {

        super(
                McpResultType.COMPLETE,
                builder.serverInfo,
                builder.additionalMetadata
        );

        this.completion =
                Objects.requireNonNull(
                        builder.completion,
                        "MCP completion must not be null."
                );

        validateCompletion(
                completion
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
     * Creates a completion result.
     *
     * @param completion completion
     * @return result
     */
    public static McpCompleteResult of(
            McpCompletion completion
    ) {

        return builder()
                .completion(
                        completion
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessor
     * ------------------------------------------------------------
     */

    public McpCompletion getCompletion() {
        return completion;
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private McpCompletion completion;

        private McpServerInfo serverInfo;

        private final Map<String, Object> additionalMetadata =
                new LinkedHashMap<>();


        private Builder() {
        }


        public Builder completion(
                McpCompletion completion
        ) {

            this.completion =
                    completion;

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


        public McpCompleteResult build() {

            return new McpCompleteResult(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static void validateCompletion(
            McpCompletion completion
    ) {

        Objects.requireNonNull(
                completion,
                "MCP completion must not be null."
        );


        /*
         * MCP 2026-07-28:
         *
         * completion.values MUST NOT contain more than
         * 100 values.
         */
        if (completion.getValues() == null) {

            throw new IllegalArgumentException(
                    "MCP completion values must not be null."
            );
        }


        if (completion.getValues().size() > 100) {

            throw new IllegalArgumentException(
                    "MCP completion values must not exceed "
                            + "100 items."
            );
        }


        /*
         * total is optional.
         *
         * If supplied, it must not be negative and should not
         * be smaller than the number of returned values.
         */
        if (completion.getTotal() != null) {

            int total =
                    completion.getTotal();


            if (total < 0) {

                throw new IllegalArgumentException(
                        "MCP completion total must not be negative."
                );
            }


            if (total < completion.getValues().size()) {

                throw new IllegalArgumentException(
                        "MCP completion total must not be smaller "
                                + "than the number of returned values."
                );
            }
        }
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


        if (!(object instanceof McpCompleteResult)) {
            return false;
        }


        if (!super.equals(
                object
        )) {

            return false;
        }


        McpCompleteResult other =
                (McpCompleteResult) object;


        return Objects.equals(
                completion,
                other.completion
        );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                super.hashCode(),
                completion
        );
    }


    @Override
    public String toString() {

        return "McpCompleteResult{"
                + "resultType="
                + getResultType()
                + ", completion="
                + completion
                + '}';
    }
}