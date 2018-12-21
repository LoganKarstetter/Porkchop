import java.awt.*;
import java.util.HashMap;

public abstract class Entity
{
    public static final int IDLE_GRAPHICS = 0;
    public static final int LEFT_GRAPHICS = 1;
    public static final int RIGHT_GRAPHICS = 2;
    public static final int MIDAIR_GRAPHICS = 3;
    public static final int DYING_GRAPHICS = 4;
    protected int graphicsState;

    protected static final int NORMAL_STATE = 0;
    protected static final int FALLING_STATE = 1;
    protected static final int JUMPING_STATE = 2;
    protected static final int DEAD_STATE = 3;
    protected int state;

    protected int speed;
    protected long elapsedAnimationTimeInMs;
    protected Point spawnPoint;
    protected Rectangle boundingBox;
    protected HashMap<Integer, Animation> graphicsMap;

    final protected boolean moveHorizontal(int[][] blockIdMap, int xPixelsMoved)
    {
        int gridX; //Entity x position converted to block index
        int gridYTop; //Entity y position converted to block index
        int gridYBottom; //Entity y position plus height converted to block index
        int idOfBlockAbove; //Id of the block at the top of the entity
        int idOfBlockBelow; //Id of the block at the bottom of the entity

        //Calculate the y index for the blocks above and below, subtract one to avoid getting stuck
        gridYTop = (boundingBox.y / Block.BLOCK_HEIGHT);
        gridYBottom = (boundingBox.y + boundingBox.height - 1) / Block.BLOCK_HEIGHT;

        //Attempt to move left with respect for block collisions
        if (xPixelsMoved < 0)
        {
            //Calculate x index for the block space that will be moved into
            gridX = (boundingBox.x + xPixelsMoved) / Block.BLOCK_WIDTH;

            //Attempting to move off left side of map
            if (gridX < 0 || boundingBox.x <= 0)
            {
                boundingBox.x = 0;
                return true;
            }
            else if (gridYTop < 0 || gridYBottom >= blockIdMap[0].length) //Above or below map, horizontal movement safe
            {
                boundingBox.x += xPixelsMoved;
                return false;
            }

            //Retrieve the indexes of the blocks just above and below the entity
            idOfBlockAbove = blockIdMap[gridX][gridYTop];
            idOfBlockBelow = blockIdMap[gridX][gridYBottom];

            //If either index is not 0 or 100, and is a solid block, then collision
            if ((idOfBlockAbove % 100 != 0 && (idOfBlockAbove / 100) >= 1)
             || (idOfBlockBelow % 100 != 0 && (idOfBlockBelow / 100) >= 1))
            {
                //Move up against the right side of the block to the left
                boundingBox.x = (gridX * Block.BLOCK_WIDTH) + Block.BLOCK_WIDTH;
                return true;
            }
        }
        else if (xPixelsMoved > 0) //Attempt to move right with respect for block collisions
        {
            //Calculate x index for the block space that will be moved into
            gridX = (boundingBox.x + boundingBox.width + xPixelsMoved) / Block.BLOCK_WIDTH;

            //Attempting to move off right side of map
            if (gridX >= blockIdMap.length)
            {
                boundingBox.x = (blockIdMap.length * Block.BLOCK_WIDTH) - boundingBox.width;
                return true;
            }
            else if (gridYTop < 0 || gridYBottom >= blockIdMap[0].length) //Above or below map, horizontal movement safe
            {
                boundingBox.x += xPixelsMoved;
                return false;
            }

            //Retrieve the indexes of the blocks just above and below the entity
            idOfBlockAbove = blockIdMap[gridX][gridYTop];
            idOfBlockBelow = blockIdMap[gridX][gridYBottom];

            //If either index is not 0 or 100, and is a solid block, then collision
            if ((idOfBlockAbove % 100 != 0 && (idOfBlockAbove / 100) >= 1)
             || (idOfBlockBelow % 100 != 0 && (idOfBlockBelow / 100) >= 1))
            {
                //Move up against the left side of the block to the right
                boundingBox.x = (gridX * Block.BLOCK_WIDTH) - boundingBox.width;
                return true;
            }
        }

        //No collision
        boundingBox.x += xPixelsMoved;
        return false;
    }

