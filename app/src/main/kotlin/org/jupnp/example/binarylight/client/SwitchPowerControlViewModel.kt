package org.jupnp.example.binarylight.client

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.ViewModelInitializer
import kotlinx.coroutines.launch
import org.jupnp.android.AndroidUpnpService
import org.jupnp.controlpoint.ActionCallback
import org.jupnp.controlpoint.SubscriptionCallback
import org.jupnp.example.binarylight.common.AndroidUpnpServiceViewModel
import org.jupnp.model.action.ActionInvocation
import org.jupnp.model.gena.CancelReason
import org.jupnp.model.gena.GENASubscription
import org.jupnp.model.message.UpnpResponse
import org.jupnp.model.meta.Action
import org.jupnp.model.meta.Service
import org.jupnp.model.types.InvalidValueException
import org.jupnp.model.types.UDAServiceId
import org.jupnp.model.types.UDN
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SwitchPowerControlViewModel(
    private val udn: UDN,
    private val upnpService: AndroidUpnpService
) : ViewModel() {
    val initialized = MutableLiveData<Boolean>(false)

    val status = MutableLiveData<Boolean>()

    override fun onCleared() {
        subscriptionCallback = null // used to end subscription
        super.onCleared()
    }

    // region Upnp
    fun setup() {
        subscribe()

        initialized.value = true
    }

    fun toggleTarget() {
        viewModelScope.launch {
            val newValue = !(status.value ?: false)
            setTarget(newValue)
            status.value = newValue
        }
    }

    /**
     * SetTarget
     *
     * @param value new target value
     */
    suspend fun setTarget(value: Boolean): Boolean = suspendCoroutine { cont ->
        val service = findService() ?: return@suspendCoroutine

        val action =
            ActionInvocation(service.getAction("SetTarget") as Action<Service<*, *>>).apply {
                try {
                    // Throws InvalidValueException if the value is of wrong type
                    setInput("NewTargetValue", value)
                } catch (e: InvalidValueException) {
                    Log.e(TAG, "Value type error", e)
                    throw e
                }
            }

        upnpService.controlPoint.execute(object : ActionCallback(action) {
            override fun success(invocation: ActionInvocation<*>) {
                require(invocation.outputMap.isEmpty())
                Log.d(TAG, "Successfully called action(SetTarget)!")
                cont.resume(value)
            }

            override fun failure(
                invocation: ActionInvocation<*>?,
                operation: UpnpResponse?,
                defaultMsg: String?
            ) {
                Log.d(TAG, "Failed called action(SetTarget)! msg=${defaultMsg}")
                cont.resumeWithException(Exception("$defaultMsg"))
            }
        })
    }

    /**
     * Get status from device
     *
     * Because we use [SubscriptionCallback] to sync with device's status, so we no need to call it manually
     */
    suspend fun getStatus(): Boolean = suspendCoroutine { cont ->
        val service = findService() ?: return@suspendCoroutine
        val action = ActionInvocation(service.getAction("GetStatus") as Action<Service<*, *>>)
        upnpService.controlPoint.execute(object : ActionCallback(action) {
            override fun success(invocation: ActionInvocation<*>) {
                require(invocation.outputMap.isNotEmpty())
                Log.d(TAG, "Successfully called action(GetStatus)! ")
                cont.resume(invocation.getOutput("ResultStatus").value as Boolean)
            }

            override fun failure(
                invocation: ActionInvocation<*>?,
                operation: UpnpResponse?,
                defaultMsg: String?
            ) {
                Log.d(TAG, "Failed called action(GetStatus)! msg=${defaultMsg}")
                cont.resumeWithException(Exception("$defaultMsg"))
            }
        })
    }

    private fun findService(): Service<*, *>? {
        return upnpService.registry
            .getDevice(udn, true)
            ?.findService(ID_SWITCH_POWER)
    }

    private var subscriptionCallback: SubscriptionCallback? = null
        set(value) {
            field?.end()
            field = value
        }

    /**
     * Subscribe device's events so we can sync status with it.
     */
    private fun subscribe() {
        val service = findService() ?: return
        val subscriptionCallback = object : SubscriptionCallback(service) {
            override fun failed(
                subscription: GENASubscription<*>?,
                responseStatus: UpnpResponse?,
                exception: java.lang.Exception?,
                defaultMsg: String?
            ) {
                Log.d(TAG, "SubscriptionCallback failed: $defaultMsg", exception)
            }

            override fun established(subscription: GENASubscription<*>?) {
                Log.d(TAG, "SubscriptionCallback established: ")
            }

            override fun ended(
                subscription: GENASubscription<*>?,
                reason: CancelReason?,
                responseStatus: UpnpResponse?
            ) {
                Log.d(TAG, "SubscriptionCallback ended: ")
            }

            override fun eventReceived(subscription: GENASubscription<*>) {
                Log.d(TAG, "SubscriptionCallback eventReceived: ")
                subscription.currentValues.forEach {
                    Log.d(TAG, "    ${it.key} -> ${it.value}")
                }

                subscription.currentValues["Status"]?.let { variable ->
                    status.postValue(variable.value as Boolean)
                }
            }

            override fun eventsMissed(
                subscription: GENASubscription<*>?,
                numberOfMissedEvents: Int
            ) {
                Log.d(TAG, "SubscriptionCallback eventsMissed: $numberOfMissedEvents")
            }
        }.also {
            this.subscriptionCallback = it
        }

        upnpService.controlPoint.execute(subscriptionCallback)
    }
    // endregion

    companion object {
        private const val TAG = "SwitchPowerControl"

        val UDN_KEY = object : CreationExtras.Key<UDN> {}

        val INITIALIZER = ViewModelInitializer(SwitchPowerControlViewModel::class) {
            val udn = get(UDN_KEY)!!
            val upnpService = get(AndroidUpnpServiceViewModel.UPNP_SERVICE_KEY)!!
            SwitchPowerControlViewModel(udn, upnpService)
        }

        val ID_SWITCH_POWER = UDAServiceId("SwitchPower")
    }
}