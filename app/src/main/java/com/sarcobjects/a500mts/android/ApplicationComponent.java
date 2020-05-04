package com.sarcobjects.a500mts.android;

import com.sarcobjects.a500mts.MapsActivity;

import dagger.Component;

@Component(modules = ContextModule.class)
public interface ApplicationComponent {

    void inject(MapsActivity mapsActivity);
}