    final protected void moveVertical(int[][] blockMap, int yPixelsMoved)
    {
        int gridXLeft; //Entity x position converted to block index
        int gridXRight; //Entity x position plus width converted to block index
        int gridY; //Entity y position converted to block index
        int idOfBlockLeft; //Id of the block to the left of the entity
        int idOfBlockRight; //Id of the block to the right of the entity

        //Calculate the x index for the blocks to the left and right, add/subtract one to avoid getting stuck
        gridXLeft = (boundingBox.x + 1) / Block.BLOCK_WIDTH;
        gridXRight = (boundingBox.x + boundingBox.width - 1) / Block.BLOCK_WIDTH;

        //Attempt to move up with respect for block collisions
        if (yPixelsMoved < 0)
        {
            //Calculate x index for the block space that will be moved into
            gridY = (boundingBox.y + yPixelsMoved) / Block.BLOCK_HEIGHT;

            //Attempting to jump above map
            if (gridY < 0)
            {
                boundingBox.y += yPixelsMoved;
                return;
            }
            else if (gridY >= blockMap[0].length) //Below map, kill the entity
            {
                if (gridY > blockMap[0].length)
                {
                    state = DEAD_STATE;
                }
                else //Kill the entity only after it has fallen offscreen
                {
                    boundingBox.y += yPixelsMoved;
                }
                return;
            }
            else if (gridXRight >= blockMap.length) //umping along right side of map
            {
                //Ignore gridXRight, no collision outside of map
                gridXRight = gridXLeft;
            }

            //Retrieve the indexes of the blocks just to the left and right of the entity
            idOfBlockLeft = blockMap[gridXLeft][gridY];
            idOfBlockRight = blockMap[gridXRight][gridY];

            //If either index is not 0 or 100, and is a solid block, then collision
            if ((idOfBlockLeft % 100 != 0 && (idOfBlockLeft / 100) >= 1)
             || (idOfBlockRight % 100 != 0 && (idOfBlockRight / 100) >= 1))
            {
                //Begin falling, move up to bottom of block above
                boundingBox.y = (gridY * Block.BLOCK_HEIGHT) + Block.BLOCK_HEIGHT;
                state = FALLING_STATE;
            }
            else
            {
                //No collision, continue jumping
                boundingBox.y += yPixelsMoved;
                state = JUMPING_STATE;
            }
        }
        else if (yPixelsMoved > 0) //Attempt to move down with respect for block collisions
        {
            //Calculate x index for the block space that will be moved into
            gridY = (boundingBox.y + boundingBox.height + yPixelsMoved) / Block.BLOCK_HEIGHT;

            //Attempting to fall from above the map
            if (gridY < 0)
            {
                boundingBox.y += yPixelsMoved;
                return;
            }
            else if (gridY >= blockMap[0].length) //Below map, kill the entity
            {
                if (gridY > blockMap[0].length)
                {
                    state = DEAD_STATE;
                }
                else
                {
                    //Kill the entity only after it has fallen offscreen
                    boundingBox.y += yPixelsMoved;
                }
                return;
            }
            else if (gridXRight >= blockMap.length) //Falling along right side of map
            {
                gridXRight = gridXLeft; //Ignore gridXRight, no collision outside of map
            }

            //Retrieve the indexes of the blocks just to the left and right of the entity
            idOfBlockLeft = blockMap[gridXLeft][gridY];
            idOfBlockRight = blockMap[gridXRight][gridY];

            //If either side of the entity would collide with a block when falling, the hundreds place has a one
            //Check that the entity is currently above the block it may fall onto (does not include yPixelsMoved)
            if ((boundingBox.y + boundingBox.height <= gridY * Block.BLOCK_HEIGHT)
            && ((idOfBlockLeft % 100 != 0 && (idOfBlockLeft / 100) >= 1)
             || (idOfBlockRight % 100 != 0 && (idOfBlockRight / 100) >= 1)))
            {
                //Landed on ground, move bottom of entity to top of block beneath
                boundingBox.y = (gridY * Block.BLOCK_HEIGHT) - boundingBox.height;
                state = NORMAL_STATE;
            }
            else //No collision, continue falling
            {
                boundingBox.y += yPixelsMoved;
                state = FALLING_STATE;
            }
        }
    }

    final protected int getEntityState()
    {
        return state;
    }

    final protected void setEntityState(int newState)
    {
        state = newState;
    }

    final protected void setGraphicsState(int newGraphicsState)
    {
        //Change if new state is different
        if (graphicsState != newGraphicsState)
        {
            graphicsState = newGraphicsState;
            elapsedAnimationTimeInMs = 0L;
        }
    }

    final protected void setSpawnPosition(int newX, int newY)
    {
        spawnPoint = new Point(newX, newY);
        boundingBox.setLocation(spawnPoint);
    }

    final protected boolean checkCollision(Rectangle otherBoundingBox, int pixelTolerance)
    {
        return (boundingBox.intersects(otherBoundingBox.x + pixelTolerance, otherBoundingBox.y + pixelTolerance,
                otherBoundingBox.width - pixelTolerance, otherBoundingBox.height - pixelTolerance));
    }
}
