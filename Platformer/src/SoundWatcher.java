public interface SoundWatcher
{
    int STOPPED = 0;
    int LOOPING = 1;

    void soundClipEnding(String soundName, int status);
}
