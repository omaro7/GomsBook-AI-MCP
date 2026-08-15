/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import kr.co.goms.gomsbook.ai.mcp.content.McpTextContent;
import kr.co.goms.gomsbook.ai.tool.AgentTool;
import kr.co.goms.gomsbook.ai.tool.ToolContext;
import kr.co.goms.gomsbook.ai.tool.ToolIssue;
import kr.co.goms.gomsbook.ai.tool.ToolRequest;
import kr.co.goms.gomsbook.ai.tool.ToolResult;
import kr.co.goms.gomsbook.ai.tool.ToolValidationResult;

/**
 * Adapter that exposes a native GomsBook {@link AgentTool}
 * as an MCP {@link McpTool}.
 *
 * <pre>
 * MCP tools/call
 *      ↓
 * AgentToolMcpAdapter
 *      ↓
 * ToolRequest / ToolContext
 *      ↓
 * AgentTool
 *      ↓
 * ToolResult
 *      ↓
 * McpToolResult
 * </pre>
 */
public final class AgentToolMcpAdapter
        implements McpTool {

    private static final String REQUEST_ID_PREFIX =
            "mcp-";

    private final AgentTool agentTool;

    private final McpToolDefinition definition;


    /**
     * Creates an adapter using the AgentTool metadata
     * and input schema.
     *
     * @param agentTool native Agent Tool
     */
    public AgentToolMcpAdapter(
            AgentTool agentTool) {

        this.agentTool =
                Objects.requireNonNull(
                        agentTool,
                        "AgentTool must not be null."
                );

        this.definition =
                createDefinition(
                        agentTool
                );
    }


    /**
     * Creates an adapter using an explicit
     * MCP Tool definition.
     *
     * @param agentTool native Agent Tool
     * @param definition MCP Tool definition
     */
    public AgentToolMcpAdapter(
            AgentTool agentTool,
            McpToolDefinition definition) {

        this.agentTool =
                Objects.requireNonNull(
                        agentTool,
                        "AgentTool must not be null."
                );

        this.definition =
                Objects.requireNonNull(
                        definition,
                        "MCP Tool definition must not be null."
                );

        this.definition.validate();

        String nativeName =
                normalizeRequired(
                        agentTool.getName(),
                        "AgentTool name"
                );

        if (!nativeName.equals(
                definition.getName())) {

            throw new IllegalArgumentException(
                    "AgentTool name and MCP Tool name "
                            + "must match. agentTool="
                            + nativeName
                            + ", mcpTool="
                            + definition.getName()
            );
        }
    }


    @Override
    public McpToolDefinition getDefinition() {

        return definition;
    }


    public AgentTool getAgentTool() {

        return agentTool;
    }


    @Override
    public McpToolResult execute(
            Map<String, Object> arguments) {

        Map<String, Object> safeArguments =
                arguments == null
                        ? new LinkedHashMap<>()
                        : new LinkedHashMap<>(
                                arguments
                        );

        String requestId =
                createRequestId();

        try {

            ToolRequest request =
                    createToolRequest(
                            requestId,
                            safeArguments
                    );

            ToolContext context =
                    createToolContext(
                            requestId,
                            safeArguments
                    );

            ToolResult nativeResult =
                    agentTool.execute(
                            request,
                            context
                    );

            if (nativeResult == null) {

                return McpToolResult.error(
                        "AgentTool returned null result: "
                                + agentTool.getName()
                );
            }

            return convertResult(
                    nativeResult
            );

        } catch (RuntimeException exception) {

            return McpToolResult.error(
                    createExceptionMessage(
                            exception
                    )
            );
        }
    }


    /**
     * Converts MCP arguments to the native ToolRequest.
     */
    private ToolRequest createToolRequest(
            String requestId,
            Map<String, Object> arguments) {

        return ToolRequest.builder()
                .requestId(
                        requestId
                )
                .toolName(
                        agentTool.getName()
                )
                .arguments(
                        arguments
                )
                .build();
    }


    /**
     * Creates the native Tool execution context.
     */
    private ToolContext createToolContext(
            String requestId,
            Map<String, Object> arguments) {

        return ToolContext.builder()
                .requestId(
                        requestId
                )
                .attributes(
                        arguments
                )
                .build();
    }


    /**
     * Converts native ToolResult into an
     * MCP completed Tool result.
     */
    private McpToolResult convertResult(
            ToolResult result) {

        Map<String, Object> structuredContent =
                createStructuredContent(
                        result
                );

        String text =
                createResultText(
                        result
                );

        return McpToolResult.builder()
                .content(
                        McpTextContent.builder()
                                .text(
                                        text
                                )
                                .build()
                )
                .structuredContent(
                        structuredContent
                )
                .isError(
                        !result.isSuccess()
                )
                .build();
    }

    /**
     * Creates structured MCP result data.
     */
    private Map<String, Object> createStructuredContent(
            ToolResult result) {

        Map<String, Object> structured =
                new LinkedHashMap<>();

        structured.put(
                "toolName",
                result.getToolName()
        );


        if (result.getStatus() != null) {

            structured.put(
                    "status",
                    result.getStatus()
                            .name()
            );
        }


        if (result.hasRequestId()) {

            structured.put(
                    "requestId",
                    result.getRequestId()
            );
        }


        if (result.hasToolCallId()) {

            structured.put(
                    "toolCallId",
                    result.getToolCallId()
            );
        }


        if (result.hasMessage()) {

            structured.put(
                    "message",
                    result.getMessage()
            );
        }


        if (result.hasData()) {

            structured.put(
                    "data",
                    result.getData()
            );
        }


        if (result.hasIssues()) {

            structured.put(
                    "issues",
                    convertIssues(
                            result.getIssues()
                    )
            );
        }


        if (result.hasValidationResult()) {

            structured.put(
                    "validation",
                    convertValidation(
                            result.getValidationResult()
                    )
            );
        }


        if (result.getErrorCode() != null) {

            structured.put(
                    "errorCode",
                    result.getErrorCode()
            );
        }


        if (result.getErrorMessage() != null
                && !result.getErrorMessage()
                        .isBlank()) {

            structured.put(
                    "errorMessage",
                    result.getErrorMessage()
            );
        }


        long durationMillis =
                result.getDurationMillis();

        if (durationMillis >= 0L) {

            structured.put(
                    "durationMillis",
                    durationMillis
            );
        }

        return structured;
    }


    private List<Map<String, Object>> convertIssues(
            List<ToolIssue> issues) {

        List<Map<String, Object>> converted =
                new ArrayList<>();

        if (issues == null) {

            return converted;
        }


        for (ToolIssue issue
                : issues) {

            if (issue == null) {

                continue;
            }

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put(
                    "detail",
                    issue.toString()
            );

            converted.add(
                    item
            );
        }

        return converted;
    }


    private Map<String, Object> convertValidation(
            ToolValidationResult validation) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        if (validation == null) {

            return result;
        }

        result.put(
                "valid",
                validation.isValid()
        );

        result.put(
                "detail",
                validation.toString()
        );

        return result;
    }


    private String createResultText(
            ToolResult result) {

        if (result.hasMessage()) {

            return result.getMessage();
        }


        if (result.getErrorMessage() != null
                && !result.getErrorMessage()
                        .isBlank()) {

            return result.getErrorMessage();
        }


        if (result.isSuccess()) {

            return "Tool execution completed successfully: "
                    + result.getToolName();
        }

        return "Tool execution failed: "
                + result.getToolName();
    }


    /**
     * Creates an MCP Tool definition from
     * the native AgentTool.
     *
     * <p>
     * The native AgentTool input schema is reused
     * whenever available. This keeps MCP tools/list
     * aligned with the actual Tool contract.
     * </p>
     */
    private static McpToolDefinition createDefinition(
            AgentTool agentTool) {

        String name =
                normalizeRequired(
                        agentTool.getName(),
                        "AgentTool name"
                );

        String description =
                normalizeOptional(
                        agentTool.getDescription()
                );


        Map<String, Object> inputSchema =
                resolveInputSchema(
                        agentTool
                );


        return McpToolDefinition.builder()
                .name(
                        name
                )
                .description(
                        description
                )
                .inputSchema(
                        inputSchema
                )
                .build();
    }


    /**
     * Resolves the Tool input schema.
     *
     * <p>
     * Native AgentTool schema takes precedence.
     * A minimal object schema is used only when
     * the Tool does not provide a schema.
     * </p>
     */
    private static Map<String, Object> resolveInputSchema(
            AgentTool agentTool) {

        Map<String, Object> nativeSchema =
                agentTool.getInputSchema();

        if (nativeSchema != null
                && !nativeSchema.isEmpty()) {

            Map<String, Object> schema =
                    new LinkedHashMap<>(
                            nativeSchema
                    );

            ensureObjectSchema(
                    schema
            );

            return schema;
        }


        return createDefaultInputSchema();
    }


    /**
     * Ensures that an MCP Tool inputSchema has
     * an object root.
     */
    private static void ensureObjectSchema(
            Map<String, Object> schema) {

        Object type =
                schema.get(
                        "type"
                );

        if (type == null) {

            schema.put(
                    "type",
                    "object"
            );

            return;
        }


        if (!"object".equals(
                String.valueOf(
                        type
                ))) {

            throw new IllegalArgumentException(
                    "AgentTool input schema root type "
                            + "must be 'object'. tool="
                            + schema
            );
        }
    }


    /**
     * Creates the fallback MCP input schema.
     */
    private static Map<String, Object>
            createDefaultInputSchema() {

        Map<String, Object> schema =
                new LinkedHashMap<>();

        schema.put(
                "type",
                "object"
        );

        schema.put(
                "properties",
                new LinkedHashMap<String, Object>()
        );

        schema.put(
                "additionalProperties",
                Boolean.TRUE
        );

        return schema;
    }


    private static String createRequestId() {

        return REQUEST_ID_PREFIX
                + UUID.randomUUID();
    }


    private String createExceptionMessage(
            RuntimeException exception) {

        String message =
                exception == null
                        ? null
                        : exception.getMessage();

        if (message == null
                || message.isBlank()) {

            message =
                    exception == null
                            ? "Unknown AgentTool error"
                            : exception.getClass()
                                    .getSimpleName();
        }

        return "AgentTool execution failed. tool="
                + agentTool.getName()
                + ", error="
                + message;
    }


    private static String normalizeRequired(
            String value,
            String fieldName) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must not be blank."
            );
        }

        return value.trim();
    }


    private static String normalizeOptional(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }


    @Override
    public String toString() {

        return "AgentToolMcpAdapter{"
                + "toolName='"
                + getName()
                + '\''
                + '}';
    }
}