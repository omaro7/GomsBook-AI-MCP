/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.Collections;
import java.util.List;

/**
 * Provider for MCP resources.
 *
 * <p>
 * A resource provider exposes one or more resources and optionally
 * resource templates to the MCP resource layer.
 * </p>
 *
 * <p>
 * Providers are responsible for resolving resource URIs and
 * returning the corresponding resource contents.
 * </p>
 */
public interface McpResourceProvider {

    /**
     * Returns a stable provider identifier.
     *
     * <p>
     * This value is used internally by the MCP server and is not
     * necessarily exposed through the MCP protocol.
     * </p>
     *
     * @return provider identifier
     */
    String getId();

    /**
     * Returns resources currently exposed by this provider.
     *
     * <p>
     * These resources are candidates for the
     * {@code resources/list} response.
     * </p>
     *
     * @return resource list
     */
    default List<McpResource> listResources() {

        return Collections.emptyList();
    }

    /**
     * Returns resource templates exposed by this provider.
     *
     * <p>
     * These templates are candidates for the
     * {@code resources/templates/list} response.
     * </p>
     *
     * @return resource template list
     */
    default List<McpResourceTemplate> listResourceTemplates() {

        return Collections.emptyList();
    }

    /**
     * Determines whether this provider can resolve the specified URI.
     *
     * @param uri resource URI
     *
     * @return {@code true} if this provider can resolve the URI
     */
    boolean supports(
            String uri);

    /**
     * Reads the resource identified by the specified URI.
     *
     * <p>
     * A single MCP resource URI may return one or more resource
     * content entries.
     * </p>
     *
     * @param uri resource URI
     *
     * @return resource contents
     *
     * @throws IllegalArgumentException
     *         if the URI is invalid
     *
     * @throws McpResourceNotFoundException
     *         if the resource does not exist
     */
    List<McpResourceContents> read(
            String uri);
}