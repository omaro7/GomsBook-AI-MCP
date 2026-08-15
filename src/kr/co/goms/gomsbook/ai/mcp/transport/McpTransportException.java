/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.transport;

/**
 * Indicates an MCP transport-level failure.
 *
 * <p>
 * This exception represents I/O or framing failures and is not
 * an MCP JSON-RPC protocol error.
 * </p>
 */
public final class McpTransportException
        extends RuntimeException {

    private static final long serialVersionUID =
            1L;


    public McpTransportException(
            String message
    ) {

        super(
                message
        );
    }


    public McpTransportException(
            String message,
            Throwable cause
    ) {

        super(
                message,
                cause
        );
    }
}