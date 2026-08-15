/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.List;
import java.util.Optional;

/**
 * Registry for MCP completion providers.
 *
 * <p>
 * The registry manages {@link McpCompletionProvider} instances
 * and resolves a provider capable of handling a given
 * {@link McpCompleteParams} request.
 * </p>
 */
public interface McpCompletionRegistry {

    /**
     * Registers a completion provider.
     *
     * @param provider completion provider
     */
    void register(
            McpCompletionProvider provider);

    /**
     * Unregisters a completion provider.
     *
     * @param provider completion provider
     * @return {@code true} if the provider was removed
     */
    boolean unregister(
            McpCompletionProvider provider);

    /**
     * Finds a completion provider that supports
     * the given request parameters.
     *
     * @param params completion request parameters
     * @return matching provider, if present
     */
    Optional<McpCompletionProvider> findProvider(
            McpCompleteParams params);

    /**
     * Returns all registered completion providers.
     *
     * @return immutable provider list
     */
    List<McpCompletionProvider> getProviders();

    /**
     * Returns whether no completion providers are registered.
     *
     * @return {@code true} if empty
     */
    boolean isEmpty();

    /**
     * Returns the number of registered providers.
     *
     * @return provider count
     */
    int size();
}