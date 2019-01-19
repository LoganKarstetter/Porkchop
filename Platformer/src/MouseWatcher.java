import java.awt.*;
/**
 * @author Logan Karstetter
 * Date: 2019
 */
public interface MouseWatcher
{
    /** This method is called by the Game when the user clicks the mouse */
    void mouseClicked(Point mousePosition, InputComponent playerInputComponent);
}
