/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * MCP resource provider for XHTML documents in
 * the current GomsBook project.
 */
public final class ProjectXhtmlResourceProvider
        implements McpResourceProvider {

    private static final String PROVIDER_ID =
            "project-xhtml";

    private static final String URI_PREFIX =
            "gomsbook://project/xhtml/";

    private static final String URI_TEMPLATE =
            "gomsbook://project/xhtml/{fileName}";

    private static final String MIME_TYPE =
            "application/xhtml+xml";

    private static final String XHTML_EXTENSION =
            ".xhtml";

    private final Path xhtmlRoot;

    /**
     * Creates a provider using the project's XHTML root directory.
     *
     * @param xhtmlRoot XHTML directory
     */
    public ProjectXhtmlResourceProvider(
            Path xhtmlRoot) {

        this.xhtmlRoot =
                Objects.requireNonNull(
                        xhtmlRoot,
                        "xhtmlRoot must not be null.")
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
                xhtmlRoot)
                || !Files.isDirectory(
                        xhtmlRoot)) {

            return Collections.emptyList();
        }

        List<McpResource> resources =
                new ArrayList<>();

        try (Stream<Path> stream =
                Files.list(
                        xhtmlRoot)) {

            stream
                    .filter(
                            Files::isRegularFile)
                    .filter(
                            this::isXhtmlFile)
                    .sorted(
                            Comparator.comparing(
                                    path ->
                                            path.getFileName()
                                                    .toString()))
                    .forEach(
                            path -> {

                                McpResource resource =
                                        createResource(
                                                path);

                                resources.add(
                                        resource);
                            });

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to list XHTML resources: "
                            + xhtmlRoot,
                    exception);
        }

        if (resources.isEmpty()) {

            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                resources);
    }

    @Override
    public List<McpResourceTemplate> listResourceTemplates() {

        McpResourceTemplate template =
                McpResourceTemplate.builder()
                        .uriTemplate(
                                URI_TEMPLATE)
                        .name(
                                PROVIDER_ID)
                        .title(
                                "Project XHTML")
                        .description(
                                "Reads XHTML documents from the current GomsBook project.")
                        .mimeType(
                                MIME_TYPE)
                        .build();

        return List.of(
                template);
    }

    @Override
    public boolean supports(
            String uri) {

        if (uri == null
                || uri.isBlank()) {

            return false;
        }

        String normalizedUri =
                uri.trim();

        if (!normalizedUri.startsWith(
                URI_PREFIX)) {

            return false;
        }

        String fileName =
                normalizedUri.substring(
                        URI_PREFIX.length());

        if (fileName.isBlank()) {

            return false;
        }

        return fileName
                .toLowerCase()
                .endsWith(
                        XHTML_EXTENSION);
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
                    "Unsupported XHTML resource URI: "
                            + normalizedUri);
        }

        Path resourcePath =
                resolveResourcePath(
                        normalizedUri);

        if (!Files.exists(
                resourcePath)
                || !Files.isRegularFile(
                        resourcePath)) {

            throw new McpResourceNotFoundException(
                    normalizedUri,
                    "XHTML resource does not exist: "
                            + normalizedUri);
        }

        try {

            String content =
                    Files.readString(
                            resourcePath,
                            StandardCharsets.UTF_8);

            McpResourceContents resourceContents =
                    McpResourceContents.text(
                            normalizedUri,
                            MIME_TYPE,
                            content);

            return List.of(
                    resourceContents);

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to read XHTML resource: "
                            + normalizedUri,
                    exception);
        }
    }

    private McpResource createResource(
            Path path) {

        String fileName =
                path.getFileName()
                        .toString();

        String uri =
                URI_PREFIX
                        + fileName;

        Long size =
                resolveFileSize(
                        path);

        return McpResource.builder()
                .uri(
                        uri)
                .name(
                        fileName)
                .title(
                        createTitle(
                                fileName))
                .description(
                        "XHTML document in the current GomsBook project.")
                .mimeType(
                        MIME_TYPE)
                .size(
                        size)
                .build();
    }

    private Path resolveResourcePath(
            String uri) {

        String fileName =
                uri.substring(
                        URI_PREFIX.length());

        validateFileName(
                fileName);

        Path resolved =
                xhtmlRoot.resolve(
                                fileName)
                        .normalize()
                        .toAbsolutePath();

        /*
         * Prevent path traversal such as:
         *
         * gomsbook://project/xhtml/../../secret.xhtml
         */
        if (!resolved.startsWith(
                xhtmlRoot)) {

            throw new IllegalArgumentException(
                    "Resource path escapes XHTML root: "
                            + uri);
        }

        return resolved;
    }

    private void validateFileName(
            String fileName) {

        if (fileName == null
                || fileName.isBlank()) {

            throw new IllegalArgumentException(
                    "XHTML file name must not be blank.");
        }

        if (fileName.contains(
                "/")
                || fileName.contains(
                        "\\")) {

            throw new IllegalArgumentException(
                    "Nested XHTML resource paths are not supported: "
                            + fileName);
        }

        if (!fileName
                .toLowerCase()
                .endsWith(
                        XHTML_EXTENSION)) {

            throw new IllegalArgumentException(
                    "Resource is not an XHTML file: "
                            + fileName);
        }
    }

    private boolean isXhtmlFile(
            Path path) {

        String fileName =
                path.getFileName()
                        .toString();

        return fileName
                .toLowerCase()
                .endsWith(
                        XHTML_EXTENSION);
    }

    private Long resolveFileSize(
            Path path) {

        try {

            return Files.size(
                    path);

        } catch (IOException exception) {

            return null;
        }
    }

    private String createTitle(
            String fileName) {

        int extensionIndex =
                fileName.toLowerCase()
                        .lastIndexOf(
                                XHTML_EXTENSION);

        if (extensionIndex <= 0) {

            return fileName;
        }

        return fileName.substring(
                0,
                extensionIndex);
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