import java.awt.*;
import java.util.HashMap;

/**
 * @author Logan Karstetter
 * Date: 2018
 */
public abstract class Entity
{
    /** The enum representing the idle left graphics state */
    public static final int IDLE_LEFT_GRAPHICS = 0;
    /** The enum representing the idle right graphics state */
    public static final int IDLE_RIGHT_GRAPHICS = 1;
    /** The enum representing the moving left graphics state */
    public static final int MOVE_LEFT_GRAPHICS = 2;
    /** The enum representing the moving right graphics state */
    public static final int MOVE_RIGHT_GRAPHICS = 3;
    /** The enum representing the jumping/falling left graphics state */
    public static final int MIDAIR_LEFT_GRAPHICS = 4;
    /** The enum representing the jumping/falling right graphics state */
    public static final int MIDAIR_RIGHT_GRAPHICS = 5;
    /** The enum representing the dying left graphics state */
    public static final int DYING_LEFT_GRAPHICS = 6;
    /** The enum representing the dying right graphics state */
    public static final int DYING_RIGHT_GRAPHICS = 7;
    /** The graphic state of the entity */
    protected int graphicsState;

    /** The enum representing the entity's normal state */
    protected static final int NORMAL_STATE = 0;
    /** The enum representing the entity's falling state */
    protected static final int FALLING_STATE = 1;
    /** The enum representing the entity's jumping state */
    protected static final int JUMPING_STATE = 2;
    /** The enum representing the entity's dead state */
    protected static final int DEAD_STATE = 3;
    /** The game logic state of the entity */
    protected int state;

    /** The enum representing the not moving direction */
    public static final int STILL = 0;
    /** The enum representing the left direction */
    public static final int LEFT = 1;
    /** The enum representing the right direction */
    public static final int RIGHT = 2;
    /** The direction state of the entity. */
    protected int direction;

    /** The number of pixels the entity moves every game update */
    protected int speed;
    /** The amount of time elapsed in the entity's current animation */
    protected long elapsedAnimationTimeInMs;
    /** A flag specifying that the entity is waiting for an animation to complete */
    protected boolean waitingForAnimation;
    /** The spawn point of the entity */
    protected Point spawnPoint;
    /** The bounding box of the entity that stores its position and dimensions */
    protected Rectangle boundingBox;
    /** Maps graphics states (integers) to animations */
    protected HashMap<Integer, Animation> graphicsMap;
    /** The watcher that is notified when the entity triggers level events */
    protected LevelWatcher levelWatcher;

    /**
     * Move the entity horizontally. The direction of movement is determined by the
     * xPixelsMove argument. If the entity can freely moved the requested number of
     * pixels then this method returns false. Otherwise, if the entity collides with
     * a block this method will return true and it will not move the full distance.
     * @param blockIdMap The grid of blocks id's that are checked for collision detection.
     * @param xPixelsMoved The number of pixels to move.
     * @return True if the entity collided with a block, false otherwise.
     */
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

    /**
     * Move the entity vertically. The direction of movement is determined by the
     * yPixelsMove argument. If the entity can freely moved the requested number of
     * pixels then this method returns false. Otherwise, if the entity collides with
     * a block this method will return true and it will not move the full distance.
     * @param blockIdMap The grid of blocks id's that are checked for collision detection.
     * @param yPixelsMoved The number of pixels to move.
     * @return True if the entity collided with a block, false otherwise.
     */
    final protected void moveVertical(int[][] blockIdMap, int yPixelsMoved)
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
            else if (gridY >= blockIdMap[0].length) //Below map, kill the entity
            {
                if (gridY > blockIdMap[0].length)
                {
                    state = DEAD_STATE;
                }
                else //Kill the entity only after it has fallen offscreen
                {
                    boundingBox.y += yPixelsMoved;
                }
                return;
            }
            else if (gridXRight >= blockIdMap.length) //Jumping along right side of map
            {
                //Ignore gridXRight, no collision outside of map
                gridXRight = gridXLeft;
            }

