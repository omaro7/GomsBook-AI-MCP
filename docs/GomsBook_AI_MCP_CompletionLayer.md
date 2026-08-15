# MCP Completion 계층

GomsBook AI Agent의 MCP(Model Context Protocol) Completion 계층입니다.

> 기준 프로토콜: **MCP 2026-07-28**

## 1. 목적

Completion 계층은 MCP Client가 Prompt 또는 Resource Template의 인자 값을
자동완성할 수 있도록 하는 계층입니다.

주요 책임은 다음과 같습니다.

-   Completion 대상 Reference 표현
-   Prompt Reference 지원
-   Resource Template Reference 지원
-   자동완성 Argument 표현
-   Completion Context 전달
-   `completion/complete` 요청 Params 처리
-   Completion 후보 결과 표현
-   Completion Provider 등록 및 검색
-   Completion Service를 통한 Provider 호출
-   Reference 타입의 Gson 직렬화/역직렬화 지원

상위 Server/Dispatcher/Runtime 통합은 다른 MCP 하위 계층이 완료된 후
일괄 정리합니다.

------------------------------------------------------------------------

## 2. 패키지

``` text
kr.co.goms.gomsbook.ai.mcp.completion
```

Codec 관련 구현은 프로젝트의 MCP codec 패키지에 위치할 수 있습니다.

------------------------------------------------------------------------

## 3. 전체 구조

현재 Completion 계층의 주요 클래스는 다음과 같습니다.

``` text
completion
├─ McpCompletionReference.java
├─ McpPromptReference.java
├─ McpResourceTemplateReference.java
│
├─ McpCompletionArgument.java
├─ McpCompletionContext.java
├─ McpCompleteParams.java
│
├─ McpCompletion.java
├─ McpCompleteResult.java
│
├─ McpCompletionProvider.java
├─ McpCompletionRegistry.java
├─ DefaultMcpCompletionRegistry.java
│
├─ McpCompletionService.java
├─ DefaultMcpCompletionService.java
│
└─ McpCompletionNotFoundException.java

codec
└─ McpCompletionReferenceTypeAdapterFactory.java
```

공통 JSON codec에서는 필요에 따라 다음과 연결됩니다.

``` text
GsonMcpJsonCodec
        │
        └─ McpCompletionReferenceTypeAdapterFactory
```

------------------------------------------------------------------------

## 4. 계층 구조

``` text
McpCompleteParams
        │
        ├─ McpCompletionReference
        │      ├─ McpPromptReference
        │      └─ McpResourceTemplateReference
        │
        ├─ McpCompletionArgument
        └─ McpCompletionContext
                │
                ▼
      McpCompletionService
                │
                ▼
      McpCompletionRegistry
                │
                ▼
      McpCompletionProvider
                │
                ▼
          McpCompletion
                │
                ▼
       McpCompleteResult
```

Completion 계층 내부에서는 **Reference → Params → Registry → Provider →
Service → Result** 책임을 분리합니다.

------------------------------------------------------------------------

## 5. MCP Operation

Completion 계층의 핵심 MCP operation은 다음과 같습니다.

``` text
completion/complete
```

Client가 특정 Prompt 또는 Resource Template의 argument에 대해 현재
입력값을 전달하면 Server가 가능한 Completion 후보를 반환합니다.

개념적인 요청 흐름:

``` text
completion/complete
        ↓
McpCompleteParams
        ↓
Reference + Argument + Context
        ↓
McpCompletionService
        ↓
McpCompletionProvider
        ↓
McpCompletion
        ↓
McpCompleteResult
```

------------------------------------------------------------------------

## 6. Completion Reference

### `McpCompletionReference`

Completion 대상의 공통 추상화입니다.

Completion 대상은 대표적으로 다음 두 종류입니다.

``` text
Prompt
Resource Template
```

따라서 구조는 다음과 같습니다.

``` text
McpCompletionReference
        ▲
        ├─ McpPromptReference
        └─ McpResourceTemplateReference
```

Reference 타입은 JSON 역직렬화 시 실제 subtype을 판별할 수 있어야
합니다.

### `McpPromptReference`

Prompt 기반 Completion 대상을 표현합니다.

예:

``` text
Prompt: generate-xhtml
Argument: style
Current value: "ac"
```

Server는 해당 Prompt의 `style` argument에 적합한 후보를 반환할 수
있습니다.

