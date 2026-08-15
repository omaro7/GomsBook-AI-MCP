/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.server;

import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.co.goms.gomsbook.ai.mcp.McpContentTypeAdapterFactory;
import kr.co.goms.gomsbook.ai.mcp.codec.GsonMcpJsonCodec;
import kr.co.goms.gomsbook.ai.mcp.codec.McpJsonCodec;

import kr.co.goms.gomsbook.ai.mcp.completion.McpCompleteRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.completion.McpCompletionReferenceTypeAdapterFactory;
import kr.co.goms.gomsbook.ai.mcp.completion.McpCompletionService;

import kr.co.goms.gomsbook.ai.mcp.discovery.DefaultMcpDiscoveryService;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpDiscoverRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.discovery.McpDiscoveryService;

import kr.co.goms.gomsbook.ai.mcp.dispatch.McpRequestDispatcher;
import kr.co.goms.gomsbook.ai.mcp.prompts.McpGetPromptRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.prompts.McpListPromptsRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.prompts.McpPromptService;
import kr.co.goms.gomsbook.ai.mcp.resources.McpListResourceTemplatesRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.resources.McpListResourcesRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.resources.McpReadResourceRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.resources.McpResourceService;
import kr.co.goms.gomsbook.ai.mcp.server.runtime.DefaultMcpServerRuntime;
import kr.co.goms.gomsbook.ai.mcp.server.runtime.McpServerRuntime;
import kr.co.goms.gomsbook.ai.mcp.tool.McpCallToolRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.tool.McpListToolsRequestHandler;
import kr.co.goms.gomsbook.ai.mcp.tool.McpToolService;
import kr.co.goms.gomsbook.ai.mcp.transport.McpTransport;
import kr.co.goms.gomsbook.ai.mcp.transport.stdio.StdioMcpTransport;

/**
 * Composition root for the MCP server.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This factory creates and connects protocol-independent
 * infrastructure components required by the MCP server:
 * </p>
 *
 * <ul>
 *     <li>JSON codec</li>
 *     <li>discovery service</li>
 *     <li>request handlers</li>
 *     <li>request dispatcher</li>
 *     <li>server runtime</li>
 *     <li>transport listener</li>
 *     <li>transport</li>
 *     <li>server facade</li>
 * </ul>
 *
 * <p>
 * {@link McpServerConfig} is the authoritative source for
 * advertised server capabilities. This factory validates that
 * configured capabilities and installed services are consistent.
 * </p>
 */
public final class McpServerComponentFactory {

    private McpServerComponentFactory() {
    }


    /*
     * ------------------------------------------------------------
     * STDIO server
     * ------------------------------------------------------------
     */

    public static McpServer createStdioServer(
            McpServerConfig config,
            McpToolService toolService,
            McpResourceService resourceService,
            McpPromptService promptService,
            McpCompletionService completionService
    ) {

        return createServer(
                config,
                toolService,
                resourceService,
                promptService,
                completionService,
                new StdioMcpTransport()
        );
    }


    /*
     * ------------------------------------------------------------
     * Generic server
     * ------------------------------------------------------------
     */

    public static McpServer createServer(
            McpServerConfig config,
            McpToolService toolService,
            McpResourceService resourceService,
            McpPromptService promptService,
            McpCompletionService completionService,
            McpTransport transport
    ) {

        Objects.requireNonNull(
                config,
                "MCP server config must not be null."
        );

        Objects.requireNonNull(
                transport,
                "MCP transport must not be null."
        );


        /*
         * --------------------------------------------------------
         * Validate configured capabilities against installed
         * services before any runtime components are created.
         * --------------------------------------------------------
         */

        validateCapabilities(
                config.getCapabilities(),
                toolService,
                resourceService,
                promptService,
                completionService
        );


        /*
         * --------------------------------------------------------
         * JSON codec
         * --------------------------------------------------------
         */

        McpJsonCodec codec =
                createJsonCodec();


        /*
         * --------------------------------------------------------
         * Discovery
         * --------------------------------------------------------
         */

        McpDiscoveryService discoveryService =
                createDiscoveryService(
                        config
                );


        /*
         * --------------------------------------------------------
         * Dispatcher
         * --------------------------------------------------------
         */

        McpRequestDispatcher dispatcher =
                createDispatcher(
                        config.getCapabilities(),
                        codec,
                        discoveryService,
                        toolService,
                        resourceService,
                        promptService,
                        completionService
                );


        /*
         * --------------------------------------------------------
         * Runtime
         * --------------------------------------------------------
         */

        McpServerRuntime runtime =
                new DefaultMcpServerRuntime(
                        config,
                        dispatcher
                );


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
         * Server facade
         * --------------------------------------------------------
         */

        return new DefaultMcpServer(
                config,
                runtime,
                transport
        );
    }


    /*
     * ------------------------------------------------------------
     * Gson / Codec
     * ------------------------------------------------------------
     */

    public static McpJsonCodec createJsonCodec() {

        return new GsonMcpJsonCodec(
                createGson()
        );
    }


    /**
     * Creates the single Gson configuration used by all MCP
     * server components.
     *
     * @return configured Gson instance
     */
    public static Gson createGson() {

        return new GsonBuilder()
                .disableHtmlEscaping()

                /*
                 * MCP Content polymorphism:
                 *
                 * text
                 * image
                 * audio
                 * resource_link
                 * resource
                 */
                .registerTypeAdapterFactory(
                        new McpContentTypeAdapterFactory()
                )

                /*
                 * Completion reference polymorphism:
                 *
                 * ref/prompt
                 * ref/resource
                 */
                .registerTypeAdapterFactory(
                        new McpCompletionReferenceTypeAdapterFactory()
                )

                .create();
    }


