# MCP Resources 계층

GomsBook AI Agent의 MCP(Model Context Protocol) Resources 계층입니다.

> 기준 프로토콜: **MCP 2026-07-28**

## 1. 목적

Resources 계층은 MCP Client가 GomsBook 프로젝트의 EPUB 리소스를 탐색하고
읽을 수 있도록 하는 계층입니다.

주요 책임은 다음과 같습니다.

-   프로젝트 Resource 목록 제공
-   URI 기반 Resource 읽기
-   Resource Template 목록 제공
-   Resource Provider 등록 및 검색
-   cursor 기반 pagination
-   Result cache hint 제공
-   프로젝트 XHTML, CSS, 이미지, 메타데이터, 내비게이션 Resource 제공

상위 Server/Dispatcher/Runtime 통합은 다른 MCP 하위 계층이 완료된 후
일괄 정리합니다.

------------------------------------------------------------------------

## 2. 패키지

``` text
kr.co.goms.gomsbook.ai.mcp.resources
```

------------------------------------------------------------------------

## 3. 전체 구조

``` text
resources
├─ McpResource.java
├─ McpResourceContents.java
├─ McpResourceTemplate.java
│
├─ McpListResourcesParams.java
├─ McpReadResourceParams.java
├─ McpListResourceTemplatesParams.java
│
├─ McpListResourcesResult.java
├─ McpReadResourceResult.java
├─ McpListResourceTemplatesResult.java
│
├─ McpResourceProvider.java
├─ McpResourceRegistry.java
├─ DefaultMcpResourceRegistry.java
│
├─ McpResourceService.java
├─ DefaultMcpResourceService.java
│
├─ McpResourceNotFoundException.java
│
├─ ProjectXhtmlResourceProvider.java
├─ ProjectMetadataResourceProvider.java
├─ ProjectNavigationResourceProvider.java
├─ ProjectImageResourceProvider.java
└─ ProjectCssResourceProvider.java
```

------------------------------------------------------------------------

## 4. 계층 구조

``` text
Project Resource Providers
        │
        ▼
McpResourceRegistry
        │
        ▼
McpResourceService
        │
        ▼
Resources Result
        │
        ▼
MCP Request Handler / Dispatcher
```

Resources 계층 내부에서는 Provider → Registry → Service → Result 책임을
분리합니다.

상위 Request Handler, Dispatcher, Server, Runtime은 Resources 계층에
포함하지 않습니다.

------------------------------------------------------------------------

## 5. MCP Operations

현재 Resources 계층은 다음 MCP operation을 대상으로 합니다.

### `resources/list`

서버가 노출하는 Resource 목록을 반환합니다.

입력:

``` java
McpListResourcesParams
```

출력:

``` java
McpListResourcesResult
```

주요 필드:

-   `resources`
-   `nextCursor`
-   `ttlMs`
-   `cacheScope`

### `resources/read`

Resource URI를 사용하여 실제 내용을 읽습니다.

입력:

``` java
McpReadResourceParams
```

출력:

``` java
McpReadResourceResult
```

주요 필드:

-   `contents`
-   `ttlMs`
-   `cacheScope`

### `resources/templates/list`

Resource Template 목록을 반환합니다.

입력:

``` java
McpListResourceTemplatesParams
```

출력:

``` java
McpListResourceTemplatesResult
```

주요 필드:

-   `resourceTemplates`
-   `nextCursor`
-   `ttlMs`
-   `cacheScope`

------------------------------------------------------------------------

## 6. Core Models

### `McpResource`

MCP Client에 노출되는 Resource의 메타정보를 표현합니다.

대표 정보:

``` text
uri
name
title
description
mimeType
annotations
size
_meta
```

향후 공통 `McpIcon` 정리 시 `icons` 지원을 함께 검토합니다.

### `McpResourceContents`

`resources/read`에서 반환하는 실제 Resource 내용을 표현합니다.

텍스트 Resource:

``` java
McpResourceContents.text(
        uri,
        mimeType,
        text);
```

Binary Resource:

``` java
McpResourceContents.blob(
        uri,
        mimeType,
        base64);
```

`text`와 `blob`은 동시에 존재할 수 없습니다.

빈 문자열은 유효한 Resource 내용으로 취급합니다.

즉:

``` text
null = 해당 content field가 없음
""   = field는 존재하지만 내용이 비어 있음
```

### `McpResourceTemplate`

URI Template 기반으로 동적 Resource를 표현합니다.

예:

``` text
gomsbook://project/xhtml/{fileName}
gomsbook://project/css/{fileName}
gomsbook://project/images/{fileName}
```

------------------------------------------------------------------------

## 7. Params

### `McpListResourcesParams`

`resources/list`의 pagination cursor를 전달합니다.

``` java
McpListResourcesParams.empty()
```

또는:

``` java
McpListResourcesParams.builder()
        .cursor(cursor)
        .build();
```

### `McpReadResourceParams`

