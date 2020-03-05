package app.newt.id.server.response;

import java.util.List;

import app.newt.id.server.model.Lesson;
import app.newt.id.server.model.Teacher;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class LessonsResponse extends BaseResponse {
    public class Data {
        private List<Lesson> lessons;

        public List<Lesson> getLessons() {
            return lessons;
        }

        public void setLessons(List<Lesson> lessons) {
            this.lessons = lessons;
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