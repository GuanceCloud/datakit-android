package com.ft;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import com.ft.sdk.FTAutoTrack;
import com.ft.webview.CustomWebView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class WebViewActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private WebViewPagerAdapter pagerAdapter;

    // URLs for each tab
    private String[] urls = {
            "http://10.100.64.166/test/rum/",
            "https://appassets.androidplatform.net/assets/browser_sdk_sample.html",
            "file:///android_asset/local_sample.html"
    };

    // Tab titles
    private String[] tabTitles = {"RUM Test", "Browser SDK", "Local Sample"};

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // Initialize views
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);

        // Setup ViewPager2 with adapter
        pagerAdapter = new WebViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();

        // Setup ViewPager2 page change listener to load URL on first tab selection
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Load URL for the selected tab
                loadCurrentTabUrl();
            }
        });

        // Setup progress bar visibility
        progressBar.setVisibility(View.GONE);
    }

    // WebView Fragment for each tab
    public static class WebViewFragment extends Fragment {
        private CustomWebView webView;
        private String url;
        private boolean isLoaded = false;
        private WebViewAssetLoader assetLoader;
        private ProgressBar progressBar;

        public WebViewFragment() {
            // Required empty public constructor
        }

        public static WebViewFragment newInstance(String url) {
            WebViewFragment fragment = new WebViewFragment();
            Bundle args = new Bundle();
            args.putString("url", url);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                url = getArguments().getString("url");
            }
        }

        @Override
        public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                 android.view.ViewGroup container,
                                 Bundle savedInstanceState) {
            // Create a LinearLayout to hold WebView and ProgressBar
            android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT));

            // Create WebView
            webView = new CustomWebView(getContext());
            webView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT));

            // Create ProgressBar
            progressBar = new ProgressBar(getContext(), null,
                    android.R.attr.progressBarStyleHorizontal);
            progressBar.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 4));
            progressBar.setVisibility(View.GONE);

            layout.addView(webView);
            layout.addView(progressBar);

            setupWebView();
            return layout;
        }

        private void setupWebView() {
            // Setup WebViewAssetLoader
            assetLoader = new WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(getContext()))
                    .build();

            // Setup WebViewClient
            webView.setWebViewClient(new WebViewClientCompat() {
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    return assetLoader.shouldInterceptRequest(request.getUrl());
                }

                @Override
                @SuppressWarnings("deprecation") // for API < 21
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    return assetLoader.shouldInterceptRequest(Uri.parse(url));
                }
            });

            // Setup WebSettings
            WebSettings webViewSettings = webView.getSettings();
            webViewSettings.setAllowFileAccessFromFileURLs(false);
            webViewSettings.setAllowUniversalAccessFromFileURLs(false);
            webViewSettings.setAllowFileAccess(false);
            webViewSettings.setAllowContentAccess(false);
            webViewSettings.setJavaScriptEnabled(true);
            webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webView.clearCache(true);

            // Setup WebChromeClient for progress tracking
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    if (getActivity() != null) {
                        getActivity().setTitle(title);
                    }
                }

                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (progressBar != null) {
                        progressBar.setProgress(newProgress);
                        if (newProgress == 100) {
                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }

        // Method to load URL when tab is first selected
        public void loadUrlIfNeeded() {
            if (!isLoaded && webView != null && url != null) {
                webView.loadUrl(url);
                isLoaded = true;
            }
        }

        // Method to refresh the WebView
        public void refreshWebView() {
            if (webView != null) {
                webView.reload();
            }
        }
    }

    // ViewPager2 Adapter
    private class WebViewPagerAdapter extends FragmentStateAdapter {
        public WebViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return WebViewFragment.newInstance(urls[position]);
        }

        @Override
        public int getItemCount() {
            return urls.length;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load URL for current tab when activity resumes
        loadCurrentTabUrl();
    }

    // Load URL for the currently selected tab
    private void loadCurrentTabUrl() {
        int currentPosition = viewPager.getCurrentItem();
        // Find the fragment by tag
        String fragmentTag = "f" + currentPosition;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment instanceof WebViewFragment) {
            ((WebViewFragment) fragment).loadUrlIfNeeded();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.webview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            refreshCurrentWebView();
        } else if (item.getItemId() == R.id.go_to_first_activity) {
            Intent intent = new Intent(this, FirstActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to refresh the current WebView fragment
    private void refreshCurrentWebView() {
        int currentPosition = viewPager.getCurrentItem();
        // Find the fragment by tag
        String fragmentTag = "f" + currentPosition;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment instanceof WebViewFragment) {
            ((WebViewFragment) fragment).refreshWebView();
        }
    }

    // Method to set cookie permission (if needed)
    private void setCookiePermission(Context context, WebView webView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } else {
            CookieSyncManager.createInstance(context);
            CookieManager.getInstance().setAcceptCookie(true);
        }
    }
}