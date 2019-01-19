import java.awt.*;
import java.util.HashMap;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class Block
{
    /** The width of all blocks in the game */
    public static final int BLOCK_WIDTH = 60;
    /** The height of all blocks in the game */
    public static final int BLOCK_HEIGHT = 60;
    /** The default graphic state of every block */
    public static final int NORMAL_GRAPHICS = 0;

    /** The amount of time elapsed for the block's animation */
    protected long elapsedAnimationTimeInMs;
    /** Maps graphics states (integers) to animations */
    protected HashMap<Integer, Animation> graphicsMap;

    /**
     * Create a block with a set of animations.
     * @param blockSpecificGraphics The animation map for the block.
     */
    public Block(HashMap<Integer, Animation> blockSpecificGraphics)
    {
        graphicsMap = blockSpecificGraphics;
        elapsedAnimationTimeInMs = 0L;
    }

    /**
     * Update the block. This method recalculates the blocks elapsed animation time.
     * @param loopPeriodInMs The loop period of the game loop.
     */
    public void update(long loopPeriodInMs)
    {
        if (graphicsMap != null)
        {
            elapsedAnimationTimeInMs = graphicsMap.get(NORMAL_GRAPHICS).update(loopPeriodInMs, elapsedAnimationTimeInMs);
        }

    }

    /**
     * Draw the block using its normal graphic state. This method does not support animation blocks.
     * @param dbGraphics The graphics object that will draw the block.
     * @param xOffset The x position to draw the block at on the screen.
     * @param yOffset The y position to draw the block at on the screen.
     */
    public void draw(Graphics dbGraphics, int xOffset, int yOffset)
    {
        graphicsMap.get(NORMAL_GRAPHICS).draw(dbGraphics, xOffset, yOffset, elapsedAnimationTimeInMs);
    }
}
