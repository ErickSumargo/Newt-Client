package app.newt.id.server.response.wrapper;

import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.DialogsResponse;
import app.newt.id.server.response.TeachersResponse;

/**
 * Created by Erick Sumargo on 3/14/2018.
 */

public class BaseWrapper extends BaseResponse{
    private DialogsResponse dialogsResponse;
    private TeachersResponse teachersResponse;

    public BaseWrapper(DialogsResponse dialogsResponse, TeachersResponse teachersResponse) {
        this.dialogsResponse = dialogsResponse;
        this.teachersResponse = teachersResponse;
    }

    public BaseWrapper(DialogsResponse dialogsResponse) {
        this.dialogsResponse = dialogsResponse;
    }

    public DialogsResponse getDialogsResponse() {
        return dialogsResponse;
    }

    public void setDialogsResponse(DialogsResponse dialogsResponse) {
        this.dialogsResponse = dialogsResponse;
    }

    public TeachersResponse getTeachersResponse() {
        return teachersResponse;
    }

    public void setTeachersResponse(TeachersResponse teachersResponse) {
        this.teachersResponse = teachersResponse;
    }
}