### `McpResourceTemplateReference`

Resource Template 기반 Completion 대상을 표현합니다.

예:

``` text
gomsbook://project/xhtml/{fileName}
```

`fileName` argument에 대해 프로젝트 내 XHTML 파일명을 Completion 후보로
제공할 수 있습니다.

------------------------------------------------------------------------

## 7. `McpCompletionArgument`

현재 자동완성하려는 argument를 표현합니다.

개념적으로 다음 정보를 가집니다.

``` text
name
value
```

예:

``` text
name  = fileName
value = chapter
```

Provider는 `value`를 prefix 또는 검색어로 사용하여 후보를 생성할 수
있습니다.

------------------------------------------------------------------------

## 8. `McpCompletionContext`

현재 Completion 요청과 관련된 추가 argument/context를 표현합니다.

예를 들어 Resource Template이:

``` text
gomsbook://project/{type}/{fileName}
```

형태라면 현재 자동완성 대상이 `fileName`이더라도 이미 선택된:

``` text
type = xhtml
```

정보가 Completion 후보 결정에 필요할 수 있습니다.

즉 Context는 **현재 argument 외의 관련 입력 상태**를 Provider에 전달하는
역할을 합니다.

------------------------------------------------------------------------

## 9. `McpCompleteParams`

`completion/complete` 요청의 입력 DTO입니다.

개념적인 구조:

``` text
McpCompleteParams
├─ reference
├─ argument
└─ context
```

각 필드의 역할:

``` text
reference
    → 어떤 Prompt/Resource Template을 완성하는가

argument
    → 어떤 argument를 현재 완성하는가

context
    → 다른 argument들의 현재 값
```

Service 계층은 raw JSON을 직접 처리하기보다 `McpCompleteParams`를
입력으로 받도록 유지하는 것이 좋습니다.

------------------------------------------------------------------------

## 10. `McpCompletion`

실제 Completion 후보 집합을 표현합니다.

대표적으로 다음 정보가 포함될 수 있습니다.

``` text
values
total
hasMore
```

예:

``` text
values:
  - chapter01.xhtml
  - chapter02.xhtml
  - chapter03.xhtml

total:
  24

hasMore:
  true
```

`values`는 현재 응답에 포함된 Completion 후보입니다.

`total`은 가능한 전체 후보 수를 나타낼 수 있습니다.

`hasMore`는 추가 후보가 존재하는지 나타냅니다.

------------------------------------------------------------------------

## 11. `McpCompleteResult`

`completion/complete` operation의 최종 결과 DTO입니다.

개념적인 구조:

``` text
McpCompleteResult
└─ completion
       ├─ values
       ├─ total
       └─ hasMore
```

GomsBook MCP Core에서 공통 `McpResult` 계층을 사용하는 경우 Completion
Result 역시 최종적으로 공통 Result 구조와 정합성을 유지해야 합니다.

상위 Result 통합은 다른 MCP 하위 계층 완료 후 공통 Result 점검 단계에서
일괄 확인합니다.

------------------------------------------------------------------------

## 12. `McpCompletionProvider`

실제 Completion 후보를 생성하는 SPI입니다.

Provider는 특정 Reference 또는 Completion 영역을 담당합니다.

개념적인 책임:

``` text
Completion 요청 지원 여부 판단
        ↓
Argument / Context 확인
        ↓
Completion 후보 생성
        ↓
McpCompletion 반환
```

예를 들어 GomsBook에서는 다음과 같은 Provider를 향후 구성할 수 있습니다.

``` text
PromptArgumentCompletionProvider
ProjectXhtmlCompletionProvider
ProjectCssCompletionProvider
ProjectImageCompletionProvider
```

단, 실제 Provider 클래스는 필요한 Completion 시나리오가 확정된 뒤
추가합니다.

------------------------------------------------------------------------

## 13. `McpCompletionRegistry`

Completion Provider 등록과 검색을 담당합니다.

Registry의 책임은 다음 범위로 제한합니다.

``` text
Provider 등록
Provider 제거
Provider 검색
Provider 목록 제공
```

실제 Completion 수행이나 결과 조립은 Registry가 담당하지 않습니다.

------------------------------------------------------------------------

## 14. `DefaultMcpCompletionRegistry`

`McpCompletionRegistry`의 기본 구현입니다.

권장 책임:

``` text
Provider 저장
중복 등록 방지
Reference에 적합한 Provider 검색
등록 순서의 안정성 유지
```

