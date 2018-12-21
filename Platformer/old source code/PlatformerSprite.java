import java.awt.*;
/**
 * @author Logan Karstetter
 * Date: 09/05/2018
 */
public class PlatformerSprite extends DynamicSprite
{
    /** The maximum number of calls to the sprite's update method that can occur before
     * its rising behavior ends and it begins falling downwards. */
    private static final int MAX_UP_STEPS = 15;
    /** The current number of updates that have occurred since the sprite began rising. */
    private int upSteps;

    /** The KeyManager used to move the sprite up and down */
    private KeyManager keyManager;

    /**
     * Create a new platformer sprite with a unique id, the number of pixels the
     * sprite should move each update in the x and y directions, the name of its
     * representing image, an ImageLoader for loading images, the loop period of
     * the game loop, a KeyManager for interpreting user input, and a MapManager
     * for handling block interactions.
     * @param id The unique identifier for this platformer sprite.
     * @param xStep Number of pixels the sprite should move in the x direction each update.
     * @param yStep Number of pixels the sprite should move in the y direction each update.
     * @param imageName The name of the image that will represent this sprite.
     * @param imageLoader An ImageLoader used to load images and animations.
     * @param loopPeriod The number of nanoseconds allowed for each cycle of the game loop.
     * @param keyManager The KeyManager used to interpret user input.
     * @param mapManager The MapManager that the handles block interactions.
     */
    public PlatformerSprite(int id, int xStep, int yStep, String imageName, ImageLoader imageLoader, long loopPeriod, KeyManager keyManager, MapManager mapManager)
    {
        //Super constructor
        super(id, xStep, yStep, imageName, imageLoader, loopPeriod, mapManager);

        //Store the keyManager and set upSteps to zero
        this.keyManager = keyManager;
        upSteps = 0;
    }

    /**
     * Update the state
     */
    public void update()
    {
        //Update the sequence player
        sequencePlayer.update();
        move();
    }

    /**
     * Update the position of the sprite according to its current state and user input.
     */
    public void move()
    {
        //Move the sprite as long as its state is not dead
        if (state != DEAD)
        {
            if (keyManager.left) //Move left
            {
                //Check for block collisions while trying to move left
                xPos += mapManager.checkHorizontalBlockCollisions(xPos, yPos, width, -xStep);

                //Do not allow the sprite to go off the left of the map
                if (xPos < 0)
                {
                    xPos = 0;
                }
            }
            else if (keyManager.right) //Move right
            {
                //Check for block collisions while trying to move right
                xPos += mapManager.checkHorizontalBlockCollisions(xPos, yPos, width, xStep);

                //Make sure the sprite doesn't go off the right side of the map
                if (xPos + xStep + width > mapManager.getMapDimensions().x )
                {
                    xPos = mapManager.getMapDimensions().x - width;
                }
            }

            //Determine if the sprite has collided with another sprite
            if (mapManager.checkSpriteCollisions(getRectangle(), id))
            {
                state = DEAD;
            }
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
            else //If the sprite is on solid ground
            {
                //Check for jump input from the user
                if (keyManager.up)
                {
                    //Make sure the sprite has room to move upwards before changing states
                    newYStep = mapManager.checkVerticalBlockCollisions(xPos, yPos, width, height, -yStep);
                    if (newYStep < 0) //Negative is upwards
                    {
                        //Change the sprite's state to rising
                        state = RISING;
                        yPos += newYStep;
                        upSteps++;
                    }
                }
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
        else if (state == RISING)
        {
            //Check if the sprite needs to begin falling
            if (upSteps >= MAX_UP_STEPS)
            {
                //Change the state and reset upSteps to zero
                state = FALLING;
                upSteps = 0;
            }
            else //The sprite is free to continue rising upwards
            {
                //If the sprite doesn't have room to move upwards it should begin falling
                int newYStep = mapManager.checkVerticalBlockCollisions(xPos, yPos, width, height, -yStep);
                if (newYStep < 0) //Negative is upwards
                {
                    //Move upwards
                    yPos += newYStep;
                    upSteps++;
                }
                else //The sprite has hit something, start falling
                {
                    state = FALLING;
                    upSteps = 0;
                }
            }
        }
        else if (state == DEAD)
        {
            //Fall out of the map
        }
    }

    /**
     * Get the map coordinates of the sprite. Note that
     * the map coordinates are measured with respect to
     * the game map, not the panel's dimensions.
     * @return A new point containing the map coordinates.
     */
    public Point getMapCoords()
    {
        return new Point(xPos, yPos);
    }
}
