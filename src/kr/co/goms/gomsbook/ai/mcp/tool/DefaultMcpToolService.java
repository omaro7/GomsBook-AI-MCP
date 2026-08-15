/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.core.McpToolRegistry;

/**
 * Default implementation of {@link McpToolService}.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Responsibilities:
 * </p>
 *
 * <ul>
 *     <li>list registered MCP tools</li>
 *     <li>apply deterministic tool ordering</li>
 *     <li>apply cursor based pagination</li>
 *     <li>execute a requested MCP tool</li>
 *     <li>return protocol-compatible MCP result models</li>
 * </ul>
 */
public final class DefaultMcpToolService
        implements McpToolService {

    public static final long DEFAULT_LIST_TTL_MS =
            3_600_000L;

    public static final int DEFAULT_PAGE_SIZE =
            100;

    private final McpToolRegistry toolRegistry;

    private final int pageSize;

    private final long listTtlMs;

    private final String listCacheScope;


    /*
     * ------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------
     */

    public DefaultMcpToolService(
            McpToolRegistry toolRegistry
    ) {

        this(
                toolRegistry,
                DEFAULT_PAGE_SIZE,
                DEFAULT_LIST_TTL_MS,
                McpListToolsResult.CACHE_SCOPE_PUBLIC
        );
    }


    public DefaultMcpToolService(
            McpToolRegistry toolRegistry,
            int pageSize,
            long listTtlMs,
            String listCacheScope
    ) {

        this.toolRegistry =
                Objects.requireNonNull(
                        toolRegistry,
                        "MCP tool registry must not be null."
                );

        this.pageSize =
                requirePageSize(
                        pageSize
                );

        this.listTtlMs =
                requireTtlMs(
                        listTtlMs
                );

        this.listCacheScope =
                requireCacheScope(
                        listCacheScope
                );
    }


    /*
     * ------------------------------------------------------------
     * McpToolService
     * ------------------------------------------------------------
     */

    @Override
    public McpListToolsResult listTools(
            McpListToolsParams params
    ) {

        McpListToolsParams effectiveParams =
                params == null
                        ? McpListToolsParams.create()
                        : params;


        List<McpTool> tools =
                loadTools();


        int offset =
                decodeCursor(
                        effectiveParams.getCursor(),
                        tools.size()
                );


        int toIndex =
                Math.min(
                        offset + pageSize,
                        tools.size()
                );


        List<McpTool> page =
                tools.subList(
                        offset,
                        toIndex
                );


        String nextCursor =
                toIndex < tools.size()
                        ? encodeCursor(
                                toIndex
                        )
                        : null;


        return McpListToolsResult.builder()
                .tools(
                        page
                )
                .nextCursor(
                        nextCursor
                )
                .ttlMs(
                        listTtlMs
                )
                .cacheScope(
                        listCacheScope
                )
                .build();
    }


    @Override
    public McpResult callTool(
            McpCallToolParams params
    ) {

        validateCallParams(
                params
        );

        String name =
                params.getName()
                        .trim();

        McpTool tool =
                toolRegistry.find(
                        name
                );

        if (tool == null) {

            throw new McpToolNotFoundException(
                    name
            );
        }

        McpToolResult result =
                tool.execute(
                        normalizeArguments(
                                params.getArguments()
                        )
                );

        if (result == null) {

            throw new IllegalStateException(
                    "MCP tool returned null result: "
                            + name
            );
        }

        return result;
    }


    /*
     * ------------------------------------------------------------
     * Tool listing
     * ------------------------------------------------------------
     */

    private List<McpTool> loadTools() {

        List<McpTool> registered = toolRegistry.getTools();

        if (registered == null || registered.isEmpty()) {
            return Collections.emptyList();
        }

        List<McpTool> tools = new ArrayList<>();

        for (McpTool tool : registered) {
            if (tool != null) {
                tools.add(
                        tool
                );
            }
        }

        /*
         * MCP list results should remain deterministic while
         * underlying data has not changed.
         */
        tools.sort(
                Comparator.comparing(
                        DefaultMcpToolService::toolName,
                        String.CASE_INSENSITIVE_ORDER
                )
                .thenComparing(
                        DefaultMcpToolService::toolName
                )
        );

        return Collections.unmodifiableList(
                tools
        );
    }


    private static String toolName(
            McpTool tool
    ) {
        if (tool == null || tool.getName() == null) {
            return "";
        }
        return tool.getName();
    }


    /*
     * ------------------------------------------------------------
     * Call validation
     * ------------------------------------------------------------
     */

    private static void validateCallParams(
            McpCallToolParams params
    ) {

        if (params == null) {

            throw new IllegalArgumentException(
                    "MCP tools/call params must not be null."
            );
        }


        String name =
                params.getName();


        if (name == null
                || name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP tools/call requires a tool name."
            );
        }
    }


    private static Map<String, Object> normalizeArguments(
            Map<String, Object> arguments
    ) {

        if (arguments == null
                || arguments.isEmpty()) {

            return Collections.emptyMap();
        }


        return Collections.unmodifiableMap(
                arguments
        );
    }


    /*
     * ------------------------------------------------------------
     * Cursor
     * ------------------------------------------------------------
     */

    /**
     * Encodes a stable list offset as the MCP cursor.
     *
     * <p>
     * The cursor is intentionally opaque to MCP clients even
     * though the current server implementation uses a numeric
     * offset internally.
     * </p>
     */
    private static String encodeCursor(
            int offset
    ) {

        return Integer.toString(
                offset
        );
    }


    private static int decodeCursor(
            String cursor,
            int size
    ) {

        if (cursor == null
                || cursor.trim().isEmpty()) {

            return 0;
        }


        final int offset;

        try {

            offset =
                    Integer.parseInt(
                            cursor.trim()
                    );

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "Invalid MCP tools/list cursor: "
                            + cursor,
                    exception
            );
        }


        if (offset < 0
                || offset > size) {

            throw new IllegalArgumentException(
                    "MCP tools/list cursor is out of range: "
                            + cursor
            );
        }


        return offset;
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static int requirePageSize(
            int pageSize
    ) {

        if (pageSize <= 0) {

            throw new IllegalArgumentException(
                    "MCP tool page size must be greater than zero."
            );
        }

        return pageSize;
    }


    private static long requireTtlMs(
            long ttlMs
    ) {

        if (ttlMs < 0L) {

            throw new IllegalArgumentException(
                    "MCP tools/list ttlMs must not be negative."
            );
        }

        return ttlMs;
    }


    private static String requireCacheScope(
            String cacheScope
    ) {

        Objects.requireNonNull(
                cacheScope,
                "MCP tools/list cacheScope must not be null."
        );


        String normalized =
                cacheScope.trim();


        if (McpListToolsResult.CACHE_SCOPE_PUBLIC.equals(
                normalized
        )) {

            return McpListToolsResult.CACHE_SCOPE_PUBLIC;
        }


        if (McpListToolsResult.CACHE_SCOPE_PRIVATE.equals(
                normalized
        )) {

            return McpListToolsResult.CACHE_SCOPE_PRIVATE;
        }


        throw new IllegalArgumentException(
                "Unsupported MCP tools/list cacheScope: "
                        + normalized
        );
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public McpToolRegistry getToolRegistry() {
        return toolRegistry;
    }


    public int getPageSize() {
        return pageSize;
    }


    public long getListTtlMs() {
        return listTtlMs;
    }


    public String getListCacheScope() {
        return listCacheScope;
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "DefaultMcpToolService{"
                + "pageSize="
                + pageSize
                + ", listTtlMs="
                + listTtlMs
                + ", listCacheScope='"
                + listCacheScope
                + '\''
                + '}';
    }
}