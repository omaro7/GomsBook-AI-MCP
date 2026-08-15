/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import kr.co.goms.gomsbook.ai.mcp.common.McpAnnotations;
import kr.co.goms.gomsbook.ai.mcp.common.McpIcon;
import kr.co.goms.gomsbook.ai.mcp.content.McpAudioContent;
import kr.co.goms.gomsbook.ai.mcp.content.McpContent;
import kr.co.goms.gomsbook.ai.mcp.content.McpContentType;
import kr.co.goms.gomsbook.ai.mcp.content.McpEmbeddedResourceContent;
import kr.co.goms.gomsbook.ai.mcp.content.McpImageContent;
import kr.co.goms.gomsbook.ai.mcp.content.McpResourceLinkContent;
import kr.co.goms.gomsbook.ai.mcp.content.McpTextContent;
import kr.co.goms.gomsbook.ai.mcp.resources.McpResourceContents;

/**
 * Gson type adapter factory for MCP content polymorphism.
 *
 * <p>
 * MCP content blocks use the {@code type} field as a discriminator.
 * This factory maps the discriminator to the corresponding
 * {@link McpContent} implementation.
 * </p>
 *
 * <p>
 * Deserialization intentionally uses each content model's builder
 * so that validation and normalization logic is preserved.
 * </p>
 */
