/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.protocol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.gomsbook.ai.mcp.core.McpResult;
import kr.co.goms.gomsbook.ai.mcp.core.McpResultType;

/**
 * MCP result indicating that additional client input is required.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * This result is used by the MCP Multi Round-Trip Requests
 * mechanism.
 * </p>
 *
 * <p>
 * The {@code resultType} of this result is always:
 * </p>
 *
 * <pre>
 * input_required
 * </pre>
 *
 * <p>
 * At least one of the following MUST be present:
 * </p>
 *
 * <ul>
 *     <li>{@code inputRequests}</li>
 *     <li>{@code requestState}</li>
 * </ul>
 */
public final class McpInputRequiredResult
        extends McpResult {

    /**
     * Server-assigned input request identifiers mapped to
     * embedded client requests.
     */
    private final Map<String, McpInputRequest> inputRequests;

    /**
     * Opaque state that must be echoed unchanged by the client
     * when retrying the original request.
     */
    private final String requestState;


    private McpInputRequiredResult(
            Builder builder
    ) {

        super(
                McpResultType.INPUT_REQUIRED,
                builder.serverInfo,
                builder.additionalMetadata
        );

        this.inputRequests =
                immutableInputRequests(
                        builder.inputRequests
                );

        this.requestState =
                normalizeOptional(
                        builder.requestState
                );

        validate();
    }


    /*
     * ------------------------------------------------------------
     * Factory
     * ------------------------------------------------------------
     */

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Creates an input-required result containing only
     * server-side request state.
     *
     * @param requestState opaque request state
     * @return input required result
     */
    public static McpInputRequiredResult ofState(
            String requestState
    ) {

        return builder()
                .requestState(
                        requestState
                )
                .build();
    }


    /**
     * Creates an input-required result containing a single
     * input request.
     *
     * @param id server-assigned input identifier
     * @param request embedded request
     * @return input required result
     */
    public static McpInputRequiredResult ofRequest(
            String id,
            McpInputRequest request
    ) {

        return builder()
                .inputRequest(
                        id,
                        request
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Accessors
     * ------------------------------------------------------------
     */

    public Map<String, McpInputRequest> getInputRequests() {
        return inputRequests;
    }

    public String getRequestState() {
        return requestState;
    }


    /*
     * ------------------------------------------------------------
     * Predicates
     * ------------------------------------------------------------
     */

    public boolean hasInputRequests() {
        return !inputRequests.isEmpty();
    }

    public boolean hasRequestState() {
        return requestState != null;
    }

    public boolean hasInputRequest(
            String id
    ) {

        if (id == null) {
            return false;
        }

        return inputRequests.containsKey(
                id
        );
    }

    public McpInputRequest getInputRequest(
            String id
    ) {

        if (id == null) {
            return null;
        }

        return inputRequests.get(
                id
        );
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private void validate() {

        /*
         * MCP 2026-07-28:
         *
         * Every InputRequiredResult MUST contain at least one of:
         *
         * - inputRequests
         * - requestState
         */
        if (inputRequests.isEmpty()
                && requestState == null) {

            throw new IllegalArgumentException(
                    "MCP input-required result must contain "
                            + "at least one of inputRequests "
                            + "or requestState."
            );
        }
    }


    private static Map<String, McpInputRequest> immutableInputRequests(
            Map<String, McpInputRequest> source
    ) {

        if (source == null
                || source.isEmpty()) {

            return Collections.emptyMap();
        }

        Map<String, McpInputRequest> copy =
                new LinkedHashMap<>();

        for (Map.Entry<String, McpInputRequest> entry
                : source.entrySet()) {

            String id =
                    requireInputRequestId(
                            entry.getKey()
                    );

            McpInputRequest request =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "MCP input request must not be null."
                    );

            if (copy.containsKey(id)) {

                throw new IllegalArgumentException(
                        "Duplicate MCP input request id: "
                                + id
                );
            }

            copy.put(
                    id,
                    request
            );
        }

        return Collections.unmodifiableMap(
                copy
        );
    }


    private static String requireInputRequestId(
            String id
    ) {

        Objects.requireNonNull(
                id,
                "MCP input request id must not be null."
        );

        String normalized =
                id.trim();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP input request id must not be blank."
            );
        }

        return normalized;
    }


    private static String normalizeOptional(
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


    /*
     * ------------------------------------------------------------
     * Builder
     * ------------------------------------------------------------
     */

    public static final class Builder {

        private final Map<String, McpInputRequest> inputRequests =
                new LinkedHashMap<>();

        private String requestState;

        private McpServerInfo serverInfo;

        private final Map<String, Object> additionalMetadata =
                new LinkedHashMap<>();


        private Builder() {
        }


        public Builder inputRequest(
                String id,
                McpInputRequest request
        ) {

            String normalizedId =
                    requireInputRequestId(
                            id
                    );

            Objects.requireNonNull(
                    request,
                    "MCP input request must not be null."
            );

            if (inputRequests.containsKey(
                    normalizedId
            )) {

                throw new IllegalArgumentException(
                        "Duplicate MCP input request id: "
                                + normalizedId
                );
            }

            inputRequests.put(
                    normalizedId,
                    request
            );

            return this;
        }


        public Builder inputRequests(
                Map<String, McpInputRequest> requests
        ) {

            if (requests == null
                    || requests.isEmpty()) {

                return this;
            }

            for (Map.Entry<String, McpInputRequest> entry
                    : requests.entrySet()) {

                inputRequest(
                        entry.getKey(),
                        entry.getValue()
                );
            }

            return this;
        }


        public Builder requestState(
                String requestState
        ) {

            this.requestState =
                    requestState;

            return this;
        }


        public Builder serverInfo(
                McpServerInfo serverInfo
        ) {

            this.serverInfo =
                    serverInfo;

            return this;
        }


        public Builder metadata(
                String key,
                Object value
        ) {

            Objects.requireNonNull(
                    key,
                    "MCP result metadata key must not be null."
            );

            String normalizedKey =
                    key.trim();

            if (normalizedKey.isEmpty()) {

                throw new IllegalArgumentException(
                        "MCP result metadata key must not be blank."
                );
            }

            if (McpResult.KEY_SERVER_INFO.equals(
                    normalizedKey
            )) {

                throw new IllegalArgumentException(
                        "Reserved MCP metadata key cannot be "
                                + "set through additional metadata: "
                                + normalizedKey
                );
            }

            if (value == null) {

                additionalMetadata.remove(
                        normalizedKey
                );

            } else {

                additionalMetadata.put(
                        normalizedKey,
                        value
                );
            }

            return this;
        }


        public McpInputRequiredResult build() {

            return new McpInputRequiredResult(
                    this
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public boolean equals(
            Object object
    ) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof McpInputRequiredResult)) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        McpInputRequiredResult other =
                (McpInputRequiredResult) object;

        return Objects.equals(
                inputRequests,
                other.inputRequests
        )
                && Objects.equals(
                        requestState,
                        other.requestState
                );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                super.hashCode(),
                inputRequests,
                requestState
        );
    }


    @Override
    public String toString() {

        return "McpInputRequiredResult{"
                + "resultType="
                + getResultType()
                + ", inputRequests="
                + inputRequests
                + ", requestState='"
                + requestState
                + '\''
                + '}';
    }
}