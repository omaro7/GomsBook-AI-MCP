/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.protocol;

import kr.co.goms.gomsbook.ai.mcp.core.McpNotification;
import kr.co.goms.gomsbook.ai.mcp.core.McpRequest;
import kr.co.goms.gomsbook.ai.mcp.core.McpResponse;

/**
 * Codec for MCP(Model Context Protocol) JSON-RPC messages.
 *
 * <p>
 * Supports the three JSON-RPC message categories used
 * by MCP:
 * </p>
 *
 * <ul>
 *     <li>Request</li>
 *     <li>Response</li>
 *     <li>Notification</li>
 * </ul>
 *
 * <p>
 * Implementations are responsible only for message
 * serialization, deserialization, and message-type
 * detection. Transport and protocol dispatching logic
 * should remain outside this interface.
 * </p>
 */
public interface McpMessageCodec {

    /*
     * Request.
     */

    /**
     * Encodes an MCP request.
     *
     * @param request MCP request
     * @return JSON string
     */
    String encodeRequest(
            McpRequest request
    );


    /**
     * Decodes JSON into an MCP request.
     *
     * @param json JSON string
     * @return MCP request
     */
    McpRequest decodeRequest(
            String json
    );


    /*
     * Response.
     */

    /**
     * Encodes an MCP response.
     *
     * @param response MCP response
     * @return JSON string
     */
    String encodeResponse(
            McpResponse response
    );


    /**
     * Decodes JSON into an MCP response.
     *
     * @param json JSON string
     * @return MCP response
     */
    McpResponse decodeResponse(
            String json
    );


    /*
     * Notification.
     */

    /**
     * Encodes an MCP notification.
     *
     * @param notification MCP notification
     * @return JSON string
     */
    String encodeNotification(
            McpNotification notification
    );


    /**
     * Decodes JSON into an MCP notification.
     *
     * @param json JSON string
     * @return MCP notification
     */
    McpNotification decodeNotification(
            String json
    );


    /*
     * Message type detection.
     */

    /**
     * Checks whether JSON represents an MCP request.
     *
     * <p>
     * A request contains:
     * </p>
     *
     * <ul>
     *     <li>jsonrpc = 2.0</li>
     *     <li>id</li>
     *     <li>method</li>
     * </ul>
     *
     * @param json JSON string
     * @return {@code true} when request
     */
    boolean isRequest(
            String json
    );


    /**
     * Checks whether JSON represents an MCP response.
     *
     * <p>
     * A response contains an id and exactly one of
     * result or error.
     * </p>
     *
     * @param json JSON string
     * @return {@code true} when response
     */
    boolean isResponse(
            String json
    );


    /**
     * Checks whether JSON represents an MCP notification.
     *
     * <p>
     * A notification contains a method but does not
     * contain an id.
     * </p>
     *
     * @param json JSON string
     * @return {@code true} when notification
     */
    boolean isNotification(
            String json
    );


    /**
     * Checks whether JSON represents any supported
     * MCP JSON-RPC message.
     *
     * @param json JSON string
     * @return {@code true} when request, response,
     *         or notification
     */
    default boolean isMessage(
            String json) {

        return isRequest(json)
                || isResponse(json)
                || isNotification(json);
    }
}