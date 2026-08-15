/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

/**
 * Service for MCP resource operations.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Supported MCP operations:
 * </p>
 *
 * <ul>
 *     <li>{@code resources/list}</li>
 *     <li>{@code resources/read}</li>
 *     <li>{@code resources/templates/list}</li>
 * </ul>
 */
public interface McpResourceService {

    /**
     * Handles {@code resources/list}.
     *
     * @param params list parameters; may be {@code null}
     *
     * @return resource list result
     */
    McpListResourcesResult listResources(
            McpListResourcesParams params);

    /**
     * Handles {@code resources/read}.
     *
     * @param params read parameters
     *
     * @return resource read result
     *
     * @throws IllegalArgumentException
     *         if params or URI are invalid
     *
     * @throws McpResourceNotFoundException
     *         if no provider supports the URI
     */
    McpReadResourceResult readResource(
            McpReadResourceParams params);

    /**
     * Handles {@code resources/templates/list}.
     *
     * @param params list parameters; may be {@code null}
     *
     * @return resource template list result
     */
    McpListResourceTemplatesResult listResourceTemplates(
            McpListResourceTemplatesParams params);

    /**
     * Returns whether a registered provider supports
     * the specified resource URI.
     *
     * @param uri resource URI
     *
     * @return {@code true} if supported
     */
    default boolean supports(
            String uri) {

        if (uri == null
                || uri.trim().isEmpty()) {

            return false;
        }

        return false;
    }
}