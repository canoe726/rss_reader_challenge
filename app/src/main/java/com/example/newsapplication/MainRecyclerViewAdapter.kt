package com.example.newsapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

// 빠른 이미지 로딩을 위해 Picasso 라이브러리 사용 -> 링크 : https://github.com/square/picasso [implementation 'com.squareup.picasso:picasso:2.71828']

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {      // activity_news_content.xml 의 view items
    var thumbnail: ImageView = itemView.findViewById(R.id.itemImageView)
    var title: TextView = itemView.findViewById(R.id.itemTitleTextView)
    var desc: TextView = itemView.findViewById(R.id.itemContentTextView)
    var keyword1: TextView = itemView.findViewById(R.id.itemKeyword1)
    var keyword2: TextView = itemView.findViewById(R.id.itemKeyword2)
    var keyword3: TextView = itemView.findViewById(R.id.itemKeyword3)
}

class MainRecyclerViewAdapter(private val newsList: ArrayList<RssItem>, private val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }
    lateinit var itemClick: ItemClick

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.main_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(newsList[position].thumbnail != "") {
            Picasso.get().load(newsList[position].thumbnail).into(holder.thumbnail)     // 빠른 이미지 로딩을 위한 Picasso 라이브러리 사용
       } else {
            holder.thumbnail.setImageResource(R.drawable.ic_image_black_24dp)       // thumbnail 이 없으면 default 이미지 표시
        }

        holder.title.text = newsList[position].title
        holder.desc.text = newsList[position].desc
        holder.keyword1.text = newsList[position].keyword[0]
        holder.keyword2.text = newsList[position].keyword[1]
        holder.keyword3.text = newsList[position].keyword[2]

        holder.itemView.setOnClickListener {
            v-> itemClick.onClick(v, position)
        }
    }
}