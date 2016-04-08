package com.lexmark.lexi;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private EditText mRequestText;
    private ViewGroup mChatLogs;
    private TextToSpeech mTextToSpeech;

    private final String SCAN_RESULT = "SCAN_RESULT";
    private final String SCAN_MODE = "SCAN_MODE";
    private final String QR_CODE_MODE = "QR_CODE_MODE";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_QR_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChatLogs = (ViewGroup)findViewById(R.id.chatLogs);

        ImageButton speekBtn = (ImageButton)findViewById(R.id.speek_btn);
        speekBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();

            }
        });

        mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTextToSpeech.setLanguage(Locale.US);
            }
        });

    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Please say a command");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Text to speech not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    addRequest(result.get(0));
                    addResponse("This is my response to your command.");
                }
                break;
            }
            case REQ_CODE_QR_CODE: {
                // DO STUFF WITH QR CODE
                break;
            }
        }
    }

    private void addRequest(String text)
    {
        View newView = getLayoutInflater().inflate(R.layout.request_item, null);
        TextView textView = (TextView)newView.findViewById(R.id.text);
        textView.setText(text);
        mChatLogs.addView(newView);
    }

    private void addResponse(String text)
    {
        View newView = getLayoutInflater().inflate(R.layout.response_item, null);
        TextView textView = (TextView)newView.findViewById(R.id.text);
        textView.setText(text);
        mTextToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH, null, null);
        mChatLogs.addView(newView);
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
