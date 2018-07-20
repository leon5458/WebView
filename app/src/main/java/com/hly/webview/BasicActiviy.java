package com.hly.webview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * ~~~~~~文件描述:~~~~~~
 * ~~~~~~作者:huleiyang~~~~~~
 * ~~~~~~创建时间:2018/7/19~~~~~~
 * ~~~~~~更改时间:2018/7/19~~~~~~
 * ~~~~~~版本号:1~~~~~~
 */
public class BasicActiviy extends AppCompatActivity{
    private WebView webView;

    private ProgressBar progressBar;

    private TextView mtitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_activity_layout);

        webView = findViewById(R.id.web);
        progressBar= findViewById(R.id.progressbar);//进度条
        mtitle = findViewById(R.id.title_tv);

        WebSettings webSettings = webView.getSettings();
        if (webSettings == null) return;
        webSettings.setSupportZoom(true);

        //加载本地
//        webView.loadUrl("file:///android_asset/yushouxin.html");
        //加载网络url
        webView.loadUrl("https://juejin.im/post/5ae9cc3a5188253dc612842b");

        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(webViewClient);
    }

    //WebViewClient主要帮助WebView处理各种通知、请求事件
    private WebViewClient webViewClient=new WebViewClient(){
        @Override
        public void onPageFinished(WebView view, String url) {//页面加载完成
            progressBar.setVisibility(View.GONE);
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//页面开始加载
            progressBar.setVisibility(View.VISIBLE);
        }
    };

    //WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    private WebChromeClient webChromeClient=new WebChromeClient(){

        //获取网页标题
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            mtitle.setText(title);//
            Log.i("ansen","网页标题:"+title);
        }

        //加载进度回调
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        webView.destroy();
        webView=null;
    }
}
