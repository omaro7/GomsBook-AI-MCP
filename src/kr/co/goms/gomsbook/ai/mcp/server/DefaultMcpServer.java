/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.server.runtime.McpServerRuntime;
import kr.co.goms.gomsbook.ai.mcp.transport.McpTransport;

/**
 * Default implementation of {@link McpServer}.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This class coordinates the MCP transport and server runtime.
 * It does not maintain MCP protocol initialization or session
 * state.
 * </p>
 *
 * <p>
 * Responsibilities:
 * </p>
 *
 * <ul>
 *     <li>manage server lifecycle</li>
 *     <li>start and stop the configured transport</li>
 *     <li>expose the configured runtime</li>
 * </ul>
 *
 * <p>
 * Request parsing, dispatching and response generation are
 * delegated to the transport and runtime layers.
 * </p>
 */
public final class DefaultMcpServer
        implements McpServer {

    private final McpServerConfig config;

    private final McpServerRuntime runtime;

    private final McpTransport transport;

    private final Object lifecycleLock =
            new Object();

    private volatile boolean running;


    /**
     * Creates an MCP server.
     *
     * @param config server configuration
     * @param runtime server runtime
     * @param transport MCP transport
     */
    public DefaultMcpServer(
            McpServerConfig config,
            McpServerRuntime runtime,
            McpTransport transport
    ) {

        this.config =
                Objects.requireNonNull(
                        config,
                        "MCP server config must not be null."
                );

        this.runtime =
                Objects.requireNonNull(
                        runtime,
                        "MCP server runtime must not be null."
                );

        this.transport =
                Objects.requireNonNull(
                        transport,
                        "MCP transport must not be null."
                );
    }


    /*
     * ------------------------------------------------------------
     * Lifecycle
     * ------------------------------------------------------------
     */

    @Override
    public void start() {

        synchronized (lifecycleLock) {

            if (running) {
                return;
            }

            try {

                transport.start();

                running = true;

            } catch (RuntimeException exception) {

                /*
                 * Ensure the server never remains marked as
                 * running when transport startup fails.
                 */
                running = false;

                throw exception;
            }
        }
    }


    @Override
    public void stop() {

        synchronized (lifecycleLock) {

            if (!running) {
                return;
            }

            try {

                transport.stop();

            } finally {

                running = false;
            }
        }
    }


    @Override
    public boolean isRunning() {

        return running
                && transport.isRunning();
    }


    @Override
    public void close() {

        stop();
    }


    /*
     * ------------------------------------------------------------
     * Components
     * ------------------------------------------------------------
     */

    /**
     * Returns the immutable MCP server configuration.
     *
     * @return server configuration
     */
    public McpServerConfig getConfig() {
        return config;
    }


    /**
     * Returns the MCP server runtime.
     *
     * @return server runtime
     */
    public McpServerRuntime getRuntime() {
        return runtime;
    }


    /**
     * Returns the configured MCP transport.
     *
     * @return transport
     */
    public McpTransport getTransport() {
        return transport;
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "DefaultMcpServer{"
                + "config="
                + config
                + ", transport="
                + transport
                + ", running="
                + running
                + '}';
    }
}