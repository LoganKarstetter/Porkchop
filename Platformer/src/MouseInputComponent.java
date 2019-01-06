import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

public class MouseInputComponent implements MouseListener
{
    private int gameState;
    private HashMap<Integer, Boolean> clickMap;
    protected boolean startClicked;
    protected boolean quitClicked;
    protected boolean soundClicked;
    protected boolean musicClicked;
    protected boolean restartClicked;

    public MouseInputComponent()
    {
        //Set the values for the buttons
        startClicked = false;
        quitClicked = false;
        soundClicked = false;
        musicClicked = false;
        restartClicked = false;

        //Map the key booleans to KeyEvent codes
        clickMap = new HashMap<>();
        //clickMap.put(startButton, false);
        //clickMap.put(quitButton, false);
        //clickMap.put(soundButton, false);
        //clickMap.put(musicButton, false);
        //clickMap.put(restartButton, false);

        //Default state to Main Menu
        gameState = Game.MAIN_MENU;
    }

    public void update(int currentGameState)
    {
        //Keep track of the game state
        gameState = currentGameState;
    }


    @Override
    public void mouseClicked(MouseEvent e)
    {
        //Determine if the mouse was clicked on any game buttons
        if (gameState == Game.MAIN_MENU)
        {
            //Start button

            //Quit button
        }
        else if (gameState == Game.PLAYING_GAME)
        {
            //Sound button

            //Music button
        }
        else if (gameState == Game.FINAL_MENU)
        {
            //Restart button
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) { /* Do nothing */ }

    @Override
    public void mouseExited(MouseEvent e) { /* Do nothing */ }

    @Override
    public void mousePressed(MouseEvent e) { /* Do nothing */ }

    @Override
    public void mouseReleased(MouseEvent e) { /* Do nothing */ }
}
