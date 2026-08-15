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
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * MCP resource provider for CSS files in
 * the current GomsBook project.
 */
public final class ProjectCssResourceProvider
        implements McpResourceProvider {

    private static final String PROVIDER_ID =
            "project-css";

    private static final String URI_PREFIX =
            "gomsbook://project/css/";

    private static final String URI_TEMPLATE =
            "gomsbook://project/css/{fileName}";

    private static final String MIME_TYPE =
            "text/css";

    private static final String CSS_EXTENSION =
            ".css";

    private final Path cssRoot;

    /**
     * Creates a provider for the specified CSS directory.
     *
     * @param cssRoot CSS root directory
     */
    public ProjectCssResourceProvider(
            Path cssRoot) {

        this.cssRoot =
                Objects.requireNonNull(
                        cssRoot,
                        "cssRoot must not be null.")
                        .toAbsolutePath()
                        .normalize();
    }

    @Override
    public String getId() {

        return PROVIDER_ID;
    }

    @Override
    public List<McpResource> listResources() {

        if (!Files.exists(cssRoot)
                || !Files.isDirectory(cssRoot)) {

            return Collections.emptyList();
        }

        List<McpResource> resources =
                new ArrayList<>();

        try (Stream<Path> stream =
                Files.list(cssRoot)) {

            stream
                    .filter(Files::isRegularFile)
                    .filter(this::isCssFile)
                    .sorted(
                            Comparator.comparing(
                                    path ->
                                            path.getFileName()
                                                    .toString()))
                    .forEach(
                            path ->
                                    resources.add(
                                            createResource(
                                                    path)));

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to list project CSS resources: "
                            + cssRoot,
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
                                "Project CSS")
                        .description(
                                "Reads CSS stylesheets from the current GomsBook project.")
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

        return isCssFileName(
                fileName);
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
                    "Unsupported project CSS resource URI: "
                            + normalizedUri);
        }

        Path resourcePath =
                resolveResourcePath(
                        normalizedUri);

        if (!Files.exists(resourcePath)
                || !Files.isRegularFile(resourcePath)) {

            throw new McpResourceNotFoundException(
                    normalizedUri,
                    "Project CSS resource does not exist: "
                            + normalizedUri);
        }

        try {

            String content =
                    Files.readString(
                            resourcePath,
                            StandardCharsets.UTF_8);

            McpResourceContents contents =
                    McpResourceContents.text(
                            normalizedUri,
                            MIME_TYPE,
                            content);

            return List.of(
                    contents);

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to read project CSS resource: "
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

        return McpResource.builder()
                .uri(
                        uri)
                .name(
                        fileName)
                .title(
                        createTitle(
                                fileName))
                .description(
                        "CSS stylesheet in the current GomsBook project.")
                .mimeType(
                        MIME_TYPE)
                .size(
                        resolveFileSize(
                                path))
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
                cssRoot
                        .resolve(
                                fileName)
                        .toAbsolutePath()
                        .normalize();

        /*
         * Prevent path traversal such as:
         *
         * gomsbook://project/css/../../secret.css
         */
        if (!resolved.startsWith(
                cssRoot)) {

            throw new IllegalArgumentException(
                    "Resource path escapes CSS root: "
                            + uri);
        }

        return resolved;
    }

    private void validateFileName(
            String fileName) {

        if (fileName == null
                || fileName.isBlank()) {

            throw new IllegalArgumentException(
                    "CSS file name must not be blank.");
        }

        if (fileName.contains("/")
                || fileName.contains("\\")) {

            throw new IllegalArgumentException(
                    "Nested CSS resource paths are not supported: "
                            + fileName);
        }

        if (!isCssFileName(
                fileName)) {

            throw new IllegalArgumentException(
                    "Resource is not a CSS file: "
                            + fileName);
        }
    }

    private boolean isCssFile(
            Path path) {

        return isCssFileName(
                path.getFileName()
                        .toString());
    }

    private boolean isCssFileName(
            String fileName) {

        if (fileName == null
                || fileName.isBlank()) {

            return false;
        }

        return fileName
                .toLowerCase(
                        Locale.ROOT)
                .endsWith(
                        CSS_EXTENSION);
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

        String lower =
                fileName.toLowerCase(
                        Locale.ROOT);

        int extensionIndex =
                lower.lastIndexOf(
                        CSS_EXTENSION);

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