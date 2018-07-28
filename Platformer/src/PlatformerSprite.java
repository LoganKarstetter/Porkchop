import java.awt.*;

public class PlatformerSprite extends Sprite
{
    /** The state of the sprite when it is rising upwards */
    private static final int RISING = 2;

    /** The maximum number of calls to the sprite's update method that can occur before
     * its rising behavior ends and it begins falling downwards. */
    private static final int MAX_UP_STEPS = 15;
    /** The current number of updates that have occurred since the sprite began rising. */
    private int upSteps;

    /** The KeyManager used to move the sprite up and down */
    private KeyManager keyManager;

    /**
     * Create a new platformer sprite with an x position, y position, and the name of the image intended to
     * visually represent the sprite. The loopPeriod (in nanos) and imageLoader are included to
     * load the images and control the speed of the animation. The sprite uses a keyManager to control
     * the jumping behavior of the sprite.
     * @param loopPeriod The amount of time allocated for each cycle of the animation loop (nanos).
     * @param imageLoader The ImageLoader used to load images and animations for this platformer.
     * @param imageName The name of the image that will visually represent this sprite.
     * @param keyManager The KeyManager used to move the sprite.
     * @param blockManager The BlockManager that the controls the movement of the blocks.
     */
    public PlatformerSprite(long loopPeriod, ImageLoader imageLoader, String imageName, KeyManager keyManager,
                            BlockManager blockManager)
    {
        //Call the sprite super constructor
        super(loopPeriod, imageLoader, imageName, blockManager);

        //Store the keyManager and set upSteps to zero
        this.keyManager = keyManager;
        upSteps = 0;

        //Request the spawn point from the blockManager
        Point spawnPoint = blockManager.getSpawnPoint();
        xPos = spawnPoint.x;
        yPos = spawnPoint.y;
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
        //Move the sprite regardless of state according to user input
        if (keyManager.left) //Move left
        {
            //Check for block collisions while trying to move left
            xPos += blockManager.checkHorizontalCollisions(xPos, yPos, width, -xStep);

            //Do not allow the sprite to go off the left of the map
            if (xPos < 0)
            {
                xPos = 0;
            }
        }
        else if (keyManager.right) //Move right
        {
            //Check for block collisions while trying to move right
            xPos += blockManager.checkHorizontalCollisions(xPos, yPos, width, xStep);

            //Make sure the sprite doesn't go off the right side of the map
            if (xPos + xStep + width > blockManager.getMapDimensions().x )
            {
                xPos = blockManager.getMapDimensions().x - width;
            }
        }

        //Perform various actions based on the sprite's state
        if (state == NORMAL)
        {
            //Check if the sprite is standing in thin air, if so make it fall
            int newYStep = blockManager.checkVerticalCollisions(xPos, yPos, width, height, yStep);
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
                    newYStep = blockManager.checkVerticalCollisions(xPos, yPos, width, height, -yStep);
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
            int newYStep = blockManager.checkVerticalCollisions(xPos, yPos, width, height, yStep);
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
                int newYStep = blockManager.checkVerticalCollisions(xPos, yPos, width, height, -yStep);
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
    }

    /**
     * Draw the sprite using its current image of animation. If the current image cannot be
     * found then a placeholder red square is draw instead.
     * @param dbGraphics The Graphics object used to draw the spaceship.
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
