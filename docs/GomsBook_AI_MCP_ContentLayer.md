# MCP Content Layer

GomsBook AI Agent의 MCP Content 계층은 **Model Context Protocol(MCP)** 에서 교환되는 Content Block을 Java 모델로 표현하기 위한 공통 계층입니다.

현재 구현은 MCP `2026-07-28` 명세의 Content Block 구조를 기준으로 설계합니다.

---

## 1. 목적

Content 계층은 MCP Client와 Server 사이에서 전달되는 다양한 형태의 콘텐츠를 하나의 공통 인터페이스로 처리하기 위해 사용합니다.

지원하는 Content Block은 다음과 같습니다.

| MCP Type        | Java Model                   | 설명                  |
| --------------- | ---------------------------- | ------------------- |
| `text`          | `McpTextContent`             | 텍스트 콘텐츠             |
| `image`         | `McpImageContent`            | Base64 이미지 콘텐츠      |
| `audio`         | `McpAudioContent`            | Base64 오디오 콘텐츠      |
| `resource_link` | `McpResourceLinkContent`     | MCP Resource에 대한 링크 |
| `resource`      | `McpEmbeddedResourceContent` | 응답 내부에 포함된 Resource |

모든 Content 구현체는 `McpContent` 인터페이스를 구현합니다.

---

# 2. 패키지 구조

```text
kr.co.goms.gomsbook.ai.mcp
│
├─ common
│  ├─ McpRole.java
│  ├─ McpAnnotations.java
│  └─ McpIcon.java
│
└─ content
   ├─ McpContent.java
   ├─ McpContentType.java
   ├─ McpTextContent.java
   ├─ McpImageContent.java
   ├─ McpAudioContent.java
   ├─ McpResourceLinkContent.java
   └─ McpEmbeddedResourceContent.java
```

Gson 다형성 처리를 위해 별도로 다음 Factory를 사용합니다.

```text
McpContentTypeAdapterFactory.java
```

`GsonMcpJsonCodec`에서 이 Factory를 등록하여 사용합니다.

---

# 3. 전체 구조

```text
                         McpContent
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
 McpTextContent       McpImageContent      McpAudioContent
        │
        ├─────────────────────────────────────────┐
        │                                         │
McpResourceLinkContent                 McpEmbeddedResourceContent
```

공통 타입은 다음과 같습니다.

```text
McpContent
 ├─ McpContentType
 ├─ McpAnnotations
 │   └─ McpRole
 └─ _meta

McpResourceLinkContent
 └─ McpIcon
```

---

# 4. McpContent

`McpContent`는 모든 MCP Content Block의 최상위 인터페이스입니다.

주요 계약은 다음과 같습니다.

```java
public interface McpContent {

    McpContentType getType();

    McpAnnotations getAnnotations();

    Map<String, Object> getMeta();
}
```

공통 편의 메서드도 제공합니다.

```java
hasAnnotations()
hasMeta()

isText()
isImage()
isAudio()
isResourceLink()
isResource()
```

따라서 호출자는 구현 클래스를 직접 검사하기 전에 Content Type을 쉽게 확인할 수 있습니다.

예:

```java
if (content.isText()) {

    McpTextContent textContent =
            (McpTextContent) content;
}
```

---

# 5. McpContentType

`McpContentType`은 MCP Content의 `type` discriminator를 표현합니다.

```text
TEXT          -> "text"
IMAGE         -> "image"
AUDIO         -> "audio"
RESOURCE_LINK -> "resource_link"
RESOURCE      -> "resource"
```

Gson 직렬화/역직렬화 정합성을 위해 `@SerializedName`을 사용합니다.

예:

```java
@SerializedName("text")
TEXT("text")
```

따라서 Java enum 이름과 MCP wire format을 분리해서 관리할 수 있습니다.

---

# 6. McpTextContent

텍스트 콘텐츠를 표현합니다.

구조:

```json
{
  "type": "text",
  "text": "Hello MCP"
}
```

Annotations와 `_meta`를 포함할 수도 있습니다.

```json
{
  "type": "text",
  "text": "Hello MCP",
  "annotations": {
    "audience": [
      "user"
    ],
    "priority": 0.8
  },
  "_meta": {
    "kr.co.goms.gomsbook/source": "editor"
  }
}
```

