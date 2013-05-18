package com.example.sudoku;


import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.widget.TextView;
import android.widget.Toast;

public class Game extends Activity {
	private static final String TAG = "Sudoku";

	public static final String KEY_DIFFICULTY = "com.example.sudoku.difficulty";
	public static final String NoOfRounds = "com.example.sudoku.norounds";
	public static int DIFFICULTY = 0;

	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HARD = 2;

	public static int[] originalPuzzle;
	public static int puzzle[];
	public static int[][] arr;
	public static int solving_tiles;
	public static boolean new_game;

	private PuzzleView puzzleView;

	/**Sounds
	 * 
	 */
	static private SoundPool sounds;
	static private int right;
	static private int wrong;
	static private int loseSound;
	static private int winSound;
	static private int Tie;
	static private int endOfRound;
	static private int hurry;
	boolean hurryPlayed=false;
	boolean initialized = false;
	// Layout Views
//    private TextView mTitle;
	/*
	 * Bluetooth Attributes
	 */
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	// private SharedPreferences preferences;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;

	int score = 0;
	int oppScore = 0;

	boolean admin = false;

	int noOfRounds;

	// Timer
	long initialTimer;
	long currentTimer;

	TimerThread myTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		 // Set up the custom title
      
		if (new_game ) {
			//
			noOfRounds = getIntent().getIntExtra(NoOfRounds, noOfRounds);

			puzzle = new int[9 * 9];
			originalPuzzle = new int[9 * 9];
			DIFFICULTY = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY);
			Log.d("amira", "" + DIFFICULTY);
			setNoHiddenTiles();
			

				// generate sudoku
				Logic l = new Logic();
				arr = l.save();
				Log.d("solving array ", tostring(arr));
				int[][] hide;
				hide = l.hide(DIFFICULTY);
				// convert it to 1D array
				for (int i = 0; i < hide.length; i++) {
					for (int j = 0; j < hide.length; j++) {
						puzzle[j * 9 + i] = hide[j][i];
					}
				}
				for (int i = 0; i < puzzle.length; i++) {
					originalPuzzle[i] = puzzle[i];
				}
				@SuppressWarnings("deprecation")
				final Object data = getLastNonConfigurationInstance();
				if (data != null)
					puzzle = (int[]) data;
				super.onCreate(savedInstanceState);
				Log.d(TAG, "onCreate");
				puzzleView = new PuzzleView(this);
				puzzleView.noOfRounds = noOfRounds;
				setContentView(puzzleView);
				puzzleView.requestFocus();
				Sudoku.puzzle = puzzle;
				Sudoku.original_puzzle = originalPuzzle;
				Sudoku.arr = arr;
				Sudoku.difficulty = DIFFICULTY;
				Sudoku.solved_tiles = solving_tiles;

//				  mTitle = (TextView) findViewById(R.id.title_left_text);
//			        mTitle.setText(R.string.app_name);
//			        mTitle = (TextView) findViewById(R.id.title_right_text);
				// If the adapter is null, then Bluetooth is not supported
				if (mBluetoothAdapter == null) {
					Toast.makeText(this, "Bluetooth is not available",
							Toast.LENGTH_LONG).show();
					finish();
					return;
				}
				if (mChatService == null) {
					setupChat();
				}
				
				// sounds
				sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
				right = sounds.load(this, R.raw.pcmouseclick1, 1);
				wrong = sounds.load(this, R.raw.doh, 1);
				Tie = sounds.load(this, R.raw.finish, 1);
				winSound = sounds.load(this, R.raw.win, 1);
				loseSound = sounds.load(this, R.raw.lose, 1);
				endOfRound = sounds.load(this, R.raw.round, 1);
//				hurry=sounds.load(this, R.raw.hurry, 1);
				initializeTimer();
			

			
		} else {
			super.onCreate(savedInstanceState);
			Log.d(TAG, "onCreate");
			puzzleView = new PuzzleView(this);
			Log.d(TAG, toPuzzleString(puzzle));
			Log.d(TAG, toPuzzleString(originalPuzzle));
			setContentView(puzzleView);
			puzzleView.requestFocus();

		}
		new_game = false;
		// Get local Bluetooth adapter
		// mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//
		// // If the adapter is null, then Bluetooth is not supported
		// if (mBluetoothAdapter == null) {
		// Toast.makeText(this, "Bluetooth is not available",
		// Toast.LENGTH_LONG).show();
		// finish();
		// return;
		// }

	}

	public void onStart() {
		super.onStart();

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null) {
				setupChat();
			}
		}
