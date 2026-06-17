package com.oa.common;

/**
 * 系统常量
 */
public final class Constants {

    private Constants() {}

    // ===== 用户状态 =====
    public static final int USER_STATUS_NORMAL = 1;
    public static final int USER_STATUS_DISABLED = 0;

    // ===== 审批状态 =====
    public static final String APPROVAL_STATUS_DRAFT = "DRAFT";
    public static final String APPROVAL_STATUS_PENDING = "PENDING";
    public static final String APPROVAL_STATUS_APPROVING = "APPROVING";
    public static final String APPROVAL_STATUS_PASSED = "PASSED";
    public static final String APPROVAL_STATUS_REJECTED = "REJECTED";
    public static final String APPROVAL_STATUS_CANCELLED = "CANCELLED";

    // ===== 审批节点类型 =====
    public static final String NODE_TYPE_APPROVE = "APPROVE";
    public static final String NODE_TYPE_CC = "CC";
    public static final String NODE_TYPE_SIGN = "SIGN";
    public static final String NODE_TYPE_OR_SIGN = "OR_SIGN";
    public static final String NODE_TYPE_CONDITION = "CONDITION";

    // ===== 审批操作 =====
    public static final String ACTION_APPROVE = "APPROVE";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_TRANSFER = "TRANSFER";

    // ===== 会议状态 =====
    public static final String MEETING_SCHEDULED = "SCHEDULED";
    public static final String MEETING_CANCELLED = "CANCELLED";
    public static final String MEETING_FINISHED = "FINISHED";

    // ===== 资产状态 =====
    public static final String ASSET_IDLE = "IDLE";
    public static final String ASSET_IN_USE = "IN_USE";
    public static final String ASSET_SCRAPPED = "SCRAPPED";

    // ===== 会话类型(IM) =====
    public static final String CONVERSATION_PRIVATE = "PRIVATE";
    public static final String CONVERSATION_GROUP = "GROUP";

    // ===== 当前登录用户（线程绑定） =====
    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();

    public static void setCurrentUser(Long userId, String username) {
        CURRENT_USER.set(userId);
        CURRENT_USERNAME.set(username);
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER.get();
    }

    public static String getCurrentUsername() {
        return CURRENT_USERNAME.get();
    }

    public static void clearCurrentUser() {
        CURRENT_USER.remove();
        CURRENT_USERNAME.remove();
    }
}
