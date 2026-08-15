/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.protocol;

/**
 * JSON-RPC and MCP error codes.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * JSON-RPC standard errors occupy the standard JSON-RPC
 * reserved range.
 * </p>
 *
 * <p>
 * MCP 2026-07-28 reserves:
 * </p>
 *
 * <pre>
 * -32020 .. -32099
 * </pre>
 *
 * <p>
 * exclusively for MCP specification-defined errors.
 * Implementations MUST NOT allocate arbitrary custom errors
 * from this range.
 * </p>
 */
public final class McpErrorCode {

    /*
     * ------------------------------------------------------------
     * JSON-RPC 2.0 standard errors
     * ------------------------------------------------------------
     */

    /**
     * Invalid JSON was received by the server.
     */
    public static final int PARSE_ERROR =
            -32700;

    /**
     * The JSON message is not a valid JSON-RPC request.
     */
    public static final int INVALID_REQUEST =
            -32600;

    /**
     * The requested method does not exist or is unavailable.
     */
    public static final int METHOD_NOT_FOUND =
            -32601;

    /**
     * Invalid method parameters.
     */
    public static final int INVALID_PARAMS =
            -32602;

    /**
     * Internal JSON-RPC server error.
     */
    public static final int INTERNAL_ERROR =
            -32603;


    /*
     * ------------------------------------------------------------
     * MCP 2026-07-28 specification errors
     * ------------------------------------------------------------
     */

    /**
     * Transport/header metadata does not match the MCP message body.
     *
     * <p>
     * Example:
     * MCP-Protocol-Version HTTP header does not match
     * _meta["io.modelcontextprotocol/protocolVersion"].
     * </p>
     */
    public static final int HEADER_MISMATCH =
            -32020;

    /**
     * The request requires a client capability that the client
     * did not advertise.
     */
    public static final int MISSING_REQUIRED_CLIENT_CAPABILITY =
            -32021;

    /**
     * The requested MCP protocol version is not supported
     * by the server.
     */
    public static final int UNSUPPORTED_PROTOCOL_VERSION =
            -32022;


    /*
     * ------------------------------------------------------------
     * Reserved ranges
     * ------------------------------------------------------------
     */

    /**
     * Start of the legacy implementation-defined MCP range.
     *
     * <p>
     * New implementations SHOULD NOT allocate errors from
     * {@code -32000 .. -32019}.
     * </p>
     */
    public static final int LEGACY_SERVER_ERROR_MAX =
            -32000;

    /**
     * End of the legacy implementation-defined MCP range.
     */
    public static final int LEGACY_SERVER_ERROR_MIN =
            -32019;

    /**
     * First MCP specification-reserved error code.
     */
    public static final int MCP_RESERVED_MAX =
            -32020;

    /**
     * Last MCP specification-reserved error code.
     */
    public static final int MCP_RESERVED_MIN =
            -32099;


    /*
     * ------------------------------------------------------------
     * Deprecated / forbidden codes
     * ------------------------------------------------------------
     */

    /**
     * Resource-not-found error used by older MCP revisions.
     *
     * <p>
     * MCP 2026-07-28 implementations MUST NOT emit this code.
     * It is retained only so clients may recognize responses
     * from older MCP implementations.
     * </p>
     */
    @Deprecated
    public static final int LEGACY_RESOURCE_NOT_FOUND =
            -32002;

    /**
     * URL elicitation-required error used by MCP 2025-11-25.
     *
     * <p>
     * MCP 2026-07-28 implementations MUST NOT emit this code.
     * </p>
     */
    @Deprecated
    public static final int LEGACY_URL_ELICITATION_REQUIRED =
            -32042;


    private McpErrorCode() {
    }


    /*
     * ------------------------------------------------------------
     * Classification
     * ------------------------------------------------------------
     */

    /**
     * Returns whether the code is one of the JSON-RPC
     * standard errors used by MCP.
     *
     * @param code error code
     * @return true if standard JSON-RPC error
     */
    public static boolean isJsonRpcStandard(
            int code
    ) {

        return code == PARSE_ERROR
                || code == INVALID_REQUEST
                || code == METHOD_NOT_FOUND
                || code == INVALID_PARAMS
                || code == INTERNAL_ERROR;
    }

    /**
     * Returns whether the code is currently defined by
     * MCP 2026-07-28.
     *
     * @param code error code
     * @return true if defined MCP error
     */
    public static boolean isDefinedMcpError(
            int code
    ) {

        return code == HEADER_MISMATCH
                || code == MISSING_REQUIRED_CLIENT_CAPABILITY
                || code == UNSUPPORTED_PROTOCOL_VERSION;
    }

    /**
     * Returns whether the code lies in the MCP specification
     * reserved range.
     *
     * @param code error code
     * @return true if MCP-reserved
     */
    public static boolean isMcpReserved(
            int code
    ) {

        return code <= MCP_RESERVED_MAX
                && code >= MCP_RESERVED_MIN;
    }

    /**
     * Returns whether the code lies in the legacy
     * implementation-defined range.
     *
     * @param code error code
     * @return true if legacy range
     */
    public static boolean isLegacyServerErrorRange(
            int code
    ) {

        return code <= LEGACY_SERVER_ERROR_MAX
                && code >= LEGACY_SERVER_ERROR_MIN;
    }

    /**
     * Returns whether this error code must not be emitted
     * by an MCP 2026-07-28 implementation.
     *
     * @param code error code
     * @return true if forbidden for emission
     */
    public static boolean isForbiddenToEmit(
            int code
    ) {

        return code == LEGACY_RESOURCE_NOT_FOUND
                || code == LEGACY_URL_ELICITATION_REQUIRED;
    }

    /**
     * Validates that an MCP specification-reserved code
     * is actually defined by this protocol revision.
     *
     * @param code error code
     *
     * @throws IllegalArgumentException
     *         if the code occupies the MCP reserved range
     *         but is not defined by MCP 2026-07-28
     */
    public static void validateForEmission(
            int code
    ) {

        if (isForbiddenToEmit(code)) {

            throw new IllegalArgumentException(
                    "MCP 2026-07-28 must not emit legacy error code: "
                            + code
            );
        }

        if (isMcpReserved(code)
                && !isDefinedMcpError(code)) {

            throw new IllegalArgumentException(
                    "Undefined MCP reserved error code: "
                            + code
            );
        }
    }
}