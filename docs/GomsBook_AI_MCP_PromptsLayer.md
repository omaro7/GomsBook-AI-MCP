# MCP Prompts

`kr.co.goms.gomsbook.ai.mcp.prompts` 패키지는 GomsBook AI Agent의 MCP(Model Context Protocol) 서버에서 **Prompt 탐색 및 Prompt 생성 기능**을 담당한다.

본 계층은 MCP의 다음 서버 기능을 구현하기 위한 하위 계층이다.

```text
prompts/list
prompts/get
```

현재 구현은 GomsBook AI Agent의 MCP 공통 계층과 분리되어 있으며, 상위 `Server`, `Dispatcher`, `Capabilities` 계층과의 최종 연결은 다른 MCP 하위 계층 구현 완료 후 일괄 적용한다.

---

## 1. 역할

MCP Prompt는 서버가 제공하는 **재사용 가능한 Prompt 템플릿**이다.

Tool이 실제 기능 실행을 담당하고 Resource가 데이터를 제공한다면, Prompt는 LLM 또는 Client가 사용할 메시지 구성을 제공한다.

```text
Tools
 └─ 작업 실행

Resources
 └─ 데이터 제공

Prompts
 └─ 재사용 가능한 Prompt 메시지 제공
```

GomsBook AI Agent에서는 향후 다음과 같은 Prompt를 제공할 수 있다.

```text
ebook_chapter_write
accessibility_review
epub_validation_review
xhtml_improvement
project_summary
```

---

## 2. 패키지 구조

```text
kr.co.goms.gomsbook.ai.mcp.prompts
│
├─ McpPrompt.java
├─ McpPromptArgument.java
├─ McpPromptMessage.java
│
├─ McpListPromptsParams.java
├─ McpGetPromptParams.java
│
├─ McpListPromptsResult.java
├─ McpGetPromptResult.java
│
├─ McpPromptProvider.java
│
├─ McpPromptRegistry.java
├─ DefaultMcpPromptRegistry.java
│
├─ McpPromptService.java
├─ DefaultMcpPromptService.java
│
└─ McpPromptNotFoundException.java
```

---

## 3. 전체 구조

```text
MCP Client
    │
    │ prompts/list
    │ prompts/get
    ▼
McpRequestDispatcher
    │
    ▼
McpPromptService
    │
    ▼
DefaultMcpPromptService
    │
    ├──────────────┐
    ▼              ▼
McpPromptRegistry  Argument Validation
    │
    ▼
DefaultMcpPromptRegistry
    │
    ▼
McpPromptProvider
    │
    ├─ getPrompt()
    │
    └─ get(arguments)
           │
           ▼
    McpGetPromptResult
           │
           ▼
 List<McpPromptMessage>
           │
           ▼
       McpContent
```

---

# 4. 주요 모델

## McpPrompt

MCP 서버가 제공하는 Prompt 정의를 표현한다.

주요 필드는 다음과 같다.

```text
name
title
description
arguments
```

`name`은 Prompt의 고유 식별자이며 필수값이다.

예:

```java
McpPrompt prompt =
        McpPrompt.builder()
                .name(
                        "ebook_chapter_write"
                )
                .title(
                        "전자책 장 작성"
                )
                .description(
                        "전자책의 특정 장을 작성합니다."
                )
                .build();
```

Prompt 자체에는 실제 메시지를 저장하지 않는다.

```text
McpPrompt
    = Prompt 정의

McpGetPromptResult
    = Prompt 실행 결과
```

이를 통해 Prompt 메타데이터와 Prompt 생성 로직을 분리한다.

---

## McpPromptArgument

Prompt에서 사용할 argument의 정의를 표현한다.

```text
name
title
description
required
```

예:

```java
McpPromptArgument argument =
        McpPromptArgument.builder()
                .name(
                        "chapterTitle"
                )
                .title(
                        "장 제목"
                )
                .description(
                        "작성할 전자책 장 제목"
                )
                .required(
                        true
                )
                .build();
```

`required=true`인 argument는 `DefaultMcpPromptService`에서 공통 검증한다.

---

## McpPromptMessage

`prompts/get` 결과로 반환되는 하나의 Prompt 메시지를 표현한다.

```text
McpPromptMessage
 ├─ McpRole
 └─ McpContent
```

예:

