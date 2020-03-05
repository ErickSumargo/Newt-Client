package app.newt.id.server.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.newt.id.server.model.Attempt;
import app.newt.id.server.model.Challenge;
import app.newt.id.server.model.Challenger;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class ChallengeResponse extends BaseResponse {
    public class Data {
        @SerializedName("challenger_name")
        private String challengerName;

        private int points, solved;
        private List<String> infos;

        @SerializedName("easy_questions")
        private List<Challenge> easyQuestions;

        @SerializedName("medium_questions")
        private List<Challenge> mediumQuestions;

        @SerializedName("hard_questions")
        private List<Challenge> hardQuestions;

        private String periode;

        private int rank;

        private List<Challenger> challengers;

        private Challenge challenge;

        @SerializedName("challenge_attempt")
        private Attempt challengeAttempt;

        private List<Challenge> records, histories;

        public List<String> getInfos() {
            return infos;
        }

        public void setInfos(List<String> infos) {
            this.infos = infos;
        }

        public String getChallengerName() {
            return challengerName;
        }

        public void setChallengerName(String challengerName) {
            this.challengerName = challengerName;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public int getSolved() {
            return solved;
        }

        public void setSolved(int solved) {
            this.solved = solved;
        }

        public List<Challenge> getEasyQuestions() {
            return easyQuestions;
        }

        public void setEasyQuestions(List<Challenge> easyQuestions) {
            this.easyQuestions = easyQuestions;
        }

        public List<Challenge> getMediumQuestions() {
            return mediumQuestions;
        }

        public void setMediumQuestions(List<Challenge> mediumQuestions) {
            this.mediumQuestions = mediumQuestions;
        }

        public List<Challenge> getHardQuestions() {
            return hardQuestions;
        }

        public void setHardQuestions(List<Challenge> hardQuestions) {
            this.hardQuestions = hardQuestions;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public String getPeriode() {
            return periode;
        }

        public void setPeriode(String periode) {
            this.periode = periode;
        }

        public List<Challenger> getChallengers() {
            return challengers;
        }

        public void setChallengers(List<Challenger> challengers) {
            this.challengers = challengers;
        }

        public Challenge getChallenge() {
            return challenge;
        }

        public void setChallenge(Challenge challenge) {
            this.challenge = challenge;
        }

        public Attempt getChallengeAttempt() {
            return challengeAttempt;
        }

        public void setChallengeAttempt(Attempt challengeAttempt) {
            this.challengeAttempt = challengeAttempt;
        }

        public List<Challenge> getRecords() {
            return records;
        }

        public void setRecords(List<Challenge> records) {
            this.records = records;
        }

        public List<Challenge> getHistories() {
            return histories;
        }

        public void setHistories(List<Challenge> histories) {
            this.histories = histories;
        }
    }

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}