읽을 Resource URI를 전달합니다.

``` java
McpReadResourceParams.of(
        "gomsbook://project/xhtml/chapter01.xhtml");
```

### `McpListResourceTemplatesParams`

Resource Template 목록의 pagination cursor를 전달합니다.

``` java
McpListResourceTemplatesParams.empty()
```

------------------------------------------------------------------------

## 8. Result 계층

Resources Result는 GomsBook MCP Core의 공통 Result 체계를 따릅니다.

``` text
McpResult
   ▲
   ├─ McpListResourcesResult
   ├─ McpReadResourceResult
   └─ McpListResourceTemplatesResult
```

공통 `resultType`은 상위 `McpResult`가 관리합니다.

Resources Result가 자체적으로 중복 `resultType` 필드를 관리하지 않도록
합니다.

### Cache Hint

현재 프로젝트 Resource의 기본 정책은 다음과 같이 운용할 수 있습니다.

``` text
ttlMs      = 0
cacheScope = private
```

프로젝트 편집 중 Resource가 자주 변경되므로 초기 구현에서는 즉시 stale
처리하는 정책이 안전합니다.

------------------------------------------------------------------------

## 9. Provider

### `McpResourceProvider`

특정 Resource 영역을 제공하는 SPI입니다.

Provider는 일반적으로 다음 책임을 갖습니다.

``` text
Provider ID 제공
Resource URI 지원 여부 판단
Resource 목록 제공
Resource Template 제공
Resource 읽기
```

각 Provider는 자신이 담당하는 프로젝트 영역만 처리합니다.

------------------------------------------------------------------------

## 10. Registry

### `McpResourceRegistry`

Resource Provider 등록과 검색을 담당합니다.

주요 책임:

``` text
register(provider)
unregister(providerId)
findById(providerId)
findProvider(uri)
getProviders()
supports(uri)
```

`findProvider(uri)`는:

``` java
Optional<McpResourceProvider>
```

을 반환합니다.

Registry는 Resource/Template 목록을 직접 집계하지 않습니다.

### `DefaultMcpResourceRegistry`

기본 Registry 구현입니다.

Provider 등록 순서를 유지하면서 URI를 지원하는 Provider를 검색합니다.

------------------------------------------------------------------------

## 11. Service

### `McpResourceService`

Resources protocol operation의 application-level 계약입니다.

권장 형태:

``` java
McpListResourcesResult listResources(
        McpListResourcesParams params);

McpReadResourceResult readResource(
        McpReadResourceParams params);

McpListResourceTemplatesResult listResourceTemplates(
        McpListResourceTemplatesParams params);

boolean supports(
        String uri);
```

### `DefaultMcpResourceService`

다음 작업을 담당합니다.

``` text
Provider Resource 집계
Provider Template 집계
URI → Provider 검색
cursor pagination
stable ordering
TTL/cacheScope 설정
Result 생성
```

Registry에는 `listResources()`나 `listResourceTemplates()`를 추가하지
않습니다.

목록 집계는 Service가 다음과 같이 수행합니다.

``` text
resourceRegistry.getProviders()
        ↓
각 Provider의 Resource/Template 수집
        ↓
null 제거
        ↓
stable sort
        ↓
pagination
        ↓
Result 생성
```

------------------------------------------------------------------------

## 12. Pagination

Resource와 Resource Template 목록은 cursor 기반 pagination을 지원하도록
설계합니다.

안정적인 pagination을 위해 목록은 정렬 후 페이지를 생성합니다.

예:

``` text
전체 목록
   ↓
URI/name 기준 stable sort
   ↓
cursor 해석
   ↓
page slice
   ↓
nextCursor 생성
```

Provider 등록 순서나 파일 시스템 반환 순서에 pagination 결과가 의존하지
않도록 합니다.

------------------------------------------------------------------------

## 13. Project Resource Providers

### `ProjectXhtmlResourceProvider`

EPUB XHTML 문서를 Resource로 제공합니다.

예:

``` text
gomsbook://project/xhtml/chapter01.xhtml
gomsbook://project/xhtml/chapter02.xhtml
```

대표 MIME type:

``` text
application/xhtml+xml
```

### `ProjectCssResourceProvider`

EPUB CSS 파일을 제공합니다.

예:

``` text
gomsbook://project/css/style.css
gomsbook://project/css/nav.css
```

대표 MIME type:

``` text
text/css
```

### `ProjectImageResourceProvider`

EPUB 이미지 파일을 제공합니다.

예:

``` text
gomsbook://project/images/cover.jpg
gomsbook://project/images/chapter01.png
```

Binary 내용은 Base64 encoded `blob`으로 반환합니다.

### `ProjectMetadataResourceProvider`

EPUB package metadata를 제공합니다.

예:

``` text
gomsbook://project/metadata
```

주 대상은 EPUB package document인 `content.opf`입니다.

### `ProjectNavigationResourceProvider`

