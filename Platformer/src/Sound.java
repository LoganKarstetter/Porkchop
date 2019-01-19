import javax.sound.sampled.*;
import java.io.IOException;
/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class Sound implements LineListener
{
    /** The name of the sound */
    private String soundName;
    /** The sound clip */
    private Clip soundClip;
    /** The duration of the sound in seconds */
    private int durationInSecs;
    /** Flag specifying if the sound is looping */
    private boolean isLoopingSound;
    /** The watcher that is notified when sound events occur */
    private SoundWatcher soundWatcher;

    /**
     * Create a new sound.
     * @param nameOfSound The name of the sound.
     * @param filePath The file path to the sound's location.
     */
    public Sound(String nameOfSound, String filePath)
    {
        //Store the sound data
        soundName = nameOfSound;

        //Attempt to the load the sound clip
        if (loadSound(filePath)) {
            //Determine the duration of the clip in secs
            durationInSecs = (int) (soundClip.getMicrosecondLength() / 1000000);
            soundClip.addLineListener(this);
        }
    }

    /**
     * Load the sound from the passed filePath.
     * @param filePath The file path to the sound to load.
     * @return True if the sound is loaded, false otherwise.
     */
    private boolean loadSound(String filePath)
    {
        try
        {
            //Create an AudioInputStream to the file
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream(filePath));
            AudioFormat audioFormat = audioStream.getFormat();

            //Convert ULAW and ALAW formats to PCM format to support sound manipulation, if desired
            if ((audioFormat.getEncoding() == AudioFormat.Encoding.ALAW)
                    || (audioFormat.getEncoding() == AudioFormat.Encoding.ULAW)) {
                //ULAW and ALAW use an 8-bit byte for each sample that expands to 14 bits when decompressed, converting
                //this to PCM (16 bits) gives two extra bits, thus the sample size and frame size are multiplied.
                //The BigEndian format specifies high-to-low byte format for when the data decompresses to 14 bits.
                AudioFormat pcmFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(),
                        audioFormat.getSampleSizeInBits() * 2, audioFormat.getChannels(),
                        audioFormat.getFrameSize() * 2, audioFormat.getFrameRate(), true);

                //Update the audioStream with the new format
                audioStream = AudioSystem.getAudioInputStream(pcmFormat, audioStream);
                audioFormat = pcmFormat;
            }

            //Check that the audio system supports data lines
            DataLine.Info lineInfo = new DataLine.Info(Clip.class, audioFormat);
            if (!AudioSystem.isLineSupported(lineInfo)) {
                System.out.println("Unsupported clip: " + filePath);
                return false;
            }

            //Get the soundClip's line resource
            soundClip = (Clip) AudioSystem.getLine(lineInfo);
            soundClip.open(audioStream);

            //Close the audio stream
            audioStream.close();
            return true;
        }
        catch (IOException exception)
        {
            System.out.println("Error loading sound: " + filePath);
            exception.printStackTrace();
        } catch (UnsupportedAudioFileException exception)
        {
            System.out.println("Audio file error loading sound: " + filePath);
            exception.printStackTrace();
        } catch (LineUnavailableException exception)
        {
            System.out.println("Line error loading sound: " + filePath);
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * This method is called when the sound clip's line detects open, start, stop,
     * and close events. This not a typical update method that needs to be called
     * for sounds to work.
     * @param lineEvent The line event that occurred.
     */
    public void update(LineEvent lineEvent)
    {
        //If a soundsClip has stopped or reached its end
        if (lineEvent.getType() == LineEvent.Type.STOP)
        {
            //Stop and reset the soundClip
            soundClip.stop();
            soundClip.setFramePosition(0);

            //Inform the watching objects if soundClip is ending or restarting
            if (isLoopingSound && soundWatcher != null)
            {
                soundWatcher.soundClipEnding(soundName, SoundWatcher.LOOPING);
                soundClip.start();
            } else if (soundWatcher != null)
            {
                soundWatcher.soundClipEnding(soundName, SoundWatcher.STOPPED);
            }
        }
    }

    /**
     * Play the sound. If the sound is already playing, restart it.
     * @param loopSound Flag specifying if the sound should loop.
     */
    public void play(boolean loopSound)
    {
        //Play the sound
        if (soundClip != null)
        {
            //If the sound is already playing, restart it
            if (soundClip.getFramePosition() > 0)
            {
                soundClip.stop();
                soundClip.setFramePosition(0);
            }
            soundClip.start();
            isLoopingSound = loopSound;
        }
    }

    /**
     * Pause the sound.
     */
    public void pause()
    {
        //Pause the clip
        if (soundClip != null)
        {
            soundClip.stop();
        }
    }

    /**
     * Resume the sound.
     */
    public void resume()
    {
        //Play the sound
        if (soundClip != null)
        {
            soundClip.start();
        }
    }

    /**
     * Stop and reset the sound.
     */
    public void stop()
    {
        //Stop and reset the sound
        if (soundClip != null)
        {
            soundClip.stop();
            soundClip.setFramePosition(0);
            isLoopingSound = false;
        }
    }

    /**
     * Stop and close the sound.
     */
    public void close()
    {
        if (soundClip != null)
        {
            soundClip.stop();
            soundClip.close();
        }
    }

    /**
     * Set the sound watcher for this sound.
     * @param soundClipWatcher The sound clip watcher to notify.
     */
    public void setSoundWatcher(SoundWatcher soundClipWatcher)
    {
        soundWatcher = soundClipWatcher;
    }
}
