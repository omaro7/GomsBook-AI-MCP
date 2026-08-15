/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.protocol;

/**
 * Represents an MCP client request that may be embedded inside
 * {@link McpInputRequiredResult}.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * MCP Multi Round-Trip Requests may return embedded client
 * requests when a server requires additional client-side input
 * before the original request can be completed.
 * </p>
 *
 * <p>
 * Implementations represent concrete MCP client request types,
 * such as elicitation or other protocol-defined input requests.
 * </p>
 */
public interface McpInputRequest {

    /**
     * Returns the MCP method represented by this embedded request.
     *
     * @return MCP method name
     */
    String getMethod();
}