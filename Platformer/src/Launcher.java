import javax.swing.*;

public class Launcher extends JFrame
{
    private static final int DEFAULT_FPS = 30;

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
