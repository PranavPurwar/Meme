package dev.pranav.memes.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.pranav.memes.api.Meme
import dev.pranav.memes.api.MemeRequest
import dev.pranav.memes.databinding.MemeItemBinding
import java.net.URL
import java.util.concurrent.Executors

class MemeAdapter : RecyclerView.Adapter<MemeAdapter.MemeViewHolder>() {

    private val memes = mutableListOf<Meme>()

    fun submit(request: MemeRequest) {
        val start = memes.size
        memes.clear()
        memes.addAll(request.memes)
        notifyItemRangeInserted(start, memes.size)
    }

    fun clear() {
        memes.clear()
        notifyItemRangeRemoved(0, memes.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeViewHolder {
        val memeBinding =
            MemeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemeViewHolder(memeBinding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MemeViewHolder, position: Int) {
        val meme = memes[position]
        holder.binding.apply {
            Glide.with(memeImage)
                .load(meme.url)
                .into(memeImage)

            upvotes.text = "${meme.ups} upvotes"
            memeSubreddit.text = "${meme.author} ~ r/${meme.subreddit}"
            memeTitle.text = meme.title

            root.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meme.postLink))
                root.context.startActivity(intent)
            }

            root.setOnLongClickListener {
                Executors.newCachedThreadPool().execute {
                    val path = download(meme)
                    root.post {
                        Toast.makeText(
                            root.context,
                            "Saved to $path",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                true
            }
        }
    }

    private fun download(meme: Meme): String {
        val file =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .resolve(meme.title + "." + meme.url.substringAfterLast("."))
        file.outputStream().use {
            URL(meme.url).openStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file.absolutePath
    }

    override fun getItemCount(): Int {
        return memes.size
    }

    class MemeViewHolder(val binding: MemeItemBinding) : RecyclerView.ViewHolder(binding.root)
}