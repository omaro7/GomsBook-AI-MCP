/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server.runtime;

import java.util.Collection;
import java.util.Objects;

import com.google.gson.Gson;

import kr.co.goms.gomsbook.ai.mcp.codec.GsonMcpJsonCodec;
import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodec;
import kr.co.goms.gomsbook.ai.mcp.completion.McpCompleteRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.completion.McpCompletionService;
import kr.co.goms.gomsbook.ai.mcp.core.McpToolRegistry;
import kr.co.goms.gomsbook.ai.mcp.discovery.DefaultMcpDiscoveryService;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpDiscoverRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpDiscoveryService;
import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestDispatcher;
import kr.co.goms.gomsbook.ai.mcp.gson.GsonMcpMessageCodec;
import kr.co.goms.gomsbook.ai.mcp.prompts.McpGetPromptRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.prompts.McpListPromptsRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.prompts.McpPromptService;
import kr.co.goms.gomsbook.ai.mcp.protocol.McpMessageCodec;
import kr.co.goms.gomsbook.ai.mcp.resources.McpListResourceTemplatesRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.resources.McpListResourcesRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.resources.McpReadResourceRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.resources.McpResourceService;
import kr.co.goms.gomsbook.ai.mcp.server.DefaultMcpServer;
import kr.co.goms.gomsbook.ai.mcp.server.McpServer;
import kr.co.goms.gomsbook.ai.mcp.server.McpServerCapabilities;
import kr.co.goms.gomsbook.ai.mcp.server.McpServerConfig;
import kr.co.goms.gomsbook.ai.mcp.server.McpServerDiscoveryResult;
import kr.co.goms.gomsbook.ai.mcp.server.McpServerTransportListener;
import kr.co.goms.gomsbook.ai.mcp.tool.AgentToolMcpAdapter;
import kr.co.goms.gomsbook.ai.mcp.tool.DefaultMcpToolService;
import kr.co.goms.gomsbook.ai.mcp.tool.McpCallToolRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.tool.McpListToolsRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.tool.McpToolService;
import kr.co.goms.gomsbook.ai.mcp.transport.McpTransport;
import kr.co.goms.gomsbook.ai.mcp.transport.stdio.StdioMcpTransport;
import kr.co.goms.gomsbook.ai.tool.AgentTool;

/**
 * Component factory for the GomsBook MCP Server.
 *
 * <p>
 * Assembles the server-side MCP runtime stack.
 * </p>
 *
 * <pre>
 * Gson
 *   ↓
 * GsonMcpMessageCodec
 *   ↓
 * AgentTool
 *   ↓
 * AgentToolMcpAdapter
 *   ↓
 * McpToolRegistry
 *   ↓
 * McpServerConfig
 *   ↓
 * McpServerDiscoveryResult
 *   ↓
 * McpRequestDispatcher
 *   ↓
 * StdioMcpTransport
 *   ↓
 * DefaultMcpServer
 *   ↓
 * DefaultMcpServerRuntime
 * </pre>
 */
public final class McpServerComponentFactory {

    private McpServerComponentFactory() {

        throw new AssertionError(
                "McpServerComponentFactory "
                        + "must not be instantiated."
        );
    }


    /**
     * Creates the default STDIO-based MCP Server runtime.
     *
     * @param gson Gson instance
     * @param agentTools native GomsBook Agent Tools
     * @return MCP Server runtime
     */
    public static McpServer createDefault(
            Gson gson,
            Collection<? extends AgentTool> agentTools) {

        return create(
                gson,
                agentTools,
                McpServerConfig.defaultConfig()
        );
    }


