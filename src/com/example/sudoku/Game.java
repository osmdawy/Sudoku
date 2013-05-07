package com.example.sudoku;



import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class Game extends Activity {
	private static final String TAG = "Sudoku";

	public static final String KEY_DIFFICULTY = "com.example.sudoku.difficulty";
	public static int DIFFICULTY = 0;

	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HARD = 2;
	public static final String CONTINUE = "com.example.sudoku.continue";

	public static int[] originalPuzzle;
	public static int puzzle[];
	public static int[][] arr;
	public static int solving_tiles;
	public static boolean new_game;

	private PuzzleView puzzleView;

	/*Bluetooth Attributes
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
	// private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		if (new_game || puzzle == null) {
			puzzle = new int[9 * 9];
			originalPuzzle = new int[9 * 9];
			boolean continueGame = false;
			continueGame = getIntent().getBooleanExtra(CONTINUE, continueGame);
			DIFFICULTY = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY);
			Log.d("amira", "" + DIFFICULTY);
			setNoHiddenTiles();
			if (!continueGame) {
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
				setContentView(puzzleView);
				puzzleView.requestFocus();
				Sudoku.puzzle = puzzle;
				Sudoku.original_puzzle = originalPuzzle;
				Sudoku.arr = arr;
				Sudoku.difficulty = DIFFICULTY;
				Sudoku.solved_tiles = solving_tiles;
			} else {
				puzzle = Sudoku.puzzle;
				arr = Sudoku.arr;
				DIFFICULTY = Sudoku.difficulty;
				solving_tiles = Sudoku.solved_tiles;
				super.onCreate(savedInstanceState);
				Log.d(TAG, toPuzzleString(originalPuzzle));
				puzzleView = new PuzzleView(this);
				setContentView(puzzleView);
				puzzleView.requestFocus();
			}
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
			setTile(x, y, value);
			Log.d("puzzle", toPuzzleString(puzzle));
			return true;
		} else
			return false;

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

	    //done
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        Intent serverIntent = null;
	        switch (item.getItemId()) {
	        case R.id.secure_connect_scan:
	            // Launch the DeviceListActivity to see devices and do scan
	            serverIntent = new Intent(this, DeviceListActivity.class);
	            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
	            return true;
	        case R.id.insecure_connect_scan:
	            // Launch the DeviceListActivity to see devices and do scan
	            serverIntent = new Intent(this, DeviceListActivity.class);
	            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
	            return true;
	        case R.id.discoverable:
	            // Ensure this device is discoverable by others
//	            ensureDiscoverable();
	            return true;
	        }
	        return false;
	    }

}