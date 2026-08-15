/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.transport;

/**
 * Listener for MCP transport events.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * The listener receives complete serialized JSON-RPC messages
 * from the transport layer. Parsing and protocol processing are
 * handled by higher layers.
 * </p>
 */
public interface McpTransportListener {

    /**
     * Called when a complete MCP JSON-RPC message is received.
     *
     * @param message serialized JSON-RPC message
     */
    void onMessage(
            String message
    );

    /**
     * Called when a transport-level error occurs.
     *
     * <p>
     * This callback represents I/O or framing failures.
     * MCP protocol errors should be handled by the codec/runtime
     * layers instead.
     * </p>
     *
     * @param error transport error
     */
    default void onError(
            Throwable error
    ) {
        // Default no-op.
    }

    /**
     * Called when the transport has been closed.
     *
     * <p>
     * The closure may be caused by an explicit stop operation,
     * end-of-stream, or transport failure.
     * </p>
     */
    default void onClosed() {
        // Default no-op.
    }
}