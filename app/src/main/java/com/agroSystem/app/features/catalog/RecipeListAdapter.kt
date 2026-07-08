package com.agroSystem.app.features.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Recipe
import com.google.android.material.card.MaterialCardView

class RecipeListAdapter(
    private var recipes: List<Recipe>,
    private val onAddIngredientsClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    fun updateData(newRecipes: List<Recipe>) {
        this.recipes = newRecipes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageIllustration: ImageView = itemView.findViewById(R.id.image_illustration)
        private val textTime: TextView = itemView.findViewById(R.id.text_time)
        private val textName: TextView = itemView.findViewById(R.id.text_name)
        private val textInfo: TextView = itemView.findViewById(R.id.text_info)
        private val btnAddIngredients: MaterialCardView = itemView.findViewById(R.id.btn_add_ingredients)

        fun bind(recipe: Recipe) {
            imageIllustration.setImageResource(recipe.imageResId)
            textTime.text = recipe.time
            textName.text = recipe.name
            textInfo.text = "${recipe.difficulty} • ★ ${recipe.rating}"

            btnAddIngredients.setOnClickListener { onAddIngredientsClick(recipe) }
        }
    }
}
