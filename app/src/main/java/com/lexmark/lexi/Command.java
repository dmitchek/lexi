package com.lexmark.lexi;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * Created by dmitchell on 4/8/2016.
 */
public class Command {

    private Context mContext;

    public Command(Context context) { mContext = context;}

    public void sendCommand(String host, int sessionId, String command, Response.Listener<JSONObject> callback)
    {
        String url = host + "/sendText/" + sessionId;
        Log.d("Lexi", "Sending command: " + url);

        JSONObject data;
        try {
            data = new JSONObject();
            data.put("text", command);
        }
        catch (JSONException e )
        {
            Toast toast = Toast.makeText(mContext, "Error sending command", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, data, callback, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Lexi", "Error sending command: " + error.toString());
                Toast toast = Toast.makeText(mContext, "Error communicating with service", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }

}
