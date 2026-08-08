# GomsBook AI MCP

> Model Context Protocol Integration Layer for GomsBook AI Agent

---

## Overview

GomsBook AI MCP is an independent MCP(Model Context Protocol) integration module for the GomsBook AI Agent ecosystem.

The module provides a standardized runtime layer for connecting GomsBook AI Agent to external MCP servers, discovering available tools, executing remote or local MCP tools, and managing multiple MCP server lifecycles.

GomsBook AI MCP is designed as a separate infrastructure layer so that the existing Agent, Tool, RAG, Accessibility, EPUB, and Chat architectures can remain independent from transport- or provider-specific MCP implementations.

---

## Vision

Extend GomsBook AI Agent from an internal AI tool framework into an extensible AI publishing platform capable of securely connecting to external tools and services through MCP.

Instead of implementing every external integration directly inside GomsBookEditor, GomsBook AI MCP provides a common protocol layer that allows the AI Agent to discover and invoke capabilities exposed by MCP servers.

---

## Project Goals

- MCP protocol abstraction
- JSON-RPC request / response handling
- MCP initialization lifecycle
- MCP server capability negotiation
- MCP tool discovery
- MCP tool execution
- Multi-server MCP registry
- STDIO transport
- Streamable HTTP transport
- MCP runtime lifecycle management
- External tool integration
- GomsBook AI Agent integration
- Filesystem MCP integration
- Git MCP integration
- GitHub MCP integration
- Canva MCP integration

---

## Architecture

```text
                         User Request
                              │
                              ▼
                     GomsBook AI Agent
                              │
                              ▼
                         Tool Router
                              │
                  ┌───────────┴───────────┐
                  ▼                       ▼
             Native Tool              MCP Tool
                  │                       │
                  │                       ▼
                  │                  McpExecutor
                  │                       │
                  │                       ▼
                  │                  McpRegistry
                  │                       │
                  │                       ▼
                  │                   McpClient
                  │                       │
                  │                       ▼
                  │                  McpTransport
                  │                 /            \
                  │                ▼              ▼
                  │       STDIO Transport   Streamable HTTP
                  │                │              │
                  └────────────────┼──────────────┘
                                   ▼
                              MCP Servers
```

---

## Core Components

### MCP Protocol

Provides the common protocol models required for MCP communication.

- `McpProtocolVersion`
- `McpMessage`
- `McpRequest`
- `McpResponse`
- `McpError`

---

### MCP Initialization

Handles the MCP client/server initialization lifecycle.

- `McpInitializeRequest`
- `McpInitializeResult`
- `McpClientInfo`
- `McpServerInfo`
- `McpClientCapabilities`
- `McpServerCapabilities`

Initialization flow:

```text
connect
   │
   ▼
initialize
   │
   ▼
protocol negotiation
   │
   ▼
capability negotiation
   │
   ▼
notifications/initialized
   │
   ▼
READY
```

---

### MCP Tool Model

Represents tools exposed by MCP servers and their execution results.

- `McpToolDefinition`
- `McpToolCall`
- `McpToolResult`

Tool execution flow:

```text
tools/list
    │
    ▼
Tool Discovery
    │
    ▼
McpRegistry
    │
    ▼
tools/call
    │
    ▼
McpToolResult
```

MCP protocol errors and tool execution errors are handled separately.

```text
McpResponse.isError()
        │
        └── JSON-RPC / protocol error

McpToolResult.isError()
        │
        └── Tool execution error
```

---

## Transport

GomsBook AI MCP supports transport-independent client architecture.

### STDIO Transport

`StdioMcpTransport`

Designed for local MCP server processes.

Typical use cases:

- Filesystem MCP
- Git MCP
- Process-based local MCP servers
- Local development tools

```text
GomsBook AI
     │
     ▼
StdioMcpTransport
     │
     ├── stdin
     ├── stdout
     └── stderr
     │
     ▼
Local MCP Server
```

---

### Streamable HTTP Transport

`StreamableHttpMcpTransport`

Designed for remote MCP servers.

Core support includes:

- HTTP POST
- JSON responses
- SSE responses
- MCP Session ID
- MCP Protocol Version header
- Notifications
- Session termination

Typical use cases:

- Canva MCP
- GitHub MCP
- Cloud MCP services
- Remote enterprise MCP servers

---

## MCP Client

### McpClient

Defines the client contract for communicating with a single MCP server.

Main operations:

```java
connect();
initialize();
listTools();
callTool();
close();
```

### DefaultMcpClient

Default MCP client implementation.

Responsibilities:

- Transport lifecycle
- MCP initialization
- Protocol version validation
- Server capability discovery
- Tool pagination
- Tool execution
- Server information management

---

## MCP Registry

### McpRegistry

Maintains registered MCP clients and discovered tools.

