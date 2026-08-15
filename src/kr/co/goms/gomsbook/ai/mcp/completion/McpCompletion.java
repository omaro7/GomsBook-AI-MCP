/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * MCP completion result payload.
 *
 * <p>
 * Contains candidate values for an argument completion request.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * {
 *   "values": [
 *     "python",
 *     "pytorch"
 *   ],
 *   "total": 12,
 *   "hasMore": true
 * }
 * </pre>
 */
public final class McpCompletion {

    /**
     * Maximum number of completion values allowed
     * by the MCP specification.
     */
    public static final int MAX_VALUES = 100;

    private final List<String> values;

    private final Integer total;

    private final Boolean hasMore;

    /**
     * Creates a completion containing only candidate values.
     *
     * @param values completion candidate values
     */
    public McpCompletion(
            List<String> values) {

        this(
                values,
                null,
                null
        );
    }

    /**
     * Creates a completion.
     *
     * @param values  completion candidate values
     * @param total   optional total number of matching candidates
     * @param hasMore optional indication that more candidates exist
     */
    public McpCompletion(
            List<String> values,
            Integer total,
            Boolean hasMore) {

        Objects.requireNonNull(
                values,
                "values must not be null."
        );

        if (values.size() > MAX_VALUES) {

            throw new IllegalArgumentException(
                    "values must not contain more than " +
                            MAX_VALUES +
                            " items."
            );
        }

        List<String> normalizedValues =
                new ArrayList<>(
                        values.size()
                );

        for (String value : values) {

            normalizedValues.add(
                    Objects.requireNonNull(
                            value,
                            "completion value must not be null."
                    )
            );
        }

        if (total != null
                && total < 0) {

            throw new IllegalArgumentException(
                    "total must not be negative."
            );
        }

        this.values =
                Collections.unmodifiableList(
                        normalizedValues
                );

        this.total =
                total;

        this.hasMore =
                hasMore;
    }

    /**
     * Returns completion candidate values.
     *
     * @return immutable candidate list
     */
    public List<String> getValues() {

        return values;
    }

    /**
     * Returns the total number of matching candidates.
     *
     * <p>
     * This value may be greater than {@link #size()}
     * when the result has been truncated.
     * </p>
     *
     * @return total number of candidates, or {@code null}
     */
    public Integer getTotal() {

        return total;
    }

    /**
     * Returns whether additional completion candidates exist.
     *
     * @return {@code true}, {@code false}, or {@code null}
     */
    public Boolean getHasMore() {

        return hasMore;
    }

    /**
     * Returns whether this completion contains no candidate values.
     *
     * @return {@code true} if no candidates exist
     */
    public boolean isEmpty() {

        return values.isEmpty();
    }

    /**
     * Returns the number of completion values
     * contained in this result.
     *
     * @return candidate count
     */
    public int size() {

        return values.size();
    }

    @Override
    public String toString() {

        return "McpCompletion{" +
                "values=" + values +
                ", total=" + total +
                ", hasMore=" + hasMore +
                '}';
    }
}