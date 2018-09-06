import java.awt.*;

/**
 * @author Logan Karstetter
 * Date: 07/22/2018
 */
public class GameCamera
{
    /** The number of pixels the game's elements are shifted on the x-axis */
    private int offsetX;
    /** The number of pixels the game's elements are shifted on the x-axis */
    private int offsetY;

    /** The rectangular view of the map the camera is displaying */
    private Rectangle view;
    /** The dimensions of the game map */
    private Point mapDimensions;

    /** The PlatformerSprite controlled by the user */
    private PlatformerSprite platformerSprite;
    /** The MapManager that manages the game map */
    private MapManager mapManager;

    /**
     * Create a GameCamera for displaying the game's elements in the panel. The
     * GameCamera is responsible for calculating offsets to track the playerSprite
     * in the center of the screen and subsequently drawing the game elements with
     * respect to the offsets.
     * @param mapManager The mapManager that maintains the game map.
     */
    public GameCamera(MapManager mapManager)
    {
        //Fetch a reference to the platformerSprite, it's id should always be 1
        this.platformerSprite = (PlatformerSprite) mapManager.getSprite(1);
        this.mapManager = mapManager;

        //Set up the view and get the map dimensions
        view = new Rectangle(offsetX, offsetY, PlatformerPanel.WIDTH, PlatformerPanel.HEIGHT);
        mapDimensions = mapManager.getMapDimensions();
    }

    /**
     * Update the game camera and recalculate its x and y offset values.
     */
    public void update()
    {
        //Get the map coordinates of the sprite
        Point spriteCoords = platformerSprite.getMapCoords();

        //If the sprite's x coordinate is to the left of the center of the panel
        //or if the entire map is already visible in the panel, set the offset to zero
        if (spriteCoords.x < view.width/2 || mapDimensions.x == PlatformerPanel.WIDTH)
        {
            offsetX = 0;
        }
        else if (spriteCoords.x + view.width/2 >= mapDimensions.x) //If the coordinate is close to end of the map
        {
            offsetX = mapDimensions.x - view.width; //The offset is the remaining distance
        }
        else if (spriteCoords.x >= view.width/2) //If the coordinate is past the middle of the panel
        {
            offsetX = spriteCoords.x - view.width/2;
        }
    }

    /**
     * Draw the game elements that are affected by the camera offsets.
     * @param dbGraphics The graphics object used to draw this game's elements.
     */
    public void draw(Graphics dbGraphics)
    {
        //Draw the blocks using the game camera offsets
        mapManager.drawBlocks(dbGraphics, -offsetX, -offsetY);

        //Draw the sprite with respect to the offset
        if (offsetX == 0)
        {
            //Draw the sprite at its own coordinates
            platformerSprite.draw(dbGraphics, 0, 0);
        }
        else if (offsetX > 0)
        {
            //Draw the sprite in the center or far right of the screen
            platformerSprite.draw(dbGraphics, -offsetX, -offsetY);
        }
    }
}