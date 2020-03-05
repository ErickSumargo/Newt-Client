package app.newt.id.helper;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import app.newt.id.BuildConfig;
import app.newt.id.background.service.ChattingService;
import app.newt.id.background.service.CheckOnlineService;
import app.newt.id.background.service.FetchProfilesService;
import app.newt.id.background.service.FetchTeachersService;
import app.newt.id.background.service.StoreChatService;
import app.newt.id.server.model.User;
import app.newt.id.view.activity.IntroActivity;
import app.newt.id.view.activity.LoginActivity;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by Erick Sumargo on 2/27/2018.
 */

public class Session {
    private Context context;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Session(Context context) {
        this.context = context;

        pref = context.getSharedPreferences(Constant.PREF_NAME, 0);
        editor = pref.edit();
    }

    public static Session with(Context context) {
        return new Session(context);
    }

    public void saveFirebaseToken(String token) {
        editor.putString(Constant.PREF_FIREBASE, token);
        editor.commit();
    }

    public String getFirebaseToken() {
        return pref.getString(Constant.PREF_FIREBASE, "");
    }

    public void saveFirstLogin() {
        editor.putBoolean(Constant.PREF_IS_FIRST_LOGIN, true);
        editor.commit();
    }

    public boolean isFirstLogin() {
        return pref.getBoolean(Constant.PREF_IS_FIRST_LOGIN, false);
    }

    public void saveChatSelected(boolean selected) {
        editor.putBoolean(Constant.PREF_CHAT_SELECTED, selected);
        editor.commit();
    }

    public boolean isChatSelected() {
        return pref.getBoolean(Constant.PREF_CHAT_SELECTED, false);
    }

    public void saveLogin() {
        editor.putBoolean(Constant.PREF_IS_LOGIN, true);
        editor.commit();
    }

    public boolean isLogin() {
        return pref.getBoolean(Constant.PREF_IS_LOGIN, false);
    }

    public void saveUser(User user) {
        editor.putString(Constant.PREF_USER, new Gson().toJson(User.parse(user)));
        editor.putString(Constant.PREF_USER_TYPE, user.getType());
        editor.commit();
    }

    public User getUser() {
        return new Gson().fromJson(pref.getString(Constant.PREF_USER, null), User.class);
    }

    public String getUserType() {
        return pref.getString(Constant.PREF_USER_TYPE, "");
    }

    public void savePhone(String phone) {
        editor.putString(Constant.PREF_PHONE, phone);
        editor.commit();
    }

    public String getPhone() {
        return pref.getString(Constant.PREF_PHONE, null);
    }

    public void saveToken(String token) {
        editor.putString(Constant.PREF_TOKEN, token);
        editor.commit();
    }

    public String getToken() {
        return pref.getString(Constant.PREF_TOKEN, "");
    }

    public void saveBaseDataLoaded() {
        editor.putBoolean(Constant.REALM_BASE_DATA_LOADED, true);
        editor.commit();
    }

    public boolean isBaseDataLoaded() {
        return pref.getBoolean(Constant.REALM_BASE_DATA_LOADED, false);
    }

    public void saveProfilePhotoChanged(boolean changed) {
        editor.putBoolean(Constant.PREF_IS_PROFILE_PHOTO_CHANGED, changed);
        editor.commit();
    }

    public boolean isProfilePhotoChanged() {
        return pref.getBoolean(Constant.PREF_IS_PROFILE_PHOTO_CHANGED, false);
    }

    public void saveServerTime(String time) {
        editor.putString(Constant.PREF_SERVER_TIME, time);
        editor.commit();
    }

    public String getServerTime() {
        return pref.getString(Constant.PREF_SERVER_TIME, "");
    }

    public void saveNotificationSetting(boolean enabled) {
        editor.putBoolean(Constant.PREF_NOTIFICATION, enabled);
        editor.commit();
    }

    public boolean isNotificationEnabled() {
        return pref.getBoolean(Constant.PREF_NOTIFICATION, true);
    }

    public void saveReplySoundSetting(boolean enabled) {
        editor.putBoolean(Constant.PREF_REPLY_SOUND, enabled);
        editor.commit();
    }

    public boolean isReplySoundEnabled() {
        return pref.getBoolean(Constant.PREF_REPLY_SOUND, true);
    }

    public void saveCurrentTheme(int lesson) {
        editor.putInt(Constant.PREF_THEME, lesson);
        editor.commit();
    }

    public int getCurrentTheme() {
        return pref.getInt(Constant.PREF_THEME, 0);
    }

    public void saveEnvChat(int env) {
        editor.putInt(Constant.PREF_ENV_CHAT, env);
        editor.commit();
    }

    public int getEnvChat() {
        return pref.getInt(Constant.PREF_ENV_CHAT, 0);
    }

    public void saveCurrentDialog(String dialogId) {
        editor.putString(Constant.PREF_DIALOG_ID, dialogId);
        editor.commit();
    }

    public String getCurrentDialog() {
        return pref.getString(Constant.PREF_DIALOG_ID, "");
    }

