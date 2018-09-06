import java.awt.*;

/**
 * @author Logan Karstetter
 * Date: 07/01/2018
 */
public class DynamicSprite extends Sprite
{
    /** The state of the sprite when it is not undergoing vertical motion */
    protected static final int NORMAL = 0;
    /** The state of the sprite when it is falling downwards */
    protected static final int FALLING = 1;

    /** The direction of the sprite when it is standing still */
    private static final int STILL = 0;
    /** The left direction */
    private static final int LEFT = 1;
    /** The right direction */
    private static final int RIGHT = 2;

    /** An integer used to specify the vertical behavior of the sprite */
    protected int state;
    /** The direction the sprite is currently moving */
    private int direction;

    /** The x-coordinate position of this sprite with respect to the map */
    protected int xPos;
    /** The y-coordinate position of this sprite with respect to the map */
    protected int yPos;

    /** The number of pixels this sprite will move in the x-direction each update */
    protected int xStep = 5;
    /** The number of pixels this sprite can move upwards or downwards in the y-direction each update */
    protected int yStep = 7;

    /** The MapManager that manages map interactions */
    protected MapManager mapManager;

    /**
     * Create a new dynamic sprite with a unique id, the name of the
     * image used to represent the sprite, an ImageLoader to load images,
     * the loop period of the game loop, and a MapManager to handle block
     * interactions.
     * @param imageName The name of the image that will visually represent this sprite.
     * @param imageLoader The ImageLoader used to load images and animations for this platformer.
     * @param loopPeriod The number of nanoseconds allocated for a single game loop cycle.
     * @param mapManager The MapManager that controls interactions with the map.
     */
    public DynamicSprite(int id, String imageName, ImageLoader imageLoader, long loopPeriod, MapManager mapManager)
    {
        //Super constructor
        super(id, true, false, imageName, imageLoader, loopPeriod);
        this.mapManager = mapManager;
        state = NORMAL;
    }

    /**
     * Create a new dynamic sprite with a unique id, the name of the
     * image used to represent the sprite, an ImageLoader to load images,
     * the loop period of the game loop, and a MapManager to handle block
     * interactions. Note that dynamic sprites are not blocks.
     * @param imageName The name of the image that will visually represent this sprite.
     * @param imageLoader The ImageLoader used to load images and animations for this platformer.
     * @param loopPeriod The number of nanoseconds allocated for a single game loop cycle.
     * @param mapManager The MapManager that controls interactions with the map.
     */
    public DynamicSprite(int id, int x, int y, String imageName, ImageLoader imageLoader, long loopPeriod, MapManager mapManager)
    {
        //Super constructor
        super(id, true, false, imageName, imageLoader, loopPeriod);

        //Setup position data
        xPos = x;
        yPos = y;
        state = NORMAL;
        direction = STILL;
        this.mapManager = mapManager;
    }

    /**
     * Update the state of this sprite.
     */
    public void update()
    {
        //Update the animation
        sequencePlayer.update();
        move();
    }

    /**
     * Move the sprite.
     */
    public void move()
    {
        //Move the sprite according to its direction
        if (direction == LEFT)
        {
            xPos -= xStep;
        }
        else if (direction == RIGHT)
        {
            xPos += xStep;
        }
    }

    /**
     * Draw the dynamic sprite using its current image. Otherwise, draw
     * a placeholder square in its place.
     * @param dbGraphics The Graphics object used to draw the dynamic sprite.
     */
    public void draw(Graphics dbGraphics)
    {
        //Get the image from the sequence player
        image = sequencePlayer.getCurrentImage();
        if (image != null)
        {
            dbGraphics.drawImage(image, xPos, yPos, null);
        }
        else //Draw the sprite as a square
        {
            dbGraphics.setColor(Color.MAGENTA);
            dbGraphics.fillRect(xPos, yPos, width, height);
        }
    }
}
