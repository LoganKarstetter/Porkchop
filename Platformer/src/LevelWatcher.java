public interface LevelWatcher
{
    //This method is called by the Game once the player has encountered
    //the event block for triggering a level change
    void changeToNextLevel();

    //Called by the Game once the player has picked up a collectable
    void itemCollected();

    //This method is called by the Game once the player has died
    void playerHasDied();
}