```java
McpPromptMessage message =
        McpPromptMessage.builder()
                .role(
                        McpRole.USER
                )
                .content(
                        McpTextContent.builder()
                                .text(
                                        "전자책 1장을 작성해주세요."
                                )
                                .build()
                )
                .build();
```

Prompt 전용 Content 또는 Role 계층은 만들지 않는다.

공통 MCP Content 계층의 다음 클래스를 재사용한다.

```text
McpRole

McpContent
 ├─ McpTextContent
 ├─ McpImageContent
 ├─ McpAudioContent
 ├─ McpEmbeddedResourceContent
 └─ McpResourceLinkContent
```

---

# 5. Request Parameters

## McpListPromptsParams

`prompts/list` 요청 파라미터를 표현한다.

```text
cursor
```

`cursor`는 선택값이다.

첫 목록 조회:

```java
McpListPromptsParams params =
        McpListPromptsParams.empty();
```

현재 초기 구현에서는 실제 cursor pagination을 적용하지 않는다.

향후 Resources 및 다른 list 계열 API와 함께 공통 pagination 정책을 적용할 수 있다.

---

## McpGetPromptParams

`prompts/get` 요청 파라미터를 표현한다.

```text
name
arguments
```

예:

```java
McpGetPromptParams params =
        McpGetPromptParams.builder()
                .name(
                        "ebook_chapter_write"
                )
                .argument(
                        "chapterTitle",
                        "꽃은 자신을 재촉하지 않는다"
                )
                .argument(
                        "style",
                        "감성 에세이"
                )
                .build();
```

Prompt argument는 다음 형태로 관리한다.

```java
Map<String, String>
```

argument 값은 공백이나 개행이 의미를 가질 수 있으므로 임의로 `trim()`하지 않고 원문을 보존한다.

---

# 6. Result

## McpListPromptsResult

`prompts/list` 결과를 표현한다.

```text
prompts
nextCursor
```

구조:

```text
McpListPromptsResult
 ├─ List<McpPrompt>
 └─ String nextCursor
```

현재 전체 목록을 반환하는 구현에서는 `nextCursor`를 사용하지 않는다.

---

## McpGetPromptResult

`prompts/get` 결과를 표현한다.

```text
description
messages
```

구조:

```text
McpGetPromptResult
 ├─ String description
 └─ List<McpPromptMessage>
```

예:

```json
{
  "description": "전자책 장 작성 프롬프트",
  "messages": [
    {
      "role": "user",
      "content": {
        "type": "text",
        "text": "전자책의 새로운 장을 작성해주세요."
      }
    }
  ]
}
```

DTO 자체에서는 빈 `messages`를 표현할 수 있지만 실제 Prompt 처리 시에는 `DefaultMcpPromptService`가 빈 결과를 검증한다.

---

# 7. McpPromptProvider

`McpPromptProvider`는 **하나의 Prompt를 제공하는 실제 구현 계약**이다.

주요 메서드는 다음과 같다.

```java
McpPrompt getPrompt();

McpGetPromptResult get(
        Map<String, String> arguments
);
```

역할은 다음과 같이 구분한다.

```text
getPrompt()
    ↓
Prompt 정의 제공
    ↓
prompts/list


get(arguments)
    ↓
Prompt 메시지 생성
    ↓
prompts/get
```

예를 들어 GomsBook에서는 다음과 같은 구현체를 추가할 수 있다.

```text
EbookChapterWritePromptProvider
AccessibilityReviewPromptProvider
EpubValidationPromptProvider
XhtmlImprovementPromptProvider
ProjectSummaryPromptProvider
```

---

# 8. McpPromptRegistry

`McpPromptRegistry`는 Prompt Provider를 관리한다.

주요 기능:

```text
register(provider)
unregister(name)
find(name)
getAll()
contains(name)
size()
isEmpty()
```

Registry에는 `McpPrompt`가 아니라 `McpPromptProvider`를 등록한다.

```text
DefaultMcpPromptRegistry
        │
        ├─ ebook_chapter_write
        │       └─ McpPromptProvider
        │
        ├─ accessibility_review
        │       └─ McpPromptProvider
        │
        └─ project_summary
                └─ McpPromptProvider
```

`DefaultMcpPromptRegistry`는 `LinkedHashMap`을 사용하여 Provider를 이름 기준으로 관리한다.

동일한 Prompt 이름의 중복 등록은 허용하지 않는다.

