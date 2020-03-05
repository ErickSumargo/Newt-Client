package app.newt.id.server.response;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class BaseResponse {
    private boolean success;
    private int error;
    private String tag;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getError() {
        return error;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}