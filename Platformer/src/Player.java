import java.awt.*;
import java.util.HashMap;

public class Player extends Entity implements AnimationWatcher
{
    private static final int MAX_JUMPING_UPDATES = 14;
    private int numOfJumpingUpdates;
    private InputComponent inputComponent;
    private LevelWatcher levelWatcher;
    private boolean waitingForAnimation;

    public Player(int x, int y, int speedInPixels, int defaultGraphicsState,
                  HashMap<Integer, Animation> playerSpecificGraphics, InputComponent playerInputComponent)
    {
        //Set class data
        state = NORMAL_STATE;
        numOfJumpingUpdates = 0;
        elapsedAnimationTimeInMs = 0L;
        lastDirectionMoved = Entity.RIGHT;
        waitingForAnimation = false;

        //Store player data
        speed = speedInPixels;
        graphicsState = defaultGraphicsState;
        graphicsMap = playerSpecificGraphics;
        inputComponent = playerInputComponent;
        boundingBox = new Rectangle(x, y, graphicsMap.get(graphicsState).getImageWidth(), graphicsMap.get(graphicsState).getImageHeight());

        //Store the player spawn point for re-spawning
        spawnPoint = new Point(x, y);
    }

    public Point update(int[][] blockMap, Enemy[] enemies, int numOfEnemies, EventBlock[] eventBlocks,
                        int numEventBlocks, Ribbon[] ribbons, int numRibbons, long loopPeriodInMs)
    {
        inputComponent.update();
        elapsedAnimationTimeInMs = graphicsMap.get(graphicsState).update(loopPeriodInMs, elapsedAnimationTimeInMs);
        move(blockMap, enemies, numOfEnemies, eventBlocks, numEventBlocks, ribbons, numRibbons);
        return boundingBox.getLocation();
    }

    private void checkEnemyCollisions(Enemy[] enemies, int numEnemies)
    {
        for (int i = 0; i < numEnemies; i++)
        {
            if (checkCollision(enemies[i].boundingBox, 10) && enemies[i].getEntityState() != DEAD_STATE)
            {
                numOfJumpingUpdates = 0; //Clear jumping

                //If the player has landed on the enemy, kill the enemy
                if ((boundingBox.y + boundingBox.height/2) <= enemies[i].boundingBox.y)
                {
                    enemies[i].setEntityState(DEAD_STATE);
                    state = JUMPING_STATE;
                }
                else //Kill the player
                {
                    state = DEAD_STATE;
                }
            }
        }
    }

    private boolean checkEventBlockCollisions(EventBlock[] eventBlocks, int numEventBlocks)
    {
        for (int i = 0; i < numEventBlocks; i++)
        {
            if (checkCollision(eventBlocks[i].getBoundingBox(), 45))
            {
                //Perform various actions depending on the block type
                if (eventBlocks[i].getBlockType() == EventBlock.BLOCK_LEVEL)
                {
                    //Transition to the next level
                    state = NORMAL_STATE;
                    levelWatcher.changeToNextLevel();
                    return true;
                }
                else if (eventBlocks[i].getBlockType() == EventBlock.BLOCK_DANGER)
                {
                    //Kill the player
                    state = DEAD_STATE;
                }
            }
        }
        return false;
    }

