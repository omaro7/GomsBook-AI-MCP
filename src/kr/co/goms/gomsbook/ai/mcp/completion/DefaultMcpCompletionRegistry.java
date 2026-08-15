/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of {@link McpCompletionRegistry}.
 *
 * <p>
 * Providers are evaluated in registration order.
 * The first provider whose {@link McpCompletionProvider#supports(McpCompleteParams)}
 * method returns {@code true} is selected.
 * </p>
 *
 * <p>
 * This implementation is thread-safe.
 * </p>
 */
public final class DefaultMcpCompletionRegistry
        implements McpCompletionRegistry {

    private final CopyOnWriteArrayList<McpCompletionProvider> providers;

    /**
     * Creates an empty completion registry.
     */
    public DefaultMcpCompletionRegistry() {

        this.providers =
                new CopyOnWriteArrayList<>();
    }

    /**
     * Creates a completion registry with initial providers.
     *
     * @param providers initial completion providers
     */
    public DefaultMcpCompletionRegistry(
            List<McpCompletionProvider> providers) {

        Objects.requireNonNull(
                providers,
                "providers must not be null."
        );

        this.providers =
                new CopyOnWriteArrayList<>();

        for (McpCompletionProvider provider : providers) {

            register(provider);
        }
    }

    /**
     * Registers a completion provider.
     *
     * <p>
     * The same provider instance is not registered twice.
     * </p>
     *
     * @param provider completion provider
     */
    @Override
    public void register(
            McpCompletionProvider provider) {

        Objects.requireNonNull(
                provider,
                "provider must not be null."
        );

        providers.addIfAbsent(
                provider
        );
    }

    /**
     * Unregisters a completion provider.
     *
     * @param provider completion provider
     * @return {@code true} if the provider was removed
     */
    @Override
    public boolean unregister(
            McpCompletionProvider provider) {

        if (provider == null) {
            return false;
        }

        return providers.remove(
                provider
        );
    }

    /**
     * Finds the first provider capable of handling
     * the given completion request.
     *
     * @param params completion request parameters
     * @return matching provider, if present
     */
    @Override
    public Optional<McpCompletionProvider> findProvider(
            McpCompleteParams params) {

        Objects.requireNonNull(
                params,
                "params must not be null."
        );

        for (McpCompletionProvider provider : providers) {

            if (provider.supports(params)) {

                return Optional.of(
                        provider
                );
            }
        }

        return Optional.empty();
    }

    /**
     * Returns all registered providers.
     *
     * <p>
     * The returned list is an immutable snapshot.
     * </p>
     *
     * @return immutable provider list
     */
    @Override
    public List<McpCompletionProvider> getProviders() {

        return Collections.unmodifiableList(
                List.copyOf(
                        providers
                )
        );
    }

    /**
     * Returns whether this registry contains no providers.
     *
     * @return {@code true} if empty
     */
    @Override
    public boolean isEmpty() {

        return providers.isEmpty();
    }

    /**
     * Returns the number of registered providers.
     *
     * @return provider count
     */
    @Override
    public int size() {

        return providers.size();
    }

    @Override
    public String toString() {

        return "DefaultMcpCompletionRegistry{" +
                "providers=" + providers.size() +
                '}';
    }
}