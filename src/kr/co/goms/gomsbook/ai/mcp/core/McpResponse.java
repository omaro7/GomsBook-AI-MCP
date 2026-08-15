/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.core;

import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.protocol.McpErrorCode;

/**
 * MCP(Model Context Protocol) JSON-RPC response.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Represents either:
 * </p>
 *
 * <ul>
 *     <li>a successful MCP result response</li>
 *     <li>an MCP JSON-RPC error response</li>
 * </ul>
 */
public final class McpResponse {

    public static final String JSON_RPC_VERSION =
            "2.0";

    private static final String FIELD_RESULT_TYPE =
            "resultType";


    private String jsonrpc;

    private Object id;

    private Object result;

    private McpError error;


    /*
     * ------------------------------------------------------------
     * Gson constructor
     * ------------------------------------------------------------
     */

    /**
     * Constructor for Gson deserialization.
     */
    public McpResponse() {

        this.jsonrpc =
                JSON_RPC_VERSION;
    }


    /*
     * ------------------------------------------------------------
     * Constructor
     * ------------------------------------------------------------
     */

    private McpResponse(
            Builder builder
    ) {

        this.jsonrpc =
                JSON_RPC_VERSION;

        this.id =
                builder.id;

        this.result =
                builder.result;

        this.error =
                builder.error;

        validate();
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static Builder builder() {

        return new Builder();
    }


    /*
     * ------------------------------------------------------------
     * Success
     * ------------------------------------------------------------
     */

    /**
     * Creates a successful MCP response.
     *
     * @param id request identifier
     * @param result MCP result
     * @return response
     */
    public static McpResponse success(
            Object id,
            Object result
    ) {

        return builder()
                .id(
                        id
                )
                .result(
                        result
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Generic error
     * ------------------------------------------------------------
     */

    /**
     * Creates an MCP error response.
     *
     * @param id request identifier
     * @param error MCP error
     * @return response
     */
    public static McpResponse failure(
            Object id,
            McpError error
    ) {

        return builder()
                .id(
                        id
                )
                .error(
                        Objects.requireNonNull(
                                error,
                                "MCP error must not be null."
                        )
                )
                .build();
    }


    /**
     * Alias for {@link #failure(Object, McpError)}.
     *
     * <p>
     * Server runtime code may use {@code error(...)} while
     * client-side code may use {@code failure(...)}.
     * Both produce the same JSON-RPC error response.
     * </p>
     */
    public static McpResponse error(
            Object id,
            McpError error
    ) {

        return failure(
                id,
                error
        );
    }


    /*
     * ------------------------------------------------------------
     * Parse error
     * ------------------------------------------------------------
     */

    /**
     * Creates JSON-RPC -32700 Parse error.
     *
     * <p>
     * Request id is unknown at parse time.
     * </p>
     */
    public static McpResponse parseError() {

        return failure(
                null,
                McpError.of(
                        McpErrorCode.PARSE_ERROR,
                        "Parse error",
                        null
                )
        );
    }


    public static McpResponse parseError(
            String message
    ) {

        return failure(
                null,
                McpError.of(
                        McpErrorCode.PARSE_ERROR,
                        normalizeMessage(
                                message,
                                "Parse error"
                        ),
                        null
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * Invalid Request
     * ------------------------------------------------------------
     */

    /**
     * Creates JSON-RPC -32600 Invalid Request.
     */
    public static McpResponse invalidRequest() {

        return failure(
                null,
                McpError.of(
                        McpErrorCode.INVALID_REQUEST,
                        "Invalid Request",
                        null
                )
        );
    }


    public static McpResponse invalidRequest(
            Object id
    ) {

        return failure(
                id,
                McpError.of(
                        McpErrorCode.INVALID_REQUEST,
                        "Invalid Request",
                        null
                )
        );
    }


    public static McpResponse invalidRequest(
            Object id,
            String message
    ) {

        return failure(
                id,
                McpError.of(
                        McpErrorCode.INVALID_REQUEST,
                        normalizeMessage(
                                message,
                                "Invalid Request"
                        ),
                        null
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * Method Not Found
     * ------------------------------------------------------------
     */

    /**
     * Creates JSON-RPC -32601 Method not found.
     */
    public static McpResponse methodNotFound(
            Object id,
            String method
    ) {

        String normalizedMethod =
                normalizeOptional(
                        method
                );

        String message =
                normalizedMethod == null
                        ? "Method not found"
                        : "MCP method not found: "
                                + normalizedMethod;

        return failure(
                id,
                McpError.of(
                        McpErrorCode.METHOD_NOT_FOUND,
                        message,
                        normalizedMethod == null
                                ? null
                                : Map.of(
                                        "method",
                                        normalizedMethod
                                )
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * Invalid Params
     * ------------------------------------------------------------
     */

    /**
     * Creates JSON-RPC -32602 Invalid params.
     */
    public static McpResponse invalidParams(
            Object id,
            String message
    ) {

        return failure(
                id,
                McpError.invalidParams(
                        normalizeMessage(
                                message,
                                "Invalid params"
                        ),
                        null
                )
        );
    }


    public static McpResponse invalidParams(
            Object id,
            String message,
            Object data
    ) {

        return failure(
                id,
                McpError.invalidParams(
                        normalizeMessage(
                                message,
                                "Invalid params"
                        ),
                        data
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * Internal Error
     * ------------------------------------------------------------
     */

    /**
     * Creates JSON-RPC -32603 Internal error.
     */
    public static McpResponse internalError(
            Object id,
            String message
    ) {

        return failure(
                id,
                McpError.of(
                        McpErrorCode.INTERNAL_ERROR,
                        normalizeMessage(
                                message,
                                "Internal MCP server error."
                        ),
                        null
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public String getJsonrpc() {

        return jsonrpc;
    }


    public Object getId() {

        return id;
    }


    public Object getResult() {

        return result;
    }


    public McpError getError() {

        return error;
    }


    public McpResult getMcpResult() {

        if (result instanceof McpResult) {

            return (McpResult) result;
        }

        return null;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean isSuccess() {

        return result != null
                && error == null;
    }


    public boolean isError() {

        return error != null;
    }


    public boolean hasResult() {

        return result != null;
    }


    public boolean hasError() {

        return error != null;
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    public void validate() {

        validateJsonRpc();

        validateResponseShape();


        if (result != null) {

            validateSuccessId(
                    id
            );

            validateResult(
                    result
            );

            return;
        }


        /*
         * Error responses may use null id when the request
         * identifier could not be determined.
         */
        validateErrorId(
                id
        );
    }


    private void validateJsonRpc() {

        if (!JSON_RPC_VERSION.equals(
                jsonrpc
        )) {

            throw new IllegalArgumentException(
                    "Invalid JSON-RPC version: "
                            + jsonrpc
            );
        }
    }


    /**
     * Response must contain exactly one of result or error.
     */
    private void validateResponseShape() {

        boolean resultPresent =
                result != null;

        boolean errorPresent =
                error != null;


        if (resultPresent == errorPresent) {

            throw new IllegalArgumentException(
                    "MCP response must contain exactly "
                            + "one of result or error."
            );
        }
    }


    private static void validateSuccessId(
            Object id
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "MCP successful response id "
                            + "must not be null."
            );
        }


        validateIdType(
                id
        );
    }


    private static void validateErrorId(
            Object id
    ) {

        if (id == null) {
            return;
        }


        validateIdType(
                id
        );
    }


    private static void validateIdType(
            Object id
    ) {

        if (id instanceof String) {

            if (((String) id).isBlank()) {

                throw new IllegalArgumentException(
                        "MCP response id must not be blank."
                );
            }

            return;
        }


        if (id instanceof Byte
                || id instanceof Short
                || id instanceof Integer
                || id instanceof Long) {

            return;
        }


        throw new IllegalArgumentException(
                "MCP response id must be "
                        + "a string or integer: "
                        + id.getClass().getName()
        );
    }


    /**
     * Validates a successful MCP result.
     */
    private static void validateResult(
            Object result
    ) {

        if (result instanceof McpResult) {

            McpResult mcpResult =
                    (McpResult) result;


            /*
             * McpResult validates resultType in its constructor.
             *
             * Do NOT call mcpResult.validate() here.
             */
            if (mcpResult.getResultType() == null) {

                throw new IllegalArgumentException(
                        "MCP resultType must not be null."
                );
            }

            return;
        }


        /*
         * Gson generic deserialization may represent result
         * objects as Map instances.
         */
        if (result instanceof Map) {

            validateResultMap(
                    (Map<?, ?>) result
            );

            return;
        }


        throw new IllegalArgumentException(
                "Unsupported MCP result object: "
                        + result.getClass()
                                .getName()
        );
    }


    /**
     * Validates a generic decoded result map.
     */
    private static void validateResultMap(
            Map<?, ?> result
    ) {

        Object resultType =
                result.get(
                        FIELD_RESULT_TYPE
                );


        if (!(resultType instanceof String)) {

            throw new IllegalArgumentException(
                    "MCP result must contain "
                            + "a string resultType."
            );
        }


        String value =
                ((String) resultType)
                        .trim();


        if (value.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP resultType must not be blank."
            );
        }


        /*
         * McpResultType is an extensible string value object.
         *
         * complete / input_required are core values, but
         * extension-defined result types are also preserved.
         */
        McpResultType.of(
                value
        );
    }


    /*
     * ------------------------------------------------------------
     * Utility
     * ------------------------------------------------------------
     */

    private static String normalizeMessage(
            String message,
            String defaultMessage
    ) {

        if (message == null
                || message.isBlank()) {

            return defaultMessage;
        }


        return message.trim();
    }


    private static String normalizeOptional(
            String value
    ) {

        if (value == null) {
            return null;
        }


        String normalized =
                value.trim();


        return normalized.isEmpty()
                ? null
                : normalized;
    }


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private Object id;

        private Object result;

        private McpError error;


        private Builder() {
        }


        public Builder id(
                Object id
        ) {

            this.id =
                    id;

            return this;
        }


        public Builder id(
                String id
        ) {

            this.id =
                    id;

            return this;
        }


        public Builder id(
                long id
        ) {

            this.id =
                    Long.valueOf(
                            id
                    );

            return this;
        }


        public Builder result(
                Object result
        ) {

            this.result =
                    result;

            return this;
        }


        public Builder error(
                McpError error
        ) {

            this.error =
                    error;

            return this;
        }


        public McpResponse build() {

            return new McpResponse(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "McpResponse{"
                + "jsonrpc='"
                + jsonrpc
                + '\''
                + ", id="
                + id
                + ", result="
                + result
                + ", error="
                + error
                + '}';
    }
}