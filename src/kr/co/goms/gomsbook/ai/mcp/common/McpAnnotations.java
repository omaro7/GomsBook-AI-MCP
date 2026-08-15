/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * MCP annotations.
 *
 * <p>
 * Represents optional annotation metadata shared by MCP
 * resources and content blocks.
 * </p>
 *
 * <p>
 * Supported fields:
 * </p>
 *
 * <ul>
 *     <li>audience</li>
 *     <li>priority</li>
 *     <li>lastModified</li>
 * </ul>
 */
public final class McpAnnotations {

    /**
     * Intended audiences.
     */
    private final List<McpRole> audience;

    /**
     * Optional priority value.
     *
     * <p>
     * Valid range is {@code 0.0} to {@code 1.0}.
     * </p>
     */
    private final Double priority;

    /**
     * Optional ISO-8601 last modified timestamp.
     */
    private final String lastModified;

    private McpAnnotations(
            Builder builder
    ) {

        this.audience =
                normalizeAudience(
                        builder.audience
                );

        this.priority =
                validatePriority(
                        builder.priority
                );

        this.lastModified =
                normalizeLastModified(
                        builder.lastModified
                );
    }

    /**
     * Returns intended audiences.
     *
     * @return immutable audience list
     */
    public List<McpRole> getAudience() {
        return audience;
    }

    /**
     * Returns priority.
     *
     * @return priority or {@code null}
     */
    public Double getPriority() {
        return priority;
    }

    /**
     * Returns last modified timestamp.
     *
     * @return timestamp or {@code null}
     */
    public String getLastModified() {
        return lastModified;
    }

    /**
     * Returns whether audience information exists.
     *
     * @return {@code true} if audience exists
     */
    public boolean hasAudience() {
        return !audience.isEmpty();
    }

    /**
     * Returns whether priority exists.
     *
     * @return {@code true} if priority exists
     */
    public boolean hasPriority() {
        return priority != null;
    }

    /**
     * Returns whether lastModified exists.
     *
     * @return {@code true} if lastModified exists
     */
    public boolean hasLastModified() {
        return lastModified != null;
    }

    /**
     * Returns whether no annotation values are present.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {

        return audience.isEmpty()
                && priority == null
                && lastModified == null;
    }

    /**
     * Creates a new builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates empty annotations.
     *
     * @return empty annotations
     */
    public static McpAnnotations empty() {

        return builder()
                .build();
    }

    private static List<McpRole> normalizeAudience(
            List<McpRole> audience
    ) {

        if (audience == null
                || audience.isEmpty()) {

            return Collections.emptyList();
        }

        List<McpRole> normalized =
                new ArrayList<>();

        for (McpRole role : audience) {

            McpRole safeRole =
                    Objects.requireNonNull(
                            role,
                            "MCP audience role must not be null."
                    );

            if (!normalized.contains(
                    safeRole
            )) {

                normalized.add(
                        safeRole
                );
            }
        }

        return Collections.unmodifiableList(
                normalized
        );
    }

    private static Double validatePriority(
            Double priority
    ) {

        if (priority == null) {
            return null;
        }

        if (priority.isNaN()
                || priority.isInfinite()) {

            throw new IllegalArgumentException(
                    "MCP priority must be a finite number."
            );
        }

        if (priority < 0.0
                || priority > 1.0) {

            throw new IllegalArgumentException(
                    "MCP priority must be between 0.0 and 1.0."
            );
        }

        return priority;
    }

    /**
     * Normalizes the MCP lastModified value.
     *
     * <p>
     * The protocol value is preserved as a string so the model layer
     * does not impose stronger parsing restrictions than MCP itself.
     * </p>
     */
    private static String normalizeLastModified(
            String lastModified
    ) {

        if (lastModified == null) {
            return null;
        }

        String normalized =
                lastModified.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    /**
     * Builder for {@link McpAnnotations}.
     */
    public static final class Builder {

        private List<McpRole> audience;

        private Double priority;

        private String lastModified;

        private Builder() {
        }

        /**
         * Sets intended audiences.
         *
         * @param audience audience roles
         * @return this builder
         */
        public Builder audience(
                List<McpRole> audience
        ) {

            this.audience =
                    audience == null
                            ? null
                            : new ArrayList<>(
                                    audience
                            );

            return this;
        }

        /**
         * Adds an intended audience.
         *
         * @param role audience role
         * @return this builder
         */
        public Builder addAudience(
                McpRole role
        ) {

            Objects.requireNonNull(
                    role,
                    "MCP audience role must not be null."
            );

            if (this.audience == null) {

                this.audience =
                        new ArrayList<>();
            }

            if (!this.audience.contains(
                    role
            )) {

                this.audience.add(
                        role
                );
            }

            return this;
        }

        /**
         * Sets priority.
         *
         * @param priority value between 0.0 and 1.0
         * @return this builder
         */
        public Builder priority(
                Double priority
        ) {

            this.priority =
                    priority;

            return this;
        }

        /**
         * Sets last modified timestamp.
         *
         * @param lastModified ISO-8601 formatted string
         * @return this builder
         */
        public Builder lastModified(
                String lastModified
        ) {

            this.lastModified =
                    lastModified;

            return this;
        }

        /**
         * Sets last modified timestamp from an Instant.
         *
         * @param lastModified instant
         * @return this builder
         */
        public Builder lastModified(
                Instant lastModified
        ) {

            this.lastModified =
                    lastModified == null
                            ? null
                            : lastModified.toString();

            return this;
        }

        /**
         * Builds annotations.
         *
         * @return annotations
         */
        public McpAnnotations build() {

            return new McpAnnotations(
                    this
            );
        }
    }
}