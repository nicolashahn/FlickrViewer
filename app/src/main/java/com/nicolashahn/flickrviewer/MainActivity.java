package com.nicolashahn.flickrviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.List;

public class MainActivity extends Activity {

    //the last JSON string we got from the server
    public String lastResult;

    private static final String LOG_TAG = "FlickrViewer";

    // where we send the server call to request json object
    private static final String SERVER_URL= "https://api.flickr.com/services/feeds/photos_public.gne?format=json";

    private ServerCall uploader;

    private class ListElement {
        ListElement() {};

        public String titleLabel;
        public String authLabel;
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

            final ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fill the title, author
            TextView tv = (TextView) newView.findViewById(R.id.itemText);
            tv.setText(w.titleLabel);
            TextView tv1 = (TextView) newView.findViewById(R.id.itemText1);
            tv1.setText(w.authLabel);

            // Fill image using SmartImageView (external package)
            SmartImageView i = (SmartImageView) newView.findViewById(R.id.imageView1);
            i.setImageUrl(w.imageLink);

            i.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra("IMG_URL", w.imageLink);
                    startActivity(intent);
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
        // make server call on application load
        refreshList();
    }

    // refresh button starts progress spinner, refreshes list
    public void clickRefresh(View v){
        ProgressBar pbar = (ProgressBar) findViewById(R.id.progressBar);
        pbar.setVisibility(View.VISIBLE);
        refreshList();
    }

    // send a server call
    public void refreshList(){
        RefreshImagesSpec myCallSpec = new RefreshImagesSpec();
        myCallSpec.url = SERVER_URL;
        myCallSpec.context = MainActivity.this;
        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);
    }

    // send a call to the server, handle response
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
                //display image objects we got
                displayResult(result);
                // rm spinner, rm message text, set lastResult
                ProgressBar pbar = (ProgressBar) findViewById(R.id.progressBar);
                pbar.setVisibility(View.INVISIBLE);
                lastResult = result;
            }
        }
    }

    // Take the json string, create a list of objects for each json object
    // display them in the listView
    private void displayResult(String resultj) {
        // chop off unnecessary bits of string for gson
        String result = resultj.substring("jsonFlickrFeed(".length(),resultj.length()-2);
        Log.i(LOG_TAG,result);
        // takes result string from server in JSON
        // converts to ImageInfo using GSON
        Gson gson = new Gson();
        ImageInfo im = gson.fromJson(result, ImageInfo.class);
        // Fills aList(global object), so we can fill the listView.
        aList.clear();
        // creates a new ListElement for each Flickr image object
        for (int i = 0; i < im.items.length; i++) {
            ListElement ael = new ListElement();
            ael.titleLabel = im.items[i].title;
            // have to get rid of "nobody@flickr.com"
            String auth = im.items[i].author.substring("nobody@flickr.com (".length(), im.items[i].author.length()-1);
            ael.authLabel = auth;
            ael.imageLink = im.items[i].media.m;
            aList.add(ael);
        }
        aa.notifyDataSetChanged();
    }
}
