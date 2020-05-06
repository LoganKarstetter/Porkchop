import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class GamePanel extends JPanel implements Runnable
{
    /** The width of the game panel */
    public static final int WIDTH = 600;
    /** The height of the game panel */
    public static final int HEIGHT = 600;

    /** The thread that runs the animation loop */
    private Thread animator;
    /** The amount of time allocated for each cycle of the game loop (in nanos) */
    private long loopPeriod;
    /** Determines whether the animator thread is running */
    private volatile boolean isRunning;

    /** The Graphics used to double buffer/render the screen */
    private Graphics dbGraphics;
    /** The image that is created/rendered offscreen and later painted to the screen */
    private Image dbImage;

    /** The game object that is actually "played" */
    private Game game;

    /**
     * Create a new game panel and subsequent game.
     * @param framesPerSecond The desired FPS to run at. (30)
     */
    public GamePanel(int framesPerSecond)
    {
        //Calculate nanoseconds per game loop cycle
        loopPeriod = 1000000000/framesPerSecond;

        //Set essential panel data
        setBackground(Color.BLACK);
        setDoubleBuffered(false);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();

        //Setup game input data
        InputComponent userInputComponent = new InputComponent();
        this.addKeyListener(userInputComponent);
        this.addMouseListener(userInputComponent);

        //Create game
        game = new Game("Levels/LevelsConfig.txt", userInputComponent);
    }

    /**
     * Notifies this component that it now has a parent component.
     * This method informs the GamePanel that it has been added to a
     * parent container such as a JFrame. Once notified it starts the
     * game. This prevents the game starting before the user can see it.
     */
    public void addNotify()
    {
        super.addNotify();
        if (animator == null || !isRunning)
        {
            animator = new Thread(this);
            animator.start();
        }
    }

    /**
     * Stop the game, set isRunning to false.
     */
    public void stopGame()
    {
        isRunning = false;
    }

    /**
     * Repeatably update, render, paint, and sleep such that the game loop takes close to the amount of
     * time allotted by the desired FPS (loopPeriod).
     */
    public void run()
    {
        long timeBeforeLoop; //The time measured before the game methods are called
        long timeAfterLoop; //The time measured after the game methods complete
        long timeDifference; //The time taken to complete run the methods (timeAfterLoop - timeBeforeLoop)

        long overtime = 0L; //The excess time taken to complete the loop (actual - loopPeriod)
        long timeToSleep = 0L; //Time left for sleeping to maintain fps (loopPeriod - timeDifference) - timeOverslept
        long timeOverslept = 0L; //The amount of time the thread overslept

        int numDelays = 0; //The number of times the thread has looped without sleeping
        int numFramesSkipped = 0; //The number of times the thread has skipped rendering due to running overtime
        int numDelaysBeforeYield = 16; //Number of times the thread can loop with sleep < 0 before yielding to other threads
        int framesSkippedBeforeRender = 5; //Number of frames can skip rendering before it is forced to do so

        //Capture the time before the first loop begins
        timeBeforeLoop = System.nanoTime();
        isRunning = true;
        while (isRunning)
        {
            //Update, render, paint
            gameUpdate();
            gameRender();
            paintScreen();

            //Capture the time taken to run the methods
            timeAfterLoop = System.nanoTime();
            timeDifference = (timeAfterLoop - timeBeforeLoop);

            //Calculate time to sleep to maintain fps
            timeToSleep = (loopPeriod - timeDifference) - timeOverslept;
            if (timeToSleep > 0)
            {
                try
                {
                    //Convert timeToSleep to milliseconds
                    Thread.sleep(timeToSleep/1000000);
                }
                catch (InterruptedException exception) { /* Do nothing */ }
            }
            else
            {
                //Capture overtime and clear timeOverslept
                overtime = overtime - timeToSleep;
                timeOverslept = 0L;

                //Increment delays and check for yield condition
                if (++numDelays >= numDelaysBeforeYield)
                {
                    Thread.yield();
                    numDelays = 0;
                }
            }

            //Capture the time before the next loop begins
            timeBeforeLoop = System.nanoTime();

            //Force an update if rendering is consuming too much of the loop period to maintain ups
            numFramesSkipped = 0;
            while ((overtime > loopPeriod) && (numFramesSkipped < framesSkippedBeforeRender))
            {
                overtime = overtime - loopPeriod;
                gameUpdate();
                numFramesSkipped++;
            }
        }
        System.exit(0);
    }

    /**
     * Update the game as long as the game is not over or paused.
     */
    private void gameUpdate()
    {
        game.update(loopPeriod);
    }

    /**
     * Render the game using double buffering. If it does not already exist, this
     * method creates an Image the size of the GamePanel and draws to it offscreen.
     * Drawing offscreen prevents flickering and then allows the paintScreen() method
     * to draw the entire screen as an image rather than in layers.
     */
    private void gameRender()
    {
        //If the double buffered image is null, define it
        if (dbImage == null)
        {
            //Create double buffered image
            dbImage = createImage(WIDTH, HEIGHT);
            if (dbImage == null)
            {
                return;
            }
            else
            {
                //Fetch the image graphics context to enable drawing
                dbGraphics = dbImage.getGraphics();
            }
        }

        //Draw the background and game
        dbGraphics.setColor(Color.BLACK);
        dbGraphics.fillRect(0, 0, WIDTH, HEIGHT);
        game.draw(dbGraphics);
    }

    /**
     * Actively render/draw the dbImage (created in gameRender()) onto the screen.
     */
    private void paintScreen()
    {
        try
        {
            //Retrieve graphics context
            Graphics graphics = this.getGraphics();

            //Draw double buffered image to the panel
            if ((graphics != null) && (dbImage != null))
            {
                graphics.drawImage(dbImage, 0, 0, null);
            }
            Toolkit.getDefaultToolkit().sync(); //Sync display
            graphics.dispose();
        }
        catch (NullPointerException exception)
        {
            System.out.println("Graphics context error");
            exception.printStackTrace();
        }
    }
}
