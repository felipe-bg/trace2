package com.iteso.trace.utils;

public class Constants {
    // For login intent request code
    public static final int RC_SIGN_IN = 9999;
    // Database node names
    public static final String DB_USERS = "users";
    public static final String DB_MESSAGES = "messages";
    public static final String DB_MEMBERS = "members";
    public static final String DB_CHANNELS = "channels";
    public static final String DB_CHATS = "chats";
    public static final String DB_EMAILS = "emails";
    public static final String DB_TAGS = "tags";
    // Database log tag
    public static final String RTDB_LOG = "RTDB_LOG";
    // Intent extras IDs
    public static final String CONVERSATION_ID = "conversationId";
    public static final String USER_ID = "userId";
    // Default user id
    public static final String DEFAULT_USER_UID = "0";
    public static final String DEFAULT_USER_AVATAR = "https://s3.amazonaws.com/cc-698969-mobile/avatars/cat.png";
    public static final String DEFAULT_TAG = "random";
    // NavigationDrawer menu group ids
    public static final int CHANNELS_GROUP = 0;
    public static final int CHATS_GROUP = 1;
    // Error message
    public static final String EMAIL_ERROR = "There was a problem with your input or the database.";
    // Current conversation user property
    public static final String CURRENT_CONVERSATION = "currentConversation";

}