    /*
     * ------------------------------------------------------------
     * Discovery
     * ------------------------------------------------------------
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
                .ttlMs(
                        config.getDiscoveryTtlMs()
                )
                .cacheScope(
                        config.getDiscoveryCacheScope()
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Dispatcher
     * ------------------------------------------------------------
     */

    public static McpRequestDispatcher createDispatcher(
            McpServerCapabilities capabilities,
            McpJsonCodec codec,
            McpDiscoveryService discoveryService,
            McpToolService toolService,
            McpResourceService resourceService,
            McpPromptService promptService,
            McpCompletionService completionService
    ) {

        Objects.requireNonNull(
                capabilities,
                "MCP server capabilities must not be null."
        );

        Objects.requireNonNull(
                codec,
                "MCP JSON codec must not be null."
        );

        Objects.requireNonNull(
                discoveryService,
                "MCP discovery service must not be null."
        );


        McpRequestDispatcher dispatcher = new McpRequestDispatcher();


        /*
         * --------------------------------------------------------
         * server/discover
         *
         * Mandatory for MCP 2026-07-28 servers.
         * --------------------------------------------------------
         */

        dispatcher.register(
                new McpDiscoverRequestHandler(
                        discoveryService
                )
        );


        /*
         * --------------------------------------------------------
         * Tools
         * --------------------------------------------------------
         */

        if (capabilities.supportsTools()) {

        	dispatcher.register(
                    new McpListToolsRequestHandler(
                            codec,
                            requireToolService(
                                    toolService
                            )
                    )
            );

        	dispatcher.register(
                    new McpCallToolRequestHandler(
                            codec,
                            toolService
                    )
            );
        }


        /*
         * --------------------------------------------------------
         * Resources
         * --------------------------------------------------------
         */

        if (capabilities.supportsResources()) {

            McpResourceService service =
                    requireResourceService(
                            resourceService
                    );


            dispatcher.register(
                    new McpListResourcesRequestHandler(
                            codec,
                            service
                    )
            );

            dispatcher.register(
                    new McpReadResourceRequestHandler(
                            codec,
                            service
                    )
            );

            dispatcher.register(
                    new McpListResourceTemplatesRequestHandler(
                            codec,
                            service
                    )
            );
        }


        /*
         * --------------------------------------------------------
         * Prompts
         * --------------------------------------------------------
         */

        if (capabilities.supportsPrompts()) {

            McpPromptService service =
                    requirePromptService(
                            promptService
                    );


            dispatcher.register(
                    new McpListPromptsRequestHandler(
                            codec,
                            service
                    )
            );

            dispatcher.register(
                    new McpGetPromptRequestHandler(
                            codec,
                            service
                    )
            );
        }


        /*
         * --------------------------------------------------------
         * Completion
         * --------------------------------------------------------
         */

        if (capabilities.supportsCompletions()) {

        	dispatcher.register(
                    new McpCompleteRequestHandler(
                            codec,
                            requireCompletionService(
                                    completionService
                            )
                    )
            );
        }


        return dispatcher;
    }


    /*
     * ------------------------------------------------------------
     * Capability / Service consistency
     * ------------------------------------------------------------
     */

    /**
     * Validates that advertised capabilities exactly match
     * installed feature services.
     */
    public static void validateCapabilities(
            McpServerCapabilities capabilities,
            McpToolService toolService,
            McpResourceService resourceService,
            McpPromptService promptService,
            McpCompletionService completionService
    ) {

        Objects.requireNonNull(
                capabilities,
                "MCP server capabilities must not be null."
        );


        validateCapability(
                "tools",
                capabilities.supportsTools(),
                toolService != null
        );


        validateCapability(
                "resources",
                capabilities.supportsResources(),
                resourceService != null
        );


        validateCapability(
                "prompts",
                capabilities.supportsPrompts(),
                promptService != null
        );


        validateCapability(
                "completions",
                capabilities.supportsCompletions(),
                completionService != null
        );
    }


    private static void validateCapability(
            String name,
            boolean advertised,
            boolean installed
    ) {

        if (advertised
                && !installed) {

            throw new IllegalStateException(
                    "MCP capability '"
                            + name
                            + "' is advertised but its service "
                            + "is not installed."
            );
        }


        if (!advertised
                && installed) {

            throw new IllegalStateException(
                    "MCP service for capability '"
                            + name
                            + "' is installed but the capability "
                            + "is not advertised."
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Required services
     * ------------------------------------------------------------
     */

    private static McpToolService requireToolService(
            McpToolService service
    ) {

        return Objects.requireNonNull(
                service,
                "MCP tools capability requires McpToolService."
        );
    }


    private static McpResourceService requireResourceService(
            McpResourceService service
    ) {

        return Objects.requireNonNull(
                service,
                "MCP resources capability requires McpResourceService."
        );
    }


    private static McpPromptService requirePromptService(
            McpPromptService service
    ) {

        return Objects.requireNonNull(
                service,
                "MCP prompts capability requires McpPromptService."
        );
    }


    private static McpCompletionService requireCompletionService(
            McpCompletionService service
    ) {

        return Objects.requireNonNull(
                service,
                "MCP completions capability requires "
                        + "McpCompletionService."
        );
    }
}