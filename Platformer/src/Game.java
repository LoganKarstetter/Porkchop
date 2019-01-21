import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class Game implements LevelWatcher, MouseWatcher
{
    /** The maximum number of enemies in a single level */
    public static final int MAX_ENEMIES = 15;
    /** The maximum number of event blocks in a single level */
    public static final int MAX_EVENT_BLOCKS = 10;
    /** The maximum number of ribbons in a single level */
    public static final int MAX_RIBBONS = 2;

    /** The constant representing the main menu state */
    public static final int MAIN_MENU = 0;
    /** The constant representing the playing state */
    public static final int PLAYING_GAME = 1;
    /** The constant representing the final menu state */
    public static final int FINAL_MENU = 2;
    /** The state of the game */
    private int gameState;

    /** The imageManager that loads and stores all of the game's images */
    private ImageManager imageManager;
    /** The midiManager that loads and controls all of the midi tracks */
    private MidiManager midiManager;
    /** The soundManager that loads and controls the sounds */
    private SoundManager soundManager;
    /** The game camera that calculates the drawing offsets */
    private GameCamera gameCamera;
    /** The player */
    private Player player;

    /** Look up table of id's read from the levels config to block definitions */
    private HashMap<Integer, Block> blocks;
    /** The raw map level data read from the levels config */
    private ArrayList<int[][]> levelMaps;
    /** The name's of songs to be played at each level */
    private String[] levelSongs;

    /** The enemies present in the current level */
    private Enemy[] enemies;
    /** The event blocks present in the current level */
    private EventBlock[] eventBlocks;
    /** The ribbons draw in the background of the current level */
    private Ribbon[] ribbons;

    /** The current number of enemies */
    private int numEnemies;
    /** The current number of event blocks */
    private int numEventBlocks;
    /** The current number of ribbons */
    private int numRibbons;
    /** The current level */
    private int currentLevel;
    /** The total number of carrots in the entire game */
    private int totalNumCarrots;
    /** The number of carrots collected this game */
    private int numCarrotsCollected;
    /** The decimal place values of the number of carrots */
    private int[] numCarrotsValues;
    /** The number of carrots collected this game */
    private int numEnemiesDefeated;
    /** The decimal place values of the number of enemies */
    private int[] numEnemiesValues;
    /** The number of player lives remaining */
    private int numPlayerLives;

    /** Flag specifying if the golden carrot was found */
    private boolean goldenCarrotFound;
    /** Flag specifying the easter egg has been triggered */
    private boolean easterEggActivated;
    /** The number of game cycles waited activating the easter egg */
    private int gameCyclesWaitedForEasterEgg;


    /**
     * Create a game.
     * @param levelsFilePath The path to the LevelsConfig.txt file.
     * @param playerInputComponent The inputComponent that processes the user's inputs.
     */
    public Game(String levelsFilePath, InputComponent playerInputComponent)
    {
        //Load the maps from the file path
        levelMaps = loadGameLevels(levelsFilePath);
        currentLevel = 0;

        //Define global game data
        blocks = new HashMap<>();
        imageManager = new ImageManager("ImagesConfig.txt");
        midiManager = new MidiManager("MidiConfig.txt");
        soundManager = new SoundManager("SoundsConfig.txt");
        gameCamera = new GameCamera(Block.BLOCK_WIDTH * levelMaps.get(currentLevel).length,
                                    Block.BLOCK_HEIGHT * levelMaps.get(currentLevel)[0].length);

        //Set the state to the main menu
        gameState = MAIN_MENU;
        totalNumCarrots = 0;

        //Enable the sound and music
        levelSongs = new String[]{ "takemehomecountryroads", "amarillobymorning", "dancinginthedark", "eyeswithoutaface", "takeonme" };
        gameCyclesWaitedForEasterEgg = 0;

        //Setup mouse event monitoring
        playerInputComponent.setMouseWatcher(this);
    }

    /**
     * Initialize the current level.
     * @param playerInputComponent The inputComponent that processes the user's inputs.
     */
    private void initializeLevel(InputComponent playerInputComponent)
    {
        //Create/clear the enemies, eventBlocks, and ribbons
        enemies = new Enemy[MAX_ENEMIES];
        eventBlocks = new EventBlock[MAX_EVENT_BLOCKS];
        ribbons = new Ribbon[MAX_RIBBONS];
        numEnemies = 0;
        numEventBlocks = 0;
        numRibbons = 0;

        //Setup level data according to the current level
        HashMap<Integer, HashMap<Integer, Animation>> graphicsMap = new HashMap<>();
        if (currentLevel == 0)
        {
            //Player
            graphicsMap.put(1, new HashMap<>());
            graphicsMap.get(1).put(Entity.IDLE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Pig Left"), 0, false));
            graphicsMap.get(1).put(Entity.IDLE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Pig Right"), 0, false));
            graphicsMap.get(1).put(Entity.MOVE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Pig Walk Left"), 500, true));
            graphicsMap.get(1).put(Entity.MOVE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Pig Walk Right"), 500, true));
            graphicsMap.get(1).put(Entity.MIDAIR_LEFT_GRAPHICS,  new Animation(imageManager.getImages("Pig Left"), 0, false));
            graphicsMap.get(1).put(Entity.MIDAIR_RIGHT_GRAPHICS, new Animation(imageManager.getImages("Pig Right"), 0, false));
            graphicsMap.get(1).put(Entity.DYING_LEFT_GRAPHICS,   new Animation(imageManager.getImages("Smoke Puff"), 500, false));
            graphicsMap.get(1).put(Entity.DYING_RIGHT_GRAPHICS,  new Animation(imageManager.getImages("Smoke Puff"), 500, false));

            //Set the number of carrots collected and enemies defeated
            numCarrotsCollected = 0;
            numCarrotsValues = new int[3]; //Hundreds -> ones places
            numEnemiesDefeated = 0;
            numEnemiesValues = new int[3];

            //Set the number of lives
            numPlayerLives = 3;

            //Set the goldenCarrotFound flag
            goldenCarrotFound = false;
        }
        else
        {
            //Update the gameCamera's map dimensions
            gameCamera.resetCamera();
            gameCamera.setMapDimensions(Block.BLOCK_WIDTH * levelMaps.get(currentLevel).length,
                                        Block.BLOCK_HEIGHT * levelMaps.get(currentLevel)[0].length);
        }
        //Define common elements that are used in every level
        //Define grass terrain blocks
        String[] blockNames = {"Grass Block", "Grass Block Column", "Grass Block Column Merge", "Grass Block Column Base", "Grass Block Column Left Merge",
                "Grass Block Column Right Merge", "Grass Block Column Top", "Grass Block Ground", "Grass Block Left Edge", "Grass Block Left Edge Merge",
                "Grass Block Left Merge", "Grass Block Left Side", "Grass Block Right Edge", "Grass Block Right Edge Merge", "Grass Block Right Merge",
                "Grass Block Right Side", "Grass Block Cavern Ceiling", "Grass Block Cavern Ceiling Column", "Grass Block Cavern Lower Left",
                "Grass Block Cavern Lower Right", "Grass Block Spike Base", "Grass Block Double Merge", "Grass Block Left Side Merge",
                "Grass Block Right Side Merge", "Grass Block Column Top Merge", "Grass Block Cavern Left", "Grass Block Cavern Right", "Grass Block Cavern",
                "Grass Block Cavern Left Merge", "Grass Block Cavern Right Merge"};

        //Turtle
        graphicsMap.put(32, new HashMap<>());
        graphicsMap.get(32).put(Entity.IDLE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Turtle Left"), 0, false));
        graphicsMap.get(32).put(Entity.IDLE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Turtle Right"), 0, false));
        graphicsMap.get(32).put(Entity.MOVE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Turtle Walk Left"), 700, true));
        graphicsMap.get(32).put(Entity.MOVE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Turtle Walk Right"), 700, true));
        graphicsMap.get(32).put(Entity.MIDAIR_LEFT_GRAPHICS,  new Animation(imageManager.getImages("Turtle Left"), 0, false));
        graphicsMap.get(32).put(Entity.MIDAIR_RIGHT_GRAPHICS, new Animation(imageManager.getImages("Turtle Right"), 0, false));
        graphicsMap.get(32).put(Entity.DYING_LEFT_GRAPHICS,   new Animation(imageManager.getImages("Smoke Puff"), 500, false));
        graphicsMap.get(32).put(Entity.DYING_RIGHT_GRAPHICS,  new Animation(imageManager.getImages("Smoke Puff"), 500, false));

        //Level transition event block
        graphicsMap.put(33, new HashMap<>());
        graphicsMap.get(33).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Level Complete Sign"), 0, false));

        //Grass Spike Base event block
        graphicsMap.put(34, new HashMap<>());
        graphicsMap.get(34).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Grass Block Spike Top"), 0, false));

        //Carrot event block
        graphicsMap.put(35, new HashMap<>());
        graphicsMap.get(35).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Carrot"), 1200, true));

        //Golden Carrot event block
        graphicsMap.put(36, new HashMap<>());
        graphicsMap.get(36).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Golden Carrot"), 1200, true));

        //Boar
        graphicsMap.put(37, new HashMap<>());
        graphicsMap.get(37).put(Entity.IDLE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Boar Left"), 0, false));
        graphicsMap.get(37).put(Entity.IDLE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Boar Right"), 0, false));
        graphicsMap.get(37).put(Entity.MOVE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Boar Walk Left"), 500, true));
        graphicsMap.get(37).put(Entity.MOVE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Boar Walk Right"), 500, true));
        graphicsMap.get(37).put(Entity.MIDAIR_LEFT_GRAPHICS,  new Animation(imageManager.getImages("Boar Left"), 0, false));
        graphicsMap.get(37).put(Entity.MIDAIR_RIGHT_GRAPHICS, new Animation(imageManager.getImages("Boar Right"), 0, false));
        graphicsMap.get(37).put(Entity.DYING_LEFT_GRAPHICS,   new Animation(imageManager.getImages("Smoke Puff"), 500, false));
        graphicsMap.get(37).put(Entity.DYING_RIGHT_GRAPHICS,  new Animation(imageManager.getImages("Smoke Puff"), 500, false));

        //Chicken
        graphicsMap.put(38, new HashMap<>());
        graphicsMap.get(38).put(Entity.IDLE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Chicken Left"), 0, false));
        graphicsMap.get(38).put(Entity.IDLE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Chicken Right"), 0, false));
        graphicsMap.get(38).put(Entity.MOVE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Chicken Walk Left"), 600, true));
        graphicsMap.get(38).put(Entity.MOVE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Chicken Walk Right"), 600, true));
        graphicsMap.get(38).put(Entity.MIDAIR_LEFT_GRAPHICS,  new Animation(imageManager.getImages("Chicken Left"), 0, false));
        graphicsMap.get(38).put(Entity.MIDAIR_RIGHT_GRAPHICS, new Animation(imageManager.getImages("Chicken Right"), 0, false));
        graphicsMap.get(38).put(Entity.DYING_LEFT_GRAPHICS,   new Animation(imageManager.getImages("Smoke Puff"), 500, false));
        graphicsMap.get(38).put(Entity.DYING_RIGHT_GRAPHICS,  new Animation(imageManager.getImages("Smoke Puff"), 500, false));

        //Purple Boar
        graphicsMap.put(39, new HashMap<>());
        graphicsMap.get(39).put(Entity.IDLE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Purple Boar Left"), 0, false));
        graphicsMap.get(39).put(Entity.IDLE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Purple Boar Left"), 0, false));
        graphicsMap.get(39).put(Entity.MOVE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Purple Boar Left"), 0, false));
        graphicsMap.get(39).put(Entity.MOVE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Purple Boar Left"), 0, false));
        graphicsMap.get(39).put(Entity.MIDAIR_LEFT_GRAPHICS,  new Animation(imageManager.getImages("Purple Boar Left"), 0, false));
        graphicsMap.get(39).put(Entity.MIDAIR_RIGHT_GRAPHICS, new Animation(imageManager.getImages("Purple Boar Left"), 0, false));
        graphicsMap.get(39).put(Entity.DYING_LEFT_GRAPHICS,   new Animation(imageManager.getImages("Smoke Puff"), 500, false));
        graphicsMap.get(39).put(Entity.DYING_RIGHT_GRAPHICS,  new Animation(imageManager.getImages("Smoke Puff"), 500, false));

        //Purple Carrot event block
        graphicsMap.put(40, new HashMap<>());
        graphicsMap.get(40).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Purple Carrot"), 1200, true));

        //Define the ribbon(s)
        addRibbon(new Ribbon(imageManager.getImages("Platformer Ribbon").get(0), Ribbon.SCROLL_STILL, 2));

        //Initialize the player, enemies and blocks from the map data
        for (int x = 0; x < levelMaps.get(currentLevel).length; x++)
        {
            for (int y = 0; y < levelMaps.get(currentLevel)[0].length; y++)
            {
                //Define game objects according to their ids
                int mappedId = levelMaps.get(currentLevel)[x][y] % 100;
                switch (mappedId)
                {
                    //Air Block
                    case 0:
                        break;
                    //Player
                    case 1:
                        if (player == null)
                        {
                            player = new Player(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, 5,
                                    Entity.IDLE_RIGHT_GRAPHICS, graphicsMap.get(mappedId), playerInputComponent, soundManager);
                            player.setLevelWatcher(this);
                        }
                        else //The player has already been defined, change position to start point
                        {
                            player.setSpawnPosition(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT);
                        }
                        break;
                    //Grass Terrain
                    case 2: //Grass Block
                    case 3: //Grass Block Column
                    case 4: //Grass Block Column Merge
                    case 5: //Grass Block Column Base
                    case 6: //Grass Column Left Merge
                    case 7: //Grass Column Right Merge
                    case 8: //Grass Column Top
                    case 9: //Grass Block Ground
                    case 10: //Grass Block Left Edge
                    case 11: //Grass Block Left Edge Merge
                    case 12: //Grass Block Left Merge
                    case 13: //Grass Block Left Side
                    case 14: //Grass Block Right Edge
                    case 15: //Grass Block Right Edge Merge
                    case 16: //Grass Block Right Merge
                    case 17: //Grass Block Right Side
                    case 18: //Grass Block Cavern Ceiling
                    case 19: //Grass Block Cavern Ceiling Column
                    case 20: //Grass Block Cavern Lower Left
                    case 21: //Grass Block Cavern Lower Right
                    case 22: //Grass Block Spike Base
                    case 23: //Grass Block Double Merge
                    case 24: //Grass Block Left Side Merge
                    case 25: //Grass Block Right Side Merge
                    case 26: //Grass Block Column Top Merge
                    case 27: //Grass Block Cavern Left
                    case 28: //Grass Block Cavern Right
                    case 29: //Grass Block Cavern
                    case 30: //Grass Block Cavern Left Merge
                    case 31: //Grass Block Cavern Right Merge
                        if (!blocks.containsKey(mappedId))
                        {
                            if (!graphicsMap.containsKey(mappedId))
                            {
                                graphicsMap.put(mappedId, new HashMap<>());
                                graphicsMap.get(mappedId).put(Block.NORMAL_GRAPHICS, new Animation(imageManager.getImages(blockNames[mappedId - 2]), 0 ,false));
                            }

                            blocks.put(mappedId, new Block(graphicsMap.get(mappedId)));
                        }
                        break;
                    case 32: //Turtle
                        addEnemy(new Enemy(x * Block.BLOCK_WIDTH,
                                y * Block.BLOCK_HEIGHT + (Block.BLOCK_HEIGHT - graphicsMap.get(mappedId).get(Entity.IDLE_LEFT_GRAPHICS).getImageHeight()),
                                1, Enemy.LEFT, graphicsMap.get(mappedId)));
                        break;
                    case 33: //Level Complete Sign
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_LEVEL, graphicsMap.get(mappedId)));
                        break;
                    case 34: //Grass Block Spike Top
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_DANGER, graphicsMap.get(mappedId)));
                        break;
                    case 35: //Carrot
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_COLLECT, graphicsMap.get(mappedId)));
                        totalNumCarrots++;
                        break;
                    case 36: //Golden Carrot
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_LEVEL, graphicsMap.get(mappedId)));
                        totalNumCarrots++;
                        break;
                    case 37: //Boar
                        addEnemy(new Enemy(x * Block.BLOCK_WIDTH,
                                y * Block.BLOCK_HEIGHT + (Block.BLOCK_HEIGHT - graphicsMap.get(mappedId).get(Entity.IDLE_LEFT_GRAPHICS).getImageHeight()),
                                3, Enemy.LEFT, graphicsMap.get(mappedId)));
                        break;
                    case 38: //Chicken
                        addEnemy(new Enemy(x * Block.BLOCK_WIDTH,
                                y * Block.BLOCK_HEIGHT + (Block.BLOCK_HEIGHT - graphicsMap.get(mappedId).get(Entity.IDLE_LEFT_GRAPHICS).getImageHeight()),
                                2, Enemy.LEFT, graphicsMap.get(mappedId)));
                        break;
                    case 39: //Purple Boar
                        if ( addEnemy(new Enemy(x * Block.BLOCK_WIDTH,
                                y * Block.BLOCK_HEIGHT + (Block.BLOCK_HEIGHT - graphicsMap.get(mappedId).get(Entity.IDLE_LEFT_GRAPHICS).getImageHeight()),
                                0, Enemy.STILL, graphicsMap.get(mappedId))))
                        {
                            //Add the level watcher so that the purple boar's death can trigger special events
                            enemies[numEnemies - 1].setLevelWatcher(this);
                        }
                        break;
                    case 40: //Purple Carrot
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_SPECIAL_INACTIVE, graphicsMap.get(mappedId)));
                        totalNumCarrots++;
                        break;
                    default: //Default
                        System.out.println("No definition found for id = " + mappedId);
                        break;
                }
            }
        }
    }

    /**
     * Update the game. This method drives the updates to the player, enemies, everything.
     * @param loopPeriodInNanos The loop period of the game loop in nanoseconds.
     */
    public void update(long loopPeriodInNanos)
    {
        //Update the game according to the gameState
        if (gameState == PLAYING_GAME)
        {
            //Update the player and use its new location to update the game camera
            Point playerLocation = player.update(levelMaps.get(currentLevel), enemies, numEnemies, eventBlocks, numEventBlocks,
                    ribbons, numRibbons,loopPeriodInNanos / 1000000);
            gameCamera.update(playerLocation);

            //Update the enemies, blocks, and ribbons
            for (Map.Entry<Integer, Block> entry : blocks.entrySet())
            {
                entry.getValue().update(loopPeriodInNanos / 1000000);
            }
            for (int i = 0; i < numEnemies; i++)
            {
                enemies[i].update(levelMaps.get(currentLevel), eventBlocks, numEventBlocks,loopPeriodInNanos / 1000000);
            }
            for (int i = 0; i < numEventBlocks; i++)
            {
                eventBlocks[i].update(loopPeriodInNanos / 1000000);
            }
            for (int i = 0; i < numRibbons; i++)
            {
                ribbons[i].update();
            }

            //If the easter egg has been activated wait before playing the sounds/music.
            //This prevents the weird track ending MIDI sounds from playing over it.
            if (easterEggActivated)
            {
                //Increment the wait counter, play after ~2 seconds
                gameCyclesWaitedForEasterEgg++;
                if (gameCyclesWaitedForEasterEgg >= 2 * Launcher.ONE_SECOND)
                {
                    soundManager.playSound("GameBlouses", false);
                    midiManager.play("purplerain", true);
                    easterEggActivated = false;
                }
            }
        }
    }

    /**
     * Draw the game. This method draws all of the components in the game depending on the
     * current game state.
     * @param dbGraphics The graphics object used to draw the game.
     */
    public void draw(Graphics dbGraphics)
    {
        //Draw the game according to the gameState
        if (gameState == MAIN_MENU)
        {
            //Draw the main menu image
            dbGraphics.drawImage(imageManager.getImages("Main Menu").get(0), 0, 0, null);
        }
        else if (gameState == PLAYING_GAME)
        {
            gameCamera.draw(dbGraphics, levelMaps.get(currentLevel), blocks, enemies, numEnemies, player, eventBlocks, numEventBlocks,
                    ribbons, numRibbons);

            //Draw the game header
            dbGraphics.drawImage(imageManager.getImages("Platformer Header").get(0), 0, 0, null);

            //Draw the number of carrots collected
            int carrotOffset = 112; //x position to draw first digit
            for (int i = 2; i >= 0; i--)
            {
                //If i is zero, always draw. Otherwise, make sure we don't draw leading zeros
                if (i == 0 || (numCarrotsValues[i] != 0 || (i != 2 && numCarrotsValues[i + 1] != 0)))
                {
                    dbGraphics.drawImage(imageManager.getImages("Numbers").get(numCarrotsValues[i]), carrotOffset, 0, null);
                    carrotOffset += 9;
                }
            }

            //Draw the player's lives onto the header
            int livesOffset = 490; //x position to draw first pig life icon
            for (int i = 0; i < numPlayerLives; i++)
            {
                dbGraphics.drawImage(imageManager.getImages("Pig Life Icon").get(0), livesOffset, 0, null);
                livesOffset += 22;
            }
        }
        else if (gameState == FINAL_MENU)
        {
            //Draw the main menu image
            dbGraphics.drawImage(imageManager.getImages("Final Menu").get(0), 0, 0, null);

            //Draw the Golden Carrot not found screen if the player lost
            if (!goldenCarrotFound)
            {
                dbGraphics.drawImage(imageManager.getImages("Menu Final Tablet Golden Carrot Lose").get(0), 150, 270, null);
            }

            //Draw the number of carrots collected
            int finalMenuOffset = 374; //x position to draw first digit
            for (int i = 2; i >= 0; i--)
            {
                //If i is zero, always draw. Otherwise, make sure we don't draw leading zeros
                if (i == 0 || (numCarrotsValues[i] != 0 || (i != 2 && numCarrotsValues[i + 1] != 0)))
                {
                    dbGraphics.drawImage(imageManager.getImages("Numbers").get(numCarrotsValues[i]), finalMenuOffset, 380, null);
                    finalMenuOffset += 9;
                }
            }

            //Draw the number of enemies defeated
            finalMenuOffset = 374; //x position to draw first digit
            for (int i = 2; i >= 0; i--)
            {
                //If i is zero, always draw. Otherwise, make sure we don't draw leading zeros
                if (i == 0 || (numEnemiesValues[i] != 0 || (i != 2 && numEnemiesValues[i + 1] != 0)))
                {
                    dbGraphics.drawImage(imageManager.getImages("Numbers").get(numEnemiesValues[i]), finalMenuOffset, 470, null);
                    finalMenuOffset += 9;
                }
            }
        }

        //Draw the sound and music disabled symbols if necessary
        if (!midiManager.isMusicEnabled())
        {
            dbGraphics.drawImage(imageManager.getImages("Music Symbol Disabled").get(0),
                    GamePanel.WIDTH - imageManager.getImages("Music Symbol Disabled").get(0).getWidth(), 0, null);
        }
        if (!soundManager.isSoundEnabled()) //Add two to the sound disabled symbol so that it overlaps with the music symbol correctly
        {
            dbGraphics.drawImage(imageManager.getImages("Sound Symbol Disabled").get(0),
                    GamePanel.WIDTH - (imageManager.getImages("Sound Symbol Disabled").get(0).getWidth() * 2) + 2, 0, null);
        }
    }

    /**
     * Process mouse input according to the game's current state.
     * @param mousePosition The point on the screen that the mouse was clicked.
     * @param playerInputComponent The inputComponent that processes the user's inputs.
     */
    @Override
    public void mouseClicked(Point mousePosition, InputComponent playerInputComponent)
    {
        //Determine actions based on game state
        if (gameState == MAIN_MENU || gameState == FINAL_MENU)
        {
            //Start/Restart button (coordinates from GIMP)
            if (new Rectangle(218, 549, 64, 42).contains(mousePosition))
            {
                initializeLevel(playerInputComponent);
                midiManager.play(levelSongs[currentLevel], true);
                gameState = PLAYING_GAME;
            }

            //Quit button
            if (new Rectangle(318, 549, 64, 42).contains(mousePosition))
            {
                System.exit(0);
            }
        }

        //Sound button
        if (new Rectangle(563, 0, 18, 19).contains(mousePosition))
        {
            soundManager.enableSound(!soundManager.isSoundEnabled());
        }

        //Music button
        if (new Rectangle(581, 0, 18, 19).contains(mousePosition))
        {
            midiManager.enableMusic(!midiManager.isMusicEnabled());
        }
    }

    /**
     * When an easter egg is activated by the player, this method will be called.
     * It sets a flag specifying the easter egg has been triggered, but the special
     * sounds/music will not actually play for ~2 seconds. This prevents the trailing
     * level's music from interrupting the easter egg.
     */
    @Override
    public void activateEasterEgg()
    {
        midiManager.pause();
        easterEggActivated = true;
    }

    /**
     * Change the scrolling direction of the background ribbons. The player drives this action
     * since it is based on its movement.
     * @param newScrollDirection The new direction the ribbon should scroll.
     */
    @Override
    public void changeRibbonScrollDirection(int newScrollDirection)
    {
        //Set the ribbon scroll direction
        for (int i = 0; i < numRibbons; i++)
        {
            ribbons[i].setScrollDirection(newScrollDirection);
        }
    }

    /**
     * Change to the next level. If the next level does not exist, then
     * the game ends.
     * @param playerInputComponent The inputComponent that processes the user's inputs.
     */
    @Override
    public void changeToNextLevel(InputComponent playerInputComponent)
    {
        //Increment the level number and check if the game has ended
        currentLevel++;
        if (currentLevel < levelMaps.size())
        {
            //Player is not re-initialized, so an input component is not needed
            initializeLevel(null);
            midiManager.play(levelSongs[currentLevel], true);
        }
        else
        {
            //The Golden Carrot was found
            goldenCarrotFound = true;
            numCarrotsCollected++;
            gameOver();
        }
    }

    /**
     * Increment the number of carrots collected. This method also calculates
     * the decimal place values of the numCarrotsCollected so that they can be
     * printed nicely on the final menu.
     */
    @Override
    public void itemCollected()
    {
        //Increment the number of carrots collected
        numCarrotsCollected++;

        if (numCarrotsCollected < 1000)
        {
            //Determine the values in the hundreds, tens, and ones places
            numCarrotsValues[2] = numCarrotsCollected/100 % 10; //Hundreds
            numCarrotsValues[1] = numCarrotsCollected/10 % 10; //Tens
            numCarrotsValues[0] = numCarrotsCollected % 10; //Ones
        }
        else //We've got too many carrots
        {
            numCarrotsValues[2] = 9; //Hundreds
            numCarrotsValues[1] = 9; //Tens
            numCarrotsValues[0] = 9; //Ones
        }
    }

    /**
     * Increment the number of enemies defeated. This method also calculates
     * the decimal place values of the numEnemiesDefeated so that they can be
     * printed nicely on the final menu.
     */
    @Override
    public void enemyDefeated()
    {
        //Increment the number of enemies defeated
        numEnemiesDefeated++;

        if (numEnemiesDefeated < 1000)
        {
            //Determine the values in the hundreds, tens, and ones places
            numEnemiesValues[2] = numEnemiesDefeated/100 % 10; //Hundreds
            numEnemiesValues[1] = numEnemiesDefeated/10 % 10; //Tens
            numEnemiesValues[0] = numEnemiesDefeated % 10; //Ones
        }
        else //We've are a beast
        {
            numEnemiesValues[2] = 9; //Hundreds
            numEnemiesValues[1] = 9; //Tens
            numEnemiesValues[0] = 9; //Ones
        }
    }

    /**
     * Decrement the number of player lives. If the player
     * is out of lives, then the game ends.
     */
    @Override
    public void playerHasDied()
    {
        //Subtract a life
        numPlayerLives--;

        //If the player has lost all its lives, game over
        if (numPlayerLives < 0)
        {
            gameOver();
        }
        else
        {
            //Reset the enemies and ribbons
            for (int i = 0; i < numEnemies; i++)
            {
                enemies[i].reset();
            }
            for (int i = 0; i < numRibbons; i++)
            {
                ribbons[i].reset();
            }
        }
    }

    /**
     * Handle the death of special enemies. This method is only used to trigger
     * special block events in the game.
     */
    @Override
    public void specialEnemyDied()
    {
        for (int i = 0; i < numEventBlocks; i++)
        {
            if (eventBlocks[i].getBlockType() == EventBlock.BLOCK_SPECIAL_INACTIVE)
            {
                eventBlocks[i].setBlockType(EventBlock.BLOCK_SPECIAL_COLLECT);
            }
        }
    }

    /**
     * End the game and transition the state to the final menu.
     */
    public void gameOver()
    {
        //Set the state to the final menu
        midiManager.pause();
        currentLevel = 0;
        gameState = FINAL_MENU;

        //Reward the players that found all the carrots
        if (numCarrotsCollected == totalNumCarrots)
        {
            soundManager.playSound("CarrotCongratulations",false);
        }
    }

    /**
     * Load the game levels from the specified filePath.
     * @param filePath The path to the levels config file.
     * @return An arrayList containing the raw data for each level.
     */
    private ArrayList<int[][]> loadGameLevels(String filePath)
    {
        //An ArrayList of the loaded game levelManagers
        ArrayList<int[][]> loadedLevels = new ArrayList<>();

        //Output file reading to the console
        System.out.println("Reading levelManagers from file: " + filePath);
        try
        {
            //Create an InputStream and BufferedReader to read the file
            InputStream inputStream = this.getClass().getResourceAsStream(filePath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            //Read the file until EOF
            String line;
            int numLinesRead = 0;
            while ((line = bufferedReader.readLine()) != null)
            {
                //Allow comments and blank lines
                if (line.startsWith("//") || line.length() == 0)
                {
                    continue;
                }
                else if (line.startsWith("["))
                {
                    //Read map dimensions
                    int mapWidth = Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(',')).trim());
                    int mapHeight = Integer.parseInt(line.substring(line.indexOf(',') + 1, line.indexOf(']')).trim());
                    loadedLevels.add(new int[mapWidth][mapHeight]);
                    numLinesRead = 0;
                }
                else
                {
                    //Read map data
                    String lineData[] = line.split(",");
                    for (int i = 0; i < lineData.length; i++)
                    {
                        loadedLevels.get(loadedLevels.size() - 1)[i][numLinesRead] = Integer.parseInt(lineData[i].trim());
                    }
                    numLinesRead++;
                }
            }

            System.out.println("Stored " + loadedLevels.size() + " level(s)");

            //Close the BufferedReader and output file read complete
            bufferedReader.close();
            System.out.println("Completed reading file: " + filePath);

            //Return
            return loadedLevels;
        }
        catch (IOException exception)
        {
            System.out.println("Error reading file: " + filePath);
            exception.printStackTrace();
        }
        catch (NumberFormatException exception)
        {
            System.out.println("Format error reading file: " + filePath);
            exception.printStackTrace();
        }
        catch (IndexOutOfBoundsException exception)
        {
            System.out.println("Index error reading file: " + filePath);
            exception.printStackTrace();
        }
        catch (NullPointerException exception)
        {
            System.out.println("Null error reading file: " + filePath);
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Add a new enemy to the enemies array. If the array is
     * full then this method returns false. Otherwise, the enemy
     * is added and it returns true.
     * @param enemyToAdd The new enemy.
     * @return True if the enemy is added, false otherwise.
     */
    private boolean addEnemy(Enemy enemyToAdd)
    {
        //Add the new enemy if possible
        if (numEnemies < MAX_ENEMIES)
        {
            enemies[numEnemies] = enemyToAdd;
            numEnemies++;
            return true;
        }
        return false;
    }

    /**
     * Add a new event block to the eventBlocks array. If the array is
     * full then this method returns false. Otherwise, the block
     * is added and it returns true.
     * @param blockToAdd The new event block.
     * @return True if the block is added, false otherwise.
     */
    private boolean addEventBlock(EventBlock blockToAdd)
    {
        //Add the new event block if possible
        if (numEventBlocks < MAX_EVENT_BLOCKS)
        {
            eventBlocks[numEventBlocks] = blockToAdd;
            numEventBlocks++;
            return true;
        }
        return false;
    }

    /**
     * Add a new ribbon to the ribbons array. If the array is
     * full then this method returns false. Otherwise, the ribbon
     * is added and it returns true.
     * @param ribbonToAdd The new event block.
     * @return True if the ribbon is added, false otherwise.
     */
    private boolean addRibbon(Ribbon ribbonToAdd)
    {
        //Add the new ribbon if possible
        if (numRibbons < MAX_RIBBONS)
        {
            ribbons[numRibbons] = ribbonToAdd;
            numRibbons++;
            return true;
        }
        return false;
    }

    /** This method is not used and does nothing */
    public void mouseClicked(MouseEvent e) { /* Do nothing */ }

    /** This method is not used and does nothing */
    public void mouseEntered(MouseEvent event) { /* Do nothing */ }

    /** This method is not used and does nothing */
    public void mouseExited(MouseEvent event) { /* Do nothing */ }

    /** This method is not used and does nothing */
    public void mousePressed(MouseEvent event) { /* Do nothing */ }

    /** This method is not used and does nothing */
    public void mouseReleased(MouseEvent event) { /* Do nothing */ }
}
