package com.sarcobjects.a500mts.android

import android.content.Context
import com.sarcobjects.a500mts.BingSnapToRoad
import com.sarcobjects.a500mts.SnapToRoad
import dagger.Module
import dagger.Provides

@Module
class ContextModule(@get:Provides var context: Context) {

    @get:Provides
    val snapToRoad: SnapToRoad
        get() = BingSnapToRoad(context)

}