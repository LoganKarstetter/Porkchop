import javax.sound.sampled.*;
import java.io.IOException;

public class Sound implements LineListener
{
    private String soundName;
    private Clip soundClip;
    private int durationInSecs;
    private boolean isLoopingSound;
    private SoundWatcher soundWatcher;

    public Sound(String nameOfSound, String filePath)
    {
        //Store the sound data
        soundName = nameOfSound;

        //Attempt to the load the sound clip
        if (loadSound(filePath)) {
            //Determine the duration of the clip in secs
            durationInSecs = (int) (soundClip.getMicrosecondLength() / 1000000);
        }
    }

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
        } catch (IOException exception)
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

    //Called when the soundClip's line detects open, start, stop, and close events
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

    public void setSoundWatcher(SoundWatcher soundClipWatcher)
    {
        soundWatcher = soundClipWatcher;
    }

    public void play(boolean loopSound)
    {
        //Play the sound
        if (soundClip != null)
        {
            soundClip.start();
            isLoopingSound = loopSound;
        }
    }

    public void pause()
    {
        //Pause the clip
        if (soundClip != null)
        {
            soundClip.stop();
        }
    }

    public void resume()
    {
        //Play the sound
        if (soundClip != null)
        {
            soundClip.start();
        }
    }

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

    public void close()
    {
        if (soundClip != null)
        {
            soundClip.stop();
            soundClip.close();
        }
    }
}
