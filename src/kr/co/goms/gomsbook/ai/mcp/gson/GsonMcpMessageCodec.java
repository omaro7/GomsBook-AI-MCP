/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.gson;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import kr.co.goms.gomsbook.ai.mcp.core.McpError;
import kr.co.goms.gomsbook.ai.mcp.core.McpNotification;
import kr.co.goms.gomsbook.ai.mcp.core.McpRequest;
import kr.co.goms.gomsbook.ai.mcp.core.McpResponse;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpMessageCodec;

/**
 * Gson based MCP JSON-RPC message codec.
 *
 * <p>
 * Supports:
 * </p>
 *
 * <ul>
 *     <li>MCP Request</li>
 *     <li>MCP Response</li>
 *     <li>MCP Notification</li>
 * </ul>
 *
 * <p>
 * Numeric JSON-RPC identifiers are explicitly restored
 * as integral Java values to avoid Gson 2.8.9 converting
 * generic numbers into {@link Double}.
 * </p>
 */
public final class GsonMcpMessageCodec
        implements McpMessageCodec {

    private static final String JSON_RPC_VERSION =
            "2.0";

    private static final String FIELD_JSONRPC =
            "jsonrpc";

    private static final String FIELD_ID =
            "id";

    private static final String FIELD_METHOD =
            "method";

    private static final String FIELD_PARAMS =
            "params";

    private static final String FIELD_RESULT =
            "result";

    private static final String FIELD_ERROR =
            "error";


    private final Gson gson;


    public GsonMcpMessageCodec(
            Gson gson) {

        this.gson =
                Objects.requireNonNull(
                        gson,
                        "Gson must not be null."
                );
    }


    /*
     * Request.
     */

    @Override
    public String encodeRequest(
            McpRequest request) {

        Objects.requireNonNull(
                request,
                "MCP request must not be null."
        );

        request.validate();

        return gson.toJson(
                request
        );
    }


    @Override
    public McpRequest decodeRequest(
            String json) {

        JsonObject object =
                parseObject(
                        json
                );

        validateJsonRpc(
                object
        );

        if (!object.has(
                FIELD_ID)) {

            throw new IllegalArgumentException(
                    "MCP request id is required."
            );
        }

        if (!object.has(
                FIELD_METHOD)) {

            throw new IllegalArgumentException(
                    "MCP request method is required."
            );
        }

        Object id =
                parseId(
                        object.get(
                                FIELD_ID
                        ),
                        false
                );

        String method =
                readRequiredString(
                        object,
                        FIELD_METHOD
                );

        Map<String, Object> params =
                readObjectMap(
                        object,
                        FIELD_PARAMS,
                        true
                );

        return McpRequest.builder()
                .id(
                        id
                )
                .method(
                        method
                )
                .params(
                        params
                )
                .build();
    }


    /*
     * Response.
     */

    @Override
    public String encodeResponse(
            McpResponse response) {

        Objects.requireNonNull(
                response,
                "MCP response must not be null."
        );

        response.validate();

        return gson.toJson(
                response
        );
    }


    @Override
    public McpResponse decodeResponse(
            String json) {

        JsonObject object =
                parseObject(
                        json
                );

        validateJsonRpc(
                object
        );

        if (!object.has(
                FIELD_ID)) {

            throw new IllegalArgumentException(
                    "MCP response id is required."
            );
        }

        Object id =
                parseId(
                        object.get(
                                FIELD_ID
                        ),
                        true
                );

        boolean hasResult =
                object.has(
                        FIELD_RESULT
                );

        boolean hasError =
                object.has(
                        FIELD_ERROR
                );

        if (hasResult == hasError) {

            throw new IllegalArgumentException(
                    "MCP response must contain exactly "
                            + "one of result or error."
            );
        }

        McpResponse.Builder builder =
                McpResponse.builder()
                        .id(
                                id
                        );

        if (hasResult) {

            builder.result(
                    toJavaValue(
                            object.get(
                                    FIELD_RESULT
                            )
                    )
            );

        } else {

            JsonElement errorElement =
                    object.get(
                            FIELD_ERROR
                    );

            if (errorElement == null
                    || errorElement.isJsonNull()
                    || !errorElement.isJsonObject()) {

                throw new IllegalArgumentException(
                        "MCP response error must be "
                                + "a JSON object."
                );
            }

            McpError error =
                    gson.fromJson(
                            errorElement,
                            McpError.class
                    );

            if (error == null) {

                throw new IllegalArgumentException(
                        "Unable to decode MCP error."
                );
            }

            builder.error(
                    error
            );
        }

        return builder.build();
    }


    /*
     * Notification.
     */

    @Override
    public String encodeNotification(
            McpNotification notification) {

        Objects.requireNonNull(
                notification,
                "MCP notification must not be null."
        );

        notification.validate();

        return gson.toJson(
                notification
        );
    }


    @Override
    public McpNotification decodeNotification(
            String json) {

        JsonObject object =
                parseObject(
                        json
                );

        validateJsonRpc(
                object
        );

        if (object.has(
                FIELD_ID)) {

            throw new IllegalArgumentException(
                    "MCP notification must not contain id."
            );
        }

        String method =
                readRequiredString(
                        object,
                        FIELD_METHOD
                );

        Map<String, Object> params =
                readObjectMap(
                        object,
                        FIELD_PARAMS,
                        true
                );

        return McpNotification.builder()
                .method(
                        method
                )
                .params(
                        params
                )
                .build();
    }


    /*
     * Message type detection.
     */

    @Override
    public boolean isRequest(
            String json) {

        try {

            JsonObject object =
                    parseObject(
                            json
                    );

            if (!isJsonRpc20(
                    object)) {

                return false;
            }

            return object.has(
                    FIELD_ID
            )
                    && object.has(
                            FIELD_METHOD
                    )
                    && !object.has(
                            FIELD_RESULT
                    )
                    && !object.has(
                            FIELD_ERROR
                    );

        } catch (RuntimeException exception) {

            return false;
        }
    }


    @Override
    public boolean isResponse(
            String json) {

        try {

            JsonObject object =
                    parseObject(
                            json
                    );

            if (!isJsonRpc20(
                    object)) {

                return false;
            }

            if (!object.has(
                    FIELD_ID)) {

                return false;
            }

            if (object.has(
                    FIELD_METHOD)) {

                return false;
            }

            boolean hasResult =
                    object.has(
                            FIELD_RESULT
                    );

            boolean hasError =
                    object.has(
                            FIELD_ERROR
                    );

            return hasResult != hasError;

        } catch (RuntimeException exception) {

            return false;
        }
    }


    @Override
    public boolean isNotification(
            String json) {

        try {

            JsonObject object =
                    parseObject(
                            json
                    );

            if (!isJsonRpc20(
                    object)) {

                return false;
            }

            return !object.has(
                    FIELD_ID
            )
                    && object.has(
                            FIELD_METHOD
                    )
                    && !object.has(
                            FIELD_RESULT
                    )
                    && !object.has(
                            FIELD_ERROR
                    );

        } catch (RuntimeException exception) {

            return false;
        }
    }


    /*
     * Parsing helpers.
     */

    private JsonObject parseObject(
            String json) {

        if (json == null
                || json.isBlank()) {

            throw new IllegalArgumentException(
                    "MCP JSON must not be blank."
            );
        }

        try {

            /*
             * Gson 2.8.9 compatible parsing.
             */
            JsonElement element =
                    new JsonParser()
                            .parse(
                                    json
                            );

            if (element == null
                    || element.isJsonNull()
                    || !element.isJsonObject()) {

                throw new IllegalArgumentException(
                        "MCP message must be "
                                + "a JSON object."
                );
            }

            return element.getAsJsonObject();

        } catch (JsonSyntaxException exception) {

            throw new IllegalArgumentException(
                    "Invalid MCP JSON.",
                    exception
            );
        }
    }


    private void validateJsonRpc(
            JsonObject object) {

        if (!isJsonRpc20(
                object)) {

            throw new IllegalArgumentException(
                    "Unsupported JSON-RPC version."
            );
        }
    }


    private boolean isJsonRpc20(
            JsonObject object) {

        if (object == null
                || !object.has(
                        FIELD_JSONRPC
                )) {

            return false;
        }

        JsonElement element =
                object.get(
                        FIELD_JSONRPC
                );

        if (element == null
                || element.isJsonNull()
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive()
                        .isString()) {

            return false;
        }

        return JSON_RPC_VERSION.equals(
                element.getAsString()
        );
    }


    /**
     * Parses a JSON-RPC id.
     *
     * <p>
     * MCP request identifiers must be strings or
     * integral numbers. Response identifiers may also
     * be null when an incoming request id could not
     * be determined.
     * </p>
     */
    private Object parseId(
            JsonElement element,
            boolean allowNull) {

        if (element == null
                || element.isJsonNull()) {

            if (allowNull) {

                return null;
            }

            throw new IllegalArgumentException(
                    "MCP request id must not be null."
            );
        }

        if (!element.isJsonPrimitive()) {

            throw new IllegalArgumentException(
                    "MCP id must be a string "
                            + "or integer."
            );
        }

        if (element.getAsJsonPrimitive()
                .isString()) {

            String value =
                    element.getAsString();

            if (value == null
                    || value.isBlank()) {

                throw new IllegalArgumentException(
                        "MCP id must not be blank."
                );
            }

            return value;
        }

        if (element.getAsJsonPrimitive()
                .isNumber()) {

            String value =
                    element.getAsString();

            try {

                return Long.valueOf(
                        value
                );

            } catch (NumberFormatException exception) {

                throw new IllegalArgumentException(
                        "MCP numeric id must "
                                + "be an integer: "
                                + value,
                        exception
                );
            }
        }

        throw new IllegalArgumentException(
                "MCP id must be a string "
                        + "or integer."
        );
    }


    private String readRequiredString(
            JsonObject object,
            String fieldName) {

        if (!object.has(
                fieldName)) {

            throw new IllegalArgumentException(
                    "Required MCP property "
                            + "is missing: "
                            + fieldName
            );
        }

        JsonElement element =
                object.get(
                        fieldName
                );

        if (element == null
                || element.isJsonNull()
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive()
                        .isString()) {

            throw new IllegalArgumentException(
                    "MCP property must be "
                            + "a string: "
                            + fieldName
            );
        }

        String value =
                element.getAsString();

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    "MCP property must not "
                            + "be blank: "
                            + fieldName
            );
        }

        return value.trim();
    }


    /**
     * Reads a JSON object into a Java Map.
     *
     * <p>
     * Nested objects such as params._meta are preserved
     * as nested maps. This is important because MCP
     * 2026-07-28 carries protocol metadata inside
     * params._meta on every request.
     * </p>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> readObjectMap(
            JsonObject object,
            String fieldName,
            boolean optional) {

        if (!object.has(
                fieldName)
                || object.get(
                        fieldName
                ) == null
                || object.get(
                        fieldName
                ).isJsonNull()) {

            if (optional) {

                return new LinkedHashMap<>();
            }

            throw new IllegalArgumentException(
                    "Required MCP object "
                            + "is missing: "
                            + fieldName
            );
        }

        JsonElement element =
                object.get(
                        fieldName
                );

        if (!element.isJsonObject()) {

            throw new IllegalArgumentException(
                    "MCP property must be "
                            + "a JSON object: "
                            + fieldName
            );
        }

        Map<String, Object> map =
                gson.fromJson(
                        element,
                        LinkedHashMap.class
                );

        if (map == null) {

            return new LinkedHashMap<>();
        }

        return map;
    }


    /**
     * Converts arbitrary JSON into a Java value.
     */
    private Object toJavaValue(
            JsonElement element) {

        if (element == null
                || element.isJsonNull()) {

            return null;
        }

        return gson.fromJson(
                element,
                Object.class
        );
    }
}