/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.content.McpContent;
import kr.co.goms.gomsbook.ai.mcp.content.McpTextContent;
import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.core.McpResultType;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpServerInfo;

/**
 * Result returned by the MCP {@code tools/call} method.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * A tool call result is a normal MCP result and therefore uses
 * {@code resultType = complete}.
 * </p>
 *
 * <p>
 * Tool execution failures that are part of normal tool behavior
 * are represented using {@code isError = true}. They are not
 * JSON-RPC protocol errors.
 * </p>
 */
public final class McpToolResult
        extends McpResult {

    private final List<McpContent> content;

    private final Map<String, Object> structuredContent;

    private final boolean isError;


    private McpToolResult(
            Builder builder
    ) {

        super(
                McpResultType.COMPLETE,
                builder.serverInfo,
                builder.additionalMetadata
        );

        this.content =
                immutableContent(
                        builder.content
                );

        this.structuredContent =
                immutableStructuredContent(
                        builder.structuredContent
                );

        this.isError =
                builder.isError;
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
     * Creates a successful tool result.
     *
     * @param content result content
     * @return tool result
     */
    public static McpToolResult success(
            List<McpContent> content
    ) {

        return builder()
                .content(
                        content
                )
                .isError(
                        false
                )
                .build();
    }


    /**
     * Creates a tool-level error result.
     *
     * <p>
     * This is still a successful JSON-RPC response.
     * </p>
     *
     * @param content error content
     * @return tool result
     */
    public static McpToolResult error(
            List<McpContent> content
    ) {

        return builder()
                .content(
                        content
                )
                .isError(
                        true
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public List<McpContent> getContent() {
        return content;
    }


    public Map<String, Object> getStructuredContent() {
        return structuredContent;
    }


    public boolean isError() {
        return isError;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasContent() {
        return !content.isEmpty();
    }


    public boolean hasStructuredContent() {
        return !structuredContent.isEmpty();
    }


    public boolean isSuccess() {
        return !isError;
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private final List<McpContent> content =
                new ArrayList<>();

        private final Map<String, Object> structuredContent =
                new LinkedHashMap<>();

        private boolean isError;

        private McpServerInfo serverInfo;

        private final Map<String, Object> additionalMetadata =
                new LinkedHashMap<>();


        private Builder() {
        }


        public Builder content(
                McpContent content
        ) {

            if (content != null) {

                this.content.add(
                        content
                );
            }

            return this;
        }


        public Builder content(
                List<McpContent> content
        ) {

            this.content.clear();

            if (content == null
                    || content.isEmpty()) {

                return this;
            }

            for (McpContent item : content) {

                if (item != null) {

                    this.content.add(
                            item
                    );
                }
            }

            return this;
        }


        public Builder structuredContent(
                String name,
                Object value
        ) {

            String normalizedName =
                    requireStructuredContentName(
                            name
                    );

            if (value == null) {

                structuredContent.remove(
                        normalizedName
                );

            } else {

                structuredContent.put(
                        normalizedName,
                        value
                );
            }

            return this;
        }


        public Builder structuredContent(
                Map<String, Object> structuredContent
        ) {

            this.structuredContent.clear();

            if (structuredContent == null
                    || structuredContent.isEmpty()) {

                return this;
            }

            for (Map.Entry<String, Object> entry
                    : structuredContent.entrySet()) {

                structuredContent(
                        entry.getKey(),
                        entry.getValue()
                );
            }

            return this;
        }


        public Builder isError(
                boolean isError
        ) {

            this.isError =
                    isError;

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


        public McpToolResult build() {

            return new McpToolResult(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Validation / immutable copies
     * ------------------------------------------------------------
     */

    private static List<McpContent> immutableContent(
            List<McpContent> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyList();
        }

        List<McpContent> copy =
                new ArrayList<>();

        for (McpContent item : source) {

            if (item != null) {

                copy.add(
                        item
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


    private static Map<String, Object> immutableStructuredContent(
            Map<String, Object> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyMap();
        }

        Map<String, Object> copy =
                new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry
                : source.entrySet()) {

            String name =
                    requireStructuredContentName(
                            entry.getKey()
                    );

            if (entry.getValue() != null) {

                copy.put(
                        name,
                        entry.getValue()
                );
            }
        }

        if (copy.isEmpty()) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                copy
        );
    }


    private static String requireStructuredContentName(
            String name
    ) {

        Objects.requireNonNull(
                name,
                "MCP structured content name must not be null."
        );

        String normalized =
                name.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP structured content name must not be blank."
            );
        }

        return normalized;
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

        if (!(object instanceof McpToolResult)) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        McpToolResult other =
                (McpToolResult) object;

        return isError == other.isError
                && Objects.equals(
                        content,
                        other.content
                )
                && Objects.equals(
                        structuredContent,
                        other.structuredContent
                );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                super.hashCode(),
                content,
                structuredContent,
                isError
        );
    }


    @Override
    public String toString() {

        return "McpToolResult{"
                + "resultType="
                + getResultType()
                + ", content="
                + content
                + ", structuredContent="
                + structuredContent
                + ", isError="
                + isError
                + '}';
    }


    /**
     * Creates an MCP tool error result.
     *
     * @param message error message
     * @return MCP tool result
     */
    public static McpToolResult error(
            String message
    ) {

        Objects.requireNonNull(
                message,
                "MCP tool error message must not be null."
        );

        String normalized =
                message.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP tool error message must not be blank."
            );
        }

        return builder()
                .content(
                        Collections.singletonList(
                                McpTextContent.builder()
                                        .text(
                                                normalized
                                        )
                                        .build()
                        )
                )
                .isError(
                        true
                )
                .build();
    }
}