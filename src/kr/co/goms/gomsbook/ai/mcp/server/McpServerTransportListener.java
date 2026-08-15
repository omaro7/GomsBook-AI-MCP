/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodec;
import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodecException;
import kr.co.goms.gomsbook.ai.mcp.core.McpError;
import kr.co.goms.gomsbook.ai.mcp.core.McpRequest;
import kr.co.goms.gomsbook.ai.mcp.core.McpResponse;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpRequestId;
import kr.co.goms.gomsbook.ai.mcp.server.runtime.McpServerRuntime;
import kr.co.goms.gomsbook.ai.mcp.transport.McpTransport;
import kr.co.goms.gomsbook.ai.mcp.transport.McpTransportListener;

/**
 * Bridges an MCP transport to the MCP JSON codec and server runtime.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Processing flow:
 * </p>
 *
 * <pre>
 * Transport
 *     -> raw JSON
 *     -> McpJsonCodec
 *     -> McpRequest
 *     -> McpServerRuntime
 *     -> McpResponse
 *     -> McpJsonCodec
 *     -> raw JSON
 *     -> Transport
 * </pre>
 *
 * <p>
 * This listener contains no protocol-level initialization or
 * session state.
 * </p>
 */
public final class McpServerTransportListener
        implements McpTransportListener {

    private final McpTransport transport;

    private final McpJsonCodec codec;

    private final McpServerRuntime runtime;


    /**
     * Creates the server transport listener.
     *
     * @param transport MCP transport
     * @param codec MCP JSON codec
     * @param runtime MCP server runtime
     */
    public McpServerTransportListener(
            McpTransport transport,
            McpJsonCodec codec,
            McpServerRuntime runtime
    ) {

        this.transport =
                Objects.requireNonNull(
                        transport,
                        "MCP transport must not be null."
                );

        this.codec =
                Objects.requireNonNull(
                        codec,
                        "MCP JSON codec must not be null."
                );

        this.runtime =
                Objects.requireNonNull(
                        runtime,
                        "MCP server runtime must not be null."
                );
    }


    /*
     * ------------------------------------------------------------
     * McpTransportListener
     * ------------------------------------------------------------
     */

    @Override
    public void onMessage(
            String message
    ) {

        /*
         * An empty transport frame cannot represent a valid JSON
         * document.
         */
        if (message == null
                || message.trim().isEmpty()) {

            sendResponse(
                    McpResponse.failure(
                            null,
                            McpError.parseError()
                    )
            );

            return;
        }


        final McpRequest request;

        try {

            request =
                    codec.decodeRequest(
                            message
                    );

        } catch (McpJsonCodecException exception) {

            handleDecodeFailure(
                    exception
            );

            return;

        } catch (RuntimeException exception) {

            /*
             * Defensive codec boundary.
             *
             * Unexpected decoding failures are treated as an
             * invalid request because a usable McpRequest was not
             * produced.
             */
            sendResponse(
                    McpResponse.failure(
                            null,
                            McpError.invalidRequest()
                    )
            );

            return;
        }


        /*
         * --------------------------------------------------------
         * Notification
         * --------------------------------------------------------
         *
         * A JSON-RPC notification has no request id.
         *
         * It MUST NOT receive a JSON-RPC response.
         *
         * The current server runtime represents the no-response
         * case by returning null.
         * --------------------------------------------------------
         */

        if (request.isNotification()) {

            handleNotification(
                    request
            );

            return;
        }


        /*
         * --------------------------------------------------------
         * Request
         * --------------------------------------------------------
         */

        final McpResponse response;

        try {

            response =
                    runtime.handle(
                            request
                    );

        } catch (RuntimeException exception) {

            /*
             * Runtime should normally convert protocol/domain
             * exceptions to McpResponse itself.
             *
             * This is the final server boundary for an unexpected
             * runtime failure.
             */
            sendResponse(
                    McpResponse.failure(
                            request.getId(),
                            McpError.internalError(
                                    safeMessage(
                                            exception
                                    )
                            )
                    )
            );

            return;
        }


        if (response == null) {

            /*
             * A normal request has an id and therefore requires a
             * response. Returning null here indicates an internal
             * runtime contract violation.
             */
            sendResponse(
                    McpResponse.failure(
                            request.getId(),
                            McpError.internalError(
                                    "MCP runtime returned no response "
                                            + "for a request."
                            )
                    )
            );

            return;
        }


        sendResponse(
                response
        );
    }


    @Override
    public void onError(
            Throwable error
    ) {

        if (error == null) {
            return;
        }

        /*
         * STDOUT is reserved for MCP messages when using STDIO.
         * Diagnostics therefore go to STDERR.
         */
        System.err.println(
                "[MCP] Transport error: "
                        + safeMessage(
                                error
                        )
        );
    }


    @Override
    public void onClosed() {

        /*
         * Transport closure is only a transport lifecycle event.
         *
         * MCP 2026-07-28 server core maintains no initialize /
         * initialized session state requiring cleanup here.
         */
    }


    /*
     * ------------------------------------------------------------
     * Notification
     * ------------------------------------------------------------
     */

    private void handleNotification(
            McpRequest request
    ) {

        try {

            /*
             * Runtime may perform notification-side processing.
             *
             * The returned value is deliberately ignored because
             * notifications MUST NOT receive JSON-RPC responses.
             */
            runtime.handle(
                    request
            );

        } catch (RuntimeException exception) {

            /*
             * Notification errors cannot be returned to the
             * sender as JSON-RPC responses.
             *
             * Report them only through the local error channel.
             */
            onError(
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Codec decode failure
     * ------------------------------------------------------------
     */

    private void handleDecodeFailure(
            McpJsonCodecException exception
    ) {

        Objects.requireNonNull(
                exception,
                "MCP JSON codec exception must not be null."
        );


        /*
         * --------------------------------------------------------
         * -32700 Parse error
         * --------------------------------------------------------
         *
         * JSON syntax could not be parsed. No trustworthy request
         * id exists.
         */

        if (exception.isParseError()) {

            sendResponse(
                    McpResponse.failure(
                            null,
                            McpError.parseError()
                    )
            );

            return;
        }


        /*
         * --------------------------------------------------------
         * -32600 Invalid Request
         * --------------------------------------------------------
         */

        if (exception.isInvalidRequest()) {

            sendInvalidRequest(
                    exception
            );

            return;
        }


        /*
         * --------------------------------------------------------
         * -32602 Invalid params
         * --------------------------------------------------------
         *
         * If the codec successfully decoded the request id before
         * parameter validation failed, preserve that id.
         *
         * If no id exists, the message is treated as a
         * notification and no response is emitted.
         */

        if (exception.isInvalidParams()) {

            sendInvalidParams(
                    exception
            );

            return;
        }


        /*
         * --------------------------------------------------------
         * Conversion
         * --------------------------------------------------------
         *
         * Conversion during initial request decoding is treated as
         * invalid params when a valid request id is available.
         *
         * Method-specific conversions normally happen later in a
         * request handler and are mapped by the runtime.
         */

        if (exception.isConversionError()) {

            sendConversionFailure(
                    exception
            );

            return;
        }


        /*
         * SERIALIZATION should never originate from
         * decodeRequest().
         *
         * Do not manufacture another protocol response from an
         * unexpected codec state when no reliable request context
         * exists.
         */
        onError(
                exception
        );
    }


    /*
     * ------------------------------------------------------------
     * Invalid request response
     * ------------------------------------------------------------
     */

    private void sendInvalidRequest(
            McpJsonCodecException exception
    ) {

        Object requestId =
                exception.getRequestId();

        sendResponse(
                McpResponse.failure(
                        requestId,
                        McpError.invalidRequest(
                                safeMessage(
                                        exception
                                )
                        )
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * Invalid params response
     * ------------------------------------------------------------
     */

    private void sendInvalidParams(
            McpJsonCodecException exception
    ) {

        McpRequestId requestId =
                exception.getRequestId();


        /*
         * No id means no JSON-RPC request response can be
         * correlated.
         *
         * In the MCP client->server direction a structurally
         * decoded message without an id is a notification, which
         * receives no response.
         */
        if (requestId == null) {
            return;
        }


        sendResponse(
                McpResponse.failure(
                        requestId,
                        McpError.invalidParams(
                                safeMessage(
                                        exception
                                ),
                                null
                        )
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * Conversion response
     * ------------------------------------------------------------
     */

    private void sendConversionFailure(
            McpJsonCodecException exception
    ) {

        McpRequestId requestId =
                exception.getRequestId();


        if (requestId == null) {

            /*
             * No response to a notification.
             */
            return;
        }


        sendResponse(
                McpResponse.failure(
                        requestId,
                        McpError.invalidParams(
                                safeMessage(
                                        exception
                                ),
                                null
                        )
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * Response sending
     * ------------------------------------------------------------
     */

    private void sendResponse(
            McpResponse response
    ) {

        Objects.requireNonNull(
                response,
                "MCP response must not be null."
        );


        final String json;

        try {

            json =
                    codec.encodeResponse(
                            response
                    );

        } catch (McpJsonCodecException exception) {

            /*
             * Do not attempt to encode a second JSON-RPC response
             * using the same failed codec path.
             */
            onError(
                    exception
            );

            return;

        } catch (RuntimeException exception) {

            onError(
                    exception
            );

            return;
        }


        try {

            transport.send(
                    json
            );

        } catch (RuntimeException exception) {

            onError(
                    exception
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public McpTransport getTransport() {
        return transport;
    }


    public McpJsonCodec getCodec() {
        return codec;
    }


    public McpServerRuntime getRuntime() {
        return runtime;
    }


    /*
     * ------------------------------------------------------------
     * Utility
     * ------------------------------------------------------------
     */

    private static String safeMessage(
            Throwable throwable
    ) {

        if (throwable == null) {
            return "Unknown error";
        }


        String message =
                throwable.getMessage();


        if (message == null
                || message.trim().isEmpty()) {

            return throwable
                    .getClass()
                    .getSimpleName();
        }


        return message.trim();
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "McpServerTransportListener{"
                + "transport="
                + transport
                + ", codec="
                + codec.getClass().getSimpleName()
                + ", runtime="
                + runtime.getClass().getSimpleName()
                + '}';
    }
}