import java.awt.*;
import java.util.HashMap;

/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class EventBlock extends Block
{
    /** The constant representing the inactive block type */
    public static final int BLOCK_INACTIVE = 0;
    /** The constant representing the advance level block type */
    public static final int BLOCK_LEVEL = 1;
    /** The constant representing the dangerous block type */
    public static final int BLOCK_DANGER = 2;
    /** The constant representing the collectible block type */
    public static final int BLOCK_COLLECT = 3;
    /** The constant representing a special block type */
    public static final int BLOCK_SPECIAL_INACTIVE = 4;
    /** The constant representing a special block type */
    public static final int BLOCK_SPECIAL_COLLECT = 5;
    /** The type of the event block */
    private int blockType;

    /** The rectangle representing the position and dimensions of the block */
    private Rectangle boundingBox;

    /**
     * Create an event block with a given position, type, and set of animations.
     * @param xPos The x position of the event block.
     * @param yPos The y position of the event block.
     * @param typeOfBlock The type of block, it must match the types defined in the EventBlock class.
     * @param blockSpecificGraphics The graphic state to animation map for the block.
     */
    public EventBlock(int xPos, int yPos, int typeOfBlock, HashMap<Integer, Animation> blockSpecificGraphics)
    {
        super(blockSpecificGraphics);
        blockType = typeOfBlock;
        boundingBox = new Rectangle(xPos, yPos, blockSpecificGraphics.get(0).getImageWidth(), blockSpecificGraphics.get(0).getImageHeight());
    }

    /**
     * Draw the non-inactive event blocks. Blocks with the BLOCK_INACTIVE type are not drawn.
     * @param dbGraphics The graphics object that will draw the block.
     * @param xOffset The x position to draw the block at on the screen.
     * @param yOffset The y position to draw the block at on the screen.
     */
    public void draw(Graphics dbGraphics, int xOffset, int yOffset)
    {
        if (blockType != BLOCK_INACTIVE && blockType != BLOCK_SPECIAL_INACTIVE)
        {
            graphicsMap.get(NORMAL_GRAPHICS).draw(dbGraphics, boundingBox.x + xOffset, boundingBox.y + yOffset, elapsedAnimationTimeInMs);
        }
    }

    /**
     * Active the block. This method performs various actions depending on the block type.
     * For example, collectible blocks are set to inactive so that they cannot be collectible again or drawn.
     */
    public void activate()
    {
        //If a block is activated by the player, perform actions based on block type
        if (blockType == BLOCK_COLLECT || blockType == BLOCK_SPECIAL_COLLECT)
        {
            //Disable collectibles after the first interaction
            blockType = BLOCK_INACTIVE;
        }
    }

    /**
     * Get the bounding box for an event block.
     * @return The bounding box.
     */
    public Rectangle getBoundingBox()
    {
        return boundingBox;
    }

    /**
     * Get the type of an event block
     * @return The block type.
     */
    public int getBlockType()
    {
        return blockType;
    }

    /**
     * Set the block type of a block.
     * @param newBlockType The new block type.
     */
    public void setBlockType(int newBlockType)
    {
        blockType = newBlockType;
    }
}
