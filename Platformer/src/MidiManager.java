import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MidiManager implements MetaEventListener
{
    private static final int META_END_OF_TRACK = 47; //0x2f

    private String directory = "Midi/";
    private Sequencer sequencer;
    private MidiSequence currentSequence;
    private HashMap<String, MidiSequence> sequenceMap;
    private SoundWatcher sequenceWatcher;

    public MidiManager(String midiConfigFile)
    {
        //Setup the sequenceMap
        sequenceMap = new HashMap<>();
        currentSequence = null;

        //Initialize the sequencer, if successful, load sequences
        if (initializeSequencer())
        {
            loadSequencesFromFile(midiConfigFile);
        }
    }

    private boolean initializeSequencer()
    {
        try
        {
            //Retrieve the sequencer from the MidiSystem
            sequencer = MidiSystem.getSequencer();
            if (sequencer == null)
            {
                //Report null sequencer
                System.out.println("Error initializing sequencer");
                return false;
            }

            //Open the sequencer and add a MetaEventListener
            sequencer.open();
            sequencer.addMetaEventListener(this);

            //Link the sequencer to a synthesizer if necessary
            if (!(sequencer instanceof Synthesizer))
            {
                //Retrieve the synthesizer from the MidiSystem
                Synthesizer synthesizer = MidiSystem.getSynthesizer();
                synthesizer.open();

                //Link the sequencer
                Receiver receiver = synthesizer.getReceiver();
                Transmitter transmitter = sequencer.getTransmitter();
                transmitter.setReceiver(receiver);
            }
            return true;
        }
        catch (MidiUnavailableException exception)
        {
            System.out.println("Error initializing sequencer");
            exception.printStackTrace();
        }
        return false;
    }

    private void loadSequencesFromFile(String fileName)
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
                else //Store the sequence name and load it
                {
                    //Get the sequence name (remove an .extension, if any)
                    String sequenceName = line; //Set the sequenceName to the line by default
                    if (line.contains("."))
                    {
                        sequenceName = line.substring(0, line.indexOf('.'));
                    }

                    //Check if the sequenceName already exists in the sequenceMap
                    if (!sequenceMap.containsKey(sequenceMap))
                    {
                        //Pass the entire line since it contains the file extension
                        sequenceMap.put(sequenceName, new MidiSequence(sequenceName, directory + line, sequencer));
                        System.out.println("Stored " + sequenceName + " [" + line + "]");
                    }
                    else //Inform the user a duplicate sequence is in the directory
                    {
                        System.out.println("SequenceMap already contains: " + sequenceName);
                    }
                }
            }

            //Close the BufferedReader
            br.close();

            //Inform the user the MidiManager is done reading
            System.out.println("Finished reading file: " + directory + fileName);
        }
        catch (IOException e)
        {
            System.out.println("Error reading file: " + directory + fileName + " " + e);
            e.printStackTrace();
        }
    }

    //Called when a meta event, such as a sequence ending, is triggered
    public void meta(MetaMessage metaMessage)
    {
        //If the meta message signals the end of a sequence
        if (metaMessage.getType() == META_END_OF_TRACK)
        {
            //If the sequence is not going to loop, clear it
            String sequenceName = currentSequence.getSequenceName();
            boolean sequenceIsLooping = currentSequence.loopSequence();
            if (!sequenceIsLooping)
            {
                currentSequence = null;
            }

            //Inform the sequenceWatcher of the sequence ending or looping
            if (sequenceWatcher != null)
            {
                if (sequenceIsLooping)
                {
                    sequenceWatcher.soundClipEnding(sequenceName, SoundWatcher.LOOPING);
                }
                else //Inform the watcher the sequence is ending
                {
                    sequenceWatcher.soundClipEnding(sequenceName, SoundWatcher.STOPPED);
                }
            }
        }
    }

    public void setSoundWatcher(SoundWatcher midiSequenceWatcher)
    {
        sequenceWatcher = midiSequenceWatcher;
    }

    public boolean play(String sequenceName, boolean loopSequence)
    {
        if (sequenceMap.containsKey(sequenceName))
        {
            //Stop the currently playing song
            if (currentSequence != null)
            {
                currentSequence.stopWithoutNotify();
            }

            //Set the sequence and play it
            currentSequence = sequenceMap.get(sequenceName);
            currentSequence.play(loopSequence);
            return true;
        }
        else
        {
            //Inform the user the sequence does not exist
            System.out.println("Sequence does not exist: " + sequenceName);
        }
        return false;
    }

    public void pause()
    {
        //Pause the sequence
        if (currentSequence != null)
        {
            currentSequence.pause();
        }
    }

    public void resume()
    {
        //Resume the sequence
        if (currentSequence != null)
        {
            currentSequence.resume();
        }
    }

    public void stop()
    {
        //Stop the sequence
        if (currentSequence != null)
        {
            currentSequence.stop();
        }
    }

    public void close()
    {
        //Stop the current sequence
        stop();

        //Stop the sequencer
        if (sequencer != null)
        {
            if (sequencer.isRunning())
            {
                sequencer.stop();
            }

            //Clear the sequencer and listener
            sequencer.removeMetaEventListener(this);
            sequencer.close();
            sequencer = null;
        }
    }
}
