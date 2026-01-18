package io.github.some_example_name;
import  io.github.some_example_name.manager.MemoryManager;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

public class GameSession {

    public GameState state;
    long nextTrashSpawnTime;
    long sessionStartTime;
    private int score;
    long pauseStartTime;
    int destructedTrashNumber;


    public GameSession() {
    }

    public void destructionRegistration()
    {
        destructedTrashNumber += 1;
    }

    public void updateScore()
    {
      score = destructedTrashNumber;
    }
    public int getScore()
    {
        return score;
    }

    public void startGame() {
        state = GameState.PLAYING;
        score = 0;
        destructedTrashNumber = 0;
        sessionStartTime = TimeUtils.millis();
        nextTrashSpawnTime = sessionStartTime + (long) (GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN
            * getTrashPeriodCoolDown());
    }
    public void resumeGame()
    {
        state = GameState.PLAYING;
        sessionStartTime += TimeUtils.millis() - pauseStartTime;
    }
    public void endGame()
    {
        updateScore();
        state = GameState.ENDED;
        ArrayList<Integer> recordsTable = MemoryManager.loadRecordsTable();
        if (recordsTable == null) {
            recordsTable = new ArrayList<>();
        }
        int foundIdx = 0;
        for (; foundIdx < recordsTable.size(); foundIdx++) {
            if (recordsTable.get(foundIdx) < getScore()) break;
        }
        recordsTable.add(foundIdx, getScore());
        MemoryManager.saveTableOfRecords(recordsTable);
    }

    public void pauseGame()
    {
        state = GameState.PAUSED;
        pauseStartTime = TimeUtils.millis();
    }

    public boolean shouldSpawnTrash() {
        if (nextTrashSpawnTime <= TimeUtils.millis()) {
            nextTrashSpawnTime = TimeUtils.millis() + (long) (GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN
                * getTrashPeriodCoolDown());
            return true;
        }
        return false;
    }

    private float getTrashPeriodCoolDown() {
        return (float) Math.exp(-0.001 * (TimeUtils.millis() - sessionStartTime + 1) / 1000);
    }
}