//		sounds.play(endOfRound, 1.0f,1.0f, 0, 0, 1.5f);
	}

	
	@Override
	public synchronized void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}else{
			setupChat();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if(D) Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				// Toast.makeText(this, R.string.bt_not_enabled_leaving,
				// Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void setupChat() {
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
		sendMessage(encodeMsgToSend(false));
	}

	protected void showKeypadOrError(int x, int y) {
		// check if empty cell
		if (puzzle[y * 9 + x] == 0) {
			Dialog v = new Keypad(this, x, y, puzzleView);
			v.show();
		} else {
			Toast toast = Toast.makeText(this, R.string.no_moves_label,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}

	}

	protected boolean setTileIfValid(int x, int y, int value) {
		if (isvalid(x, y, value)) {
			sounds.play(right, 1.0f,
					1.0f, 0, 0, 1.5f);
			score=score+DIFFICULTY+1;
			puzzleView.score = score;
			puzzleView.invalidate();
			setTile(x, y, value);
			Log.d("puzzle", toPuzzleString(puzzle));

			return true;
		} else {
			sounds.play(wrong, 1.0f,
					1.0f, 0, 0, 1.5f);
			score=score-(DIFFICULTY+1);
			puzzleView.score = score;
			puzzleView.invalidate();
			String scoremsg = "s " + score;
			sendMessage(scoremsg);
			return false;
		}

	}

	static private String toPuzzleString(int[] puz) {
		StringBuilder buf = new StringBuilder();
		for (int element : puz) {
			buf.append(element);
		}
		return buf.toString();
	}

	static private String tostring(int[][] puz) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < puz.length; i++) {
			for (int j = 0; j < puz.length; j++) {
				buf.append(puz[i][j] + " ");
			}
			buf.append("\n");
		}
		return buf.toString();
	}

	static protected int[] fromPuzzleString(String string) {
		int[] puz = new int[string.length()];
		for (int i = 0; i < puz.length; i++) {
			puz[i] = string.charAt(i) - '0';
		}
		return puz;
	}

	private int getTile(int x, int y) {
		return puzzle[y * 9 + x];
	}

	private void setTile(int x, int y, int value) {
		puzzle[y * 9 + x] = value;
		solving_tiles--;
		Sudoku.puzzle = puzzle;
		// send the current puzzle to opponent
		sendMessage(encodeMsgToSend(false));
		if (solving_tiles == 0) {
			if(myTimer!=null){
			myTimer.cancel();
			}
			if (noOfRounds <= 1) {
				
				if(score==oppScore){
					Intent intent = new Intent(Game.this, Sudoku.class);
					startActivity(intent);
					sounds.play(Tie, 1.0f,
							1.0f, 0, 0, 1.5f);
//					sounds.autoPause();
				}else if(oppScore>score){
					Intent intent = new Intent(Game.this, Sudoku.class);
					startActivity(intent);
					sounds.play(loseSound, 1.0f,
							1.0f, 0, 0, 1.5f);
//					sounds.autoPause();
				}else if (score > oppScore) {
					sounds.play(winSound, 1.0f,
							1.0f, 0, 0, 1.5f);
					Intent intent = new Intent(Game.this, HomeActivity.class);
					intent.putExtra("score", score);
					startActivity(intent);
//					sounds.autoPause();
				}
			} else {
				reInit();
				noOfRounds--;
				
				puzzleView.noOfRounds = noOfRounds;
				// if (admin) {
				sendMessage(encodeMsgToSend(true));
				// sendMessage("n " + noOfRounds);

				// }

			}
		}

	}

	private void reInit() {
		//
		// myTimer = new TimerThread();
		// myTimer.run();

		Logic l = new Logic();
		arr = l.save();
		Log.d("solving array ", tostring(arr));
		int[][] hide;
		hide = l.hide(DIFFICULTY);
		// convert it to 1D array
		for (int i = 0; i < hide.length; i++) {
			for (int j = 0; j < hide.length; j++) {
				puzzle[j * 9 + i] = hide[j][i];
			}
		}
		for (int i = 0; i < puzzle.length; i++) {
			originalPuzzle[i] = puzzle[i];
		}
		setNoHiddenTiles();
		puzzleView = new PuzzleView(this);
		setContentView(puzzleView);
		puzzleView.requestFocus();
		puzzleView.invalidate();
		puzzleView.score = score;
		puzzleView.oppScore = oppScore;
		Sudoku.puzzle = puzzle;
		Sudoku.original_puzzle = originalPuzzle;
		Sudoku.arr = arr;
		Sudoku.difficulty = DIFFICULTY;
		Sudoku.solved_tiles = solving_tiles;
		puzzleView.noOfRounds = noOfRounds;
		initializeTimer();

	}

	private String encodeMsgToSend(boolean initTimer) {
		String msg = "";
		// put puzzle
		msg = toPuzzleString(puzzle);
		msg = msg + " ";
		// put 2D array
		int[] myArr = convert2Darrto1D();
		msg = msg + toPuzzleString(myArr);
		msg = msg + " ";
		// put original array
		msg = msg + toPuzzleString(originalPuzzle);

		// score
		msg = msg + " " + score;
		// number of tiles
		msg = msg + " " + solving_tiles;
		// no of rounds
		msg = msg + " " + noOfRounds;
		// // initial timer
		msg = msg + " " + initTimer;

		return msg;
	}

	private void decodeMsgReceieved(String msg) {
		if (msg.charAt(0) == 's') {

			Log.d("Score", msg);
			String oppScoreS = msg.substring(2, msg.length());
			oppScore = Integer.parseInt(oppScoreS);
			puzzleView.score = score;
			puzzleView.oppScore = oppScore;
		} else {
			String[] result = msg.split(" ");
			puzzle = fromPuzzleString(result[0]);
			String array = result[1];
			int[] array1 = fromPuzzleString(array);

			arr = convert1Darrayto2D(array1);
			originalPuzzle = fromPuzzleString(result[2]);

			oppScore = Integer.parseInt(result[3]);
			Log.d("OppS", oppScore + "");
			puzzleView.oppScore = oppScore;

			solving_tiles = Integer.parseInt(result[4]);

			noOfRounds = Integer.parseInt(result[5]);
			puzzleView.noOfRounds = noOfRounds;
			String initTimer = result[6];

			if (initTimer.equals("true")) {
				initializeTimer();
			}
			// initialTimer = Long.parseLong(result[6]);
			if (solving_tiles == 0) {
				if (noOfRounds == 1) {
					
					if (score == oppScore || oppScore > score) {
						Intent intent = new Intent(Game.this, Sudoku.class);
						startActivity(intent);
						Log.d("Check", "yessss");
					}
				}
			}
			puzzleView.invalidate();
		}
	}

	private int[] convert2Darrto1D() {
		int[] newArr = new int[9 * 9];
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr.length; j++) {
				newArr[j * 9 + i] = arr[j][i];
			}
		}
		return newArr;
	}

	private int[][] convert1Darrayto2D(int[] array) {
		int[][] newArray = new int[9][9];
		for (int i = 0; i < newArray.length; i++) {
			for (int j = 0; j < newArray.length; j++) {
				// newArr[j * 9 + i] = arr[j][i];
				newArray[j][i] = array[j * 9 + i];
			}
		}
		return newArray;
	}

	protected String getTileString(int x, int y) {

		int v = getTile(x, y);
		if (v == 0)
			return "";
		else
			return String.valueOf(v);
	}

	public static boolean isvalid(int x, int y, int tile) {
		Log.d("selection", "x  " + x + "y  " + y);
		if (arr[y][x] == tile) {
			Log.d("selection", "arr" + arr[y][x]);
			return true;

		} else
			return false;
	}

	public Object onRetainNonConfigurationInstance() {
		return puzzle;
	}

	public void setNoHiddenTiles() {
		if (DIFFICULTY == DIFFICULTY_EASY)
			solving_tiles = 1;
		else if (DIFFICULTY == DIFFICULTY_MEDIUM)
			solving_tiles = 40;
		else
			solving_tiles = 50;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	// done
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			admin = true;
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

			return true;
		case R.id.insecure_connect_scan:
			admin = true;
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent,
					REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
//			Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
//			mTitle.setText("Not connected");
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);

		}
	}

	private void ensureDiscoverable() {

		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private void initializeTimer() {

		
		initialTimer = System.currentTimeMillis();
		currentTimer = System.currentTimeMillis();
		if (myTimer != null) {
			myTimer.cancel();
		}
		if (DIFFICULTY == DIFFICULTY_EASY) {

			// TODO: change this initialization
			// initialize this for each diffucilty
			myTimer = new TimerThread(60 * 1000, 1000);
			//

		} else if (DIFFICULTY == DIFFICULTY_MEDIUM) {
			myTimer = new TimerThread(600 * 1000, 1000);
		} else if (DIFFICULTY == DIFFICULTY_HARD) {
			myTimer = new TimerThread(900000, 1000);
		}

		myTimer.onTick(100);

		myTimer.start();
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:

				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
//					 mTitle.setText("Connected to");
//	                    mTitle.append(mConnectedDeviceName);
					// mTitle.setText(R.string.title_connected_to);
					// mTitle.append(mConnectedDeviceName);
					// mConversationArrayAdapter.clear();
					if (!initialized) {
						initializeTimer();
						if (admin) {
							initialTimer = System.currentTimeMillis();
							// sendMessage(encodeMsgToSend());
							mChatService.write(encodeMsgToSend(false)
									.getBytes());

							// Reset out string buffer to zero and clear the
							// edit
							mOutStringBuffer.setLength(0);
						}
						initialized = true;
					}
					break;
				case BluetoothChatService.STATE_CONNECTING:
					// mTitle.setText(R.string.title_connecting);
//					 mTitle.setText("Connecting");
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
//					 mTitle.setText("not connected");
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				Log.d("Bluetooth", "Sent " + writeMessage);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				Log.d("Bluetooth", "Received " + readMessage);
				decodeMsgReceieved(readMessage);
				puzzleView.invalidate();
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				puzzleView.oppDeviceName = mConnectedDeviceName;
				puzzleView.invalidate();

				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	class TimerThread extends CountDownTimer {
		public TimerThread(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
			initialTimer=System.currentTimeMillis();
//			sounds.play(endOfRound, 1.0f,
//					1.0f, 0, 0, 1.5f);
		}

		private String getCurrentTime() {
			long noOfSecondsPassed = (currentTimer - initialTimer) / 1000;
			int mins = (int) (noOfSecondsPassed / 60);
			int seconds = (int) (noOfSecondsPassed % 60);
			String minutes=mins+"";
			String secs=seconds+"";
			if(mins<10){
				minutes="0"+mins;
			}
			if(seconds<10){
				secs="0"+seconds;
			}
			String timer = "Time " + minutes + ":" + secs;
			return timer;
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			
			
			
		 
				if (noOfRounds <= 1) {
				
//					sounds.autoPause();
					if (score > oppScore) {
						Toast.makeText(Game.this, "You Won", Toast.LENGTH_SHORT)
								.show();
					} else if (oppScore > score) {
						Toast.makeText(Game.this, "You Lost",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(Game.this, "Tie", Toast.LENGTH_SHORT)
								.show();

					}

					Intent intent = new Intent(Game.this, Sudoku.class);
//					intent.putExtra("score", score);
					startActivity(intent);

				}else{
				reInit();
				noOfRounds--;

				puzzleView.noOfRounds = noOfRounds;
		
				}

				if (admin) {
					
					
					// if (admin) {
					sendMessage(encodeMsgToSend(true));
					// sendMessage("n " + noOfRounds);

					// }
					
				} 
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			currentTimer = System.currentTimeMillis();
			boolean red = false;
			
			int left = (int) ((currentTimer - initialTimer) / 1000);
			if (DIFFICULTY == DIFFICULTY_EASY) {

				
				if ((60 - left) < 10) {
					hurryPlayed=true;
					red = true;
				}
			} else if (DIFFICULTY == DIFFICULTY_MEDIUM) {
				
				if ((60 * 10 - left) < 60) {
					hurryPlayed=true;
					red = true;
				}
			} else if (DIFFICULTY == DIFFICULTY_HARD) {
				
				if ((60 * 15 - left) < 60) {
					hurryPlayed=true;
					red = true;
				}
			}

			if(hurryPlayed){
//				sounds.play(hurry, 1.0f,
//						1.0f, 0, 0, 1.5f);
				hurryPlayed=false;
			}
			if (red) {
				setTitleColor(Color.RED);
			}else{
				setTitleColor(Color.WHITE);
			}
			setTitle(getCurrentTime());
		}

	};

}