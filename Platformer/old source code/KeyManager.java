import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

/**
 * @author Logan Karstetter
 * Date: 07/01/2018
 */
public class KeyManager implements KeyListener
{
    /** A HashMap of booleans determining if a key has been pressed.
     * The key values for the map are the unique KeyCodes for each key. */
    private HashMap<Integer, Boolean> keys;

    /** Determines if the esc button is pressed */
    private boolean escape;
    /** Determines if the left key was pressed */
    public boolean left;
    /** Determines if the right key was pressed */
    public boolean right;
    /** Determines if the up key was pressed */
    public boolean up;

    /** A reference to the PlatformerPanel this KeyManager listens for */
    private PlatformerPanel platformerPanel;

    /**
     * A KeyManager is used to process concurrent keyboard inputs. The manager maintains
     * a HashMap of boolean values corresponding to each relevant key. When a key is pressed/held
     * the value in the HashMap is set to true using the keyCode as the key. The value is set to
     * false when a key is released.
     * @param platformerPanel The PlatformerPanel this KeyManager handles KeyEvents for.
     */
    public KeyManager(PlatformerPanel platformerPanel)
    {
        //Store the reference to the platformerPanel
        this.platformerPanel = platformerPanel;

        //Create the keys map
        keys = new HashMap<>();
        keys.put(KeyEvent.VK_ESCAPE, false);
        keys.put(KeyEvent.VK_LEFT, false);
        keys.put(KeyEvent.VK_RIGHT, false);
        keys.put(KeyEvent.VK_UP, false);

    }

    /** Update the KeyManager's escape, left, right, and up key boolean values. */
    public void update()
    {
        //Set the booleans according to the map contents
        escape = keys.get(KeyEvent.VK_ESCAPE);
        left = keys.get(KeyEvent.VK_LEFT);
        right = keys.get(KeyEvent.VK_RIGHT);
        up = keys.get(KeyEvent.VK_UP);

        //Check if escape is set to true, stop the game
        if (escape)
        {
            platformerPanel.stopGame();
        }
    }

    /**
     * Invoked when a key is pressed. The keyCode of the keyEvent is used to
     * set the corresponding boolean in the keys HashMap.
     * @param e A KeyEvent
     */
    public void keyPressed(KeyEvent e)
    {
        keys.replace(e.getKeyCode(), true);
    }

    /**
     * Invoked when a key is released. The keyCode of the keyEvent is used to
     * set the corresponding boolean in the keys HashMap.
     * @param e A KeyEvent
     */
    public void keyReleased(KeyEvent e)
    {
        keys.replace(e.getKeyCode(), false);
    }

    /**
     * Invoked when a key is pressed and then released. This method does nothing.
     * @param e A KeyEvent
     */
    public void keyTyped(KeyEvent e)
    {
        //Do nothing
    }
}
