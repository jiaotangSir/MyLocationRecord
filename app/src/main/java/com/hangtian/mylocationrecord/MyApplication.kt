package com.hangtian.mylocationrecord

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.view.LocationDisplay

/**
 * Author:         刘叶波
 * CreateDate:     2020/6/8 10:38
 * Description:
 *
 */
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        context = this

        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud6301317339,none,LHH93PJPXJM5RJE15100")
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context


        var locationDisplay:LocationDisplay?=null
    }
}