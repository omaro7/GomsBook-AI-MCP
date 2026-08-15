/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server.runtime;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpError;
import kr.co.goms.gomsbook.ai.mcp.core.McpRequest;
import kr.co.goms.gomsbook.ai.mcp.core.McpRequestMetadata;
import kr.co.goms.gomsbook.ai.mcp.core.McpResponse;
import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpClientCapabilities;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpClientCapabilities.ElicitationCapability;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpMethodNotFoundException;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestContext;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestDispatcher;
import kr.co.goms.gomsbook.ai.mcp.server.McpServerConfig;

/**
 * Default stateless MCP server runtime.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Every MCP request is processed independently.
 * No protocol-level initialization or session state is retained
 * between requests.
 * </p>
 */
public final class DefaultMcpServerRuntime
        implements McpServerRuntime {

    private final McpServerConfig config;

    private final McpRequestDispatcher dispatcher;


    /**
     * Creates the default MCP server runtime.
     *
     * @param config server configuration
     * @param dispatcher request dispatcher
     */
    public DefaultMcpServerRuntime(
            McpServerConfig config,
            McpRequestDispatcher dispatcher
    ) {

        this.config =
                Objects.requireNonNull(
                        config,
                        "MCP server config must not be null."
                );

        this.dispatcher =
                Objects.requireNonNull(
                        dispatcher,
                        "MCP request dispatcher must not be null."
                );
    }


	@Override
    public McpResponse handle(
            McpRequest request
    ) {

        Objects.requireNonNull(
                request,
                "MCP request must not be null."
        );

        /*
         * JSON-RPC notifications do not receive a response.
         *
         * Notification dispatching may be introduced separately.
         */
        if (request.isNotification()) {
            return null;
        }


        McpResponse validationResponse =
                validateRequest(
                        request
                );

        if (validationResponse != null) {
            return validationResponse;
        }


        McpRequestContext context =
                createContext(
                        request
                );


        try {

            McpResult result =
                    dispatcher.dispatch(
                            context
                    );


            if (result == null) {

                /*
                 * Dispatcher already guards against this,
                 * but keep a runtime boundary invariant.
                 */
                throw new IllegalStateException(
                        "MCP request dispatcher returned null result."
                );
            }


            return McpResponse.success(
                    request.getId(),
                    result
            );


        } catch (McpMethodNotFoundException exception) {

            return McpResponse.methodNotFound(
                    request.getId(),
                    exception.getMethod()
            );


        } catch (McpMissingClientCapabilityException exception) {

            return McpResponse.failure(
                    request.getId(),
                    McpError.missingRequiredClientCapability(
                            exception.getRequiredCapabilities()
                    )
            );


        } catch (IllegalArgumentException exception) {

            /*
             * Method-specific parameter validation failures,
             * unknown resource/prompt references converted by
             * handlers, etc.
             */
            return McpResponse.invalidParams(
                    request.getId(),
                    safeMessage(
                            exception
                    )
            );


        } catch (RuntimeException exception) {

            /*
             * Do not expose stack traces or exception objects
             * over the MCP wire.
             */
            return McpResponse.internalError(
                    request.getId(),
                    safeMessage(
                            exception
                    )
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Common request validation
     * ------------------------------------------------------------
     */

    /**
     * Validates protocol-level information already decoded into
     * {@link McpRequest}.
     *
     * <p>
     * Structural JSON / JSON-RPC validation belongs to the codec.
     * This method performs server-runtime validation only.
     * </p>
     *
     * @param request request
     * @return error response, or {@code null} when valid
     */
    private McpResponse validateRequest(
            McpRequest request
    ) {

        /*
         * Normal MCP requests require an id.
         *
         * Notifications are handled before reaching this method.
         */
        if (!request.hasId()) {

            return McpResponse.invalidRequest();
        }


        McpRequestMetadata metadata = request.getMetadata();


        /*
         * McpRequest currently requires metadata at construction
         * time, so this is defensive validation.
         *
         * Missing mandatory MCP params metadata maps to
         * -32602 Invalid params.
         */
        if (metadata == null) {

            return McpResponse.invalidParams(
                    request.getId(),
                    "MCP request metadata is required."
            );
        }


        String protocolVersion =
                metadata.getProtocolVersion();


        if (protocolVersion == null
                || protocolVersion.trim().isEmpty()) {

            return McpResponse.invalidParams(
                    request.getId(),
                    "MCP protocol version is required."
            );
        }


        /*
         * Important:
         *
         * McpRequestMetadata preserves unsupported version
         * strings. Runtime is responsible for deciding whether
         * the server supports the requested version.
         */
        if (!config.supportsVersion(
                protocolVersion
        )) {

            return McpResponse.error(
                    request.getId(),
                    McpError.unsupportedProtocolVersion(
                            protocolVersion,
                            config.getSupportedVersions()
                    )
            );
        }


        /*
         * clientCapabilities is mandatory request metadata.
         *
         * Missing mandatory metadata is Invalid params (-32602),
         * not MissingRequiredClientCapability (-32021).
         *
         * -32021 is used only when the client supplied its
         * capabilities but a particular operation requires a
         * capability it did not advertise.
         */
        if (metadata.getClientCapabilities() == null) {

            return McpResponse.invalidParams(
                    request.getId(),
                    "MCP client capabilities are required."
            );
        }


        return null;
    }


    /*
     * ------------------------------------------------------------
     * Request context
     * ------------------------------------------------------------
     */

    private McpRequestContext createContext(
            McpRequest request
    ) {

        return McpRequestContext.builder()
                .request(
                        request
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Missing capability conversion
     * ------------------------------------------------------------
     */

    /**
     * Converts the current string-based
     * {@link McpMissingClientCapabilityException} representation
     * into an MCP ClientCapabilities object suitable for the
     * -32021 error payload.
     *
     * <p>
     * This method can be removed when
     * McpMissingClientCapabilityException is changed to carry
     * McpClientCapabilities directly.
     * </p>
     */
    private static McpClientCapabilities createRequiredCapabilities(
            String capability
    ) {

        Objects.requireNonNull(
                capability,
                "Required MCP client capability must not be null."
        );


        String normalized =
                capability.trim();


        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "Required MCP client capability must not be blank."
            );
        }


        /*
         * Elicitation requirement.
         */
        if ("elicitation".equals(
                normalized
        )
                || "elicitation.form".equals(
                        normalized
                )) {

            return McpClientCapabilities.builder()
                    .elicitation(
                    		ElicitationCapability
                                    .form()
                    )
                    .build();
        }


        if ("elicitation.url".equals(
                normalized
        )) {

            return McpClientCapabilities.builder()
                    .elicitation(
                            McpClientCapabilities
                            .ElicitationCapability
                            .builder()
                            .url()
                            .build()
                    )
                    .build();
        }


        /*
         * Do not silently encode an unknown capability using an
         * unrelated standard field.
         *
         * The exception should eventually carry the complete
         * McpClientCapabilities object directly.
         */
        throw new IllegalArgumentException(
                "Unsupported MCP required client capability: "
                        + normalized
        );
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public McpServerConfig getConfig() {
        return config;
    }


    public McpRequestDispatcher getDispatcher() {
        return dispatcher;
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

        return "DefaultMcpServerRuntime{"
                + "config="
                + config
                + ", dispatcher="
                + dispatcher
                + '}';
    }
}