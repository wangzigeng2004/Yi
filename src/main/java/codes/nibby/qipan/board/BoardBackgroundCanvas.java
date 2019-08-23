package codes.nibby.qipan.board;

import codes.nibby.qipan.config.Config;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * The bottom-most layer of the game board canvas stack.
 * This layer draws:
 * <ul>
 *     <li>Background image/color</li>
 *     <li>Line grid</li>
 *     <li>Co-ordinate labels</li>
 *     <li>Stone shadows</li>
 * </ul>
 *
 * Stone shadows are drawn in this layer because of stone placement
 * animations. Stones that are animated will be elevated to the
 * top-most layer (BoardInputCanvas). If shadows are included as
 * part of the stone drawing routine, then there will be shadow
 * overlay issues.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class BoardBackgroundCanvas extends Canvas {

    private static final DropShadow TEXTURE_SHADOW = new DropShadow();
    private static final Color TEXTURE_SHADOW_COLOR = Color.color(0.25d, 0.25d, 0.25d, 0.25d);
    private static final int TEXTURE_SHADOW_MARGIN = 10;

    private GameBoard gameBoard;
    private GraphicsContext g;

    public BoardBackgroundCanvas(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        g = getGraphicsContext2D();
    }

    public void render() {
        BoardMetrics metrics = gameBoard.getMetrics();
        double gridSize = metrics.getGridSize();
        int boardWidth = metrics.getBoardWidth();
        int boardHeight = metrics.getBoardHeight();
        double offsetX = metrics.getOffsetX();
        double offsetY = metrics.getOffsetY();
        double gridOffsetX = metrics.getGridOffsetX();
        double gridOffsetY = metrics.getGridOffsetY();
        double gap = metrics.getGap();

        g.clearRect(0, 0, getWidth(), getHeight());
        {

            // Draw the backdrop
            if (Config.boardTheme().shouldDrawBackground()) {
                Image backgroundTexture = Config.boardTheme().getBoardBackgroundTexture();
                g.drawImage(backgroundTexture, 0, 0, getWidth(), getHeight());
            }

            // Draw the board
            double width = gridSize * boardWidth;
            double height = gridSize * boardHeight;
            double x = getWidth() / 2 - width / 2;
            double y = getHeight() / 2 - height / 2;

            g.setEffect(TEXTURE_SHADOW);
            g.setFill(TEXTURE_SHADOW_COLOR);
            g.fillRect(x - TEXTURE_SHADOW_MARGIN, y - TEXTURE_SHADOW_MARGIN,
                    width + TEXTURE_SHADOW_MARGIN * 2, height + TEXTURE_SHADOW_MARGIN * 2);
            g.setEffect(null);
            if (Config.boardTheme().shouldDrawBoardTexture()) {
                Image boardTexture = Config.boardTheme().getBoardTexture();
                g.drawImage(boardTexture, x, y, width, height);
            }
        }

        g.setFill(Color.BLACK);
        g.setStroke(Color.BLACK);

        // Board lines
        for (int x = 0; x < boardWidth; x++) {
            g.strokeLine(metrics.getGridX(x),offsetY + gridOffsetY, metrics.getGridX(x),
                    metrics.getGridY(boardHeight - 1));
        }

        for (int y = 0; y < boardHeight; y++) {
            g.strokeLine(offsetX + gridOffsetX, metrics.getGridY(y),
                    metrics.getGridX(boardWidth - 1), metrics.getGridY(y));
        }

        // Board star points
        double dotSize = gridSize / 6;
        int centerDot = (boardWidth % 2 == 1) ? (boardWidth - 1) / 2 : -1;

        if (boardWidth == boardHeight && (boardWidth == 9 || boardWidth == 13 || boardWidth == 19)) {
            int corner = boardWidth == 9 ? 2 : 3;
            double grid = gridSize - gap;

            if (centerDot != -1)
                g.fillOval(metrics.getGridX(centerDot) - dotSize / 2,
                        metrics.getGridY(centerDot) - dotSize / 2, dotSize, dotSize);

            g.fillOval(gridOffsetX + offsetX + corner * grid - dotSize / 2,
                    gridOffsetY + offsetY + corner * grid - dotSize / 2, dotSize, dotSize);
            g.fillOval(gridOffsetX + offsetX + (boardWidth - corner - 1) * grid - dotSize / 2,
                    gridOffsetY + offsetY + corner * grid - dotSize / 2, dotSize, dotSize);
            g.fillOval(gridOffsetX + offsetX + corner * grid - dotSize / 2,
                    gridOffsetY + offsetY + (boardHeight - corner - 1) * grid - dotSize / 2, dotSize, dotSize);
            g.fillOval(gridOffsetX + offsetX + (boardWidth - corner - 1) * grid - dotSize / 2,
                    gridOffsetY + offsetY + (boardHeight - corner - 1) * grid - dotSize / 2, dotSize, dotSize);

            if (boardWidth == 19) {
                g.fillOval(gridOffsetX + offsetX + centerDot * grid - dotSize / 2,
                        gridOffsetY + offsetY + corner * grid - dotSize / 2, dotSize, dotSize);
                g.fillOval(gridOffsetX + offsetX + centerDot * grid - dotSize / 2,
                        gridOffsetY + offsetY + (boardHeight - corner - 1) * grid - dotSize / 2, dotSize, dotSize);
                g.fillOval(gridOffsetX + offsetX + corner * grid - dotSize / 2,
                        gridOffsetY + offsetY + centerDot * grid - dotSize / 2, dotSize, dotSize);
                g.fillOval(gridOffsetX + offsetX + (boardWidth - corner - 1) * grid - dotSize / 2,
                        gridOffsetY + offsetY + centerDot * grid - dotSize / 2, dotSize, dotSize);
            }
        }
    }
}
