/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.protocol;

import java.util.Objects;

/**
 * Identifies the MCP server implementation.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This object describes the implementation of the MCP server.
 * It is used by server discovery and may also be included in
 * response metadata.
 * </p>
 *
 * <p>
 * Required fields:
 * </p>
 *
 * <ul>
 *     <li>{@code name}</li>
 *     <li>{@code version}</li>
 * </ul>
 *
 * <p>
 * Optional descriptive fields:
 * </p>
 *
 * <ul>
 *     <li>{@code title}</li>
 *     <li>{@code description}</li>
 *     <li>{@code websiteUrl}</li>
 * </ul>
 */
public final class McpServerInfo {

    private final String name;

    private final String version;

    private final String title;

    private final String description;

    private final String websiteUrl;

    private McpServerInfo(
            Builder builder
    ) {

        this.name =
                requireText(
                        builder.name,
                        "MCP server name"
                );

        this.version =
                requireText(
                        builder.version,
                        "MCP server version"
                );

        this.title =
                normalizeOptional(
                        builder.title
                );

        this.description =
                normalizeOptional(
                        builder.description
                );

        this.websiteUrl =
                normalizeOptional(
                        builder.websiteUrl
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
     * Creates minimum server information.
     *
     * @param name server implementation name
     * @param version server implementation version
     * @return server information
     */
    public static McpServerInfo of(
            String name,
            String version
    ) {

        return builder()
                .name(name)
                .version(version)
                .build();
    }

    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasTitle() {
        return title != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasWebsiteUrl() {
        return websiteUrl != null;
    }

    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static String requireText(
            String value,
            String fieldName
    ) {

        Objects.requireNonNull(
                value,
                fieldName + " must not be null."
        );

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return normalized;
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
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private String name;

        private String version;

        private String title;

        private String description;

        private String websiteUrl;

        private Builder() {
        }

        public Builder name(
                String name
        ) {

            this.name = name;

            return this;
        }

        public Builder version(
                String version
        ) {

            this.version = version;

            return this;
        }

        public Builder title(
                String title
        ) {

            this.title = title;

            return this;
        }

        public Builder description(
                String description
        ) {

            this.description = description;

            return this;
        }

        public Builder websiteUrl(
                String websiteUrl
        ) {

            this.websiteUrl = websiteUrl;

            return this;
        }

        public McpServerInfo build() {

            return new McpServerInfo(
                    this
            );
        }
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

        if (!(object instanceof McpServerInfo)) {
            return false;
        }

        McpServerInfo other =
                (McpServerInfo) object;

        return Objects.equals(
                name,
                other.name
        )
                && Objects.equals(
                        version,
                        other.version
                )
                && Objects.equals(
                        title,
                        other.title
                )
                && Objects.equals(
                        description,
                        other.description
                )
                && Objects.equals(
                        websiteUrl,
                        other.websiteUrl
                );
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                name,
                version,
                title,
                description,
                websiteUrl
        );
    }

    @Override
    public String toString() {

        return "McpServerInfo{"
                + "name='" + name + '\''
                + ", version='" + version + '\''
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", websiteUrl='" + websiteUrl + '\''
                + '}';
    }
}