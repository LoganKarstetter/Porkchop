import java.awt.*;

public class PlatformerSprite extends Sprite
{
    /** The state of the sprite when it is rising upwards */
    private static final int RISING = 2;

    /** The maximum number of calls to the sprite's update method that can occur before
     * its rising behavior ends and it begins falling downwards. */
    private static final int MAX_UP_STEPS = 10;
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
        //Move the sprite according to user input
        if (keyManager.left)
        {
            xPos -= xStep;
        }
        else if (keyManager.right)
        {
            xPos += xStep;
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
