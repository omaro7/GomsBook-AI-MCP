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
 * Result returned by the MCP {@code prompts/get} method.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * A prompt result contains an optional description and one or
 * more prompt messages.
 * </p>
 */
public final class McpGetPromptResult
        extends McpResult {

    private final String description;

    private final List<McpPromptMessage> messages;


    private McpGetPromptResult(
            Builder builder
    ) {

        super(
                McpResultType.COMPLETE,
                builder.serverInfo,
                builder.additionalMetadata
        );

        this.description =
                normalizeOptional(
                        builder.description
                );

        this.messages =
                immutableMessages(
                        builder.messages
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
     * Creates a prompt result containing messages.
     *
     * @param messages prompt messages
     * @return prompt result
     */
    public static McpGetPromptResult of(
            List<McpPromptMessage> messages
    ) {

        return builder()
                .messages(
                        messages
                )
                .build();
    }


    /**
     * Creates a prompt result containing description and messages.
     *
     * @param description prompt description
     * @param messages prompt messages
     * @return prompt result
     */
    public static McpGetPromptResult of(
            String description,
            List<McpPromptMessage> messages
    ) {

        return builder()
                .description(
                        description
                )
                .messages(
                        messages
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public String getDescription() {
        return description;
    }


    public List<McpPromptMessage> getMessages() {
        return messages;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasDescription() {
        return description != null;
    }


    public boolean hasMessages() {
        return !messages.isEmpty();
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private String description;

        private final List<McpPromptMessage> messages =
                new ArrayList<>();

        private McpServerInfo serverInfo;

        private final Map<String, Object> additionalMetadata =
                new LinkedHashMap<>();


        private Builder() {
        }


        public Builder description(
                String description
        ) {

            this.description =
                    description;

            return this;
        }


        public Builder message(
                McpPromptMessage message
        ) {

            if (message != null) {

                messages.add(
                        message
                );
            }

            return this;
        }


        public Builder messages(
                List<McpPromptMessage> messages
        ) {

            this.messages.clear();

            if (messages == null
                    || messages.isEmpty()) {

                return this;
            }

            for (McpPromptMessage message : messages) {

                if (message != null) {

                    this.messages.add(
                            message
                    );
                }
            }

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


        public McpGetPromptResult build() {

            return new McpGetPromptResult(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static List<McpPromptMessage> immutableMessages(
            List<McpPromptMessage> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyList();
        }

        List<McpPromptMessage> copy =
                new ArrayList<>();

        for (McpPromptMessage message : source) {

            if (message != null) {

                copy.add(
                        message
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

        if (!(object instanceof McpGetPromptResult)) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        McpGetPromptResult other =
                (McpGetPromptResult) object;

        return Objects.equals(
                description,
                other.description
        )
                && Objects.equals(
                        messages,
                        other.messages
                );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                super.hashCode(),
                description,
                messages
        );
    }


    @Override
    public String toString() {

        return "McpGetPromptResult{"
                + "resultType="
                + getResultType()
                + ", description='"
                + description
                + '\''
                + ", messages="
                + messages
                + '}';
    }
}