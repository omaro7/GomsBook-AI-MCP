/*
 * Copyright (c) 2026 GomsBook (JungHoon Han)
 * All rights reserved.
 */
package kr.co.goms.gomsbook.ai.mcp.protocol;

/**
 * MCP(Model Context Protocol) JSON-RPC method constants.
 *
 * <p>
 * Defines MCP protocol methods supported by
 * GomsBook AI MCP.
 * </p>
 *
 * <p>
 * This class targets MCP protocol version 2026-07-28.
 * </p>
 */
public final class McpMethod {

    /*
     * Server discovery.
     */

    public static final String SERVER_DISCOVER =
            "server/discover";


    /*
     * Tool methods.
     */

    public static final String TOOLS_LIST =
            "tools/list";

    public static final String TOOLS_CALL =
            "tools/call";


    /*
     * Resource methods.
     */

    public static final String RESOURCES_LIST =
            "resources/list";

    public static final String RESOURCES_READ =
            "resources/read";

    public static final String RESOURCES_TEMPLATES_LIST =
            "resources/templates/list";


    /*
     * Prompt methods.
     */

    public static final String PROMPTS_LIST =
            "prompts/list";

    public static final String PROMPTS_GET =
            "prompts/get";


    /*
     * Completion methods.
     */

    public static final String COMPLETION_COMPLETE =
            "completion/complete";


    /*
     * Subscription methods.
     *
     * Introduced in MCP 2026-07-28 as the unified
     * notification subscription mechanism.
     */

    public static final String SUBSCRIPTIONS_LISTEN =
            "subscriptions/listen";


    /*
     * Notifications.
     */

    public static final String NOTIFICATION_CANCELLED =
            "notifications/cancelled";

    /**
     * Progress notification.
     *
     * <p>
     * In MCP 2026-07-28 this notification is
     * server-to-client only.
     * </p>
     */
    public static final String NOTIFICATION_PROGRESS =
            "notifications/progress";

    public static final String NOTIFICATION_TOOLS_LIST_CHANGED =
            "notifications/tools/list_changed";

    public static final String NOTIFICATION_RESOURCES_LIST_CHANGED =
            "notifications/resources/list_changed";

    public static final String NOTIFICATION_RESOURCES_UPDATED =
            "notifications/resources/updated";

    public static final String NOTIFICATION_PROMPTS_LIST_CHANGED =
            "notifications/prompts/list_changed";

    public static final String NOTIFICATION_MESSAGE =
            "notifications/message";


    private McpMethod() {

        throw new AssertionError(
                "McpMethod must not be instantiated."
        );
    }


    /**
     * Checks whether the given method is the
     * server discovery method.
     *
     * @param method MCP method
     * @return {@code true} when server/discover
     */
    public static boolean isDiscoveryMethod(
            String method) {

        return SERVER_DISCOVER.equals(
                method
        );
    }


    /**
     * Checks whether the given method belongs
     * to the tool namespace.
     *
     * @param method MCP method
     * @return {@code true} for tool methods
     */
    public static boolean isToolMethod(
            String method) {

        return TOOLS_LIST.equals(method)
                || TOOLS_CALL.equals(method);
    }


    /**
     * Checks whether the given method belongs
     * to the resource namespace.
     *
     * @param method MCP method
     * @return {@code true} for resource methods
     */
    public static boolean isResourceMethod(
            String method) {

        return RESOURCES_LIST.equals(method)
                || RESOURCES_READ.equals(method)
                || RESOURCES_TEMPLATES_LIST.equals(method);
    }


    /**
     * Checks whether the given method belongs
     * to the prompt namespace.
     *
     * @param method MCP method
     * @return {@code true} for prompt methods
     */
    public static boolean isPromptMethod(
            String method) {

        return PROMPTS_LIST.equals(method)
                || PROMPTS_GET.equals(method);
    }


    /**
     * Checks whether the given method belongs
     * to the completion namespace.
     */
    public static boolean isCompletionMethod(
            String method) {

        return COMPLETION_COMPLETE.equals(
                method
        );
    }


    /**
     * Checks whether the given method belongs
     * to the subscription namespace.
     */
    public static boolean isSubscriptionMethod(
            String method) {

        return SUBSCRIPTIONS_LISTEN.equals(
                method
        );
    }


    /**
     * Checks whether the method represents
     * an MCP notification.
     *
     * @param method MCP method
     * @return {@code true} when notification
     */
    public static boolean isNotification(
            String method) {

        return method != null
                && method.startsWith(
                        "notifications/"
                );
    }


    /**
     * Checks whether a notification may be
     * sent from the server to the client.
     */
    public static boolean isServerNotification(
            String method) {

        return NOTIFICATION_PROGRESS.equals(method)
                || NOTIFICATION_TOOLS_LIST_CHANGED.equals(method)
                || NOTIFICATION_RESOURCES_LIST_CHANGED.equals(method)
                || NOTIFICATION_RESOURCES_UPDATED.equals(method)
                || NOTIFICATION_PROMPTS_LIST_CHANGED.equals(method)
                || NOTIFICATION_MESSAGE.equals(method);
    }


    /**
     * Checks whether a notification may be
     * sent from the client to the server.
     */
    public static boolean isClientNotification(
            String method) {

        return NOTIFICATION_CANCELLED.equals(
                method
        );
    }
}