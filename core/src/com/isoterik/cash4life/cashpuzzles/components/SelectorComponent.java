package com.isoterik.cash4life.cashpuzzles.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.isoterik.cash4life.cashpuzzles.Cell;
import com.isoterik.cash4life.cashpuzzles.GamePlayScene;
import com.isoterik.cash4life.cashpuzzles.WordManager;
import com.isoterik.cash4life.cashpuzzles.utils.Board;
import io.github.isoteriktech.xgdx.Component;
import io.github.isoteriktech.xgdx.Transform;
import io.github.isoteriktech.xgdx.x2d.components.renderer.SpriteRenderer;

import java.util.ArrayList;
import java.util.Random;

public class SelectorComponent extends Component {
    private Transform transform;
    private SpriteRenderer spriteRenderer;

    private ArrayList<String> words;
    private Board board;

    private Vector2 initialSize;
    private Vector2 firstTouchPosition;
    private Vector2 mousePos;
    private Vector2 stopDragPosition;
    private Vector2[] magnitudePositions;

    private boolean flag = true;
    private boolean doOnce;

    private float keepAngle;
    private float size;

    public void setWords(ArrayList<String> words) {
        this.words = words;
    }

    @Override
    public void start() {
        transform = gameObject.transform;
        spriteRenderer = gameObject.getComponent(SpriteRenderer.class);

        initialSize = new Vector2(
                transform.getWidth(),
                transform.getHeight()
        );
        firstTouchPosition = Vector2.Zero;

        spriteRenderer.setColor(new Color(1,1,1,0.5f));
        spriteRenderer.setVisible(false);

        board = GameManager.getInstance().getBoard();
    }

    @Override
    public void update(float deltaTime) {
        if (input.isTouched()) {
            spriteRenderer.setVisible(true);
            mousePos = new Vector2(input.getTouchedX(), input.getTouchedY());

            if (flag) {
                firstTouchPosition = new Vector2(
                        mousePos.x,
                        mousePos.y
                );
                setMagnitudePositions();
                flag = false;
            }

            float angle = 0f;
            Vector2 magnitudePos = Vector2.Zero;
            // 2nd quadrant
            if (firstTouchPosition.x >= mousePos.x && firstTouchPosition.y <= mousePos.y) {
                float xDelta = firstTouchPosition.x - mousePos.x;
                float yDelta = -firstTouchPosition.y + mousePos.y;
                float div = yDelta / xDelta;
                angle = 90 - (float) Math.toDegrees(Math.atan(div));

                magnitudePos = magnitudePositions[0];
            }
            // 3rd quadrant
            else if (firstTouchPosition.x >= mousePos.x && firstTouchPosition.y >= mousePos.y) {
                float xDelta = firstTouchPosition.x - mousePos.x;
                float yDelta = firstTouchPosition.y - mousePos.y;
                float div = yDelta / xDelta;
                angle = 90 + (float) Math.toDegrees(Math.atan(div));

                magnitudePos = magnitudePositions[1];
            }
            // 4th quadrant
            else if (firstTouchPosition.x <= mousePos.x && firstTouchPosition.y >= mousePos.y) {
                float xDelta = -firstTouchPosition.x + mousePos.x;
                float yDelta = firstTouchPosition.y - mousePos.y;
                float div = yDelta / xDelta;
                angle = 270 - (float) Math.toDegrees(Math.atan(div));

                magnitudePos = magnitudePositions[2];
            }
            // 1st quadrant
            else if (firstTouchPosition.x <= mousePos.x && firstTouchPosition.y <= mousePos.y) {
                float xDelta = -firstTouchPosition.x + mousePos.x;
                float yDelta = -firstTouchPosition.y + mousePos.y;
                float div = yDelta / xDelta;
                angle = 270 + (float) Math.toDegrees(Math.atan(div));

                magnitudePos = magnitudePositions[3];
            }

            transform.setRotation(angle);

            float magnitude = mousePos.dst(magnitudePos);
            transform.setSize(transform.getWidth(), Math.max(magnitude, initialSize.y));

            doOnce = true;
        }

        if (! input.isTouched()) {
            flag = true;
            //spriteRenderer.setVisible(false);
            //transform.setRotation(0.0f);
            //transform.setSize(initialSize.x, initialSize.y);

            if (doOnce) {
                checkValid();
                doOnce = false;
            }
        }
    }

    public void setSize(float size) {
        this.size = size;
    }

    private void setMagnitudePositions() {
        magnitudePositions = new Vector2[]{
                new Vector2(
                        firstTouchPosition.x + (initialSize.x / 2),
                        firstTouchPosition.y - (initialSize.x / 2)
                ),
                new Vector2(
                        firstTouchPosition.x,
                        firstTouchPosition.y + (initialSize.y / 2f)
                ),
                new Vector2(
                        firstTouchPosition.x - (initialSize.x / 2),
                        firstTouchPosition.y + (initialSize.x / 2)
                ),
                new Vector2(
                        firstTouchPosition.x,
                        firstTouchPosition.y - (initialSize.y / 2f)
                )
        };
    }

    private boolean withinBounds(Vector2 target, Vector2 primary) {
        //float size = 0.3f;
        boolean xBounded = target.x >= primary.x && target.x <= primary.x + size;
        boolean yBounded = target.y >= primary.y && target.y <= primary.y + size;

        return xBounded && yBounded;
    }

