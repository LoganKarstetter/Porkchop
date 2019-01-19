import java.awt.*;
import java.awt.image.BufferedImage;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class Ribbon
{
    /** The constant defining the still movement */
    public static final int SCROLL_STILL = 0;
    /** The constant defining the scroll left movement */
    public static final int SCROLL_LEFT = 1;
    /** The constant defining the scroll right movement */
    public static final int SCROLL_RIGHT = 2;
    /** The scroll direction of the ribbon */
    private int scrollDirection;

    /** The x position of the head of the ribbon on the screen */
    private int xPos;
    /** The speed the ribbon scrolls */
    private int speed;
    /** The width of the ribbon */
    private int width;
    /** The image displayed by the ribbon */
    private BufferedImage image;

    /**
     * Create a new ribbon with an image, scroll direction, and speed.
     * @param ribbonImage The image displayed by the ribbon.
     * @param initialScrollDirection The initial scroll direction of the ribbon, this must match the Ribbon constants.
     * @param ribbonSpeed The scroll speed of the ribbon.
     */
    public Ribbon(BufferedImage ribbonImage, int initialScrollDirection, int ribbonSpeed)
    {
        //Store ribbon data
        image = ribbonImage;
        scrollDirection = initialScrollDirection;
        speed = ribbonSpeed;

        //Set ribbon position and size
        xPos = 0;
        width = image.getWidth();
    }

    /**
     * Update the position of the head of the ribbon (scroll).
     */
    public void update()
    {
        //Move the image horizontally, reset the position to zero once the entire image has cycled through
        if (scrollDirection == SCROLL_LEFT)
        {
            xPos = (xPos - speed) % width;
        }
        else if (scrollDirection == SCROLL_RIGHT)
        {
            xPos = (xPos + speed) % width;
        }
        //Otherwise, don't scroll
    }

    /**
     * Draw the ribbon.
     * @param dbGraphics The graphics object used to draw the ribbon.
     */
    public void draw(Graphics dbGraphics)
    {
        //Draw the image according to the x position
        if (xPos == 0)
        {
            //Draw full image normally
            dbGraphics.drawImage(image, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        }
        else if(xPos > 0 && xPos < GamePanel.WIDTH) //Image head end is within the panel
        {
            //Draw the tail of the image
            dbGraphics.drawImage(image, 0, 0, xPos, GamePanel.HEIGHT, width - xPos, 0, width, GamePanel.HEIGHT, null);
            //Draw the head of the image
            dbGraphics.drawImage(image, xPos, 0, GamePanel.WIDTH, GamePanel.HEIGHT, 0, 0, GamePanel.WIDTH - xPos, GamePanel.HEIGHT, null);
        }
        else if(xPos >= GamePanel.WIDTH)
        {
            //Draw the tail of the image
            dbGraphics.drawImage(image, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, width - xPos, 0, width - xPos + GamePanel.WIDTH, GamePanel.HEIGHT, null);
        }
        else if(xPos < 0 && (xPos >= GamePanel.WIDTH - width)) //Moving left, image head and tail end outside of panel
        {
            //Draw the body of the image, no head or tail
            dbGraphics.drawImage(image, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, -xPos, 0, GamePanel.WIDTH - xPos, GamePanel.HEIGHT, null);
        }
        else if(xPos < GamePanel.WIDTH - width) //Moving left, head and tail inside panel
        {
            //Draw the tail of the image
            dbGraphics.drawImage(image, 0, 0, width + xPos, GamePanel.HEIGHT, -xPos, 0, width, GamePanel.HEIGHT, null);
            //Draw the head of the image
            dbGraphics.drawImage(image, width + xPos, 0, GamePanel.WIDTH, GamePanel.HEIGHT, 0, 0, GamePanel.WIDTH - width - xPos, GamePanel.HEIGHT, null);
        }
    }

    /**
     * Reset the ribbon's scroll direction to zero.
     */
    public void reset()
    {
        //Rest the ribbon head, typically on player death
        xPos = 0;
    }

    /**
     * Set the scroll direction for the ribbon.
     * @param newScrollDirection The new scroll direction.
     */
    public void setScrollDirection(int newScrollDirection)
    {
        scrollDirection = newScrollDirection;
    }
}
