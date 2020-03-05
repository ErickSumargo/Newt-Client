package app.newt.id.server.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class Attempt {
    @SerializedName("challenger_id")
    private int challengerId;

    @SerializedName("challenge_question_id")
    private int challengeQuestionId;

    private int attempt, status;

    public int getChallengerId() {
        return challengerId;
    }

    public void setChallengerId(int challengerId) {
        this.challengerId = challengerId;
    }

    public int getChallengeQuestionId() {
        return challengeQuestionId;
    }

    public void setChallengeQuestionId(int challengeQuestionId) {
        this.challengeQuestionId = challengeQuestionId;
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
}