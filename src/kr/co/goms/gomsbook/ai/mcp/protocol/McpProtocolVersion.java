/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.protocol;

/**
 * MCP(Model Context Protocol) protocol version constants.
 *
 * <p>
 * This class centralizes protocol version values used by
 * MCP client/server implementations.
 * </p>
 */
public final class McpProtocolVersion {

    /**
     * Current MCP protocol version supported by GomsBook AI MCP.
     */
    public static final String CURRENT = "2026-07-28";

    /**
     * Previous stable MCP protocol version.
     *
     * <p>
     * Kept for compatibility when communicating with
     * MCP implementations that have not yet migrated
     * to the current protocol version.
     * </p>
     */
    public static final String V_2025_11_25 =
            "2025-11-25";

    /**
     * MCP protocol version released on 2025-06-18.
     */
    public static final String V_2025_06_18 =
            "2025-06-18";

    /**
     * MCP protocol version released on 2025-03-26.
     */
    public static final String V_2025_03_26 =
            "2025-03-26";

    /**
     * MCP protocol version released on 2024-11-05.
     */
    public static final String V_2024_11_05 =
            "2024-11-05";

    private McpProtocolVersion() {
        throw new AssertionError(
                "McpProtocolVersion must not be instantiated."
        );
    }

    /**
     * Checks whether the supplied protocol version is supported.
     *
     * @param version protocol version
     * @return {@code true} when the version is supported
     */
    public static boolean isSupported(
            String version) {

        if (version == null
                || version.isBlank()) {

            return false;
        }

        return CURRENT.equals(version)
                || V_2025_11_25.equals(version)
                || V_2025_06_18.equals(version)
                || V_2025_03_26.equals(version)
                || V_2024_11_05.equals(version);
    }

    /**
     * Resolves a protocol version.
     *
     * <p>
     * When the supplied value is {@code null} or blank,
     * the current protocol version is returned.
     * </p>
     *
     * @param version protocol version
     * @return resolved protocol version
     * @throws IllegalArgumentException
     *         when the protocol version is unsupported
     */
    public static String resolve(
            String version) {

        if (version == null
                || version.isBlank()) {

            return CURRENT;
        }

        String normalized =
                version.trim();

        if (!isSupported(normalized)) {

            throw new IllegalArgumentException(
                    "Unsupported MCP protocol version: "
                            + normalized
            );
        }

        return normalized;
    }
}