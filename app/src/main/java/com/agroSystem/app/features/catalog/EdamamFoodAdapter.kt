package com.agroSystem.app.features.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

data class EdamamResponse(
    @com.google.gson.annotations.SerializedName("hints") val hints: List<EdamamHint>?
)

data class EdamamHint(
    @com.google.gson.annotations.SerializedName("food") val food: EdamamFood?
)

data class EdamamFood(
    @com.google.gson.annotations.SerializedName("foodId") val foodId: String?,
    @com.google.gson.annotations.SerializedName("label") val label: String?,
    @com.google.gson.annotations.SerializedName("category") val category: String?,
    @com.google.gson.annotations.SerializedName("image") val image: String?,
    @com.google.gson.annotations.SerializedName("nutrients") val nutrients: EdamamNutrients?
)

data class EdamamNutrients(
    @com.google.gson.annotations.SerializedName("ENERC_KCAL") val ENERC_KCAL: Double? = 0.0,
    @com.google.gson.annotations.SerializedName("PROCNT") val PROCNT: Double? = 0.0,
    @com.google.gson.annotations.SerializedName("FAT") val FAT: Double? = 0.0,
    @com.google.gson.annotations.SerializedName("CHOCDF") val CHOCDF: Double? = 0.0
)

class EdamamFoodAdapter(
    private var items: List<EdamamHint>,
    private val onItemClick: (EdamamHint) -> Unit
) : RecyclerView.Adapter<EdamamFoodAdapter.ViewHolder>() {

    fun updateItems(newItems: List<EdamamHint>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_edamam_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hint = items[position]
        holder.bind(hint)
        holder.itemView.setOnClickListener {
            onItemClick(hint)
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageFood: ImageView = view.findViewById(R.id.image_food)
        private val textTitle: TextView = view.findViewById(R.id.text_title)
        private val textCategory: TextView = view.findViewById(R.id.text_category)
        private val textNutrients: TextView = view.findViewById(R.id.text_nutrients)

        fun bind(hint: EdamamHint) {
            val food = hint.food ?: return
            textTitle.text = food.label ?: "-"
            textCategory.text = food.category ?: "Umum"

            val nutrients = food.nutrients
            val df = DecimalFormat("#.#")
            val kcal = df.format(nutrients?.ENERC_KCAL ?: 0.0)
            val prot = df.format(nutrients?.PROCNT ?: 0.0)
            val fat = df.format(nutrients?.FAT ?: 0.0)
            val carb = df.format(nutrients?.CHOCDF ?: 0.0)

            textNutrients.text = "Kalori: ${kcal} kcal | Prot: ${prot}g | Lemak: ${fat}g | Karbo: ${carb}g"

            // Safe self-contained image loader from URL
            loadImageFromUrl(food.image, imageFood)
        }

        private fun loadImageFromUrl(urlStr: String?, imageView: ImageView) {
            if (urlStr.isNullOrEmpty()) {
                imageView.setImageResource(R.drawable.sayuran)
                return
            }

            val secureUrlStr = if (urlStr.startsWith("http://")) {
                urlStr.replace("http://", "https://")
            } else {
                urlStr
            }

            // Bind to lifecycle scope of view to avoid leaks
            val lifecycleOwner = itemView.findViewTreeLifecycleOwner()
            val scope = lifecycleOwner?.lifecycleScope ?: kotlinx.coroutines.GlobalScope

            scope.launch(Dispatchers.IO) {
                try {
                    val connection = URL(secureUrlStr).openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input = connection.inputStream
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap)
                        } else {
                            imageView.setImageResource(R.drawable.sayuran)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        imageView.setImageResource(R.drawable.sayuran)
                    }
                }
            }
        }
    }
}
