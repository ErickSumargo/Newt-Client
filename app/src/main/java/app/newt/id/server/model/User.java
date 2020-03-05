package app.newt.id.server.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Erick Sumargo on 2/27/2018.
 */

public class User extends RealmObject {
    @PrimaryKey
    private int id;
    private String name, code, phone, school, photo, subscription, type;
    private int active, pro, online = 0;
    private boolean challenger;

    @SerializedName("social_links")
    private String socialLinks;

    @SerializedName("created_at")
    private String createdAt;

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
    }


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getPhoto() {
        return photo;
    }


    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(String socialLinks) {
        this.socialLinks = socialLinks;
    }

    public int getActive() {
        return active;
    }


    public void setActive(int active) {
        this.active = active;
    }


    public String getSubscription() {
        return subscription;
    }


    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }


    public String getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public int getPro() {
        return pro;
    }

    public void setPro(int pro) {
        this.pro = pro;
    }

    public boolean isOnline() {
        return online == 1;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public boolean isChallenger() {
        return challenger;
    }

    public void setChallenger(boolean challenger) {
        this.challenger = challenger;
    }

    public static User parse(User user) {
        return new Gson().fromJson(new Gson().toJson(user), User.class);
    }
}