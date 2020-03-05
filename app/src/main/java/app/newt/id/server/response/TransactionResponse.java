package app.newt.id.server.response;

import java.util.List;

import app.newt.id.server.model.Bank;
import app.newt.id.server.model.Package;
import app.newt.id.server.model.Provider;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class TransactionResponse extends BaseResponse {
    public class Data {
        private User user;
        private String token;
        private boolean existed;

        private List<Package> packages;
        private List<Provider> providers;
        private List<Bank> banks;

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

        public boolean isExisted() {
            return existed;
        }

        public void setExisted(boolean existed) {
            this.existed = existed;
        }

        public List<Package> getPackages() {
            return packages;
        }

        public void setPackages(List<Package> packages) {
            this.packages = packages;
        }

        public List<Provider> getProviders() {
            return providers;
        }

        public void setProviders(List<Provider> providers) {
            this.providers = providers;
        }

        public List<Bank> getBanks() {
            return banks;
        }

        public void setBanks(List<Bank> banks) {
            this.banks = banks;
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