    /**
     * Creates an STDIO-based MCP Server runtime.
     *
     * @param gson Gson instance
     * @param agentTools native GomsBook Agent Tools
     * @param serverConfig MCP Server configuration
     * @return MCP Server runtime
     */
    public static McpServer create(
            Gson gson,
            Collection<? extends AgentTool> agentTools,
            McpServerConfig serverConfig
    ) {

        Objects.requireNonNull(
                gson,
                "Gson must not be null."
        );

        Objects.requireNonNull(
                agentTools,
                "AgentTool collection must not be null."
        );

        Objects.requireNonNull(
                serverConfig,
                "MCP server config must not be null."
        );


        /*
         * --------------------------------------------------------
         * JSON codec
         * --------------------------------------------------------
         */

        McpJsonCodec codec =
                createJsonCodec(
                        gson
                );


        /*
         * --------------------------------------------------------
         * Tool registry / service
         * --------------------------------------------------------
         */

        McpToolRegistry toolRegistry =
                createToolRegistry(
                        agentTools
                );


        McpToolService toolService =
                new DefaultMcpToolService(
                        toolRegistry
                );


        /*
         * --------------------------------------------------------
         * Discovery
         * --------------------------------------------------------
         */

        McpDiscoveryService discoveryService =
                createDiscoveryService(
                        serverConfig
                );


        /*
         * --------------------------------------------------------
         * Dispatcher
         * --------------------------------------------------------
         */

        McpRequestDispatcher dispatcher =
                createDispatcher(
                        discoveryService,
                        toolService,

                        /*
                         * Resources
                         */
                        null,

                        /*
                         * Prompts
                         */
                        null,

                        /*
                         * Completion
                         */
                        null,

                        codec,
                        serverConfig.getCapabilities()
                );


        /*
         * --------------------------------------------------------
         * Runtime
         * --------------------------------------------------------
         */

        McpServerRuntime runtime =
                createRuntime(
                        serverConfig,
                        dispatcher
                );


        /*
         * --------------------------------------------------------
         * Transport
         * --------------------------------------------------------
         */

        McpTransport transport =
                createStdioTransport();


        /*
         * --------------------------------------------------------
         * Transport bridge
         * --------------------------------------------------------
         */

        McpServerTransportListener listener =
                new McpServerTransportListener(
                        transport,
                        codec,
                        runtime
                );

        transport.setListener(
                listener
        );


        /*
         * --------------------------------------------------------
         * Server
         * --------------------------------------------------------
         */

        return createServer(
                serverConfig,
                transport,
                runtime
        );
    }


    public static McpJsonCodec createJsonCodec(
            Gson gson
    ) {

        Objects.requireNonNull(
                gson,
                "Gson must not be null."
        );

        return new GsonMcpJsonCodec(
                gson
        );
    }
    
    /**
     * Creates the MCP discovery service.
     */
    public static McpDiscoveryService createDiscoveryService(
            McpServerConfig config
    ) {

        Objects.requireNonNull(
                config,
                "MCP server config must not be null."
        );

        return DefaultMcpDiscoveryService.builder()
                .serverInfo(
                        config.getServerInfo()
                )
                .capabilities(
                        config.getCapabilities()
                )
                .supportedVersions(
                        config.getSupportedVersions()
                )
                .instructions(
                        config.getInstructions()
                )
                .build();
    }
    
    /**
     * Creates the MCP JSON message codec.
     */
    public static McpMessageCodec createMessageCodec(
            Gson gson) {

        Objects.requireNonNull(
                gson,
                "Gson must not be null."
        );

        return new GsonMcpMessageCodec(
                gson
        );
    }


    /**
     * Creates the MCP Tool registry and exposes
     * native AgentTools through MCP adapters.
     */
    public static McpToolRegistry createToolRegistry(
            Collection<? extends AgentTool> agentTools) {

        Objects.requireNonNull(
                agentTools,
                "AgentTool collection must not be null."
        );

        McpToolRegistry registry =
                new McpToolRegistry();

        for (AgentTool agentTool
                : agentTools) {

            if (agentTool == null) {

                throw new IllegalArgumentException(
                        "AgentTool collection must not "
                                + "contain null values."
                );
            }

            AgentToolMcpAdapter adapter =
                    new AgentToolMcpAdapter(
                            agentTool
                    );

            registry.register(
                    adapter
            );
        }

        return registry;
    }


