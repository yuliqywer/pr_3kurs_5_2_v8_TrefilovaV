package com.example.pr5_2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class api_fragment : Fragment() {

    private var foundTitle = ""
    private var foundIngredients = ""
    private var foundTime = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.api_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etQuery = view.findViewById<EditText>(R.id.etApiQuery)
        val btnSearch = view.findViewById<Button>(R.id.btnSearchApi)
        val tvResult = view.findViewById<TextView>(R.id.tvApiResult)
        val btnSave = view.findViewById<Button>(R.id.btnSaveToBase)
        val btnView = view.findViewById<Button>(R.id.btnViewCollection)

        btnSearch.setOnClickListener {
            val query = etQuery.text.toString().trim()
            if (query.isNotEmpty()) {
                searchRecipe(query, tvResult, btnSave)
            } else {
                Toast.makeText(requireContext(), "Введите запрос", Toast.LENGTH_SHORT).show()
            }
        }

        btnSave.setOnClickListener {
            if (foundTitle.isNotEmpty()) {
                val db = AppDatabase.getDatabase(requireContext())
                val recipe = Recipe(title = foundTitle, ingredients = foundIngredients, time = foundTime)
                Thread {
                    db.recipeDao().insert(recipe)
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Сохранено!", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            } else {
                Toast.makeText(requireContext(), "Сначала найдите рецепт", Toast.LENGTH_SHORT).show()
            }
        }

        btnView.setOnClickListener {
            (activity as MainActivity).replaceFragment(base_fragment())
        }
    }

    private fun searchRecipe(query: String, tvResult: TextView, btnSave: Button) {
        val apiKey = "cc361549231a4a68b59532f01f952e46"
        val searchUrl = "https://api.spoonacular.com/recipes/complexSearch?query=$query&apiKey=$apiKey&number=1"

        val queue = Volley.newRequestQueue(requireContext())
        val searchRequest = JsonObjectRequest(
            Request.Method.GET, searchUrl, null,
            { response ->
                try {
                    val results = response.getJSONArray("results")
                    if (results.length() > 0) {
                        val recipe = results.getJSONObject(0)
                        val recipeId = recipe.getInt("id")
                        foundTitle = recipe.getString("title")
                        getRecipeDetails(recipeId, tvResult, btnSave)
                    } else {
                        tvResult.text = "Ничего не найдено"
                        btnSave.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    tvResult.text = "Ошибка парсинга поиска"
                    Log.d("MyLog", "Search parse error: ${e.message}")
                }
            },
            { error ->
                tvResult.text = "Ошибка сети: ${error.message}"
                Log.d("MyLog", "Search error: $error")
            }
        )
        searchRequest.retryPolicy = DefaultRetryPolicy(10000, 1, 1f)
        queue.add(searchRequest)
    }

    private fun getRecipeDetails(recipeId: Int, tvResult: TextView, btnSave: Button) {
        val apiKey = "cc361549231a4a68b59532f01f952e46"
        val detailUrl = "https://api.spoonacular.com/recipes/$recipeId/information?apiKey=$apiKey"

        val queue = Volley.newRequestQueue(requireContext())
        val detailRequest = JsonObjectRequest(
            Request.Method.GET, detailUrl, null,
            { response ->
                try {
                    foundTime = response.getInt("readyInMinutes").toString()
                    val ingredientsArray = response.getJSONArray("extendedIngredients")
                    val ingredientsBuilder = StringBuilder()
                    for (i in 0 until ingredientsArray.length()) {
                        val ingredient = ingredientsArray.getJSONObject(i)
                        if (i > 0) ingredientsBuilder.append("\n")
                        ingredientsBuilder.append(ingredient.getString("original"))
                    }
                    foundIngredients = ingredientsBuilder.toString()

                    tvResult.text = "Найдено: $foundTitle\n\nВремя: $foundTime мин\n\nИнгредиенты:\n$foundIngredients"
                    btnSave.visibility = View.VISIBLE
                } catch (e: Exception) {
                    tvResult.text = "Ошибка парсинга деталей"
                    Log.d("MyLog", "Detail parse error: ${e.message}")
                }
            },
            { error ->
                tvResult.text = "Ошибка загрузки деталей: ${error.message}"
                Log.d("MyLog", "Detail error: $error")
            }
        )
        detailRequest.retryPolicy = DefaultRetryPolicy(20000, 1, 1f)
        queue.add(detailRequest)
    }
}