/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;

/**
 * Default implementation of {@link McpResourceService}.
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
 *     <li>list registered MCP resources</li>
 *     <li>list registered MCP resource templates</li>
 *     <li>read MCP resources</li>
 *     <li>apply deterministic ordering</li>
 *     <li>apply cursor-based pagination</li>
 *     <li>produce cacheable MCP result models</li>
 * </ul>
 */
public final class DefaultMcpResourceService
        implements McpResourceService {

    public static final int DEFAULT_PAGE_SIZE =
            100;

    public static final long DEFAULT_LIST_TTL_MS =
            3_600_000L;

    public static final long DEFAULT_READ_TTL_MS =
            60_000L;


    private final McpResourceRegistry resourceRegistry;

    private final int pageSize;

    private final long listTtlMs;

    private final long readTtlMs;

    private final String listCacheScope;

    private final String readCacheScope;


    /*
     * ------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------
     */

    public DefaultMcpResourceService(
            McpResourceRegistry resourceRegistry
    ) {

        this(
                resourceRegistry,
                DEFAULT_PAGE_SIZE,
                DEFAULT_LIST_TTL_MS,
                DEFAULT_READ_TTL_MS,
                McpListResourcesResult.CACHE_SCOPE_PUBLIC,
                McpReadResourceResult.CACHE_SCOPE_PRIVATE
        );
    }


    public DefaultMcpResourceService(
            McpResourceRegistry resourceRegistry,
            int pageSize,
            long listTtlMs,
            long readTtlMs,
            String listCacheScope,
            String readCacheScope
    ) {

        this.resourceRegistry =
                Objects.requireNonNull(
                        resourceRegistry,
                        "MCP resource registry must not be null."
                );

        this.pageSize =
                requirePageSize(
                        pageSize
                );

        this.listTtlMs =
                requireTtlMs(
                        listTtlMs,
                        "MCP resource list ttlMs"
                );

        this.readTtlMs =
                requireTtlMs(
                        readTtlMs,
                        "MCP resource read ttlMs"
                );

        this.listCacheScope =
                requireListCacheScope(
                        listCacheScope
                );

        this.readCacheScope =
                requireReadCacheScope(
                        readCacheScope
                );
    }


    /*
     * ------------------------------------------------------------
     * McpResourceService
     * ------------------------------------------------------------
     */

    @Override
    public McpListResourcesResult listResources(
            McpListResourcesParams params
    ) {

        McpListResourcesParams effectiveParams =
                params == null
                        ? McpListResourcesParams.empty()
                        : params;


        List<McpResource> resources =
                loadResources();


        int offset =
                decodeCursor(
                        effectiveParams.getCursor(),
                        resources.size(),
                        "resources/list"
                );


        int toIndex =
                Math.min(
                        offset + pageSize,
                        resources.size()
                );


        List<McpResource> page =
                resources.subList(
                        offset,
                        toIndex
                );


        String nextCursor =
                toIndex < resources.size()
                        ? encodeCursor(
                                toIndex
                        )
                        : null;


        return McpListResourcesResult.builder()
                .resources(
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
    public McpReadResourceResult readResource(
            McpReadResourceParams params
    ) {

        validateReadParams(
                params
        );


        String uri =
                params.getUri()
                        .trim();


        McpResourceProvider provider =
                resourceRegistry.findProvider(
                                uri)
                        .orElseThrow(
                                () ->
                                        new McpResourceNotFoundException(
                                                uri));

        if (provider == null) {

            throw new McpResourceNotFoundException(
                    uri
            );
        }


        List<McpResourceContents> contents =
                provider.read(
                        uri
                );


        if (contents == null) {

            throw new IllegalStateException(
                    "MCP resource provider returned null contents: "
                            + uri
            );
        }


        return McpReadResourceResult.builder()
                .contents(
                        contents
                )
                .ttlMs(
                        readTtlMs
                )
                .cacheScope(
                        readCacheScope
                )
                .build();
    }


    @Override
    public McpListResourceTemplatesResult listResourceTemplates(
            McpListResourceTemplatesParams params
    ) {

        McpListResourceTemplatesParams effectiveParams =
                params == null
                        ? McpListResourceTemplatesParams.empty()
                        : params;


        List<McpResourceTemplate> templates =
                loadResourceTemplates();


        int offset =
                decodeCursor(
                        effectiveParams.getCursor(),
                        templates.size(),
                        "resources/templates/list"
                );


        int toIndex =
                Math.min(
                        offset + pageSize,
                        templates.size()
                );


        List<McpResourceTemplate> page =
                templates.subList(
                        offset,
                        toIndex
                );


        String nextCursor =
                toIndex < templates.size()
                        ? encodeCursor(
                                toIndex
                        )
                        : null;


        return McpListResourceTemplatesResult.builder()
                .resourceTemplates(
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


    /*
     * ------------------------------------------------------------
     * Resource loading
     * ------------------------------------------------------------
     */

    private List<McpResource> loadResources() {

        List<McpResource> registered =
                new ArrayList<>();

        for (McpResourceProvider provider
                : resourceRegistry.getProviders()) {

            if (provider == null) {

                continue;
            }

            List<McpResource> providerResources =
                    provider.listResources();

            if (providerResources != null) {

                registered.addAll(
                        providerResources);
            }
        }


        if (registered == null
                || registered.isEmpty()) {

            return Collections.emptyList();
        }


        List<McpResource> resources =
                new ArrayList<>();


        for (McpResource resource : registered) {

            if (resource != null) {

                resources.add(
                        resource
                );
            }
        }


        /*
         * Stable ordering is important for cursor pagination
         * and response caching.
         */
        resources.sort(
                Comparator.comparing(
                        DefaultMcpResourceService::resourceUri,
                        String.CASE_INSENSITIVE_ORDER
                )
                .thenComparing(
                        DefaultMcpResourceService::resourceUri
                )
        );


        return Collections.unmodifiableList(
                resources
        );
    }


    private List<McpResourceTemplate> loadResourceTemplates() {

    	List<McpResourceTemplate> registered = new ArrayList<>();

    	for (McpResourceProvider provider
    	        : resourceRegistry.getProviders()) {

    	    List<McpResourceTemplate> templates =
    	            provider.listResourceTemplates();

    	    if (templates != null) {

    	        registered.addAll(
    	                templates);
    	    }
    	}


        if (registered == null
                || registered.isEmpty()) {

            return Collections.emptyList();
        }


        List<McpResourceTemplate> templates =
                new ArrayList<>();


        for (McpResourceTemplate template : registered) {

            if (template != null) {

                templates.add(
                        template
                );
            }
        }


        templates.sort(
                Comparator.comparing(
                        DefaultMcpResourceService::resourceTemplateUri,
                        String.CASE_INSENSITIVE_ORDER
                )
                .thenComparing(
                        DefaultMcpResourceService::resourceTemplateUri
                )
        );


        return Collections.unmodifiableList(
                templates
        );
    }


    /*
     * ------------------------------------------------------------
     * Read validation
     * ------------------------------------------------------------
     */

    private static void validateReadParams(
            McpReadResourceParams params
    ) {

        if (params == null) {

            throw new IllegalArgumentException(
                    "MCP resources/read params must not be null."
            );
        }


        String uri =
                params.getUri();


        if (uri == null
                || uri.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP resources/read requires a resource URI."
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Cursor
     * ------------------------------------------------------------
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
            int size,
            String method
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
                    "Invalid MCP "
                            + method
                            + " cursor: "
                            + cursor,
                    exception
            );
        }


        if (offset < 0
                || offset > size) {

            throw new IllegalArgumentException(
                    "MCP "
                            + method
                            + " cursor is out of range: "
                            + cursor
            );
        }


        return offset;
    }


    /*
     * ------------------------------------------------------------
     * Resource identifiers
     * ------------------------------------------------------------
     */

    private static String resourceUri(
            McpResource resource
    ) {

        if (resource == null
                || resource.getUri() == null) {

            return "";
        }

        return resource.getUri();
    }


    private static String resourceTemplateUri(
            McpResourceTemplate template
    ) {

        if (template == null
                || template.getUriTemplate() == null) {

            return "";
        }

        return template.getUriTemplate();
    }


    /*
     * ------------------------------------------------------------
     * Configuration validation
     * ------------------------------------------------------------
     */

    private static int requirePageSize(
            int pageSize
    ) {

        if (pageSize <= 0) {

            throw new IllegalArgumentException(
                    "MCP resource page size must be greater than zero."
            );
        }

        return pageSize;
    }


    private static long requireTtlMs(
            long ttlMs,
            String fieldName
    ) {

        if (ttlMs < 0L) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must not be negative."
            );
        }

        return ttlMs;
    }


    private static String requireListCacheScope(
            String cacheScope
    ) {

        Objects.requireNonNull(
                cacheScope,
                "MCP resource list cacheScope must not be null."
        );


        String normalized =
                cacheScope.trim();


        if (McpListResourcesResult.CACHE_SCOPE_PUBLIC.equals(
                normalized
        )) {

            return McpListResourcesResult.CACHE_SCOPE_PUBLIC;
        }


        if (McpListResourcesResult.CACHE_SCOPE_PRIVATE.equals(
                normalized
        )) {

            return McpListResourcesResult.CACHE_SCOPE_PRIVATE;
        }


        throw new IllegalArgumentException(
                "Unsupported MCP resource list cacheScope: "
                        + normalized
        );
    }


    private static String requireReadCacheScope(
            String cacheScope
    ) {

        Objects.requireNonNull(
                cacheScope,
                "MCP resource read cacheScope must not be null."
        );


        String normalized =
                cacheScope.trim();


        if (McpReadResourceResult.CACHE_SCOPE_PUBLIC.equals(
                normalized
        )) {

            return McpReadResourceResult.CACHE_SCOPE_PUBLIC;
        }


        if (McpReadResourceResult.CACHE_SCOPE_PRIVATE.equals(
                normalized
        )) {

            return McpReadResourceResult.CACHE_SCOPE_PRIVATE;
        }


        throw new IllegalArgumentException(
                "Unsupported MCP resource read cacheScope: "
                        + normalized
        );
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public McpResourceRegistry getResourceRegistry() {
        return resourceRegistry;
    }


    public int getPageSize() {
        return pageSize;
    }


    public long getListTtlMs() {
        return listTtlMs;
    }


    public long getReadTtlMs() {
        return readTtlMs;
    }


    public String getListCacheScope() {
        return listCacheScope;
    }


    public String getReadCacheScope() {
        return readCacheScope;
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "DefaultMcpResourceService{"
                + "pageSize="
                + pageSize
                + ", listTtlMs="
                + listTtlMs
                + ", readTtlMs="
                + readTtlMs
                + ", listCacheScope='"
                + listCacheScope
                + '\''
                + ", readCacheScope='"
                + readCacheScope
                + '\''
                + '}';
    }
}