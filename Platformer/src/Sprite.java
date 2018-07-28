import java.awt.*;
import java.awt.image.BufferedImage;
/**
 * @author Logan Karstetter
 * Date: 07/01/2018
 */
public class Sprite
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

    /** The width of this sprite's image in pixels */
    protected int width;
    /** The height of this sprite's image in pixels */
    protected int height;

    /** The number of pixels this sprite will move in the x-direction each update */
    protected int xStep = 5;
    /** The number of pixels this sprite can move upwards or downwards in the y-direction each update */
    protected int yStep = 7;

    /** The amount of time allocated for each cycle of the animation loop (nanos) */
    protected long loopPeriod;
    /** Determines whether the animation sequence associated with this sprite should loop indefinitely */
    protected boolean isLooping;

    /** The ImageLoader used to load and store images for this game */
    protected ImageLoader imageLoader;
    /** The SequencePlayer used to display the animations for this sprite */
    protected SequencePlayer sequencePlayer;
    /** The current image used to represent the sprite */
    protected BufferedImage image;
    /** The name of the current image presenting the sprite */
    protected String imageName;

    /** The BlockManager that the controls the movement of the blocks. */
    protected BlockManager blockManager;

    /**
     * Create a new sprite with the name of the image intended to visually represent the sprite.
     * The loopPeriod (in nanos) and imageLoader are included to load the images and control the
     * speed of the animation. The BlockManager allows for sprite/block collision detection.
     * This constructor is primarily used to setup the user controlled platformerSprite which
     * needs its x and y position calculated with respect to its image.
     * @param loopPeriod The amount of time allocated for each cycle of the animation loop (nanos).
     * @param imageLoader The ImageLoader used to load images and animations for this platformer.
     * @param imageName The name of the image that will visually represent this sprite.
     * @param blockManager The BlockManager that the controls the movement of the blocks.
     */
    public Sprite(long loopPeriod, ImageLoader imageLoader, String imageName, BlockManager blockManager)
    {
        //Store the loopPeriod, imageLoader, imageName, and blockManager
        this.loopPeriod = loopPeriod;
        this.imageLoader = imageLoader;
        this.imageName = imageName;
        setImage(imageName);
        this.blockManager = blockManager;

        //Set the state to normal
        state = NORMAL;

        //Create the sequencePlayer
        sequencePlayer = new SequencePlayer(imageName, false, 1, loopPeriod, imageLoader);
    }

    /**
     * Create a new sprite with an x position, y position, the name of the image intended to
     * visually represent the sprite, and a reference to the BlockManager. The loopPeriod
     * (in nanos) and imageLoader are included to load the images and control the speed of
     * the animation. The BlockManager allows for sprite/block collision detection.
     * @param x The x coordinate position of this sprite.
     * @param y The y coordinate position of this sprite.
     * @param loopPeriod The amount of time allocated for each cycle of the animation loop (nanos).
     * @param imageLoader The ImageLoader used to load images and animations for this platformer.
     * @param imageName The name of the image that will visually represent this sprite.
     * @param blockManager The BlockManager that the controls the movement of the blocks.
     */
    public Sprite(int x, int y, long loopPeriod, ImageLoader imageLoader, String imageName, BlockManager blockManager)
    {
        //Store the loopPeriod, imageLoader, imageName, and blockManager
        xPos = x;
        yPos = y;
        this.loopPeriod = loopPeriod;
        this.imageLoader = imageLoader;
        this.imageName = imageName;
        setImage(imageName);
        this.blockManager = blockManager;

        //Set the state to normal and the direction to still
        state = NORMAL;
        direction = STILL;

        //Create the sequencePlayer
        sequencePlayer = new SequencePlayer(imageName, false, 1, loopPeriod, imageLoader);
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
     * Set the image to be displayed as a visual representation of this sprite.
     * This method also clears any existing sequencePlayers.
     * @param imageName The name of the image as it is stored in the ImageLoader's imagesMap.
     */
    public void setImage(String imageName)
    {
        //Load the sprite's image and get its dimensions
        image = imageLoader.getImage(imageName);
        if (image != null)
        {
            width = image.getWidth();
            height = image.getHeight();
        }

        //Clear any existing sequencePlayers and reset the looping status
        sequencePlayer = null;
        isLooping = false;
    }

    /**
     * Start an animation loop using this sprite's current image/sequence.
     */
    public void loopAnimation()
    {
        //Check that the animation is more than a single image
        if (imageLoader.getNumberImages(imageName) > 0)
        {
            sequencePlayer = null;
            isLooping = true;
            sequencePlayer = new SequencePlayer(imageName, true, 1, loopPeriod, imageLoader);
        }
    }

    /**
     * Cancel a currently looping animation.
     */
    public void stopLooping()
    {
        //Stop the animation from looping
        if (isLooping)
        {
            sequencePlayer.stop();
            isLooping = false;
        }
    }

    /**
     * Draw the sprite using its current image of animation. If the current image cannot be
     * found then a placeholder red square is draw instead.
     * @param dbGraphics The Graphics object used to draw the spaceship.
     */
    public void draw(Graphics dbGraphics)
    {
        //Get the image from the sequence player
        image = sequencePlayer.getCurrentImage();
        if (image != null)
        {
            dbGraphics.drawImage(image, xPos, yPos, null);
        }
        else //Draw the sprite as a purple square
        {
            dbGraphics.setColor(Color.MAGENTA);
            dbGraphics.fillRect(xPos, yPos, width, height);
        }
    }
}
