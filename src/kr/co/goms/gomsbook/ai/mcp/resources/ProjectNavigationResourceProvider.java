/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * MCP resource provider for the EPUB navigation document
 * of the current GomsBook project.
 */
public final class ProjectNavigationResourceProvider
        implements McpResourceProvider {

    private static final String PROVIDER_ID =
            "project-navigation";

    private static final String RESOURCE_URI =
            "gomsbook://project/navigation";

    private static final String RESOURCE_NAME =
            "project-navigation";

    private static final String MIME_TYPE =
            "application/xhtml+xml";

    private final Path navigationFile;

    /**
     * Creates a provider for the specified EPUB navigation file.
     *
     * <p>
     * Typically this path points to {@code nav.xhtml}.
     * </p>
     *
     * @param navigationFile navigation document
     */
    public ProjectNavigationResourceProvider(
            Path navigationFile) {

        this.navigationFile =
                Objects.requireNonNull(
                        navigationFile,
                        "navigationFile must not be null.")
                        .toAbsolutePath()
                        .normalize();
    }

    @Override
    public String getId() {

        return PROVIDER_ID;
    }

    @Override
    public List<McpResource> listResources() {

        if (!Files.exists(
                navigationFile)
                || !Files.isRegularFile(
                        navigationFile)) {

            return Collections.emptyList();
        }

        McpResource resource =
                McpResource.builder()
                        .uri(
                                RESOURCE_URI)
                        .name(
                                RESOURCE_NAME)
                        .title(
                                "Project Navigation")
                        .description(
                                "EPUB navigation document for the current GomsBook project.")
                        .mimeType(
                                MIME_TYPE)
                        .size(
                                resolveFileSize())
                        .build();

        return List.of(
                resource);
    }

    @Override
    public List<McpResourceTemplate> listResourceTemplates() {

        /*
         * This provider exposes a single fixed resource.
         * Therefore no resource template is required.
         */

        return Collections.emptyList();
    }

    @Override
    public boolean supports(
            String uri) {

        if (uri == null
                || uri.isBlank()) {

            return false;
        }

        return RESOURCE_URI.equals(
                uri.trim());
    }

    @Override
    public List<McpResourceContents> read(
            String uri) {

        String normalizedUri =
                normalizeUri(
                        uri);

        if (!supports(
                normalizedUri)) {

            throw new IllegalArgumentException(
                    "Unsupported project navigation resource URI: "
                            + normalizedUri);
        }

        if (!Files.exists(
                navigationFile)
                || !Files.isRegularFile(
                        navigationFile)) {

            throw new McpResourceNotFoundException(
                    normalizedUri,
                    "Project navigation resource does not exist: "
                            + navigationFile);
        }

        try {

            String content =
                    Files.readString(
                            navigationFile,
                            StandardCharsets.UTF_8);

            McpResourceContents contents =
                    McpResourceContents.text(
                            RESOURCE_URI,
                            MIME_TYPE,
                            content);

            return List.of(
                    contents);

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to read project navigation resource: "
                            + navigationFile,
                    exception);
        }
    }

    private Long resolveFileSize() {

        try {

            return Files.size(
                    navigationFile);

        } catch (IOException exception) {

            return null;
        }
    }

    private static String normalizeUri(
            String uri) {

        if (uri == null) {

            throw new IllegalArgumentException(
                    "uri must not be null.");
        }

        String normalized =
                uri.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "uri must not be blank.");
        }

        return normalized;
    }
}