    private void checkValid() {
        Cell start = null, stop = null;
        Cell[][] cells = board.getCells();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                Cell cell = cells[i][j];
                Vector2 cellPosition = cell.getPosition();
                if (withinBounds(firstTouchPosition, cellPosition))
                    start = cell;
                if (withinBounds(mousePos, cellPosition))
                    stop = cell;
            }
        }

        if (start == null || stop == null) {
            notValid();
        }
        else {
            valid(start, stop);
        }
    }

    private void notValid() {
        //removeComponent(SelectorController.class);
        removeGameObject(gameObject);
    }

    private void validate(String selection, float angle) {
        if (words.contains(selection) && !WordManager.getInstance().getFoundWords().contains(selection)) {
            keepAngle = angle;
            WordManager.getInstance().getFoundWords().add(selection);
            keep();

            if (WordManager.getInstance().getFoundWords().size() == words.size()) {
                GameManager.getInstance().currentLevelFinished();
            }
        }
        else {
            notValid();
        }
    }

    private void valid(Cell start, Cell stop) {
        Cell[][] cells = board.getCells();

        int startRow = start.getRow(), startColumn = start.getColumn();
        int stopRow = stop.getRow(), stopColumn = stop.getColumn();

        stopDragPosition = stop.getPosition();

        // Vertical up
        if (startColumn == stopColumn && startRow > stopRow) {
            StringBuilder selection = new StringBuilder();
            int currPosX = startRow;
            while (currPosX >= stopRow) {
                char letter = cells[currPosX][startColumn].getLetter();
                selection.append(letter);
                --currPosX;
            }

            validate(selection.toString(), 0f);
        }
        // Vertical down
        else if (startColumn == stopColumn && startRow < stopRow) {
            StringBuilder selection = new StringBuilder();
            int currPosX = startRow;
            while (currPosX <= stopRow) {
                char letter = cells[currPosX][startColumn].getLetter();
                selection.append(letter);
                ++currPosX;
            }

            validate(selection.toString(), 180f);
        }
        // Horizontal right
        else if (startRow == stopRow && startColumn < stopColumn) {
            StringBuilder selection = new StringBuilder();
            int currPosY = startColumn;
            while (currPosY <= stopColumn) {
                char letter = cells[startRow][currPosY].getLetter();
                selection.append(letter);
                ++currPosY;
            }

            validate(selection.toString(), 270f);
        }
        // Horizontal left
        else if (startRow == stopRow && startColumn > stopColumn) {
            StringBuilder selection = new StringBuilder();
            int currPosY = startColumn;
            while (currPosY >= stopColumn) {
                char letter = cells[startRow][currPosY].getLetter();
                selection.append(letter);
                --currPosY;
            }

            validate(selection.toString(), 90f);
        }
        else {
            boolean b = Math.abs(startRow - stopRow) == Math.abs(startColumn - stopColumn);
            // Diagonal up /
            if (startRow > stopRow && startColumn < stopColumn) {
                if (b) {
                    StringBuilder selection = new StringBuilder();
                    int currPosX = startRow, currPosY = startColumn;
                    while (currPosX >= stopRow && currPosY <= stopColumn) {
                        char letter = cells[currPosX][currPosY].getLetter();
                        selection.append(letter);
                        --currPosX; ++currPosY;
                    }

                    validate(selection.toString(), 315f);
                }
                else {
                    notValid();
                }
            }
            // Diagonal down /
            else if (startRow < stopRow && startColumn > stopColumn) {
                if (b) {
                    StringBuilder selection = new StringBuilder();
                    int currPosX = startRow, currPosY = startColumn;
                    while (currPosX <= stopRow && currPosY >= stopColumn) {
                        char letter = cells[currPosX][currPosY].getLetter();
                        selection.append(letter);
                        ++currPosX; --currPosY;
                    }

                    validate(selection.toString(), 135f);
                }
                else {
                    notValid();
                }
            }
            // Diagonal up \
            else if (startRow > stopRow) {
                if (b) {
                    StringBuilder selection = new StringBuilder();
                    int currPosX = startRow, currPosY = startColumn;
                    while (currPosX >= stopRow && currPosY >= stopColumn) {
                        char letter = cells[currPosX][currPosY].getLetter();
                        selection.append(letter);
                        --currPosX; --currPosY;
                    }

                    validate(selection.toString(), 45f);
                }
                else {
                    notValid();
                }
            }
            // Diagonal down \
            else if (startRow < stopRow) {
                if (b) {
                    StringBuilder selection = new StringBuilder();
                    int currPosX = startRow, currPosY = startColumn;
                    while (currPosX <= stopRow && currPosY <= stopColumn) {
                        char letter = cells[currPosX][currPosY].getLetter();
                        selection.append(letter);
                        ++currPosX; ++currPosY;
                    }

                    validate(selection.toString(), 225f);
                }
                else {
                    notValid();
                }
            }
            else {
                notValid();
            }
        }
    }

    private Color getRandomColor() {
        float opacity = 0.5f;
        Color[] colors = {
                new Color(1.0f, 0.0f, 0.0f, opacity),
                new Color(0.0f, 1.0f, 0.0f, opacity),
                new Color(1.0f, 0.0f, 1.0f, opacity),
                new Color(1.0f, 1.0f, 0.0f, opacity),
                new Color(1.0f, 0.0f, 1.0f, opacity),
                new Color(0.0f, 1.0f, 1.0f, opacity)
        };

        return colors[new Random().nextInt(colors.length)];
    }

    public void keep() {
        float magnitude = firstTouchPosition.dst(stopDragPosition);
        //transform.setSize(transform.getWidth(), magnitude);
        transform.setRotation(keepAngle);
        getComponent(SpriteRenderer.class).setColor(getRandomColor());
        removeComponent(SelectorComponent.class);
    }
}