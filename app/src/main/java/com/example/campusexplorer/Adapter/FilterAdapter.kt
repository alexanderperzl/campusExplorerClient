package com.example.campusexplorer.Adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import com.example.campusexplorer.R

class FilterAdapter(private val myDataset: Array<String>) :
    RecyclerView.Adapter<FilterAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val switch: Switch) : RecyclerView.ViewHolder(switch)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterAdapter.MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_list_item, parent, false)
        val switch = view.findViewById<Switch>(R.id.toggle)

        return MyViewHolder(switch)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.switch.text = myDataset[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}