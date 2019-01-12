package com.example.campusexplorer.Adapter

class FilterAdapter : ListAdapter<${Model_Class}, ${NAME}.ItemViewholder>(DiffCallback())  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewholder {
        return ItemViewholder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.${Item_Layout_ID}, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ${NAME}.ItemViewholder, position: Int) {
        holder.bind(getItem(position))
    }

    class ItemViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ${Model_Class}) = with(itemView) {
            // TODO: Bind the data with View

            setOnClickListener {
                // TODO: Handle on click
            }
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<${Model_Class}>() {
    override fun areItemsTheSame(oldItem: ${Model_Class}?, newItem: ${Model_Class}?): Boolean {
        return oldItem?.id == newItem?.id
    }

    override fun areContentsTheSame(oldItem: ${Model_Class}?, newItem: ${Model_Class}?): Boolean {
        return oldItem == newItem
    }
}