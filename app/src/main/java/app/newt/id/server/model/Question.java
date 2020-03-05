package app.newt.id.server.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class Question {
    private User teacher;

    private int id, answer, level, point, attempt, status;

    @SerializedName("challenge_lesson_id")
    private int challengeLessonId;

    private String content, image, material;

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChallengeLessonId() {
        return challengeLessonId;
    }

    public void setChallengeLessonId(int challengeLessonId) {
        this.challengeLessonId = challengeLessonId;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }
}