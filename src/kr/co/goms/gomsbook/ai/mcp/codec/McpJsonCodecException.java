/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.codec;

import kr.co.goms.gomsbook.ai.mcp.protocol.McpRequestId;

/**
 * Indicates an MCP JSON serialization or deserialization failure.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This exception belongs to the codec layer and is not itself
 * an MCP or JSON-RPC error response.
 * </p>
 *
 * <p>
 * When an inbound request identifier has already been decoded
 * before the codec failure occurs, the identifier may be
 * preserved in this exception. This allows the server transport
 * bridge to return an error response associated with the
 * original JSON-RPC request.
 * </p>
 */
public final class McpJsonCodecException
        extends RuntimeException {

    private static final long serialVersionUID =
            1L;

    private final ErrorType errorType;

    private final McpRequestId requestId;


    /*
     * ------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------
     */

    public McpJsonCodecException(
            String message
    ) {

        this(
                ErrorType.CONVERSION,
                null,
                message,
                null
        );
    }


    public McpJsonCodecException(
            String message,
            Throwable cause
    ) {

        this(
                ErrorType.CONVERSION,
                null,
                message,
                cause
        );
    }


    public McpJsonCodecException(
            ErrorType errorType,
            String message
    ) {

        this(
                errorType,
                null,
                message,
                null
        );
    }


    public McpJsonCodecException(
            ErrorType errorType,
            String message,
            Throwable cause
    ) {

        this(
                errorType,
                null,
                message,
                cause
        );
    }


    public McpJsonCodecException(
            ErrorType errorType,
            McpRequestId requestId,
            String message
    ) {

        this(
                errorType,
                requestId,
                message,
                null
        );
    }


    public McpJsonCodecException(
            ErrorType errorType,
            McpRequestId requestId,
            String message,
            Throwable cause
    ) {

        super(
                requireMessage(
                        message
                ),
                cause
        );

        if (errorType == null) {

            throw new IllegalArgumentException(
                    "MCP JSON codec error type must not be null."
            );
        }

        this.errorType =
                errorType;

        this.requestId =
                requestId;
    }


    /*
     * ------------------------------------------------------------
     * Parse error
     * ------------------------------------------------------------
     */

    public static McpJsonCodecException parseError(
            String message
    ) {

        return new McpJsonCodecException(
                ErrorType.PARSE,
                null,
                message
        );
    }


    public static McpJsonCodecException parseError(
            String message,
            Throwable cause
    ) {

        return new McpJsonCodecException(
                ErrorType.PARSE,
                null,
                message,
                cause
        );
    }


    /*
     * ------------------------------------------------------------
     * Invalid Request
     * ------------------------------------------------------------
     */

    public static McpJsonCodecException invalidRequest(
            String message
    ) {

        return new McpJsonCodecException(
                ErrorType.INVALID_REQUEST,
                null,
                message
        );
    }


    public static McpJsonCodecException invalidRequest(
            String message,
            Throwable cause
    ) {

        return new McpJsonCodecException(
                ErrorType.INVALID_REQUEST,
                null,
                message,
                cause
        );
    }


    /**
     * Creates an invalid-request error while preserving a request
     * identifier that was successfully decoded.
     *
     * <p>
     * Use this only when the identifier itself was structurally
     * valid. If the request envelope cannot be trusted, omit the
     * identifier.
     * </p>
     */
    public static McpJsonCodecException invalidRequest(
            McpRequestId requestId,
            String message
    ) {

        return new McpJsonCodecException(
                ErrorType.INVALID_REQUEST,
                requestId,
                message
        );
    }


    public static McpJsonCodecException invalidRequest(
            McpRequestId requestId,
            String message,
            Throwable cause
    ) {

        return new McpJsonCodecException(
                ErrorType.INVALID_REQUEST,
                requestId,
                message,
                cause
        );
    }


    /*
     * ------------------------------------------------------------
     * Invalid params
     * ------------------------------------------------------------
     */

    public static McpJsonCodecException invalidParams(
            String message
    ) {

        return new McpJsonCodecException(
                ErrorType.INVALID_PARAMS,
                null,
                message
        );
    }


    public static McpJsonCodecException invalidParams(
            String message,
            Throwable cause
    ) {

        return new McpJsonCodecException(
                ErrorType.INVALID_PARAMS,
                null,
                message,
                cause
        );
    }


    /**
     * Creates an invalid-params error associated with a decoded
     * JSON-RPC request identifier.
     *
     * @param requestId decoded request id
     * @param message error message
     * @return codec exception
     */
    public static McpJsonCodecException invalidParams(
            McpRequestId requestId,
            String message
    ) {

        return new McpJsonCodecException(
                ErrorType.INVALID_PARAMS,
                requestId,
                message
        );
    }


    public static McpJsonCodecException invalidParams(
            McpRequestId requestId,
            String message,
            Throwable cause
    ) {

        return new McpJsonCodecException(
                ErrorType.INVALID_PARAMS,
                requestId,
                message,
                cause
        );
    }


    /*
     * ------------------------------------------------------------
     * Conversion
     * ------------------------------------------------------------
     */

    public static McpJsonCodecException conversionError(
            String message
    ) {

        return new McpJsonCodecException(
                ErrorType.CONVERSION,
                null,
                message
        );
    }


    public static McpJsonCodecException conversionError(
            String message,
            Throwable cause
    ) {

        return new McpJsonCodecException(
                ErrorType.CONVERSION,
                null,
                message,
                cause
        );
    }


    public static McpJsonCodecException conversionError(
            McpRequestId requestId,
            String message
    ) {

        return new McpJsonCodecException(
                ErrorType.CONVERSION,
                requestId,
                message
        );
    }


    public static McpJsonCodecException conversionError(
            McpRequestId requestId,
            String message,
            Throwable cause
    ) {

        return new McpJsonCodecException(
                ErrorType.CONVERSION,
                requestId,
                message,
                cause
        );
    }


    /*
     * ------------------------------------------------------------
     * Serialization
     * ------------------------------------------------------------
     */

    public static McpJsonCodecException serializationError(
            String message
    ) {

        return new McpJsonCodecException(
                ErrorType.SERIALIZATION,
                null,
                message
        );
    }


    public static McpJsonCodecException serializationError(
            String message,
            Throwable cause
    ) {

        return new McpJsonCodecException(
                ErrorType.SERIALIZATION,
                null,
                message,
                cause
        );
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public ErrorType getErrorType() {
        return errorType;
    }


    /**
     * Returns the JSON-RPC request id decoded before the codec
     * failure occurred.
     *
     * @return request id, or {@code null} if unavailable
     */
    public McpRequestId getRequestId() {
        return requestId;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasRequestId() {
        return requestId != null;
    }


    public boolean isParseError() {

        return errorType
                == ErrorType.PARSE;
    }


    public boolean isInvalidRequest() {

        return errorType
                == ErrorType.INVALID_REQUEST;
    }


    public boolean isInvalidParams() {

        return errorType
                == ErrorType.INVALID_PARAMS;
    }


    public boolean isConversionError() {

        return errorType
                == ErrorType.CONVERSION;
    }


    public boolean isSerializationError() {

        return errorType
                == ErrorType.SERIALIZATION;
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static String requireMessage(
            String message
    ) {

        if (message == null) {

            throw new IllegalArgumentException(
                    "MCP JSON codec error message must not be null."
            );
        }

        String normalized =
                message.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP JSON codec error message must not be blank."
            );
        }

        return normalized;
    }


    /*
     * ------------------------------------------------------------
     * Error type
     * ------------------------------------------------------------
     */

    public enum ErrorType {

        /**
         * Invalid JSON syntax.
         *
         * Maps to:
         * -32700 Parse error
         */
        PARSE,

        /**
         * Invalid JSON-RPC request envelope.
         *
         * Maps to:
         * -32600 Invalid Request
         */
        INVALID_REQUEST,

        /**
         * Invalid or missing MCP request parameters / metadata.
         *
         * Maps to:
         * -32602 Invalid params
         */
        INVALID_PARAMS,

        /**
         * Generic decoded data could not be converted to the
         * requested Java MCP model.
         */
        CONVERSION,

        /**
         * Outbound MCP data could not be serialized.
         */
        SERIALIZATION
    }
}