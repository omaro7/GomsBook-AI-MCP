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
 * Default implementation of {@link McpCompletionService}.
 *
 * <p>
 * Protocol target:
 * {@code 2026-07-28}
 * </p>
 *
 * <p>
 * Responsibilities:
 * </p>
 *
 * <ul>
 *     <li>resolve the appropriate completion provider</li>
 *     <li>generate completion candidates</li>
 *     <li>enforce MCP completion limits</li>
 *     <li>return {@link McpCompleteResult}</li>
 * </ul>
 */
public final class DefaultMcpCompletionService
        implements McpCompletionService {

    /**
     * MCP completion result maximum.
     */
    public static final int MAX_COMPLETION_VALUES =
            100;


    private final McpCompletionRegistry completionRegistry;


    /*
     * ------------------------------------------------------------
     * Constructor
     * ------------------------------------------------------------
     */

    public DefaultMcpCompletionService(
            McpCompletionRegistry completionRegistry
    ) {

        this.completionRegistry =
                Objects.requireNonNull(
                        completionRegistry,
                        "MCP completion registry must not be null."
                );
    }


    /*
     * ------------------------------------------------------------
     * McpCompletionService
     * ------------------------------------------------------------
     */

    @Override
    public McpCompleteResult complete(
            McpCompleteParams params
    ) {

        validateParams(
                params
        );


        McpCompletionReference reference =
                params.getRef();


        McpCompletionProvider provider =
                completionRegistry.findProvider(
                        params
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No MCP completion provider supports "
                                        + "the requested completion."
                        )
                );


        if (provider == null) {

            throw new McpCompletionNotFoundException(
                    reference.toString()
            );
        }


        McpCompletion completion =
                provider.complete(
                        params
                );


        if (completion == null) {

            throw new IllegalStateException(
                    "MCP completion provider returned null."
            );
        }


        McpCompletion normalizedCompletion =
                normalizeCompletion(
                        completion
                );


        return McpCompleteResult.builder()
                .completion(
                        normalizedCompletion
                )
                .build();
    }


    /*
     * ------------------------------------------------------------
     * Validation
     * ------------------------------------------------------------
     */

    private static void validateParams(
            McpCompleteParams params
    ) {

        if (params == null) {

            throw new IllegalArgumentException(
                    "MCP completion/complete params must not be null."
            );
        }


        if (params.getRef() == null) {

            throw new IllegalArgumentException(
                    "MCP completion/complete requires ref."
            );
        }


        if (params.getArgument() == null) {

            throw new IllegalArgumentException(
                    "MCP completion/complete requires argument."
            );
        }


        validateArgument(
                params.getArgument()
        );
    }


    private static void validateArgument(
            McpCompletionArgument argument
    ) {

        String name =
                argument.getName();


        if (name == null
                || name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "MCP completion argument name must not be blank."
            );
        }


        if (argument.getValue() == null) {

            throw new IllegalArgumentException(
                    "MCP completion argument value must not be null."
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Completion normalization
     * ------------------------------------------------------------
     */

    private static McpCompletion normalizeCompletion(
            McpCompletion completion
    ) {

        List<String> sourceValues =
                completion.getValues();


        if (sourceValues == null
                || sourceValues.isEmpty()) {

        	return new McpCompletion(
        	        Collections.emptyList(),
        	        normalizeTotal(
        	                completion,
        	                0
        	        ),
        	        normalizeHasMore(
        	                completion,
        	                0,
        	                0
        	        )
        	);
        }


        List<String> normalizedValues =
                normalizeValues(
                        sourceValues
                );


        int availableCount =
                normalizedValues.size();


        List<String> limitedValues;


        if (availableCount > MAX_COMPLETION_VALUES) {

            limitedValues =
                    new ArrayList<>(
                            normalizedValues.subList(
                                    0,
                                    MAX_COMPLETION_VALUES
                            )
                    );

        } else {

            limitedValues =
                    normalizedValues;
        }


        Integer total =
                normalizeTotal(
                        completion,
                        availableCount
                );


        Boolean hasMore =
                normalizeHasMore(
                        completion,
                        availableCount,
                        limitedValues.size()
                );


        return new McpCompletion(
                limitedValues,
                total,
                hasMore
        );
    }


    private static List<String> normalizeValues(
            List<String> values
    ) {

        if (values == null
                || values.isEmpty()) {

            return Collections.emptyList();
        }


        List<String> normalized =
                new ArrayList<>();


        for (String value : values) {

            if (value == null) {
                continue;
            }


            /*
             * Do not trim the completion value itself.
             *
             * Completion candidates may intentionally contain
             * leading or trailing whitespace.
             */
            normalized.add(
                    value
            );
        }


        if (normalized.isEmpty()) {

            return Collections.emptyList();
        }


        return normalized;
    }


    /*
     * ------------------------------------------------------------
     * total / hasMore
     * ------------------------------------------------------------
     */

    private static Integer normalizeTotal(
            McpCompletion completion,
            int availableCount
    ) {

        Integer total =
                completion.getTotal();


        if (total == null) {

            return availableCount;
        }


        if (total < 0) {

            throw new IllegalArgumentException(
                    "MCP completion total must not be negative."
            );
        }


        /*
         * total cannot be smaller than the number of candidates
         * already produced by the provider.
         */
        if (total < availableCount) {

            throw new IllegalArgumentException(
                    "MCP completion total must not be smaller "
                            + "than the number of available values."
            );
        }


        return total;
    }


    private static Boolean normalizeHasMore(
            McpCompletion completion,
            int availableCount,
            int returnedCount
    ) {

        Boolean providerHasMore =
                completion.getHasMore();


        /*
         * Local truncation itself proves that more values exist.
         */
        if (availableCount > returnedCount) {

            return Boolean.TRUE;
        }


        return providerHasMore;
    }


    /*
     * ------------------------------------------------------------
     * Accessor
     * ------------------------------------------------------------
     */

    public McpCompletionRegistry getCompletionRegistry() {
        return completionRegistry;
    }


    /*
     * ------------------------------------------------------------
     * Object
     * ------------------------------------------------------------
     */

    @Override
    public String toString() {

        return "DefaultMcpCompletionService{"
                + "completionRegistry="
                + completionRegistry
                + '}';
    }
}