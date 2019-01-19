import javax.swing.*;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class Launcher extends JFrame
{
    /** The default and recommend FPS value */
    private static final int DEFAULT_FPS = 30;

    /**
     * Create a launcher for launching the game.
     * @param framesPerSecond The desired FPS.
     */
    public Launcher(int framesPerSecond)
    {
        super("");

        //Create and add the game panel
        GamePanel gamePanel = new GamePanel(framesPerSecond);
        getContentPane().add(gamePanel);

        //Set window data
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Reads command line arguments and starts the game.
     * @param args The command line argument for the FPS.
     */
    public static void main(String[] args)
    {
        //Read FPS argument from command line
        if (args.length > 0)
        {
            try
            {
                int framesPerSec = Integer.parseInt(args[0]);
                System.out.println("Running with FPS: " + framesPerSec);
                new Launcher(framesPerSec);
            }
            catch (Exception anyException)
            {
                System.out.println("Unable to set requested FPS: " + args[0]
                        + "\nEnter only a single integer. Exiting.");
                System.exit(0);
            }
        }
        else //Use a hardcoded FPS
        {
            System.out.println("Running with default FPS: " + DEFAULT_FPS);
            new Launcher(DEFAULT_FPS);
        }
    }
}
