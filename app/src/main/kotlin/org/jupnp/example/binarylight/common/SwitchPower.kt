package org.jupnp.example.binarylight.common

import android.util.Log
import org.jupnp.binding.annotations.UpnpAction
import org.jupnp.binding.annotations.UpnpInputArgument
import org.jupnp.binding.annotations.UpnpOutputArgument
import org.jupnp.binding.annotations.UpnpService
import org.jupnp.binding.annotations.UpnpServiceId
import org.jupnp.binding.annotations.UpnpServiceType
import org.jupnp.binding.annotations.UpnpStateVariable
import org.jupnp.internal.compat.java.beans.PropertyChangeSupport

@UpnpService(
    serviceId = UpnpServiceId("SwitchPower"),
    serviceType = UpnpServiceType(value = "SwitchPower", version = 1)
)
class SwitchPower {
    @get:UpnpAction(out = [UpnpOutputArgument(name = "RetTargetValue")])
    @set:UpnpAction
    @UpnpStateVariable(defaultValue = "0", sendEvents = false)
    var target: Boolean = false
        set(@UpnpInputArgument(name = "NewTargetValue") newValue) {
            val oldValue = field
            field = newValue
            status = newValue
            Log.d(TAG, "NewTargetValue: $newValue")

            // These have no effect on the UPnP monitoring but it's JavaBean compliant
            // propertyChangeSupport.firePropertyChange("status", oldValue, newValue)

            // This will send a UPnP event, it's the name of a state variable that sends events
            propertyChangeSupport.firePropertyChange("Status", oldValue, newValue)
        }

    @get:UpnpAction(out = [UpnpOutputArgument(name = "ResultStatus")])
    @UpnpStateVariable(defaultValue = "0")
    var status: Boolean = false
        private set

    val propertyChangeSupport = PropertyChangeSupport(this)

    companion object {
        private const val TAG = "SwitchPower"
    }
}