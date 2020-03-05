package app.newt.id.server.model;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

import app.newt.id.Newt;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Erick Sumargo on 2/27/2018.
 */

public class Chat extends RealmObject {
    @PrimaryKey
    private String uuid;

    private int id;

    @SerializedName("sender_code")
    private String senderCode;

    @SerializedName("receiver_code")
    private String receiverCode;

    private String content;

    @SerializedName("content_type")
    private int contentType;

    private int lesson_id, sent;

    @SerializedName("created_at")
    private String createdAt;

    public Chat() {
        Realm realm = Realm.getDefaultInstance();
        generateUuid(realm);
        realm.close();
    }

    private void generateUuid(Realm realm) {
        String uuid = UUID.randomUUID().toString();
        Chat c = realm.where(Chat.class).equalTo("uuid", uuid).findFirst();
        if (c != null) {
            generateUuid(realm);
        } else {
            this.uuid = uuid;
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenderCode() {
        return senderCode;
    }

    public void setSenderCode(String senderCode) {
        this.senderCode = senderCode;
    }

    public String getReceiverCode() {
        return receiverCode;
    }

    public void setReceiverCode(String receiverCode) {
        this.receiverCode = receiverCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public int getLesson_id() {
        return lesson_id;
    }

    public void setLesson_id(int lesson_id) {
        this.lesson_id = lesson_id;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}