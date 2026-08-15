/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.prompts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Default implementation of {@link McpPromptRegistry}.
 */
public final class DefaultMcpPromptRegistry
        implements McpPromptRegistry {

    private final Map<String, McpPromptProvider> providers =
            new LinkedHashMap<>();

    public DefaultMcpPromptRegistry() {
    }

    public DefaultMcpPromptRegistry(
            Collection<McpPromptProvider> providers
    ) {

        if (providers != null) {

            for (McpPromptProvider provider : providers) {

                register(
                        provider
                );
            }
        }
    }

    @Override
    public synchronized void register(
            McpPromptProvider provider
    ) {

        Objects.requireNonNull(
                provider,
                "provider must not be null."
        );

        String name =
                requireText(
                        provider.getName(),
                        "prompt name"
                );

        if (providers.containsKey(name)) {

            throw new IllegalArgumentException(
                    "Prompt provider already registered: "
                            + name
            );
        }

        providers.put(
                name,
                provider
        );
    }

    @Override
    public synchronized boolean unregister(
            String name
    ) {

        String normalized =
                normalize(
                        name
                );

        if (normalized == null) {

            return false;
        }

        return providers.remove(
                normalized
        ) != null;
    }

    @Override
    public synchronized Optional<McpPromptProvider> find(
            String name
    ) {

        String normalized =
                normalize(
                        name
                );

        if (normalized == null) {

            return Optional.empty();
        }

        return Optional.ofNullable(
                providers.get(
                        normalized
                )
        );
    }

    @Override
    public synchronized Collection<McpPromptProvider> getAll() {

        if (providers.isEmpty()) {

            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                new ArrayList<>(
                        providers.values()
                )
        );
    }

    @Override
    public synchronized int size() {

        return providers.size();
    }

    @Override
    public synchronized boolean isEmpty() {

        return providers.isEmpty();
    }

    private static String requireText(
            String value,
            String fieldName
    ) {

        Objects.requireNonNull(
                value,
                fieldName + " must not be null."
        );

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return normalized;
    }

    private static String normalize(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}