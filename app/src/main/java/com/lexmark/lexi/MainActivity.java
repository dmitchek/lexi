package com.lexmark.lexi;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ViewGroup mChatLogs;
    private ViewGroup mSpeekOptions;
    private View mProgress;
    private TextToSpeech mTextToSpeech;
    private Context mContext;

    private final String SCAN_RESULT = "SCAN_RESULT";
    private final String SCAN_MODE = "SCAN_MODE";
    private final String QR_CODE_MODE = "QR_CODE_MODE";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_QR_CODE = 200;

    public String mServiceHost = "http://192.168.19.56:8081";
    private String mPrinter = "127.0.0.1";
    public static final String PREFS_NAME = "LexiSharedPrefs";

    private int mControlledDevice = -1;
    private int mSessionId = -1;
    private CommandResponse mCommandResponseHandler;
    private Command mCommand;

    private class CommandResponse implements Response.Listener<JSONObject>
    {
        @Override
        public void onResponse(JSONObject response) {
            mProgress.setVisibility(View.GONE);
            mSpeekOptions.setVisibility(View.VISIBLE);
            try
            {
                String responseText = response.getString("dictationString");
                addResponse(responseText);

            }catch (JSONException e)
            {
                Toast toast = Toast.makeText(mContext, "Error receiving response", Toast.LENGTH_SHORT);
                toast.show();
                Log.d("Lexi", "Error receiving response: " + e.toString());
            }
        }
    }

    private void getSettings()
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mServiceHost = settings.getString("serviceHost", "");
        mPrinter = settings.getString("printer", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSettings();

        mContext = getApplicationContext();

        mCommand = new Command(mContext);
        mCommandResponseHandler = new CommandResponse();

        mChatLogs = (ViewGroup)findViewById(R.id.chatLogs);

        mSpeekOptions = (ViewGroup)findViewById(R.id.speekOptions);
        mProgress = findViewById(R.id.progressBar);

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

        createControlledDevice();
        mSessionId = -1;
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
            Toast.makeText(mContext,
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
                    mSpeekOptions.setVisibility(View.GONE);
                    mProgress.setVisibility(View.VISIBLE);
                    sendCommand(result.get(0));
                }
                break;
            }
            case REQ_CODE_QR_CODE: {
                // DO STUFF WITH QR CODE
                break;
            }
        }
    }

    private void sendCommand(String text)
    {
        if(mSessionId > 0) {
            View newView = getLayoutInflater().inflate(R.layout.request_item, null);
            TextView textView = (TextView) newView.findViewById(R.id.text);
            textView.setText(text);
            mChatLogs.addView(newView);
            mCommand.sendCommand(mServiceHost, mSessionId, text, mCommandResponseHandler);
        }
    }

    private void addResponse(String text)
    {
        View newView = getLayoutInflater().inflate(R.layout.response_item, null);
        TextView textView = (TextView)newView.findViewById(R.id.text);
        textView.setText(text);
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
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
            case R.id.reconnect:
                createControlledDevice();
                return true;
            case R.id.settings:
                new SettingsFragment().show(getFragmentManager(), "Settings");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*private void scanQR()
    {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra(SCAN_MODE, QR_CODE_MODE); // "PRODUCT_MODE for bar codes

        startActivityForResult(intent, 0);
    }*/

    private void createControlledDevice()
    {
        getSettings();
        mChatLogs.removeAllViewsInLayout();
        String url = mServiceHost;
        url += "/controlledDevice?userId=dev&deviceAddress=" + mPrinter;
        Log.d("Lexi", "Creating controller: " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Lexi", "Got a response!");
                try
                {
                    String address = response.getString("address");
                    String user = response.getString("user");
                    int controlledDevice = response.getInt("id");
                    mControlledDevice = controlledDevice;

                    String message = "new Controlled Device for: " + address + " and user: " + user;
                    Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
                    toast.show();

                    createSession();
                }
                catch (JSONException e)
                {
                    Log.d("Lexi", "Unexpected JSON response: " + e.toString());
                    Toast toast = Toast.makeText(mContext, "Unable to create controlled device on", Toast.LENGTH_SHORT);
                    toast.show();
                    mControlledDevice = -1;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Lexi", "Error getting json!" + error.toString());
                Toast toast = Toast.makeText(mContext, "Unable to connect to " + mServiceHost, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        VolleySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }

    private void createSession()
    {
        if(mControlledDevice > 0) {
            String url = mServiceHost + "/createSession?controlledDeviceId=" + mControlledDevice;
            Log.d("Lexi", "Creating session: " + url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Lexi", "Got a response!");

                    try
                    {
                        mSessionId = response.getInt("id");
                    }
                    catch (JSONException e)
                    {
                        Log.d("Lexi", "Unable to create session: " + e.toString());
                        Toast toast = Toast.makeText(mContext, "Unable to create session", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    String message = "New session created: " + mSessionId;
                    Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
                    toast.show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Lexi", "Error getting json!" + error.toString());
                    Toast toast = Toast.makeText(mContext, "Unable to create session", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

            VolleySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
        }
    }
}
