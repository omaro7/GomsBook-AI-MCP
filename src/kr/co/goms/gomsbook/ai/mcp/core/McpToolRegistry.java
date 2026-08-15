/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.tool.McpTool;
import kr.co.goms.gomsbook.ai.mcp.tool.McpToolDefinition;

/**
 * Registry for MCP(Model Context Protocol) tools.
 *
 * <p>
 * This registry stores tools exposed through the MCP
 * {@code tools/list} and {@code tools/call} methods.
 * </p>
 *
 * <p>
 * Tool names must be unique.
 * </p>
 */
public final class McpToolRegistry {

    private final Map<String, McpTool> tools =
            new LinkedHashMap<>();


    /**
     * Registers an MCP tool.
     *
     * @param tool MCP tool
     * @throws IllegalArgumentException
     *         when another tool with the same name
     *         is already registered
     */
    public synchronized void register(
            McpTool tool) {

        Objects.requireNonNull(
                tool,
                "MCP tool must not be null."
        );

        tool.validate();

        String name =
                normalizeName(
                        tool.getName()
                );

        if (tools.containsKey(name)) {

            throw new IllegalArgumentException(
                    "MCP tool is already registered: "
                            + name
            );
        }

        tools.put(
                name,
                tool
        );
    }


    /**
     * Registers multiple MCP tools.
     *
     * @param tools MCP tools
     */
    public synchronized void registerAll(
            Collection<? extends McpTool> tools) {

        Objects.requireNonNull(
                tools,
                "MCP tools must not be null."
        );

        for (McpTool tool : tools) {

            register(
                    tool
            );
        }
    }


    /**
     * Returns a registered MCP tool.
     *
     * @param name tool name
     * @return registered tool
     * @throws IllegalArgumentException
     *         when the tool does not exist
     */
    public synchronized McpTool get(
            String name) {

        String normalizedName =
                normalizeName(
                        name
                );

        McpTool tool =
                tools.get(
                        normalizedName
                );

        if (tool == null) {

            throw new IllegalArgumentException(
                    "MCP tool is not registered: "
                            + normalizedName
            );
        }

        return tool;
    }


    /**
     * Returns a registered MCP tool if present.
     *
     * @param name tool name
     * @return tool or {@code null}
     */
    public synchronized McpTool find(
            String name) {

        if (name == null
                || name.isBlank()) {

            return null;
        }

        return tools.get(
                name.trim()
        );
    }


    /**
     * Checks whether a tool is registered.
     *
     * @param name tool name
     * @return {@code true} when the tool exists
     */
    public synchronized boolean contains(
            String name) {

        if (name == null
                || name.isBlank()) {

            return false;
        }

        return tools.containsKey(
                name.trim()
        );
    }


    /**
     * Removes a registered MCP tool.
     *
     * @param name tool name
     * @return removed tool or {@code null}
     */
    public synchronized McpTool unregister(
            String name) {

        if (name == null
                || name.isBlank()) {

            return null;
        }

        return tools.remove(
                name.trim()
        );
    }


    /**
     * Returns all registered MCP tools.
     *
     * <p>
     * Registration order is preserved.
     * </p>
     *
     * @return immutable tool list
     */
    public synchronized List<McpTool> getTools() {

        return Collections.unmodifiableList(
                new ArrayList<>(
                        tools.values()
                )
        );
    }


    /**
     * Returns all MCP tool definitions.
     *
     * <p>
     * This method is primarily used to build
     * {@code tools/list} responses.
     * </p>
     *
     * @return immutable tool definition list
     */
    public synchronized List<McpToolDefinition>
            getDefinitions() {

        List<McpToolDefinition> definitions =
                new ArrayList<>();

        for (McpTool tool
                : tools.values()) {

            definitions.add(
                    tool.getDefinition()
            );
        }

        return Collections.unmodifiableList(
                definitions
        );
    }


    /**
     * Returns the number of registered MCP tools.
     *
     * @return tool count
     */
    public synchronized int size() {

        return tools.size();
    }


    /**
     * Checks whether the registry is empty.
     *
     * @return {@code true} when no tools are registered
     */
    public synchronized boolean isEmpty() {

        return tools.isEmpty();
    }


    /**
     * Removes all registered MCP tools.
     */
    public synchronized void clear() {

        tools.clear();
    }


    private static String normalizeName(
            String name) {

        if (name == null
                || name.isBlank()) {

            throw new IllegalArgumentException(
                    "MCP tool name must not be blank."
            );
        }

        return name.trim();
    }


    @Override
    public synchronized String toString() {

        return "McpToolRegistry{"
                + "tools="
                + tools.keySet()
                + '}';
    }
}