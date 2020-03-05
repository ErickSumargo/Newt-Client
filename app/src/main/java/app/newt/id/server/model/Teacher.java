package app.newt.id.server.model;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class Teacher extends RealmObject {
    @PrimaryKey
    private String uuid = UUID.randomUUID().toString();

    private User user;

    private RealmList<Lesson> lessons;

    private RealmList<Available> availables;

    private RealmList<DaysOff> days_off;

    public String getUuid() {
        return uuid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RealmList<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(RealmList<Lesson> lessons) {
        this.lessons = lessons;
    }

    public RealmList<Available> getAvailables() {
        return availables;
    }

    public void setAvailables(RealmList<Available> availables) {
        this.availables = availables;
    }

    public RealmList<DaysOff> getDays_off() {
        return days_off;
    }

    public void setDays_off(RealmList<DaysOff> days_off) {
        this.days_off = days_off;
    }
}