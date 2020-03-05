package app.newt.id.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.webkit.MimeTypeMap;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app.newt.id.R;
import app.newt.id.server.model.Chat;
import app.newt.id.server.model.DaysOff;
import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.Lesson;
import app.newt.id.server.model.User;
import io.realm.RealmList;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by Erick Sumargo on 3/5/2018.
 */

public class Utils {
    private Context context;

    public static int[] challenges = {R.string.logics, R.string.mathematics};
    public static int[] lessons = {R.string.mathematics, R.string.physics, R.string.chemistry};
    public static String[] prefLessons = {Constant.PREF_MATH_UNREAD_DIALOG, Constant.PREF_PHYS_UNREAD_DIALOG, Constant.PREF_CHEM_UNREAD_DIALOG};
    public static int[] payments = {R.string.payment_1, R.string.payment_2};

    public static int[] colorsPrimary = {R.color.mathematics, R.color.physics, R.color.chemistry};
    public static int[] colorsPrimaryDark = {R.color.mathematicsDark, R.color.physicsDark, R.color.chemistryDark};
    public static int[] colorsIntro = {R.color.intro1, R.color.intro2, R.color.intro3, R.color.intro4};

    public static int[] colorsHex = {0xffef4100, 0xff00aeef, 0xff7c26cb};

    public static int[] icLessons = {R.drawable.ic_mathematics, R.drawable.ic_physics, R.drawable.ic_chemistry};
    public static int[] bubbles = {R.drawable.custom_bubble_mathematics, R.drawable.custom_bubble_physics, R.drawable.custom_bubble_chemistry};

    public static int[] styles = {R.style.AppTheme_Mathematics, R.style.AppTheme_Physics, R.style.AppTheme_Chemistry};
    public static int[] privateStyles = {R.style.AppTheme_SD, R.style.AppTheme_SMP, R.style.AppTheme_SMA,
            R.style.AppTheme_PEnglish, R.style.AppTheme_PMandarin, R.style.AppTheme_Programming};
    public static int[] challengeStyles = {R.style.AppTheme_Mathematics_Challenge, R.style.AppTheme_Physics_Challenge};

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Utils(Context context) {
        this.context = context;
    }

    public static Utils with(Context context) {
        return new Utils(context);
    }

    public void createAppDir() {
        createBaseDir();
        createMediaDir();
    }

    private void createBaseDir() {
        String path = Constant.APP_DIR + File.separator + Constant.DIR_MEDIA_INTERNAL + File.separator + Constant.DIR_PICTURES_INTERNAL;
        createDir(path, 0);

        path = Constant.APP_DIR + File.separator + Constant.DIR_MEDIA_INTERNAL + File.separator + Constant.DIR_DOCUMENTS_INTERNAL;
        createDir(path, 0);
    }

    private void createMediaDir() {
        String path = context.getString(R.string.app_name) + File.separator + Constant.DIR_DOWNLOAD_EXTERNAL;
        createDir(path, 1);

        path = context.getString(R.string.app_name) + File.separator + Constant.DIR_SHARE_EXTERNAL;
        createDir(path, 1);
    }

