/**
 * @author Logan Karstetter
 * Date: 2018
 */
public interface SoundWatcher
{
    /** The constant defining that a sound has stopped */
    int STOPPED = 0;
    /** The constant defining that a sound has stopped, but will restart */
    int LOOPING = 1;

    /** This method is called when a sound clip reaches its end */
    void soundClipEnding(String soundName, int status);
}
