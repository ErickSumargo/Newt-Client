package app.newt.id.helper;

import android.os.Environment;

import java.io.File;

/**
 * Created by Erick Sumargo on 2/27/2018.
 */

public class Constant {
    public static final String URL = "https://newt.mobi/";
    public static final String URL_MEDIA_IMAGE = "https://newt.mobi/media/image/";
    public static final String URL_MEDIA_DOCUMENT = "https://newt.mobi/media/document/";
    public static final String NODE_JS = "https://newt.mobi:3000";

    public static final String USER = "user";
    public static final String STUDENT = "student";
    public static final String TEACHER = "teacher";
    public static final String GUEST = "guest";
    public static final String AUTHORIZATION = "Authorization";
    public static final String JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6LTEsIm5hbWUiOiJndWVzdCJ9.8FpeHr_OuGCLmprbJfmJ92X7VmUQU2joXUKFHMlvO5s";

    public static final String APP_DIR = Environment.getExternalStorageDirectory().toString() + File.separator + "Newt";

    public static final String PREF_NAME = "NEWT";
    public static final String PREF_THEME = "NEWT.THEME";

    public static final String PREF_IS_FIRST_LOGIN = "NEWT.IS_FIRST_LOGIN";
    public static final String PREF_IS_LOGIN = "NEWT.IS_LOGIN";

    public static final String PREF_FIREBASE = "NEWT.FIREBASE";
    public static final String PREF_USER = "NEWT.USER";
    public static final String PREF_USER_TYPE = "NEWT.USER_TYPE";
    public static final String PREF_PHONE = "NEWT.PHONE";
    public static final String PREF_TOKEN = "NEWT.TOKEN";
    public static final String PREF_IS_PROFILE_PHOTO_CHANGED = "NEWT.IS_PROFILE_PHOTO_CHANGED";

    public static final String PREF_RECEIVER_CODE = "NEWT.RECEIVER_CODE";
    public static final String PREF_RECEIVER_TYPE = "NEWT.RECEIVER_TYPE";
    public static final String PREF_DIALOG_ID = "NEWT.DIALOG_ID";
    public static final String PREF_ENV_CHAT = "NEWT.ENV_CHAT";
    public static final String PREF_CHAT_SELECTED = "NEWT.CHAT_SELECTED";

    public static final String PREF_UNREAD_BADGE_SET = "NEWT.UNREAD_DIALOG_SET";
    public static final String PREF_UNREAD_BADGE_UPDATED = "PREF.UNREAD_BADGE_UPDATED";
    public static final String PREF_TOTAL_UNREAD_DIALOG = "NEWT.TOTAL_UNREAD_DIALOG";
    public static final String PREF_MATH_UNREAD_DIALOG = "NEWT.MATH_UNREAD_DIALOG";
    public static final String PREF_PHYS_UNREAD_DIALOG = "NEWT.PHYS_UNREAD_DIALOG";
    public static final String PREF_CHEM_UNREAD_DIALOG = "NEWT.CHEM_UNREAD_DIALOG";

    public static final String PREF_NOTIFICATION = "NEWT.NOTIFICATION";
    public static final String PREF_REPLY_SOUND = "NEWT.REPLAY_SOUND";
    public static final String PREF_ACTIVE_DIALOG = "NEWT.ACTIVE_DIALOG";
    public static final String PREF_CHATTING_PAUSED = "NEWT.CHATTING_PAUSED";

    public static final String PREF_APP_RATER_FIRST_LAUNCH = "NEWT.APP_RATER_FIRST_LAUNCH";
    public static final String PREF_APP_RATER_DATE_LAUNCH = "NEWT.APP_RATER_DATE_LAUNCH";
    public static final int PREF_APP_RATER_NEXT_PROMPT = 3;
    public static final String PREF_APP_SUBSCRIPTION_DATE_LAUNCH = "NEWT.APP_SUBSCRIPTION_DATE_LAUNCH";

    public static final String PREF_SERVER_TIME = "NEWT.SERVER_TIME";
    public static final String PREF_VERSION_CODE = "NEWT.VERSION_CODE";

