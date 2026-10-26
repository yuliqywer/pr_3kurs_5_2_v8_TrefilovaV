package com.example.pr5_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class base_fragment : Fragment() {

    private lateinit var recipesContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.base_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recipesContainer = view.findViewById(R.id.recipesContainer)

        val btnBackToApi = view.findViewById<Button>(R.id.btnBackToApi)
        btnBackToApi.setOnClickListener {
            (activity as MainActivity).replaceFragment(api_fragment())
        }
    }

    override fun onResume() {
        super.onResume()
        loadRecipes()
    }

    private fun loadRecipes() {
        val db = AppDatabase.getDatabase(requireContext())
        Thread {
            val recipes = db.recipeDao().getAll()
            requireActivity().runOnUiThread {
                recipesContainer.removeAllViews()

                if (recipes.isEmpty()) {
                    val tvEmpty = TextView(requireContext())
                    tvEmpty.text = "Список пуст"
                    tvEmpty.setTextColor(android.graphics.Color.BLACK)
                    recipesContainer.addView(tvEmpty)
                } else {
                    for (recipe in recipes) {
                        val itemLayout = LinearLayout(requireContext())
                        itemLayout.orientation = LinearLayout.HORIZONTAL

                        itemLayout.gravity = android.view.Gravity.TOP
                        itemLayout.setPadding(0, 0, 0, 32)

                        val textLayout = LinearLayout(requireContext())
                        textLayout.orientation = LinearLayout.VERTICAL

                        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        textLayout.layoutParams = params

                        val tvTitle = TextView(requireContext())
                        tvTitle.text = recipe.title
                        tvTitle.textSize = 18f
                        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
                        tvTitle.setTextColor(android.graphics.Color.BLACK)

                        val tvIngredients = TextView(requireContext())
                        tvIngredients.text = "Ингредиенты:\n${recipe.ingredients}"
                        tvIngredients.textSize = 14f
                        tvIngredients.setTextColor(android.graphics.Color.BLACK)

                        val tvTime = TextView(requireContext())
                        tvTime.text = "\nВремя: ${recipe.time} мин"
                        tvTime.textSize = 14f
                        tvTime.setTextColor(android.graphics.Color.BLACK)

                        textLayout.addView(tvTitle)
                        textLayout.addView(tvIngredients)
                        textLayout.addView(tvTime)

                        val btnDelete = Button(requireContext())
                        btnDelete.text = "✖"
                        btnDelete.textSize = 14f
                        btnDelete.setTextColor(android.graphics.Color.WHITE)

                        val size = (45 * resources.displayMetrics.density).toInt()
                        val btnParams = LinearLayout.LayoutParams(size, size)
                        btnParams.setMargins(16, 0, 0, 0)
                        btnDelete.layoutParams = btnParams

                        btnDelete.background = resources.getDrawable(R.drawable.btn_rounded, null)

                        btnDelete.setOnClickListener {
                            deleteRecipe(recipe)
                        }

                        itemLayout.addView(textLayout)
                        itemLayout.addView(btnDelete)

                        recipesContainer.addView(itemLayout)
                    }
                }
            }
        }.start()
    }

    private fun deleteRecipe(recipe: Recipe) {
        val db = AppDatabase.getDatabase(requireContext())
        Thread {
            db.recipeDao().delete(recipe)
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Удалено", Toast.LENGTH_SHORT).show()
                loadRecipes()
            }
        }.start()
    }
}