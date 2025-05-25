package org.jupnp.example.binarylight.client

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.ViewModelInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jupnp.android.AndroidUpnpService
import org.jupnp.example.binarylight.client.bean.DeviceDisplay
import org.jupnp.example.binarylight.common.AndroidUpnpServiceViewModel
import org.jupnp.model.meta.Device
import org.jupnp.model.meta.LocalDevice
import org.jupnp.model.meta.RemoteDevice
import org.jupnp.registry.Registry
import org.jupnp.registry.RegistryListener

class BrowserViewModel(
    private val context: Application,
    private val upnpService: AndroidUpnpService
) : AndroidViewModel(context) {

    val initialized = MutableLiveData<Boolean>(false)

    val deviceList = MutableLiveData<List<DeviceDisplay>>()

    override fun onCleared() {
        removeRegistryListener()
        super.onCleared()
    }

    // region Upnp
    fun setup() {
        // Get ready for future device advertisements
        upnpService.registry.addListener(registryListener)

        deviceList.postValue(
            upnpService.registry.devices.map {
                DeviceDisplay(it)
            }
        )

        // Search asynchronously for all devices, they will respond soon
        upnpService.controlPoint.search()

        initialized.value = true
    }

    private val registryListener = object : RegistryListener {
        override fun remoteDeviceDiscoveryStarted(registry: Registry?, device: RemoteDevice) {

        }

        override fun remoteDeviceDiscoveryFailed(
            registry: Registry?, device: RemoteDevice, ex: Exception?
        ) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    ("Discovery failed of '" + device.displayString + "': " + (ex?.toString()
                        ?: "Couldn't retrieve device/service descriptors")),
                    Toast.LENGTH_LONG
                ).show()

                deviceRemoved(device)
            }
        }

        override fun remoteDeviceAdded(registry: Registry?, device: RemoteDevice) {
            deviceAdded(device)
        }

        override fun remoteDeviceUpdated(registry: Registry?, device: RemoteDevice) {

        }

        override fun remoteDeviceRemoved(registry: Registry?, device: RemoteDevice) {
            deviceRemoved(device)
        }

        override fun localDeviceAdded(registry: Registry?, device: LocalDevice) {
            deviceAdded(device)
        }

        override fun localDeviceRemoved(registry: Registry?, device: LocalDevice) {
            deviceRemoved(device)
        }

        override fun beforeShutdown(registry: Registry?) {

        }

        override fun afterShutdown() {

        }

        private fun deviceAdded(device: Device<*, *, *>) {
            val deviceDisplay = DeviceDisplay(device)
            val oldList = deviceList.value ?: emptyList()
            deviceList.postValue(oldList - deviceDisplay + deviceDisplay)
        }

        private fun deviceRemoved(device: Device<*, *, *>) {
            val deviceDisplay = DeviceDisplay(device)
            val oldList = deviceList.value ?: return
            deviceList.postValue(oldList - deviceDisplay)
        }
    }

    private fun removeRegistryListener() {
        upnpService.registry.removeListener(registryListener)
    }
    // endregion

    companion object {
        val INITIALIZER = ViewModelInitializer(BrowserViewModel::class) {
            val application = get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY)!!
            val upnpService = get(AndroidUpnpServiceViewModel.UPNP_SERVICE_KEY)!!
            BrowserViewModel(application, upnpService)
        }
    }
}
