package app.newt.id.server.model;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            final RealmObjectSchema user = schema.get("User");
            user.addField("school", String.class);

            oldVersion++;
        }
        if (oldVersion == 1) {
            final RealmObjectSchema user = schema.get("User");
            user.addField("challenger", boolean.class);

            oldVersion++;
        }
        if (oldVersion == 2) {
            final RealmObjectSchema user = schema.get("User");
            user.addField("socialLinks", String.class);
        }
    }
}