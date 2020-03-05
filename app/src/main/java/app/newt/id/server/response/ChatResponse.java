package app.newt.id.server.response;

import app.newt.id.server.model.Chat;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class ChatResponse extends BaseResponse {
    public class Data {
        private Chat chat;

        public void setChat(Chat chat) {
            this.chat = chat;
        }

        public Chat getChat() {
            return chat;
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