    public void saveCurrentReceiverCode(String receiverCode) {
        editor.putString(Constant.PREF_RECEIVER_CODE, receiverCode);
        editor.commit();
    }

    public String getCurrentReceiverCode() {
        return pref.getString(Constant.PREF_RECEIVER_CODE, "");
    }

    public void saveCurrentReceiverType(String receiverType) {
        editor.putString(Constant.PREF_RECEIVER_TYPE, receiverType);
        editor.commit();
    }

    public String getCurrentReceiverType() {
        return pref.getString(Constant.PREF_RECEIVER_TYPE, "");
    }

    public void saveActiveDialog(String dialogId) {
        editor.putString(Constant.PREF_ACTIVE_DIALOG, dialogId);
        editor.commit();
    }

    public String getActiveDialog() {
        return pref.getString(Constant.PREF_ACTIVE_DIALOG, "");
    }

    public void saveUnreadBadgeSet() {
        editor.putBoolean(Constant.PREF_UNREAD_BADGE_SET, true);
        editor.commit();
    }

    public boolean isUnreadBadgeSet() {
        return pref.getBoolean(Constant.PREF_UNREAD_BADGE_SET, false);
    }

    public void saveUnreadBadgeUpdated(boolean updated) {
        editor.putBoolean(Constant.PREF_UNREAD_BADGE_UPDATED, updated);
        editor.commit();
    }

    public boolean isUnreadBadgeUpdated() {
        return pref.getBoolean(Constant.PREF_UNREAD_BADGE_UPDATED, true);
    }

    public void saveUnreadDialogCounter(int lesson, int unread) {
        editor.putInt(Utils.prefLessons[lesson], unread);
        editor.commit();
    }

    public int getUnreadDialogCounter(int lesson) {
        return pref.getInt(Utils.prefLessons[lesson], 0);
    }

    public void saveTotalUnreadDialog(int total) {
        editor.putInt(Constant.PREF_TOTAL_UNREAD_DIALOG, total);
        editor.commit();
    }

    public int getTotalUnreadDialog() {
        return pref.getInt(Constant.PREF_TOTAL_UNREAD_DIALOG, 0);
    }

    public void saveAppRaterFirstLaunch() {
        editor.putBoolean(Constant.PREF_APP_RATER_FIRST_LAUNCH, true);
        editor.putLong(Constant.PREF_APP_RATER_DATE_LAUNCH, System.currentTimeMillis());
        editor.commit();
    }

    public boolean isAppRaterFirstLaunch() {
        return pref.getBoolean(Constant.PREF_APP_RATER_FIRST_LAUNCH, false);
    }

    public long getAppRaterDateLaunch() {
        return pref.getLong(Constant.PREF_APP_RATER_DATE_LAUNCH, 0);
    }

    public void saveAppSubscriptionLaunch() {
        editor.putLong(Constant.PREF_APP_SUBSCRIPTION_DATE_LAUNCH, System.currentTimeMillis());
        editor.commit();
    }

    public long getAppSubscriptionDateLaunch() {
        return pref.getLong(Constant.PREF_APP_SUBSCRIPTION_DATE_LAUNCH, 0);
    }

    public void saveChattingPaused(boolean paused) {
        editor.putBoolean(Constant.PREF_CHATTING_PAUSED, paused);
        editor.commit();
    }

    public boolean isChattingPaused() {
        return pref.getBoolean(Constant.PREF_CHATTING_PAUSED, false);
    }

    public void saveVersionCode(int versionCode) {
        editor.putInt(Constant.PREF_VERSION_CODE, versionCode);
        editor.commit();
    }

    public int getVersionCode() {
        return pref.getInt(Constant.PREF_VERSION_CODE, 0);
    }

    public void startServices() {
        context.startService(new Intent(context, FetchProfilesService.class));
        context.startService(new Intent(context, CheckOnlineService.class));
        context.startService(new Intent(context, StoreChatService.class));
        context.startService(new Intent(context, ChattingService.class));

        if (getUserType().equals(Constant.STUDENT)) {
            context.startService(new Intent(context, FetchTeachersService.class));
        }
    }

    private void stopServices() {
        context.stopService(new Intent(context, FetchProfilesService.class));
        context.stopService(new Intent(context, CheckOnlineService.class));
        context.stopService(new Intent(context, StoreChatService.class));
        context.stopService(new Intent(context, ChattingService.class));

        if (getUserType().equals(Constant.STUDENT)) {
            context.stopService(new Intent(context, FetchTeachersService.class));
        }
    }

    public void logout(int mode) {
        stopServices();
        clear(mode);

        Utils.with(context).deleteAppDir();
        Utils.with(context).createAppDir();

        ShortcutBadger.removeCount(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        Intent intent;
        if (mode == 0) {
            intent = new Intent(context, LoginActivity.class);
        } else {
            intent = new Intent(context, IntroActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void clear(int mode) {
        String phone = getUser().getPhone();
        String firebase = getFirebaseToken();
        int versionCode = getVersionCode();

        editor.clear().commit();
        if (mode == 0) {
            saveFirstLogin();
            savePhone(phone);
            saveFirebaseToken(firebase);
//            saveVersionCode(versionCode);
        }
    }
}