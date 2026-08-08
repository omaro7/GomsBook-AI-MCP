# GomsBook AI Agent - MCP 독립 모듈 1차 Core

## 1. 개요

GomsBook AI Agent의 MCP(Model Context Protocol) 계층은 외부 MCP Server와
통신하고, 서버가 제공하는 Tool을 탐색·등록·실행하기 위한 독립 모듈이다.

1차 Core의 목표는 다음 실행 흐름을 완성하는 것이다.

``` text
MCP Server Configuration
        ↓
MCP Transport
        ↓
MCP Client
        ↓
MCP Registry
        ↓
MCP Executor
        ↓
MCP Runtime
```

이를 통해 향후 Filesystem, Git, GitHub, Canva 등 다양한 MCP Server를
GomsBook AI Agent에 연결할 수 있는 기반을 제공한다.

------------------------------------------------------------------------

## 2. 설계 원칙

MCP 계층은 다음 원칙으로 구성한다.

-   MCP 프로토콜 모델과 실제 전송 계층을 분리한다.
-   STDIO와 Streamable HTTP Transport를 동일한 인터페이스로 추상화한다.
-   MCP Server별 Client를 독립적으로 관리한다.
-   Tool 이름은 전역적으로 유일하다고 가정하지 않는다.
-   Tool의 실질적인 식별자는 `(serverName, toolName)` 조합으로 관리한다.
-   Registry는 등록과 조회를 담당하고 Client lifecycle은 Runtime이
    담당한다.
-   Executor는 MCP Tool 실행만 담당한다.
-   하나의 MCP Server 장애가 다른 MCP Server 사용을 막지 않도록 한다.
-   Factory는 객체 생성만 담당하며 lifecycle을 자동으로 시작하지 않는다.

------------------------------------------------------------------------

## 3. 패키지 구조

권장 패키지 구조는 다음과 같다.

``` text
kr.co.goms.gomsbook.ai.mcpai.mcp
│
├─ McpComponentFactory.java
│
├─ protocol
│  ├─ McpProtocolVersion.java
│  ├─ McpMessage.java
│  ├─ McpRequest.java
│  ├─ McpResponse.java
│  ├─ McpError.java
│  └─ initialize
│     ├─ McpInitializeRequest.java
│     └─ McpInitializeResult.java
│
├─ tool
│  ├─ McpToolDefinition.java
│  ├─ McpToolCall.java
│  └─ McpToolResult.java
│
├─ client
│  ├─ McpClient.java
│  ├─ DefaultMcpClient.java
│  ├─ McpClientInfo.java
│  ├─ McpServerInfo.java
│  ├─ McpClientCapabilities.java
│  └─ McpServerCapabilities.java
│
├─ config
│  └─ McpServerConfiguration.java
│
├─ transport
│  ├─ McpTransport.java
│  ├─ McpTransportFactory.java
│  ├─ DefaultMcpTransportFactory.java
│  ├─ StdioMcpTransport.java
│  └─ StreamableHttpMcpTransport.java
│
├─ registry
│  ├─ McpRegistry.java
│  ├─ DefaultMcpRegistry.java
│  └─ McpToolReference.java
│
├─ executor
│  ├─ McpExecutor.java
│  └─ DefaultMcpExecutor.java
│
└─ runtime
   ├─ McpRuntime.java
   └─ DefaultMcpRuntime.java
```

실제 프로젝트의 기존 package naming convention에 따라 세부 경로는 조정할
수 있다.

------------------------------------------------------------------------

## 4. 구현 파일 목록

### 4.1 Protocol 계층

#### McpProtocolVersion.java

MCP 프로토콜 버전을 표현한다.

주요 책임:

-   프로토콜 버전 값 보관
-   버전 비교
-   기본 MCP 버전 제공
-   initialize 과정에서 negotiated version 검증

