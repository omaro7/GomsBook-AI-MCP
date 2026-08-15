/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.transport;

import java.util.Objects;

/**
 * Transport abstraction used by the MCP server.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * A transport is responsible only for carrying MCP JSON-RPC
 * messages between the client and server.
 * </p>
 *
 * <p>
 * Protocol parsing, request validation, dispatching and result
 * generation are handled by higher layers.
 * </p>
 */
public interface McpTransport
        extends AutoCloseable {

    /**
     * Starts the transport.
     *
     * <p>
     * After startup the transport may begin receiving MCP
     * messages and forwarding them to the configured listener.
     * </p>
     */
    void start();

    /**
     * Stops the transport and releases transport resources.
     */
    void stop();

    /**
     * Returns whether the transport is currently running.
     *
     * @return {@code true} if running
     */
    boolean isRunning();

    /**
     * Installs the listener that receives inbound MCP messages.
     *
     * <p>
     * A transport has one active listener.
     * </p>
     *
     * @param listener transport listener
     */
    void setListener(
            McpTransportListener listener
    );

    /**
     * Returns the currently configured listener.
     *
     * @return listener, or {@code null} if none is configured
     */
    McpTransportListener getListener();

    /**
     * Sends an MCP message through this transport.
     *
     * <p>
     * The supplied value represents one complete serialized
     * JSON-RPC message. Framing rules such as STDIO newline
     * termination are the responsibility of the concrete
     * transport implementation.
     * </p>
     *
     * @param message serialized MCP JSON-RPC message
     */
    void send(
            String message
    );

    /**
     * Returns whether this transport currently has a listener.
     *
     * @return {@code true} if listener configured
     */
    default boolean hasListener() {
        return getListener() != null;
    }

    /**
     * Requires that a listener has been configured.
     *
     * @return configured listener
     *
     * @throws IllegalStateException
     *         if no listener has been configured
     */
    default McpTransportListener requireListener() {

        McpTransportListener listener =
                getListener();

        if (listener == null) {

            throw new IllegalStateException(
                    "MCP transport listener has not been configured."
            );
        }

        return listener;
    }

    /**
     * Validates an outbound serialized message.
     *
     * <p>
     * Concrete transports may call this helper before writing
     * a message.
     * </p>
     *
     * @param message serialized message
     * @return validated message
     */
    static String requireMessage(
            String message
    ) {

        Objects.requireNonNull(
                message,
                "MCP transport message must not be null."
        );

        String normalized =
                message.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP transport message must not be blank."
            );
        }

        return normalized;
    }

    /**
     * Closes this transport.
     */
    @Override
    default void close() {
        stop();
    }
}