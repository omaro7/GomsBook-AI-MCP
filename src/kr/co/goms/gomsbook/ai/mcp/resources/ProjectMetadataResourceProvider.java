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
 * MCP resource provider for the metadata document
 * of the current GomsBook project.
 */
public final class ProjectMetadataResourceProvider
        implements McpResourceProvider {

    private static final String PROVIDER_ID =
            "project-metadata";

    private static final String RESOURCE_URI =
            "gomsbook://project/metadata";

    private static final String RESOURCE_NAME =
            "project-metadata";

    private static final String MIME_TYPE =
            "application/oebps-package+xml";

    private final Path metadataFile;

    /**
     * Creates a provider for the specified project metadata file.
     *
     * <p>
     * Typically this path points to the EPUB {@code content.opf}
     * package document.
     * </p>
     *
     * @param metadataFile metadata file
     */
    public ProjectMetadataResourceProvider(
            Path metadataFile) {

        this.metadataFile =
                Objects.requireNonNull(
                        metadataFile,
                        "metadataFile must not be null.")
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
                metadataFile)
                || !Files.isRegularFile(
                        metadataFile)) {

            return Collections.emptyList();
        }

        McpResource resource =
                McpResource.builder()
                        .uri(
                                RESOURCE_URI)
                        .name(
                                RESOURCE_NAME)
                        .title(
                                "Project Metadata")
                        .description(
                                "EPUB package metadata for the current GomsBook project.")
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
                    "Unsupported project metadata resource URI: "
                            + normalizedUri);
        }

        if (!Files.exists(
                metadataFile)
                || !Files.isRegularFile(
                        metadataFile)) {

            throw new McpResourceNotFoundException(
                    normalizedUri,
                    "Project metadata resource does not exist: "
                            + metadataFile);
        }

        try {

            String content =
                    Files.readString(
                            metadataFile,
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
                    "Failed to read project metadata resource: "
                            + metadataFile,
                    exception);
        }
    }

    private Long resolveFileSize() {

        try {

            return Files.size(
                    metadataFile);

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