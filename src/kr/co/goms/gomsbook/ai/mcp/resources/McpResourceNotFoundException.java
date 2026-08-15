/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

/**
 * Exception thrown when an MCP resource cannot be found.
 *
 * <p>
 * This exception may occur when:
 * </p>
 *
 * <ul>
 *     <li>No registered provider supports the requested URI.</li>
 *     <li>A provider supports the URI but the resource does not exist.</li>
 * </ul>
 *
 * <p>
 * The MCP request dispatcher may translate this exception
 * into an appropriate MCP / JSON-RPC error response.
 * </p>
 */
public class McpResourceNotFoundException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String uri;

    public McpResourceNotFoundException(
            String uri) {

        super(
                createMessage(
                        uri));

        this.uri =
                normalizeUri(
                        uri);
    }

    public McpResourceNotFoundException(
            String uri,
            Throwable cause) {

        super(
                createMessage(
                        uri),
                cause);

        this.uri =
                normalizeUri(
                        uri);
    }

    public McpResourceNotFoundException(
            String uri,
            String message) {

        super(
                normalizeMessage(
                        message,
                        uri));

        this.uri =
                normalizeUri(
                        uri);
    }

    public McpResourceNotFoundException(
            String uri,
            String message,
            Throwable cause) {

        super(
                normalizeMessage(
                        message,
                        uri),
                cause);

        this.uri =
                normalizeUri(
                        uri);
    }

    /**
     * Returns the resource URI that could not be resolved.
     *
     * @return resource URI
     */
    public String getUri() {

        return uri;
    }

    private static String createMessage(
            String uri) {

        return "MCP resource not found: "
                + normalizeUri(
                        uri);
    }

    private static String normalizeMessage(
            String message,
            String uri) {

        if (message == null
                || message.isBlank()) {

            return createMessage(
                    uri);
        }

        return message.trim();
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