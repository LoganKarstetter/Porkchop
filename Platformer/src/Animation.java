import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Animation
{
    private ArrayList<BufferedImage> images;
    private long totalDurationInMs;
    private long imageDurationInMs;
    private boolean isLooping;
    private int imagePosition;
    private AnimationWatcher watcher;

    public Animation(ArrayList<BufferedImage> imageSequence, long durationInMs, boolean loopAnimation)
    {
        //Verify valid duration
        if (durationInMs <= 0)
        {
            durationInMs = 1000;
        }

        //Store animation data
        images = imageSequence;
        totalDurationInMs = durationInMs;
        imageDurationInMs = totalDurationInMs / images.size();
        isLooping = loopAnimation;
        imagePosition = 0;
    }

    public long update(long loopPeriodInMs, long localElapsedTimeInMs)
    {
        //Verify looping flag after the first complete loop
        if (imagePosition < (images.size() - 1) || isLooping)
        {
            //Compute elapsed time and reset to zero if it's greater than or equal to the total duration
            localElapsedTimeInMs = (localElapsedTimeInMs + loopPeriodInMs) % totalDurationInMs;

            //Update the image position
            imagePosition = (int) (localElapsedTimeInMs / imageDurationInMs);
        }
        else //Inform the watcher that the animation ended and reset the animation
        {
            if (watcher != null)
            {
                watcher.animationHasEnded();
            }

            //Reset the image position, it is assumed the graphics state will change before the next update
            imagePosition = 0;
        }

        //Return updated local elapsed time
        return localElapsedTimeInMs;
    }

    public void draw(Graphics dbGraphics, int x, int y)
    {
        //Draw the animation
        dbGraphics.drawImage(images.get(imagePosition), x, y, null);
    }

    public void setWatcher(AnimationWatcher animationWatcher)
    {
        watcher = animationWatcher;
    }

    public int getImageWidth()
    {
        return images.get(0).getWidth();
    }

    public int getImageHeight()
    {
        return images.get(0).getHeight();
    }
}
