import java.awt.*;
import java.util.HashMap;

public class Enemy extends Entity implements AnimationWatcher
{
    private static final int ENEMY_VERTICAL_SPEED = 5;
    private boolean isActive;

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

    public void update(int[][] blockMap, EventBlock[] eventBlocks, int numEventBlocks, long loopPeriodInMs)
    {
        if (isActive)
        {
            elapsedAnimationTimeInMs = graphicsMap.get(graphicsState).update(loopPeriodInMs, elapsedAnimationTimeInMs);
            move(blockMap, eventBlocks, numEventBlocks);
        }
    }

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

    public void reset()
    {
        isActive = true;
        state = NORMAL_STATE;
        setGraphicsState(state, direction, true);
        boundingBox.setLocation(spawnPoint);
    }

    public void draw(Graphics dbGraphics, int xOffset, int yOffset)
    {
        graphicsMap.get(graphicsState).draw(dbGraphics, boundingBox.x + xOffset, boundingBox.y + yOffset, elapsedAnimationTimeInMs);
    }

    public void animationHasEnded()
    {
        if (waitingForAnimation)
        {
            waitingForAnimation = false;
        }
    }
}
