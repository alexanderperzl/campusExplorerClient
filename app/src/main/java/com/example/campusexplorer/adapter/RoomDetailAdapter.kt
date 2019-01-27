package com.example.campusexplorer.adapter

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.campusexplorer.R
import com.example.campusexplorer.model.Event
import com.example.campusexplorer.model.Lecture
import com.example.campusexplorer.model.Room
import kotlin.math.absoluteValue


class RoomDetailAdapter(private val myDataset : Triple<Room, List<Lecture>, Lecture?>) :
    RecyclerView.Adapter<RoomDetailAdapter.MyViewHolder>() {

    val TAG = "RoomDetailActivity"

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val event_name = view.findViewById<TextView>(R.id.event_name)
        val event_type = view.findViewById<TextView>(R.id.event_type)
        val event_times = view.findViewById<TextView>(R.id.event_time)
        val event_link = view.findViewById<TextView>(R.id.event_link)
        val event_faculty = view.findViewById<TextView>(R.id.event_faculty)
        val event_type_icon = view.findViewById<ImageView>(R.id.event_type_icon)
        val background = view.findViewById<ConstraintLayout>(R.id.item_container)
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
        val currentLecture : Lecture = myDataset.second[position]
        holder.event_name.text = currentLecture.name
        holder.event_type.text =currentLecture.type
        holder.event_faculty.text = currentLecture.faculty
        val linkText = Html.fromHtml("<a href=\"${currentLecture.link}\">LSF-Link</a>", Html.FROM_HTML_MODE_LEGACY)
        holder.event_link.text = linkText
        holder.event_link.movementMethod = LinkMovementMethod.getInstance()

        val eventIcon = getEventTypeIcon(currentLecture.type)
        holder.event_type_icon.setImageResource(eventIcon)
        val eventIconBackground = getEventTypeIconBackground(currentLecture.type)
        holder.event_type_icon.setBackgroundResource(eventIconBackground)
        if (myDataset.second.indexOf(currentLecture) < myDataset.second.indexOfFirst { lecture ->  lecture.events[0] == myDataset.third!!.events[0]} ){
            holder.background.alpha = 0.5f
        }
        if (currentLecture.events[0] == myDataset.third!!.events[0]){
            Log.d(TAG, "$currentLecture is the current third ${myDataset.third}")
            holder.background.setBackgroundResource(android.R.color.holo_green_dark)
        }

        var events : List<Event> = currentLecture.events.distinctBy {it.dayOfWeek to it.time}
        val eventTimes = events.fold("") { accumulator, item -> accumulator + "${item.cycle}, ${item.dayOfWeek}, ${item.time} \n"}.replaceAfterLast("t.", "")

        holder.event_times.text = eventTimes
    }

    private fun getEventTypeIcon(eventType: String): Int {
        return when(eventType) {
            "Vorlesung" -> R.drawable.icon_vorlesung
            "Übung" -> R.drawable.icon_uebung
            "Seminar" -> R.drawable.icon_seminar
            else -> R.drawable.icon_other
        }
    }

    private fun getEventTypeIconBackground(eventType: String): Int {
        return when (eventType) {
            "Vorlesung" -> R.drawable.circle_background_vorlesung
            "Übung" -> R.drawable.circle_background_uebung
            "Seminar" -> R.drawable.circle_background_seminar
            else -> R.drawable.circle_background_other
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.second.size
}