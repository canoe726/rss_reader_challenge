package com.example.newsapplication

import java.io.Serializable

class RssItem : Serializable {
    var title: String = ""
    var link: String = ""
    var thumbnail: String = ""
    var desc: String = ""
    var keyword: ArrayList<String> = arrayListOf("","","")

    override fun toString(): String {
        return title
    }
}