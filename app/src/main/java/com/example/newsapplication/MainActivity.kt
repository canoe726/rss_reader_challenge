package com.example.newsapplication

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import java.io.IOException
import java.io.Serializable
import java.lang.ref.WeakReference
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

// HTML 태그를 쉽게 가져오기 위해 Jsoup 라이브러리 사용 -> 링크 : https://jsoup.org/download [implementation 'org.jsoup:jsoup:1.13.1']

private const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36"

class MainActivity : AppCompatActivity() {
    private val newsURL = "https://news.google.com/rss?hl=ko&gl=KR&ceid=KR:ko"      // Google News Rss 한국어 링크
    private var newsList : ArrayList<RssItem> = arrayListOf()

    private lateinit var mainRecyclerViewAdapter: MainRecyclerViewAdapter       // newList 의 Adapter
    private lateinit var rssTask: ProcessRssTask        // rss 뉴스 링크의 자료를 가져오는 async 작업

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadRss()

        setSupportActionBar(mainToolbar as Toolbar?)        // Activity 상단 툴바에 title 추가
        supportActionBar?.setTitle(R.string.app_name)

        val linearLayoutManager = LinearLayoutManager(this)     // RecyclerView LayoutManager 추가
        mainRecyclerView.layoutManager = linearLayoutManager
        mainRecyclerView.setHasFixedSize(true)
        mainRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))       // RecyclerView 구분선 추가      // RecyclerView 구분선 추가

        setRecyclerPullUp()     // RecyclerView 리스트를 당겨서 새로고침 이벤트 설정
    }

    private fun loadRss() {      // RssFeed 불러오기
        refreshRecyclerViewProgressBar.visibility = View.VISIBLE
        rssTask = ProcessRssTask(this)
        rssTask.execute(newsURL)
    }

    private fun setRecyclerView() {     // RecyclerView Adapter 설정
        mainRecyclerViewAdapter = MainRecyclerViewAdapter(newsList, this)       // RecyclerView Adapter 추가
        mainRecyclerViewAdapter.itemClick = object: MainRecyclerViewAdapter.ItemClick {         // RecyclerView 의 ClickListener 정의 : Rss 뉴스피드 -> 뉴스본문
            override fun onClick(view: View, position: Int) {
                val newsContentIntent = Intent(this@MainActivity, NewsContentActivity::class.java)      // RecyclerView Item 클릭시 Content 액티비티로 이동
                newsContentIntent.putExtra("newsItem", newsList[position] as Serializable)      // newsList 데이터도 함께 이동
                startActivity(newsContentIntent)
            }
        }
        mainRecyclerView.adapter = mainRecyclerViewAdapter
    }

    private fun setRecyclerPullUp() {
        itemSwipeToRefresh.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.coolBlue))        // RecyclerView swipe 시 색상 설정
        itemSwipeToRefresh.setColorSchemeColors(Color.WHITE)

        itemSwipeToRefresh.setOnRefreshListener {       // RecyclerView swipe 시 작동 이벤트
            newsList.clear()        // Rss 뉴스리스트 초기화 및 UI 초기화
            newsList = arrayListOf()
            setRecyclerView()

            itemSwipeToRefresh.isRefreshing = false     // RecyclerView 로딩 아이콘 비활성화

            loadRss()
        }
    }

    /*
    1. inner class 선언으로 인한 this asynctask class should be static or leaks might occur 경고 발생 원인
        -> MainActivity 가 끝나고 나서도 asyncTask 가 작동할 수 있기 때문에 경고 발생
    2. 해결 -> To prevent leaks, you can make the inner class static / private class 로 선언하고 WeakReference 로 MainActivity 의 private 변수들을 가져온다
     */
    private class ProcessRssTask
    internal constructor(val context: MainActivity): AsyncTask<String, Void, Void>() {
        private val activityReference: WeakReference<MainActivity> = WeakReference(context)     // MainActivity 의 주소를 가져와 변수에 접근
        val activity = activityReference.get()

        override fun onPreExecute() {
            super.onPreExecute()
            if(activity == null || activity.isFinishing) return
        }

        override fun doInBackground(vararg urls: String?): Void? {
            try {
                val rssUrl = URL(urls[0])
                val saxParserFactory: SAXParserFactory = SAXParserFactory.newInstance()
                val saxParser: SAXParser = saxParserFactory.newSAXParser()
                val xmlReader: XMLReader = saxParser.xmlReader
                val rssHandler = RssHandler()       // SAX XML parser 로 rss 의 정보 추출

                xmlReader.contentHandler = rssHandler

                val inputSource = InputSource(rssUrl.openStream())
                xmlReader.parse(inputSource)

                val rssFeed: RssFeed
                rssFeed = rssHandler.feed
                activity?.newsList = rssFeed.itemList

            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if(activity != null) {
                activity.setRecyclerView()
                activity.refreshRecyclerViewProgressBar.visibility = View.GONE

                /*
                1. execute() : queue 에 쓰레드를 넣어 하나씩 실행시킨다 -> 느린 속도
                2. executeOnExecutor() : 쓰레드를 병렬로 실행해 한꺼번에 실행한다 -> 빠른 속도
                 */
                var idx = 0
                while (idx < activity.newsList.size) {
                    val metaTask = GetMetaPropertyTask(context)
                    metaTask.executeOnExecutor(     // parallel execution of both AsyncTasks
                        THREAD_POOL_EXECUTOR,
                        Pair(activity.newsList[idx].link, idx)
                    )
                    idx += 1
                }
            }
        }
    }

    private class GetMetaPropertyTask
    internal constructor(context: MainActivity) : AsyncTask<Pair<String, Int>, Void, Void>() {
        private var newsIdx: Int = 0

        private val activityReference: WeakReference<MainActivity> = WeakReference(context)
        val activity = activityReference.get()

        override fun doInBackground(vararg urls: Pair<String, Int>): Void? {
            try {
                if(activity != null) {
                    try {
                        newsIdx = urls[0].second

                        val connect: Connection = Jsoup.connect(urls[0].first)      // HTML 태그인 meta property 를 쉽게 가져오기 위해 Jsoup 사용
                            .timeout(1000 * 10)      // 10 초 동안 웹 정보를 받아옴
                            .ignoreHttpErrors(true)
                            .header("Content-Type", "application/json;charset=UTF-8")       // bot 이 아닌 client 로 인식 하게 하기 위함
                            .userAgent(USER_AGENT)                                                       //
                            .ignoreContentType(true)                                    //
                            .method(Connection.Method.GET)
                            .referrer("http://www.google.com")      // HTTP header field that identifies the address of the webpage

                        val document: Document = connect.get()

                        val metaOgImage = document.select("meta[property=og:image]")
                        val metaOgDesc = document.select("meta[property=og:description]")

                        if (metaOgImage.size != 0) {
                            activity.newsList[newsIdx].thumbnail = metaOgImage.attr("content")
                        } else {
                            activity.newsList[newsIdx].thumbnail = ""
                        }

                        if (metaOgDesc.size != 0) {
                            activity.newsList[newsIdx].desc = metaOgDesc.attr("content")

                            // val symbols = Regex("[^\\uAC00-\\uD7A3xfe0-9a-zA-Z\\\\s]")
                            val symbols = Regex("[^A-Za-z0-9ㄱ-힇]")      // 영어 소문자, 대문자, 숫자 0~9, 한글만 가져오도록 설정
                            val contents: MutableList<String> = activity.newsList[newsIdx].desc.split(" ").toList().toMutableList()

                            var idx = 0
                            while (idx < contents.size) {
                                val word = contents[idx].replace(symbols, "")       // 키워드의 특수문자 제거
                                if (word.length < 2) {        // 길이가 2 이하인 단어 제거
                                    contents.removeAt(idx)
                                    idx -= 1
                                } else {
                                    contents[idx] = word
                                }
                                idx += 1
                            }

                            // 키워드 빈도수 검사
                            val words = HashMap<String, Int>()
                            idx = 0
                            while (idx < contents.size) {
                                if (words.containsKey(contents[idx])) {
                                    words.put(contents[idx], words[contents[idx]]!!.plus(1))
                                } else {
                                    words.put(contents[idx], 1)
                                }
                                idx += 1
                            }

                            // 키워드 정렬
                            var keywords: List<Pair<String, Int>> = words.toList()
                            keywords = keywords.sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })        // 1. 빈도수 내림차순 정렬 2. 빈도수가 같을 경우 문자정렬 오름차순 적용

                            activity.newsList[newsIdx].keyword[0] = keywords[0].first
                            activity.newsList[newsIdx].keyword[1] = keywords[1].first
                            activity.newsList[newsIdx].keyword[2] = keywords[2].first

                        } else {
                            activity.newsList[newsIdx].desc = "No Description"
                            activity.newsList[newsIdx].keyword[0] = "keyword1"
                            activity.newsList[newsIdx].keyword[1] = "keyword2"
                            activity.newsList[newsIdx].keyword[2] = "keyword3"
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    } catch (e: HttpStatusException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            activity?.mainRecyclerViewAdapter?.notifyItemChanged(newsIdx)       // rss 뉴스 링크의 정보를 모두 가져오면 RecyclerView 업데이트
        }
    }
}
