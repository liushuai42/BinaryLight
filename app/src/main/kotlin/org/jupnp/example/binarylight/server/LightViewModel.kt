package org.jupnp.example.binarylight.server

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.ViewModelInitializer
import org.jupnp.android.AndroidUpnpService
import org.jupnp.binding.annotations.AnnotationLocalServiceBinder
import org.jupnp.example.binarylight.R
import org.jupnp.example.binarylight.common.AndroidUpnpServiceViewModel
import org.jupnp.example.binarylight.common.SwitchPower
import org.jupnp.internal.compat.java.beans.PropertyChangeEvent
import org.jupnp.internal.compat.java.beans.PropertyChangeListener
import org.jupnp.model.DefaultServiceManager
import org.jupnp.model.meta.DeviceDetails
import org.jupnp.model.meta.DeviceIdentity
import org.jupnp.model.meta.Icon
import org.jupnp.model.meta.LocalDevice
import org.jupnp.model.meta.LocalService
import org.jupnp.model.meta.ManufacturerDetails
import org.jupnp.model.meta.ModelDetails
import org.jupnp.model.types.UDADeviceType
import org.jupnp.model.types.UDAServiceType

class LightViewModel(
    private val context: Application,
    private val upnpService: AndroidUpnpService,
) : AndroidViewModel(context) {
    private val udn = context.uniqueSystemIdentifier()

    val initialized = MutableLiveData<Boolean>(false)

    val status = MutableLiveData<Boolean>()

    fun setTarget(newValue: Boolean) {
        val switchPower = getSwitchPowerService() ?: return

        switchPower
            .manager
            .implementation
            .target = newValue
    }

    private fun setLightbulb(value: Boolean) {
        status.postValue(value)
    }

    override fun onCleared() {
        unregisterPropertyChangeListener()

        super.onCleared()
    }

    // region Upnp
    private val propertyChangeListener = object : PropertyChangeListener {
        override fun propertyChange(event: PropertyChangeEvent) {
            Log.d(
                TAG,
                "propertyChange: event(${event.propertyName}): ${event.oldValue} -> ${event.newValue}"
            )
            if (event.propertyName == "Status") {
                Log.d(TAG, "Turning light : ${event.newValue}")
                setLightbulb(event.newValue as Boolean)
            }
        }
    }

    private fun unregisterPropertyChangeListener() {
        val service = getSwitchPowerService() ?: return
        service.manager
            .implementation
            .propertyChangeSupport
            .removePropertyChangeListener(propertyChangeListener)
    }

    fun setup() {
        var switchPowerService = getSwitchPowerService()
        if (switchPowerService == null) {
            runCatching {
                val binaryLightDevice = createDevice()
                upnpService.registry.addDevice(binaryLightDevice)
                switchPowerService = getSwitchPowerService()
            }.onFailure {
                Log.e(TAG, "Creating BinaryLight device failed", it)

                Toast.makeText(
                    context, R.string.create_device_failed,
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            requireNotNull(switchPowerService) {
                "switchPowerService is null"
            }

            // Obtain the state of the power switch and update the UI
            setLightbulb(
                switchPowerService
                    .manager
                    .implementation
                    .status
            )

            // Start monitoring the power switch
            switchPowerService
                .manager
                .propertyChangeSupport
                .addPropertyChangeListener(propertyChangeListener)
        }

        initialized.value = true
    }

    private fun getSwitchPowerService(): LocalService<SwitchPower>? {
        val binaryLightDevice = upnpService.registry.getLocalDevice(udn, true) ?: return null
        return binaryLightDevice.findService(
            UDAServiceType("SwitchPower", 1)
        ) as LocalService<SwitchPower>?
    }

    private fun createDevice(): LocalDevice {
        val type = UDADeviceType("BinaryLight", 1)
        val details = DeviceDetails(
            "Friendly Binary Light",
            ManufacturerDetails("ACME"),
            ModelDetails("Android Light", "A light with on/off switch.", "v1")
        )

        val service: LocalService<SwitchPower> =
            AnnotationLocalServiceBinder().read(SwitchPower::class.java) as LocalService<SwitchPower>

        service.manager = DefaultServiceManager(service, SwitchPower::class.java)

        return LocalDevice(DeviceIdentity(udn), type, details, createDefaultDeviceIcon(), service)
    }

    private fun createDefaultDeviceIcon(): Icon = Icon(
        "image/png",
        144,
        144,
        8,
        "assets:///android_asset/icon_light_bulb.png", // fake as schema=assets, otherwise it will fails
        context.assets.open("icon_light_bulb.png")
    )
    // endregion

    companion object {
        private const val TAG = "LightViewModel"

        val INITIALIZER = ViewModelInitializer(LightViewModel::class) {
            val application = get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY)!!
            val upnpService = get(AndroidUpnpServiceViewModel.UPNP_SERVICE_KEY)!!
            LightViewModel(application, upnpService)
        }
    }
}