
package com.ultraswift.brainrecall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.*;

import bennet_za.followme.R;

/**
 * @author Bennet Makwakwa - Bennet_ZA (twitter)
 * Presents the GUI by which the player plays the game.
 * Upon pressing start, the player observes a random sequence
 * of simulated button presses, then must repeat the sequence
 * to move on to the next level.  At each level the sequence
 * grows by one click and the sequence is randomized.
 */
public class FollowMeGameActivity extends Activity 
{
	/**
	 * The set of state in which the game can reside.
	 */
	private enum GameState 
	{
		GameOver,
		ComputerTurn,
		PlayerTurn
	}
	
	/**
	 * Fields
	 */
	private GameState mState = GameState.GameOver;
	
	private Button[] mButtons = new Button[4];
	private TextView mLevelTextView;
	private TextView mStatusTextView;
	private ImageButton mStartButton;
	
	private List<Integer> mComputerClicks = new ArrayList<Integer>();
	private int mPlayerClick = 0;
	
	/**
     * Called when the activity is first created. 
     *
     * @param  savedInstanceState  If the activity is being re-initialized after previously being shut down
     *                             then this Bundle contains the data it most recently supplied in
     *                             onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        
        mButtons[0] = (Button) findViewById(R.id.button0);
        mButtons[1] = (Button) findViewById(R.id.button1);
        mButtons[2] = (Button) findViewById(R.id.button2);
        mButtons[3] = (Button) findViewById(R.id.button3);
        mLevelTextView = (TextView) findViewById(R.id.game_level);
        mStatusTextView = (TextView) findViewById(R.id.game_status);
        mStartButton = (ImageButton) findViewById(R.id.start_game);
        
        setButtonsClickable(false);
    }
    
    /**
     * Called whenever the first button is clicked. 
     *
     * @param  view  The view that was clicked.  This is unused.
     */
    public void button0Clicked(View view) {
    	buttonClicked(0);
    }
    
    /**
     * Called whenever the second button is clicked. 
     *
     * @param  view  The view that was clicked.  This is unused.
     */
    public void button1Clicked(View view) {
    	buttonClicked(1);
    }
    
    /**
     * Called whenever the third button is clicked. 
     *
     * @param  view  The view that was clicked.  This is unused.
     */
    public void button2Clicked(View view) {
    	buttonClicked(2);
    }
    
    /**
     * Called whenever the fourth button is clicked. 
     *
     * @param  view  The view that was clicked.  This is unused.
     */
    public void button3Clicked(View view) {
    	buttonClicked(3);
    }
    
    /**
     * Called whenever the start button is clicked. 
     *
     * @param  view  The view that was clicked.  This is unused.
     */
    public void startGame(View view) {
    	mStartButton.setEnabled(false);
    	doComputerTurn();
    }
    
    /**
     * Sets the state of the game. 
     *
     * @param  newState  The new state for the game.
     */
    private void setState(GameState newState) {
    	mState = newState;
    }
    
    /**
     * Sets the game buttons to click-able or not, as indicated by the parameter. 
     *
     * @param  clickable  A value indicating whether the buttons should be clickable.
     */
    private void setButtonsClickable(Boolean clickable) 
    {
    	for (int i = 0; i < mButtons.length; ++i) {
			mButtons[i].setClickable(clickable);
		}
    }
    
    /**
     * Called whenever one of the game buttons is clicked.
     * If it is the player's turn, the index of the button clicked
     * is compared to the saved computer clicks to determine if
     * the player clicked the correct button in sequence. If the
     * player clicks all buttons in the correct sequence, another
     * computer turn is started.  If the player clicks an
     * incorrect button, the game is ended.
     *
     * @param  button  The zero-based button identifier.
     */
    private void buttonClicked(int button) 
    {
    	if (mState != GameState.PlayerTurn) {
    		return;
    	}
    	
		if (mComputerClicks.get(mPlayerClick) == button) {
    		++mPlayerClick;
			if (mComputerClicks.size() == mPlayerClick) {
	    		doComputerTurn();
			}
		}
		else {
			doGameOver();
		}
    }
    
    /**
     * Starts a computer turn in which a sequence of button clicks
     * are simulated and recorded. 
     */
    private void doComputerTurn() {
    	setState(GameState.ComputerTurn);
    	setButtonsClickable(false);
    	
    	int clicks = mComputerClicks.size() + 1;
    	mComputerClicks.clear();
    	
    	mLevelTextView.setText("Level " + clicks);
    	
    	mStatusTextView.setText(R.string.get_ready);
    	mStatusTextView.setTextColor(getResources().getColor(R.color.light_red));
    	
    	new ComputerTurnTask().execute(clicks);
    }
    
