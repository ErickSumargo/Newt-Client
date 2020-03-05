package app.newt.id.server.response;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class RegistrationResponse extends BaseResponse {
    public class Data {
        private boolean skipped;
        private String code;

        public void setSkipped(boolean skipped) {
            this.skipped = skipped;
        }

        public boolean isSkipped() {
            return skipped;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
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