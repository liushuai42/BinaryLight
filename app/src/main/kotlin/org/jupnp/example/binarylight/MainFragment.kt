package org.jupnp.example.binarylight

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import org.jupnp.example.binarylight.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentMainBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.guidelineTop.setGuidelineBegin(systemBars.top)
            insets
        }

        binding.browser.setOnClickListener {
            it.findNavController().navigate(R.id.action_browser)
        }

        binding.light.setOnClickListener {
            it.findNavController().navigate(R.id.action_light)
        }
    }
}