Resources/Prompts와 마찬가지로 Registry와 Service의 책임을 분리합니다.

------------------------------------------------------------------------

## 15. `McpCompletionService`

Completion application service 계약입니다.

개념적으로:

``` java
McpCompleteResult complete(
        McpCompleteParams params);
```

형태로 `completion/complete` operation을 처리합니다.

Service는 다음 작업을 담당합니다.

``` text
Params 검증
        ↓
Reference 확인
        ↓
Registry에서 Provider 검색
        ↓
Provider Completion 실행
        ↓
Result 생성
```

------------------------------------------------------------------------

## 16. `DefaultMcpCompletionService`

기본 Completion Service 구현입니다.

권장 처리 흐름:

``` text
McpCompleteParams
        ↓
reference 검증
        ↓
argument 검증
        ↓
context 확인
        ↓
Completion Provider 검색
        ↓
provider.complete(...)
        ↓
McpCompletion 검증
        ↓
McpCompleteResult 생성
```

Provider가 존재하지 않는 경우 `McpCompletionNotFoundException`으로
처리합니다.

------------------------------------------------------------------------

## 17. `McpCompletionNotFoundException`

Completion 요청을 처리할 Provider 또는 대상이 존재하지 않는 경우
사용하는 domain exception입니다.

다음과 같은 상황을 구분하는 데 사용합니다.

``` text
지원하지 않는 Completion Reference
Completion Provider 없음
Completion 대상 Prompt/Resource Template 없음
```

잘못된 argument 또는 필수 parameter 누락은 별도의 invalid parameter
흐름으로 처리하고, 모든 오류를 `NotFound`로 변환하지 않습니다.

------------------------------------------------------------------------

## 18. Gson Polymorphic Reference 처리

Completion Reference는 다음 두 subtype을 가질 수 있습니다.

``` text
McpPromptReference
McpResourceTemplateReference
```

따라서 Gson이 JSON을 역직렬화할 때 어떤 구현 클래스를 생성해야 하는지
판별해야 합니다.

이를 담당하는 클래스가:

``` text
McpCompletionReferenceTypeAdapterFactory
```

입니다.

개념적인 처리 흐름:

``` text
JSON
 ↓
Reference type 확인
 ↓
Prompt Reference?
Resource Template Reference?
 ↓
해당 Java subtype 생성
```

`GsonMcpJsonCodec`에서는 Completion Reference Adapter를 등록하여 공통
codec에서 자동으로 처리할 수 있도록 합니다.

------------------------------------------------------------------------

## 19. `GsonMcpJsonCodec` 연동

Completion 계층 자체가 JSON codec의 전체 책임을 갖지는 않습니다.

Codec 계층에서 다음과 같이 Completion Reference Adapter를 등록합니다.

``` text
GsonBuilder
    ↓
McpCompletionReferenceTypeAdapterFactory
    ↓
Gson
```

이렇게 하면 Dispatcher/Handler/Service에서 subtype 판별을 직접 수행하지
않아도 됩니다.

------------------------------------------------------------------------

## 20. Prompt 계층과의 관계

Completion은 Prompt argument 자동완성에 사용될 수 있습니다.

``` text
McpPrompt
    ↓
McpPromptArgument
    ↓
McpPromptReference
    ↓
completion/complete
    ↓
Completion Provider
```

예:

``` text
Prompt:
  generate-chapter

Argument:
  tone

입력:
  "for"

Completion:
  formal
```

Completion 계층은 Prompt 자체를 소유하지 않습니다.

Prompt 정의와 Prompt 실행은 Prompts 계층이 담당합니다.

------------------------------------------------------------------------

## 21. Resources 계층과의 관계

Resource Template argument도 Completion 대상입니다.

``` text
McpResourceTemplate
        ↓
McpResourceTemplateReference
        ↓
completion/complete
        ↓
Completion Provider
```

예:

``` text
URI Template:
gomsbook://project/xhtml/{fileName}

현재 입력:
chapter

후보:
chapter01.xhtml
chapter02.xhtml
chapter03.xhtml
```

Completion 계층은 Resource를 직접 읽는 계층이 아닙니다.

실제 Resource 탐색이 필요하면 Resources Service/Provider와 협력합니다.

------------------------------------------------------------------------

## 22. GomsBook 활용 예

### XHTML 파일명 Completion

