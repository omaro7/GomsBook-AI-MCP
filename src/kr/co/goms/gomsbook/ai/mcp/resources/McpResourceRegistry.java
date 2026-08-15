/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.List;
import java.util.Optional;

/**
 * Registry for MCP resource providers.
 *
 * <p>
 * The registry manages resource providers and resolves
 * the appropriate provider for a resource URI.
 * </p>
 */
public interface McpResourceRegistry {

    /**
     * Registers a resource provider.
     *
     * <p>
     * Provider identifiers must be unique.
     * </p>
     *
     * @param provider resource provider
     *
     * @throws IllegalArgumentException
     *         if the provider is null
     *
     * @throws IllegalStateException
     *         if a provider with the same identifier
     *         is already registered
     */
    void register(
            McpResourceProvider provider);

    /**
     * Removes the resource provider with the specified identifier.
     *
     * @param providerId provider identifier
     *
     * @return {@code true} if a provider was removed
     */
    boolean unregister(
            String providerId);

    /**
     * Returns the provider registered with the specified identifier.
     *
     * @param providerId provider identifier
     *
     * @return provider if registered
     */
    Optional<McpResourceProvider> findById(
            String providerId);

    /**
     * Finds a provider capable of resolving the specified URI.
     *
     * <p>
     * Providers are evaluated in registration order.
     * The first matching provider is returned.
     * </p>
     *
     * @param uri resource URI
     *
     * @return matching provider if available
     */
    Optional<McpResourceProvider> findProvider(
            String uri);

    /**
     * Returns all registered resource providers.
     *
     * <p>
     * The returned list should preserve registration order
     * and should not be directly modifiable.
     * </p>
     *
     * @return registered providers
     */
    List<McpResourceProvider> getProviders();

    /**
     * Returns whether a provider with the specified identifier
     * is registered.
     *
     * @param providerId provider identifier
     *
     * @return {@code true} if registered
     */
    default boolean contains(
            String providerId) {

        return findById(
                providerId)
                .isPresent();
    }

    /**
     * Returns whether at least one provider can resolve
     * the specified URI.
     *
     * @param uri resource URI
     *
     * @return {@code true} if a matching provider exists
     */
    default boolean supports(
            String uri) {

        return findProvider(
                uri)
                .isPresent();
    }

    /**
     * Returns the number of registered providers.
     *
     * @return provider count
     */
    default int size() {

        return getProviders()
                .size();
    }

    /**
     * Returns whether no providers are registered.
     *
     * @return {@code true} if empty
     */
    default boolean isEmpty() {

        return getProviders()
                .isEmpty();
    }
}