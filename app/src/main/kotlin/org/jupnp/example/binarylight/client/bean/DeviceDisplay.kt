package org.jupnp.example.binarylight.client.bean

import org.jupnp.model.meta.Device
import org.jupnp.model.types.UDAServiceId

class DeviceDisplay(val device: Device<*, *, *>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceDisplay

        return device == other.device
    }

    val udnString: String
        get() = device.identity.udn.toString()

    val hasSwitchPower: Boolean
        get() = device.findService(UDAServiceId("SwitchPower")) != null

    override fun hashCode(): Int {
        return device.hashCode()
    }

    override fun toString(): String {
        val friendlyName = device.details?.friendlyName ?: device.displayString
        // Display a little star while the device is being loaded (see performance optimization earlier)
        return if (device.isFullyHydrated) {
            friendlyName
        } else {
            "$friendlyName *"
        }
    }
}