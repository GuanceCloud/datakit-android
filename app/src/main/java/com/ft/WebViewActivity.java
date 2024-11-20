package com.ft;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


public class WebViewActivity extends AppCompatActivity {
    private WebView webView;         // visit
//    private CustomWebView webView; // visit skip
    private Spinner spinner;
    private ProgressBar progressBar;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        WebView.setWebContentsDebuggingEnabled(true);
        setContentView(R.layout.activity_web);
        spinner = findViewById(R.id.spinner);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        String[] data = new String[]{
//                "https://www.taobao.com",
//                "https://www.tmall.com/",
//                "https://www.jd.com/",
//                "https://www.toutiao.com/",
//                "https://www.baidu.com/test",
//                "https://www.csdn.net/",
                "file:///android_asset/local_sample.html",
                "http://10.100.64.166/test/rum/"
        };
        spinner.setAdapter(new ArrayAdapter(this, R.layout.spinner_item, R.id.textView, data));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                webView.loadUrl(data[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.clearCache(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

        });
    }
//
//    private void setCookiePermission(Context context, WebView webview) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            CookieSyncManager.createInstance(context);
//        }
//        CookieManager cookieManager = CookieManager.getInstance();
//        CookieManager.setAcceptFileSchemeCookies(true);
//        cookieManager.setAcceptCookie(true);// 允许接受 Cookie
//        //>=LOLLIPOP 版本
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            cookieManager.setAcceptThirdPartyCookies(webview, true);
//            cookieManager.acceptThirdPartyCookies(webview);//跨域cookie读取
//
//
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            webView.reload();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
