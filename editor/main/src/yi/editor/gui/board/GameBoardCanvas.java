package yi.editor.gui.board;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import yi.core.go.GoGameModel;

/**
 * Represents one rendered content layer on the board.
 */
abstract class GameBoardCanvas extends Canvas {

    private final GraphicsContext graphics;
    protected final GameBoardManager manager;

    GameBoardCanvas(GameBoardManager manager) {
        this.graphics = getGraphicsContext2D();

        this.manager = manager;
        this.manager.addGameUpdateListener(() -> render(manager));
    }

    void render(GameBoardManager manager) {
        _render(graphics, manager);
    }

    protected abstract void _render(GraphicsContext g, GameBoardManager manager);

    public abstract void onGameModelSet(GoGameModel model, GameBoardManager manager);

    public abstract void onGameUpdate(GoGameModel game, GameBoardManager manager);
}
