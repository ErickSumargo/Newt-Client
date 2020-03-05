package app.newt.id.server.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class Package {
    private int transaction, days;

    public int getTransaction() {
        return transaction;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }

    public void setTransaction(int transaction) {
        this.transaction = transaction;
    }
}