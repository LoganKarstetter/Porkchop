import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class SoundManager
{
    /** The directory containing the sound files and config */
    private String directory = "Sounds/";
    /** Maps sound names to actual sound clips */
    private HashMap<String, Sound> soundMap;
    /** Flag specifying if sound is enabled or disabled */
    private boolean soundEnabled;

    /**
     * Create a new SoundManager for playing and storing sounds.
     * @param soundConfigFile The name/path to the sound config file.
     */
    public SoundManager(String soundConfigFile)
    {
        //Setup the sound map and load sounds
        soundMap = new HashMap<>();
        soundEnabled = true;
        loadSoundsFromFile(soundConfigFile);
    }

    /**
     * Load the configured sounds from the files.
     * @param fileName The path to the sounds config file.
     */
    private void loadSoundsFromFile(String fileName)
    {
        //Inform the user of the file reading
        System.out.println("Reading file: " + directory + fileName);
        try
        {
            //Create an InputStream and BufferedReader to read the file
            InputStream inputStream = this.getClass().getResourceAsStream(directory + fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            //Loop until the end of the file is reached
            String line;
            while ((line = br.readLine()) != null)
            {
                //Determine what action to take based off the line read
                if (line.startsWith("//") || (line.length() == 0)) //This line is a comment or blank line
                {
                    continue;
                }
                else //Store the sound name and load it
                {
                    //Get the sound name (remove an .extension, if any)
                    String soundName = line; //Set the soundName to the line by default
                    if (line.contains("."))
                    {
                        soundName = line.substring(0, line.indexOf('.'));
                    }

                    //Check if the soundName already exists in the soundMap
                    if (!soundMap.containsKey(soundName))
                    {
                        //Pass the entire line since it contains the file extension
                        soundMap.put(soundName, new Sound(soundName, directory + line));
                        System.out.println("Stored " + soundName + " [" + line + "]");
                    }
                    else //Inform the user a duplicate sound is in the directory
                    {
                        System.out.println("SoundMap already contains: " + soundName);
                    }
                }
            }

            //Close the BufferedReader
            br.close();

            //Inform the user the SoundManager is done reading
            System.out.println("Finished reading file: " + directory + fileName);
        }
        catch (IOException e)
        {
            System.out.println("Error reading file: " + directory + fileName + " " + e);
            e.printStackTrace();
        }
    }

    /**
     * Play the sound that is mapped to the passed sound name.
     * @param soundName The name of the sound to play.
     * @param loopSound Flag specifying if the sound should loop.
     * @return True if the sound can be played, false otherwise.
     */
    public boolean playSound(String soundName, boolean loopSound)
    {
        if (soundEnabled)
        {
            if (soundMap.containsKey(soundName))
            {
                soundMap.get(soundName).play(loopSound);
                return true;
            }

            //Inform the user the sound does not exist
            System.out.println("Sound does not exist: " + soundName);
        }
        return false;
    }

    /**
     * Pause the sound that is mapped to the passed sound name.
     * @param soundName The name of the sound to pause.
     * @return True if the sound can be paused, false otherwise.
     */
    public boolean pauseSound(String soundName)
    {
        if (soundMap.containsKey(soundName))
        {
            soundMap.get(soundName).pause();
            return true;
        }

        //Inform the user the sound does not exist
        System.out.println("Sound does not exist: " + soundName);
        return false;
    }

    /**
     * Resume the sound that is mapped to the passed sound name.
     * @param soundName The name of the sound to pause.
     * @return True if the sound can be paused, false otherwise.
     */
    public boolean resumeSound(String soundName)
    {
        if (soundEnabled)
        {
            if (soundMap.containsKey(soundName))
            {
                soundMap.get(soundName).resume();
                return true;
            }

            //Inform the user the sound does not exist
            System.out.println("Sound does not exist: " + soundName);
        }
        return false;
    }

    /**
     * Stop the sound mapped to the passed sound name.
     * @param soundName The the name of the sound to stop.
     * @return True if the sound can be stopped, false otherwise.
     */
    public boolean stopSound(String soundName)
    {
        if (soundMap.containsKey(soundName))
        {
            soundMap.get(soundName).stop();
            return true;
        }

        //Inform the user the sound does not exist
        System.out.println("Sound does not exist: " + soundName);
        return false;
    }

    /**
     * Close the sound mapped to the passedsound name.
     * @param soundName The name of the sound to close.
     * @return True if the sound can be closed, false otherwise.
     */
    public boolean closeSound(String soundName)
    {
        if (soundMap.containsKey(soundName))
        {
            soundMap.get(soundName).close();
            return true;
        }

        //Inform the user the sound does not exist
        System.out.println("Sound does not exist: " + soundName);
        return false;
    }

    /**
     * Set the sound watcher to notify when sound event occur.
     * @param soundName The name of the sound to watch.
     * @param soundClipWatcher The sound watcher to notify.
     * @return True if the sound can be watched, false otherwise.
     */
    public boolean setSoundWatcher(String soundName, SoundWatcher soundClipWatcher)
    {
        if (soundMap.containsKey(soundName))
        {
            soundMap.get(soundName).setSoundWatcher(soundClipWatcher);
            return true;
        }

        //Inform the user the sound does not exist
        System.out.println("Sound does not exist: " + soundName);
        return false;
    }

    /**
     * Enable or disable sounds from playing
     * @param soundIsEnabled True if sounds should be enabled, false otherwise.
     */
    public void enableSound(boolean soundIsEnabled)
    {
        //Store the enabled status
        soundEnabled = soundIsEnabled;
    }

    /**
     * Is sound enabled?
     * @return True if enabled, false otherwise.
     */
    public boolean isSoundEnabled()
    {
        return soundEnabled;
    }
}
