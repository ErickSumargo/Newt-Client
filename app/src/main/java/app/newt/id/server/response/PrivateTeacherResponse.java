package app.newt.id.server.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.newt.id.server.model.PrivateTeacher;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class PrivateTeacherResponse extends BaseResponse {
    public class Data {
        @SerializedName("private_teachers")
        private List<PrivateTeacher> privateTeachers;

        @SerializedName("tuition_teachers")
        private List<PrivateTeacher> tuitionTeachers;

        @SerializedName("private_promos")
        private List<String> privatePromos;

        @SerializedName("tuition_promos")
        private List<String> tuitionPromos;

        public List<String> getPrivatePromos() {
            return privatePromos;
        }

        public void setPrivatePromos(List<String> privatePromos) {
            this.privatePromos = privatePromos;
        }

        public List<String> getTuitionPromos() {
            return tuitionPromos;
        }

        public void setTuitionPromos(List<String> tuitionPromos) {
            this.tuitionPromos = tuitionPromos;
        }

        public List<PrivateTeacher> getPrivateTeachers() {
            return privateTeachers;
        }

        public void setPrivateTeachers(List<PrivateTeacher> privateTeachers) {
            this.privateTeachers = privateTeachers;
        }

        public List<PrivateTeacher> getTuitionTeachers() {
            return tuitionTeachers;
        }

        public void setTuitionTeachers(List<PrivateTeacher> tuitionTeachers) {
            this.tuitionTeachers = tuitionTeachers;
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