    private void move(int[][] blockMap, Enemy[] enemies, int numEnemies, EventBlock[] eventBlocks,
                      int numEventBlocks, Ribbon[] ribbons, int numRibbons)
    {
        //Check for collisions, then move if not dead
        if (state != DEAD_STATE)
        {
            //Determine if the player has encountered any enemies or event blocks
            checkEnemyCollisions(enemies, numEnemies);
            if (checkEventBlockCollisions(eventBlocks, numEventBlocks))
                return; //If a new level has loaded, skip the movement this update

            //Default ribbons to not scroll
            changeRibbonScrollDirection(ribbons, numRibbons, Ribbon.SCROLL_STILL);

            //Attempt to move depending on direction
            if (inputComponent.left)
            {
                if (!moveHorizontal(blockMap, -speed)) //Move ribbons only if there was no collision
                {
                    changeRibbonScrollDirection(ribbons, numRibbons, Ribbon.SCROLL_RIGHT);
                }
                if (state != FALLING_STATE && state != JUMPING_STATE) //Graphics change
                {
                    setGraphicsState(Entity.MOVE_LEFT_GRAPHICS);
                }
                lastDirectionMoved = Entity.LEFT;
            }
            else if (inputComponent.right)
            {
                if (!moveHorizontal(blockMap, speed)) //Move ribbons only if there was no collision
                {
                    changeRibbonScrollDirection(ribbons, numRibbons, Ribbon.SCROLL_LEFT);
                }
                if (state != FALLING_STATE && state != JUMPING_STATE)
                {
                    setGraphicsState(Entity.MOVE_RIGHT_GRAPHICS);
                }
                lastDirectionMoved = Entity.RIGHT;
            }
            else if (state != FALLING_STATE && state != JUMPING_STATE) //Idle
            {
                if (lastDirectionMoved == Entity.RIGHT)
                {
                    setGraphicsState(Entity.IDLE_RIGHT_GRAPHICS);
                }
                else
                {
                    setGraphicsState(Entity.IDLE_LEFT_GRAPHICS);
                }
            }
        }

        //Determine vertical movement
        if (state == NORMAL_STATE)
        {
            //Make the player fall if it is standing on thin air
            moveVertical(blockMap, speed);

            //Player is on the ground
            if (state == NORMAL_STATE)
            {
                //Attempt to jump upwards
                if (inputComponent.up)
                {
                    moveVertical(blockMap, -speed);
                    numOfJumpingUpdates++;
                    if (lastDirectionMoved == Entity.RIGHT)
                    {
                        setGraphicsState(Entity.MIDAIR_RIGHT_GRAPHICS);
                    }
                    else
                    {
                        setGraphicsState(Entity.MIDAIR_LEFT_GRAPHICS);
                    }
                }
            }
            else if (state == FALLING_STATE) //Started falling
            {
                if (lastDirectionMoved == Entity.RIGHT)
                {
                    setGraphicsState(Entity.MIDAIR_RIGHT_GRAPHICS);
                }
                else
                {
                    setGraphicsState(Entity.MIDAIR_LEFT_GRAPHICS);
                }
            }
        }
        else if (state == JUMPING_STATE)
        {
            //Continue jumping if possible
            if (numOfJumpingUpdates < MAX_JUMPING_UPDATES)
            {
                moveVertical(blockMap, -speed);
                numOfJumpingUpdates++;
            }
            else //Max height reached
            {
                state = FALLING_STATE;
                if (lastDirectionMoved == Entity.RIGHT)
                {
                    setGraphicsState(Entity.MIDAIR_RIGHT_GRAPHICS);
                }
                else
                {
                    setGraphicsState(Entity.MIDAIR_LEFT_GRAPHICS);
                }
            }
        }
        else if (state == FALLING_STATE)
        {
            //Clear jumping update counter
            numOfJumpingUpdates = 0;
            moveVertical(blockMap, speed);

            if (lastDirectionMoved == Entity.RIGHT)
            {
                setGraphicsState(Entity.MIDAIR_RIGHT_GRAPHICS);
            }
            else
            {
                setGraphicsState(Entity.MIDAIR_LEFT_GRAPHICS);
            }
        }
        else if (state == DEAD_STATE)
        {
            //Wait for the death animation to finish before re-spawning
            if (lastDirectionMoved == Entity.RIGHT)
            {
                //If the graphic state changed, this is the first update where the player
                //is in the dead state. Set the waiting for animation flag here.
                if (setGraphicsState(Entity.DYING_RIGHT_GRAPHICS))
                {
                    waitingForAnimation = true;
                }
            }
            else
            {
                if (setGraphicsState(Entity.DYING_LEFT_GRAPHICS))
                {
                    waitingForAnimation = true;
                }
            }
            changeRibbonScrollDirection(ribbons, numRibbons, Ribbon.SCROLL_STILL);

            if (!waitingForAnimation)
            {
                state = NORMAL_STATE;
                numOfJumpingUpdates = 0;
                boundingBox.setLocation(spawnPoint);
                setGraphicsState(Entity.IDLE_RIGHT_GRAPHICS);
                resetRibbons(ribbons, numRibbons);
                resetEnemies(enemies, numEnemies);
            }
        }
    }

    private void changeRibbonScrollDirection(Ribbon[] ribbons, int numRibbons, int newScrollDirection)
    {
        //Set the ribbon scroll direction
        for (int i = 0; i < numRibbons; i++)
        {
            ribbons[i].setScrollDirection(newScrollDirection);
        }
    }

    public void resetEnemies(Enemy[] enemies, int numEnemies)
    {
        for (int i = 0; i < numEnemies; i++)
        {
            enemies[i].reset();
        }
    }

    private void resetRibbons(Ribbon[] ribbons, int numRibbons)
    {
        //Set the ribbon scroll direction
        for (int i = 0; i < numRibbons; i++)
        {
            ribbons[i].reset();
        }
    }

    public void draw(Graphics dbGraphics, int xOffset, int yOffset)
    {
        graphicsMap.get(graphicsState).draw(dbGraphics, boundingBox.x + xOffset, boundingBox.y + yOffset);
    }

    public void setLevelWatcher(LevelWatcher gameLevelWatcher)
    {
        levelWatcher = gameLevelWatcher;
    }

    public void animationHasEnded()
    {
        if (waitingForAnimation)
        {
            waitingForAnimation = false;
        }
    }
}
