import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Game implements LevelWatcher
{
    public static final int MAX_ENEMIES = 15;
    public static final int MAX_EVENT_BLOCKS = 10;
    public static final int MAX_RIBBONS = 3;

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

            //Define grass terrain blocks
            String[] grassBlocks = {"Grass Block", "Grass Block Column", "Grass Block Column Merge", "Grass Block Column Base", "Grass Block Column Left Merge",
                    "Grass Block Column Right Merge", "Grass Block Column Top", "Grass Block Ground", "Grass Block Left Edge", "Grass Block Left Edge Merge",
                    "Grass Block Left Merge", "Grass Block Left Side", "Grass Block Right Edge", "Grass Block Right Edge Merge", "Grass Block Right Merge",
                    "Grass Block Right Side", "Grass Block Cavern Ceiling", "Grass Block Cavern Ceiling Column", "Grass Block Cavern Lower Left",
                    "Grass Block Cavern Lower Right", "Grass Block Spike Base"};

            //Loop and add animations to the HashMap, start at 2g
            for (int i = 2; i < grassBlocks.length + 2; i++)
            {
                graphicsMap.put(i, new HashMap<>());
                graphicsMap.get(i).put(Block.NORMAL_GRAPHICS, new Animation(imageManager.getImages(grassBlocks[i - 2]), 0, false));
            }
        }
        else if (currentLevel == 1)
        {
            //Update the gameCamera's map dimensions
            gameCamera.resetCamera();
            gameCamera.setMapDimensions(Block.BLOCK_WIDTH * levelMaps.get(currentLevel).length,
                                        Block.BLOCK_HEIGHT * levelMaps.get(currentLevel)[0].length);

        }

        //Define common elements that are used in every level
        //Test enemy
        graphicsMap.put(23, new HashMap<>());
        graphicsMap.get(23).put(Entity.IDLE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Serpent Body"), 0, false));
        graphicsMap.get(23).put(Entity.IDLE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Serpent Body"), 0, false));
        graphicsMap.get(23).put(Entity.MOVE_LEFT_GRAPHICS,    new Animation(imageManager.getImages("Serpent Body"), 0, false));
        graphicsMap.get(23).put(Entity.MOVE_RIGHT_GRAPHICS,   new Animation(imageManager.getImages("Serpent Body"), 0, false));
        graphicsMap.get(23).put(Entity.MIDAIR_LEFT_GRAPHICS,  new Animation(imageManager.getImages("Serpent Body"), 0, false));
        graphicsMap.get(23).put(Entity.MIDAIR_RIGHT_GRAPHICS, new Animation(imageManager.getImages("Serpent Body"), 0, false));
        graphicsMap.get(23).put(Entity.DYING_LEFT_GRAPHICS,   new Animation(imageManager.getImages("Serpent Body"), 0, false));
        graphicsMap.get(23).put(Entity.DYING_RIGHT_GRAPHICS,  new Animation(imageManager.getImages("Serpent Body"), 0, false));

        //Level transition event block
        graphicsMap.put(24, new HashMap<>());
        graphicsMap.get(24).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Level Complete Sign"), 0, false));

        //Grass Spike Base event block
        graphicsMap.put(25, new HashMap<>());
        graphicsMap.get(25).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Grass Block Spike Top"), 0, false));

        //Carrot event block
        graphicsMap.put(26, new HashMap<>());
        graphicsMap.get(26).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Carrot"), 0, false));

        //Golden Carrot event block
        graphicsMap.put(27, new HashMap<>());
        graphicsMap.get(27).put(Block.NORMAL_GRAPHICS,  new Animation(imageManager.getImages("Golden Carrot"), 0, false));

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
                            player = new Player(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, 5, Entity.IDLE_RIGHT_GRAPHICS, graphicsMap.get(mappedId), playerInputComponent);
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
                            blocks.put(mappedId, new Block(graphicsMap.get(mappedId)));
                        }
                        break;
                    case 23: //Test Enemy
                        addEnemy(new Enemy(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, 3, Enemy.MOVING_RIGHT, graphicsMap.get(mappedId)));
                        break;
                    case 24: //LevelManager block
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_LEVEL, graphicsMap.get(mappedId)));
                        break;
                    case 25: //Grass Block Spike Top
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_DANGER, graphicsMap.get(mappedId)));
                        break;
                    case 26: //Carrot
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_EMPTY, graphicsMap.get(mappedId)));
                        break;
                    case 27: //Golden Carrot
                        addEventBlock(new EventBlock(x * Block.BLOCK_WIDTH, y * Block.BLOCK_HEIGHT, EventBlock.BLOCK_EMPTY, graphicsMap.get(mappedId)));
                        break;
                    default: //Default
                        System.out.println("No definition found for id = " + mappedId);
                        break;
                }
            }
        }
    }

    public void changeToNextLevel()
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
            //TODO: Game completed
        }
    }

    public void update(long loopPeriodInNanos)
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
            enemies[i].update(levelMaps.get(currentLevel), loopPeriodInNanos / 1000000);
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

    public void draw(Graphics dbGraphics)
    {
        gameCamera.draw(dbGraphics, levelMaps.get(currentLevel), blocks, enemies, numEnemies, player, eventBlocks, numEventBlocks,
                ribbons, numRibbons);

        //Draw the game icons
        dbGraphics.drawImage(imageManager.getImages("Carrot Icon").get(0), 0, 0, null);
        dbGraphics.drawImage(imageManager.getImages("Pig Life Icon").get(0), GamePanel.WIDTH - imageManager.getImages("Pig Life Icon").get(0).getWidth(), 0, null);
        dbGraphics.drawImage(imageManager.getImages("Pig Life Icon").get(0), GamePanel.WIDTH - imageManager.getImages("Pig Life Icon").get(0).getWidth() * 2, 0, null);
        dbGraphics.drawImage(imageManager.getImages("Pig Life Icon").get(0), GamePanel.WIDTH - imageManager.getImages("Pig Life Icon").get(0).getWidth() * 3, 0, null);
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
}