``` text
Reference:
gomsbook://project/xhtml/{fileName}

Argument:
fileName = "chapter"

Result:
chapter01.xhtml
chapter02.xhtml
chapter03.xhtml
```

### CSS 파일 Completion

``` text
Reference:
gomsbook://project/css/{fileName}

Argument:
fileName = "nav"

Result:
nav.css
```

### 이미지 Resource Completion

``` text
Reference:
gomsbook://project/images/{fileName}

Argument:
fileName = "cover"

Result:
cover.jpg
cover.png
```

### Prompt Argument Completion

``` text
Prompt:
generate-xhtml

Argument:
style = "ac"

Result:
accessible
```

------------------------------------------------------------------------

## 23. 안정성 원칙

Completion Provider는 다음 원칙을 유지합니다.

1.  null Params를 허용할지 명확히 정의합니다.
2.  Reference subtype을 명시적으로 검증합니다.
3.  필수 argument name/value를 검증합니다.
4.  Context가 없어도 처리 가능한 Provider는 empty context를 허용합니다.
5.  Completion 후보의 순서를 가능한 한 deterministic하게 유지합니다.
6.  중복 후보는 제거하는 것을 권장합니다.
7.  Completion 후보 생성 실패를 대상 미존재와 혼동하지 않습니다.
8.  Registry는 Provider 실행 책임을 갖지 않습니다.
9.  Service가 Provider 탐색과 결과 조립을 담당합니다.
10. JSON polymorphism은 Codec 계층에서 처리합니다.

------------------------------------------------------------------------

## 24. 상위 계층 통합 정책

Completion 계층 구현 중에는 다음 상위 클래스를 반복 수정하지 않습니다.

``` text
McpServerCapabilities
McpRequestDispatcher
DefaultMcpServer
McpServerComponentFactory
DefaultMcpServerRuntime
```

다른 MCP 하위 계층까지 완료한 뒤 일괄 통합합니다.

최종 통합 시 Dispatcher에는 개념적으로 다음 operation이 추가됩니다.

``` text
completion/complete
```

그리고 Server Capability와 protocol routing을 당시 최종 MCP 구조에 맞춰
함께 점검합니다.

------------------------------------------------------------------------

## 25. 현재 구현 상태

``` text
Completion Reference                    완료
Prompt Reference                        완료
Resource Template Reference             완료

Completion Argument                     완료
Completion Context                      완료
Complete Params                         완료

Completion                              완료
Complete Result                         완료

Completion Provider                     완료
Completion Registry                     완료
Default Completion Registry             완료

Completion Service                      완료
Default Completion Service              완료

Completion Not Found Exception          완료

Completion Reference Gson Adapter       완료
GsonMcpJsonCodec 연동                   점검/통합 대상

공통 McpResult 정합성                   최종 공통 점검 대상
Server/Dispatcher 통합                  보류
실제 GomsBook Completion Provider       필요 시 추가
```

------------------------------------------------------------------------

## 26. 완료 기준

Completion 계층 자체의 1차 완료 기준은 다음과 같습니다.

``` text
Reference subtype 모델 완성
        ↓
Argument / Context / Params 완성
        ↓
Completion / Result 완성
        ↓
Provider 계약 완성
        ↓
Registry 구현 완성
        ↓
Service 구현 완성
        ↓
Exception 처리 완성
        ↓
Gson Reference polymorphism 처리
```

이후 전체 MCP 통합 단계에서 다음을 다시 점검합니다.

``` text
McpResult / McpResultType
        ↓
GsonMcpJsonCodec
        ↓
McpServerCapabilities
        ↓
McpRequestDispatcher
        ↓
DefaultMcpServer
        ↓
McpServerComponentFactory
        ↓
DefaultMcpServerRuntime
```

------------------------------------------------------------------------

## 27. 다음 단계

Completion 계층은 현재 **하위 계층 1차 구현 완료 상태**로 관리합니다.

다른 MCP 하위 계층을 구현한 후 마지막에:

``` text
MCP 하위 계층 완료
        ↓
공통 Result/Metadata 구조 정합성 점검
        ↓
Codec 통합
        ↓
Server Capability 통합
        ↓
Request Dispatcher 통합
        ↓
Server/Factory/Runtime 통합
        ↓
전체 컴파일 점검
        ↓
MCP 2026-07-28 프로토콜 최종 점검
```

순서로 마무리합니다.
