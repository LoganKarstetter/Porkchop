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
    /** The state of the sprite when it is rising upwards */
    protected static final int RISING = 2;
    /** The state of the sprite when it is dead */
    protected static final int DEAD = 3;
    /** An integer used to specify the vertical behavior of the sprite */
    protected int state;

    /** The x-coordinate position of this sprite with respect to the map */
    protected int xPos;
    /** The y-coordinate position of this sprite with respect to the map */
    protected int yPos;
    /** The number of pixels this sprite will move in the x-direction each update */
    protected int xStep;
    /** The number of pixels this sprite can move upwards or downwards in the y-direction each update */
    protected int yStep;

    /** The MapManager that manages map interactions */
    protected MapManager mapManager;

    /**
     * Create a new dynamic sprite with a unique id, the number of pixels the
     * sprite should move each update in the x and y directions, the name of
     * the image used to represent the sprite, an ImageLoader to load images,
     * the loop period of the game loop, and a MapManager to handle block interactions.
     * @param id The unique identifier for this sprite.
     * @param xStep The number of pixels to move in the x-direction each update.
     * @param yStep The number of pixels to move in the y-direction each update.
     * @param imageName The name of the image that will visually represent this sprite.
     * @param imageLoader The ImageLoader used to load images and animations for this platformer.
     * @param loopPeriod The number of nanoseconds allocated for a single game loop cycle.
     * @param mapManager The MapManager that controls interactions with the map.
     */
    public DynamicSprite(int id, int xStep, int yStep, String imageName, ImageLoader imageLoader, long loopPeriod, MapManager mapManager)
    {
        //Super constructor
        super(id, true, false, imageName, imageLoader, loopPeriod);
        this.xStep = xStep;
        this.yStep = yStep;
        this.mapManager = mapManager;
        state = NORMAL;
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
        int newXStep = 0;
        if (xStep < 0) //Move left
        {
            //Check for block collisions while moving left
            newXStep = mapManager.checkHorizontalBlockCollisions(xPos, yPos, width, -xStep);
            xPos += newXStep;

            //Do not allow the sprite to go off the left of the map
            if (xPos < 0)
            {
                xPos = 0;
            }
        }
        else if (xStep > 0) //Move right
        {
            //Check for block collisions while moving right
            newXStep = mapManager.checkHorizontalBlockCollisions(xPos, yPos, width, xStep);
            xPos += newXStep;

            //Make sure the sprite doesn't go off the right side of the map
            if (xPos + xStep + width > mapManager.getMapDimensions().x )
            {
                xPos = mapManager.getMapDimensions().x - width;
            }
        }

        //Change direction if the sprite has run into a block
        if (newXStep == 0)
        {
            xStep = -xStep;
        }

        //Perform various actions based on the sprite's state
        if (state == NORMAL)
        {
            //Check if the sprite is standing in thin air, if so make it fall
            int newYStep = mapManager.checkVerticalBlockCollisions(xPos, yPos, width, height, yStep);
            if (newYStep != 0) //The sprite is not on the ground
            {
                state = FALLING;
                yPos += newYStep;
            }
        }
        else if (state == FALLING)
        {
            //Fall until the sprite reaches the ground
            int newYStep = mapManager.checkVerticalBlockCollisions(xPos, yPos, width, height, yStep);
            if (newYStep != 0) //The sprite has not reached the ground
            {
                yPos += newYStep;
            }
            else //The sprite hit the ground, so its state should be changed to normal
            {
                state = NORMAL;
            }
        }
        else if (state == DEAD)
        {
            //Fall out of the map
        }
    }

    /**
     * Draw the sprite using its current image of animation. If the
     * image cannot be found then a placeholder square is draw instead.
     * @param dbGraphics The Graphics object used to draw the sprite.
     * @param offsetX The x coordinate offset the sprite should be draw with respect to.
     * @param offsetY The y coordinate offset the sprite should be draw with respect to.
     */
    public void draw(Graphics dbGraphics, int offsetX, int offsetY)
    {
        //Get the image from the sequence player
        image = sequencePlayer.getCurrentImage();
        if (image != null)
        {
            dbGraphics.drawImage(image, xPos + offsetX, yPos + offsetY, null);
        }
        else //Draw the sprite as a purple square
        {
            dbGraphics.setColor(Color.MAGENTA);
            dbGraphics.fillRect(xPos + offsetX, yPos + offsetY, width, height);
        }
    }

    /**
     * Get a rectangle object representing the dimensions/position of this sprite.
     * @return A new rectangle with the sprite's dimensions and position.
     */
    public Rectangle getRectangle()
    {
        //Give the player some wiggle room
        return new Rectangle(xPos, yPos, width, height);
    }
}
