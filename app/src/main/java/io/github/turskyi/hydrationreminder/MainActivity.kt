package io.github.turskyi.hydrationreminder

import android.content.*
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.turskyi.hydrationreminder.sync.ReminderTasks
import io.github.turskyi.hydrationreminder.sync.ReminderUtilities
import io.github.turskyi.hydrationreminder.sync.WaterReminderIntentService
import io.github.turskyi.hydrationreminder.utilities.NotificationUtils
import io.github.turskyi.hydrationreminder.utilities.PreferenceUtilities
import io.github.turskyi.hydrationreminder.utilities.PreferenceUtilities.getChargingReminderCount
import io.github.turskyi.hydrationreminder.utilities.PreferenceUtilities.getWaterCount

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var mWaterCountDisplay: TextView? = null
    private var mChargingCountDisplay: TextView? = null
    private var mChargingImageView: ImageView? = null
    private var mToast: Toast? = null
    var mChargingReceiver: ChargingBroadcastReceiver? = null
    var mChargingIntentFilter: IntentFilter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /** Get the views  */
        mWaterCountDisplay = findViewById<View>(R.id.tv_water_count) as TextView
        mChargingCountDisplay = findViewById<View>(R.id.tv_charging_reminder_count) as TextView
        mChargingImageView = findViewById<View>(R.id.iv_power_increment) as ImageView
        /** Set the original values in the UI  */
        updateWaterCount()
        updateChargingReminderCount()
        ReminderUtilities.scheduleChargingReminder(this)
        /** Setup the shared preference listener  */
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)

        /*
         * Setup and register the broadcast receiver
         */mChargingIntentFilter = IntentFilter()
        mChargingReceiver = ChargingBroadcastReceiver()
        mChargingIntentFilter?.addAction(Intent.ACTION_POWER_CONNECTED)
        mChargingIntentFilter?.addAction(Intent.ACTION_POWER_DISCONNECTED)

//        for testing proposes only
        mChargingCountDisplay?.setOnClickListener {
            NotificationUtils.remindUserBecauseCharging(this)
        }
    }

    override fun onResume() {
        super.onResume()
        /** Determine the current charging state  */
        //  Checking if we are on Android M or later, if so...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /* Getting a BatteryManager instance using getSystemService() */
            val batteryManager: BatteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
            // Call isCharging on the battery manager and pass the result on to your show
            // charging method
            showCharging(batteryManager.isCharging || batteryManager.equals(BatteryManager.BATTERY_PLUGGED_USB))
        } else {
            // If user is not on M+, then...

            // Create a new intent filter with the action ACTION_BATTERY_CHANGED. This is a
            // sticky broadcast that contains a lot of information about the battery state.
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            //  Set a new Intent object equal to what is returned by registerReceiver, passing in null
            // for the receiver. Pass in this intent filter as well. Passing in null means that we're
            // getting the current state of a sticky broadcast - the intent returned will contain the
            // battery information we need.
            val currentBatteryStatusIntent = registerReceiver(null, intentFilter)
            // Getting the integer extra BatteryManager.EXTRA_STATUS. Checking if it matches
            // BatteryManager.BATTERY_STATUS_CHARGING or BatteryManager.BATTERY_STATUS_FULL. This means
            // the battery is currently charging.
            val batteryStatus =
                currentBatteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                    batteryStatus == BatteryManager.BATTERY_STATUS_FULL
            //  Update the UI using your showCharging method
            showCharging(isCharging)
        }
        /** Register the receiver for future state changes  */
        registerReceiver(mChargingReceiver, mChargingIntentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mChargingReceiver)
    }

    /**
     * Updates the TextView to display the new water count from SharedPreferences
     */
    private fun updateWaterCount() {
        val waterCount = getWaterCount(this)
        mWaterCountDisplay?.text = waterCount.toString()
    }

    /**
     * Updates the TextView to display the new charging reminder count from SharedPreferences
     */
    private fun updateChargingReminderCount() {
        val chargingReminders = getChargingReminderCount(this)
        val formattedChargingReminders = resources.getQuantityString(
            R.plurals.charge_notification_count, chargingReminders, chargingReminders
        )
        mChargingCountDisplay?.text = formattedChargingReminders
    }

    /**
     * Adds one to the water count and shows a toast
     */
    fun incrementWater(view: View?) {
        if (mToast != null) mToast!!.cancel()
        mToast = Toast.makeText(this, R.string.water_chug_toast, Toast.LENGTH_SHORT)
        mToast?.show()
        val incrementWaterCountIntent = Intent(this, WaterReminderIntentService::class.java)
        incrementWaterCountIntent.action = ReminderTasks.ACTION_INCREMENT_WATER_COUNT
        WaterReminderIntentService().enqueueWork(this, incrementWaterCountIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        /** Cleanup the shared preference listener  */
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * This is a listener that will update the UI when the water count or charging reminder counts
     * change
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (PreferenceUtilities.KEY_WATER_COUNT == key) {
            updateWaterCount()
        } else if (PreferenceUtilities.KEY_CHARGING_REMINDER_COUNT == key) {
            updateChargingReminderCount()
        }
    }

    private fun showCharging(isCharging: Boolean) {
        if (isCharging) {
            mChargingImageView?.setImageResource(R.drawable.ic_power_pink_80px)
        } else {
            mChargingImageView?.setImageResource(R.drawable.ic_power_grey_80px)
        }
    }

    inner class ChargingBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            val isCharging: Boolean = action == Intent.ACTION_POWER_CONNECTED
            showCharging(isCharging)
        }
    }
}