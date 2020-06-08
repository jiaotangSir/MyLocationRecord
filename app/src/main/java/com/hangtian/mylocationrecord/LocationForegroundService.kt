package com.hangtian.mylocationrecord

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.view.LocationDisplay

class LocationForegroundService : Service() {

    private val pointList = mutableListOf<Point>()//可用来保存到本地或者上传服务器或显示在页面上

    private val locationBinder = LocationBinder()

    override fun onBind(intent: Intent): IBinder {
        return locationBinder
    }

    override fun onCreate() {
        super.onCreate()
        showNotify()
        goLocation()
    }

    //显示通知栏
    private fun showNotify() {

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val name = "后台定位"
            val channel = NotificationChannel("foreground", name, NotificationManager.IMPORTANCE_HIGH)
            channel.description = "后台定位通道"
            nm.createNotificationChannel(channel)
        }
        val intent = Intent(this, ArcgisLocationActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val mBuilder = NotificationCompat.Builder(this, "foreground")
        mBuilder.setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("正在后台定位")
            .setContentText("定位进行中")
            .setWhen(System.currentTimeMillis())
            .setContentIntent(pendingIntent)
        val notification = mBuilder.build()
        startForeground(2001, notification)
    }
    fun goLocation() {
//        val locationDisplay = MyApplication.locationDisplay

//            Log.d("定位","进入goLocation的次数:${locationSum++}")

        MyApplication.locationDisplay?.apply {
            autoPanMode = LocationDisplay.AutoPanMode.OFF
            isShowLocation = false//隐藏符号
            isShowAccuracy = false//隐藏符号的缓存区域
            isShowPingAnimation = false//隐藏位置更新的符号动画
            startAsync()

            addLocationChangedListener {
//                    Log.d("定位","进入addLocationChangedListener的次数:${sum++}")

                val changedLocation = it.location
                val wgsChangedPoint = changedLocation.position

                Log.d("定位","当前监听gps坐标系下的坐标：(${wgsChangedPoint.x},${wgsChangedPoint.y})")
//                    pointList.add(wgsChangedPoint)
                /** GeometryEngine.project(wgsChangedPoint, sr)中，
                 *  wgsChangedPoint表示要转换的坐标（即原坐标）
                 *  sr表示想要转换成的坐标系，本例中是将系统获得的坐标转换成3857坐标系
                 *  所以最后得到的就是3857坐标系下的点 */
                val sr = SpatialReference.create(3857)
                val mapPoint = GeometryEngine.project(wgsChangedPoint, sr) as Point

                Log.d("定位","当前地图gps坐标系下的坐标：(${mapPoint.x},${mapPoint.y})")
                pointList.add(mapPoint)
                locationBinder.mCallback(mapPoint)
            }
        }

    }
    class LocationBinder: Binder() {



        lateinit var mCallback : (Point)->Unit
        fun setOnLocationChangedListener(callback: (Point) -> Unit) {
            mCallback = callback
        }
    }
}
