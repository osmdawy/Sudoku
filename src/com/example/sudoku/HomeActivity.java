package com.example.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.socialsharing.FacebookEventObserver;
import com.example.socialsharing.TwitterEventObserver;
import com.example.sudoku.Constants.Extra;
import com.example.sudoku.R;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class HomeActivity extends Activity {

	private FacebookEventObserver facebookEventObserver;
	private TwitterEventObserver twitterEventObserver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Constants.score = getIntent().getIntExtra("score", Constants.score);
		Log.d("score",Constants.score+"");
		setContentView(R.layout.ac_home);
		findViewById(R.id.Button01).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, Sudoku.class);
				startActivity(intent);
			}
		});
		findViewById(R.id.button_share_facebook).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startFacebookActivity();
			}
		});
		findViewById(R.id.button_share_twitter).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startTwitterActivity();
			}
		});

		facebookEventObserver = FacebookEventObserver.newInstance();
		twitterEventObserver = TwitterEventObserver.newInstance();
	}

	@Override
	public void onStart() {
		super.onStart();
		facebookEventObserver.registerListeners(this);
		twitterEventObserver.registerListeners(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		facebookEventObserver.unregisterListeners();
		twitterEventObserver.unregisterListeners();
	}

	private void startFacebookActivity() {
		Intent intent = new Intent(this, FacebookActivity.class);
		intent.putExtra(Extra.POST_MESSAGE, Constants.FACEBOOK_SHARE_MESSAGE);
		intent.putExtra(Extra.POST_LINK, Constants.FACEBOOK_SHARE_LINK);
		intent.putExtra(Extra.POST_LINK_NAME, Constants.FACEBOOK_SHARE_LINK_NAME);
		intent.putExtra(Extra.POST_LINK_DESCRIPTION, Constants.FACEBOOK_SHARE_LINK_DESCRIPTION);
		intent.putExtra(Extra.POST_PICTURE, Constants.FACEBOOK_SHARE_PICTURE);
		intent.putExtra("score", Constants.score);

		startActivity(intent);
	}

	private void startTwitterActivity() {
		Intent intent = new Intent(this, TwitterActivity.class);
		intent.putExtra(Extra.POST_MESSAGE, Constants.TWITTER_SHARE_MESSAGE);
		intent.putExtra("score", Constants.score);

		startActivity(intent);
	}
}