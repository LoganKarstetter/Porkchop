import java.awt.*;
import java.util.HashMap;

public class Player extends Entity
{
    private static final int MAX_JUMPING_UPDATES = 14;
    private int numOfJumpingUpdates;
    private InputComponent inputComponent;
    private LevelWatcher levelWatcher;
    private boolean hopOnDeath;

    public Player(int x, int y, int speedInPixels, int defaultGraphicsState,
                  HashMap<Integer, Animation> playerSpecificGraphics, InputComponent playerInputComponent)
    {
        //Set class data
        state = NORMAL_STATE;
        numOfJumpingUpdates = 0;
        elapsedAnimationTimeInMs = 0L;
        hopOnDeath = false;

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
                    hopOnDeath = true;
                }
            }
        }
    }

    private boolean checkEventBlockCollisions(EventBlock[] eventBlocks, int numEventBlocks)
    {
        for (int i = 0; i < numEventBlocks; i++)
        {
            if (checkCollision(eventBlocks[i].getBoundingBox(), 10))
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
                    hopOnDeath = true;
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
                    setGraphicsState(Entity.LEFT_GRAPHICS);
                }
            }
            else if (inputComponent.right)
            {
                if (!moveHorizontal(blockMap, speed)) //Move ribbons only if there was no collision
                {
                    changeRibbonScrollDirection(ribbons, numRibbons, Ribbon.SCROLL_LEFT);
                }
                if (state != FALLING_STATE && state != JUMPING_STATE)
                {
                    setGraphicsState(Entity.RIGHT_GRAPHICS);
                }
            }
            else if (state != FALLING_STATE && state != JUMPING_STATE) //Idle
            {
                setGraphicsState(Entity.IDLE_GRAPHICS);
            }
        }

        //Determine vertical movement
        if (state == NORMAL_STATE)
        {
            //Make the player fall if it is standing on thin air
            moveVertical(blockMap, speed);

            //Attempt to jump upwards if the player is still grounded
            if (state == NORMAL_STATE && inputComponent.up)
            {
                moveVertical(blockMap, -speed);
                numOfJumpingUpdates++;
                setGraphicsState(Entity.MIDAIR_GRAPHICS);
            }
            else if (state == FALLING_STATE) //Started falling
            {
                setGraphicsState(Entity.MIDAIR_GRAPHICS);
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
            }
        }
        else if (state == FALLING_STATE)
        {
            //Clear jumping update counter
            numOfJumpingUpdates = 0;
            moveVertical(blockMap, speed);

            //Back to idle
            if (state == NORMAL_STATE)
            {
                setGraphicsState(Entity.IDLE_GRAPHICS);
            }
        }
        else if (state == DEAD_STATE)
        {
            //Death animation
            setGraphicsState(Entity.DYING_GRAPHICS);
            changeRibbonScrollDirection(ribbons, numRibbons, Ribbon.SCROLL_STILL);

            //Do a little jump then fall out of the map
            if (hopOnDeath)
            {
                if (numOfJumpingUpdates < MAX_JUMPING_UPDATES/2)
                {
                    boundingBox.y += (-speed);
                    numOfJumpingUpdates++;
                }
                else //Fall until not longer visible, then respawn
                {
                    boundingBox.y += speed;
                    if (boundingBox.y > (blockMap[0].length * Block.BLOCK_HEIGHT))
                    {
                        //Once the hop is done, reset data
                        hopOnDeath = false;
                    }
                }
            }

            if (!hopOnDeath)
            {
                state = NORMAL_STATE;
                numOfJumpingUpdates = 0;
                boundingBox.setLocation(spawnPoint);
                setGraphicsState(Entity.IDLE_GRAPHICS);
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
        graphicsMap.get(graphicsState).draw(dbGraphics, boundingBox.x + xOffset, boundingBox.y + yOffset, elapsedAnimationTimeInMs);
    }

    public void setLevelWatcher(LevelWatcher gameLevelWatcher)
    {
        levelWatcher = gameLevelWatcher;
    }
}