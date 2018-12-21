import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * @author Logan Karstetter
 * Date: 09/05/2018
 */
public class Sprite
{
    /** The unique identifier for this sprite */
    protected int id;
    /** Determines whether this sprite is active (collision detection) */
    protected boolean isActive;
    /** Determines whether this sprite is a block */
    protected boolean isBlock;
    /** Determines whether this sprite is animated */
    protected boolean isAnimated;
    /** The pixel width of this sprite */
    protected int width;
    /** The pixel height of this sprite */
    protected int height;

    /** The number of nanoseconds allowed for each cycle of the animation loop */
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

    /**
     * Create a sprite with a unique id, a flag identifying
     * whether the sprite is active, and a flag identifying
     * whether the sprite is a block. After each sprite is
     * created, its definition is stored in the sprites array.
     * @param id The unique id for this sprite.
     * @param isActive Determines whether this sprite is active.
     * @param isBlock Determines whether this sprite is a static block.
     */
    public Sprite(int id, boolean isActive, boolean isBlock)
    {
        this.id = id;
        this.isActive = isActive;
        this.isBlock = isBlock;
        isAnimated = false;
    }

    /**
     * Create a sprite with a unique id, a flag identifying
     * whether the sprite is active, a flag identifying
     * whether the sprite is a block, the name of the image
     * representing it, and an ImageLoader to load images.
     * @param id The unique id for this sprite.
     * @param isActive Determines whether this sprite is active.
     * @param isBlock Determines whether this sprite is a static block.
     * @param imageName The name of the image representing the sprite.
     * @param imageLoader An ImageLoader used to load the sprite images.
     */
    public Sprite(int id, boolean isActive, boolean isBlock, String imageName, ImageLoader imageLoader)
    {
        //Base constructor
        this(id, isActive, isBlock);
        this.imageName = imageName;
        this.imageLoader = imageLoader;

        //Set the image
        isAnimated = false;
        setImage(imageName);
    }

    /**
     * Create an animated sprite with a unique id,
     * a flag identifying whether the sprite is active,
     * a flag identifying whether the sprite is a block,
     * the name of the image representing it, an
     * ImageLoader to load images, and the loop period
     * of the game loop.
     * @param id The unique id for this sprite.
     * @param isActive Determines whether this sprite is active.
     * @param isBlock Determines whether this sprite is a static block.
     * @param imageName The name of the image representing the sprite.
     * @param imageLoader An ImageLoader used to load the sprite images.
     * @param loopPeriod The number of nanoseconds for a game loop cycle.
     */
    public Sprite(int id, boolean isActive, boolean isBlock, String imageName, ImageLoader imageLoader, long loopPeriod)
    {
        //Base constructor
        this(id, isActive, isBlock);
        this.imageName = imageName;
        this.imageLoader = imageLoader;
        this.loopPeriod = loopPeriod;

        //Setup the animation
        isAnimated = true;
        setImage(imageName);
        sequencePlayer = new SequencePlayer(imageName, false, 1, loopPeriod, imageLoader);
    }

    /**
     * Update the sprite.
     */
    public void update()
    {
        //Update the animation
        if (isAnimated)
        {
            sequencePlayer.update();
        }
    }

    /**
     * Draw the sprite at the given x and y coordinates if it is active
     * (should be drawn and checked for collisions).
     * @param dbGraphics The Graphics object used to draw the sprites.
     * @param xPos The x coordinate describing the location of the sprite.
     * @param yPos The y coordinate describing the location of the sprite.
     */
    public void draw(Graphics dbGraphics, int xPos, int yPos)
    {
        //Check whether the block is active
        if (isActive)
        {
            //Draw the block's image if it exists
            if (image != null)
            {
                dbGraphics.drawImage(image, xPos, yPos, width, height, null);
            }
            else //Draw a placeholder block instead
            {
                dbGraphics.setColor(Color.MAGENTA);
                dbGraphics.drawRect(xPos, yPos, width, height);
            }
        }
    }

    /**
     * Set the image to be displayed as a visual representation of this sprite.
     * This method also clears any existing sequencePlayers.
     * @param imageName The name of the image as it is stored in the imagesMap.
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
     * Start an animation loop using the sprite's current image.
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
     * Cancel a looping animation.
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
     * Returns true if the sprite is an active
     * block. Otherwise, this method returns false.
     * @return True or false.
     */
    public boolean isActiveBlock()
    {
        return (isActive && isBlock);
    }

    /**
     * Returns true if the sprite is an active
     * sprite (not a block). Otherwise, this
     * method returns false.
     * @return True or false.
     */
    public boolean isActiveSprite()
    {
        return (isActive && !isBlock);
    }
}