---

# 9. McpPromptService

`McpPromptService`는 MCP Prompt 기능의 서비스 계약이다.

```java
McpListPromptsResult listPrompts(
        McpListPromptsParams params
);

McpGetPromptResult getPrompt(
        McpGetPromptParams params
);
```

MCP 프로토콜과 다음처럼 대응한다.

```text
prompts/list
    ↓
listPrompts()


prompts/get
    ↓
getPrompt()
```

---

# 10. DefaultMcpPromptService

Prompt 계층의 핵심 서비스 구현체이다.

## prompts/list

처리 흐름:

```text
listPrompts()
    ↓
registry.getAll()
    ↓
McpPromptProvider
    ↓
provider.getPrompt()
    ↓
List<McpPrompt>
    ↓
McpListPromptsResult
```

현재 Registry 기반 구현에서는 전체 Prompt 목록을 반환한다.

---

## prompts/get

처리 흐름:

```text
McpGetPromptParams
        ↓
Prompt name 검증
        ↓
registry.find(name)
        ↓
Provider 조회
        ↓
McpPrompt 정의 조회
        ↓
required arguments 검증
        ↓
provider.get(arguments)
        ↓
McpGetPromptResult
        ↓
messages 검증
```

Prompt가 존재하지 않으면:

```text
McpPromptNotFoundException
```

을 발생시킨다.

Provider가 `null` 결과를 반환하거나 유효한 Prompt message를 반환하지 않으면 Provider 구현 오류로 처리한다.

---

# 11. Argument Validation

Prompt argument 검증은 두 단계로 분리한다.

```text
DefaultMcpPromptService
    ↓
구조적 검증

McpPromptProvider
    ↓
업무/의미 검증
```

Service에서는 다음을 검증한다.

```text
Prompt 존재 여부
required argument 존재 여부
null 여부
```

Provider에서는 Prompt별 의미를 검증한다.

예:

```java
String chapterTitle =
        arguments.get(
                "chapterTitle"
        );

if (chapterTitle == null
        || chapterTitle.isBlank()) {

    throw new IllegalArgumentException(
            "chapterTitle must not be blank."
    );
}
```

이를 통해 Prompt마다 공통적인 required 검증 코드가 중복되는 것을 방지한다.

---

# 12. Content 계층과의 관계

Prompts 계층은 별도의 Content 모델을 만들지 않고 MCP 공통 Content 계층을 사용한다.

```text
prompts
   │
   └─ McpPromptMessage
           │
           ▼
content
   │
   ├─ McpRole
   └─ McpContent
        ├─ McpTextContent
        ├─ McpImageContent
        ├─ McpAudioContent
        ├─ McpEmbeddedResourceContent
        └─ McpResourceLinkContent
```

따라서 다음과 같은 Prompt 전용 타입은 추가하지 않는다.

```text
McpPromptRole
McpPromptContent
McpPromptTextContent
```

---

# 13. Gson 직렬화

`McpPromptMessage.content`는 `McpContent` 인터페이스이므로 공통 Content 계층의 Gson polymorphic adapter를 사용한다.

```text
GsonMcpJsonCodec
        ↓
McpContentTypeAdapterFactory
        ↓
McpContent
        ↓
실제 Content 구현체
```

따라서 Prompts 계층만을 위한 별도의 Gson TypeAdapterFactory는 필요하지 않는다.

다음 클래스들은 일반 Gson 직렬화/역직렬화를 사용한다.

```text
McpPrompt
McpPromptArgument
McpListPromptsParams
McpGetPromptParams
McpListPromptsResult
McpGetPromptResult
```

---

# 14. 예외 처리

## McpPromptNotFoundException

요청된 Prompt가 Registry에 존재하지 않을 때 발생한다.

```text
registry.find(name)
        ↓
Optional.empty()
        ↓
DefaultMcpPromptService
        ↓
McpPromptNotFoundException
```

Registry 자체는 MCP 예외를 발생시키지 않는다.

Registry는 단순 조회 책임만 담당하고 Service가 해당 결과를 MCP 서비스 오류로 변환한다.

향후 상위 `McpRequestDispatcher`에서 이 예외를 적절한 MCP/JSON-RPC 오류 응답으로 변환한다.

---

# 15. GomsBook Prompt 예시

전자책 장 작성 Prompt Provider는 다음과 같은 구조로 구현할 수 있다.