```text
McpRegistry
    │
    ├── filesystem
    │      ├── read_file
    │      ├── write_file
    │      └── list_directory
    │
    ├── git
    │      ├── status
    │      ├── diff
    │      └── commit
    │
    └── canva
           ├── create_design
           └── export_design
```

Tool names are not assumed to be globally unique.

The effective tool identity is:

```text
(serverName, toolName)
```

### McpToolReference

Represents the relationship between an MCP server and a discovered tool.

---

## MCP Executor

### McpExecutor

Executes MCP tools through registered MCP clients.

Explicit server execution:

```java
executor.execute(
        "filesystem",
        "read_file",
        arguments);
```

Unique tool resolution:

```java
executor.executeUnique(
        "read_file",
        arguments);
```

If multiple MCP servers expose the same tool name, automatic execution fails instead of selecting a server arbitrarily.

### DefaultMcpExecutor

Execution validation flow:

```text
Tool Registered?
      │
      ▼
Client Available?
      │
      ▼
Connected?
      │
      ▼
Initialized?
      │
      ▼
McpClient.callTool()
```

---

## MCP Runtime

### McpRuntime

High-level lifecycle facade for all MCP infrastructure.

Responsibilities:

- Register MCP clients
- Start MCP servers
- Stop MCP servers
- Initialize clients
- Refresh tools
- Access registry
- Access executor

### DefaultMcpRuntime

Server startup flow:

```text
register
   │
   ▼
connect
   │
   ▼
initialize
   │
   ▼
tools/list
   │
   ▼
registry refresh
```

Failures are isolated per MCP server.

Example:

```text
filesystem  → READY
git         → READY
canva       → FAILED
```

A Canva connection failure does not disable Filesystem or Git MCP servers.

---

## Component Factory

### McpComponentFactory

Central factory for constructing MCP infrastructure.

Creates:

- `McpTransportFactory`
- `McpClient`
- `McpRegistry`
- `McpExecutor`
- `McpRuntime`
- `McpClientInfo`
- `McpClientCapabilities`

```text
McpComponentFactory
        │
        ├── DefaultMcpTransportFactory
        ├── DefaultMcpClient
        ├── DefaultMcpRegistry
        ├── DefaultMcpExecutor
        └── DefaultMcpRuntime
```

Object construction and runtime lifecycle are intentionally separated.

```text
Factory  = component creation
Runtime  = lifecycle management
```

---

## Planned MCP Integrations

### Filesystem MCP

Primary integration target.

Planned capabilities:

- Read EPUB/XHTML/CSS files
- Write and update publishing resources
- Browse project directories
- Inspect EPUB workspace resources

Example workflow:

```text
User
 "chapter01.xhtml을 수정해줘"
        │
        ▼
GomsBook AI Agent
        │
        ▼
Filesystem MCP
        │
        ▼
chapter01.xhtml
```

---

### Git MCP

Local Git repository integration.

Planned capabilities:

- status
- diff
- log
- branch
- add
- commit
- merge
- tag

Git operations remain separate from GitHub service operations.

---

### GitHub MCP

Remote GitHub workflow integration.

Planned capabilities:

- Repository information
- Issues
- Pull Requests
- Actions
- Releases
- Code review workflows

---

### Canva MCP

Design workflow integration for GomsBook publishing projects.

Planned use cases:

- Book cover design
- Section divider design
- Interior graphic assets
- Existing design lookup
- Design export
- Publishing asset workflow

Conceptual flow:

```text
User Request
     │
     ▼
GomsBook AI Agent
     │
     ▼
Canva MCP
     │
     ▼
Canva Design
     │
     ▼
Export
     │
     ▼
GomsBook Project
```

A dedicated Design Agent is not required for the initial MCP integration. It can be introduced later if design workflows become complex enough to justify a specialized Agent.

---

## Relationship with GomsBook Native Tools

GomsBook AI Agent already contains an internal Tool framework.

Native tools and MCP tools remain separate at the infrastructure level.

```text
GomsBook AI Agent
        │
        ▼
    Tool Router
     /       \
    ▼         ▼
Native Tool  MCP Tool
    │         │
    ▼         ▼
ToolExecutor McpExecutor
```

Examples of Native Tools:

- XHTML generation
- XHTML validation
- EPUB generation
- EPUB validation
- Accessibility validation
- Image analysis
- Alt text application
- Metadata generation

Examples of MCP Tools:

- Filesystem operations
- Git operations
- GitHub services
- Canva services

A future integration layer will allow the Agent Router to expose both tool types as execution candidates.

---

## Technology Stack

| Category | Technology |
|---|---|
| Language | Java |
| Desktop Integration | Eclipse RCP / e4 |
| Protocol | Model Context Protocol |
| RPC | JSON-RPC 2.0 |
| JSON | Gson |
| Local Transport | STDIO |
| Remote Transport | Streamable HTTP |
| Streaming | Server-Sent Events |
| AI Runtime | GomsBook AI Agent |
| Document Domain | EPUB3 / XHTML5 |
| Version Control Integration | Git / GitHub |
| Design Integration | Canva |

