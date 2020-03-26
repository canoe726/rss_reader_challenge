package com.example.newsapplication

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

class RssHandler : DefaultHandler() {       // 구글 Rss 링크의 XML 분석

    private val stateUnKnown: Int = 0
    private val stateLink: Int = 1
    private val stateTitle: Int = 2
    private var curState: Int = stateUnKnown

    lateinit var feed: RssFeed
    lateinit var item: RssItem
    var itemFound: Boolean = false

    @Throws(SAXException::class)
    override fun startDocument() {
        super.startDocument()
        itemFound = false
        feed = RssFeed()
        item = RssItem()
    }

    @Throws(SAXException::class)
    override fun endDocument() {
        super.endDocument()
    }

    @Throws(SAXException::class)
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
        super.startElement(uri, localName, qName, attributes)
        if (localName.equals("item")) {
            itemFound = true
            item = RssItem()
            curState = stateUnKnown
        } else if (localName.equals("link")) {
            curState = stateLink
        } else if (localName.equals("title")) {
            curState = stateTitle
        } else {
            curState = stateUnKnown
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String?, localName: String?, qName: String?) {
        super.endElement(uri, localName, qName)
        if (localName.equals("item")) {     // XML 값을 일단 불러오면 나타날 값 설정
            item.desc = "Loading..."
            item.keyword[0] = "keyword1"
            item.keyword[1] = "keyword2"
            item.keyword[2] = "keyword3"
            feed.itemList.add(item)
        }
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
        super.characters(ch, start, length)

        val strCharacters = String(ch, start, length)
        if(itemFound) {
            when (curState) {
                stateTitle -> item.title = strCharacters      // item 내의 link 일 경우 값 설정
                stateLink -> item.link = strCharacters        // item 내의 title 일 경우 값 설정
            }
        }
        curState = stateUnKnown
    }
}