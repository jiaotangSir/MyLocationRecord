package com.hangtian.mylocationrecord

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.hangtian.permissionxlibrary.PermissionX
import kotlinx.android.synthetic.main.activity_arcgis_location.*


class ArcgisLocationActivity : AppCompatActivity() {

    val baseUrl = "http://119.80.161.7:6080/arcgis/rest/services//YGDT/MapServer"
    lateinit var binder: LocationForegroundService.LocationBinder

    private val graphicsOverlay = GraphicsOverlay()

    private val connection = object : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as LocationForegroundService.LocationBinder
//            binder.goLocation()
            binder.setOnLocationChangedListener { mapPoint->
                val smp = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,Color.RED,5f)
                val graphic = Graphic(mapPoint,smp)
                graphicsOverlay.graphics.clear()
                graphicsOverlay.graphics.add(graphic)

            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcgis_location)

        val arcGISMap = ArcGISMap()
        arcGISMap.basemap = Basemap(ArcGISTiledLayer(baseUrl))

        mMapView.map = arcGISMap
        mMapView.graphicsOverlays.add(graphicsOverlay)


        arcGISMap.basemap.addDoneLoadingListener {
            val parms = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            PermissionX.request(this,"请同意定位权限",*parms){
                currentLocation()
            }
        }


    }

    /** 有问题，这里应该在mapView加载出来以后再调用 */
    private fun currentLocation() {
        val locationDisplay = mMapView.locationDisplay

        //将定位保存到application
        if (MyApplication.locationDisplay==null) {
            MyApplication.locationDisplay = locationDisplay

            val intent = Intent(this,LocationForegroundService::class.java)
            startService(intent)

            bindService(intent,connection,Context.BIND_AUTO_CREATE)
        }
        val bindIntent = Intent(this,LocationForegroundService::class.java)
        bindService(bindIntent,connection,Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}
