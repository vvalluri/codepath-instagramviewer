package com.codepath.instagramv1;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;


public class PhotosActivity extends Activity {
	public static final String CLIENT_ID = "b008f314e4004692b103cc1a1f98f4bf";
	private ArrayList<InstaPhoto> photos;
	private InstaPhotosAdapter aphotos;
    private SwipeRefreshLayout swipeContainer;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
            	fetchPopularPhotos();
            } 
        });
        
        // Configure the refreshing colors
        swipeContainer.setColorSchemeColors(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);
        
        fetchPopularPhotos();
    }


    private void fetchPopularPhotos() {
    	photos = new ArrayList<InstaPhoto>();
    	// Create Adapter and bind it to photos list
    	aphotos = new InstaPhotosAdapter(this, photos);
    	
    	// get the listview from layout
    	ListView lvphotos = (ListView) findViewById(R.id.lvPhotos);
    	
    	// Bind adapter to the listview
    	lvphotos.setAdapter(aphotos);
    	
    	//https://api.instagram.com/v1/media/popular?client_id=b008f314e4004692b103cc1a1f98f4bf
		//json data->[x]->images->standard resolution->url
    	// setup url endpoint
    	String popularurl= "https://api.instagram.com/v1/media/popular?client_id=" + CLIENT_ID;
    	// Create network client
		AsyncHttpClient client = new AsyncHttpClient();
		// Trigger network request
		client.get(popularurl, new JsonHttpResponseHandler() {
			// response is json object for photos
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				//url, height, username, caption
				JSONArray photosJSON = null;

				try {
					//Clear the list first
					photos.clear();
					photosJSON = response.getJSONArray("data");
					Log.i("INFO", "INSTAGRAM DATA ARRAY LENGTH: " + photosJSON.length());
					//username:  data->[x]->user -> username
					//caption:   data->[x]->caption -> text
					//imgurl:    data->[x]->images -> standard_resolution -> url
					//imgheight: data->[x]->images -> standard_resolution -> height
					//likes:     data->[x]->likes -> count
					for (int i = 0; i < photosJSON.length(); i++) {
						JSONObject photoJSON = photosJSON.getJSONObject(i);
						JSONArray commentsJSON = null;
						
						
						if (photoJSON.optJSONObject("images") != null) {
							InstaPhoto iPhoto = new InstaPhoto();
							iPhoto.commcnt = 0;
						
							// ALso check for standard_resolution
							if (photoJSON.getJSONObject("images").optJSONObject("standard_resolution") != null) {
								iPhoto.imgurl = photoJSON.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
								iPhoto.imgheight = photoJSON.getJSONObject("images").getJSONObject("standard_resolution").getInt("height");
								iPhoto.imgWidth = photoJSON.getJSONObject("images").getJSONObject("standard_resolution").getInt("width");
							}
							
							if (photoJSON.optJSONObject("user") != null) {
								iPhoto.username = photoJSON.getJSONObject("user").getString("username");
								iPhoto.profimgurl = photoJSON.getJSONObject("user").getString("profile_picture");
							}
							
							if (photoJSON.optJSONObject("caption") != null) {
								iPhoto.caption = photoJSON.getJSONObject("caption").getString("text");
							}
						
							if (photoJSON.optJSONObject("likes") != null) {
								iPhoto.likes_count = photoJSON.getJSONObject("likes").getInt("count");
							}
							
							iPhoto.createtime = Long.valueOf(photoJSON.getString("created_time")).longValue();
							
							if (photoJSON.optJSONObject("comments") != null) {
								// data ->[x]->comments ->data ->[y] ->text
								// data ->[x]->comments ->data ->[y] ->from -> username
								
								if (photoJSON.getJSONObject("comments").optJSONArray("data") != null) {
									commentsJSON = photoJSON.getJSONObject("comments").getJSONArray("data");
									JSONObject commentJSON = null;
									Log.i("INFO", "INSTAGRAM COMMENTS ARRAY LENGTH: " + commentsJSON.length());
									
									for (int j = 0; j < commentsJSON.length(); j++) {
										if (j > 1) {
											break;
										}
										commentJSON = commentsJSON.getJSONObject(j);
										if (j == 0) {
											iPhoto.comment1 = commentJSON.getString("text");
											if (commentJSON.optJSONObject("from") != null) {
												iPhoto.commentuser1 = commentJSON.getJSONObject("from").getString("username");
											}
											iPhoto.commcnt = 1;
										} else {
											iPhoto.comment2 = commentJSON.getString("text");
											if (commentJSON.optJSONObject("from") != null) {
												iPhoto.commentuser2 = commentJSON.getJSONObject("from").getString("username");
											}
											iPhoto.commcnt = 2;
										}
										
										
									}
								}
							} else {
								iPhoto.comment1 = null;
								iPhoto.comment2 = null;
							}
							
							// add photo to the array
							photos.add(iPhoto);
						}
					}

					// Update adapter
					aphotos.notifyDataSetChanged();
					// ...the data has come back, finished populating listview...
	                // Now we call onRefreshComplete to signify refresh has finished
					 swipeContainer.setRefreshing(false);
				} catch (JSONException e) {
					Log.d("DEBUG", "Instagram popular photo fetch error: ");
					e.printStackTrace();
				}
			}
			
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			super.onFailure(statusCode, headers, responseString, throwable);
		}
			
		});
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photos, menu);
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
}