생성 예:

```java
McpTextContent content =
        McpTextContent.builder()
                .text(
                        "Hello MCP"
                )
                .build();
```

---

# 7. McpImageContent

Base64로 인코딩된 이미지 데이터를 표현합니다.

주요 필드:

```text
type
data
mimeType
annotations
_meta
```

예:

```json
{
  "type": "image",
  "data": "iVBORw0KGgo...",
  "mimeType": "image/png"
}
```

생성 예:

```java
McpImageContent content =
        McpImageContent.builder()
                .data(
                        base64Image
                )
                .mimeType(
                        "image/png"
                )
                .build();
```

현재 구현에서는 MIME type이 `image/`로 시작하는지 검증합니다.

---

# 8. McpAudioContent

Base64로 인코딩된 오디오 데이터를 표현합니다.

주요 필드:

```text
type
data
mimeType
annotations
_meta
```

예:

```json
{
  "type": "audio",
  "data": "UklGRiQAAABXQVZF...",
  "mimeType": "audio/wav"
}
```

생성 예:

```java
McpAudioContent content =
        McpAudioContent.builder()
                .data(
                        base64Audio
                )
                .mimeType(
                        "audio/wav"
                )
                .build();
```

현재 구현에서는 MIME type이 `audio/`로 시작하는지 검증합니다.

---

# 9. McpResourceLinkContent

MCP Resource에 대한 링크를 Content Block으로 전달할 때 사용합니다.

주요 필드:

```text
type
icons
name
title
uri
description
mimeType
annotations
size
_meta
```

예:

```json
{
  "type": "resource_link",
  "name": "chapter01.xhtml",
  "title": "Chapter 01",
  "uri": "file:///project/OEBPS/Text/chapter01.xhtml",
  "mimeType": "application/xhtml+xml",
  "size": 12540
}
```

아이콘도 지정할 수 있습니다.

```java
McpResourceLinkContent content =
        McpResourceLinkContent.builder()
                .name(
                        "chapter01.xhtml"
                )
                .title(
                        "Chapter 01"
                )
                .uri(
                        "file:///project/OEBPS/Text/chapter01.xhtml"
                )
                .mimeType(
                        "application/xhtml+xml"
                )
                .size(
                        12540L
                )
                .addIcon(
                        McpIcon.builder()
                                .src(
                                        "https://example.com/xhtml.png"
                                )
                                .mimeType(
                                        "image/png"
                                )
                                .addSize(
                                        "48x48"
                                )
                                .build()
                )
                .build();
```

`size`는 Resource 원본 데이터의 크기를 나타냅니다.

---

# 10. McpEmbeddedResourceContent

Resource 데이터를 Content 내부에 직접 포함할 때 사용합니다.

구조:

```json
{
  "type": "resource",
  "resource": {
    "uri": "file:///project/chapter01.xhtml",
    "mimeType": "application/xhtml+xml",
    "text": "<html>...</html>"
  }
}
```

Java 모델에서는 기존 Resources 계층의:

```java
McpResourceContents
```

를 사용합니다.

```java
McpEmbeddedResourceContent content =
        McpEmbeddedResourceContent.builder()
                .resource(
                        resourceContents
                )
                .build();
```

## ResourceContents 관련 확인사항

MCP `2026-07-28` 구조에서 Embedded Resource의 `resource`는 다음 union 구조입니다.

```text
ResourceContents
 ├─ TextResourceContents
 └─ BlobResourceContents
```

따라서 Resources 계층 재점검 시 현재 `McpResourceContents` 구현이 다음 구조를 정확히 표현하는지 확인해야 합니다.

```text
McpResourceContents
 ├─ McpTextResourceContents
 └─ McpBlobResourceContents
```

이 항목은 Content 계층 자체의 구현을 막지는 않으며 Resources 계층 점검 시 확인합니다.

---

# 11. McpRole

`McpRole`은 MCP에서 사용하는 공통 Role을 표현합니다.

```text
USER      -> "user"
ASSISTANT -> "assistant"
```

Content 전용 타입이 아니므로 `content` 패키지가 아닌:

```text
mcp.common
```

에 위치합니다.

Annotations뿐 아니라 향후 Prompt Message 등에서도 재사용할 수 있습니다.

