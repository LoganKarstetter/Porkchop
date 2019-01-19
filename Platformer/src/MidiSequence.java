import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.io.IOException;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class MidiSequence
{
    /** The name of the midi sequence */
    private String sequenceName;
    /** The midi sequence */
    private Sequence midiSequence;
    /** The sequencer that plays the midi sequence */
    private Sequencer sequencer;
    /** Flag specifying if the sequence is looping */
    private boolean isLoopingSequence;

    /**
     * Create a new MidiSequence.
     * @param nameOfSequence The name of the sequence.
     * @param filePath The file path to where the sequence is stored.
     * @param midiSequencer The sequencer that plays midi sequences.
     */
    public MidiSequence(String nameOfSequence, String filePath, Sequencer midiSequencer)
    {
        //Store the Midi data
        sequenceName = nameOfSequence;
        sequencer = midiSequencer;

        //Load the sequence
        loadMidiSequence(filePath);
    }

    /**
     * Load the midi sequence from the inputted file path.
     * @param filePath The path to the midi sequence.
     * @return True if the sequence is loaded, false otherwise.
     */
    private boolean loadMidiSequence(String filePath)
    {
        try
        {
            //Retrieve the midi sequence from the file path
            midiSequence = MidiSystem.getSequence(getClass().getResource(filePath));
            return true;
        }
        catch (IOException exception)
        {
            System.out.println("Error loading sequence: " + filePath);
            exception.printStackTrace();
        }
        catch (InvalidMidiDataException exception)
        {
            System.out.println("Midi error loading sequence: " + filePath);
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Play the midi sequence.
     * @param loopMidiSequence Flag specifiying if the sequence should loop.
     */
    public void play(boolean loopMidiSequence)
    {
        //Check that the sequencer and sequence exist
        if (sequencer != null && midiSequence != null)
        {
            try
            {
                //Set the sequence and data
                sequencer.setSequence(midiSequence);
                sequencer.setTickPosition(0);
                isLoopingSequence = loopMidiSequence;
                sequencer.start();
            }
            catch (InvalidMidiDataException exception)
            {
                System.out.println("Midi error playing sequence: " + sequenceName);
                exception.printStackTrace();
            }
        }
    }

    /**
     * Attempt to loop the sequence if it is set to loop.
     * This method is called when a sequence is ending
     * by the regular stop() method. It relies on meta
     * events.
     * @return True if the sequence has looped, false otherwise.
     */
    public boolean loopSequence()
    {
        if (sequencer != null && midiSequence != null)
        {
            //Stop and reset the sequence regardless of looping
            if (sequencer.isRunning())
            {
                sequencer.stop();
            }
            sequencer.setTickPosition(0);

            //If the sequence is set to loop, restart it
            if (isLoopingSequence)
            {
                sequencer.start();
                return true;
            }
        }
        return false;
    }

    /**
     * Pause the midi sequence.
     */
    public void pause()
    {
        //Check that the sequencer and sequence exist
        if (sequencer != null && midiSequence != null)
        {
            if (sequencer.isRunning())
            {
                sequencer.stop();
            }
        }
    }

    /**
     * Resume the midi sequence.
     */
    public void resume()
    {
        //Check that the sequencer and sequence exist
        if (sequencer != null && midiSequence != null)
        {
            //If the song is at the start
            if (sequencer.getTickPosition() == 0)
            {
                try
                {
                    //Set the sequence
                    sequencer.setSequence(midiSequence);
                }
                catch (InvalidMidiDataException exception)
                {
                    System.out.println("Midi error playing sequence: " + sequenceName);
                    exception.printStackTrace();
                }
            }
            //Start the song
            sequencer.start();
        }
    }

    /**
     * Stop the midi sequence and reset it. Note this method will
     * NOT stop the sequence immediately. Instead it relies on
     * triggering a meta event which will either loop or end
     * the sequence. If you intend to stop/swap the song with
     * another, then use stopWithoutNotify().
     */
    public void stop()
    {
        //Check that the sequencer and sequence exist
        if (sequencer != null && midiSequence != null)
        {
            //Start and move the tick to the end of the sequence
            //to trigger a meta event for the MidiManager
            isLoopingSequence = false;
            if (!sequencer.isRunning())
            {
                sequencer.start();
            }
            sequencer.setTickPosition(sequencer.getTickLength());
        }
    }

    /**
     * Immediately stop the midi sequence and reset it.
     * This method does NOT trigger a meta event.
     */
    public void stopWithoutNotify()
    {
        //Check that the sequencer and sequence exist
        if (sequencer != null && midiSequence != null)
        {
            isLoopingSequence = false;
            sequencer.stop();
            sequencer.setTickPosition(0);
        }
    }

    /**
     * Get the name of the sequence.
     * @return The sequence name.
     */
    public String getSequenceName()
    {
        return sequenceName;
    }
}
