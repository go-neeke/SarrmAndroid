package com.android.sarrm.service


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import android.os.PowerManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.android.kinpecauto.kinpecdriver.application.SarrmApplication
import com.android.sarrm.watchers.PhoneCallDetector
import java.util.*


class PhoneCallService() : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate")
        regsterGpsService()
        val timer = Timer()
        timer.schedule(timeTask, 0, 20000)
    }

    private val TAG = "PhoneCallService"

    constructor(parcel: Parcel) : this()

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy")
        timeTask.cancel()
        // 서비스 종료 시 1초 뒤 알람이 실행되도록 호출
        setAlarmTimer()
    }

    private fun setAlarmTimer() {
        val c: Calendar = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.add(Calendar.SECOND, 1)
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, c.timeInMillis, sender)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callDetactor = PhoneCallDetector(this)
        val tm = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        getSystemService(Context.LOCATION_SERVICE) as TelephonyService
        base.GetSystemService(TelephonyService)
            .Listen(callDetactor, PhoneStateListenerFlags.CallState)
        return START_STICKY
    }

    private fun regsterGpsService() {
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationListener = MainActivity.SpeedoActionListener()
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, mLocationListener)
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1f, mLocationListener)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private var timeTask: TimerTask = object : TimerTask() {
        override fun run() {
            whiteListCheck()
            if (lat == 0.0) {
                Log.e("location is 0.0", "lat : ${lat}\nlng : $lng")
                val location = if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else {
                    // TODO: Consider calling ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                if (location != null) {
                    Log.e("GpsService", "lat_ : ${location.latitude}\nlng_ : ${location.longitude}")
                    fetchDriverLocation(
                        DriverPreferences.getUserId(),
                        DriverPreferences.getPhoneNumber(),
                        location.latitude.toString(),
                        location.longitude.toString()
                    )
                } else {
                    /* Default Lat, Lng */
                    val lat = 35.183687
                    val lng = 129.10876190
                    Log.e("Default position : ", "lat : $lat\nlng : $lng")
                    fetchDriverLocation(
                        DriverPreferences.getUserId(),
                        DriverPreferences.getPhoneNumber(),
                        lat.toString(),
                        lng.toString()
                    )
                }
            } else {
                Log.e("onLocationChanged", "lat : $lat\nlng : $lng")
                fetchDriverLocation(
                    DriverPreferences.getUserId(),
                    DriverPreferences.getPhoneNumber(),
                    lat.toString(),
                    lng.toString()
                )
            }
        }
    }

    @SuppressLint("BatteryLife")
    fun whiteListCheck() {
        /**
         * 안드로이드 6.0 이상 (API23) 부터는 Doze모드가 추가됨.
         * 일정시간 화면이꺼진 상태로 디바이스를 이용하지 않을 시 일부 백그라운드 서비스 및 알림서비스가 제한됨.
         * 6.0이상의 버전이라면 화이트리스트에 등록이 됐는지 Check
         */
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = SarrmApplication.getInstance().applicationContext.packageName
        var whiteCheck = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /**
             * 등록이 되어있따면 TRUE
             * 등록이 안되있다면 FALSE
             */
            whiteCheck = powerManager.isIgnoringBatteryOptimizations(packageName)
            /** 만약 화이트리스트에 등록이 되지않았다면 등록을 해줍니다.  */
            if (!whiteCheck) {
                Log.e("화이트리스트", "화이트리스트에 등록 실행")
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                SarrmApplication.getInstance().applicationContext.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
            } else Log.e("화이트리스트", "화이트리스트에 등록되어져 있습니다.")
        }
    }
}