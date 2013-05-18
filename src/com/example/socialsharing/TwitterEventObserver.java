package com.example.socialsharing;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.widget.Toast;

import com.example.sudoku.R;
import com.nostra13.socialsharing.common.AuthListener;
import com.nostra13.socialsharing.common.LogoutListener;
import com.nostra13.socialsharing.common.PostListener;
import com.nostra13.socialsharing.twitter.TwitterEvents;

/**
 * Observes Twitter events (authentication, publishing, logging out) and shows appropriate {@link Toast toasts}. Use
 * {@link #registerListeners()} to start observe events and {@link #unregisterListeners()} to stop observing.<br />
 * <b>Good practice:</b> Call {@link #registerListeners()} at {@link Activity#onStart()} method and necessarily call
 * {@link #unregisterListeners()} at {@link Activity#onStop()} method
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class TwitterEventObserver {

	private Reference<Activity> context;

	private TwitterEventObserver() {
		context = new WeakReference<Activity>(null);
	}

	public static TwitterEventObserver newInstance() {
		return new TwitterEventObserver();
	}

	private AuthListener authListener = new AuthListener() {
		@Override
		public void onAuthSucceed() {
			showToastOnUIThread(R.string.toast_twitter_auth_success);
		}

		@Override
		public void onAuthFail(String error) {
			showToastOnUIThread(R.string.toast_twitter_auth_fail);
		}
	};

	private PostListener postListener = new PostListener() {
		@Override
		public void onPostPublishingFailed() {
			showToastOnUIThread(R.string.twitter_post_publishing_failed);
		}

		@Override
		public void onPostPublished() {
			showToastOnUIThread(R.string.twitter_post_published);
		}
	};

	private LogoutListener logoutListener = new LogoutListener() {
		@Override
		public void onLogoutComplete() {
			showToastOnUIThread(R.string.twitter_logged_out);
		}
	};

	private void showToastOnUIThread(final int textRes) {
		final Activity curActivity = context.get();
		if (curActivity != null) {
			curActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(curActivity, textRes, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	public void registerListeners(Activity context) {
		this.context = new WeakReference<Activity>(context);

		TwitterEvents.addAuthListener(authListener);
		TwitterEvents.addPostListener(postListener);
		TwitterEvents.addLogoutListener(logoutListener);
	}

	public void unregisterListeners() {
		context.clear();

		TwitterEvents.removeAuthListener(authListener);
		TwitterEvents.removePostListener(postListener);
		TwitterEvents.removeLogoutListener(logoutListener);
	}
}
