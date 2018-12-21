import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class SoundManager
{
    private String directory = "Sounds/";
    private HashMap<String, Sound> soundMap;

    public SoundManager(String soundConfigFile)
    {
        //Setup the sound map and load sounds
        soundMap = new HashMap<>();
        loadSoundsFromFile(soundConfigFile);
    }

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

    public boolean playSound(String soundName, boolean loopSound)
    {
        if (soundMap.containsKey(soundName))
        {
            soundMap.get(soundName).play(loopSound);
            return true;
        }

        //Inform the user the sound does not exist
        System.out.println("Sound does not exist: " + soundName);
        return false;
    }

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

    public boolean resumeSound(String soundName)
    {
        if (soundMap.containsKey(soundName))
        {
            soundMap.get(soundName).resume();
            return true;
        }

        //Inform the user the sound does not exist
        System.out.println("Sound does not exist: " + soundName);
        return false;
    }

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
}
