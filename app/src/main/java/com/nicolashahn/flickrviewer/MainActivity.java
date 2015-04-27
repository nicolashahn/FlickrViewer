package com.nicolashahn.flickrviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.image.SmartImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    //the last result we got from the server
    public String lastResult;

    private static final String LOG_TAG = "FlickrViewer";

    public static final String PREF_IMAGES = "pref_images";

    private static final String SERVER_URL= "https://api.flickr.com/services/feeds/photos_public.gne?format=json";

    // Uploader.
    private ServerCall uploader;

    private class ListElement {
        ListElement() {};

        public String textLabel;
        public String imageLink;
    }

    private ArrayList<ListElement> aList;

    private class MyAdapter extends ArrayAdapter<ListElement> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fill the title, timestamp
            TextView tv = (TextView) newView.findViewById(R.id.itemText);
            tv.setText(w.textLabel);

            // Fill the image using DownloadImageTask class at end of this file
            // ImageView i = (ImageView) newView.findViewById(R.id.imageView);
            SmartImageView i = (SmartImageView) newView.findViewById(R.id.imageView1);
            i.setImageUrl(w.imageLink);

            // new DownloadImageTask(i).execute(w.imageLink);


            // Sets a listener for the button, and a tag for the button as well.
            // toasts the item index
            //b.setTag(new Integer(position));
            i.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Reacts to a button press.

                    // Gets the message to share.
                    // ListElement msg = getItem(((Integer)v.getTag()).intValue()).textLabel;

                    //String s = v.getTag().toString();
                    Toast toast = Toast.makeText(context, "boop", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

            // Set a listener for the whole list item.
            // toasts the message content
            newView.setTag(w.textLabel);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = v.getTag().toString();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, s, duration);
                    toast.show();
                }
            });

            return newView;
        }
    }

    private MyAdapter aa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aList = new ArrayList<>();
        aa = new MyAdapter(this, R.layout.list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void clickRefresh(View v){
        ProgressBar pbar = (ProgressBar) findViewById(R.id.progressBar);
        pbar.setVisibility(View.VISIBLE);
        refreshList();
    }
    public void refreshList(){

        RefreshImagesSpec myCallSpec = new RefreshImagesSpec();
        myCallSpec.url = SERVER_URL;
        myCallSpec.context = MainActivity.this;

        // Let's add the parameters.
        HashMap<String, String> m = new HashMap<String, String>();

        myCallSpec.setParams(m);

        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);


    }

    class RefreshImagesSpec extends ServerCallSpec {
        @Override
        public void useResult(Context context, String result) {
            if (result == null){
                //display toast saying the server can't be reached
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "can't reach server", duration);
                toast.show();
            }else {
                // Translates the string result, decoding the Json.
                Log.i(LOG_TAG, "Received string: " + result);
                displayResult(result);
                // Stores in the settings the last images received.
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_IMAGES, result);
                editor.commit();

                // rm spinner, toast 'message sent', rm message text, set lastResult
                ProgressBar pbar = (ProgressBar) findViewById(R.id.progressBar);
                pbar.setVisibility(View.INVISIBLE);
                Toast toast = Toast.makeText(context, "Images updated", Toast.LENGTH_SHORT);
                toast.show();
                lastResult = result;

            }
        }
    }

    private void displayResult(String resultj) {
        // chop off unnecessary bits of string for gson
        String result = resultj.substring("jsonFlickrFeed(".length(),resultj.length()-1);
        result = result.substring(0, result.length()-1);
        Log.i(LOG_TAG,result);

        Gson gson = new Gson();
        // takes result string from server in JSON
        // converts to MessageList using GSON
        ImageInfo im = gson.fromJson(result, ImageInfo.class);
        // Fills aList(global object), so we can fill the listView.
        aList.clear();
        // creates a new ListElement for each item in MessageList
        for (int i = 0; i < im.items.length; i++) {
            ListElement ael = new ListElement();
            ael.textLabel = im.items[i].title+"\n"+im.items[i].author;
            ael.imageLink = im.items[i].media.m;
            aList.add(ael);
        }
        aa.notifyDataSetChanged();
    }


}
