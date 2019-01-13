package com.example.campusexplorer.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import com.example.campusexplorer.R
import com.example.campusexplorer.model.Event
import com.example.campusexplorer.model.Lecture

class RoomDetailAdapter(private val myDataset: List<Lecture>) :
    RecyclerView.Adapter<RoomDetailAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val event_name = view.findViewById<TextView>(R.id.event_name)
        val event_times = view.findViewById<TextView>(R.id.event_time)
        val event_department = view.findViewById<TextView>(R.id.event_department)
        val event_faculty = view.findViewById<TextView>(R.id.event_faculty)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomDetailAdapter.MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.room_detail_list_item, parent, false)
        return MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.event_name.text = myDataset[position].name
        holder.event_faculty.text = myDataset[position].faculty
        holder.event_department.text = myDataset[position].link


        var list : List<Event> = myDataset[position].events
        val eventTimes = list.fold("") { accumulator,item -> accumulator + "${item.cycle}, ${item.dayOfWeek}, ${item.time} \n"}

        holder.event_times.text = eventTimes
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}