import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.io.IOException;

public class MidiSequence
{
    private String sequenceName;
    private Sequence midiSequence;
    private Sequencer sequencer;
    private boolean isLoopingSequence;

    public MidiSequence(String nameOfSequence, String filePath, Sequencer midiSequencer)
    {
        //Store the Midi data
        sequenceName = nameOfSequence;
        sequencer = midiSequencer;

        //Load the sequence
        loadMidiSequence(filePath);
    }

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

    public void resume()
    {
        //Check that the sequencer and sequence exist
        if (sequencer != null && midiSequence != null)
        {
            sequencer.start();
        }
    }

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

    //Stops the sequence immediately without triggering a meta event
    public void stopWithoutNotify()
    {
        //Check that the sequencer and sequence exist
        if (sequencer != null && midiSequence != null)
        {
            isLoopingSequence = false;
            sequencer.stop();
        }
    }

    public boolean loopSequence()
    {
        //This method is called when the sequence is ending
        //Attempt to loop the sequence if the isLoopingSequence
        //flag is set to TRUE
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

    public String getSequenceName()
    {
        return sequenceName;
    }
}