            //Retrieve the indexes of the blocks just to the left and right of the entity
            idOfBlockLeft = blockIdMap[gridXLeft][gridY];
            idOfBlockRight = blockIdMap[gridXRight][gridY];

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
            else if (gridY >= blockIdMap[0].length) //Below map, kill the entity
            {
                if (gridY > blockIdMap[0].length)
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
            else if (gridXRight >= blockIdMap.length) //Falling along right side of map
            {
                gridXRight = gridXLeft; //Ignore gridXRight, no collision outside of map
            }

            //Retrieve the indexes of the blocks just to the left and right of the entity
            idOfBlockLeft = blockIdMap[gridXLeft][gridY];
            idOfBlockRight = blockIdMap[gridXRight][gridY];

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

    /**
     * Check collisions against an inputted bounding box.
     * @param otherBoundingBox The bounding box to check collision against.
     * @param pixelTolerance A tolerance value for the collision.
     * @return True if the rectangles intersect, false otherwise.
     */
    final protected boolean checkCollision(Rectangle otherBoundingBox, int pixelTolerance)
    {
        return (boundingBox.intersects(otherBoundingBox.x + pixelTolerance, otherBoundingBox.y + pixelTolerance,
                otherBoundingBox.width - pixelTolerance, otherBoundingBox.height - pixelTolerance));
    }

    /**
     * Set the graphics state of an entity. This method takes the value
     * representing the new state directly. If the new graphics state
     * is not the same as the existing one, this method will reset
     * the elapsed animation time to zero and set the new state.
     * @param newGraphicsState The new graphics state.
     * @return True if the state changed, false otherwise.
     */
    final protected boolean setGraphicsState(int newGraphicsState)
    {
        //Reset animation timer and return true if the state changed
        if (graphicsState != newGraphicsState)
        {
            graphicsState = newGraphicsState;
            elapsedAnimationTimeInMs = 0L;
            return true;
        }
        return false;
    }

    /**
     * Set the graphics state based off the current entity state, the last moved direction,
     * and whether or not the entity is currently moving left or right (walking).
     * @param state The current state of the entity.
     * @param lastDirection The last moved direction of the entity.
     * @param isMoving A flag specifying whether the entity is moving left or right.
     * @return True if the graphics state changed, false otherwise.
     */
    final protected boolean setGraphicsState(int state, int lastDirection, boolean isMoving)
    {
        //Determine the new graphics state
        int newGraphicsState;
        if (state == NORMAL_STATE && !isMoving)
        {
            newGraphicsState = ( lastDirection == LEFT ? IDLE_LEFT_GRAPHICS : IDLE_RIGHT_GRAPHICS );
        }
        else if (state == NORMAL_STATE) //Is moving
        {
            newGraphicsState = ( lastDirection == LEFT ? MOVE_LEFT_GRAPHICS : MOVE_RIGHT_GRAPHICS );
        }
        else if (state == FALLING_STATE || state == JUMPING_STATE)
        {
            newGraphicsState = ( lastDirection == LEFT ? MIDAIR_LEFT_GRAPHICS : MIDAIR_RIGHT_GRAPHICS );
        }
        else //state == DEAD_STATE
        {
            newGraphicsState = ( lastDirection == LEFT ? DYING_LEFT_GRAPHICS : DYING_RIGHT_GRAPHICS );
        }

        //Reset animation timer and return true if the state changed
        return setGraphicsState(newGraphicsState);
    }

    /**
     * Set the spawn position for the entity.
     * @param newX The new x position.
     * @param newY The new y position.
     */
    final protected void setSpawnPosition(int newX, int newY)
    {
        spawnPoint = new Point(newX, newY);
        boundingBox.setLocation(spawnPoint);
    }

    /**
     * Get the current state of the entity.
     * @return The entity's game state.
     */
    final protected int getEntityState()
    {
        return state;
    }

    /**
     * Set the current state of the entity.
     * @param newState The entity's new game state.
     */
    final protected void setEntityState(int newState)
    {
        state = newState;
    }

    /**
     * Set the level watcher to notify when level events occur.
     * Note: Only the player and the purple boar enemy should call this!
     * @param gameLevelWatcher The level watcher.
     */
    public void setLevelWatcher(LevelWatcher gameLevelWatcher)
    {
        levelWatcher = gameLevelWatcher;
    }
}
