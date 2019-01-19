import java.awt.*;
import java.util.HashMap;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class GameCamera
{
    /** The rectangle view that the camera is displaying on the screen */
    private Rectangle cameraView;
    /** The dimensions of the current level in pixels, from the first block to the last */
    private Point mapDimensions;

    /**
     * Create a new game camera.
     * @param mapWidthInPixels The width of the map in pixels.
     * @param maxHeightInPixels The height of the map in pixels.
     */
    public GameCamera(int mapWidthInPixels, int maxHeightInPixels)
    {
        //Set up the camera view
        cameraView = new Rectangle(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        mapDimensions = new Point(mapWidthInPixels, maxHeightInPixels);
    }

    /**
     * Update the game camera using the player's location. This method re-calculates the
     * offsets used to position the player in the center of the screen.
     * @param playerLocation The position of the player relative to the map.
     */
    public void update(Point playerLocation)
    {
        //Set camera offsets to zero if the player is to the left of the middle of the screen
        //Or if the width of the map is less than or equal to the width of the screen
        if (playerLocation.x < (cameraView.width/2) || mapDimensions.x <= cameraView.width)
        {
            cameraView.x = 0;
        }
        else if (playerLocation.x + (cameraView.width/2) >= mapDimensions.x)
        {
            //If the player is at the far right side of the map, un-center them
            cameraView.x = mapDimensions.x - cameraView.width;
        }
        else if (playerLocation.x >= (cameraView.width/2))
        {
            //If the player has passed the center of the screen, center them
            cameraView.x = playerLocation.x - (cameraView.width/2);
        }

        //Set camera offsets to zero if the player is below the middle of the screen
        //Or if the height of the map is less than or equal to the height of the screen
        if (playerLocation.y < (cameraView.height/2) || mapDimensions.y <= cameraView.height)
        {
            cameraView.y = 0;
        }
        else if (playerLocation.y + (cameraView.height/2) >= mapDimensions.y)
        {
            //If the player is along the bottom side of the map, un-center them
            cameraView.y = mapDimensions.y - cameraView.height;
        }
        else if (playerLocation.y >= (cameraView.height/2))
        {
            //If the player has passed the center of the screen, center them
            cameraView.y = playerLocation.y - (cameraView.height/2);
        }

        //Invert the results, the computed values are the pixel difference between the player location
        //and the center of the screen. Inverting the values gives the true offset to add to center the player.
        cameraView.x = -cameraView.x;
        cameraView.y = -cameraView.y;
    }

    /**
     * Draw everything that is contained within the view of the game camera.
     * @param dbGraphics The graphics object used to draw.
     * @param blockIdMap The condensed map of block id's that only contains 1's for solid blocks and 0's for transparent air blocks.
     * @param blocks The map of block id's to actual block graphics.
     * @param enemies The enemies present in the current level.
     * @param numEnemies The number of enemies.
     * @param player The player.
     * @param eventBlocks The event blocks present in the current level.
     * @param numEventBlocks The number of event blocks.
     * @param ribbons The background ribbons.
     * @param numRibbons The number of ribbons.
     */
    public void draw(Graphics dbGraphics, int[][] blockIdMap, HashMap<Integer, Block> blocks,
                     Enemy[] enemies, int numEnemies, Player player, EventBlock[] eventBlocks,
                     int numEventBlocks, Ribbon[] ribbons, int numRibbons)
    {
        //Draw the ribbons
        for (int i = 0; i < numRibbons; i++)
        {
            ribbons[i].draw(dbGraphics);
        }

        //Draw the blockIdMap using the blocks map as a look-up-table
        for (int x = 0; x < blockIdMap.length; x++)
        {
            for (int y = 0; y < blockIdMap[0].length; y++)
            {
                //Only draw the blocks that can be seen on screen, only look at the last two digits for the id
                if (blocks.containsKey(blockIdMap[x][y] % 100)
                        && (x * Block.BLOCK_WIDTH + Block.BLOCK_WIDTH + cameraView.x) > 0
                        && (x * Block.BLOCK_WIDTH + cameraView.x) < mapDimensions.x)
                {
                    blocks.get(blockIdMap[x][y] % 100).draw(dbGraphics,
                            (x * Block.BLOCK_WIDTH) + cameraView.x, (y * Block.BLOCK_HEIGHT) + cameraView.y);
                }
            }
        }

        //Draw the event blocks
        for (int i = 0; i < numEventBlocks; i++)
        {
            eventBlocks[i].draw(dbGraphics, cameraView.x, cameraView.y);
        }

        //Draw the enemies
        for (int i = 0; i < numEnemies; i++)
        {
            enemies[i].draw(dbGraphics, cameraView.x, cameraView.y);
        }

        //Draw the player
        player.draw(dbGraphics, cameraView.x, cameraView.y);
    }

    /**
     * Set the map dimensions. This method should be called
     * every time a new level is loaded. Otherwise the camera
     * will be unable to properly calculate the offsets.
     * @param newMapX The new width of the map in pixels.
     * @param newMapY The new height of the map in pixels.
     */
    public void setMapDimensions(int newMapX, int newMapY)
    {
        mapDimensions.x = newMapX;
        mapDimensions.y = newMapY;
    }

    /**
     * Reset the camera's offsets to zero.
     */
    public void resetCamera()
    {
        cameraView.x = 0;
        cameraView.y = 0;
    }
}
