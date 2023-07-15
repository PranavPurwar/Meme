package dev.pranav.memes

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import dev.pranav.memes.adapter.MemeAdapter
import dev.pranav.memes.api.MemeRequest
import dev.pranav.memes.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        binding.grid.layoutManager  = CaughtLinearLayoutManager(this@MainActivity)
        binding.grid.adapter = MemeAdapter()
        supportActionBar?.hide()
        prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_refresh -> {
                    (binding.grid.adapter as MemeAdapter).clear()
                    refreshData()
                    true
                }
                R.id.subreddit -> {
                    val editText = EditText(this)
                    editText.setText(prefs.getString("subreddit", ""))
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Subreddit (leave blank for random)")
                        .setView(editText)
                        .setPositiveButton("OK") { _, _ ->
                            val subreddit = editText.text.toString()
                            prefs.edit().putString("subreddit", subreddit).apply()
                            (binding.grid.adapter as MemeAdapter).clear()
                            Toast.makeText(this, "Loading data from r/$subreddit", Toast.LENGTH_SHORT).show()
                            refreshData()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                    true
                }
                else -> false
            }
        }

        Log.d("MainActivity", " Fetching memes...")
        refreshData()
    }

    private fun refreshData() {
        Executors.newCachedThreadPool().execute {
            val sub = prefs.getString("subreddit", "")!!
            val subs = if (sub.isEmpty()) "" else "/$sub"
            val url = URL("https://meme-api.com/gimme$subs/50")
            val request = Request.Builder()
                .url(url)
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Error ${response.body.byteString().string(Charsets.UTF_8)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@execute
            }

            val gson = Gson()
            val text = response.body.string()
            val memeRequest = gson.fromJson(text, MemeRequest::class.java)
            memeRequest.memes = memeRequest.memes.sortedBy { it.ups }.reversed() // Sort descending by upvotes

            runOnUiThread {
                (binding.grid.adapter as MemeAdapter).submit(memeRequest)
                Log.d("MainActivity", "Memes: ${memeRequest.memes.size}")
            }
        }
    }


    class CaughtLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
        override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
                Log.e("TAG", "Out of bounds ignored")
            }
        }
    }

}