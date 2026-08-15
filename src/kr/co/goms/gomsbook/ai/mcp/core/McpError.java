/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.discovery.McpClientCapabilities;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpErrorCode;

/**
 * Represents a JSON-RPC error returned by an MCP server.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * A JSON-RPC error contains:
 * </p>
 *
 * <ul>
 *     <li>{@code code}</li>
 *     <li>{@code message}</li>
 *     <li>optional {@code data}</li>
 * </ul>
 */
public final class McpError {

    private final int code;

    private final String message;

    private final Object data;


    private McpError(
            int code,
            String message,
            Object data
    ) {

        McpErrorCode.validateForEmission(
                code
        );

        this.code =
                code;

        this.message =
                requireText(
                        message,
                        "MCP error message"
                );

        this.data =
                data;
    }


    /*
     * ------------------------------------------------------------
     * Generic factory
     * ------------------------------------------------------------
     */

    public static McpError of(
            int code,
            String message
    ) {

        return new McpError(
                code,
                message,
                null
        );
    }


    public static McpError of(
            int code,
            String message,
            Object data
    ) {

        return new McpError(
                code,
                message,
                data
        );
    }


    /*
     * ------------------------------------------------------------
     * JSON-RPC standard errors
     * ------------------------------------------------------------
     */

    public static McpError parseError() {

        return of(
                McpErrorCode.PARSE_ERROR,
                "Parse error"
        );
    }


    public static McpError parseError(
            Object data
    ) {

        return of(
                McpErrorCode.PARSE_ERROR,
                "Parse error",
                data
        );
    }


    public static McpError invalidRequest() {

        return of(
                McpErrorCode.INVALID_REQUEST,
                "Invalid Request"
        );
    }


    public static McpError invalidRequest(
            Object data
    ) {

        return of(
                McpErrorCode.INVALID_REQUEST,
                "Invalid Request",
                data
        );
    }


    public static McpError methodNotFound(
            String method
    ) {

        String normalized =
                normalizeOptional(
                        method
                );

        if (normalized == null) {

            return of(
                    McpErrorCode.METHOD_NOT_FOUND,
                    "Method not found"
            );
        }

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "method",
                normalized
        );

        return of(
                McpErrorCode.METHOD_NOT_FOUND,
                "Method not found",
                Collections.unmodifiableMap(
                        data
                )
        );
    }


    public static McpError invalidParams() {

        return of(
                McpErrorCode.INVALID_PARAMS,
                "Invalid params"
        );
    }


    public static McpError invalidParams(
            Object data
    ) {

        return of(
                McpErrorCode.INVALID_PARAMS,
                "Invalid params",
                data
        );
    }
    
    public static McpError invalidParams(
            String message,
            Object data
    ) {

        return of(
                McpErrorCode.INVALID_PARAMS,
                message,
                data
        );
    }

    public static McpError internalError() {

        return of(
                McpErrorCode.INTERNAL_ERROR,
                "Internal error"
        );
    }


    public static McpError internalError(
            Object data
    ) {

        return of(
                McpErrorCode.INTERNAL_ERROR,
                "Internal error",
                data
        );
    }


    /*
     * ------------------------------------------------------------
     * MCP 2026-07-28 protocol errors
     * ------------------------------------------------------------
     */

    public static McpError headerMismatch(
            Object data
    ) {

        return of(
                McpErrorCode.HEADER_MISMATCH,
                "Header mismatch",
                data
        );
    }


    /**
     * Creates:
     *
     * <pre>
     * {
     *   "code": -32021,
     *   "message": "...",
     *   "data": {
     *     "requiredCapabilities": { ... }
     *   }
     * }
     * </pre>
     *
     * @param requiredCapabilities capabilities required to process
     *        the request
     * @return MCP error
     */
    public static McpError missingRequiredClientCapability(
            McpClientCapabilities requiredCapabilities
    ) {

        Objects.requireNonNull(
                requiredCapabilities,
                "Required MCP client capabilities must not be null."
        );

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "requiredCapabilities",
                requiredCapabilities
        );

        return of(
                McpErrorCode.MISSING_REQUIRED_CLIENT_CAPABILITY,
                "Missing required client capability",
                Collections.unmodifiableMap(
                        data
                )
        );
    }


    /**
     * Convenience factory for a single elicitation requirement.
     *
     * @return MCP error
     */
    public static McpError missingElicitationCapability() {

        McpClientCapabilities required =
                McpClientCapabilities.builder()
                        .elicitation(
                                McpClientCapabilities
                                        .ElicitationCapability
                                        .form()
                        )
                        .build();

        return missingRequiredClientCapability(
                required
        );
    }


    /**
     * Creates:
     *
     * <pre>
     * {
     *   "code": -32022,
     *   "message": "...",
     *   "data": {
     *     "supported": [ "2026-07-28" ],
     *     "requested": "..."
     *   }
     * }
     * </pre>
     *
     * @param requestedVersion requested version
     * @param supportedVersions supported server versions
     * @return MCP error
     */
    public static McpError unsupportedProtocolVersion(
            String requestedVersion,
            List<String> supportedVersions
    ) {

        String requested =
                requireText(
                        requestedVersion,
                        "Requested MCP protocol version"
                );

        List<String> supported =
                immutableVersions(
                        supportedVersions
                );

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "supported",
                supported
        );

        data.put(
                "requested",
                requested
        );

        return of(
                McpErrorCode.UNSUPPORTED_PROTOCOL_VERSION,
                "Unsupported protocol version",
                Collections.unmodifiableMap(
                        data
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public int getCode() {
        return code;
    }


    public String getMessage() {
        return message;
    }


    public Object getData() {
        return data;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasData() {
        return data != null;
    }


    public boolean isParseError() {

        return code
                == McpErrorCode.PARSE_ERROR;
    }


    public boolean isInvalidRequest() {

        return code
                == McpErrorCode.INVALID_REQUEST;
    }


    public boolean isMethodNotFound() {

        return code
                == McpErrorCode.METHOD_NOT_FOUND;
    }


    public boolean isInvalidParams() {

        return code
                == McpErrorCode.INVALID_PARAMS;
    }


    public boolean isInternalError() {

        return code
                == McpErrorCode.INTERNAL_ERROR;
    }


    public boolean isMcpProtocolError() {

        return McpErrorCode.isDefinedMcpError(
                code
        );
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static String requireText(
            String value,
            String fieldName
    ) {

        Objects.requireNonNull(
                value,
                fieldName + " must not be null."
        );

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return normalized;
    }


    private static List<String> immutableVersions(
            List<String> versions
    ) {

        Objects.requireNonNull(
                versions,
                "Supported MCP protocol versions must not be null."
        );

        if (versions.isEmpty()) {

            throw new IllegalArgumentException(
                    "Supported MCP protocol versions must not be empty."
            );
        }

        List<String> copy =
                new ArrayList<>();

        for (String version : versions) {

            String normalized =
                    requireText(
                            version,
                            "Supported MCP protocol version"
                    );

            if (!copy.contains(
                    normalized
            )) {

                copy.add(
                        normalized
                );
            }
        }

        return Collections.unmodifiableList(
                copy
        );
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
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpError)) {
            return false;
        }

        McpError other =
                (McpError) object;

        return code == other.code
                && Objects.equals(
                        message,
                        other.message
                )
                && Objects.equals(
                        data,
                        other.data
                );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                code,
                message,
                data
        );
    }


    @Override
    public String toString() {

        return "McpError{"
                + "code="
                + code
                + ", message='"
                + message
                + '\''
                + ", data="
                + data
                + '}';
    }
}