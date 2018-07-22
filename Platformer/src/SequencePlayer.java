import java.awt.image.BufferedImage;
/**
 * @author Logan Karstetter
 * Date: 07/01/2018
 */
public class SequencePlayer
{
    /** The name of the image sequence */
    private String imageSequenceName;
    /** The number of images in the sequence */
    private int numImages;
    /** The current index of the image to be display in the sequence */
    private int sequenceIndex;
    /** Determines whether the animation should be played */
    private boolean playAnimation;
    /** Determines whether the animation should loop after completion */
    private boolean loopAnimation;

    /** The amount of time in seconds taken for the entire sequence to play from start to finish */
    private double totalDuration;
    /** The amount of time in milliseconds for a single image to be shown */
    private double imageDuration;
    /** The amount of time elapsed thus far in a single sequence loop (totalDuration) */
    private long elapsedTime;

    /** The loopPeriod of the main panel */
    private long loopPeriod;
    /** The ImageLoader used to fetch the images */
    private ImageLoader imageLoader;

    /**
     * Create a SequencePlayer for displaying a series of images in sequence/an animation.
     * @param imageSequenceName The name of the stored sequence of images to loop through.
     * @param loopAnimation Determines whether the animation should loop.
     * @param totalDuration The total duration of the start of the sequence to the end in seconds.
     * @param loopPeriod The loopPeriod of the main panel (in nanoseconds).
     * @param imageLoader The ImageLoader used to load, store, and retrieve images for this game.
     */
    public SequencePlayer(String imageSequenceName, boolean loopAnimation, double totalDuration, long loopPeriod, ImageLoader imageLoader)
    {
        //Store the name, looping behavior, totalDuration, loopPeriod, and imageLoader
        this.imageSequenceName = imageSequenceName;
        this.loopAnimation = loopAnimation;
        this.totalDuration = totalDuration;
        this.loopPeriod = loopPeriod/1000000L; //Convert from nanoseconds to milliseconds
        this.imageLoader = imageLoader;

        //Set up the player
        if (imageLoader.imageExists(imageSequenceName))
        {
            //Get the number of times and calculate the imageDuration
            numImages = imageLoader.getNumberImages(imageSequenceName);
            sequenceIndex = 0;
            imageDuration = (int) (totalDuration * 1000)/numImages; //totalDuration secs -> ms
            elapsedTime = 0L;
            playAnimation = true;
        }
        else //The requested sequence does not exist
        {
            System.out.println("No image sequence found under '" + imageSequenceName + "'");
            numImages = 0;
        }
    }

    /**
     * Update the elapsed time and sequence index of this SequencePlayer. The elapsed time is relative to
     * the start of the sequence. It begins at zero and each loopPeriod (every time this method is called
     * by the main game panel) the elapsedTime variable is incremented by loopPeriod time. Once this value
     * grows past the set total duration of the sequence (time in seconds to play start to finish), it is
     * modded back to zero. The index of the sequence is also calculated by dividing this elapsed time by
     * the total amount of time each image should be displayed for. This gives the current index of the
     * image to be displayed. If the animation is not set to loop, it will end once the full sequence has
     * been displayed.
     */
    public void update()
    {
        //If the animation is not completed/stopped
        if (playAnimation)
        {
            //This method is called every loopPeriod milliseconds
            //Add the loopPeriod time, return to zero if the totalDuration is reached
            elapsedTime = (long) ((elapsedTime + loopPeriod) % (totalDuration * 1000)); //totalDuration secs -> ms

            //Determine which image to display, elapsed time is the total time elapsed in this sequence thus far
            //For example, if elapsed time is 1000ms and image duration is 500ms, then image 2 will be displayed
            sequenceIndex = (int) (elapsedTime / imageDuration);

            //If the sequenceIndex is at the end of the sequence and the animation should not loop
            if ((sequenceIndex == (numImages - 1)) && !loopAnimation)
            {
                playAnimation = false;
            }
        }
    }

    /**
     * Stop the animation that is currently playing.
     */
    public void stop()
    {
        playAnimation = false;
    }

    /**
     * Resume the animation at its current position.
     */
    public void resume()
    {
        //Make sure the animation isn't a single image before we resume
        if (numImages > 0)
        {
            playAnimation = true;
        }
    }

    /**
     * Get the current image to be displayed in the sequence.
     * @return The current image to be displayed or null if this sequence is empty.
     */
    public BufferedImage getCurrentImage()
    {
        //Make sure there are actually images to retrieve
        if (numImages != 0)
        {
            return imageLoader.getImage(imageSequenceName, sequenceIndex);
        }
        return null;
    }

}
