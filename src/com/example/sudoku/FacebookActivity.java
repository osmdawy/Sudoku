package com.example.sudoku;

import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.socialsharing.FacebookEventObserver;
import com.example.sudoku.Constants.Extra;
import com.example.sudoku.R;
import com.nostra13.socialsharing.common.AuthListener;
import com.nostra13.socialsharing.facebook.FacebookFacade;

/**
 * Activity for sharing information with Facebook
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class FacebookActivity extends Activity {

	private FacebookFacade facebook;
	private FacebookEventObserver facebookEventObserver;

	private TextView messageView;
	static int score;
	private String link;
	private String linkName;
	private String linkDescription;
	private String picture;
	private Map<String, String> actionsMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ac_facebook);

		facebook = new FacebookFacade(this, Constants.FACEBOOK_APP_ID);
		facebookEventObserver = FacebookEventObserver.newInstance();

		messageView = (TextView) findViewById(R.id.message);
		TextView linkNameView = (TextView) findViewById(R.id.link_name);
		TextView linkDescriptionView = (TextView) findViewById(R.id.link_description);
		Button postButton = (Button) findViewById(R.id.button_post);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String message = bundle.getString(Extra.POST_MESSAGE);
			link = bundle.getString(Extra.POST_LINK);
			linkName = bundle.getString(Extra.POST_LINK_NAME);
			linkDescription = bundle.getString(Extra.POST_LINK_DESCRIPTION);
			score = bundle.getInt("score");
			picture = bundle.getString(Extra.POST_PICTURE);
			actionsMap = new HashMap<String, String>();
			actionsMap.put(Constants.FACEBOOK_SHARE_ACTION_NAME, Constants.FACEBOOK_SHARE_ACTION_LINK);

			messageView.setText(message+" : "+Constants.score);
			linkNameView.setText(linkName);
			linkDescriptionView.setText(linkDescription);
		}

		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (facebook.isAuthorized()) {
					publishMessage();
					Intent intent = new Intent(FacebookActivity.this, Sudoku.class);
					startActivity(intent);
				} else {
					// Start authentication dialog and publish message after successful authentication
					facebook.authorize(new AuthListener() {
						@Override
						public void onAuthSucceed() {
							publishMessage();
							Intent intent = new Intent(FacebookActivity.this, Sudoku.class);
							startActivity(intent);
						}
						@Override
						public void onAuthFail(String error) { // Do noting
						}
					});
				}
			}
		});
	}

	private void publishMessage() {
		facebook.publishMessage(messageView.getText().toString(), link, linkName, linkDescription, picture, actionsMap);
	}

	@Override
	public void onStart() {
		super.onStart();
		facebookEventObserver.registerListeners(this);
		if (!facebook.isAuthorized()) {
			facebook.authorize();
		}
	}

	@Override
	public void onStop() {
		facebookEventObserver.unregisterListeners();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_facebook_twitter, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.item_logout:
				facebook.logout();
				return true;
			default:
				return false;
		}
	}
}