현재 1차 Core는 단일 기본 버전을 중심으로 구성하며, 향후 여러 MCP 버전을
지원할 경우 별도의 protocol support/negotiation 전략으로 확장할 수 있다.

#### McpMessage.java

MCP JSON-RPC 메시지의 공통 기반 모델이다.

#### McpRequest.java

MCP JSON-RPC request를 표현한다.

대표 구조:

``` json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list",
  "params": {}
}
```

#### McpResponse.java

MCP JSON-RPC response를 표현한다.

정상 결과와 protocol-level error를 구분한다.

#### McpError.java

JSON-RPC/MCP protocol 오류 정보를 표현한다.

Tool 자체의 실행 실패와는 구분한다.

------------------------------------------------------------------------

## 5. Initialize 계층

### McpInitializeRequest.java

MCP Client가 Server에 최초 handshake를 수행할 때 사용하는 initialize
요청 모델이다.

포함 정보:

-   protocolVersion
-   capabilities
-   clientInfo

### McpInitializeResult.java

MCP Server가 initialize 요청에 응답하는 정보를 표현한다.

포함 정보:

-   negotiated protocolVersion
-   serverCapabilities
-   serverInfo
-   instructions
-   `_meta`

초기화 흐름:

``` text
connect
   ↓
initialize
   ↓
InitializeResult
   ↓
protocol version 검증
   ↓
notifications/initialized
   ↓
READY
```

------------------------------------------------------------------------

## 6. Client 정보 및 Capability

### McpClientInfo.java

GomsBook MCP Client의 구현 정보를 표현한다.

예:

``` text
name    = GomsBook-AI-Agent
version = 1.0.0
```

### McpServerInfo.java

MCP Server가 initialize 응답으로 제공하는 구현 정보를 표현한다.

### McpClientCapabilities.java

Client가 지원하는 MCP 기능을 Server에 알린다.

1차 Core에서는 구현되지 않은 optional capability를 무리하게 선언하지
않고 최소 capability를 사용한다.

### McpServerCapabilities.java

Server가 제공하는 capability 정보를 표현한다.

예:

-   tools
-   resources
-   prompts
-   logging
-   completions
-   tasks
-   experimental

------------------------------------------------------------------------

## 7. Tool 계층

### McpToolDefinition.java

MCP Server가 공개하는 Tool 정의를 표현한다.

대표 정보:

-   name
-   title
-   description
-   inputSchema
-   outputSchema
-   annotations
-   `_meta`

### McpToolCall.java

실제 Tool 호출 정보를 표현한다.

개념 구조:

``` text
McpToolCall
 ├─ name
 └─ arguments
```

### McpToolResult.java

`tools/call` 실행 결과를 표현한다.

대표 정보:

-   content
-   structuredContent
-   isError
-   `_meta`

중요한 오류 구분:

``` text
McpResponse.isError()
        ↓
JSON-RPC / Protocol 오류


McpToolResult.isError()
        ↓
Tool 실행 자체의 오류
```

Tool 실행 오류를 무조건 transport/protocol 예외로 변환하지 않는다.

------------------------------------------------------------------------

## 8. Server Configuration

### McpServerConfiguration.java

각 MCP Server 연결 설정을 표현한다.

예상 대상:

``` text
filesystem
git
github
canva
기타 MCP Server
```

Transport 유형에 따라 다음 정보를 관리할 수 있다.

STDIO:

``` text
command
arguments
environment
```

Streamable HTTP:

``` text
endpoint
headers
timeout
```

------------------------------------------------------------------------

## 9. Transport 계층

### McpTransport.java

MCP 통신 방식을 추상화하는 최상위 Transport 인터페이스다.

상위 Client는 실제 연결 방식이 STDIO인지 HTTP인지 알 필요가 없다.

``` text
McpTransport
    ├─ StdioMcpTransport
    └─ StreamableHttpMcpTransport
```

### McpTransportFactory.java

Server configuration에 따라 적절한 Transport를 생성하는 팩토리
인터페이스다.

