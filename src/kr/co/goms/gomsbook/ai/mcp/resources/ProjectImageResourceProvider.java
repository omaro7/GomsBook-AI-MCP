/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * MCP resource provider for image assets in
 * the current GomsBook project.
 */
public final class ProjectImageResourceProvider
        implements McpResourceProvider {

    private static final String PROVIDER_ID =
            "project-images";

    private static final String URI_PREFIX =
            "gomsbook://project/images/";

    private static final String URI_TEMPLATE =
            "gomsbook://project/images/{fileName}";

    private final Path imageRoot;

    public ProjectImageResourceProvider(
            Path imageRoot) {

        this.imageRoot =
                Objects.requireNonNull(
                        imageRoot,
                        "imageRoot must not be null.")
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
                imageRoot)
                || !Files.isDirectory(
                        imageRoot)) {

            return Collections.emptyList();
        }

        List<McpResource> resources =
                new ArrayList<>();

        try (Stream<Path> stream =
                Files.list(
                        imageRoot)) {

            stream
                    .filter(
                            Files::isRegularFile)
                    .filter(
                            this::isSupportedImage)
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
                    "Failed to list project image resources: "
                            + imageRoot,
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

        return List.of(
                McpResourceTemplate.builder()
                        .uriTemplate(
                                URI_TEMPLATE)
                        .name(
                                PROVIDER_ID)
                        .title(
                                "Project Images")
                        .description(
                                "Reads image assets from the current GomsBook project.")
                        .build());
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

        return isSupportedImageName(
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
                    "Unsupported project image resource URI: "
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
                    "Project image resource does not exist: "
                            + normalizedUri);
        }

        try {

            byte[] bytes =
                    Files.readAllBytes(
                            resourcePath);

            String blob =
                    Base64.getEncoder()
                            .encodeToString(
                                    bytes);

            String mimeType =
                    resolveMimeType(
                            resourcePath);

            McpResourceContents contents =
                    McpResourceContents.blob(
                            normalizedUri,
                            mimeType,
                            blob);

            return List.of(
                    contents);

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to read project image resource: "
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
                        "Image asset in the current GomsBook project.")
                .mimeType(
                        resolveMimeType(
                                path))
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
                imageRoot
                        .resolve(
                                fileName)
                        .toAbsolutePath()
                        .normalize();

        if (!resolved.startsWith(
                imageRoot)) {

            throw new IllegalArgumentException(
                    "Resource path escapes image root: "
                            + uri);
        }

        return resolved;
    }

    private void validateFileName(
            String fileName) {

        if (fileName == null
                || fileName.isBlank()) {

            throw new IllegalArgumentException(
                    "Image file name must not be blank.");
        }

        if (fileName.contains(
                "/")
                || fileName.contains(
                        "\\")) {

            throw new IllegalArgumentException(
                    "Nested image resource paths are not supported: "
                            + fileName);
        }

        if (!isSupportedImageName(
                fileName)) {

            throw new IllegalArgumentException(
                    "Unsupported image resource type: "
                            + fileName);
        }
    }

    private boolean isSupportedImage(
            Path path) {

        return isSupportedImageName(
                path.getFileName()
                        .toString());
    }

    private boolean isSupportedImageName(
            String fileName) {

        if (fileName == null
                || fileName.isBlank()) {

            return false;
        }

        String lower =
                fileName.toLowerCase(
                        Locale.ROOT);

        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".webp")
                || lower.endsWith(".gif");
    }

    private String resolveMimeType(
            Path path) {

        String fileName =
                path.getFileName()
                        .toString()
                        .toLowerCase(
                                Locale.ROOT);

        if (fileName.endsWith(
                ".png")) {

            return "image/png";
        }

        if (fileName.endsWith(
                ".jpg")
                || fileName.endsWith(
                        ".jpeg")) {

            return "image/jpeg";
        }

        if (fileName.endsWith(
                ".webp")) {

            return "image/webp";
        }

        if (fileName.endsWith(
                ".gif")) {

            return "image/gif";
        }

        try {

            String detected =
                    Files.probeContentType(
                            path);

            if (detected != null
                    && !detected.isBlank()) {

                return detected;
            }

        } catch (IOException exception) {

            // Fall through.
        }

        return "application/octet-stream";
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

        int index =
                fileName.lastIndexOf(
                        '.');

        if (index <= 0) {

            return fileName;
        }

        return fileName.substring(
                0,
                index);
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