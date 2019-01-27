package com.example.campusexplorer.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.example.campusexplorer.R
import com.example.campusexplorer.filter.FilterData
import com.example.campusexplorer.filter.FilterObject
import com.example.campusexplorer.util.PinColor

class FilterAdapter(private val myDataset: List<FilterObject>, private val context: Context) :
    RecyclerView.Adapter<FilterAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val switch = view.findViewById<Switch>(R.id.toggle)


    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterAdapter.MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_list_item, parent, false)


        return MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.switch.text = myDataset[position].name
        holder.switch.isChecked = myDataset[position].active
        System.out.println(myDataset[position].name)
        val color = PinColor.eventTypeToUiColor(myDataset[position].name, context)
        if (color != null) {
            if (!holder.switch.isChecked) setColorOfHolder(holder, Color.WHITE) else setColorOfHolder(holder, color)
        }
        holder.switch.setOnCheckedChangeListener { switch, isChecked ->
            FilterData.setValue(isChecked, switch.text.toString())
            val color = PinColor.eventTypeToUiColor(myDataset[position].name, context)
            if (color != null) {
                if (!isChecked) setColorOfHolder(holder, Color.WHITE) else setColorOfHolder(holder, color)
            }
        }
    }

    private fun setColorOfHolder(holder: MyViewHolder, color: Int) {
        holder.switch.thumbDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        holder.switch.trackDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}