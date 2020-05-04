package com.sarcobjects.a500mts.android;

import android.app.Application;


public class MapApplication extends Application {
    // Reference to the application graph that is used across the whole app
    public static ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationComponent = DaggerApplicationComponent.builder()
                .contextModule(new ContextModule(getApplicationContext()))
                .build();
    }

    public static ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}

