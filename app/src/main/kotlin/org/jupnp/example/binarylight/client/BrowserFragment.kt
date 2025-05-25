package org.jupnp.example.binarylight.client

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import org.jupnp.example.binarylight.R
import org.jupnp.example.binarylight.client.SwitchPowerControlFragment.Companion.EXTRA_UDN_KEY
import org.jupnp.example.binarylight.common.AndroidUpnpServiceViewModel
import org.jupnp.example.binarylight.databinding.FragmentBrowserBinding
import kotlin.getValue

class BrowserFragment : Fragment(R.layout.fragment_browser) {

    private val upnpService: AndroidUpnpServiceViewModel by navGraphViewModels(R.id.nav_browser)

    private val viewModel: BrowserViewModel by viewModels(
        extrasProducer = {
            MutableCreationExtras().apply {
                set(
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY,
                    requireContext().applicationContext as Application
                )

                set(AndroidUpnpServiceViewModel.UPNP_SERVICE_KEY, upnpService)
            }
        },
        factoryProducer = {
            ViewModelProvider.Factory.from(BrowserViewModel.INITIALIZER)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentBrowserBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val displayAdapter = DeviceDisplayAdapter { displayDevice ->
            if (displayDevice.hasSwitchPower) {
                findNavController().navigate(R.id.fragment_switch_power_control, Bundle().apply {
                    putString(EXTRA_UDN_KEY, displayDevice.udnString)
                })
            }
        }

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = displayAdapter
        }

        viewModel.deviceList.observe(viewLifecycleOwner) { deviceList ->
            displayAdapter.setItems(deviceList)

            binding.empty.visibility = if (deviceList.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.initialized.observe(viewLifecycleOwner) { initialized ->
            if (!initialized) {
                viewModel.setup()
            }
        }
    }
}