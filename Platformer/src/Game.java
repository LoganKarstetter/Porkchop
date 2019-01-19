import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Game implements LevelWatcher, MouseWatcher
{
    public static final int MAX_ENEMIES = 15;
    public static final int MAX_EVENT_BLOCKS = 10;
    public static final int MAX_RIBBONS = 3;

    public static final int MAIN_MENU = 0;
    public static final int PLAYING_GAME = 1;
    public static final int FINAL_MENU = 2;
    private int gameState;

    private ImageManager imageManager;
    private MidiManager midiManager;
    private SoundManager soundManager;
    private GameCamera gameCamera;
    private Player player;

    private HashMap<Integer, Block> blocks;
    private ArrayList<int[][]> levelMaps;

    private Enemy[] enemies;
    private EventBlock[] eventBlocks;
    private Ribbon[] ribbons;

    private int numEnemies;
    private int numEventBlocks;
    private int numRibbons;
    private int currentLevel;
    private int numCarrotsCollected;
    private int[] numCarrotsValues;
    private int numEnemiesDefeated;
    private int[] numEnemiesValues;
    private int numPlayerLives;
    private boolean goldenCarrotFound;
    private boolean soundDisabled;
    private boolean musicDisabled;


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

        //Enable the sound and music
        soundDisabled = false;
        musicDisabled = false;

        //Setup mouse event monitoring
        playerInputComponent.setMouseWatcher(this);

        //Initialize the first level
        initializeLevel(playerInputComponent);
    }

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
        else if (currentLevel == 1)
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
                "Grass Block Cavern Lower Right", "Grass Block Spike Base"};

        //Turtle
        graphicsMap.put(23, new HashMap<>());
        graphicsMap.get(23).put(Entity.IDLE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Turtle Left"), 0, false));
        graphicsMap.get(23).put(Entity.IDLE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Turtle Right"), 0, false));
        graphicsMap.get(23).put(Entity.MOVE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Turtle Walk Left"), 700, true));
        graphicsMap.get(23).put(Entity.MOVE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Turtle Walk Right"), 700, true));
        graphicsMap.get(23).put(Entity.MIDAIR_LEFT_GRAPHICS,  new Animation(imageManager.getImages("Turtle Left"), 0, false));
        graphicsMap.get(23).put(Entity.MIDAIR_RIGHT_GRAPHICS, new Animation(imageManager.getImages("Turtle Right"), 0, false));
        graphicsMap.get(23).put(Entity.DYING_LEFT_GRAPHICS,   new Animation(imageManager.getImages("Smoke Puff"), 500, false));
        graphicsMap.get(23).put(Entity.DYING_RIGHT_GRAPHICS,  new Animation(imageManager.getImages("Smoke Puff"), 500, false));

        //Level transition event block
        graphicsMap.put(24, new HashMap<>());
        graphicsMap.get(24).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Level Complete Sign"), 0, false));

        //Grass Spike Base event block
        graphicsMap.put(25, new HashMap<>());
        graphicsMap.get(25).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Grass Block Spike Top"), 0, false));

        //Carrot event block
        graphicsMap.put(26, new HashMap<>());
        graphicsMap.get(26).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Carrot"), 1200, true));

        //Golden Carrot event block
        graphicsMap.put(27, new HashMap<>());
        graphicsMap.get(27).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Golden Carrot"), 1200, true));

        //Boar
        graphicsMap.put(28, new HashMap<>());
        graphicsMap.get(28).put(Entity.IDLE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Boar Left"), 0, false));
        graphicsMap.get(28).put(Entity.IDLE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Boar Right"), 0, false));
        graphicsMap.get(28).put(Entity.MOVE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Boar Walk Left"), 500, true));
        graphicsMap.get(28).put(Entity.MOVE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Boar Walk Right"), 500, true));
        graphicsMap.get(28).put(Entity.MIDAIR_LEFT_GRAPHICS,  new Animation(imageManager.getImages("Boar Left"), 0, false));
        graphicsMap.get(28).put(Entity.MIDAIR_RIGHT_GRAPHICS, new Animation(imageManager.getImages("Boar Right"), 0, false));
        graphicsMap.get(28).put(Entity.DYING_LEFT_GRAPHICS,   new Animation(imageManager.getImages("Smoke Puff"), 500, false));
        graphicsMap.get(28).put(Entity.DYING_RIGHT_GRAPHICS,  new Animation(imageManager.getImages("Smoke Puff"), 500, false));

        //Chicken
        graphicsMap.put(29, new HashMap<>());
        graphicsMap.get(29).put(Entity.IDLE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Chicken Left"), 0, false));
        graphicsMap.get(29).put(Entity.IDLE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Chicken Right"), 0, false));
        graphicsMap.get(29).put(Entity.MOVE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Chicken Walk Left"), 600, true));
        graphicsMap.get(29).put(Entity.MOVE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Chicken Walk Right"), 600, true));
        graphicsMap.get(29).put(Entity.MIDAIR_LEFT_GRAPHICS,  new Animation(imageManager.getImages("Chicken Left"), 0, false));
        graphicsMap.get(29).put(Entity.MIDAIR_RIGHT_GRAPHICS, new Animation(imageManager.getImages("Chicken Right"), 0, false));
        graphicsMap.get(29).put(Entity.DYING_LEFT_GRAPHICS,   new Animation(imageManager.getImages("Smoke Puff"), 500, false));
        graphicsMap.get(29).put(Entity.DYING_RIGHT_GRAPHICS,  new Animation(imageManager.getImages("Smoke Puff"), 500, false));

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
                            graphicsMap.get(mappedId).get(Entity.DYING_LEFT_GRAPHICS).setWatcher(player);
                            graphicsMap.get(mappedId).get(Entity.DYING_RIGHT_GRAPHICS).setWatcher(player);
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
                    case 23: //Turtle
                        if ( addEnemy(new Enemy(x * Block.BLOCK_WIDTH,
                                y * Block.BLOCK_HEIGHT + (Block.BLOCK_HEIGHT - graphicsMap.get(mappedId).get(Entity.IDLE_LEFT_GRAPHICS).getImageHeight()),
                                1, Enemy.LEFT, graphicsMap.get(mappedId))))
                        {
                            //Add the animation watcher if the enemy is successfully added
                            graphicsMap.get(mappedId).get(Entity.DYING_LEFT_GRAPHICS).setWatcher(enemies[numEnemies]);
                            graphicsMap.get(mappedId).get(Entity.DYING_RIGHT_GRAPHICS).setWatcher(enemies[numEnemies]);
                        }
                        break;
                    case 24: //Level Complete Sign
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_LEVEL, graphicsMap.get(mappedId)));
                        break;
                    case 25: //Grass Block Spike Top
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_DANGER, graphicsMap.get(mappedId)));
                        break;
                    case 26: //Carrot
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_COLLECT, graphicsMap.get(mappedId)));
                        break;
                    case 27: //Golden Carrot
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_LEVEL, graphicsMap.get(mappedId)));
                        break;
                    case 28: //Boar
                        if ( addEnemy(new Enemy(x * Block.BLOCK_WIDTH,
                                y * Block.BLOCK_HEIGHT + (Block.BLOCK_HEIGHT - graphicsMap.get(mappedId).get(Entity.IDLE_LEFT_GRAPHICS).getImageHeight()),
                                2, Enemy.LEFT, graphicsMap.get(mappedId))))
                        {
                            //Add the animation watcher if the enemy is successfully added
                            graphicsMap.get(mappedId).get(Entity.DYING_LEFT_GRAPHICS).setWatcher(enemies[numEnemies]);
                            graphicsMap.get(mappedId).get(Entity.DYING_RIGHT_GRAPHICS).setWatcher(enemies[numEnemies]);
                        }
                        break;
                    case 29: //Chicken
                        if ( addEnemy(new Enemy(x * Block.BLOCK_WIDTH,
                                y * Block.BLOCK_HEIGHT + (Block.BLOCK_HEIGHT - graphicsMap.get(mappedId).get(Entity.IDLE_LEFT_GRAPHICS).getImageHeight()),
                                2, Enemy.LEFT, graphicsMap.get(mappedId))))
                        {
                            //Add the animation watcher if the enemy is successfully added
                            graphicsMap.get(mappedId).get(Entity.DYING_LEFT_GRAPHICS).setWatcher(enemies[numEnemies]);
                            graphicsMap.get(mappedId).get(Entity.DYING_RIGHT_GRAPHICS).setWatcher(enemies[numEnemies]);
                        }
                        break;
                    default: //Default
                        System.out.println("No definition found for id = " + mappedId);
                        break;
                }
            }
        }
    }

    @Override
    public void changeToNextLevel(InputComponent playerInputComponent)
    {
        //Increment the level number and check if the game has ended
        currentLevel++;
        if (currentLevel < levelMaps.size())
        {
            //Player is not re-initialized, so an input component is not needed
            initializeLevel(null);
        }
        else
        {
            //The Golden Carrot was found
            goldenCarrotFound = true;
            gameOver();
        }
    }

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
    }

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
            soundDisabled = !soundDisabled;
        }

        //Music button
        if (new Rectangle(581, 0, 18, 19).contains(mousePosition))
        {
            musicDisabled = !musicDisabled;
        }
    }

    public void gameOver()
    {
        //Set the state to the final menu
        currentLevel = 0;
        gameState = FINAL_MENU;
    }

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
        }
    }

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
        if (musicDisabled)
        {
            dbGraphics.drawImage(imageManager.getImages("Music Symbol Disabled").get(0),
                    GamePanel.WIDTH - imageManager.getImages("Music Symbol Disabled").get(0).getWidth(), 0, null);
        }
        if (soundDisabled) //Add two to the sound disabled symbol so that it overlaps with the music symbol correctly
        {
            dbGraphics.drawImage(imageManager.getImages("Sound Symbol Disabled").get(0),
                    GamePanel.WIDTH - (imageManager.getImages("Sound Symbol Disabled").get(0).getWidth() * 2) + 2, 0, null);
        }
    }

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

    public void mouseClicked(MouseEvent e) { /* Do nothing */ }

    public void mouseEntered(MouseEvent event) { /* Do nothing */ }

    public void mouseExited(MouseEvent event) { /* Do nothing */ }

    public void mousePressed(MouseEvent event) { /* Do nothing */ }

    public void mouseReleased(MouseEvent event) { /* Do nothing */ }
}
