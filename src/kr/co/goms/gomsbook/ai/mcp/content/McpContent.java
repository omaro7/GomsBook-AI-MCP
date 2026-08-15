/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.content;

import java.util.Map;

import kr.co.goms.gomsbook.ai.mcp.common.McpAnnotations;

/**
 * Base contract for MCP content blocks.
 *
 * <p>
 * Implementations represent concrete MCP content types such as:
 * text, image, audio, resource link, and embedded resource.
 * </p>
 */
public interface McpContent {

    /**
     * Returns the MCP content type.
     *
     * @return content type
     */
    McpContentType getType();

    /**
     * Returns optional MCP annotations.
     *
     * @return annotations or {@code null}
     */
    McpAnnotations getAnnotations();

    /**
     * Returns optional MCP metadata.
     *
     * @return metadata map or {@code null}
     */
    Map<String, Object> getMeta();

    /**
     * Returns whether annotations are present.
     *
     * @return {@code true} if non-empty annotations exist
     */
    default boolean hasAnnotations() {

        McpAnnotations annotations =
                getAnnotations();

        return annotations != null
                && !annotations.isEmpty();
    }

    /**
     * Returns whether metadata is present.
     *
     * @return {@code true} if non-empty metadata exists
     */
    default boolean hasMeta() {

        Map<String, Object> meta =
                getMeta();

        return meta != null
                && !meta.isEmpty();
    }

    /**
     * Checks whether this content is text content.
     *
     * @return {@code true} if text content
     */
    default boolean isText() {
        return getType() == McpContentType.TEXT;
    }

    /**
     * Checks whether this content is image content.
     *
     * @return {@code true} if image content
     */
    default boolean isImage() {
        return getType() == McpContentType.IMAGE;
    }

    /**
     * Checks whether this content is audio content.
     *
     * @return {@code true} if audio content
     */
    default boolean isAudio() {
        return getType() == McpContentType.AUDIO;
    }

    /**
     * Checks whether this content is a resource link.
     *
     * @return {@code true} if resource link content
     */
    default boolean isResourceLink() {
        return getType() == McpContentType.RESOURCE_LINK;
    }

    /**
     * Checks whether this content is an embedded resource.
     *
     * @return {@code true} if embedded resource content
     */
    default boolean isResource() {
        return getType() == McpContentType.RESOURCE;
    }
}