public final class McpContentTypeAdapterFactory
        implements TypeAdapterFactory {

    private static final Type META_TYPE =
            new TypeToken<Map<String, Object>>() {
            }.getType();

    private static final Type ICON_LIST_TYPE =
            new TypeToken<List<McpIcon>>() {
            }.getType();

    @Override
    public <T> TypeAdapter<T> create(
            Gson gson,
            TypeToken<T> type
    ) {

        if (type.getRawType()
                != McpContent.class) {

            return null;
        }

        TypeAdapter<McpTextContent> textAdapter =
                gson.getDelegateAdapter(
                        this,
                        TypeToken.get(
                                McpTextContent.class
                        )
                );

        TypeAdapter<McpImageContent> imageAdapter =
                gson.getDelegateAdapter(
                        this,
                        TypeToken.get(
                                McpImageContent.class
                        )
                );

        TypeAdapter<McpAudioContent> audioAdapter =
                gson.getDelegateAdapter(
                        this,
                        TypeToken.get(
                                McpAudioContent.class
                        )
                );

        TypeAdapter<McpResourceLinkContent> resourceLinkAdapter =
                gson.getDelegateAdapter(
                        this,
                        TypeToken.get(
                                McpResourceLinkContent.class
                        )
                );

        TypeAdapter<McpEmbeddedResourceContent> embeddedResourceAdapter =
                gson.getDelegateAdapter(
                        this,
                        TypeToken.get(
                                McpEmbeddedResourceContent.class
                        )
                );

        TypeAdapter<McpContent> adapter =
                new TypeAdapter<>() {

                    @Override
                    public void write(
                            JsonWriter out,
                            McpContent value
                    ) throws IOException {

                        writeContent(
                                out,
                                value,
                                textAdapter,
                                imageAdapter,
                                audioAdapter,
                                resourceLinkAdapter,
                                embeddedResourceAdapter
                        );
                    }

                    @Override
                    public McpContent read(
                            JsonReader in
                    ) throws IOException {

                        JsonElement element =
                                Streams.parse(
                                        in
                                );

                        if (element == null
                                || element.isJsonNull()) {

                            return null;
                        }

                        if (!element.isJsonObject()) {
                            throw new JsonParseException(
                                    "MCP content must be a JSON object."
                            );
                        }

                        JsonObject object =
                                element.getAsJsonObject();

                        McpContentType contentType =
                                readContentType(
                                        object
                                );

                        try {

                            switch (contentType) {

                                case TEXT:
                                    return readTextContent(
                                            gson,
                                            object
                                    );

                                case IMAGE:
                                    return readImageContent(
                                            gson,
                                            object
                                    );

                                case AUDIO:
                                    return readAudioContent(
                                            gson,
                                            object
                                    );

                                case RESOURCE_LINK:
                                    return readResourceLinkContent(
                                            gson,
                                            object
                                    );

                                case RESOURCE:
                                    return readEmbeddedResourceContent(
                                            gson,
                                            object
                                    );

                                default:
                                    throw new JsonParseException(
                                            "Unsupported MCP content type: "
                                                    + contentType
                                    );
                            }

                        } catch (IllegalArgumentException
                                | NullPointerException exception) {

                            throw new JsonParseException(
                                    "Invalid MCP content for type '"
                                            + contentType.getValue()
                                            + "'.",
                                    exception
                            );
                        }
                    }
                };

        @SuppressWarnings("unchecked")
        TypeAdapter<T> result =
                (TypeAdapter<T>) adapter.nullSafe();

        return result;
    }

    private static void writeContent(
            JsonWriter out,
            McpContent value,
            TypeAdapter<McpTextContent> textAdapter,
            TypeAdapter<McpImageContent> imageAdapter,
            TypeAdapter<McpAudioContent> audioAdapter,
            TypeAdapter<McpResourceLinkContent> resourceLinkAdapter,
            TypeAdapter<McpEmbeddedResourceContent> embeddedResourceAdapter
    ) throws IOException {

        if (value == null) {

            out.nullValue();
            return;
        }

        McpContentType contentType =
                value.getType();

        if (contentType == null) {
            throw new JsonParseException(
                    "MCP content type must not be null."
            );
        }

        switch (contentType) {

            case TEXT:

                requireImplementation(
                        value,
                        McpTextContent.class
                );

                textAdapter.write(
                        out,
                        (McpTextContent) value
                );

                return;

            case IMAGE:

                requireImplementation(
                        value,
                        McpImageContent.class
                );

                imageAdapter.write(
                        out,
                        (McpImageContent) value
                );

                return;

            case AUDIO:

                requireImplementation(
                        value,
                        McpAudioContent.class
                );

                audioAdapter.write(
                        out,
                        (McpAudioContent) value
                );

                return;

            case RESOURCE_LINK:

                requireImplementation(
                        value,
                        McpResourceLinkContent.class
                );

                resourceLinkAdapter.write(
                        out,
                        (McpResourceLinkContent) value
                );

                return;

            case RESOURCE:

                requireImplementation(
                        value,
                        McpEmbeddedResourceContent.class
                );

                embeddedResourceAdapter.write(
                        out,
                        (McpEmbeddedResourceContent) value
                );

                return;

            default:
                throw new JsonParseException(
                        "Unsupported MCP content type: "
                                + contentType
                );
        }
    }

    private static McpContentType readContentType(
            JsonObject object
    ) {

        JsonElement typeElement =
                object.get(
                        "type"
                );

        if (typeElement == null
                || typeElement.isJsonNull()) {

            throw new JsonParseException(
                    "MCP content type is required."
            );
        }

        if (!typeElement.isJsonPrimitive()
                || !typeElement
                        .getAsJsonPrimitive()
                        .isString()) {

            throw new JsonParseException(
                    "MCP content type must be a string."
            );
        }

        String value =
                typeElement.getAsString();

        try {

            return McpContentType.fromValue(
                    value
            );

        } catch (IllegalArgumentException exception) {

            throw new JsonParseException(
                    "Unsupported MCP content type: "
                            + value,
                    exception
            );
        }
    }

    private static McpTextContent readTextContent(
            Gson gson,
            JsonObject object
    ) {

        McpTextContent.Builder builder =
                McpTextContent.builder()
                        .text(
                                requireString(
                                        object,
                                        "text"
                                )
                        );

        applyAnnotations(
                gson,
                object,
                builder
        );

        applyMeta(
                gson,
                object,
                builder
        );

        return builder.build();
    }

    private static McpImageContent readImageContent(
            Gson gson,
            JsonObject object
    ) {

        McpImageContent.Builder builder =
                McpImageContent.builder()
                        .data(
                                requireString(
                                        object,
                                        "data"
                                )
                        )
                        .mimeType(
                                requireString(
                                        object,
                                        "mimeType"
                                )
                        );

        applyAnnotations(
                gson,
                object,
                builder
        );

        applyMeta(
                gson,
                object,
                builder
        );

        return builder.build();
    }

    private static McpAudioContent readAudioContent(
            Gson gson,
            JsonObject object
    ) {

        McpAudioContent.Builder builder =
                McpAudioContent.builder()
                        .data(
                                requireString(
                                        object,
                                        "data"
                                )
                        )
                        .mimeType(
                                requireString(
                                        object,
                                        "mimeType"
                                )
                        );

        applyAnnotations(
                gson,
                object,
                builder
        );

        applyMeta(
                gson,
                object,
                builder
        );

        return builder.build();
    }

    private static McpResourceLinkContent readResourceLinkContent(
            Gson gson,
            JsonObject object
    ) {

        McpResourceLinkContent.Builder builder =
                McpResourceLinkContent.builder()
                        .name(
                                requireString(
                                        object,
                                        "name"
                                )
                        )
                        .uri(
                                requireString(
                                        object,
                                        "uri"
                                )
                        );

        String title =
                optionalString(
                        object,
                        "title"
                );

        if (title != null) {

            builder.title(
                    title
            );
        }

        String description =
                optionalString(
                        object,
                        "description"
                );

        if (description != null) {

            builder.description(
                    description
            );
        }

        String mimeType =
                optionalString(
                        object,
                        "mimeType"
                );

        if (mimeType != null) {

            builder.mimeType(
                    mimeType
            );
        }

        Long size =
                optionalLong(
                        object,
                        "size"
                );

        if (size != null) {

            builder.size(
                    size
            );
        }

        JsonElement iconsElement =
                object.get(
                        "icons"
                );

        if (iconsElement != null
                && !iconsElement.isJsonNull()) {

            List<McpIcon> icons =
                    gson.fromJson(
                            iconsElement,
                            ICON_LIST_TYPE
                    );

            if (icons != null) {

                builder.icons(
                        icons
                );
            }
        }

        applyAnnotations(
                gson,
                object,
                builder
        );

        applyMeta(
                gson,
                object,
                builder
        );

        return builder.build();
    }

    private static McpEmbeddedResourceContent readEmbeddedResourceContent(
            Gson gson,
            JsonObject object
    ) {

        JsonElement resourceElement =
                object.get(
                        "resource"
                );

        if (resourceElement == null
                || resourceElement.isJsonNull()) {

            throw new JsonParseException(
                    "MCP embedded resource 'resource' is required."
            );
        }

        if (!resourceElement.isJsonObject()) {
            throw new JsonParseException(
                    "MCP embedded resource 'resource' "
                            + "must be a JSON object."
            );
        }

        McpResourceContents resource =
                gson.fromJson(
                        resourceElement,
                        McpResourceContents.class
                );

        if (resource == null) {
            throw new JsonParseException(
                    "Failed to deserialize MCP embedded resource."
            );
        }

        McpEmbeddedResourceContent.Builder builder =
                McpEmbeddedResourceContent.builder()
                        .resource(
                                resource
                        );

        applyAnnotations(
                gson,
                object,
                builder
        );

        applyMeta(
                gson,
                object,
                builder
        );

        return builder.build();
    }

    private static String requireString(
            JsonObject object,
            String fieldName
    ) {

        JsonElement element =
                object.get(
                        fieldName
                );

        if (element == null
                || element.isJsonNull()) {

            throw new JsonParseException(
                    "MCP content field '"
                            + fieldName
                            + "' is required."
            );
        }

        if (!element.isJsonPrimitive()
                || !element
                        .getAsJsonPrimitive()
                        .isString()) {

            throw new JsonParseException(
                    "MCP content field '"
                            + fieldName
                            + "' must be a string."
            );
        }

        return element.getAsString();
    }

    private static String optionalString(
            JsonObject object,
            String fieldName
    ) {

        JsonElement element =
                object.get(
                        fieldName
                );

        if (element == null
                || element.isJsonNull()) {

            return null;
        }

        if (!element.isJsonPrimitive()
                || !element
                        .getAsJsonPrimitive()
                        .isString()) {

            throw new JsonParseException(
                    "MCP content field '"
                            + fieldName
                            + "' must be a string."
            );
        }

        return element.getAsString();
    }

    private static Long optionalLong(
            JsonObject object,
            String fieldName
    ) {

        JsonElement element =
                object.get(
                        fieldName
                );

        if (element == null
                || element.isJsonNull()) {

            return null;
        }

        if (!element.isJsonPrimitive()
                || !element
                        .getAsJsonPrimitive()
                        .isNumber()) {

            throw new JsonParseException(
                    "MCP content field '"
                            + fieldName
                            + "' must be a number."
            );
        }

        try {

            return element.getAsLong();

        } catch (NumberFormatException exception) {

            throw new JsonParseException(
                    "MCP content field '"
                            + fieldName
                            + "' must be an integer.",
                    exception
            );
        }
    }

    private static McpAnnotations readAnnotations(
            Gson gson,
            JsonObject object
    ) {

        JsonElement annotationsElement =
                object.get(
                        "annotations"
                );

        if (annotationsElement == null
                || annotationsElement.isJsonNull()) {

            return null;
        }

        if (!annotationsElement.isJsonObject()) {
            throw new JsonParseException(
                    "MCP content annotations must be a JSON object."
            );
        }

        return gson.fromJson(
                annotationsElement,
                McpAnnotations.class
        );
    }

    private static Map<String, Object> readMeta(
            Gson gson,
            JsonObject object
    ) {

        JsonElement metaElement =
                object.get(
                        "_meta"
                );

        if (metaElement == null
                || metaElement.isJsonNull()) {

            return null;
        }

        if (!metaElement.isJsonObject()) {
            throw new JsonParseException(
                    "MCP content _meta must be a JSON object."
            );
        }

        return gson.fromJson(
                metaElement,
                META_TYPE
        );
    }

    private static void applyAnnotations(
            Gson gson,
            JsonObject object,
            McpTextContent.Builder builder
    ) {

        McpAnnotations annotations =
                readAnnotations(
                        gson,
                        object
                );

        if (annotations != null) {

            builder.annotations(
                    annotations
            );
        }
    }

    private static void applyAnnotations(
            Gson gson,
            JsonObject object,
            McpImageContent.Builder builder
    ) {

        McpAnnotations annotations =
                readAnnotations(
                        gson,
                        object
                );

        if (annotations != null) {

            builder.annotations(
                    annotations
            );
        }
    }

    private static void applyAnnotations(
            Gson gson,
            JsonObject object,
            McpAudioContent.Builder builder
    ) {

        McpAnnotations annotations =
                readAnnotations(
                        gson,
                        object
                );

        if (annotations != null) {

            builder.annotations(
                    annotations
            );
        }
    }

    private static void applyAnnotations(
            Gson gson,
            JsonObject object,
            McpResourceLinkContent.Builder builder
    ) {

        McpAnnotations annotations =
                readAnnotations(
                        gson,
                        object
                );

        if (annotations != null) {

            builder.annotations(
                    annotations
            );
        }
    }

    private static void applyAnnotations(
            Gson gson,
            JsonObject object,
            McpEmbeddedResourceContent.Builder builder
    ) {

        McpAnnotations annotations =
                readAnnotations(
                        gson,
                        object
                );

        if (annotations != null) {

            builder.annotations(
                    annotations
            );
        }
    }

    private static void applyMeta(
            Gson gson,
            JsonObject object,
            McpTextContent.Builder builder
    ) {

        Map<String, Object> meta =
                readMeta(
                        gson,
                        object
                );

        if (meta != null) {

            builder.meta(
                    meta
            );
        }
    }

    private static void applyMeta(
            Gson gson,
            JsonObject object,
            McpImageContent.Builder builder
    ) {

        Map<String, Object> meta =
                readMeta(
                        gson,
                        object
                );

        if (meta != null) {

            builder.meta(
                    meta
            );
        }
    }

    private static void applyMeta(
            Gson gson,
            JsonObject object,
            McpAudioContent.Builder builder
    ) {

        Map<String, Object> meta =
                readMeta(
                        gson,
                        object
                );

        if (meta != null) {

            builder.meta(
                    meta
            );
        }
    }

    private static void applyMeta(
            Gson gson,
            JsonObject object,
            McpResourceLinkContent.Builder builder
    ) {

        Map<String, Object> meta =
                readMeta(
                        gson,
                        object
                );

        if (meta != null) {

            builder.meta(
                    meta
            );
        }
    }

    private static void applyMeta(
            Gson gson,
            JsonObject object,
            McpEmbeddedResourceContent.Builder builder
    ) {

        Map<String, Object> meta =
                readMeta(
                        gson,
                        object
                );

        if (meta != null) {

            builder.meta(
                    meta
            );
        }
    }

    private static void requireImplementation(
            McpContent value,
            Class<?> expectedType
    ) {

        if (!expectedType.isInstance(
                value
        )) {

            throw new JsonParseException(
                    "MCP content type mismatch. "
                            + "discriminator="
                            + value.getType()
                            + ", actual="
                            + value.getClass().getName()
                            + ", expected="
                            + expectedType.getName()
            );
        }
    }
}