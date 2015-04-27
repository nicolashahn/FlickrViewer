# FlickrViewer
Android application to view public photos

Pulls from this url: https://api.flickr.com/services/feeds/photos_public.gne?format=json
and converts these into a list of titles, authors, and thumbnails.
Tapping a list item opens up a fullscreen view of that image.

External packages used:
- Gson for server requests
- SmartImageView to display images via URL