### DefaultMcpTransportFactory.java

기본 Transport 생성 구현체다.

개념 흐름:

``` text
McpServerConfiguration
        ↓
transport type
        ├─ STDIO
        │    ↓
        │  StdioMcpTransport
        │
        └─ STREAMABLE_HTTP
             ↓
           StreamableHttpMcpTransport
```

### StdioMcpTransport.java

로컬 프로세스 기반 MCP Server와 STDIO로 통신한다.

대표 사용 대상:

-   로컬 Filesystem MCP
-   로컬 Git MCP
-   command 기반 MCP Server

### StreamableHttpMcpTransport.java

HTTP 기반 MCP Server와 통신한다.

1차 구현 범위:

-   HTTP POST
-   `application/json`
-   `text/event-stream`
-   MCP Session ID 유지
-   negotiated protocol version header
-   notification 전송
-   session DELETE
-   request-scoped SSE 응답 처리

독립 GET SSE listener 및 server-to-client request dispatcher는 후속 확장
대상으로 둔다.

------------------------------------------------------------------------

## 10. MCP Client

### McpClient.java

하나의 MCP Server와 통신하는 Client 추상화다.

대표 기능:

``` text
connect()
initialize()
listTools()
callTool()
close()
```

### DefaultMcpClient.java

실제 MCP Client lifecycle을 구현한다.

핵심 실행 흐름:

``` text
connect
   ↓
initialize
   ↓
notifications/initialized
   ↓
tools/list
   ↓
tools/call
   ↓
close
```

`tools/list`의 cursor pagination을 Client 내부에서 처리하여 상위 계층이
pagination을 직접 관리하지 않도록 한다.

------------------------------------------------------------------------

## 11. Registry 계층

### McpRegistry.java

여러 MCP Client와 Server별 Tool 목록을 중앙 관리한다.

대표 책임:

-   Client 등록
-   Client 조회
-   Tool discovery
-   Tool cache
-   Tool 검색
-   Server별 Tool 조회

### DefaultMcpRegistry.java

Registry 기본 구현체다.

내부 구조:

``` text
clients
 ├─ filesystem → McpClient
 ├─ git        → McpClient
 └─ canva      → McpClient

toolsByServer
 ├─ filesystem → [...]
 ├─ git        → [...]
 └─ canva      → [...]
```

Tool 이름을 전역 key로 사용하지 않는다.

예를 들어:

``` text
filesystem → read_file
workspace  → read_file
```

가 동시에 존재할 수 있다.

따라서 Tool 식별은 다음 조합을 사용한다.

``` text
(serverName, toolName)
```

### McpToolReference.java

특정 MCP Server가 제공하는 Tool이라는 관계를 표현한다.

``` text
McpToolReference
 ├─ serverName
 └─ McpToolDefinition
```

------------------------------------------------------------------------

## 12. Executor 계층

### McpExecutor.java

Registry에서 대상 Server/Tool을 찾아 실제 Tool 호출을 수행하는 실행
인터페이스다.

두 가지 실행 방식을 제공한다.

명시적 Server 실행:

``` java
executor.execute(
        "filesystem",
        "read_file",
        arguments);
```

고유 Tool 자동 탐색:

``` java
executor.executeUnique(
        "read_file",
        arguments);
```

### DefaultMcpExecutor.java

실제 Tool 실행 구현체다.

검증 흐름:

``` text
Tool 등록 여부
      ↓
Client 존재 여부
      ↓
connected
      ↓
initialized
      ↓
McpClient.callTool()
```

`executeUnique()` 사용 시 동일 이름의 Tool이 여러 Server에 존재하면 임의
선택하지 않고 ambiguous 오류로 처리한다.

------------------------------------------------------------------------

## 13. Runtime 계층

### McpRuntime.java

MCP 독립 모듈의 상위 lifecycle facade다.

대표 책임:

