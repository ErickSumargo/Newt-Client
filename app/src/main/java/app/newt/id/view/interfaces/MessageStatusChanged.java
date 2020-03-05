package app.newt.id.view.interfaces;

import app.newt.id.server.model.Chat;

/**
 * Created by Erick Sumargo on 3/23/2018.
 */

public interface MessageStatusChanged {
    void onMessageStatusChanged(Chat chat);
}
