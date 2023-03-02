package com.syncdev.geofence.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.syncdev.geofence.Destination
import com.syncdev.geofence.databinding.DestinationListItemBinding

class DestinationAdapter: ListAdapter<Destination, DestinationAdapter.DestinationViewHolder>(
    DiffCallback()
) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = DestinationListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DestinationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val item=getItem(position)
        holder.bind(item)
    }

    class DestinationViewHolder(private val binding: DestinationListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Destination) {
            binding.apply {
                tvAddress.text=item.address
                tvStatus.text=item.status
                imgStatusIcon.setImageDrawable(ContextCompat.getDrawable(itemView.context,item.statusIcon))
            }
        }
    }

}


class DiffCallback : DiffUtil.ItemCallback<Destination>() {
    override fun areItemsTheSame(oldItem: Destination, newItem: Destination): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Destination, newItem: Destination): Boolean {
        return oldItem == newItem
    }
}