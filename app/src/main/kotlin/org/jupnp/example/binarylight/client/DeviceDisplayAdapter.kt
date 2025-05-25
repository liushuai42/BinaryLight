package org.jupnp.example.binarylight.client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.jupnp.example.binarylight.R
import org.jupnp.example.binarylight.client.bean.DeviceDisplay

typealias OnDeviceClickedListener = (DeviceDisplay) -> Unit

class DeviceDisplayAdapter(private val listener: OnDeviceClickedListener = {}) :
    RecyclerView.Adapter<DeviceDisplayHolder>() {
    private var items: List<DeviceDisplay> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceDisplayHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DeviceDisplayHolder(inflater.inflate(R.layout.simple_list_item_1, parent, false))
    }

    override fun onBindViewHolder(
        holder: DeviceDisplayHolder,
        position: Int
    ) {
        holder.textView.text = "${items[position]}"
        holder.itemView.setOnClickListener {
            val display = items[holder.bindingAdapterPosition]
            listener.invoke(display)
        }
    }

    override fun getItemCount() = items.size

    fun setItems(newItems: List<DeviceDisplay>) {
        val oldItems = items
        items = newItems

        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldItems.size

            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return oldItems[oldItemPosition] === newItems[newItemPosition]
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return oldItems[oldItemPosition] == newItems[newItemPosition]
            }

        }).dispatchUpdatesTo(this)
    }
}

class DeviceDisplayHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView = itemView as TextView
}
