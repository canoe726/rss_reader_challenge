package com.example.newsapplication

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_news_content.*

class NewsContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_content)

        val newsItem = intent.extras?.get("newsItem") as RssItem        // MainActivity 로 부터 newsList 값을 가져온다

        newsTitleTextView.text = newsItem.title     // 뉴스 상세보기의 기사 제목

        newsKeyWord1TextView.text = newsItem.keyword[0]     // 뉴스 상세보기의 키워드 1
        newsKeyWord2TextView.text = newsItem.keyword[1]     // 뉴스 상세보기의 키워드 2
        newsKeyWord3TextView.text = newsItem.keyword[2]     // 뉴스 상세보기의 키워드 3

        newsWebView.webViewClient = object: WebViewClient() {       // WebView 를 사용한 뉴스 본문 표시
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }
        }

        setWebViewSetting()

        newsWebView.loadUrl(newsItem.link)

        newsWebView.webViewClient = object: WebViewClient(){        // Set web view client
            override fun onPageFinished(view: WebView, url: String) {
                toast(view.title)                                   // WebView 로딩이 끝나면 메시지 출력
                loadWebViewProgressBar.visibility = View.GONE       // ProgressBar 비활성화
            }
        }
    }

    override fun onBackPressed() {
        when {
            newsWebView.canGoBack() -> newsWebView.goBack()     // WebView 내에서 이동 후 뒤로가기 버튼 시 WebView 뒤로가기
            else -> super.onBackPressed()                       // 뒤로갈 WebView 가 없으면 Activity 이동
        }
    }

    private fun setWebViewSetting() {
        val webViewSetting = newsWebView.settings       // webView 의 WebSetting 얻기

        webViewSetting.javaScriptEnabled = true     // java script 활성화

        webViewSetting.setSupportZoom(true)     // webView Zoom 기능 활성화
        webViewSetting.builtInZoomControls = true
        webViewSetting.displayZoomControls = true
        webViewSetting.textZoom = 100       // Zoom webView text

        webViewSetting.blockNetworkImage = false            // WebView 가 이미지를 불러오도록 설정
        webViewSetting.loadsImagesAutomatically = true      // WebView 는 Image 를 반드시 불러 오도록 설정

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webViewSetting.safeBrowsingEnabled = true       // (API 레벨 26) 이상에서는 setSafeBrowsingEnabled() 사용을 지원
        }

        webViewSetting.useWideViewPort = true       // settings.pluginState = WebSettings.PluginState.ON
        webViewSetting.loadWithOverviewMode = true
        webViewSetting.javaScriptCanOpenWindowsAutomatically = true
        webViewSetting.mediaPlaybackRequiresUserGesture = false

        /*
        // More optional settings, you can enable it by yourself
        webViewSetting.setAppCacheEnabled(true)     // Enable and setup web view cache
        webViewSetting.cacheMode = WebSettings.LOAD_DEFAULT
        webViewSetting.setAppCachePath(cacheDir.path)
        webViewSetting.domStorageEnabled = true
        webViewSetting.setSupportMultipleWindows(true)
        webViewSetting.loadWithOverviewMode = true
        webViewSetting.allowContentAccess = true
        webViewSetting.setGeolocationEnabled(true)
        webViewSetting.allowUniversalAccessFromFileURLs = true
        webViewSetting.allowFileAccess = true
         */

        newsWebView.fitsSystemWindows = true        // WebView settings
        newsWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)        // (API 레벨 19) 이상에서는 hardware acceleration 지원
    }
}

// Extension function to show toast message
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}