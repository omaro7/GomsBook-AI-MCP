/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server.runtime;

import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.discovery.McpClientCapabilities;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpClientCapabilities.ElicitationCapability;

/**
 * Thrown when processing an MCP request requires one or more
 * client capabilities that were not advertised by the client.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This exception is mapped by the MCP server runtime to:
 * </p>
 *
 * <pre>
 * -32021 MissingRequiredClientCapability
 * </pre>
 *
 * <p>
 * The required capabilities are preserved as a complete
 * {@link McpClientCapabilities} object so they can be serialized
 * directly into the protocol-defined error data structure.
 * </p>
 */
public final class McpMissingClientCapabilityException
        extends RuntimeException {

    private static final long serialVersionUID =
            1L;

    private final McpClientCapabilities requiredCapabilities;


    /**
     * Creates an exception for missing required client
     * capabilities.
     *
     * @param requiredCapabilities capabilities required by the
     *        current operation
     */
    public McpMissingClientCapabilityException(
            McpClientCapabilities requiredCapabilities
    ) {

        this(
                requiredCapabilities,
                null
        );
    }


    /**
     * Creates an exception for missing required client
     * capabilities with an underlying cause.
     *
     * @param requiredCapabilities capabilities required by the
     *        current operation
     * @param cause underlying cause
     */
    public McpMissingClientCapabilityException(
            McpClientCapabilities requiredCapabilities,
            Throwable cause
    ) {

        super(
                createMessage(
                        requiredCapabilities
                ),
                cause
        );

        this.requiredCapabilities =
                requireCapabilities(
                        requiredCapabilities
                );
    }


    /*
     * ------------------------------------------------------------
     * Convenience factories
     * ------------------------------------------------------------
     */

    /**
     * Creates an exception requiring baseline elicitation
     * capability.
     *
     * @return exception
     */
    public static McpMissingClientCapabilityException elicitation() {

        return new McpMissingClientCapabilityException(
                McpClientCapabilities.builder()
                        .elicitation(
                        		ElicitationCapability
                                        .form()
                        )
                        .build()
        );
    }


    /**
     * Creates an exception requiring form-based elicitation.
     *
     * @return exception
     */
    public static McpMissingClientCapabilityException elicitationForm() {

        return new McpMissingClientCapabilityException(
                McpClientCapabilities.builder()
                        .elicitation(
                        		ElicitationCapability
                                        .form()
                        )
                        .build()
        );
    }


    /**
     * Creates an exception requiring URL-based elicitation.
     *
     * @return exception
     */
    public static McpMissingClientCapabilityException elicitationUrl() {

        return new McpMissingClientCapabilityException(
                McpClientCapabilities.builder()
		                .elicitation(
		                        McpClientCapabilities
		                        .ElicitationCapability
		                        .builder()
		                        .url()
		                        .build()
		                )
                        .build()
        );
    }


    /*
     * ------------------------------------------------------------
     * Accessor
     * ------------------------------------------------------------
     */

    /**
     * Returns the capabilities required by the current operation.
     *
     * @return required client capabilities
     */
    public McpClientCapabilities getRequiredCapabilities() {
        return requiredCapabilities;
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static McpClientCapabilities requireCapabilities(
            McpClientCapabilities capabilities
    ) {

        return Objects.requireNonNull(
                capabilities,
                "Required MCP client capabilities must not be null."
        );
    }


    private static String createMessage(
            McpClientCapabilities capabilities
    ) {

        McpClientCapabilities required =
                requireCapabilities(
                        capabilities
                );

        return "Missing required MCP client capabilities: "
                + required;
    }
}