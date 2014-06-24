package org.wikitoki.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import wikitoki.core.WikiToki;




public class MainActivity extends Activity {

    WebView webViewMain;
    WebViewClient webViewMainClient;

    WikiToki wikiToki;

    private static final String DEFAULT_PAGE_NAME = "WikiToki";
    private static final String DEFAULT_PAGE_HTML = "<h1>Superduper</h1><p>Min %XX% wiki-side!!</p><a href=\"http://en.wikipedia.org/\">External link</a>\n" +
            "<a href=\"wikitoki://SomeOtherPage\">SomeOtherPage</a>";
    private static final String OTHER_PAGE_HTML = "<p>En annen %XX% wiki-side!! Back to <a href=\"wikitoki://" + DEFAULT_PAGE_NAME + "\">" + DEFAULT_PAGE_NAME + "</a></p>\n";

    private static final String DEFAULT_MIME = "text/html";
    private static final String DEFAULT_ENCODING = "utf-8";

    private static final boolean SHOW_URL_IN_EXTERNAL_BROWSER = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webViewMainClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);

                if (isWikitokiInternalUrl(url)) {
                    String pageName = uri.getHost();
                    if (wikiToki.doesLocalPageExist(pageName)) {
                        view.loadDataWithBaseURL(null, wikiToki.renderLocalPage(pageName), DEFAULT_MIME, DEFAULT_ENCODING, null);
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com")));
                    }
                    return true;
                }

                if (SHOW_URL_IN_EXTERNAL_BROWSER) {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }

                view.loadUrl(url);
                return true;
            }
        };
        webViewMain = (WebView) this.findViewById(R.id.webViewMain);
        webViewMain.setWebViewClient(webViewMainClient);

        wikiToki = new WikiToki(this);
        wikiToki.writeLocalPage(DEFAULT_PAGE_NAME, DEFAULT_PAGE_HTML.replace("%XX%", getRandomSuperduper()));
        wikiToki.writeLocalPage("SomeOtherPage", OTHER_PAGE_HTML.replace("%XX%", getRandomSuperduper()));
    }

    private boolean isWikitokiInternalUrl(String url) {
        return url.startsWith("wikitoki://");
    }

    @Override
    protected void onResume() {
        super.onResume();

        String htmlContent = wikiToki.renderLocalPage(DEFAULT_PAGE_NAME);
        webViewMain.getSettings().setJavaScriptEnabled(true);
        webViewMain.loadDataWithBaseURL(null, htmlContent, DEFAULT_MIME, DEFAULT_ENCODING, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(webViewMain.canGoBack()){
            webViewMain.goBack();
        }else{
            super.onBackPressed();
        }
    }

    private String getRandomSuperduper() {
        final String[] superduper = {"yeah", "superduper", "ultrasuper", "kjempekult", "put-your-adjective-here"};
        return superduper[(int) (Math.random() * 5)];
    }
}
