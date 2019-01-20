import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class InputComponent implements KeyListener, MouseListener
{
    /** The watcher to notify when mouse events occur */
    private MouseWatcher mouseWatcher;
    /** Maps key codes to booleans to indicate pressed keys */
    private HashMap<Integer, Boolean> keyMap;
    /** Flag specifying the left key was pressed */
    protected boolean left;
    /** Flag specifying the right key was pressed */
    protected boolean right;
    /** Flag specifying the up key was pressed */
    protected boolean up;

    /**
     * Create an InputComponent for processing user input.
     */
    public InputComponent()
    {
        //Map the key booleans to KeyEvent codes
        keyMap = new HashMap<>();
        keyMap.put(KeyEvent.VK_LEFT, false);
        keyMap.put(KeyEvent.VK_RIGHT, false);
        keyMap.put(KeyEvent.VK_UP, false);
    }

    /**
     * Update the pressed key booleans.
     */
    public void update()
    {
        //Update booleans with map values
        left = keyMap.get(KeyEvent.VK_LEFT);
        right = keyMap.get(KeyEvent.VK_RIGHT);
        up = keyMap.get(KeyEvent.VK_UP);
    }

    /**
     * Set the watcher to notify when mouse events occur.
     * @param newMouseWatcher The watcher to notify.
     */
    public void setMouseWatcher(MouseWatcher newMouseWatcher)
    {
        mouseWatcher = newMouseWatcher;
    }

    /**
     * Maps true to a key code when the key is pressed.
     * @param event The key event.
     */
    @Override
    public void keyPressed(KeyEvent event)
    {
        keyMap.replace(event.getKeyCode(), true);
    }

    /**
     * Maps false to a key code when the key is released.
     * @param event The key event.
     */
    @Override
    public void keyReleased(KeyEvent event)
    {
        keyMap.replace(event.getKeyCode(), false);
    }

    /**
     * Notifies the mouse watcher whenever the mouse is pressed.
     * @param event The mouse event that occurred.
     */
    @Override
    public void mousePressed(MouseEvent event) { mouseWatcher.mouseClicked(event.getPoint(), this); }

    /** This method does nothing and is not used. */
    @Override
    public void keyTyped(KeyEvent event) { /* Do nothing */ }

    /** This method does nothing and is not used. */
    @Override
    public void mouseClicked(MouseEvent event) { /* Do nothing */ }

    /** This method does nothing and is not used. */
    @Override
    public void mouseReleased(MouseEvent event) { /* Do nothing */ }

    /** This method does nothing and is not used. */
    @Override
    public void mouseEntered(MouseEvent event) { /* Do nothing */ }

    /** This method does nothing and is not used. */
    @Override
    public void mouseExited(MouseEvent event) { /* Do nothing */ }
}
