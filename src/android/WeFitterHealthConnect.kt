package com.wefitter.cordova.plugin

import com.google.gson.Gson
import com.wefitter.healthconnect.WeFitterHealthConnect
import com.wefitter.healthconnect.WeFitterHealthConnectError
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class WeFitterHealthConnect : CordovaPlugin() {

    private val weFitter by lazy { WeFitterHealthConnect(cordova.activity) }

    private lateinit var callbackContextForStatusListener: CallbackContext

    private class Config(
        val token: String,
        val apiUrl: String?,
        val startDate: String?,
        val notificationTitle: String?,
        val notificationText: String?,
        val notificationIcon: String?,
        val notificationChannelId: String?,
        val notificationChannelName: String?
    )

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        when (action) {
            "configure" -> {
                callbackContextForStatusListener = callbackContext
                configure(args)
                return true
            }

            "connect" -> {
                weFitter.connect()
                return true
            }

            "disconnect" -> {
                weFitter.disconnect()
                return true
            }

            "isConnected" -> {
                isConnected(callbackContext)
                return true
            }

            "isSupported" -> {
                isSupported(callbackContext)
                return true
            }

            else -> return false
        }
    }

    private fun configure(args: JSONArray) {
        val config = parseConfig(args)
        if (config === null) {
            sendPluginResult(PluginResult.Status.ERROR, "No (valid) config passed")
        } else {
            val statusListener = object : WeFitterHealthConnect.StatusListener {
                override fun onConfigured(configured: Boolean) {
                    val message = if (configured) "CONFIGURED" else "NOT_CONFIGURED"
                    sendPluginResult(PluginResult.Status.OK, message)
                }

                override fun onConnected(connected: Boolean) {
                    val message = if (connected) "CONNECTED" else "DISCONNECTED"
                    sendPluginResult(PluginResult.Status.OK, message)
                }

                override fun onError(error: WeFitterHealthConnectError) {
                    sendPluginResult(PluginResult.Status.ERROR, error.message)
                }
            }
            val notificationConfig = parseNotificationConfig(config)
            val startDate = parseStartDate(config)
            weFitter.configure(
                config.token,
                config.apiUrl,
                statusListener,
                notificationConfig,
                startDate
            )
        }
    }

    private fun isConnected(callbackContext: CallbackContext) {
        val connected = weFitter.isConnected()
        val result = PluginResult(PluginResult.Status.OK, connected)
        callbackContext.sendPluginResult(result)
    }

    private fun isSupported(callbackContext: CallbackContext) {
        val supported = weFitter.isSupported()
        val result = PluginResult(PluginResult.Status.OK, supported)
        callbackContext.sendPluginResult(result)
    }

    private fun sendPluginResult(status: PluginResult.Status, message: String) {
        sendPluginResult(PluginResult(status, message))
    }

    private fun sendPluginResult(result: PluginResult) {
        result.keepCallback = true
        callbackContextForStatusListener.sendPluginResult(result)
    }

    private fun parseConfig(args: JSONArray): Config? {
        return try {
            Gson().fromJson(args.getString(0), Config::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseNotificationConfig(config: Config): WeFitterHealthConnect.NotificationConfig {
        return WeFitterHealthConnect.NotificationConfig().apply {
            config.notificationTitle?.let { this.title = it }
            config.notificationText?.let { this.text = it }
            config.notificationIcon?.let {
                val resourceId = getResourceId(it)
                if (resourceId != 0) this.iconResourceId = resourceId
            }
            config.notificationChannelId?.let { this.channelId = it }
            config.notificationChannelName?.let { this.channelName = it }
        }
    }

    private fun parseStartDate(config: Config): Date? {
        val startDateString = config.startDate
        if (startDateString != null) {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                sdf.parse(startDateString)
            } catch (e: Exception) {
                sendPluginResult(
                    PluginResult.Status.ERROR,
                    "Passed start date `$startDateString` is not valid. Using default"
                )
                null
            }
        }
        return null
    }

    private fun getResourceId(resourceName: String): Int {
        val resources = cordova.activity.resources
        val packageName = cordova.activity.packageName
        var resourceId = resources.getIdentifier(resourceName, "mipmap", packageName)
        if (resourceId == 0) {
            resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
        }
        if (resourceId == 0) {
            resourceId = resources.getIdentifier(resourceName, "raw", packageName)
        }
        return resourceId
    }
}