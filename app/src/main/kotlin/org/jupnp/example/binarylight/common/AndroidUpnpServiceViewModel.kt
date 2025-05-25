package org.jupnp.example.binarylight.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import org.jupnp.UpnpService
import org.jupnp.UpnpServiceConfiguration
import org.jupnp.UpnpServiceImpl
import org.jupnp.android.AndroidRouter
import org.jupnp.android.AndroidUpnpService
import org.jupnp.android.AndroidUpnpServiceConfiguration
import org.jupnp.controlpoint.ControlPoint
import org.jupnp.protocol.ProtocolFactory
import org.jupnp.registry.Registry

class AndroidUpnpServiceViewModel(context: Application) :
    AndroidViewModel(context), AndroidUpnpService {
    val upnpService: UpnpService by lazy {
        object : UpnpServiceImpl(configuration) {
            override fun createRouter(
                protocolFactory: ProtocolFactory, registry: Registry?
            ) = AndroidRouter(configuration, protocolFactory, context)

            override fun shutdown() {
                // First have to remove the receiver, so Android won't complain about it leaking
                // when the main UI thread exits.
                (getRouter() as AndroidRouter).unregisterBroadcastReceiver()

                // Now we can concurrently run the Cling shutdown code, without occupying the
                // Android main UI thread. This will complete probably after the main UI thread
                // is done.
                super.shutdown(true)
            }
        }
    }

    init {
        upnpService.startup()
    }

    override fun onCleared() {
        upnpService.shutdown()
        super.onCleared()
    }

    // region AndroidUpnpService
    override fun get(): UpnpService = upnpService

    override fun getConfiguration(): UpnpServiceConfiguration {
        return AndroidUpnpServiceConfiguration()
    }

    override fun getRegistry(): Registry = upnpService.registry

    override fun getControlPoint(): ControlPoint = upnpService.controlPoint
    // endregion

    companion object {
        val UPNP_SERVICE_KEY = object : CreationExtras.Key<AndroidUpnpService> {}
    }
}