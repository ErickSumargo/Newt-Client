package app.newt.id.view.interfaces;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public interface ChatEvent {
    void onNewMessage(String senderCode, String receiverCode, String content, int contentType);
    void onTyping();
    void onStopTyping();
}