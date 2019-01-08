import java.awt.*;
import java.util.HashMap;

public class Enemy extends Entity implements AnimationWatcher
{
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

        determineGraphicsFromDirection();
        boundingBox = new Rectangle(x, y, graphicsMap.get(graphicsState).getImageWidth(), graphicsMap.get(graphicsState).getImageHeight());

        //Store the enemy spawn point for re-spawning
        spawnPoint = new Point(x, y);
    }

    public void update(int[][] blockMap,long loopPeriodInMs)
    {
        if (isActive)
        {
            elapsedAnimationTimeInMs = graphicsMap.get(graphicsState).update(loopPeriodInMs, elapsedAnimationTimeInMs);
            move(blockMap);
        }
    }

    private void move(int[][] blockMap)
    {
        //Allow horizontal movement if enemy is not dead
        if (state != DEAD_STATE)
        {
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
            moveVertical(blockMap, speed);

            //Update graphics if enemy lands
            if (state == FALLING_STATE)
            {
                setGraphicsState(state, direction, false);
            }
        }
        else if (state == DEAD_STATE)
        {
            //Wait for the death animation to finish before re-spawning
            //If the graphic state changed, this is the first update where the player
            //is in the dead state. Set the waiting for animation flag here.
            if (setGraphicsState(state, direction, false))
            {
                waitingForAnimation = true;
            }

            //If the enemy is no longer waiting for an animation, set it to inactive
            if (!waitingForAnimation)
            {
                isActive = false;
                graphicsMap.get(graphicsState).resetAnimation();
            }
        }
    }

    private void determineGraphicsFromDirection()
    {
        //Determine graphics state from direction, this initializes elapsedAnimationTimeInMs
        if (direction == LEFT)
        {
            setGraphicsState(Entity.MOVE_LEFT_GRAPHICS);
        }
        else if (direction == RIGHT)
        {
            setGraphicsState(Entity.MOVE_RIGHT_GRAPHICS);
        }
        else
        {
            setGraphicsState(Entity.IDLE_RIGHT_GRAPHICS);
        }
    }

    public void reset()
    {
        isActive = true;
        state = NORMAL_STATE;
        boundingBox.setLocation(spawnPoint);
    }

    public void draw(Graphics dbGraphics, int xOffset, int yOffset)
    {
        graphicsMap.get(graphicsState).draw(dbGraphics, boundingBox.x + xOffset, boundingBox.y + yOffset);
    }

    public void animationHasEnded()
    {
        if (waitingForAnimation)
        {
            waitingForAnimation = false;
        }
    }
}
