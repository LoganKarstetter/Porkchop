import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

public class InputComponent implements KeyListener, MouseListener
{
    private MouseWatcher mouseWatcher;
    private HashMap<Integer, Boolean> keyMap;
    protected boolean left;
    protected boolean right;
    protected boolean up;

    public InputComponent()
    {
        //Map the key booleans to KeyEvent codes
        keyMap = new HashMap<>();
        keyMap.put(KeyEvent.VK_LEFT, false);
        keyMap.put(KeyEvent.VK_RIGHT, false);
        keyMap.put(KeyEvent.VK_UP, false);
    }

    public void update()
    {
        //Update booleans with map values
        left = keyMap.get(KeyEvent.VK_LEFT);
        right = keyMap.get(KeyEvent.VK_RIGHT);
        up = keyMap.get(KeyEvent.VK_UP);
    }

    public void setMouseWatcher(MouseWatcher newMouseWatcher)
    {
        mouseWatcher = newMouseWatcher;
    }

    @Override
    public void keyPressed(KeyEvent event)
    {
        keyMap.replace(event.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent event)
    {
        keyMap.replace(event.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent event) { /* Do nothing */ }

    @Override
    public void mouseClicked(MouseEvent event) { /* Do nothing */ }

    @Override
    public void mousePressed(MouseEvent event) { mouseWatcher.mouseClicked(event.getPoint()); }

    @Override
    public void mouseReleased(MouseEvent event) { /* Do nothing */ }

    @Override
    public void mouseEntered(MouseEvent event) { /* Do nothing */ }

    @Override
    public void mouseExited(MouseEvent event) { /* Do nothing */ }
}