    public static final String REQ_REG_TYPE = "reg_type";
    public static final String REQ_PHONE = "phone";
    public static final String REQ_DEVICE = "device";
    public static final String REQ_FIREBASE = "firebase";
    public static final String REQ_CODE = "code";
    public static final String REQ_RESEND_CODE = "resend_code";

    public static final String REQ_TEACHER_ID = "teacher_id";
    public static final String REQ_RATING = "rating";
    public static final String REQ_GET_RATING = "get_rating";
    public static final String REQ_SET_RATING = "set_rating";

    public static final String REQ_LESSON_ID = "lesson_id";
    public static final String REQ_UNREAD_COUNTER = "unread";

    public static final String REQ_NAME = "name";
    public static final String REQ_PASSWORD = "password";
    public static final String REQ_SCHOOL = "school";
    public static final String REQ_PROMO_CODE = "promo_code";
    public static final String REQ_PRO = "pro";
    public static final String REQ_PHOTO_CHANGED = "photo_changed";
    public static final String REQ_IMAGE = "image\"; filename=\"image";
    public static final String REQ_DOCUMENT = "document\"; filename=";

    public static final String REQ_UNIQUE_CODE = "unique_code";
    public static final String REQ_SENDER_CODE = "sender_code";
    public static final String REQ_RECEIVER_CODE = "receiver_code";
    public static final String REQ_CONTENT = "content";
    public static final String REQ_CONTENT_TYPE = "content_type";
    public static final String REQ_SENT = "sent";
    public static final String REQ_ONLINE_LIST = "online_list";

    public static final String REQ_SENDER_ID = "sender_id";
    public static final String REQ_SENDER_NAME = "sender_name";
    public static final String REQ_SENDER_PHONE = "sender_phone";
    public static final String REQ_SENDER_PHOTO = "sender_photo";
    public static final String REQ_SENDER_ACTIVE = "sender_active";
    public static final String REQ_SENDER_CREATED_AT = "sender_created_at";
    public static final String REQ_SENDER_PRO = "sender_pro";

    public static final String DIR_MEDIA_INTERNAL = "Media";
    public static final String DIR_PICTURES_INTERNAL = "Pictures";
    public static final String DIR_DOCUMENTS_INTERNAL = "Documents";

    public static final String DIR_DOWNLOAD_EXTERNAL = "Download";
    public static final String DIR_SHARE_EXTERNAL = "Share";

    public static final String REALM_BASE_DATA_LOADED = "REALM.BASE_DATA_LOADED";
    public static final String REALM_RESTART = "REALM_RESTART";

    public static final String SOCKET_IO = "socket.io";
    public static final String SOCKET_CONNECTED = "connect";
    public static final String CHATTING_EVENT = "event";
    public static final String CHATTING_JOIN_DIALOG = "join_dialog";
    public static final String CHATTING_LEAVE_DIALOG = "leave_dialog";
    public static final String CHATTING_MESSAGE = "message";
    public static final String CHATTING_MESSAGE_SENT = "message_sent";
    public static final String CHATTING_TYPING = "typing";
    public static final String CHATTING_STOP_TYPING = "stop_typing";
    public static final String CHATTING_IS_ACTIVE = "is_active";
    public static final String CHATTING_LOAD_READ_QUEUE = "load_read_queue";
    public static final String CHATTING_READ_QUEUE_LOADED = "read_queue_loaded";
    public static final String CHATTING_CLEAR_QUEUE = "clear_queue";
    public static final String CHATTING_QUEUE_CLEARED = "queue_cleared";
    public static final String CHATTING_CHECK_ONLINE = "check_online";

    public static final int MAX_HISTORIES = 20;
    public static final int MAX_NOTIF = 5;
    public static final int MAX_UPLOAD_FILE_SIZE = 2;

    public static final String TELKOMSEL = "Telkomsel";
    public static final String XL = "XL";
    public static final String THREE = "3";
    public static final String INDOSAT = "Indosat";

    public static final String BCA = "BCA";
    public static final String BNI = "BNI";

    public static final String INSTAGRAM = "instagram";
    public static final String WHATSAPP = "whatsapp";
    public static final String LINE = "line";
}