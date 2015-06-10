package com.example.alena.checker;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class CheckerActivity extends ActionBarActivity {
    static final String NEED_TEXT = "apptest.com/i?id=";
    static final String LINK_PART1 = "http://app.mobilenobo.com/c/apptest?id=";
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_checker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startChecking (View view) {

        String msgBody = null;
        String id = null;
        String webMsg = null;

        msgBody = findMessage();
        if ((msgBody != null) && (!msgBody.isEmpty())){
            id = getIdMsg(msgBody);
            if ((id != null) && (!id.isEmpty())){
                webMsg = callLink(LINK_PART1 + id );
                if((webMsg != null) && (!webMsg.isEmpty()))
                    Toast.makeText(context, webMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    private String findMessage (){
        String msgBody = null;
        boolean isFound = false;
        Cursor cursor;
        cursor = getContentResolver().query(Uri.parse("content://sms/inbox"),
                null, null, null, null);
        if(cursor == null){
            return null;
        }

        if (cursor.moveToFirst()) {
            int i;
            for (i = 0; i < cursor.getColumnCount(); i++) {
                msgBody = cursor.getString(cursor.getColumnIndexOrThrow("body")).toString();
                /* Search for a match text in body */
                if ((msgBody != null) && (msgBody.toLowerCase().contains(NEED_TEXT.toLowerCase()))) {
                    isFound = true;
                    Toast.makeText(context, "Message is found!", Toast.LENGTH_LONG).show();
                    break;
                }
                cursor.moveToNext();
            }
            if (!isFound) {
                msgBody = null;
                Toast.makeText(context, "Message is not found!", Toast.LENGTH_LONG).show();
            }
        }else {
//            no messages text
            Toast.makeText(context, "Inbox is empty!", Toast.LENGTH_LONG).show();
        }
        cursor.close();
        return msgBody;
    }

    private String getIdMsg(String text){
        String id = null;
        if (text != null) {
            id = text.substring(text.lastIndexOf("=")+1);
            Toast.makeText(context, "id from massage is found '"+ id +"'", Toast.LENGTH_LONG).show();
        }
        return id;
    }

    private String callLink(String url){
        InputStream inputStream = null;
        String resMsg = null;

        if(isConnected()){
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

                inputStream = httpResponse.getEntity().getContent();

                if (inputStream != null)
                    resMsg = inputStreamToString(inputStream);

            } catch (Exception e) {
                Log.e("InputStream", e.getLocalizedMessage());
            }
        } else {
            Toast.makeText(context, "No connection!", Toast.LENGTH_LONG).show();
        }

        return resMsg;
    }

    /* convert inputstream to String */
    private static String inputStreamToString(InputStream inputStream) throws IOException {
        String line = "";
        String res = "";
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));

        while((line = bufferedReader.readLine()) != null)
            res += line;

        inputStream.close();
        return res;

    }

    /* check network connection */
    private boolean isConnected(){

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if ((netInfo != null) && netInfo.isConnected())
            return true;
        else
            return false;
    }
}
