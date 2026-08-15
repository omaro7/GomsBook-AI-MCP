/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server;

/**
 * MCP server lifecycle contract.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * The server coordinates the MCP transport and runtime.
 * Protocol-level initialization or session state is intentionally
 * not part of this interface.
 * </p>
 */
public interface McpServer
        extends AutoCloseable {

    /**
     * Starts the MCP server.
     *
     * <p>
     * Implementations should start the configured transport and
     * begin accepting MCP messages.
     * </p>
     */
    void start();

    /**
     * Stops the MCP server.
     *
     * <p>
     * Implementations should stop accepting new messages and
     * release transport resources.
     * </p>
     */
    void stop();

    /**
     * Returns whether the server is currently running.
     *
     * @return {@code true} if running
     */
    boolean isRunning();

    /**
     * Closes the MCP server.
     *
     * <p>
     * The default behavior delegates to {@link #stop()}.
     * </p>
     */
    @Override
    default void close() {
        stop();
    }
}