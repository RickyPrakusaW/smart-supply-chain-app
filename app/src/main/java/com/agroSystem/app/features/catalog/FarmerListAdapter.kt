package com.agroSystem.app.features.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Farmer

class FarmerListAdapter(
    private var farmers: List<Farmer>,
    private val onFarmerClick: (Farmer) -> Unit
) : RecyclerView.Adapter<FarmerListAdapter.FarmerViewHolder>() {

    fun updateData(newFarmers: List<Farmer>) {
        this.farmers = newFarmers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_farmer_card, parent, false)
        return FarmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FarmerViewHolder, position: Int) {
        holder.bind(farmers[position])
    }

    override fun getItemCount(): Int = farmers.size

    inner class FarmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardFarmer: View = itemView.findViewById(R.id.card_farmer)
        private val imageIllustration: ImageView = itemView.findViewById(R.id.image_illustration)
        private val textName: TextView = itemView.findViewById(R.id.text_name)
        private val textRating: TextView = itemView.findViewById(R.id.text_rating)
        private val textLocation: TextView = itemView.findViewById(R.id.text_location)
        private val textDistance: TextView = itemView.findViewById(R.id.text_distance)
        private val tag1: TextView = itemView.findViewById(R.id.tag1)
        private val tag2: TextView = itemView.findViewById(R.id.tag2)
        private val tag3: TextView = itemView.findViewById(R.id.tag3)

        fun bind(farmer: Farmer) {
            imageIllustration.setImageResource(farmer.imageResId)
            textName.text = farmer.name
            textRating.text = farmer.rating
            textLocation.text = farmer.location
            textDistance.text = "${farmer.distance} dari lokasi Anda"

            val tags = farmer.productsList.split(",")
            tag1.visibility = View.GONE
            tag2.visibility = View.GONE
            tag3.visibility = View.GONE

            if (tags.isNotEmpty() && tags[0].isNotBlank()) {
                tag1.text = tags[0].trim()
                tag1.visibility = View.VISIBLE
            }
            if (tags.size > 1 && tags[1].isNotBlank()) {
                tag2.text = tags[1].trim()
                tag2.visibility = View.VISIBLE
            }
            if (tags.size > 2 && tags[2].isNotBlank()) {
                tag3.text = tags[2].trim()
                tag3.visibility = View.VISIBLE
            }

            cardFarmer.setOnClickListener { onFarmerClick(farmer) }
        }
    }
}
