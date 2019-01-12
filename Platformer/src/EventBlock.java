import java.awt.*;
import java.util.HashMap;

public class EventBlock extends Block
{
    public static final int BLOCK_INACTIVE = 0;
    public static final int BLOCK_LEVEL = 1;
    public static final int BLOCK_DANGER = 2;
    public static final int BLOCK_COLLECT = 3;
    private int blockType;

    private Rectangle boundingBox;

    public EventBlock(int xPos, int yPos, int typeOfBlock, HashMap<Integer, Animation> blockSpecificGraphics)
    {
        super(blockSpecificGraphics);
        blockType = typeOfBlock;
        boundingBox = new Rectangle(xPos, yPos, blockSpecificGraphics.get(0).getImageWidth(), blockSpecificGraphics.get(0).getImageHeight());
    }

    public void draw(Graphics dbGraphics, int xOffset, int yOffset)
    {
        if (blockType != BLOCK_INACTIVE)
        {
            graphicsMap.get(NORMAL_GRAPHICS).draw(dbGraphics, boundingBox.x + xOffset, boundingBox.y + yOffset, elapsedAnimationTimeInMs);
        }
    }

    public void activate()
    {
        //If a block is activated by the player, perform actions based on block type
        if (blockType == BLOCK_COLLECT)
        {
            //Disable collectibles after the first interaction
            blockType = BLOCK_INACTIVE;
        }
    }

    public Rectangle getBoundingBox()
    {
        return boundingBox;
    }

    public int getBlockType()
    {
        return blockType;
    }
}
