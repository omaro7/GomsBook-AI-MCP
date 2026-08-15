/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.common.McpIcon;

/**
 * Information identifying an MCP client implementation.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This model is used by request metadata:
 * </p>
 *
 * <pre>
 * params._meta[
 *     "io.modelcontextprotocol/clientInfo"
 * ]
 * </pre>
 *
 * <p>
 * The MCP wire schema calls this structure
 * {@code Implementation}. GomsBook uses the more explicit
 * {@code McpClientInfo} name to distinguish client information
 * from server information.
 * </p>
 */
public final class McpClientInfo {

    private String name;

    private String title;

    private String version;

    private String description;

    private String websiteUrl;

    private List<McpIcon> icons;


    /*
     * ------------------------------------------------------------
     * Gson constructor
     * ------------------------------------------------------------
     */

    /**
     * Constructor for Gson deserialization.
     */
    public McpClientInfo() {

        this.icons =
                new ArrayList<>();
    }


    private McpClientInfo(
            Builder builder
    ) {

        this.name =
                requireText(
                        builder.name,
                        "MCP client name"
                );

        this.title =
                normalizeOptional(
                        builder.title
                );

        this.version =
                requireText(
                        builder.version,
                        "MCP client version"
                );

        this.description =
                normalizeOptional(
                        builder.description
                );

        this.websiteUrl =
                normalizeOptional(
                        builder.websiteUrl
                );

        this.icons =
                copyIcons(
                        builder.icons
                );

        validate();
    }


    /*
     * ------------------------------------------------------------
     * Factory
     * ------------------------------------------------------------
     */

    public static Builder builder() {
        return new Builder();
    }


    public static McpClientInfo of(
            String name,
            String version
    ) {

        return builder()
                .name(
                        name
                )
                .version(
                        version
                )
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


    public String getTitle() {
        return title;
    }


    public String getVersion() {
        return version;
    }


    public String getDescription() {
        return description;
    }


    public String getWebsiteUrl() {
        return websiteUrl;
    }


    public List<McpIcon> getIcons() {

        if (icons == null
                || icons.isEmpty()) {

            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                icons
        );
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


    public boolean hasIcons() {

        return icons != null
                && !icons.isEmpty();
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    public void validate() {

        this.name =
                requireText(
                        name,
                        "MCP client name"
                );

        this.version =
                requireText(
                        version,
                        "MCP client version"
                );

        this.title =
                normalizeOptional(
                        title
                );

        this.description =
                normalizeOptional(
                        description
                );

        this.websiteUrl =
                normalizeOptional(
                        websiteUrl
                );

        if (icons == null) {

            icons =
                    new ArrayList<>();

        } else {

            List<McpIcon> validatedIcons =
                    new ArrayList<>();

            for (McpIcon icon : icons) {

                if (icon == null) {
                    continue;
                }

                validatedIcons.add(
                        icon
                );
            }

            icons =
                    validatedIcons;
        }
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private String name;

        private String title;

        private String version;

        private String description;

        private String websiteUrl;

        private final List<McpIcon> icons =
                new ArrayList<>();


        private Builder() {
        }


        public Builder name(
                String name
        ) {

            this.name =
                    name;

            return this;
        }


        public Builder title(
                String title
        ) {

            this.title =
                    title;

            return this;
        }


        public Builder version(
                String version
        ) {

            this.version =
                    version;

            return this;
        }


        public Builder description(
                String description
        ) {

            this.description =
                    description;

            return this;
        }


        public Builder websiteUrl(
                String websiteUrl
        ) {

            this.websiteUrl =
                    websiteUrl;

            return this;
        }


        public Builder icon(
                McpIcon icon
        ) {

            if (icon != null) {

                this.icons.add(
                        icon
                );
            }

            return this;
        }


        public Builder icons(
                List<McpIcon> icons
        ) {

            this.icons.clear();

            if (icons == null
                    || icons.isEmpty()) {

                return this;
            }

            for (McpIcon icon : icons) {

                if (icon != null) {

                    this.icons.add(
                            icon
                    );
                }
            }

            return this;
        }


        public McpClientInfo build() {

            return new McpClientInfo(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Utility
     * ------------------------------------------------------------
     */

    private static List<McpIcon> copyIcons(
            List<McpIcon> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return new ArrayList<>();
        }

        List<McpIcon> copy =
                new ArrayList<>();

        for (McpIcon icon : source) {

            if (icon != null) {

                copy.add(
                        icon
                );
            }
        }

        return copy;
    }


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

        if (!(object instanceof McpClientInfo)) {
            return false;
        }

        McpClientInfo other =
                (McpClientInfo) object;

        return Objects.equals(
                name,
                other.name
        )
                && Objects.equals(
                        title,
                        other.title
                )
                && Objects.equals(
                        version,
                        other.version
                )
                && Objects.equals(
                        description,
                        other.description
                )
                && Objects.equals(
                        websiteUrl,
                        other.websiteUrl
                )
                && Objects.equals(
                        icons,
                        other.icons
                );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                name,
                title,
                version,
                description,
                websiteUrl,
                icons
        );
    }


    @Override
    public String toString() {

        return "McpClientInfo{"
                + "name='"
                + name
                + '\''
                + ", title='"
                + title
                + '\''
                + ", version='"
                + version
                + '\''
                + ", description='"
                + description
                + '\''
                + ", websiteUrl='"
                + websiteUrl
                + '\''
                + ", icons="
                + icons
                + '}';
    }
}