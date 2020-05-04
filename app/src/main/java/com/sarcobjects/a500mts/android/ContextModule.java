package com.sarcobjects.a500mts.android;

import android.content.Context;

import com.sarcobjects.a500mts.BingSnapToRoad;
import com.sarcobjects.a500mts.SnapToRoad;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {

    Context context;

    public ContextModule(Context context) {
        this.context = context;
    }

    @Provides
    Context getContext() {
        return context;
    }

    @Provides
    SnapToRoad getSnapToRoad() {
        return new BingSnapToRoad(getContext());
    }
}