---

# 12. McpAnnotations

MCP 공통 Annotation 정보를 표현합니다.

필드:

```text
audience
priority
lastModified
```

예:

```java
McpAnnotations annotations =
        McpAnnotations.builder()
                .addAudience(
                        McpRole.USER
                )
                .priority(
                        0.8
                )
                .build();
```

JSON:

```json
{
  "audience": [
    "user"
  ],
  "priority": 0.8
}
```

`priority`는 `0.0 ~ 1.0` 범위로 검증합니다.

빈 Annotations 객체는 Content 생성 시 `null`로 정규화하여 선택 필드가 불필요하게 wire format에 출력되지 않도록 합니다.

---

# 13. McpIcon

MCP UI에서 Resource 등을 표현할 때 사용할 아이콘 정보입니다.

주요 필드:

```text
src
mimeType
sizes
theme
```

Theme:

```text
LIGHT -> "light"
DARK  -> "dark"
```

예:

```json
{
  "src": "https://example.com/icon.png",
  "mimeType": "image/png",
  "sizes": [
    "48x48",
    "96x96"
  ],
  "theme": "light"
}
```

---

# 14. `_meta`

MCP의 확장 metadata는 `_meta` 필드로 전달합니다.

Java에서는 다음 타입으로 표현합니다.

```java
@SerializedName("_meta")
private final Map<String, Object> meta;
```

별도 Wrapper 객체를 만들지 않는 이유는 MCP의 `_meta` 자체가 key-value 형태의 확장 객체이기 때문입니다.

예:

```java
McpTextContent content =
        McpTextContent.builder()
                .text(
                        "Chapter content"
                )
                .putMeta(
                        "kr.co.goms.gomsbook/contentId",
                        "chapter01"
                )
                .putMeta(
                        "kr.co.goms.gomsbook/source",
                        "editor"
                )
                .build();
```

결과:

```json
{
  "type": "text",
  "text": "Chapter content",
  "_meta": {
    "kr.co.goms.gomsbook/contentId": "chapter01",
    "kr.co.goms.gomsbook/source": "editor"
  }
}
```

빈 `_meta`는 `null`로 정규화하여 Gson wire format에서 생략합니다.

GomsBook 전용 metadata는 충돌 방지를 위해 reverse-DNS 기반 namespace 사용을 권장합니다.

```text
kr.co.goms.gomsbook/...
```

---

# 15. Gson 다형성 처리

`McpContent`는 interface이므로 Gson 기본 역직렬화만으로는 실제 구현 클래스를 결정할 수 없습니다.

이를 위해:

```text
McpContentTypeAdapterFactory
```

를 사용합니다.

`type` discriminator에 따라 다음과 같이 변환합니다.

```text
"text"
   ↓
McpTextContent

"image"
   ↓
McpImageContent

"audio"
   ↓
McpAudioContent

"resource_link"
   ↓
McpResourceLinkContent

"resource"
   ↓
McpEmbeddedResourceContent
```

---

# 16. Gson 등록

`GsonMcpJsonCodec`의 `GsonBuilder`에 Factory를 등록합니다.

```java
return new GsonBuilder()
        .disableHtmlEscaping()
        .registerTypeAdapterFactory(
                new McpContentTypeAdapterFactory()
        )
        .create();
```

이 등록을 통해 다음과 같은 역직렬화가 가능합니다.

```java
McpContent content =
        codec.fromJson(
                json,
                McpContent.class
        );
```

---

# 17. Builder 기반 역직렬화 검증

`McpContentTypeAdapterFactory`는 단순 reflective deserialization에만 의존하지 않고 Content Builder를 이용하여 객체를 생성합니다.

처리 흐름:

```text
JSON
 ↓
type 확인
 ↓
필수 필드 확인
 ↓
필드 JSON 타입 확인
 ↓1
Content Builder
 ↓
각 Content validation
 ↓
build()
 ↓
McpContent
```

이 방식으로 Java 코드에서 객체를 생성할 때와 JSON에서 역직렬화할 때 동일한 validation 정책을 적용합니다.

예를 들어 다음 Image Content는 거부됩니다.

```json
{
  "type": "image",
  "data": "AAAA",
  "mimeType": "audio/wav"
}
```

