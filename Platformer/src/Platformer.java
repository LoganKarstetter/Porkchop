import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * @author Logan Karstetter
 * Date: 06/30/2018
 */
public class Platformer extends JFrame implements WindowListener
{
    /** The desired FPS/UPS for the platformer */
    private static int DEFAULT_FPS = 10;

    /** The PlatformerPanel used to play the game */
    private PlatformerPanel platformerPanel;

    /**
     * A single-player Platformer game.
     * @param FPS The desired FPS.
     */
    public Platformer(int FPS)
    {
        super("Platformer");

        //Create the PlatformerPanel and add it to the contentPane
        platformerPanel = new PlatformerPanel(FPS);
        getContentPane().add(platformerPanel);

        //Add a windowListener to handle window events (pausing etc.)
        addWindowListener(this);
        setResizable(false); //Don't let the game be resized
        setVisible(true);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Resumes the game when the window is activated/invoked.
     * @param e A WindowEvent
     */
    public void windowActivated(WindowEvent e)
    {
        platformerPanel.resumeGame();
    }

    /**
     * Pauses the game when the window is deactivated.
     * @param e A WindowEvent
     */
    public void windowDeactivated(WindowEvent e)
    {
        platformerPanel.pauseGame();
    }

    /**
     * Resumes the game when the window is deiconified/invoked.
     * @param e A WindowEvent
     */
    public void windowDeiconified(WindowEvent e)
    {
        platformerPanel.resumeGame();
    }

    /**
     * Pauses the game when the window is iconified.
     * @param e A WindowEvent
     */
    public void windowIconified(WindowEvent e)
    {
        platformerPanel.pauseGame();
    }

    /**
     * Stops the game when the window is closed.
     * @param e A WindowEvent
     */
    public void windowClosing(WindowEvent e)
    {
        platformerPanel.stopGame();
    }

    /**
     * This method does nothing.
     * @param e A WindowEvent
     */
    public void windowOpened(WindowEvent e)
    {
        //Do nothing
    }

    /**
     * This method does nothing.
     * @param e A WindowEvent
     */
    public void windowClosed(WindowEvent e)
    {
        //Do nothing
    }

    /**
     * Launches a platforming game. A single integer value can be specified as a
     * command line argument to set the FPS for the game. If no value is provided
     * it will run at the default FPS.
     * @param args An integer specifying the requested FPS.
     */
    public static void main(String[] args)
    {
        //Check for command line arguments
        if (args.length > 0)
        {
            //Read the first argument and try to cast it to an integer
            try
            {
                int FPS = Integer.parseInt(args[0]);
                System.out.println("Running Platformer with FPS: " + FPS);
                new Platformer(FPS);
            }
            catch (Exception e) //Horrible practice, but error catching isn't useful here
            {
                System.out.println("Unable to set requested FPS value: " + args[0] +
                        "\nPlease enter only a single integer. Exiting...");
                System.exit(0);
            }
        }
        else //Use the default FPS
        {
            System.out.println("Running Platformer with default FPS: " + DEFAULT_FPS);
            new Platformer(DEFAULT_FPS);
        }
    }
}
