package com.lexmark.lexi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private EditText mUrl;

    private final String SCAN_RESULT = "SCAN_RESULT";
    private final String SCAN_MODE = "SCAN_MODE";
    private final String QR_CODE_MODE = "QR_CODE_MODE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView)findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl("http://www.google.com");

        mUrl = (EditText)findViewById(R.id.url);

        Button goBtn = (Button)findViewById(R.id.go_btn);
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = mUrl.getText().toString();

                if(url.length() > 0)
                    mWebView.loadUrl(url);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0)
        {
            if(resultCode == RESULT_OK)
            {
                String url = data.getStringExtra(SCAN_RESULT);

                if(url.length() > 0)
                    mUrl.setText(url);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.scan_qr:
                scanQR();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void scanQR()
    {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra(SCAN_MODE, QR_CODE_MODE); // "PRODUCT_MODE for bar codes

        startActivityForResult(intent, 0);
    }
}
