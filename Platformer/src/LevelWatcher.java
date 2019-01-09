public interface LevelWatcher
{
    //This method is called by the Game once the player has encountered
    //the event block for triggering a level change
    void changeToNextLevel(InputComponent inputComponent);

    //Called by the Game once the player has defeated an enemy
    void enemyDefeated();

    //Called by the Game once the player has picked up a collectible
    void itemCollected();

    //This method is called by the Game once the player has died
    void playerHasDied(InputComponent inputComponent);
}