package yi.component.board.editmodes;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import yi.component.board.GameBoardManager;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import yi.component.board.edits.PlayMoveEdit;

import java.util.Optional;

final class PlayMoveEditMode extends AbstractEditMode {

    PlayMoveEditMode() { }

    @Override
    public void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY) {
        // TODO: Temporary. Probably should be disabled by default as the indicators are quite distracting.
        //       But can have this as a configurable preference.
//        if (manager.model.getCurrentGamePosition().getStoneColorAt(gridX, gridY) != GoStoneColor.NONE) {
//            // A stone already exists here, don't draw cursor.
//            return;
//        }
//
//        var nextTurnStoneColor = manager.model.getNextTurnStoneColor();
//        Color cursorColor = null;
//
//        if (nextTurnStoneColor == GoStoneColor.BLACK) {
//            cursorColor = Color.BLACK;
//        } else if (nextTurnStoneColor == GoStoneColor.WHITE) {
//            cursorColor = Color.WHITE;
//        }
//
//        if (cursorColor != null) {
//            double cursorSize = manager.size.getStoneSizeInPixels() / 2d;
//            double[] position = manager.size.getGridRenderPosition(gridX, gridY, cursorSize);
//
//            g.setFill(cursorColor);
//            g.fillRect(position[0], position[1], cursorSize, cursorSize);
//        }
    }

    @Override
    public Optional<Cursor> getMouseCursor() {
        return Optional.of(Cursor.HAND);
    }

    @Override
    public void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        var playMoveEdit = new PlayMoveEdit(gridX, gridY);
        manager.edit.recordAndApply(playMoveEdit, manager);
    }

    @Override
    public void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY) {

    }

    @Override
    public void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        // TODO: Feature - If the stone being dragged is the last move played, move the stone to the new position.
    }
}
