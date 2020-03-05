package app.newt.id.server.response;

import java.util.List;

import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.Teacher;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class TeachersResponse extends BaseResponse {
    public class Data {
        private List<Teacher> teachers;

        public List<Teacher> getTeachers() {
            return teachers;
        }

        public void setTeachers(List<Teacher> teachers) {
            this.teachers = teachers;
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