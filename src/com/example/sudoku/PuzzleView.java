package com.example.sudoku;





import java.util.Currency;

import android.R.color;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

public class PuzzleView extends View {
	private static final String TAG = "Sudoku";
	private final Game game;
	public static final int scoreOffset = 100;
	public int noOfRounds;
	public PuzzleView(Context context) {
		super(context);
		this.game = (Game) context;
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	private float width;
	private float height;
	private int selX;
	private int selY;
	private final Rect selRect = new Rect();
	
	int score=0;
	int oppScore=0;
	String oppDeviceName="Opponent";
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		h=h-scoreOffset;
		int min = Math.min(h,w);
		width = min / 9f;
		height = min / 9f;

		getRect(selX, selY, selRect);
		Log.d(TAG, "onSizeChanged: width " + width + " height " + height);
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	private void getRect(int x, int y, Rect rect) {
		rect.set((int) (x * width), (int) (y * height), (int) (x * width + width), (int) (y * height + height));
	}
	

	@SuppressLint({ "DrawAllocation", "ResourceAsColor" })
	@Override
	protected void onDraw(Canvas canvas) {
		
		// Draw the background
		Log.d("solving tiles",Game.solving_tiles+"");
		if(Game.solving_tiles!=0){
		Paint background = new Paint();
		background.setColor(getResources().getColor(R.color.puzzle_background));
		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
		
		// Draw the board
		// Definte colors for the grid lines
		Paint dark = new Paint();
		dark.setColor(getResources().getColor(R.color.puzzle_dark));
		dark.setStrokeWidth(5);
		Paint hilite = new Paint();
		hilite.setColor(getResources().getColor(R.color.puzzle_hilite));
		
		Paint light = new Paint();
		light.setColor(getResources().getColor(R.color.puzzle_dark));
		
		// Draw the minor grid lines
		for (int i = 0; i < 9; i++) {
			canvas.drawLine(0, i * height, width*9, i*height, light);
			canvas.drawLine(0, i * height + 1, width*9, i * height + 1, hilite);
			canvas.drawLine(i * width, 0, i * width, height*9, light);
			canvas.drawLine(i * width + 1, 0, i * width + 1, height*9, hilite);
		}
		
		// Draw the major grid lines
		for (int i = 0; i < 9; i++) {
			if (i % 3  != 0)
				continue;
			canvas.drawLine(0, i * height, width*9, i * height, dark);
//			canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
			canvas.drawLine(i * width, 0, i * width, height*9, dark);
//			canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
		}
		canvas.drawLine( (9 * width), 0,  (9 * width), height*9, dark);
		canvas.drawLine(0, 9 * height, width*9, 9 * height, dark);
		// Draw the numbers
		// Define color and style for numbers
		Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		foreground.setColor(getResources().getColor(R.color.puzzle_foreground));
		foreground.setStyle(Style.FILL);
		foreground.setTextSize(height * 0.75f);
		foreground.setTextScaleX(width / height);
		foreground.setTextAlign(Paint.Align.CENTER);
		
		Paint colored_foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		colored_foreground.setColor(getResources().getColor(R.color.color));
		colored_foreground.setStyle(Style.FILL);
		colored_foreground.setTextSize(height * 0.75f);
		colored_foreground.setTextScaleX(width / height);
		colored_foreground.setTextAlign(Paint.Align.CENTER);

		
		// Draw the number in the center of the tile
		FontMetrics fm = foreground.getFontMetrics();
		// Centering on X: use alignment (and X at midpoint)
		float x = width / 2;
		// Centering on Y: measure ascent/descent first
		float y = height / 2 - (fm.ascent + fm.descent) / 2;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if(Game.originalPuzzle[j * 9 + i]==0){
					canvas.drawText(this.game.getTileString(i, j), i * width + x, j * height + y, colored_foreground);

				}else
				canvas.drawText(this.game.getTileString(i, j), i * width + x, j * height + y, foreground);
			}
		}
		
		
		// Draw the hints
		// Pick a hint color based on #moves left
//		Paint hint = new Paint();
//		int c[] = { getResources().getColor(R.color.puzzle_hint_0),
//				getResources().getColor(R.color.puzzle_hint_1),
//				getResources().getColor(R.color.puzzle_hint_2), };
//		Rect r = new Rect();
//		for (int i = 0; i < 9; i++) {
//			for (int j = 0; j < 9; j++) {
//				int movesleft = 9 - game.getUsedTiles(i, j).length;
//				if (movesleft < c.length) {
//					getRect(i, j, r);
//					hint.setColor(c[movesleft]);
//					canvas.drawRect(r, hint);
//				}
//			}
//		}
		// Draw the selection
		Log.d(TAG, "selRect=" + selRect);
		Paint selected = new Paint();
		selected.setColor(getResources().getColor(R.color.puzzle_selected));
		canvas.drawRect(selRect, selected);
		//draw score
		Paint scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		scorePaint.setColor(getResources().getColor(R.color.puzzle_foreground));
		scorePaint.setStyle(Style.FILL);
		scorePaint.setTextSize(height/2f);
		scorePaint.setStrokeWidth(3);
		scorePaint.setTextScaleX(width/ height);
		scorePaint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(
				"Number of rounds left: "
						+ game.noOfRounds
						,
				width+100, getHeight() - scoreOffset + 30, scorePaint);
		
		scorePaint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(
				"My Score: "
						+ score
						,
				width+90, getHeight() - scoreOffset + 60, scorePaint);
		canvas.drawText(
				
						 oppDeviceName+" score: "
						+ oppScore
						
						,
				width+90, getHeight() - scoreOffset + 90, scorePaint);
		}else{
			if(noOfRounds==1){
			if(score>oppScore){
			   Toast.makeText(getContext(), "You Won",
                       Toast.LENGTH_SHORT).show();	
			}else if(oppScore>score){
				 Toast.makeText(getContext(), "You Lost",
	                       Toast.LENGTH_SHORT).show();	
			}else{
				 Toast.makeText(getContext(), "Tie",
	                       Toast.LENGTH_SHORT).show();	
				
			}
		}
		}
		
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event=" + event);
		switch(keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			select(selX, selY - 1);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			select(selX, selY + 1);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			select(selX - 1, selY);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			select(selX + 1, selY);
			break;
		case KeyEvent.KEYCODE_0:
	    case KeyEvent.KEYCODE_SPACE: setSelectedTile(0); break;
	    case KeyEvent.KEYCODE_1:     setSelectedTile(1); break;
	    case KeyEvent.KEYCODE_2:     setSelectedTile(2); break;
	    case KeyEvent.KEYCODE_3:     setSelectedTile(3); break;
	    case KeyEvent.KEYCODE_4:     setSelectedTile(4); break;
	    case KeyEvent.KEYCODE_5:     setSelectedTile(5); break;
	    case KeyEvent.KEYCODE_6:     setSelectedTile(6); break;
	    case KeyEvent.KEYCODE_7:     setSelectedTile(7); break;
	    case KeyEvent.KEYCODE_8:     setSelectedTile(8); break;
	    case KeyEvent.KEYCODE_9:     setSelectedTile(9); break;
	    case KeyEvent.KEYCODE_ENTER:
	    case KeyEvent.KEYCODE_DPAD_CENTER:
	       game.showKeypadOrError(selX, selY);
	       break;

		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	private void select(int x, int y) {
		invalidate(selRect);
		selX = Math.min(Math.max(x, 0), 8);
		selY = Math.min(Math.max(y, 0), 8);
		getRect(selX, selY, selRect);
		invalidate(selRect);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return super.onTouchEvent(event);
	
		select((int) (event.getX() / width),
				(int) (event.getY() / height));
		game.showKeypadOrError(selX, selY);
		Log.d(TAG, "onTouchEvent: x" + selX + ", y" + selY);
		return true;
	}

	public void setSelectedTile(int tile) {
		if (game.setTileIfValid(selX, selY, tile)) {
			invalidate();//may change hints
		} else {
			// Number is not valid for this tile
			Log.d(TAG, "setSelectedTile: invalid: " + tile);
			startAnimation(AnimationUtils.loadAnimation(game, R.anim.shake));
		}
	}
}