/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.codec;

import java.util.Map;

import kr.co.goms.gomsbook.ai.mcp.core.McpRequest;
import kr.co.goms.gomsbook.ai.mcp.core.McpResponse;

/**
 * JSON codec contract for MCP messages.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * The codec is responsible for converting between serialized
 * JSON-RPC messages and MCP protocol objects.
 * </p>
 */
public interface McpJsonCodec {

    /**
     * Decodes a serialized JSON-RPC request.
     *
     * @param json serialized JSON request
     * @return MCP request
     *
     * @throws McpJsonCodecException
     *         if the JSON cannot be parsed or converted
     */
    McpRequest decodeRequest(
            String json
    );

    /**
     * Encodes an MCP request as JSON.
     *
     * <p>
     * Standard MCP request metadata is serialized under
     * {@code params._meta}.
     * </p>
     *
     * @param request MCP request
     * @return serialized JSON
     *
     * @throws McpJsonCodecException
     *         if serialization fails
     */
    String encodeRequest(
            McpRequest request
    );

    /**
     * Encodes an MCP response as JSON.
     *
     * @param response MCP response
     * @return serialized JSON
     *
     * @throws McpJsonCodecException
     *         if serialization fails
     */
    String encodeResponse(
            McpResponse response
    );

    /**
     * Converts raw request parameters to a concrete MCP
     * parameter type.
     *
     * <p>
     * The {@code _meta} member is not included in the returned
     * parameter object because request metadata is represented
     * separately by {@code McpRequestMetadata}.
     * </p>
     *
     * @param params raw request parameters
     * @param type target parameter type
     * @param <T> parameter type
     * @return converted parameter object
     *
     * @throws McpJsonCodecException
     *         if conversion fails
     */
    <T> T convertParams(
            Map<String, Object> params,
            Class<T> type
    );

    /**
     * Converts an arbitrary Java object to another model type
     * using the codec's JSON mapping rules.
     *
     * <p>
     * This is primarily intended for protocol adapters and
     * request handlers where a previously decoded generic
     * structure must be converted to a strongly typed model.
     * </p>
     *
     * @param source source object
     * @param type target type
     * @param <T> target type
     * @return converted object
     *
     * @throws McpJsonCodecException
     *         if conversion fails
     */
    <T> T convert(
            Object source,
            Class<T> type
    );
}