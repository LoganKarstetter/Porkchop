import javax.swing.*;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class Launcher extends JFrame
{
    /** The default and recommend FPS value */
    private static final int DEFAULT_FPS = 30;
    /** The number of game cycles in a single second */
    public static final int ONE_SECOND = DEFAULT_FPS;

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
     * Start the game at the default FPS.
     * @param args The command line argument for the FPS.
     */
    public static void main(String[] args)
    {
        //Leave the FPS at 30
        System.out.println("Running with default FPS: " + DEFAULT_FPS);
        new Launcher(DEFAULT_FPS);
    }
}
