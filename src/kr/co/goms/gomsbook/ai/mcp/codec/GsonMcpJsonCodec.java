/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.codec;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import kr.co.goms.gomsbook.ai.mcp.core.McpClientInfo;
import kr.co.goms.gomsbook.ai.mcp.core.McpRequest;
import kr.co.goms.gomsbook.ai.mcp.core.McpRequestMetadata;
import kr.co.goms.gomsbook.ai.mcp.core.McpResponse;
import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpClientCapabilities;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpDiscoverResult;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpRequestId;
import kr.co.goms.gomsbook.ai.mcp.server.McpServerCapabilities;

/**
 * Gson based MCP JSON codec.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This codec handles MCP wire-format differences that cannot be
 * represented correctly through plain Gson reflection alone.
 * </p>
 */
public final class GsonMcpJsonCodec
        implements McpJsonCodec {

    /*
     * ------------------------------------------------------------
     * JSON-RPC fields
     * ------------------------------------------------------------
     */

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

    private static final String FIELD_META =
            "_meta";

    private static final String FIELD_RESULT_TYPE =
            "resultType";


    /*
     * ------------------------------------------------------------
     * Internal McpResult fields
     * ------------------------------------------------------------
     */

    private static final String FIELD_SERVER_INFO_INTERNAL =
            "serverInfo";

    private static final String FIELD_ADDITIONAL_METADATA_INTERNAL =
            "additionalMetadata";


    /*
     * ------------------------------------------------------------
     * Capability fields
     * ------------------------------------------------------------
     */

    private static final String FIELD_CAPABILITIES =
            "capabilities";

    private static final String FIELD_COMPLETIONS =
            "completions";

    private static final String FIELD_PROMPTS =
            "prompts";

    private static final String FIELD_RESOURCES =
            "resources";

    private static final String FIELD_TOOLS =
            "tools";

    private static final String FIELD_SAMPLING =
            "sampling";

    private static final String FIELD_ELICITATION =
            "elicitation";

    private static final String FIELD_FORM =
            "form";

    private static final String FIELD_URL =
            "url";

    private static final String FIELD_LIST_CHANGED =
            "listChanged";

    private static final String FIELD_SUBSCRIBE =
            "subscribe";

    private static final String FIELD_EXTENSIONS =
            "extensions";

    private static final String FIELD_EXPERIMENTAL =
            "experimental";


    private static final Type MAP_TYPE =
            new TypeToken<Map<String, Object>>() {
            }.getType();


    private final Gson gson;


    /*
     * ------------------------------------------------------------
     * Constructor
     * ------------------------------------------------------------
     */

    public GsonMcpJsonCodec() {

        this(
                createDefaultGson()
        );
    }


    /**
     * Creates the codec using an externally configured Gson
     * instance.
     *
     * <p>
     * This is used by {@code McpServerComponentFactory} when
     * existing MCP polymorphic adapters such as content and
     * completion-reference adapters are registered.
     * </p>
     *
     * @param gson Gson instance
     */
    public GsonMcpJsonCodec(
            Gson gson
    ) {

        this.gson =
                Objects.requireNonNull(
                        gson,
                        "Gson must not be null."
                );
    }


    /*
     * ------------------------------------------------------------
     * Decode request
     * ------------------------------------------------------------
     */

    @Override
    public McpRequest decodeRequest(
            String json
    ) {

        String source =
                requireJson(
                        json
                );

        final JsonElement rootElement;

        try {

            rootElement =
                    JsonParser.parseString(
                            source
                    );

        } catch (JsonSyntaxException exception) {

            throw McpJsonCodecException.parseError(
                    "Failed to parse MCP JSON request.",
                    exception
            );

        } catch (JsonParseException exception) {

            throw McpJsonCodecException.parseError(
                    "Failed to parse MCP JSON request.",
                    exception
            );
        }


        if (rootElement == null
                || !rootElement.isJsonObject()) {

            throw McpJsonCodecException.invalidRequest(
                    "MCP JSON-RPC request must be a JSON object."
            );
        }


        JsonObject root =
                rootElement.getAsJsonObject();


        /*
         * --------------------------------------------------------
         * JSON-RPC envelope
         * --------------------------------------------------------
         */

        String jsonrpc =
                requireEnvelopeString(
                        root,
                        FIELD_JSONRPC
                );

        if (!McpRequest.JSON_RPC_VERSION.equals(
                jsonrpc
        )) {

            throw McpJsonCodecException.invalidRequest(
                    "Unsupported JSON-RPC version: "
                            + jsonrpc
            );
        }


        McpRequestId id =
                parseRequestId(
                        root.get(
                                FIELD_ID
                        )
                );


        String method =
                requireEnvelopeString(
                        root,
                        FIELD_METHOD
                );


        /*
         * --------------------------------------------------------
         * MCP params
         * --------------------------------------------------------
         */

        JsonObject paramsObject =
                parseParamsObject(
                        root
                );


        McpRequestMetadata metadata =
                parseRequestMetadata(
                        paramsObject
                );


        Map<String, Object> params =
                parseMethodParams(
                        paramsObject
                );


        try {

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
                    .metadata(
                            metadata
                    )
                    .build();

        } catch (IllegalArgumentException exception) {

            throw McpJsonCodecException.invalidRequest(
                    "Invalid MCP request: "
                            + safeMessage(
                                    exception
                            ),
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Encode request
     * ------------------------------------------------------------
     */

    @Override
    public String encodeRequest(
            McpRequest request
    ) {

        Objects.requireNonNull(
                request,
                "MCP request must not be null."
        );

        try {

            JsonObject root =
                    new JsonObject();

            root.addProperty(
                    FIELD_JSONRPC,
                    request.getJsonrpc()
            );


            if (request.getId() != null) {

                root.add(
                        FIELD_ID,
                        requestIdToJson(
                                request.getId()
                        )
                );
            }


            root.addProperty(
                    FIELD_METHOD,
                    request.getMethod()
            );


            JsonObject params =
                    paramsToJson(
                            request.getParams()
                    );


            params.add(
                    FIELD_META,
                    requestMetadataToJson(
                            request.getMetadata()
                    )
            );


            root.add(
                    FIELD_PARAMS,
                    params
            );


            return gson.toJson(
                    root
            );

        } catch (McpJsonCodecException exception) {

            throw exception;

        } catch (RuntimeException exception) {

            throw McpJsonCodecException.serializationError(
                    "Failed to encode MCP request.",
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Encode response
     * ------------------------------------------------------------
     */

    @Override
    public String encodeResponse(
            McpResponse response
    ) {

        Objects.requireNonNull(
                response,
                "MCP response must not be null."
        );

        try {

            JsonObject root =
                    new JsonObject();


            root.addProperty(
                    FIELD_JSONRPC,
                    response.getJsonrpc()
            );


            if (response.getId() != null) {

                root.add(
                        FIELD_ID,
                        requestIdToJson(
                                response.getId()
                        )
                );
            }


            if (response.isSuccess()) {

            	McpResult result = response.getMcpResult();
            	
            	 if (result == null) {

            	        throw McpJsonCodecException.serializationError(
            	                "MCP successful response must contain "
            	                        + "an McpResult."
            	        );
            	    }

            	 
                root.add(
                        FIELD_RESULT,
                        resultToJson(
                               result
                        )
                );

            } else {

                root.add(
                        FIELD_ERROR,
                        gson.toJsonTree(
                                response.getError()
                        )
                );
            }


            return gson.toJson(
                    root
            );

        } catch (McpJsonCodecException exception) {

            throw exception;

        } catch (RuntimeException exception) {

            throw McpJsonCodecException.serializationError(
                    "Failed to encode MCP response.",
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Params conversion
     * ------------------------------------------------------------
     */

    @Override
    public <T> T convertParams(
            Map<String, Object> params,
            Class<T> type
    ) {

        Objects.requireNonNull(
                type,
                "Target parameter type must not be null."
        );


        Map<String, Object> source =
                params == null
                        ? Collections.emptyMap()
                        : params;


        try {

            return gson.fromJson(
                    gson.toJsonTree(
                            source
                    ),
                    type
            );

        } catch (JsonParseException exception) {

            throw McpJsonCodecException.conversionError(
                    "Failed to convert MCP request params to "
                            + type.getName()
                            + ".",
                    exception
            );

        } catch (RuntimeException exception) {

            throw McpJsonCodecException.conversionError(
                    "Failed to convert MCP request params to "
                            + type.getName()
                            + ".",
                    exception
            );
        }
    }


    @Override
    public <T> T convert(
            Object source,
            Class<T> type
    ) {

        Objects.requireNonNull(
                type,
                "Target type must not be null."
        );


        if (source == null) {
            return null;
        }


        try {

            return gson.fromJson(
                    gson.toJsonTree(
                            source
                    ),
                    type
            );

        } catch (JsonParseException exception) {

            throw McpJsonCodecException.conversionError(
                    "Failed to convert MCP value to "
                            + type.getName()
                            + ".",
                    exception
            );

        } catch (RuntimeException exception) {

            throw McpJsonCodecException.conversionError(
                    "Failed to convert MCP value to "
                            + type.getName()
                            + ".",
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Request metadata decoding
     * ------------------------------------------------------------
     */

    private McpRequestMetadata parseRequestMetadata(
            JsonObject params
    ) {

        JsonElement metadataElement =
                params.get(
                        FIELD_META
                );


        if (metadataElement == null
                || metadataElement.isJsonNull()) {

            throw McpJsonCodecException.invalidParams(
                    "MCP request params must contain '_meta'."
            );
        }


        if (!metadataElement.isJsonObject()) {

            throw McpJsonCodecException.invalidParams(
                    "MCP request '_meta' must be an object."
            );
        }


        JsonObject metadata =
                metadataElement.getAsJsonObject();


        /*
         * --------------------------------------------------------
         * protocolVersion
         * --------------------------------------------------------
         */

        String protocolVersion =
                requireMetadataString(
                        metadata,
                        McpRequestMetadata.KEY_PROTOCOL_VERSION
                );


        /*
         * --------------------------------------------------------
         * clientCapabilities
         * --------------------------------------------------------
         */

        JsonElement capabilitiesElement =
                metadata.get(
                        McpRequestMetadata.KEY_CLIENT_CAPABILITIES
                );


        if (capabilitiesElement == null
                || capabilitiesElement.isJsonNull()) {

            throw McpJsonCodecException.invalidParams(
                    "MCP request metadata must contain "
                            + "'"
                            + McpRequestMetadata.KEY_CLIENT_CAPABILITIES
                            + "'."
            );
        }


        if (!capabilitiesElement.isJsonObject()) {

            throw McpJsonCodecException.invalidParams(
                    "MCP clientCapabilities metadata "
                            + "must be an object."
            );
        }


        McpClientCapabilities clientCapabilities =
                parseClientCapabilities(
                        capabilitiesElement
                                .getAsJsonObject()
                );


        McpRequestMetadata.Builder builder =
                McpRequestMetadata.builder()
                        .protocolVersion(
                                protocolVersion
                        )
                        .clientCapabilities(
                                clientCapabilities
                        );


        /*
         * --------------------------------------------------------
         * clientInfo
         * --------------------------------------------------------
         */

        JsonElement clientInfoElement =
                metadata.get(
                        McpRequestMetadata.KEY_CLIENT_INFO
                );


        if (clientInfoElement != null
                && !clientInfoElement.isJsonNull()) {

            if (!clientInfoElement.isJsonObject()) {

                throw McpJsonCodecException.invalidParams(
                        "MCP clientInfo metadata must be an object."
                );
            }


            builder.clientInfo(
                    parseClientInfo(
                            clientInfoElement
                                    .getAsJsonObject()
                    )
            );
        }


        /*
         * --------------------------------------------------------
         * logLevel
         * --------------------------------------------------------
         */

        JsonElement logLevelElement =
                metadata.get(
                        McpRequestMetadata.KEY_LOG_LEVEL
                );


        if (logLevelElement != null
                && !logLevelElement.isJsonNull()) {

            if (!logLevelElement.isJsonPrimitive()
                    || !logLevelElement
                            .getAsJsonPrimitive()
                            .isString()) {

                throw McpJsonCodecException.invalidParams(
                        "MCP logLevel metadata must be a string."
                );
            }


            builder.logLevel(
                    logLevelElement.getAsString()
            );
        }


        /*
         * --------------------------------------------------------
         * Additional metadata
         * --------------------------------------------------------
         */

        for (Map.Entry<String, JsonElement> entry
                : metadata.entrySet()) {

            String key =
                    entry.getKey();


            if (McpRequestMetadata.isReservedKey(
                    key
            )) {

                continue;
            }


            Object value =
                    gson.fromJson(
                            entry.getValue(),
                            Object.class
                    );


            builder.metadata(
                    key,
                    value
            );
        }


        try {

            return builder.build();

        } catch (IllegalArgumentException exception) {

            throw McpJsonCodecException.invalidParams(
                    "Invalid MCP request metadata: "
                            + safeMessage(
                                    exception
                            ),
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Client info
     * ------------------------------------------------------------
     */

    private McpClientInfo parseClientInfo(
            JsonObject json
    ) {

        String name =
                requireMetadataString(
                        json,
                        "name"
                );


        String version =
                requireMetadataString(
                        json,
                        "version"
                );


        McpClientInfo.Builder builder =
                McpClientInfo.builder()
                        .name(
                                name
                        )
                        .version(
                                version
                        );


        addOptionalString(
                json,
                "title",
                builder::title
        );


        addOptionalString(
                json,
                "description",
                builder::description
        );


        addOptionalString(
                json,
                "websiteUrl",
                builder::websiteUrl
        );


        try {

            return builder.build();

        } catch (IllegalArgumentException exception) {

            throw McpJsonCodecException.invalidParams(
                    "Invalid MCP clientInfo: "
                            + safeMessage(
                                    exception
                            ),
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Method params decoding
     * ------------------------------------------------------------
     */

    private Map<String, Object> parseMethodParams(
            JsonObject params
    ) {

        JsonObject copy =
                new JsonObject();


        for (Map.Entry<String, JsonElement> entry
                : params.entrySet()) {

            if (FIELD_META.equals(
                    entry.getKey()
            )) {

                continue;
            }


            copy.add(
                    entry.getKey(),
                    entry.getValue()
            );
        }


        if (copy.entrySet().isEmpty()) {

            return Collections.emptyMap();
        }


        try {

            Map<String, Object> result =
                    gson.fromJson(
                            copy,
                            MAP_TYPE
                    );


            if (result == null
                    || result.isEmpty()) {

                return Collections.emptyMap();
            }


            return Collections.unmodifiableMap(
                    new LinkedHashMap<>(
                            result
                    )
            );

        } catch (JsonParseException exception) {

            throw McpJsonCodecException.invalidParams(
                    "Failed to decode MCP request params.",
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Params object
     * ------------------------------------------------------------
     */

    private static JsonObject parseParamsObject(
            JsonObject root
    ) {

        JsonElement paramsElement =
                root.get(
                        FIELD_PARAMS
                );


        if (paramsElement == null
                || paramsElement.isJsonNull()) {

            throw McpJsonCodecException.invalidParams(
                    "MCP request must contain a params object."
            );
        }


        if (!paramsElement.isJsonObject()) {

            throw McpJsonCodecException.invalidParams(
                    "MCP request params must be a JSON object."
            );
        }


        return paramsElement.getAsJsonObject();
    }


    /*
     * ------------------------------------------------------------
     * Request metadata encoding
     * ------------------------------------------------------------
     */

    private JsonObject requestMetadataToJson(
            McpRequestMetadata metadata
    ) {

        Objects.requireNonNull(
                metadata,
                "MCP request metadata must not be null."
        );


        JsonObject json =
                new JsonObject();


        json.addProperty(
                McpRequestMetadata.KEY_PROTOCOL_VERSION,
                metadata.getProtocolVersion()
        );


        json.add(
                McpRequestMetadata.KEY_CLIENT_CAPABILITIES,
                clientCapabilitiesToJson(
                        metadata.getClientCapabilities()
                )
        );


        if (metadata.getClientInfo() != null) {

            json.add(
                    McpRequestMetadata.KEY_CLIENT_INFO,
                    gson.toJsonTree(
                            metadata.getClientInfo()
                    )
            );
        }


        if (metadata.getLogLevel() != null) {

            json.addProperty(
                    McpRequestMetadata.KEY_LOG_LEVEL,
                    metadata.getLogLevel()
            );
        }


        for (Map.Entry<String, Object> entry
                : metadata
                        .getAdditionalMetadata()
                        .entrySet()) {

            json.add(
                    entry.getKey(),
                    gson.toJsonTree(
                            entry.getValue()
                    )
            );
        }


        return json;
    }


    /*
     * ------------------------------------------------------------
     * Method params encoding
     * ------------------------------------------------------------
     */

    private JsonObject paramsToJson(
            Map<String, Object> params
    ) {

        JsonObject json =
                new JsonObject();


        if (params == null
                || params.isEmpty()) {

            return json;
        }


        for (Map.Entry<String, Object> entry
                : params.entrySet()) {

            if (FIELD_META.equals(
                    entry.getKey()
            )) {

                throw McpJsonCodecException.serializationError(
                        "MCP '_meta' must be managed through "
                                + "McpRequestMetadata."
                );
            }


            json.add(
                    entry.getKey(),
                    gson.toJsonTree(
                            entry.getValue()
                    )
            );
        }


        return json;
    }


    /*
     * ------------------------------------------------------------
     * Result encoding
     * ------------------------------------------------------------
     */

    private JsonObject resultToJson(
            McpResult result
    ) {

        Objects.requireNonNull(
                result,
                "MCP result must not be null."
        );


        JsonElement element;

        try {

            element =
                    gson.toJsonTree(
                            result,
                            result.getClass()
                    );

        } catch (RuntimeException exception) {

            throw McpJsonCodecException.serializationError(
                    "Failed to serialize MCP result.",
                    exception
            );
        }


        if (!element.isJsonObject()) {

            throw McpJsonCodecException.serializationError(
                    "MCP result must serialize as a JSON object."
            );
        }


        JsonObject json =
                element.getAsJsonObject();


        /*
         * McpResultType is a Java value object but the MCP wire
         * format requires a plain string.
         */
        json.addProperty(
                FIELD_RESULT_TYPE,
                result
                        .getResultType()
                        .value()
        );


        /*
         * Remove Java implementation fields.
         */
        json.remove(
                FIELD_SERVER_INFO_INTERNAL
        );

        json.remove(
                FIELD_ADDITIONAL_METADATA_INTERNAL
        );


        /*
         * --------------------------------------------------------
         * Result _meta
         * --------------------------------------------------------
         */

        Map<String, Object> metadata =
                result.metadataAsMap();


        if (metadata.isEmpty()) {

            json.remove(
                    FIELD_META
            );

        } else {

            JsonObject metadataJson =
                    new JsonObject();


            for (Map.Entry<String, Object> entry
                    : metadata.entrySet()) {

                metadataJson.add(
                        entry.getKey(),
                        gson.toJsonTree(
                                entry.getValue()
                        )
                );
            }


            json.add(
                    FIELD_META,
                    metadataJson
            );
        }


        /*
         * Discovery capabilities require MCP-specific encoding.
         */
        if (result instanceof McpDiscoverResult) {

            McpDiscoverResult discover =
                    (McpDiscoverResult) result;


            json.add(
                    FIELD_CAPABILITIES,
                    serverCapabilitiesToJson(
                            discover.getCapabilities()
                    )
            );
        }


        return json;
    }


    /*
     * ------------------------------------------------------------
     * Request id
     * ------------------------------------------------------------
     */

    private static McpRequestId parseRequestId(
            JsonElement element
    ) {

        if (element == null
                || element.isJsonNull()) {

            return null;
        }


        if (!element.isJsonPrimitive()) {

            throw McpJsonCodecException.invalidRequest(
                    "JSON-RPC id must be a string or integer."
            );
        }


        JsonPrimitive primitive =
                element.getAsJsonPrimitive();


        if (primitive.isString()) {

            try {

                return McpRequestId.of(
                        primitive.getAsString()
                );

            } catch (IllegalArgumentException exception) {

                throw McpJsonCodecException.invalidRequest(
                        "Invalid JSON-RPC string id.",
                        exception
                );
            }
        }


        if (!primitive.isNumber()) {

            throw McpJsonCodecException.invalidRequest(
                    "JSON-RPC id must be a string or integer."
            );
        }


        String raw =
                primitive.getAsString();


        try {

            long value =
                    Long.parseLong(
                            raw
                    );


            return McpRequestId.of(
                    value
            );

        } catch (NumberFormatException exception) {

            throw McpJsonCodecException.invalidRequest(
                    "JSON-RPC numeric id must be an integer: "
                            + raw,
                    exception
            );
        }
    }


    private static JsonElement requestIdToJson(
            Object id
    ) {

        Objects.requireNonNull(
                id,
                "MCP request id must not be null."
        );

        if (id instanceof String) {

            String value =
                    ((String) id).trim();

            if (value.isEmpty()) {

                throw McpJsonCodecException.serializationError(
                        "MCP request string id must not be blank."
                );
            }

            return new JsonPrimitive(
                    value
            );
        }

        if (id instanceof Byte
                || id instanceof Short
                || id instanceof Integer
                || id instanceof Long) {

            return new JsonPrimitive(
                    (Number) id
            );
        }

        throw McpJsonCodecException.serializationError(
                "MCP request id must be a string or integer."
        );
    }

    /*
     * ------------------------------------------------------------
     * Client capabilities decoding
     * ------------------------------------------------------------
     */

    private McpClientCapabilities parseClientCapabilities(
            JsonObject json
    ) {

        McpClientCapabilities.Builder builder =
                McpClientCapabilities.builder();

        /*
         * --------------------------------------------------------
         * elicitation
         * --------------------------------------------------------
         */

        JsonElement elicitationElement =
                json.get(
                        FIELD_ELICITATION
                );


        if (elicitationElement != null
                && !elicitationElement.isJsonNull()) {

            JsonObject elicitation =
                    requireParamsObject(
                            elicitationElement,
                            FIELD_ELICITATION
                    );


            boolean form =
                    hasCapabilityObject(
                            elicitation,
                            FIELD_FORM,
                            "elicitation.form"
                    );


            boolean url =
                    hasCapabilityObject(
                            elicitation,
                            FIELD_URL,
                            "elicitation.url"
                    );


            if (form && url) {

                builder.elicitation(
                        McpClientCapabilities
                                .ElicitationCapability
                                .builder()
                                .form()
                                .url()
                                .build()
                );

            } else if (url) {

                builder.elicitation(
                        McpClientCapabilities
                                .ElicitationCapability
                                .builder()
                                .url()
                                .build()
                );

            } else {

                /*
                 * Empty elicitation object represents baseline
                 * form support.
                 */
                builder.elicitation(
                        McpClientCapabilities
                                .ElicitationCapability
                                .form()
                );
            }
        }


        /*
         * --------------------------------------------------------
         * experimental / extensions
         * --------------------------------------------------------
         */

        readCapabilityMap(
                json,
                FIELD_EXPERIMENTAL,
                (name, configuration) ->
                        builder.experimental(
                                name,
                                configuration
                        )
        );


        readCapabilityMap(
                json,
                FIELD_EXTENSIONS,
                (name, configuration) ->
                        builder.extension(
                                name,
                                configuration
                        )
        );


        try {

            return builder.build();

        } catch (IllegalArgumentException exception) {

            throw McpJsonCodecException.invalidParams(
                    "Invalid MCP client capabilities: "
                            + safeMessage(
                                    exception
                            ),
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Client capabilities encoding
     * ------------------------------------------------------------
     */

    private JsonObject clientCapabilitiesToJson(
            McpClientCapabilities capabilities
    ) {

        Objects.requireNonNull(
                capabilities,
                "MCP client capabilities must not be null."
        );


        JsonObject json =
                new JsonObject();


        if (capabilities.supportsSampling()) {

            JsonObject sampling =
                    new JsonObject();


            if (capabilities.supportsSamplingTools()) {

                sampling.add(
                        FIELD_TOOLS,
                        new JsonObject()
                );
            }


            json.add(
                    FIELD_SAMPLING,
                    sampling
            );
        }


        if (capabilities.supportsElicitation()) {

            JsonObject elicitation =
                    new JsonObject();


            if (capabilities.supportsElicitationForm()) {

                elicitation.add(
                        FIELD_FORM,
                        new JsonObject()
                );
            }


            if (capabilities.supportsElicitationUrl()) {

                elicitation.add(
                        FIELD_URL,
                        new JsonObject()
                );
            }


            json.add(
                    FIELD_ELICITATION,
                    elicitation
            );
        }


        writeCapabilityMap(
                json,
                FIELD_EXPERIMENTAL,
                capabilities.getExperimental()
        );


        writeCapabilityMap(
                json,
                FIELD_EXTENSIONS,
                capabilities.getExtensions()
        );


        return json;
    }


    /*
     * ------------------------------------------------------------
     * Server capabilities encoding
     * ------------------------------------------------------------
     */

    private JsonObject serverCapabilitiesToJson(
            McpServerCapabilities capabilities
    ) {

        Objects.requireNonNull(
                capabilities,
                "MCP server capabilities must not be null."
        );


        JsonObject json =
                new JsonObject();


        if (capabilities.supportsCompletions()) {

            json.add(
                    FIELD_COMPLETIONS,
                    new JsonObject()
            );
        }


        if (capabilities.supportsPrompts()) {

            JsonObject prompts =
                    new JsonObject();


            if (capabilities.supportsPromptListChanged()) {

                prompts.addProperty(
                        FIELD_LIST_CHANGED,
                        true
                );
            }


            json.add(
                    FIELD_PROMPTS,
                    prompts
            );
        }


        if (capabilities.supportsResources()) {

            JsonObject resources =
                    new JsonObject();


            if (capabilities.supportsResourceSubscribe()) {

                resources.addProperty(
                        FIELD_SUBSCRIBE,
                        true
                );
            }


            if (capabilities.supportsResourceListChanged()) {

                resources.addProperty(
                        FIELD_LIST_CHANGED,
                        true
                );
            }


            json.add(
                    FIELD_RESOURCES,
                    resources
            );
        }


        if (capabilities.supportsTools()) {

            JsonObject tools =
                    new JsonObject();


            if (capabilities.supportsToolListChanged()) {

                tools.addProperty(
                        FIELD_LIST_CHANGED,
                        true
                );
            }


            json.add(
                    FIELD_TOOLS,
                    tools
            );
        }


        writeCapabilityMap(
                json,
                FIELD_EXTENSIONS,
                capabilities.getExtensions()
        );


        writeCapabilityMap(
                json,
                FIELD_EXPERIMENTAL,
                capabilities.getExperimental()
        );


        return json;
    }


    /*
     * ------------------------------------------------------------
     * Capability map
     * ------------------------------------------------------------
     */

    private void writeCapabilityMap(
            JsonObject target,
            String property,
            Map<String, Map<String, Object>> values
    ) {

        if (values == null
                || values.isEmpty()) {

            return;
        }


        JsonObject json =
                new JsonObject();


        for (Map.Entry<String, Map<String, Object>> entry
                : values.entrySet()) {

            json.add(
                    entry.getKey(),
                    gson.toJsonTree(
                            entry.getValue()
                    )
            );
        }


        target.add(
                property,
                json
        );
    }


    private void readCapabilityMap(
            JsonObject source,
            String property,
            CapabilityConsumer consumer
    ) {

        JsonElement element =
                source.get(
                        property
                );


        if (element == null
                || element.isJsonNull()) {

            return;
        }


        JsonObject capabilityObject =
                requireParamsObject(
                        element,
                        property
                );


        for (Map.Entry<String, JsonElement> entry
                : capabilityObject.entrySet()) {

            JsonObject configuration =
                    requireParamsObject(
                            entry.getValue(),
                            property
                                    + "."
                                    + entry.getKey()
                    );


            Map<String, Object> config =
                    gson.fromJson(
                            configuration,
                            MAP_TYPE
                    );


            consumer.accept(
                    entry.getKey(),
                    config == null
                            ? Collections.emptyMap()
                            : config
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * JSON validation utilities
     * ------------------------------------------------------------
     */

    private static String requireJson(
            String json
    ) {

        if (json == null) {

            throw McpJsonCodecException.parseError(
                    "MCP JSON must not be null."
            );
        }


        String normalized =
                json.trim();


        if (normalized.isEmpty()) {

            throw McpJsonCodecException.parseError(
                    "MCP JSON must not be blank."
            );
        }


        return normalized;
    }


    private static String requireEnvelopeString(
            JsonObject object,
            String property
    ) {

        JsonElement element =
                object.get(
                        property
                );


        if (element == null
                || element.isJsonNull()
                || !element.isJsonPrimitive()
                || !element
                        .getAsJsonPrimitive()
                        .isString()) {

            throw McpJsonCodecException.invalidRequest(
                    "Required JSON-RPC property '"
                            + property
                            + "' must be a string."
            );
        }


        String value =
                element
                        .getAsString()
                        .trim();


        if (value.isEmpty()) {

            throw McpJsonCodecException.invalidRequest(
                    "Required JSON-RPC property '"
                            + property
                            + "' must not be blank."
            );
        }


        return value;
    }


    private static String requireMetadataString(
            JsonObject object,
            String property
    ) {

        JsonElement element =
                object.get(
                        property
                );


        if (element == null
                || element.isJsonNull()
                || !element.isJsonPrimitive()
                || !element
                        .getAsJsonPrimitive()
                        .isString()) {

            throw McpJsonCodecException.invalidParams(
                    "Required MCP metadata property '"
                            + property
                            + "' must be a string."
            );
        }


        String value =
                element
                        .getAsString()
                        .trim();


        if (value.isEmpty()) {

            throw McpJsonCodecException.invalidParams(
                    "Required MCP metadata property '"
                            + property
                            + "' must not be blank."
            );
        }


        return value;
    }


    private static JsonObject requireParamsObject(
            JsonElement element,
            String property
    ) {

        if (element == null
                || element.isJsonNull()
                || !element.isJsonObject()) {

            throw McpJsonCodecException.invalidParams(
                    "MCP property '"
                            + property
                            + "' must be an object."
            );
        }


        return element.getAsJsonObject();
    }


    private static boolean hasCapabilityObject(
            JsonObject parent,
            String property,
            String path
    ) {

        JsonElement element =
                parent.get(
                        property
                );


        if (element == null
                || element.isJsonNull()) {

            return false;
        }


        requireParamsObject(
                element,
                path
        );


        return true;
    }


    private static void addOptionalString(
            JsonObject json,
            String property,
            StringConsumer consumer
    ) {

        JsonElement element =
                json.get(
                        property
                );


        if (element == null
                || element.isJsonNull()) {

            return;
        }


        if (!element.isJsonPrimitive()
                || !element
                        .getAsJsonPrimitive()
                        .isString()) {

            throw McpJsonCodecException.invalidParams(
                    "MCP property '"
                            + property
                            + "' must be a string."
            );
        }


        consumer.accept(
                element.getAsString()
        );
    }


    /*
     * ------------------------------------------------------------
     * Gson
     * ------------------------------------------------------------
     */

    private static Gson createDefaultGson() {

        return new GsonBuilder()
                .disableHtmlEscaping()
                .create();
    }


    public Gson getGson() {
        return gson;
    }


    /*
     * ------------------------------------------------------------
     * Utility
     * ------------------------------------------------------------
     */

    private static String safeMessage(
            Throwable throwable
    ) {

        if (throwable == null) {
            return "Unknown error";
        }


        String message =
                throwable.getMessage();


        if (message == null
                || message.trim().isEmpty()) {

            return throwable
                    .getClass()
                    .getSimpleName();
        }


        return message.trim();
    }


    /*
     * ------------------------------------------------------------
     * Internal functional interfaces
     * ------------------------------------------------------------
     */

    @FunctionalInterface
    private interface CapabilityConsumer {

        void accept(
                String name,
                Map<String, Object> configuration
        );
    }


    @FunctionalInterface
    private interface StringConsumer {

        void accept(
                String value
        );
    }
}