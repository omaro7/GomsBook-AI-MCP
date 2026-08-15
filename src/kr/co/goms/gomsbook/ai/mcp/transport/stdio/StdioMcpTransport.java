/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.transport.stdio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import kr.co.goms.gomsbook.ai.mcp.transport.McpTransport;
import kr.co.goms.gomsbook.ai.mcp.transport.McpTransportException;
import kr.co.goms.gomsbook.ai.mcp.transport.McpTransportListener;

/**
 * STDIO transport implementation for MCP.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * MCP messages are exchanged as UTF-8 encoded JSON-RPC messages,
 * with one complete message per line.
 * </p>
 *
 * <p>
 * This transport uses a one-shot lifecycle. Once started,
 * it cannot be started again after it has stopped.
 * </p>
 */
public final class StdioMcpTransport
        implements McpTransport {

    private static final String DEFAULT_READER_THREAD_NAME =
            "gomsbook-mcp-stdio-reader";

    private final BufferedReader reader;

    private final BufferedWriter writer;

    private final boolean closeStreams;

    private final Object lifecycleLock =
            new Object();

    private final Object writeLock =
            new Object();

    /**
     * Indicates whether start() has ever succeeded.
     *
     * <p>
     * STDIO transport is intentionally non-restartable.
     * </p>
     */
    private final AtomicBoolean started =
            new AtomicBoolean(false);

    /**
     * Indicates whether the transport is currently active.
     */
    private final AtomicBoolean running =
            new AtomicBoolean(false);

    private volatile McpTransportListener listener;

    private volatile Thread readerThread;


    /*
     * ------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------
     */

    /**
     * Creates an STDIO transport using {@link System#in}
     * and {@link System#out}.
     *
     * <p>
     * System streams are not closed when this transport stops.
     * </p>
     */
    public StdioMcpTransport() {

        this(
                System.in,
                System.out,
                false
        );
    }


    /**
     * Creates an STDIO transport using externally managed streams.
     *
     * @param input input stream
     * @param output output stream
     */
    public StdioMcpTransport(
            InputStream input,
            OutputStream output
    ) {

        this(
                input,
                output,
                false
        );
    }


    /**
     * Creates an STDIO transport.
     *
     * @param input input stream
     * @param output output stream
     * @param closeStreams whether this transport owns the streams
     */
    public StdioMcpTransport(
            InputStream input,
            OutputStream output,
            boolean closeStreams
    ) {

        Objects.requireNonNull(
                input,
                "MCP STDIO input stream must not be null."
        );

        Objects.requireNonNull(
                output,
                "MCP STDIO output stream must not be null."
        );

        this.reader =
                new BufferedReader(
                        new InputStreamReader(
                                input,
                                StandardCharsets.UTF_8
                        )
                );

        this.writer =
                new BufferedWriter(
                        new OutputStreamWriter(
                                output,
                                StandardCharsets.UTF_8
                        )
                );

        this.closeStreams =
                closeStreams;
    }


    /*
     * ------------------------------------------------------------
     * Lifecycle
     * ------------------------------------------------------------
     */

    @Override
    public void start() {

        synchronized (lifecycleLock) {

            if (running.get()) {
                return;
            }

            /*
             * STDIO transport is intentionally one-shot.
             *
             * System.in readLine() may remain blocked even after
             * interruption. Allowing restart could therefore
             * create multiple reader threads on the same stream.
             */
            if (!started.compareAndSet(
                    false,
                    true
            )) {

                throw new IllegalStateException(
                        "MCP STDIO transport cannot be restarted."
                );
            }

            /*
             * Listener must be installed before transport starts.
             */
            requireListener();

            running.set(
                    true
            );

            Thread thread =
                    new Thread(
                            this::readLoop,
                            DEFAULT_READER_THREAD_NAME
                    );

            thread.setDaemon(
                    true
            );

            readerThread =
                    thread;

            try {

                thread.start();

            } catch (RuntimeException exception) {

                running.set(
                        false
                );

                readerThread =
                        null;

                /*
                 * started deliberately remains true.
                 *
                 * Once startup has been attempted against the
                 * underlying STDIO streams, this transport object
                 * is not reused.
                 */
                throw exception;
            }
        }
    }


    @Override
    public void stop() {

        synchronized (lifecycleLock) {

            /*
             * stop() remains idempotent.
             */
            if (!running.getAndSet(
                    false
            )) {

                return;
            }

            Thread thread =
                    readerThread;

            readerThread =
                    null;

            if (thread != null
                    && thread != Thread.currentThread()) {

                /*
                 * Custom streams may react to interruption.
                 *
                 * System.in is not guaranteed to unblock here.
                 */
                thread.interrupt();
            }

            /*
             * Owned streams are closed so blocking readLine()
             * can terminate.
             *
             * System streams are never owned by the default
             * constructor.
             */
            if (closeStreams) {

                closeOwnedStreams();
            }
        }
    }


    @Override
    public boolean isRunning() {

        return running.get();
    }


    /**
     * Returns whether this transport has ever been started.
     *
     * @return {@code true} after the first start attempt
     */
    public boolean isStarted() {

        return started.get();
    }


    /*
     * ------------------------------------------------------------
     * Listener
     * ------------------------------------------------------------
     */

    @Override
    public void setListener(
            McpTransportListener listener
    ) {

        Objects.requireNonNull(
                listener,
                "MCP transport listener must not be null."
        );

        synchronized (lifecycleLock) {

            if (started.get()) {

                throw new IllegalStateException(
                        "MCP STDIO transport listener cannot be "
                                + "changed after transport startup."
                );
            }

            this.listener =
                    listener;
        }
    }


    @Override
    public McpTransportListener getListener() {

        return listener;
    }


    /*
     * ------------------------------------------------------------
     * Outbound message
     * ------------------------------------------------------------
     */

    @Override
    public void send(
            String message
    ) {

        String normalized =
                McpTransport.requireMessage(
                        message
                );

        if (!running.get()) {

            throw new IllegalStateException(
                    "MCP STDIO transport is not running."
            );
        }

        synchronized (writeLock) {

            /*
             * Check again after acquiring the write lock.
             *
             * stop() may have occurred while waiting for another
             * sender to finish.
             */
            if (!running.get()) {

                throw new IllegalStateException(
                        "MCP STDIO transport is not running."
                );
            }

            try {

                writer.write(
                        normalized
                );

                writer.newLine();

                writer.flush();

            } catch (IOException exception) {

                handleWriteFailure(
                        exception
                );

                throw new McpTransportException(
                        "Failed to write MCP STDIO message.",
                        exception
                );
            }
        }
    }


    /*
     * ------------------------------------------------------------
     * Reader loop
     * ------------------------------------------------------------
     */

    private void readLoop() {

        boolean closedByReader =
                false;

        try {

            while (running.get()) {

                String line =
                        reader.readLine();

                if (line == null) {

                    /*
                     * EOF.
                     */
                    closedByReader =
                            true;

                    break;
                }

                if (!running.get()) {
                    break;
                }

                String message =
                        line.trim();

                /*
                 * Ignore blank lines.
                 */
                if (message.isEmpty()) {
                    continue;
                }

                notifyMessage(
                        message
                );
            }

        } catch (IOException exception) {

            if (running.get()) {

                notifyError(
                        exception
                );

                closedByReader =
                        true;
            }

        } catch (RuntimeException exception) {

            if (running.get()) {

                notifyError(
                        exception
                );

                closedByReader =
                        true;
            }

        } finally {

            boolean wasRunning =
                    running.getAndSet(
                            false
                    );

            readerThread =
                    null;

            if (closeStreams) {

                closeOwnedStreams();
            }

            /*
             * Notify closure when:
             *
             * - EOF closed the input stream
             * - reader failed
             * - transport was still marked running when loop ended
             *
             * Explicit stop() does not require a duplicate
             * notification after running has already been cleared.
             */
            if (wasRunning
                    || closedByReader) {

                notifyClosed();
            }
        }
    }


    /*
     * ------------------------------------------------------------
     * Write failure
     * ------------------------------------------------------------
     */

    private void handleWriteFailure(
            IOException exception
    ) {

        boolean wasRunning =
                running.getAndSet(
                        false
                );

        notifyError(
                exception
        );

        if (closeStreams) {

            closeOwnedStreams();
        }

        if (wasRunning) {

            notifyClosed();
        }
    }


    /*
     * ------------------------------------------------------------
     * Listener notifications
     * ------------------------------------------------------------
     */

    private void notifyMessage(
            String message
    ) {

        McpTransportListener current =
                listener;

        if (current == null) {

            throw new IllegalStateException(
                    "MCP transport listener is not configured."
            );
        }

        current.onMessage(
                message
        );
    }


    private void notifyError(
            Throwable error
    ) {

        McpTransportListener current =
                listener;

        if (current == null) {
            return;
        }

        try {

            current.onError(
                    error
            );

        } catch (RuntimeException ignored) {

            /*
             * Listener failure must not destabilize the
             * transport thread.
             */
        }
    }


    private void notifyClosed() {

        McpTransportListener current =
                listener;

        if (current == null) {
            return;
        }

        try {

            current.onClosed();

        } catch (RuntimeException ignored) {

            /*
             * Close callback failures are local application
             * failures and must not escape the transport.
             */
        }
    }


    /*
     * ------------------------------------------------------------
     * Resource management
     * ------------------------------------------------------------
     */

    private void closeOwnedStreams() {

        try {

            reader.close();

        } catch (IOException exception) {

            notifyError(
                    exception
            );
        }

        try {

            writer.close();

        } catch (IOException exception) {

            notifyError(
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "StdioMcpTransport{"
                + "started="
                + started.get()
                + ", running="
                + running.get()
                + ", closeStreams="
                + closeStreams
                + '}';
    }
}