-   Client 등록
-   Server 시작
-   Server 종료
-   전체 시작/종료
-   Tool refresh
-   Registry 접근
-   Executor 접근

### DefaultMcpRuntime.java

실제 lifecycle을 관리한다.

Server 시작 흐름:

``` text
register
   ↓
connect
   ↓
initialize
   ↓
tools/list
   ↓
registry refresh
```

전체 `start()`에서는 Server별 실패를 격리한다.

예:

``` text
filesystem  → READY
git         → READY
canva       → FAILED
```

이 경우 Canva 연결 실패 때문에 Filesystem과 Git까지 중단시키지 않는다.

반면:

``` java
runtime.start("canva");
```

처럼 특정 Server 시작을 명시적으로 요청한 경우 실패를 호출자에게
전달한다.

------------------------------------------------------------------------

## 14. Component Factory

### McpComponentFactory.java

MCP 계층의 객체 생성을 중앙화한다.

생성 대상:

-   McpTransportFactory
-   McpClient
-   McpRegistry
-   McpExecutor
-   McpRuntime
-   ClientInfo
-   ClientCapabilities

구조:

``` text
McpComponentFactory
        │
        ├─ DefaultMcpTransportFactory
        ├─ DefaultMcpClient
        ├─ DefaultMcpRegistry
        ├─ DefaultMcpExecutor
        └─ DefaultMcpRuntime
```

Factory에서는 Runtime을 자동 시작하지 않는다.

``` text
Factory = 객체 생성
Runtime = lifecycle
```

책임을 분리한다.

------------------------------------------------------------------------

## 15. 전체 아키텍처

``` text
                 GomsBook AI
                      │
                      ▼
                 McpRuntime
                      │
          ┌───────────┴───────────┐
          ▼                       ▼
     McpRegistry              McpExecutor
          │                       │
          └───────────┬───────────┘
                      ▼
                  McpClient
                      │
              DefaultMcpClient
                      │
                      ▼
                McpTransport
               /            \
              /              \
             ▼                ▼
 StdioMcpTransport   StreamableHttpMcpTransport
             │                │
             ▼                ▼
       Local Process       HTTP MCP Server
```

------------------------------------------------------------------------

## 16. 다중 MCP Server 구성

향후 GomsBook AI Agent에서는 다음과 같은 구성이 가능하다.

``` text
GomsBookAiRuntime
        │
        └─ McpRuntime
             │
             ├─ filesystem
             │    └─ Filesystem MCP Server
             │
             ├─ git
             │    └─ Git MCP Server
             │
             ├─ github
             │    └─ GitHub MCP Server
             │
             └─ canva
                  └─ Canva MCP Server
```

각 Server는 독립적인 Client와 lifecycle을 갖는다.

------------------------------------------------------------------------

## 17. GomsBook AI 기존 Tool 계층과의 관계

GomsBook AI에는 기존 내부 Tool 계층이 존재한다.

``` text
AgentTool
ToolRegistry
ToolExecutor
```

MCP Tool은 이 계층과 동일한 객체로 직접 합치지 않는다.

``` text
GomsBook Native Tool
        │
        ├─ GenerateXhtmlTool
        ├─ ValidateXhtmlTool
        ├─ AnalyzeImageTool
        ├─ GenerateEpubTool
        └─ ...

External MCP Tool
        │
        ├─ Filesystem MCP
        ├─ Git MCP
        ├─ GitHub MCP
        └─ Canva MCP
```

후속 통합 계층에서 Agent가 Native Tool과 MCP Tool을 동일한 실행 후보로
사용할 수 있도록 adapter/router를 구성하는 것이 바람직하다.

------------------------------------------------------------------------

## 18. 1차 Core 완료 범위