    /**
     * Creates the server discovery result from
     * the server configuration.
     */
    public static McpServerDiscoveryResult
            createDiscoveryResult(
                    McpServerConfig serverConfig) {

        Objects.requireNonNull(
                serverConfig,
                "MCP server config must not be null."
        );

        serverConfig.validate();

        return serverConfig
                .createDiscoveryResult();
    }


    /**
     * Creates the MCP request dispatcher.
     */
    public static McpRequestDispatcher createDispatcher(
            McpDiscoveryService discoveryService,
            McpToolService toolService,
            McpResourceService resourceService,
            McpPromptService promptService,
            McpCompletionService completionService,
            McpJsonCodec codec,
            McpServerCapabilities capabilities
    ) {

        Objects.requireNonNull(
                discoveryService,
                "MCP discovery service must not be null."
        );

        Objects.requireNonNull(
                codec,
                "MCP JSON codec must not be null."
        );

        Objects.requireNonNull(
                capabilities,
                "MCP server capabilities must not be null."
        );

        McpRequestDispatcher dispatcher = new McpRequestDispatcher();

        dispatcher.register(
                new McpDiscoverRequestHandler(
                        discoveryService
                )
        );

        if (capabilities.supportsTools()) {

            Objects.requireNonNull(
                    toolService,
                    "MCP tools capability requires McpToolService."
            );

            dispatcher.register(
                    new McpListToolsRequestHandler(
                            codec,
                            toolService
                    )
            );

            dispatcher.register(
                    new McpCallToolRequestHandler(
                            codec,
                            toolService
                    )
            );
        }

        if (capabilities.supportsResources()) {

            Objects.requireNonNull(
                    resourceService,
                    "MCP resources capability requires "
                            + "McpResourceService."
            );

            dispatcher.register(
                    new McpListResourcesRequestHandler(
                            codec,
                            resourceService
                    )
            );

            dispatcher.register(
                    new McpReadResourceRequestHandler(
                            codec,
                            resourceService
                    )
            );

            dispatcher.register(
                    new McpListResourceTemplatesRequestHandler(
                            codec,
                            resourceService
                    )
            );
        }

        if (capabilities.supportsPrompts()) {

            Objects.requireNonNull(
                    promptService,
                    "MCP prompts capability requires "
                            + "McpPromptService."
            );

            dispatcher.register(
                    new McpListPromptsRequestHandler(
                            codec,
                            promptService
                    )
            );

            dispatcher.register(
                    new McpGetPromptRequestHandler(
                            codec,
                            promptService
                    )
            );
        }

        if (capabilities.supportsCompletions()) {

            Objects.requireNonNull(
                    completionService,
                    "MCP completion capability requires "
                            + "McpCompletionService."
            );

            dispatcher.register(
                    new McpCompleteRequestHandler(
                            codec,
                            completionService
                    )
            );
        }

        return dispatcher;
    }


    /**
     * Creates the default STDIO transport.
     */
    public static McpTransport createStdioTransport() {

        return new StdioMcpTransport();
    }

    /**
     * Creates the default MCP Server.
     */
    public static McpServer createServer(
            McpServerConfig config,
            McpTransport transport,
            McpServerRuntime runtime
    ) {

        Objects.requireNonNull(
                config,
                "MCP server config must not be null."
        );

        Objects.requireNonNull(
                transport,
                "MCP transport must not be null."
        );

        Objects.requireNonNull(
                runtime,
                "MCP server runtime must not be null."
        );

        return new DefaultMcpServer(
                config,
                runtime,
                transport
        );
    }


    /**
     * Creates the MCP Server runtime.
     */
    public static McpServerRuntime createRuntime(
            McpServerConfig config,
            McpRequestDispatcher dispatcher
    ) {

        Objects.requireNonNull(
                config,
                "MCP server config must not be null."
        );

        Objects.requireNonNull(
                dispatcher,
                "MCP request dispatcher must not be null."
        );

        return new DefaultMcpServerRuntime(
                config,
                dispatcher
        );
    }
}