```java
public final class EbookChapterWritePromptProvider
        implements McpPromptProvider {

    private static final McpPrompt PROMPT =
            McpPrompt.builder()
                    .name(
                            "ebook_chapter_write"
                    )
                    .title(
                            "전자책 장 작성"
                    )
                    .description(
                            "전자책의 특정 장을 작성합니다."
                    )
                    .argument(
                            McpPromptArgument.builder()
                                    .name(
                                            "chapterTitle"
                                    )
                                    .description(
                                            "작성할 장 제목"
                                    )
                                    .required(
                                            true
                                    )
                                    .build()
                    )
                    .build();

    @Override
    public McpPrompt getPrompt() {

        return PROMPT;
    }

    @Override
    public McpGetPromptResult get(
            Map<String, String> arguments
    ) {

        String chapterTitle =
                arguments.get(
                        "chapterTitle"
                );

        McpPromptMessage message =
                McpPromptMessage.builder()
                        .role(
                                McpRole.USER
                        )
                        .content(
                                McpTextContent.builder()
                                        .text(
                                                "다음 제목으로 전자책 장을 작성해주세요: "
                                                        + chapterTitle
                                        )
                                        .build()
                        )
                        .build();

        return McpGetPromptResult.builder()
                .description(
                        PROMPT.getDescription()
                )
                .message(
                        message
                )
                .build();
    }
}
```

---

# 16. 상위 계층 연결

현재 Prompts 하위 계층에서는 상위 MCP 계층을 직접 수정하지 않는다.

다른 MCP 하위 계층 구현 완료 후 다음 파일을 일괄 수정한다.

```text
McpServerCapabilities.java
McpRequestDispatcher.java
DefaultMcpServer.java
McpServerComponentFactory.java
```

최종 연결 구조:

```text
MCP Transport
      ↓
MCP Runtime
      ↓
McpRequestDispatcher
      ↓
McpPromptService
      ↓
McpPromptRegistry
      ↓
McpPromptProvider
```

---

# 17. 현재 구현 상태

```text
McpPromptArgument              완료
McpPrompt                      완료
McpPromptMessage               완료

McpListPromptsParams           완료
McpGetPromptParams             완료

McpListPromptsResult           완료
McpGetPromptResult             완료

McpPromptProvider              완료

McpPromptRegistry              완료
DefaultMcpPromptRegistry       완료

McpPromptNotFoundException     완료

McpPromptService               완료
DefaultMcpPromptService        완료
```

Prompts 하위 계층 자체의 구조 및 컴파일 정합성 점검도 완료했다.

---

# 18. 향후 작업

Prompts 계층 이후에는 다른 MCP 하위 계층을 구현하고 최종적으로 상위 계층을 일괄 연결한다.

```text
하위 MCP 계층 구현
        ↓
각 계층 자체 정합성 점검
        ↓
McpServerCapabilities
        ↓
McpRequestDispatcher
        ↓
DefaultMcpServer
        ↓
McpServerComponentFactory
        ↓
전체 MCP 통합 점검
```

상위 계층은 하위 기능을 추가할 때마다 반복 수정하지 않고, 필요한 MCP 하위 계층 구현이 완료된 시점에 일괄 수정하는 것을 원칙으로 한다.

---

## 설계 원칙

Prompts 계층은 다음 원칙을 따른다.

```text
1. Prompt 정의와 Prompt 실행 결과를 분리한다.

2. Prompt Provider는 하나의 Prompt 구현을 담당한다.

3. Registry는 Provider의 등록과 검색만 담당한다.

4. Service는 MCP 요청 처리와 공통 검증을 담당한다.

5. Prompt별 업무 검증은 Provider가 담당한다.

6. Content와 Role은 MCP 공통 Content 계층을 재사용한다.

7. Prompt 전용 Content 계층은 만들지 않는다.

8. Registry 내부 상태를 외부에 직접 노출하지 않는다.

9. MCP 상위 계층과 하위 기능 구현을 분리한다.

10. 다른 MCP 하위 계층 구현 완료 후 상위 계층을 일괄 연결한다.
```

이 구조를 통해 Prompts 계층은 Resources, Tools 및 향후 추가될 MCP 기능과 독립적으로 유지하면서 GomsBook AI Agent의 재사용 가능한 Prompt 기능을 제공한다.