    /**
     * Ends the current game. 
     */
    private void doGameOver() {
		mStatusTextView.setText(R.string.game_over);
		
		setButtonsClickable(false);
		
		setState(GameState.GameOver);
    	mStatusTextView.setTextColor(getResources().getColor(R.color.white));
		
    	saveHighScore(mComputerClicks.size());
		
    	mComputerClicks.clear();
    	
    	mStartButton.setEnabled(true);
	}
    
    /**
     * Saves the player's score, if it is greater than the lowest score
     * in the high scores list.
     * 
     * @param score    The player's score
     */
    private void saveHighScore(final int score) 
    {
    	if (!SavedData.isHighScore(score)) {
    		return;
    	}
    	
    	final EditText input = new EditText(this);
    	input.setText(SavedData.getLastPlayer());

    	new AlertDialog.Builder(this)
    	    .setTitle("GAME OVER! , Top Score Enter Your Name")
    	    .setView(input)
    	    .setPositiveButton("Save Score", new DialogInterface.OnClickListener() {
    	         public void onClick(DialogInterface dialog, int whichButton) {
    	             String name = input.getText().toString(); 
    	             SavedData.saveScore(name, score);
    	         }
    	    }).show();
    }
    
    /**
     * Generates and displays a randomly chosen button sequence.
     */
    private class ComputerTurnTask extends AsyncTask<Integer, Integer, Void> {
    	/**
    	 * Fields
    	 */
    	Random mRandom = new Random();
    	
    	/**
    	 * Generates and publishes a random button sequence.
    	 * 
    	 * @param	clicks	 An array of size one where the only element is
    	 *                   the number of clicks to use in the sequence.
    	 */
    	protected Void doInBackground(Integer... clicks) {
    		delay(1500);
    		
    		int computerClicks = clicks[0];

    		int clickDelay = getClickDelay(computerClicks);
    		int pressDuration = getPressDuration(computerClicks);
    		
    		for (int i = 0; i < computerClicks; ++i) {
        		delay(clickDelay);
        		int buttonIndex = mRandom.nextInt(mButtons.length);
        		mComputerClicks.add(buttonIndex);
        		publishProgress(buttonIndex);
        		delay(pressDuration);
        		publishProgress(buttonIndex);
        	}
        	
        	return null;
    	}
    	
    	/**
    	 * Updates the display in response to the publication of a single
    	 * button used in the sequence.
    	 * 
    	 * @param	clicks	 An array of size one where the only element is
    	 *                   the number of clicks to use in the sequence.
    	 */
    	protected void onProgressUpdate(Integer... buttonIndexes) {
    		int buttonIndex = buttonIndexes[0];
    		
    		Button button = mButtons[buttonIndex];
    		button.setPressed(!button.isPressed());
    		button.invalidate();
        	
    	}
    	
    	/**
    	 * Completes the generation of a button sequence and begins
    	 * the player's turn.
    	 * 
    	 * @param	unused	 This parameter is unused.
    	 */
    	protected void onPostExecute(Void unused) {
    		mPlayerClick = 0;
    		
    		setState(GameState.PlayerTurn);
    		
    		setButtonsClickable(true);
    		
        	mStatusTextView.setText(R.string.player_turn);
        	mStatusTextView.setTextColor(getResources().getColor(R.color.green));
    	}
    	
    	/**
    	 * Determines the amount of time to wait between simulated button
    	 * clicks, based on the current game level.
    	 * 
    	 * @param	level	 The current game's level.
    	 */
    	private int getClickDelay(int level) {
    		int delay = 400;
    		
    		if (level <= 4) {
	    		delay = 600;
    		}
    		
    		return delay;
    	}
        
    	/**
    	 * Determines the amount of time to leave a simulated button
    	 * press in the pressed state, based on the current game level.
    	 * 
    	 * @param	level	 The current game's level.
    	 */
    	private int getPressDuration(int level) {
    		int duration = 300;
    		
    		if (level <= 7) {
    			duration = 500;
    		}
    		
    		return duration;
    	}
        
    	/**
    	 * Pauses the thread and eats the InterruptedException.
    	 * 
    	 * @param	millis	 The number of milliseconds to pause.
    	 */
    	private void delay(int millis)
    	{
        	try
        	{
        		Thread.sleep(millis);
        	}
        	catch (InterruptedException ex)
        	{
        	}
        }
    }
}
