package org.jupnp.example.binarylight.server

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.MutableCreationExtras
import org.jupnp.example.binarylight.R
import org.jupnp.example.binarylight.common.AndroidUpnpServiceViewModel
import org.jupnp.example.binarylight.databinding.FragmentLightBinding


class LightFragment : Fragment(R.layout.fragment_light) {

    private val upnpService: AndroidUpnpServiceViewModel by viewModels()

    private val viewModel: LightViewModel by viewModels(
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
            ViewModelProvider.Factory.from(LightViewModel.INITIALIZER)
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
            viewModel.setTarget(!it.isSelected)
        }

        viewModel.initialized.observe(viewLifecycleOwner) { initialized ->
            if (!initialized) {
                viewModel.setup()
            }
        }
    }
}