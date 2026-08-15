/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

import java.util.Collection;
import java.util.Optional;

/**
 * Registry for MCP prompt providers.
 */
public interface McpPromptRegistry {

    /**
     * Registers a prompt provider.
     *
     * @param provider prompt provider
     */
    void register(
            McpPromptProvider provider
    );

    /**
     * Unregisters the prompt provider with the given name.
     *
     * @param name prompt name
     * @return {@code true} if a provider was removed
     */
    boolean unregister(
            String name
    );

    /**
     * Finds a prompt provider by prompt name.
     *
     * @param name prompt name
     * @return matching provider if present
     */
    Optional<McpPromptProvider> find(
            String name
    );

    /**
     * Returns all registered prompt providers.
     *
     * @return registered providers
     */
    Collection<McpPromptProvider> getAll();

    /**
     * Checks whether a prompt with the given name is registered.
     *
     * @param name prompt name
     * @return {@code true} if registered
     */
    default boolean contains(
            String name
    ) {

        return find(
                name
        ).isPresent();
    }

    /**
     * Returns the number of registered prompt providers.
     *
     * @return provider count
     */
    default int size() {

        return getAll()
                .size();
    }

    /**
     * Checks whether the registry is empty.
     *
     * @return {@code true} if no prompt providers are registered
     */
    default boolean isEmpty() {

        return getAll()
                .isEmpty();
    }
}