/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.discovery;

/**
 * Service responsible for handling MCP {@code server/discover}.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Discovery is stateless. Implementations return the server's
 * supported protocol versions, capabilities, cache information,
 * optional instructions, and server metadata.
 * </p>
 */
public interface McpDiscoveryService {

    /**
     * Executes MCP server discovery.
     *
     * @param params discovery parameters
     * @return discovery result
     */
    McpDiscoverResult discover(
            McpDiscoverParams params
    );
}