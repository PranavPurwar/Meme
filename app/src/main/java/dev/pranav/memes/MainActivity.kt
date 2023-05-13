package dev.pranav.memes

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import androidx.core.net.toUri
import com.google.gson.Gson
import dev.pranav.memes.api.MemeRequest
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Executors.newCachedThreadPool().execute {
            val url = URL("https://meme-api.com/gimme/memes/50")
            val gson = Gson()
            val text = url.readText()
            val request = gson.fromJson(text, MemeRequest::class.java)
            request.memes = request.memes.filterNot { it.nsfw }
            runOnUiThread {
                val gridView = findViewById<GridView>(R.id.grid)
                gridView.adapter = object : BaseAdapter() {
                    override fun getCount(): Int {
                        return request.count
                    }

                    override fun getItem(position: Int): Any {
                        return request.memes[position]
                    }

                    override fun getItemId(position: Int): Long {
                        return 0
                    }

                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup?
                    ): View {
                        val view = ImageView(this@MainActivity)
                        Executors.newCachedThreadPool().execute {
                            val bitmap = request.memes[position].bitmap
                            runOnUiThread {
                                view.setImageBitmap(bitmap)
                            }
                        }
                        return view
                    }

                }
            }
        }
    }
}