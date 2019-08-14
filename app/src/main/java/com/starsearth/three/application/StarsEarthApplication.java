package com.starsearth.three.application;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;
import com.starsearth.three.domain.FirebaseRemoteConfigWrapper;
import com.starsearth.three.managers.AnalyticsManager;

/**
 * Created by faimac on 11/28/16.
 */

public class StarsEarthApplication extends Application {

    private FirebaseRemoteConfigWrapper mFirebaseRemoteConfigWrapper;
    private AnalyticsManager mAnalyticsManager;

    public FirebaseRemoteConfigWrapper getFirebaseRemoteConfigWrapper() {
        return mFirebaseRemoteConfigWrapper;
    }

    public AnalyticsManager getAnalyticsManager() {
        return mAnalyticsManager;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mFirebaseRemoteConfigWrapper = new FirebaseRemoteConfigWrapper(getApplicationContext());
        mAnalyticsManager = new AnalyticsManager(getApplicationContext());
    }

    public String getRemoteConfigAnalytics() {
        return mFirebaseRemoteConfigWrapper.get("analytics");
    }

}
