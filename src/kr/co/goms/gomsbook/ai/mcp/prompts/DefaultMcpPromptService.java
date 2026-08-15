/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link McpPromptService}.
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
 *     <li>list registered MCP prompts</li>
 *     <li>apply deterministic prompt ordering</li>
 *     <li>apply cursor-based pagination</li>
 *     <li>resolve prompts by name</li>
 *     <li>validate required prompt arguments</li>
 *     <li>produce MCP prompt result models</li>
 * </ul>
 */
public final class DefaultMcpPromptService
        implements McpPromptService {

    public static final int DEFAULT_PAGE_SIZE =
            100;

    public static final long DEFAULT_LIST_TTL_MS =
            3_600_000L;


    private final McpPromptRegistry promptRegistry;

    private final int pageSize;

    private final long listTtlMs;

    private final String listCacheScope;


    /*
     * ------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------
     */

    public DefaultMcpPromptService(
            McpPromptRegistry promptRegistry
    ) {

        this(
                promptRegistry,
                DEFAULT_PAGE_SIZE,
                DEFAULT_LIST_TTL_MS,
                McpListPromptsResult.CACHE_SCOPE_PUBLIC
        );
    }


    public DefaultMcpPromptService(
            McpPromptRegistry promptRegistry,
            int pageSize,
            long listTtlMs,
            String listCacheScope
    ) {

        this.promptRegistry =
                Objects.requireNonNull(
                        promptRegistry,
                        "MCP prompt registry must not be null."
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
     * McpPromptService
     * ------------------------------------------------------------
     */

    @Override
    public McpListPromptsResult listPrompts(
            McpListPromptsParams params
    ) {

        McpListPromptsParams effectiveParams =
                params == null
                        ? McpListPromptsParams.empty()
                        : params;


        List<McpPrompt> prompts =
                loadPrompts();


        int offset =
                decodeCursor(
                        effectiveParams.getCursor(),
                        prompts.size()
                );


        int toIndex =
                Math.min(
                        offset + pageSize,
                        prompts.size()
                );


        List<McpPrompt> page =
                prompts.subList(
                        offset,
                        toIndex
                );


        String nextCursor =
                toIndex < prompts.size()
                        ? encodeCursor(
                                toIndex
                        )
                        : null;


        return McpListPromptsResult.builder()
                .prompts(
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
    public McpGetPromptResult getPrompt(
            McpGetPromptParams params
    ) {

        validateGetParams(
                params
        );


        String name =
                params.getName()
                        .trim();


        McpPromptProvider provider =
                promptRegistry.find(
                                name)
                        .orElseThrow(
                                () ->
                                        new McpPromptNotFoundException(
                                                name));


        if (provider == null) {

            throw new McpPromptNotFoundException(
                    name
            );
        }


        McpPrompt prompt =
                provider.getPrompt();


        if (prompt == null) {

            throw new IllegalStateException(
                    "MCP prompt provider returned null prompt: "
                            + name
            );
        }


        Map<String, String> arguments =
                normalizeArguments(
                        params.getArguments()
                );


        validateRequiredArguments(
                prompt,
                arguments
        );


        McpGetPromptResult result =
                provider.get(
                        arguments);

        if (result == null) {

            throw new IllegalStateException(
                    "MCP prompt provider returned null result: "
                            + name);
        }

        return result;
    }


    /*
     * ------------------------------------------------------------
     * Prompt loading
     * ------------------------------------------------------------
     */

    private List<McpPrompt> loadPrompts() {

        Collection<McpPromptProvider> providers = promptRegistry.getAll();

        if (providers == null
                || providers.isEmpty()) {

            return Collections.emptyList();
        }

        List<McpPrompt> prompts = new ArrayList<>();

        for (McpPromptProvider provider : providers) {

            if (provider == null) {

                continue;
            }

            McpPrompt prompt =
                    provider.getPrompt();

            if (prompt != null) {

                prompts.add(
                        prompt);
            }
        }

        if (prompts.isEmpty()) {

            return Collections.emptyList();
        }

        /*
         * Stable ordering is important for deterministic
         * pagination and cache reuse.
         */
        prompts.sort(
                Comparator.comparing(
                        DefaultMcpPromptService::promptName,
                        String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(
                                DefaultMcpPromptService::promptName));

        return Collections.unmodifiableList(
                prompts);
    }


    private static String promptName(
            McpPrompt prompt
    ) {

        if (prompt == null
                || prompt.getName() == null) {

            return "";
        }

        return prompt.getName();
    }


    /*
     * ------------------------------------------------------------
     * prompts/get validation
     * ------------------------------------------------------------
     */

    private static void validateGetParams(
            McpGetPromptParams params
    ) {

        if (params == null) {

            throw new IllegalArgumentException(
                    "MCP prompts/get params must not be null."
            );
        }


        String name =
                params.getName();


        if (name == null
                || name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP prompts/get requires a prompt name."
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Prompt argument validation
     * ------------------------------------------------------------
     */

    private static void validateRequiredArguments(
            McpPrompt prompt,
            Map<String, String> arguments
    ) {

        List<McpPromptArgument> definitions =
                prompt.getArguments();


        if (definitions == null
                || definitions.isEmpty()) {

            return;
        }


        for (McpPromptArgument argument : definitions) {

            if (argument == null
                    || !argument.isRequired()) {

                continue;
            }


            String name =
                    argument.getName();


            if (name == null
                    || name.trim().isEmpty()) {

                throw new IllegalStateException(
                        "MCP prompt contains an invalid required "
                                + "argument definition."
                );
            }


            String value =
                    arguments.get(
                            name
                    );


            if (value == null
                    || value.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Missing required MCP prompt argument: "
                                + name
                );
            }
        }
    }


    private static Map<String, String> normalizeArguments(
            Map<String, String> arguments
    ) {

        if (arguments == null
                || arguments.isEmpty()) {

            return Collections.emptyMap();
        }


        Map<String, String> normalized =
                new LinkedHashMap<>();


        for (Map.Entry<String, String> entry
                : arguments.entrySet()) {

            String key =
                    normalizeArgumentName(
                            entry.getKey()
                    );


            String value =
                    entry.getValue();


            if (value != null) {

                normalized.put(
                        key,
                        value
                );
            }
        }


        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }


        return Collections.unmodifiableMap(
                normalized
        );
    }


    private static String normalizeArgumentName(
            String name
    ) {

        Objects.requireNonNull(
                name,
                "MCP prompt argument name must not be null."
        );


        String normalized =
                name.trim();


        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP prompt argument name must not be blank."
            );
        }


        return normalized;
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
                    "Invalid MCP prompts/list cursor: "
                            + cursor,
                    exception
            );
        }


        if (offset < 0
                || offset > size) {

            throw new IllegalArgumentException(
                    "MCP prompts/list cursor is out of range: "
                            + cursor
            );
        }


        return offset;
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
                    "MCP prompt page size must be greater than zero."
            );
        }

        return pageSize;
    }


    private static long requireTtlMs(
            long ttlMs
    ) {

        if (ttlMs < 0L) {

            throw new IllegalArgumentException(
                    "MCP prompts/list ttlMs must not be negative."
            );
        }

        return ttlMs;
    }


    private static String requireCacheScope(
            String cacheScope
    ) {

        Objects.requireNonNull(
                cacheScope,
                "MCP prompts/list cacheScope must not be null."
        );


        String normalized =
                cacheScope.trim();


        if (McpListPromptsResult.CACHE_SCOPE_PUBLIC.equals(
                normalized
        )) {

            return McpListPromptsResult.CACHE_SCOPE_PUBLIC;
        }


        if (McpListPromptsResult.CACHE_SCOPE_PRIVATE.equals(
                normalized
        )) {

            return McpListPromptsResult.CACHE_SCOPE_PRIVATE;
        }


        throw new IllegalArgumentException(
                "Unsupported MCP prompts/list cacheScope: "
                        + normalized
        );
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public McpPromptRegistry getPromptRegistry() {
        return promptRegistry;
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

        return "DefaultMcpPromptService{"
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