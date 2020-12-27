package yi.component.gametree;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VerticalDirection;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.models.go.GameModel;
import yi.models.go.GameNode;
import yi.models.go.StandardGameRules;

@ExtendWith(ApplicationExtension.class)
public class GameTreeMouseInteractionTest extends GameTreeUITestBase {

    private final GameModel model = new GameModel(19, 19, StandardGameRules.CHINESE);

    private GameNode highlightedNode = null;
    private double offsetX = 0;
    private double offsetY = 0;

    @Start
    @Override
    public void startFx(Stage stage) {
        super.startFx(stage);

        treeViewer.addPreviewNodeChangeListener(newHighlight -> this.highlightedNode = newHighlight);
        var camera = treeViewer.getCamera();
        camera.addOffsetChangeListener(() -> {
            offsetX = camera.getOffsetX();
            offsetY = camera.getOffsetY();
        });
    }

    @Test
    public void testInteractsCorrectly(FxRobot robot) throws InterruptedException {
        new GameModel(19, 19, StandardGameRules.CHINESE);
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)
                .playMove(2, 0)
                .playMove(3, 0);

        treeViewer.setGameModel(model);

        testHoverSetsHighlightNode(robot);
        testClickSetsCurrentNode(robot);
        testScrollWheelAdjustsViewport(robot);
        testDragAdjustsViewport(robot);

        testSetNewCurrentNodeResetsHighlightedNode(robot);
    }

    private void testSetNewCurrentNodeResetsHighlightedNode(FxRobot robot) throws InterruptedException {
        for (int i = 0; i < 100; ++i) {
            model.submitPass();
        }
        Point2D mouseLocation = centerOnRootNode(robot);
        Thread.sleep(50);
        robot.moveTo(mouseLocation.getX() - 5, mouseLocation.getY() + 60);
        Thread.sleep(50);
        for (int i = 0; i < 20; ++i) {
            robot.clickOn(MouseButton.PRIMARY);
            Thread.sleep(10);
            Assertions.assertNull(highlightedNode, "highlightedNode not being reset " +
                    "to null when clicking continuously on the tree to update currentNode");
        }
    }

    private void testScrollWheelAdjustsViewport(FxRobot robot) {
        centerOnRootNode(robot);

        // As much as I'd like to test the scroll direction is correct,
        // some test agents on GitHub Actions have natural scrolling turned
        // on, which makes it impossible to tell which direction we should be
        // expecting.

        var offsetYBeforeScroll = offsetY;
        robot.scroll(20, VerticalDirection.DOWN);
        var offsetYAfterScroll = offsetY;
        boolean offsetYIncreased = offsetYAfterScroll > offsetYBeforeScroll;
        Assertions.assertTrue(offsetYAfterScroll != offsetYBeforeScroll,
                "Scrolling down on the game tree did nothing");

        offsetYBeforeScroll = offsetYAfterScroll;
        robot.scroll(20, VerticalDirection.UP);
        offsetYAfterScroll = offsetY;
        boolean offsetYIncreasedAgain = offsetYAfterScroll > offsetYBeforeScroll;
        Assertions.assertTrue(offsetYAfterScroll != offsetYBeforeScroll,
                "Scrolling up on the game tree did nothing");

        if (offsetYIncreased && offsetYIncreasedAgain) {
            Assertions.fail("Scrolling up and down both moved viewport in the " +
                    "same direction");
        }
    }

    private void testDragAdjustsViewport(FxRobot robot) throws InterruptedException {
        var startOffsetX = offsetX;
        var startOffsetY = offsetY;
        centerOnRootNode(robot);

        robot.press(MouseButton.PRIMARY);

        robot.moveTo(stage.getX()+stage.getWidth(), stage.getY() + stage.getHeight());
        Thread.sleep(100);
        Assertions.assertTrue(offsetX > startOffsetX,
                "OffsetX not correct after dragging to bottom right");
        Assertions.assertTrue(offsetY > startOffsetY,
                "OffsetY not correct after dragging to bottom right");
        startOffsetX = offsetX;
        startOffsetY = offsetY;

        robot.moveTo(stage.getX(), stage.getY());
        Thread.sleep(200);
        Assertions.assertTrue(offsetX < startOffsetX,
                "OffsetX not correct after dragging to top left");
        Assertions.assertTrue(offsetY < startOffsetY,
                "OffsetY not correct after dragging to top left");

        robot.release(MouseButton.PRIMARY);
    }

    private void testClickSetsCurrentNode(FxRobot robot) throws InterruptedException {
        var currentNode = model.getRootNode();

        do {
            Thread.sleep(100);
            Rectangle2D bounds = treeViewer.getElementBoundsForNode(currentNode).orElseThrow();
            moveToCenter(bounds, robot);
            robot.clickOn(MouseButton.PRIMARY);
            Thread.sleep(50);
            Assertions.assertEquals(currentNode, model.getCurrentNode());
            assert currentNode != null;
            if (currentNode.isLastMoveInThisVariation()) {
                break;
            } else {
                currentNode = currentNode.getNextNodeInMainBranch();
            }
        } while (true);
    }

    private void testHoverSetsHighlightNode(FxRobot robot) throws InterruptedException {
        var currentNode = model.getRootNode();

        do {
            GameNode nodeToHover = currentNode.getNextNodeInMainBranch();
            if (nodeToHover != null) {
                Rectangle2D bounds = treeViewer.getElementBoundsForNode(nodeToHover).orElseThrow();
                moveToCenter(bounds, robot);
                Thread.sleep(100);

                if (nodeToHover.isLastMoveInThisVariation()) {
                    break;
                } else {
                    Assertions.assertEquals(nodeToHover, highlightedNode);
                    currentNode = nodeToHover;
                }
            } else {
                break;
            }
        } while (true);

        Thread.sleep(75);
        robot.moveTo(stage.getX(), stage.getY());
        Thread.sleep(75);
        Assertions.assertNull(highlightedNode,
                "Highlighted node not set to null after mouse has moved outside of any" +
                        " node element bounds");
    }

    private Point2D centerOnRootNode(FxRobot robot) {
        treeViewer.getCamera().setCenterOnCoordinateImmediately(0, 0);
        Rectangle2D bounds = treeViewer.getElementBoundsForNode(model.getRootNode()).orElseThrow();
        return moveToCenter(bounds, robot);
    }

    private Point2D moveToCenter(Rectangle2D bounds, FxRobot robot) {
        double xCenter = bounds.getMinX() + bounds.getWidth() / 2 + stage.getX();
        double yCenter = bounds.getMinY() + bounds.getHeight() / 2 + stage.getY();
        robot.moveTo(xCenter, yCenter);
        return new Point2D(xCenter, yCenter);
    }
}