---

## Repository Structure

```text
src/
└── kr/co/goms/gomsbook/ai/mcp/
    │
    ├── protocol/
    │   ├── McpProtocolVersion.java
    │   ├── McpMessage.java
    │   ├── McpRequest.java
    │   ├── McpResponse.java
    │   ├── McpError.java
    │   └── initialize/
    │       ├── McpInitializeRequest.java
    │       └── McpInitializeResult.java
    │
    ├── tool/
    │   ├── McpToolDefinition.java
    │   ├── McpToolCall.java
    │   └── McpToolResult.java
    │
    ├── client/
    │   ├── McpClient.java
    │   ├── DefaultMcpClient.java
    │   ├── McpClientInfo.java
    │   ├── McpServerInfo.java
    │   ├── McpClientCapabilities.java
    │   └── McpServerCapabilities.java
    │
    ├── config/
    │   └── McpServerConfiguration.java
    │
    ├── transport/
    │   ├── McpTransport.java
    │   ├── McpTransportFactory.java
    │   ├── DefaultMcpTransportFactory.java
    │   ├── StdioMcpTransport.java
    │   └── StreamableHttpMcpTransport.java
    │
    ├── registry/
    │   ├── McpRegistry.java
    │   ├── DefaultMcpRegistry.java
    │   └── McpToolReference.java
    │
    ├── executor/
    │   ├── McpExecutor.java
    │   └── DefaultMcpExecutor.java
    │
    ├── runtime/
    │   ├── McpRuntime.java
    │   └── DefaultMcpRuntime.java
    │
    └── McpComponentFactory.java
```

---

## Roadmap

### Phase 1 — MCP Core

- [x] MCP protocol model
- [x] JSON-RPC request / response
- [x] MCP error model
- [x] Initialization model
- [x] Client / server information
- [x] Client / server capabilities
- [x] Tool definition
- [x] Tool call
- [x] Tool result
- [x] Server configuration
- [x] Transport abstraction
- [x] STDIO transport
- [x] Streamable HTTP transport
- [x] Transport factory
- [x] MCP client
- [x] MCP registry
- [x] MCP executor
- [x] MCP runtime
- [x] Component factory

### Phase 2 — Core Validation & Integration

- [ ] Full compilation consistency check
- [ ] API consistency review
- [ ] MCP-specific exception hierarchy
- [ ] GomsBookAiRuntime integration
- [ ] Native Tool / MCP Tool routing
- [ ] Runtime status model

### Phase 3 — MCP Server Integration

- [ ] Filesystem MCP
- [ ] Git MCP
- [ ] GitHub MCP
- [ ] Canva MCP

### Phase 4 — Advanced MCP Runtime

- [ ] MCP Message Handler
- [ ] Notification Dispatcher
- [ ] Server-to-client request handling
- [ ] Streamable HTTP GET SSE listener
- [ ] Tool list changed handling
- [ ] Resource / Prompt integration
- [ ] Protocol version strategy
- [ ] Authentication / OAuth integration

---

## Current Status

🚧 Under Development

### MCP Core 1st Stage

```text
Protocol              COMPLETE
Initialization        COMPLETE
Tool Model            COMPLETE
Configuration         COMPLETE
STDIO Transport       COMPLETE
Streamable HTTP       COMPLETE
Client                COMPLETE
Registry              COMPLETE
Executor              COMPLETE
Runtime               COMPLETE
Component Factory     COMPLETE
```

**MCP independent module 1st Core implementation is complete.**

The next development step is a full compile/API consistency review followed by integration into `GomsBookAiRuntime`.

---

## Related Project

### GomsBook AI Agent

AI-powered Agent Framework for EPUB3 authoring, accessibility, and publishing automation.

GomsBook AI MCP provides the external MCP integration infrastructure used by GomsBook AI Agent.

Repository:

`https://github.com/omaro7/GomsBook-AI-Agent`

### GomsBookEditor

Desktop EPUB authoring environment based on Eclipse RCP / e4.

GomsBook AI Agent and GomsBook AI MCP are designed to extend GomsBookEditor with AI-assisted authoring, validation, accessibility, automation, and external service integration.

---

## Documentation

Additional MCP design documentation:

```text
docs/
└── GomsBook_AI_MCP_Core_1st.md
```

The document describes the first MCP Core architecture, implemented components, responsibilities, lifecycle, and planned extensions.

---

## Author

Han Junghoon

Software Developer  
Independent Publisher  
AI Agent & EPUB3 Research

---

## About

AI 기술 포트폴리오

GomsBook AI MCP is part of the GomsBook AI Agent architecture and focuses on MCP-based external tool and service integration.