    private void createDir(String path, int type) {
        File dir;
        if (type == 0) {
            dir = new File(path);
        } else {
            dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), path);
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void saveImage(Bitmap bitmap, String fileName, String dir, boolean isExternal) {
        File file = getFile(fileName, dir, isExternal);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.close();
        } catch (IOException e) {
        }
    }

    public void deleteImage(String fileName, String dir, boolean isExternal) {
        File file = getFile(fileName, dir, isExternal);
        file.delete();
    }

    public void saveDocument(File source, String fileName, String dir, boolean isExternal) {
        File file = getFile(fileName, dir, isExternal);
        try {
            FileOutputStream out = new FileOutputStream(file);
            InputStream in = new FileInputStream(source);

            byte[] buf = new byte[8192];
            int c = 0;
            while ((c = in.read(buf, 0, buf.length)) > 0) {
                out.write(buf, 0, c);
                out.flush();
            }

            out.close();
            in.close();
        } catch (IOException e) {
        }
    }

    public File getFile(String fileName, String dir, boolean isExternal) {
        createAppDir();

        File file;
        if (isExternal) {
            String path = context.getString(R.string.app_name) + File.separator + dir + File.separator + fileName;
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path);
        } else {
            String path = Constant.APP_DIR + File.separator + Constant.DIR_MEDIA_INTERNAL + File.separator + dir;
            file = new File(path, fileName);
        }
        return file;
    }

    public void deleteAppDir() {
        File dir = new File(Constant.APP_DIR);
        if (dir.exists()) {
            try {
                FileUtils.deleteDirectory(dir);
            } catch (Exception e) {
            }
        }
    }

    // Cause Bug...
    public String getUserType(String code) {
        String type = code.split("_")[0].equals("STU") ? Constant.STUDENT : Constant.TEACHER;
        return type;
    }

    public String getDialogId(String code1, String code2, int lessonId) {
        String[] partsCode1 = code1.split("_");
        String[] partsCode2 = code2.split("_");

        String id = "";
        if (partsCode1[0].equals("STU") && partsCode2[0].equals("TEA")) {
            id = code1 + "-" + code2 + "-" + lessonId;
        } else {
            id = code2 + "-" + code1 + "-" + lessonId;
        }
        return id;
    }

    public int getNumericDialogId(String code1, String code2, int lesson) {
        String[] partsCode1 = code1.split("_");
        String[] partsCode2 = code2.split("_");

        String id = "";
        if (partsCode1[0].equals("STU") && partsCode2[0].equals("TEA")) {
            id = partsCode1[1] + partsCode2[1] + lesson;
        } else {
            id = partsCode2[1] + partsCode1[1] + lesson;
        }
        return Integer.parseInt(id);
    }

    public long getDifferentDays(String createdAt, int priority) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDate = null, date = null;
        try {
            if (priority == 0) {
                currentDate = Calendar.getInstance().getTime();
            } else {
                if (!Session.with(context).getServerTime().equals("")) {
                    currentDate = df.parse(Session.with(context).getServerTime());
                } else {
                    currentDate = Calendar.getInstance().getTime();
                }
            }
            date = df.parse(createdAt);
        } catch (ParseException e) {
        }
        return TimeUnit.MILLISECONDS.toDays(currentDate.getTime() - date.getTime());
    }

    public boolean isToday(String ds) {
        Date date = null;
        try {
            date = df.parse(ds);
        } catch (ParseException e) {
        }
        return DateUtils.isToday(date.getTime());
    }

    public boolean isSameDate(String ds1, String ds2) {
        Date d1 = null, d2 = null;
        try {
            d1 = df.parse(ds1);
            d2 = df.parse(ds2);
        } catch (ParseException e) {
        }
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public boolean isExpired(String subscription) {
        Date now = null;
        Date date = null;
        try {
            if (!Session.with(context).getServerTime().equals("")) {
                now = df.parse(Session.with(context).getServerTime());
            } else {
                now = Calendar.getInstance().getTime();
            }
            date = df.parse(subscription);

            return now.getTime() > date.getTime();
        } catch (ParseException e) {
            return false;
        }
    }

    public String getCurrentDate() {
        Date currentDate = Calendar.getInstance().getTime();
        return df.format(currentDate);
    }

    public void saveServerTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NTPUDPClient timeClient = new NTPUDPClient();
                    InetAddress inetAddress = InetAddress.getByName("time-a.nist.gov");
                    TimeInfo timeInfo = timeClient.getTime(inetAddress);
                    long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();

                    Date now = new Date(returnTime);
                    Session.with(context).saveServerTime(df.format(now));
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public String formatDate(String createdAt, String format) {
        SimpleDateFormat dateDefault = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            return dateFormat.format(dateDefault.parse(createdAt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean isTodayOff(List<DaysOff> days_off) {
        Calendar c = Calendar.getInstance();
        for (DaysOff dayoff : days_off) {
            if (dayoff.getDay() == c.get(Calendar.DAY_OF_WEEK)) {
                return true;
            }
        }
        return false;
    }

    public String getCompleteFormatDate(String createdAt) {
        SimpleDateFormat of = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        Date date = null;
        try {
            date = df.parse(createdAt);
        } catch (ParseException e) {
        }
        return of.format(date);
    }

    public void playDefSound() {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer player = MediaPlayer.create(context, notification);
            player.start();
        } catch (Exception e) {
        }
    }

    public void playReplySound() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);

        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.appointed);
        mediaPlayer.start();
    }

    public void playPrioritySound() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);

        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.priority);
        mediaPlayer.start();
    }

    public void playCorrectSound() {
        try {
            vibrate();

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);

            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.definite);
            mediaPlayer.start();
        } catch (Exception e) {
        }
    }

    public void vibrate() {
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0, 250, 200, 250}, -1);
    }

    public Bitmap maintainBitmap(Uri uri) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));

        return imageLoader.loadImageSync(uri.toString());
    }

    public Bitmap adjustBitmap(Bitmap bitmap, Uri uri) {
        Bitmap output = bitmap;
        try {
            ExifInterface exifInterface = new ExifInterface(getUriPath(uri));
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int degrees = getDegrees(orientation);

            Matrix matrix = new Matrix();
            if (degrees != 0f) {
                matrix.preRotate(degrees);
            }
            output = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
        }
        return output;
    }

    public String getUriPath(Uri uri) {
        String resultPath = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            resultPath = uri.getPath();
        } else {
            if (cursor.moveToFirst()) {
                int indexPath = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                resultPath = cursor.getString(indexPath);
            }
            cursor.close();
        }
        return resultPath;
    }

    private int getDegrees(int orientation) {
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public int dpToPx(int dp) {
        return dp * (int) context.getResources().getDisplayMetrics().density;
    }

    public Dialog createDialog(String id, User receiver, RealmList<Chat> c, Lesson lesson) {
        Dialog d = new Dialog();

        User u = new User();
        u.setId(receiver.getId());
        u.setName(receiver.getName());
        u.setCode(receiver.getCode());
        u.setPhone(receiver.getPhone());
        u.setPhoto(receiver.getPhoto());
        u.setActive(receiver.getActive());
        u.setCreatedAt(receiver.getCreatedAt());
        u.setType(receiver.getType());

        final Lesson l = new Lesson();
        l.setId(lesson.getId());
        l.setName(lesson.getName());
        l.setCreatedAt(lesson.getCreatedAt());

        d.setId(id);
        d.setUser(u);
        d.setLesson(l);
        d.setChats(c);
        d.setUpdatedAt(Calendar.getInstance().getTime());

        return d;
    }

    public Chat createChat(String senderCode, String receiverCode, String content, int contentType,
                           int lessonId, int sent, String createdAt) {
        final Chat c = new Chat();
        c.setSenderCode(senderCode);
        c.setReceiverCode(receiverCode);
        c.setContent(content);
        c.setContentType(contentType);
        c.setLesson_id(lessonId);
        c.setSent(sent);
        c.setCreatedAt(createdAt);

        return c;
    }

    public JSONObject createJSON(String event, String... args) {
        JSONObject object = new JSONObject();
        try {
            if (event.equals(Constant.CHATTING_JOIN_DIALOG)) {
                object.put("dialog", args[0]);
                object.put(Constant.REQ_RECEIVER_CODE, args[1]);
            } else if (event.equals(Constant.CHATTING_LEAVE_DIALOG)) {
                object.put("dialog", args[0]);
            } else if (event.equals(Constant.CHATTING_IS_ACTIVE)) {
                object.put("dialog", args[0]);
                object.put(Constant.REQ_RECEIVER_CODE, args[1]);
            } else if (event.equals(Constant.CHATTING_MESSAGE)) {
                object.put(Constant.REQ_UNIQUE_CODE, args[0]);
                object.put(Constant.REQ_SENDER_CODE, args[1]);
                object.put(Constant.REQ_RECEIVER_CODE, args[2]);
                object.put(Constant.REQ_CONTENT, args[3]);
                object.put(Constant.REQ_CONTENT_TYPE, args[4]);
                object.put(Constant.REQ_LESSON_ID, args[5]);
                object.put(Constant.REQ_SENT, args[6]);

                object.put(Constant.REQ_SENDER_ID, args[7]);
                object.put(Constant.REQ_SENDER_NAME, args[8]);
                object.put(Constant.REQ_SENDER_PHONE, args[9]);
                object.put(Constant.REQ_SENDER_PHOTO, args[10]);
                object.put(Constant.REQ_SENDER_ACTIVE, args[11]);
                object.put(Constant.REQ_SENDER_CREATED_AT, args[12]);
                object.put(Constant.REQ_SENDER_PRO, args[13]);
            } else if (event.equals(Constant.CHATTING_CHECK_ONLINE)) {
                object.put(Constant.REQ_RECEIVER_CODE, args[0]);
            }
        } catch (JSONException e) {
        }
        return object;
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public RequestBody convertToRequestBody(String s) {
        return RequestBody.create(MediaType.parse("text/plain"), s);
    }

    public RequestBody convertToRequestBody(File file) {
        return RequestBody.create(MediaType.parse(getMimeType(Uri.fromFile(file))), file);
    }

    public String getURLMediaImage(String name, String pathType) {
        return Constant.URL_MEDIA_IMAGE + pathType + "/" + name;
    }

    public String getMimeType(Uri uri) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        return mimeType;
    }

    public String getFileExtension(Uri uri) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        return extension;
    }

    public boolean proVersionInstalled() {
        final PackageManager packageManager = context.getPackageManager();
        Intent i = packageManager.getLaunchIntentForPackage("app.newt.pro.id");
        if (i != null) {
            List<ResolveInfo> list = packageManager.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
            if (!list.isEmpty()) {
                return true;
            }
            return false;
        }
        return false;
    }
}