EPUB Navigation Document를 제공합니다.

예:

``` text
gomsbook://project/navigation
```

주 대상은 `nav.xhtml`입니다.

------------------------------------------------------------------------

## 14. URI 보안

파일 기반 Provider는 반드시 project resource root 밖으로 탈출하는 경로를
차단해야 합니다.

예:

``` text
../../secret.txt
..\..\secret.txt
```

Resource 경로는 다음 절차를 권장합니다.

``` java
Path resolved =
        resourceRoot
                .resolve(fileName)
                .toAbsolutePath()
                .normalize();

if (!resolved.startsWith(resourceRoot)) {
    throw new IllegalArgumentException(...);
}
```

현재 1차 Provider가 단일 디렉터리 파일만 허용한다면 `/`, `\`가 포함된
fileName을 차단하여 하위 경로 접근도 제한할 수 있습니다.

------------------------------------------------------------------------

## 15. Exception 정책

### `McpResourceNotFoundException`

다음 경우에 사용합니다.

``` text
URI를 지원하는 Provider가 없음
Resource가 존재하지 않음
Provider가 유효한 Resource contents를 반환하지 못함
```

잘못된 URI/parameter는:

``` text
IllegalArgumentException
```

으로 유지합니다.

파일 시스템 오류나 Provider 내부 오류는
`McpResourceNotFoundException`으로 왜곡하지 않습니다.

예:

``` text
Resource 없음
    → McpResourceNotFoundException

잘못된 URI
    → IllegalArgumentException

I/O / Provider 내부 오류
    → RuntimeException 계열
```

상위 Dispatcher가 최종 MCP error mapping을 담당합니다.

------------------------------------------------------------------------

## 16. Resources 계층에 포함하지 않는 기능

다음 기능은 Resources 내부에 직접 구현하지 않습니다.

### Server 통합

다른 MCP 하위 계층 완료 후 일괄 수정합니다.

``` text
McpServerCapabilities
McpRequestDispatcher
DefaultMcpServer
McpServerComponentFactory
DefaultMcpServerRuntime
```

### Subscription / Notification

Resource 변경 구독과 notification delivery는 별도
subscription/notification 계층에서 처리합니다.

Resources 계층은 Resource 자체의 탐색/읽기 책임에 집중합니다.

### 공통 Icon

`McpIcon`은 Resources 전용 타입으로 만들지 않고 MCP 공통 모델로
관리합니다.

### MRTR / Input Required

`InputRequiredResult` 등 공통 Result 흐름은 Resources 내부가 아니라 MCP
Core/공통 계층에서 관리합니다.

------------------------------------------------------------------------

## 17. 구현 원칙

Resources 계층에서는 다음 원칙을 유지합니다.

1.  Registry는 Provider 등록과 검색만 담당합니다.
2.  Service가 여러 Provider의 Resource를 집계합니다.
3.  Provider는 자신이 담당하는 Resource 영역만 처리합니다.
4.  Params와 Result를 protocol operation 단위로 분리합니다.
5.  Result는 공통 `McpResult` 체계를 따릅니다.
6.  목록은 deterministic pagination을 위해 stable ordering을 적용합니다.
7.  빈 Resource 내용과 Resource 미존재를 구분합니다.
8.  예상하지 못한 내부 오류를 Not Found로 변환하지 않습니다.
9.  파일 기반 Provider는 path traversal을 차단합니다.
10. 상위 Server/Runtime 통합은 하위 계층 완료 후 일괄 수행합니다.

------------------------------------------------------------------------

## 18. 현재 구현 상태

``` text
Core Resource Model               완료
Resource Contents                 완료
Resource Template                 완료

List Resources Params             완료
Read Resource Params              완료
List Resource Templates Params    완료

List Resources Result             완료
Read Resource Result              완료
List Resource Templates Result    완료

Resource Provider                 완료
Resource Registry                 완료
Default Resource Registry         완료

Resource Service                  완료
Default Resource Service          완료

Resource Not Found Exception      완료

Project XHTML Provider            완료
Project Metadata Provider         완료
Project Navigation Provider       완료
Project Image Provider            완료
Project CSS Provider              완료

Server/Dispatcher 최종 통합       보류
Subscriptions/Notifications       별도 계층
공통 Icon/MRTR 보강               공통 계층에서 처리
```

------------------------------------------------------------------------

## 19. 다음 단계

Resources 계층은 현재 **1차 구현 완료 상태**로 봅니다.

다음 MCP 하위 계층을 구현한 후 마지막 단계에서 다음을 일괄 수행합니다.

``` text
MCP 하위 계층 완료
        ↓
공통 Result / Metadata 정합성 점검
        ↓
McpServerCapabilities 통합
        ↓
McpRequestDispatcher 통합
        ↓
DefaultMcpServer 통합
        ↓
McpServerComponentFactory 통합
        ↓
Runtime 통합
        ↓
전체 컴파일 및 protocol 정합성 점검
```
