package app.newt.id.server.response;

import java.util.List;

import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.Lesson;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class DialogsResponse extends BaseResponse {
    public class Data {
        private List<Dialog> dialogs;

        private List<Lesson> lessons;

        public List<Dialog> getDialogs() {
            return dialogs;
        }

        public void setDialogs(List<Dialog> dialogs) {
            this.dialogs = dialogs;
        }

        public List<Lesson> getLessons() {
            return lessons;
        }

        public void setLessons(List<Lesson> lessons) {
            this.lessons = lessons;
        }
    }

    private Data data;

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }
}