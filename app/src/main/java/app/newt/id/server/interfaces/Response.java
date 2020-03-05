package app.newt.id.server.interfaces;

import app.newt.id.server.response.BaseResponse;

/**
 * Created by edinofri on 02/11/2016.
 */

public interface Response {
    void onSuccess(BaseResponse base);

    void onFailure(String message);
}