`McpImageContent`가 `image/*` MIME type을 요구하기 때문입니다.

다음 Resource Link 역시 거부됩니다.

```json
{
  "type": "resource_link",
  "name": "chapter.xhtml",
  "uri": "file:///chapter.xhtml",
  "size": -1
}
```

Resource size는 음수가 될 수 없습니다.

---

# 18. List\<McpContent> 처리

Content 목록은 Gson `TypeToken`을 이용하여 처리할 수 있습니다.

```java
Type type =
        new TypeToken<List<McpContent>>() {
        }.getType();

List<McpContent> contents =
        codec.fromJson(
                json,
                type
        );
```

예:

```json
[
  {
    "type": "text",
    "text": "Hello"
  },
  {
    "type": "image",
    "data": "AAAA",
    "mimeType": "image/png"
  }
]
```

역직렬화 결과:

```text
List<McpContent>
 ├─ McpTextContent
 └─ McpImageContent
```

---

# 19. 설계 원칙

Content 계층은 다음 원칙을 따릅니다.

### Protocol First

MCP wire format과 Java 모델의 구조를 가능한 한 직접 대응시킵니다.

### Immutable Model

Content 객체는 생성 이후 상태가 변경되지 않도록 설계합니다.

Collection과 Map은 defensive copy 후 immutable collection으로 보관합니다.

### Builder Validation

필수값과 타입별 제약사항은 Builder 생성 과정에서 검증합니다.

### Gson Compatibility

MCP JSON 필드명과 Java 필드명이 다른 경우 `@SerializedName`을 사용합니다.

대표적으로:

```java
@SerializedName("_meta")
```

를 사용합니다.

### Common Type Reuse

Content 전용이 아닌 MCP 공통 개념은 `mcp.common`에 위치시킵니다.

```text
McpRole
McpAnnotations
McpIcon
```

이를 통해 향후 Resources, Prompts 등 다른 MCP 계층에서도 동일한 모델을 재사용합니다.

---

# 20. 현재 구현 상태

```text
MCP Content Layer
│
├─ Common
│  ├─ McpRole                       완료
│  ├─ McpAnnotations                완료
│  └─ McpIcon                       완료
│
├─ Content
│  ├─ McpContent                    완료
│  ├─ McpContentType                완료
│  ├─ McpTextContent                완료
│  ├─ McpImageContent               완료
│  ├─ McpAudioContent               완료
│  ├─ McpResourceLinkContent        완료
│  └─ McpEmbeddedResourceContent    완료
│
└─ Gson
   └─ McpContentTypeAdapterFactory  완료
```

---

# 21. 추후 점검사항

Content 계층 자체는 구현 완료 상태로 관리합니다.

다만 다음 항목은 Resources 계층 점검 시 확인합니다.

```text
McpResourceContents
```

확인 대상:

```text
TextResourceContents
BlobResourceContents
```

권장 Java 구조:

```text
McpResourceContents
 ├─ McpTextResourceContents
 └─ McpBlobResourceContents
```

이 구조가 적용될 경우 `McpContentTypeAdapterFactory`의 Embedded Resource 역직렬화 부분도 Resources 다형성 모델에 맞춰 함께 조정합니다.

또한 `_meta` 사용 범위가 Resources, Tools, Prompts 등으로 확대되면 metadata 정규화 및 key 검증 로직을 `mcp.common` 공통 유틸리티로 승격하는 것을 검토합니다.

---

# 22. 완료 기준

Content 계층의 완료 기준은 다음과 같습니다.

```text
[✓] MCP ContentBlock 5종 지원
[✓] Content type discriminator 지원
[✓] Text Content 지원
[✓] Image Content 지원
[✓] Audio Content 지원
[✓] Resource Link 지원
[✓] Embedded Resource 지원
[✓] MCP Annotations 지원
[✓] MCP Role 공통화
[✓] MCP Icon 지원
[✓] _meta 지원
[✓] Gson polymorphic serialization 지원
[✓] Gson polymorphic deserialization 지원
[✓] Builder validation 적용
[✓] List<McpContent> 처리 지원
[ ] ResourceContents union 최종 확인
```

마지막 `ResourceContents union`은 Resources 계층의 책임 범위이므로, 현 단계에서 **Content 계층은 구현 완료**로 간주합니다.
