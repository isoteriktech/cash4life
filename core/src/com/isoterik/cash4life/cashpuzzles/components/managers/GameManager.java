package com.isoterik.cash4life.cashpuzzles.components.managers;

import com.badlogic.gdx.utils.Array;
import com.isoterik.cash4life.UserManager;
import com.isoterik.cash4life.cashpuzzles.utils.Board;
import com.isoterik.cash4life.cashpuzzles.utils.Storage;
import io.github.isoteriktech.xgdx.Component;
import io.github.isoteriktech.xgdx.GameObject;

import java.util.ArrayList;

public class GameManager extends Component {
    private UserManager userManager;
    private WordManager wordManager;
    private UIManager uiManager;
    private LetterManager letterManager;
    private StorageManager storageManager;

    private Storage storage;

    enum Difficulty {
        EASY,
        MEDIUM,
        HARD,
        INSANE,
    }

    private static float levelTime;

    private Board board;

    private Level[] levels;
    private Level currentLevel;

    private final int noOfWordsToFind = 7;
    private int maxLevels = 1;
    private int currentLevelIndex = 0;

    private final float REWARD_AMOUNT = 30000;
    @Override
    public void start() {
        wordManager = gameObject.getHostScene().findGameObject("wordManager").getComponent(WordManager.class);
        uiManager = gameObject.getHostScene().findGameObject("uiManager").getComponent(UIManager.class);
        letterManager = gameObject.getHostScene().findGameObject("letterManager").getComponent(LetterManager.class);
        storageManager = gameObject.getHostScene().findGameObject("storageManager").getComponent(StorageManager.class);
        userManager = gameObject.getHostScene().findGameObject("userManager").getComponent(UserManager.class);

        storage = storageManager.getStorage();

        setLevels();

        maxLevels = levels.length;

        if (storage.isSaved())
            currentLevelIndex = storage.getStage();

        currentLevel = levels[currentLevelIndex];

        initLevel();
    }

    private void setLevels() {
        levels = new Level[]{
                new Level("Stones", Difficulty.INSANE),
                new Level("Planets", Difficulty.MEDIUM),
                new Level("Countries", Difficulty.MEDIUM),
                new Level("Players", Difficulty.MEDIUM),
                new Level("Automobiles", Difficulty.HARD),
                new Level("States", Difficulty.HARD),
                new Level("Languages", Difficulty.HARD),
                new Level("Animals", Difficulty.INSANE),
                new Level("Elements", Difficulty.INSANE),
                new Level("Randoms", Difficulty.INSANE),
        };
    }

    private void initLevel() {
        String categoryName = currentLevel.categoryName;
        wordManager.loadCategoryWords(categoryName);

        ArrayList<String> words = wordManager.getStageWords();

        int[] boardDimensions = currentLevel.dimensions;
        int boardRow = boardDimensions[0], boardColumn = boardDimensions[1];
        board = new Board(boardRow, boardColumn, words);
        board.initializeManagers(wordManager, letterManager);
        board.generatePuzzle();
    }

    public void cont() {
        int level = currentLevelIndex + 1;
        String currentLevelText = currentLevelIndex == 9 ? String.valueOf(level) : "0" + level;
        String maxLevelsText = maxLevels >= 10 ? String.valueOf(maxLevels) : "0" + maxLevels;
        uiManager.setLevelText(currentLevelText + "/" + maxLevelsText);
        uiManager.setCategoryText(currentLevel.categoryName);
        letterManager.initLevel();

        if (storage.isSaved()) {
            uiManager.setLevelTime(storage.getTime());
            wordManager.setFoundWords(storage.getFoundWords());
            uiManager.setReloadTimeCount(storage.getTimeReloadCount());
            uiManager.setHintPowerUpText(storage.getHintCount());
            letterManager.shuffleCells();
        }
        else {
            if (currentLevelIndex == 0)
                uiManager.setLevelTime(levelTime);
        }
    }

    public void currentLevelFinished() {
        wordManager.getFoundWords().clear();
        letterManager.getValidCells().clear();
        letterManager.destroyLetters();
        destroyAllSelectors();

        ++currentLevelIndex;
        if (currentLevelIndex < maxLevels) {
            currentLevel = levels[currentLevelIndex];
            initLevel();
            cont();
        }
        else {
            gameWon();
        }
    }

    public void saveGame() {
        if (currentLevelIndex <= 4) return;

        Storage s = new Storage(
                true,
                wordManager.getFoundWords(),
                currentLevelIndex,
                uiManager.getTimeInMins(),
                uiManager.getReloadTimeCount(),
                uiManager.getShowHintAmount()
        );
        storageManager.save(s);
    }

    private void gameWon() {
        userManager.deposit(REWARD_AMOUNT);
        uiManager.gameFinished();
        storageManager.reset();
    }

    public void gameOver() {
        storageManager.reset();
        uiManager.gameLost();
    }

    public void reInitializeBoard() {
        ArrayList<String> words = wordManager.getStageWords();
        board = new Board(board.getRows(), board.getColumns(), words);
        board.initializeManagers(wordManager, letterManager);
        board.generatePuzzle();
    }

    public Board getBoard() {
        return board;
    }

    public int getNoOfWordsToFind() {
        return noOfWordsToFind;
    }

    void destroyAllSelectors() {
        Array<GameObject> selectors = scene.findGameObjects("selector");
        for (GameObject selector : selectors) {
            removeGameObject(selector);
        }
    }

    public static void setLevelTime(float time) {
        levelTime = time;
    }

    public static float getLevelTime() {
        return levelTime;
    }

    public static class Level {
        private final String categoryName;
        private final Difficulty difficulty;

        private int[] dimensions;

        public Level(String categoryName, Difficulty difficulty) {
            this.categoryName = categoryName;
            this.difficulty = difficulty;

            init();
        }

        public Level(String categoryName) {
            this(categoryName, Difficulty.MEDIUM);
        }

        private void init() {
            dimensions = setDimensions();
        }

        private int[] setDimensions() {
            switch (difficulty) {
                case EASY:
                    return new int[]{8, 8};
                case MEDIUM:
                    return new int[]{10, 10};
                case HARD:
                    return new int[]{12, 12};
                case INSANE:
                    return new int[]{14, 14};
            }

            return new int[]{10, 10};
        }
    }
}
