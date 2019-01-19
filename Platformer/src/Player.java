import java.awt.*;
import java.util.HashMap;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class Player extends Entity implements AnimationWatcher
{
    /** The maximum number of game cycles that the player can be rising without falling */
    private static final int MAX_JUMPING_UPDATES = 14;

    /** The number of updates where the player has been jumping */
    private int numOfJumpingUpdates;
    /** The inputComponent that process user input */
    private InputComponent inputComponent;
    /** The soundManager that plays game sounds */
    private SoundManager soundManager;
    /** The watcher that is notified when the player triggers level events */
    private LevelWatcher levelWatcher;

    /**
     * Create a new player.
     * @param x The x spawn position of the player.
     * @param y The y spawn position of the player.
     * @param speedInPixels The player's movement speed in pixels.
     * @param defaultGraphicsState The default graphics state that the player assumes.
     * @param playerSpecificGraphics The map of graphics states and animations for the player.
     * @param playerInputComponent The inputComponent that processes user input.
     * @param playerSoundManager The soundManager that plays game sounds.
     */
    public Player(int x, int y, int speedInPixels, int defaultGraphicsState, HashMap<Integer, Animation> playerSpecificGraphics,
                  InputComponent playerInputComponent, SoundManager playerSoundManager)
    {
        //Set class data
        state = NORMAL_STATE;
        numOfJumpingUpdates = 0;
        elapsedAnimationTimeInMs = 0L;
        direction = Entity.RIGHT;
        waitingForAnimation = false;

        //Store player data
        speed = speedInPixels;
        graphicsState = defaultGraphicsState;
        graphicsMap = playerSpecificGraphics;
        inputComponent = playerInputComponent;
        soundManager = playerSoundManager;
        boundingBox = new Rectangle(x, y, graphicsMap.get(graphicsState).getImageWidth(), graphicsMap.get(graphicsState).getImageHeight());

        //Store the player spawn point for re-spawning
        spawnPoint = new Point(x, y);
    }

    /**
     * Update the player. This method updates processes user input, updates the player's animations,
     * moves the player, and finally returns the player's new location.
     * @param blockMap The grid of id's specifying which blocks are solid and which are not.
     * @param enemies The enemies present in the current level.
     * @param numOfEnemies The number of enemies.
     * @param eventBlocks The number of event blocks in the current level.
     * @param numEventBlocks The number of event blocks.
     * @param ribbons The ribbons draw in the background of the current level.
     * @param numRibbons The number of ribbons.
     * @param loopPeriodInMs The loop period of the game cycle.
     * @return The player's new position.
     */
    public Point update(int[][] blockMap, Enemy[] enemies, int numOfEnemies, EventBlock[] eventBlocks,
                        int numEventBlocks, Ribbon[] ribbons, int numRibbons, long loopPeriodInMs)
    {
        inputComponent.update();
        elapsedAnimationTimeInMs = graphicsMap.get(graphicsState).update(loopPeriodInMs, elapsedAnimationTimeInMs);
        move(blockMap, enemies, numOfEnemies, eventBlocks, numEventBlocks, ribbons, numRibbons);
        return boundingBox.getLocation();
    }

    /**
     * Move the player and check for collisions according to its state.
     * @param blockMap The grid of id's specifying which blocks are solid and which are not.
     * @param enemies The enemies present in the current level.
     * @param numEnemies The number of enemies.
     * @param eventBlocks The number of event blocks in the current level.
     * @param numEventBlocks The number of event blocks.
     * @param ribbons The ribbons draw in the background of the current level.
     * @param numRibbons The number of ribbons.
     */
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
            levelWatcher.changeRibbonScrollDirection(Ribbon.SCROLL_STILL);

            //Attempt to move depending on direction
            if (inputComponent.left)
            {
                if (!moveHorizontal(blockMap, -speed)) //Move ribbons only if there was no collision
                {
                    levelWatcher.changeRibbonScrollDirection(Ribbon.SCROLL_RIGHT);
                }
                if (state != FALLING_STATE && state != JUMPING_STATE) //Graphics change
                {
                    setGraphicsState(Entity.MOVE_LEFT_GRAPHICS);
                }
                direction = Entity.LEFT;
            }
            else if (inputComponent.right)
            {
                if (!moveHorizontal(blockMap, speed)) //Move ribbons only if there was no collision
                {
                    levelWatcher.changeRibbonScrollDirection(Ribbon.SCROLL_LEFT);
                }
                if (state != FALLING_STATE && state != JUMPING_STATE)
                {
                    setGraphicsState(Entity.MOVE_RIGHT_GRAPHICS);
                }
                direction = Entity.RIGHT;
            }
            else if (state != FALLING_STATE && state != JUMPING_STATE) //Idle
            {
                if (direction == Entity.RIGHT)
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
                    setGraphicsState(state, direction, false);
                }
            }
            else if (state == FALLING_STATE) //Started falling
            {
                setGraphicsState(state, direction, false);
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
                setGraphicsState(state, direction, false);
            }
        }
        else if (state == FALLING_STATE)
        {
            //Clear jumping update counter
            numOfJumpingUpdates = 0;
            moveVertical(blockMap, speed);
            setGraphicsState(state, direction, false);
        }
        else if (state == DEAD_STATE)
        {
            //Wait for the death animation to finish before re-spawning
            //If the graphic state changed, this is the first update where the player
            //is in the dead state. Set the waiting for animation flag here.
            if (setGraphicsState(state, direction, false))
            {
                waitingForAnimation = true;

                //Move the player's position upwards so that the smoke puff is relative to the blocks
                boundingBox.y = (boundingBox.y / Block.BLOCK_HEIGHT) * Block.BLOCK_HEIGHT;

                //Play the death sound
                soundManager.playSound("Poof", false);
            }
            levelWatcher.changeRibbonScrollDirection(Ribbon.SCROLL_STILL);

            if (!waitingForAnimation)
            {
                //Inform the game the player has died
                levelWatcher.playerHasDied();

                //Reset player data
                state = NORMAL_STATE;
                numOfJumpingUpdates = 0;
                boundingBox.setLocation(spawnPoint);
                setGraphicsState(Entity.IDLE_RIGHT_GRAPHICS);
            }
        }
    }

    /**
     * Check if the enemy has collided with an enemies. If the player has collided with
     * an enemy, but either jumped/fell on top of them, then the enemy will be killed. Otherwise,
     * the player will be killed.
     **@param enemies The enemies present in the current level.
     * @param numOfEnemies The number of enemies.
     */
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
                    soundManager.playSound("Poof", false);
                    levelWatcher.enemyDefeated();
                    state = JUMPING_STATE;
                }
                else //Kill the player
                {
                    state = DEAD_STATE;
                }
            }
        }
    }

    /**
     * Check if the player has interacted with any event blocks.
     * @param eventBlocks The number of event blocks in the current level.
     * @param numEventBlocks The number of event blocks.
     * @return True if the player has triggered the start of the next level, false otherwise.
     */
    private boolean checkEventBlockCollisions(EventBlock[] eventBlocks, int numEventBlocks)
    {
        for (int i = 0; i < numEventBlocks; i++)
        {
            if (checkCollision(eventBlocks[i].getBoundingBox(), 30))
            {
                //Perform various actions depending on the block type
                if (eventBlocks[i].getBlockType() == EventBlock.BLOCK_LEVEL)
                {
                    //Transition to the next level
                    state = NORMAL_STATE;
                    levelWatcher.changeToNextLevel(inputComponent);
                    return true;
                }
                else if (eventBlocks[i].getBlockType() == EventBlock.BLOCK_DANGER)
                {
                    //Kill the player
                    state = DEAD_STATE;
                }
                else if (eventBlocks[i].getBlockType() == EventBlock.BLOCK_COLLECT)
                {
                    //Inform the game that an item was collected
                    soundManager.playSound("Pop", false);
                    eventBlocks[i].activate();
                    levelWatcher.itemCollected();
                }
            }
        }
        return false;
    }

    /**
     * Draw the player.
     * @param dbGraphics The graphics object used to draw the player.
     * @param xOffset The x offset added to the player's x coordinate to determine where to draw it.
     * @param yOffset The y offset added to the player's y coordinate to determine where to draw it.
     */
    public void draw(Graphics dbGraphics, int xOffset, int yOffset)
    {
        graphicsMap.get(graphicsState).draw(dbGraphics, boundingBox.x + xOffset, boundingBox.y + yOffset, elapsedAnimationTimeInMs);
    }

    /**
     * This method is called when an player's death animation ends. It sets a flag
     * specifying that the player can respawn without interrupting.
     */
    public void animationHasEnded()
    {
        if (waitingForAnimation)
        {
            waitingForAnimation = false;
        }
    }

    /**
     * Set the level watcher to notify when level event occur.
     * @param gameLevelWatcher The level watcher.
     */
    public void setLevelWatcher(LevelWatcher gameLevelWatcher)
    {
        levelWatcher = gameLevelWatcher;
    }
}
