import java.awt.*;
import java.util.HashMap;

public class Block
{
    public static final int BLOCK_WIDTH = 60;
    public static final int BLOCK_HEIGHT = 60;
    public static final int NORMAL_GRAPHICS = 0;

    protected long elapsedAnimationTimeInMs;
    protected HashMap<Integer, Animation> graphicsMap;

    public Block(HashMap<Integer, Animation> blockSpecificGraphics)
    {
        graphicsMap = blockSpecificGraphics;
        elapsedAnimationTimeInMs = 0L;
    }

    public void update(long loopPeriodInMs)
    {
        elapsedAnimationTimeInMs = graphicsMap.get(NORMAL_GRAPHICS).update(loopPeriodInMs, elapsedAnimationTimeInMs);
    }

    public void draw(Graphics dbGraphics, int xOffset, int yOffset)
    {
        graphicsMap.get(NORMAL_GRAPHICS).draw(dbGraphics, xOffset, yOffset);

    }
}
