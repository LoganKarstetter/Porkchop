import java.awt.*;
import java.util.HashMap;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class Enemy extends Entity implements AnimationWatcher
{
    /** The number of pixels an enemy falls every game loop */
    private static final int ENEMY_VERTICAL_SPEED = 5;
    /** The flag specifying if this enemy is alive and should be drawn */
    private boolean isActive;

    /**
     * Create an enemy.
     * @param x The initial x position to place the enemy at.
     * @param y The initial y position to place the enemy at.
     * @param speedInPixels The horizontal movement speed.
     * @param directionToMove The initial direction to move.
     * @param enemySpecificGraphics The graphics map for the enemy.
     */
    public Enemy(int x, int y, int speedInPixels, int directionToMove,
                 HashMap<Integer, Animation> enemySpecificGraphics)
    {
        //Set class data
        state = NORMAL_STATE;
        elapsedAnimationTimeInMs = 0L;

        //Store enemy data
        speed = speedInPixels;
        direction = directionToMove;
        graphicsMap = enemySpecificGraphics;
        waitingForAnimation = false;
        isActive = true;

        setGraphicsState(state, direction, true);
        boundingBox = new Rectangle(x, y, graphicsMap.get(graphicsState).getImageWidth(), graphicsMap.get(graphicsState).getImageHeight());

        //Store the enemy spawn point for re-spawning
        spawnPoint = new Point(x, y);
    }

    /**
     * Update the enemy's animation time and move it.
     * @param blockMap The grid of blocks used to check for movement collisions.
     * @param eventBlocks The event blocks used to check for movement collisions.
     * @param numEventBlocks The number of event blocks.
     * @param loopPeriodInMs The loop period of the game cycle.
     */
    public void update(int[][] blockMap, EventBlock[] eventBlocks, int numEventBlocks, long loopPeriodInMs)
    {
        if (isActive)
        {
            elapsedAnimationTimeInMs = graphicsMap.get(graphicsState).update(loopPeriodInMs, elapsedAnimationTimeInMs);
            move(blockMap, eventBlocks, numEventBlocks);
        }
    }

    /**
     * Check collisions with event blocks, specifically dangerous blocks. If the
     * enemy's bounding box intersects the block the enemy will die.
     * @param eventBlocks The event blocks to check collision for.
     * @param numEventBlocks The number of event blocks.
     */
    private void checkEventBlockCollisions(EventBlock[] eventBlocks, int numEventBlocks)
    {
        for (int i = 0; i < numEventBlocks; i++)
        {
            if (checkCollision(eventBlocks[i].getBoundingBox(), 30)
                    && eventBlocks[i].getBlockType() == EventBlock.BLOCK_DANGER)
            {
                //Kill the enemy
                state = DEAD_STATE;
            }
        }
    }

    /**
     * Move the enemy according to its state.
     * @param blockMap The grid of blocks used to check for movement collisions.
     * @param eventBlocks The event blocks used to check for movement collisions.
     * @param numEventBlocks The number of event blocks.
     */
    private void move(int[][] blockMap, EventBlock[] eventBlocks, int numEventBlocks)
    {
        //Allow horizontal movement if enemy is not dead
        if (state != DEAD_STATE)
        {
            //Check for event block collisions
            checkEventBlockCollisions(eventBlocks, numEventBlocks);

            //Attempt to move depending on direction
            if (direction == LEFT)
            {
                //Change direction if there was collision
                if (moveHorizontal(blockMap, -speed))
                {
                    direction = RIGHT;
                    if (state != FALLING_STATE) //Change graphics
                    {
                        setGraphicsState(Entity.MOVE_RIGHT_GRAPHICS);
                    }
                }
            }
            else if (direction == RIGHT)
            {
                if (moveHorizontal(blockMap, speed))
                {
                    direction = LEFT;
                    if (state != FALLING_STATE)
                    {
                        setGraphicsState(Entity.MOVE_LEFT_GRAPHICS);
                    }
                }
            }
        }

        //Determine vertical movement
        if (state == NORMAL_STATE)
        {
            //Make the enemy fall if it is standing on thin air
            moveVertical(blockMap, speed);

            //Update graphics if enemy starts falling
            if (state == FALLING_STATE)
            {
                setGraphicsState(state, direction, false);
            }
        }
        else if (state == FALLING_STATE)
        {
            moveVertical(blockMap, ENEMY_VERTICAL_SPEED);

            //Update graphics if enemy lands
            if (state == NORMAL_STATE)
            {
                setGraphicsState(state, direction, true);
            }
        }
        else if (state == DEAD_STATE)
        {
            //Wait for the death animation to finish before re-spawning
            //If the graphic state changed, this is the first update where the enemy
            //is in the dead state. Set the waiting for animation flag here.
            if (setGraphicsState(state, direction, false))
            {
                waitingForAnimation = true;

                //Move the enemy's position upwards so that the smoke puff is relative to the blocks
                boundingBox.y = (boundingBox.y / Block.BLOCK_HEIGHT) * Block.BLOCK_HEIGHT;
            }

            //If the enemy is no longer waiting for an animation, set it to inactive
            if (!waitingForAnimation)
            {
                isActive = false;
            }
        }
    }

    /**
     * Draw the enemy.
     * @param dbGraphics The graphics object that will draw the enemy.
     * @param xOffset The x position to draw the enemy at on the screen.
     * @param yOffset The y position to draw the enemy at on the screen.
     */
    public void draw(Graphics dbGraphics, int xOffset, int yOffset)
    {
        graphicsMap.get(graphicsState).draw(dbGraphics, boundingBox.x + xOffset, boundingBox.y + yOffset, elapsedAnimationTimeInMs);
    }

    /**
     * Reset the enemy. This method essentially re-spawns the enemy.
     */
    public void reset()
    {
        isActive = true;
        state = NORMAL_STATE;
        setGraphicsState(state, direction, true);
        boundingBox.setLocation(spawnPoint);
    }

    /**
     * This method is called when an enemy's death animation ends. It sets a flag
     * specifying that the enemy no longer needs to be drawn.
     */
    public void animationHasEnded()
    {
        if (waitingForAnimation)
        {
            waitingForAnimation = false;
        }
    }
}
