package com.codepath.instagramv1;

import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkmmte.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class InstaPhotosAdapter extends ArrayAdapter<InstaPhoto> {
	public InstaPhotosAdapter(Context context, List<InstaPhoto> photos) {
		super(context, R.layout.item_photo, photos);
	}

	// Takes a data item in the position and converts to row in listview
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// get the item from the position
		InstaPhoto photo = getItem(position);
		CharSequence postdate;
		
		// check if view is recycled
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_photo, parent, false);
		}
		
		// lookup the subviews
		TextView imgCaption = (TextView) convertView.findViewById(R.id.tvCaption);
		ImageView imgPhoto = (ImageView) convertView.findViewById(R.id.imagePhoto);
		TextView imgUser = (TextView) convertView.findViewById(R.id.tvUser);
		TextView imgTime = (TextView) convertView.findViewById(R.id.tvTime);
		TextView imgLikes = (TextView) convertView.findViewById(R.id.tvLikes);
		CircularImageView imgUserPic = (CircularImageView) convertView.findViewById(R.id.imgUser);
		TextView imgComments = (TextView) convertView.findViewById(R.id.tvComments);
	
		// Set the values
		// Get the username
		imgUser.setText(photo.username);
		
		// Get the create time
		postdate = DateUtils.getRelativeTimeSpanString(photo.createtime * 1000, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
		imgTime.setText(postdate);
		//imgTime.setBackgroundColor(0xFF00FF00);
		
		// Get the caption
		if (photo.caption == null) {
			imgCaption.setVisibility(convertView.GONE);
		} else {
			imgCaption.setText(Html.fromHtml("<font color=\"#206199\"><b>" + photo.username
                    + "  " + "</b></font>" + "<font color=\"#000000\">" + photo.caption + "</font>"));
		}
		
		// Set the comments
		if (photo.commcnt == 0) {
			imgComments.setVisibility(convertView.GONE);
		} else {
			String commtext1 = "<font color=\"#206199\"><b>" + "Comments" + "<br>" + "</b></font>" +
					"<font color=\"#206199\"><b>" + photo.commentuser1
                    + "  " + "</b></font>" + "<font color=\"#000000\">" + photo.comment1 + "</font>";
			String commtext2 = null;
			if (photo.commcnt == 2) {
				commtext2 = "<br>" + "<font color=\"#206199\"><b>" + photo.commentuser2
	                    + "  " + "</b></font>" + "<font color=\"#000000\">" + photo.comment2 + "</font>";
			}
			imgComments.setText(Html.fromHtml(commtext1 + commtext2));
		}
		
		// Get likes count
		imgLikes.setText(String.valueOf(photo.likes_count) + " likes");
		
		// Load profile picture
		if (photo.profimgurl != null) {
			Picasso.with(getContext()).load(photo.profimgurl).placeholder(R.drawable.default_avatar).into(imgUserPic);
		}
		
		// Instagram photo to fit Screen size
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenwidth = size.x;
		int screenheight = size.y;
		Log.d("DEBUG", "Instagram photo H:" + photo.imgheight + " W:" + photo.imgWidth + 
				"Screeen H:" + screenheight  + " W:" + screenwidth);

		/* Set the imageview layout params to the screen size width and aspect ratio */
		float fittedimgheight;
		float fittedimgwidth;
		fittedimgwidth = (float) screenwidth;
		fittedimgheight = fittedimgwidth * ((float) photo.imgheight / (float) photo.imgWidth);
		
		// Set the layout params to the newly calculated dimensions
		ViewGroup.LayoutParams iv_lparams = imgPhoto.getLayoutParams();
		iv_lparams.height = (int) fittedimgheight;
		iv_lparams.width = (int) fittedimgwidth;
		imgPhoto.setLayoutParams(iv_lparams);
		
		// cleanup subview if recycled to clear the previous image content
		imgPhoto.getLayoutParams().height = (int) fittedimgheight;
		imgPhoto.setImageResource(0);

		// fetch the photo from the url using Picassa asynchronously in background not in main thread
		// It downloads the imagebytes, converts to bitmap and loads the image
		//imgPhoto.setBackgroundColor(0xFF00FF00);
		Picasso.with(getContext()).load(photo.imgurl).fit().centerInside().into(imgPhoto);
		
		return convertView;
	}
	
}
