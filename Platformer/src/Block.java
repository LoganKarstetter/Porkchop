import org.w3c.dom.css.Rect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * @author Logan Karstetter
 * Date: 07/01/2018
 */
public class Block
{
    /** A list of all the blocks indexed by their id */
    public static ArrayList<Block> blocks = new ArrayList<>();
    /** The width of this block */
    private static int width = 10; //10 by default
    /** The height of this block */
    private static int height = 10;

    /** The unique identifier of this block */
    private int id;
    /** The image used to visually represent this block */
    private BufferedImage image;
    /** Determines if this block should be drawn or checked for collision */
    private boolean isActive;
    /** Determines if this block should be used as the starting location for the platformerSprite */
    private boolean isSpawn;

    /**
     * Create a block with a given image.
     * @param image The image used to visually represent the block.
     * @param isActive Determines if this block should be drawn or checked for collision.
     * @param isSpawn Determines if this block should be marked as the platformerSprite spawn.
     */
    public Block(BufferedImage image, boolean isActive, boolean isSpawn)
    {
        //Create the block, store the image, and
        id = blocks.size();
        this.image = image;
        this.isActive = isActive;
        this.isSpawn = isSpawn;

        //Store the block in the blocks list
        blocks.add(this);
    }

    /**
     * Initialize the game's blocks. This method should only be called
     * once as it creates all of the block objects for the game and loads
     * their images.
     * @param imageLoader The imageLoader used to load the block's images.
     * @param blockWidth The width of the block in pixels.
     * @param blockHeight The height of the block in pixels.
     */
    public static void initializeBlocks(ImageLoader imageLoader, int blockWidth, int blockHeight)
    {
        //Store the block width and height
        width = blockWidth;
        height = blockHeight;

        //Create each of the blocks that will be in the game
        new Block(null, false, false); //The empty block must always be created first (id == 0)
        new Block(null, false, false); //The block with id == 1 will be the spawn point
        new Block(imageLoader.getImage("Serpent Body"), true, false);
    }

    /**
     * Draw the block at the given x and y coordinates if it is active
     * (should be drawn and checked for collisions).
     * @param dbGraphics The Graphics object used to draw the blocks.
     * @param xPos The x coordinate describing the location of the block.
     * @param yPos The y coordinate describing the location of the block.
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
     * Get the boolean determining whether this block is active (can be walked through).
     * @return isActive
     */
    public boolean isActive()
    {
        return isActive;
    }

    /**
     * Get the boolean determining whether this block is the spawn point.
     * @return isSpawn
     */
    public boolean isSpawn()
    {
        return isSpawn;
    }
}
