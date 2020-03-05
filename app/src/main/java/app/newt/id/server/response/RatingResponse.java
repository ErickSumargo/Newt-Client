package app.newt.id.server.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.newt.id.server.model.Teacher;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class RatingResponse extends BaseResponse {
    public class Data {
        private int rating;

        @SerializedName("avg_rating")
        private double avgRating;

        private int reviewers;

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public double getAvgRating() {
            return avgRating;
        }

        public void setAvgRating(double avgRating) {
            this.avgRating = avgRating;
        }

        public int getReviewers() {
            return reviewers;
        }

        public void setReviewers(int reviewers) {
            this.reviewers = reviewers;
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