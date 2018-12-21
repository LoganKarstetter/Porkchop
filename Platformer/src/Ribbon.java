import java.awt.*;
import java.awt.image.BufferedImage;

public class Ribbon
{
    public static final int SCROLL_STILL = 0;
    public static final int SCROLL_LEFT = 1;
    public static final int SCROLL_RIGHT = 2;
    private int scrollDirection;

    private int xPos;
    private int speed;
    private int width;
    private BufferedImage image;

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

    public void setScrollDirection(int newScrollDirection)
    {
        scrollDirection = newScrollDirection;
    }

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

    public void reset()
    {
        //Rest the ribbon head, typically on player death
        xPos = 0;
    }

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
}
