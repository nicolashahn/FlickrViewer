package com.nicolashahn.flickrviewer;

/**
 * Created by Nick on 4/26/2015.
 * The object to hold the JSON data we get from the Flickr API call
 */
public class ImageInfo{
    public ImageInfo() {}

//    String title;
//    String link;
//    String description;
//    String modified;
//    String generator;
    ImageObj[] items;

    public class ImageObj{
        public class MediaObj{
            String m;
        }
        String title;
//        String link;
        MediaObj media;
//        String date_taken;
//        String description;
//        String published;
        String author;
//        String author_id;
//        String tags;
    }

}
