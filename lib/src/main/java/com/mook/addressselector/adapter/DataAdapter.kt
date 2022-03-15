package com.mook.addressselector.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mook.addressselector.R
import com.mook.addressselector.databinding.ItemLayoutBinding
import com.mook.addressselector.model.AddressBean


/**
 * Created by xujianliu.
 * Date: 2022/2/13.
 */
open class DataAdapter<T : AddressBean>(
    val data: MutableList<T> = mutableListOf(),
    val callback: (T) -> Unit
) : RecyclerView.Adapter<DataAdapter.MyViewHolder>() {
    private val TAG = "DataAdapter"
    private var selectedPosition = -1

    class MyViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemId(position: Int) = position.toLong()

    fun setData(data: List<T>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        if (position != selectedPosition) {
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.run {
            val address = data[position]
            name.text = address.name
            choosedImg.visibility = if (selectedPosition == position) View.VISIBLE else View.GONE
            llItem.setOnClickListener {
                val clickPosition = holder.adapterPosition
                name.setTextColor(name.context.getColor(if (selectedPosition == clickPosition) R.color.color_35C88E else R.color.color_FF3A3A3A))
                selectedPosition = clickPosition
                notifyDataSetChanged()
                callback(address)
                Log.i(TAG, "position=$clickPosition\taddress=${address.name}")
            }
        }
    }

    override fun getItemCount(): Int = data.size

}