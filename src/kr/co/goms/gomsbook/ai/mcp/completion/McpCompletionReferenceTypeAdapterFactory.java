/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Gson type adapter factory for {@link McpCompletionReference}.
 *
 * <p>
 * MCP completion references are polymorphic and are distinguished
 * by the {@code type} property.
 * </p>
 *
 * <ul>
 *     <li>{@code ref/prompt} -> {@link McpPromptReference}</li>
 *     <li>{@code ref/resource} -> {@link McpResourceTemplateReference}</li>
 * </ul>
 */
public final class McpCompletionReferenceTypeAdapterFactory
        implements TypeAdapterFactory {

    private static final String FIELD_TYPE =
            "type";

    private static final String TYPE_PROMPT =
            McpPromptReference.TYPE;

    private static final String TYPE_RESOURCE =
            McpResourceTemplateReference.TYPE;

    /**
     * Creates a type adapter for {@link McpCompletionReference}.
     *
     * @param gson Gson instance
     * @param type requested type
     * @param <T> requested Java type
     * @return type adapter, or {@code null} when this factory
     *         does not support the requested type
     */
    @Override
    public <T> TypeAdapter<T> create(
            Gson gson,
            TypeToken<T> type) {

        if (!McpCompletionReference.class.equals(
                type.getRawType())) {

            return null;
        }

        TypeAdapter<McpPromptReference> promptAdapter =
                gson.getDelegateAdapter(
                        this,
                        TypeToken.get(
                                McpPromptReference.class
                        )
                );

        TypeAdapter<McpResourceTemplateReference> resourceAdapter =
                gson.getDelegateAdapter(
                        this,
                        TypeToken.get(
                                McpResourceTemplateReference.class
                        )
                );

        TypeAdapter<JsonElement> jsonElementAdapter =
                gson.getAdapter(
                        JsonElement.class
                );

        TypeAdapter<McpCompletionReference> adapter =
                new TypeAdapter<McpCompletionReference>() {

                    @Override
                    public void write(
                            JsonWriter out,
                            McpCompletionReference value)
                            throws IOException {

                        if (value == null) {

                            out.nullValue();
                            return;
                        }

                        if (value instanceof McpPromptReference) {

                            promptAdapter.write(
                                    out,
                                    (McpPromptReference) value
                            );

                            return;
                        }

                        if (value instanceof McpResourceTemplateReference) {

                            resourceAdapter.write(
                                    out,
                                    (McpResourceTemplateReference) value
                            );

                            return;
                        }

                        throw new JsonParseException(
                                "Unsupported MCP completion reference type: " +
                                        value.getClass().getName()
                        );
                    }

                    @Override
                    public McpCompletionReference read(
                            JsonReader in)
                            throws IOException {

                        JsonElement jsonElement =
                                jsonElementAdapter.read(
                                        in
                                );

                        if (jsonElement == null
                                || jsonElement.isJsonNull()) {

                            return null;
                        }

                        if (!jsonElement.isJsonObject()) {

                            throw new JsonParseException(
                                    "MCP completion reference must be a JSON object."
                            );
                        }

                        JsonObject jsonObject =
                                jsonElement.getAsJsonObject();

                        JsonElement typeElement =
                                jsonObject.get(
                                        FIELD_TYPE
                                );

                        if (typeElement == null
                                || typeElement.isJsonNull()) {

                            throw new JsonParseException(
                                    "MCP completion reference is missing required field 'type'."
                            );
                        }

                        if (!typeElement.isJsonPrimitive()
                                || !typeElement
                                        .getAsJsonPrimitive()
                                        .isString()) {

                            throw new JsonParseException(
                                    "MCP completion reference field 'type' must be a string."
                            );
                        }

                        String referenceType =
                                typeElement.getAsString();

                        switch (referenceType) {

                        case TYPE_PROMPT:

                            return promptAdapter.fromJsonTree(
                                    jsonObject
                            );

                        case TYPE_RESOURCE:

                            return resourceAdapter.fromJsonTree(
                                    jsonObject
                            );

                        default:

                            throw new JsonParseException(
                                    "Unsupported MCP completion reference type: " +
                                            referenceType
                            );
                        }
                    }
                };

        @SuppressWarnings("unchecked")
        TypeAdapter<T> result =
                (TypeAdapter<T>) adapter.nullSafe();

        return result;
    }
}