현재 MCP 독립 모듈 1차 Core 구현 범위:

  영역                           상태
  ------------------------------ ------
  Protocol model                 완료
  Request / Response / Error     완료
  Initialize model               완료
  Client / Server info           완료
  Client / Server capabilities   완료
  Tool definition                완료
  Tool call/result               완료
  Server configuration           완료
  Transport abstraction          완료
  STDIO transport                완료
  Streamable HTTP transport      완료
  Transport factory              완료
  MCP Client                     완료
  Registry                       완료
  Tool reference                 완료
  Executor                       완료
  Runtime                        완료
  Component factory              완료

------------------------------------------------------------------------

## 19. 2차 확장 대상

1차 Core 이후 다음 기능을 단계적으로 추가한다.

### 19.1 Message Dispatcher

``` text
McpMessageHandler
McpNotificationDispatcher
```

Server가 보내는 notification과 server-to-client request를 처리한다.

### 19.2 Streamable HTTP GET SSE Listener

장시간 유지되는 SSE channel을 통해 비동기 메시지를 처리한다.

### 19.3 Tool List Changed

Server가 Tool 목록 변경을 알리는 경우 Registry를 자동 refresh한다.

``` text
notifications/tools/list_changed
        ↓
McpRegistry.refreshTools(serverName)
```

### 19.4 Protocol Version Strategy

여러 MCP protocol version을 지원할 경우 별도 협상 전략을 추가한다.

``` text
McpProtocolSupport
 ├─ version A
 └─ version B
```

### 19.5 Error Model

현재 일부 구현에서 사용하는 `IllegalStateException`을 MCP 전용 예외
계층으로 세분화할 수 있다.

예:

``` text
McpException
 ├─ McpConnectionException
 ├─ McpInitializationException
 ├─ McpProtocolException
 ├─ McpToolNotFoundException
 ├─ McpAmbiguousToolException
 └─ McpSessionExpiredException
```

### 19.6 Runtime Status

Server별 상태를 명시적으로 관리할 수 있다.

``` text
REGISTERED
CONNECTING
INITIALIZING
READY
FAILED
STOPPED
```

### 19.7 Native Tool Integration

기존 GomsBook Agent Framework와 MCP Tool을 연결한다.

``` text
DefaultAgentExecutor
        │
        ▼
   Tool Router
     /     \
    ▼       ▼
 Native    MCP
 Tools     Tools
```

------------------------------------------------------------------------

## 20. 1차 Core 이후 권장 작업 순서

``` text
1. MCP 1차 Core 컴파일 정합성 점검
        ↓
2. 기존 클래스 API와 실제 구현 간 불일치 수정
        ↓
3. GomsBookAiRuntime에 McpRuntime 통합
        ↓
4. Filesystem MCP 실제 연결 테스트
        ↓
5. Git MCP 연결
        ↓
6. GitHub MCP 연결
        ↓
7. Canva MCP 연결
        ↓
8. Native Tool / MCP Tool Router 통합
        ↓
9. Notification / SSE / Message Dispatcher 확장
```

특히 `GomsBookAiRuntime` 통합 전에 **1차 Core 전체 컴파일 정합성
점검**을 먼저 수행하는 것을 권장한다. 개별 클래스를 순차 구현하면서
constructor, factory method, getter 이름 등이 변경되었을 가능성이 있기
때문이다.

------------------------------------------------------------------------

## 21. 현재 완료 상태

``` text
MCP Independent Module
│
├─ Protocol               COMPLETE
├─ Initialize             COMPLETE
├─ Tool Model             COMPLETE
├─ Configuration          COMPLETE
├─ Transport              COMPLETE
│  ├─ STDIO               COMPLETE
│  └─ Streamable HTTP     COMPLETE
├─ Client                 COMPLETE
├─ Registry               COMPLETE
├─ Executor               COMPLETE
├─ Runtime                COMPLETE
└─ Component Factory      COMPLETE

             1st CORE COMPLETE
```

다음 단계는 새로운 기능 추가보다 **MCP 1차 Core 전체 소스의 컴파일/API
정합성 검증**을 수행한 뒤 `GomsBookAiRuntime`에 통합하는 것이다.
