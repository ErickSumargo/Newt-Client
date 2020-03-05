package app.newt.id.server.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.newt.id.server.model.User;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class UserResponse extends BaseResponse {
    public class Data {
        private User user;
        private String token;

        private List<User> users;

        @SerializedName("device_registered")
        private boolean deviceRegistered;

        public void setUser(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public boolean isDeviceRegistered() {
            return deviceRegistered;
        }

        public void setDeviceRegistered(boolean deviceRegistered) {
            this.deviceRegistered = deviceRegistered;
        }

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
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