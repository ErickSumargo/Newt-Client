package app.newt.id.server.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.newt.id.server.model.Tips;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class FeatureResponse extends BaseResponse {
    public class Data {
        private List<Integer> unsolved;

        private Tips tips;

        @SerializedName("version_code")
        private int versionCode;

        public List<Integer> getUnsolved() {
            return unsolved;
        }

        public void setUnsolved(List<Integer> unsolved) {
            this.unsolved = unsolved;
        }

        public Tips getTips() {
            return tips;
        }

        public void setTips(Tips tips) {
            this.tips = tips;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
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