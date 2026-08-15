/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@link McpResourceRegistry}.
 *
 * <p>
 * Resource providers are stored in registration order.
 * This order is also used when resolving a provider
 * for a resource URI.
 * </p>
 */
public final class DefaultMcpResourceRegistry
        implements McpResourceRegistry {

    private final Map<String, McpResourceProvider> providers =
            new LinkedHashMap<>();

    public DefaultMcpResourceRegistry() {
    }

    public DefaultMcpResourceRegistry(
            List<McpResourceProvider> providers) {

        if (providers == null
                || providers.isEmpty()) {

            return;
        }

        for (McpResourceProvider provider : providers) {

            register(
                    provider);
        }
    }

    @Override
    public synchronized void register(
            McpResourceProvider provider) {

        if (provider == null) {

            throw new IllegalArgumentException(
                    "provider must not be null.");
        }

        String providerId =
                normalizeProviderId(
                        provider.getId());

        if (providers.containsKey(
                providerId)) {

            throw new IllegalStateException(
                    "MCP resource provider is already registered: "
                            + providerId);
        }

        providers.put(
                providerId,
                provider);
    }

    @Override
    public synchronized boolean unregister(
            String providerId) {

        String normalizedId =
                normalizeProviderId(
                        providerId);

        return providers.remove(
                normalizedId) != null;
    }

    @Override
    public synchronized Optional<McpResourceProvider> findById(
            String providerId) {

        String normalizedId =
                normalizeProviderId(
                        providerId);

        return Optional.ofNullable(
                providers.get(
                        normalizedId));
    }

    @Override
    public synchronized Optional<McpResourceProvider> findProvider(
            String uri) {

        String normalizedUri =
                normalizeUri(
                        uri);

        for (McpResourceProvider provider
                : providers.values()) {

            if (supports(
                    provider,
                    normalizedUri)) {

                return Optional.of(
                        provider);
            }
        }

        return Optional.empty();
    }

    @Override
    public synchronized List<McpResourceProvider> getProviders() {

        if (providers.isEmpty()) {

            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                new ArrayList<>(
                        providers.values()));
    }

    private boolean supports(
            McpResourceProvider provider,
            String uri) {

        try {

            return provider.supports(
                    uri);

        } catch (RuntimeException exception) {

            /*
             * A single malformed or faulty provider should not
             * prevent other providers from being evaluated.
             */

            return false;
        }
    }

    private static String normalizeProviderId(
            String providerId) {

        if (providerId == null) {

            throw new IllegalArgumentException(
                    "providerId must not be null.");
        }

        String normalized =
                providerId.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "providerId must not be blank.");
        }

        return normalized;
    }

    private static String normalizeUri(
            String uri) {

        if (uri == null) {

            throw new IllegalArgumentException(
                    "uri must not be null.");
        }

        String normalized =
                uri.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "uri must not be blank.");
        }

        return normalized;
    }
}