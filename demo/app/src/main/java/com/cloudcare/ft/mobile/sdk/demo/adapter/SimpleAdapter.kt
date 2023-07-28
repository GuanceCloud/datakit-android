package com.cloudcare.ft.mobile.sdk.demo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloudcare.ft.mobile.sdk.demo.R

data class ListItem(val name: String, val description: String, val imageResourceId: Int)
class SimpleAdapter(private val dataList: List<ListItem>, val itemClick: OnItemClickListener) :
    RecyclerView.Adapter<SimpleAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(data: String)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.index_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dataItem = dataList[position]
        holder.bind(dataItem)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.index_item_name)
        private val description: TextView = itemView.findViewById(R.id.index_item_description)
        private val image: ImageView = itemView.findViewById(R.id.index_item_image)

        fun bind(data: ListItem) {
            image.setImageResource(data.imageResourceId)
            name.text = data.name
            description.text = data.description
            itemView.setOnClickListener {
                itemClick.onItemClick(data.name)
            }
        }

    }
}
