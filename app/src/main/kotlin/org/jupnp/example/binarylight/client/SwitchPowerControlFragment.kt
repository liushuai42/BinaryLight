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
import androidx.navigation.navGraphViewModels
import org.jupnp.example.binarylight.R
import org.jupnp.example.binarylight.common.AndroidUpnpServiceViewModel
import org.jupnp.example.binarylight.databinding.FragmentLightBinding
import org.jupnp.model.types.UDN
import kotlin.getValue

class SwitchPowerControlFragment : Fragment(R.layout.fragment_switch_power_control) {

    private val upnpService: AndroidUpnpServiceViewModel by navGraphViewModels(R.id.nav_browser)

    private val viewModel: SwitchPowerControlViewModel by viewModels(
        extrasProducer = {
            MutableCreationExtras().apply {
                set(
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY,
                    requireContext().applicationContext as Application
                )

                set(AndroidUpnpServiceViewModel.UPNP_SERVICE_KEY, upnpService)

                val udn = UDN.valueOf(requireArguments().getString(EXTRA_UDN_KEY)!!)
                set(SwitchPowerControlViewModel.UDN_KEY, udn)
            }
        },
        factoryProducer = {
            ViewModelProvider.Factory.from(SwitchPowerControlViewModel.INITIALIZER)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentLightBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.guidelineTop.setGuidelineBegin(systemBars.top)
            insets
        }

        viewModel.status.observe(viewLifecycleOwner) {
            binding.root.isSelected = it
        }

        binding.root.setOnClickListener {
            viewModel.toggleTarget()
        }

        viewModel.initialized.observe(viewLifecycleOwner) { initialized ->
            if (!initialized) {
                viewModel.setup()
            }
        }
    }

    companion object {
        const val EXTRA_UDN_KEY = "extra_udn"
    }
}