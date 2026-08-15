/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

import kr.co.goms.gomsbook.ai.mcp.core.McpRequestMetadata;


/**
 * Parameters for the MCP {@code completion/complete} request.
 *
 * <p>
 * MCP 2026-07-28 requires request metadata on every request.
 * </p>
 */
public final class McpCompleteParams {

    @SerializedName("_meta")
    private final McpRequestMetadata metadata;

    private final McpCompletionReference ref;

    private final McpCompletionArgument argument;

    private final McpCompletionContext context;

    /**
     * Creates completion parameters without additional context.
     *
     * @param metadata MCP request metadata
     * @param ref completion target reference
     * @param argument argument currently being completed
     */
    public McpCompleteParams(
            McpRequestMetadata metadata,
            McpCompletionReference ref,
            McpCompletionArgument argument) {

        this(
                metadata,
                ref,
                argument,
                null
        );
    }

    /**
     * Creates completion parameters.
     *
     * @param metadata MCP request metadata
     * @param ref completion target reference
     * @param argument argument currently being completed
     * @param context optional completion context
     */
    public McpCompleteParams(
            McpRequestMetadata metadata,
            McpCompletionReference ref,
            McpCompletionArgument argument,
            McpCompletionContext context) {

        this.metadata =
                Objects.requireNonNull(
                        metadata,
                        "metadata must not be null."
                );

        this.ref =
                Objects.requireNonNull(
                        ref,
                        "ref must not be null."
                );

        this.argument =
                Objects.requireNonNull(
                        argument,
                        "argument must not be null."
                );

        this.context =
                context;
    }

    public McpRequestMetadata getMetadata() {

        return metadata;
    }

    public McpCompletionReference getRef() {

        return ref;
    }

    public McpCompletionArgument getArgument() {

        return argument;
    }

    public McpCompletionContext getContext() {

        return context;
    }

    public boolean hasContext() {

        return context != null;
    }

    @Override
    public String toString() {

        return "McpCompleteParams{" +
                "metadata=" + metadata +
                ", ref=" + ref +
                ", argument=" + argument +
                ", context=" + context +
                '}';
    }
}