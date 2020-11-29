package yi.editor;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import yi.common.Property;
import yi.common.PropertyListener;
import yi.common.utilities.GuiUtilities;
import yi.common.utilities.SystemUtilities;
import yi.component.YiScene;
import yi.component.board.GameBoardViewer;
import yi.component.gametree.GameTreeViewer;
import yi.component.gametree.GameTreeViewerSettings;
import yi.core.go.GameModel;
import yi.core.go.GameModelImporter;
import yi.core.go.GameParseException;
import yi.core.go.StandardGameRules;
import yi.editor.components.EditorBoardArea;
import yi.editor.components.EditorMenuBar;
import yi.editor.components.EditorPerspective;
import yi.editor.components.EditorToolBar;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.accelerator.EditorAcceleratorManager;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.settings.EditorSettings;

import java.io.IOException;
import java.util.Objects;

/**
 * The main frame for an editor session.
 */
public class EditorFrame extends Stage {

    private static final GameModel DEFAULT_MODEL = new GameModel(1, 1, StandardGameRules.CHINESE);

    private final Property<EditorPerspective> contentLayout = new Property<>(EditorPerspective.NONE);
    private final EditorActionManager actionManager;
    private final EditorMenuBar menuBar;
    private final EditorToolBar toolBar;

    private final EditorBoardArea boardArea;
    private final GameTreeViewer treeViewer;
    private final Property<GameModel> gameModel = new Property<>(DEFAULT_MODEL);

    private boolean addedMenuBarOnce = false;

    public EditorFrame(GameModel gameModel, EditorPerspective layout) {
        actionManager = new EditorActionManager(this);

        toolBar = new EditorToolBar();
        boardArea = new EditorBoardArea();
        enableDragAndDropToOpenFile(boardArea.getGameBoardViewer());

        var treeViewerSettings = new GameTreeViewerSettings();
        // TODO: Extract these out to settings.json
        treeViewerSettings.setBackgroundColor(GuiUtilities.getColor(43, 43, 43));
        treeViewerSettings.setNodeColor(GuiUtilities.getColor(90, 90, 90));
        treeViewerSettings.setNodeHoverColor(GuiUtilities.getColor(170, 170, 170));
        treeViewerSettings.setNodeInCurrentVariationColor(GuiUtilities.getColor(203, 203, 203));
        treeViewerSettings.setNodeWithCommentaryColor(GuiUtilities.getColor(87, 125, 186));
        treeViewerSettings.setCurrentNodeColor(GuiUtilities.getColor(255, 255, 255));

        treeViewer = new GameTreeViewer();
        treeViewer.setSettings(treeViewerSettings);

        addGameModelChangeListener(newModel -> {
            boardArea.setGameModel(newModel);
            treeViewer.setGameModel(newModel);
        });

        treeViewer.addHighlightedNodeChangeListener(boardArea::onHighlightedNodeChange);
        boardArea.getGameBoardViewer().addPreviewNodeChangeListener(newPreview -> {
            if (newPreview != treeViewer.getHighlightedNode()) {
                treeViewer.setHighlightedNode(newPreview);
            }
        });

        // Current action system implementation requires all actions (both shared and
        // instance-based) to be created prior to creating the menu bar. After this point
        // newly created actions will not be added to the menu bar.
        // TODO: Consider supporting late-comers to remove this temporal coupling...
        menuBar = new EditorMenuBar(actionManager);

        setPerspective(layout);
        setGameModel(gameModel);
        setTitle(EditorHelper.getProgramName());
    }

    private void enableDragAndDropToOpenFile(GameBoardViewer boardViewer) {
        boardViewer.setDragAndDropBehaviour(files -> {
            var success = false;
            if (files.size() == 1) {
                var file = files.get(0);
                try {
                    var importedGameModel = GameModelImporter.INSTANCE.fromFile(file.toPath());
                    setGameModel(importedGameModel);
                    success = true;
                } catch (GameParseException | IOException e) {
                    // TODO: Handle this in an error dialog so that users know something
                    //       went wrong with their file rather than throwing a stack trace
                    //       in their faces.
                    e.printStackTrace();
                }
            }
            return success;
        });
    }

    public void setGameModel(@NotNull GameModel newModel) {
        if (this.gameModel.get() != null) {
            this.gameModel.get().dispose();
        }
        this.gameModel.set(newModel);
    }

    public @NotNull GameModel getGameModel() {
        Objects.requireNonNull(gameModel.get(), "No game model is set. " +
                "setGameModel() must be called once before calling getGameModel().");

        return gameModel.get();
    }

    public void setPerspective(@NotNull EditorPerspective newLayout) {
        if (this.contentLayout.get() == newLayout) {
            return; // Avoid flickering when setting the same layout
        }
        var model = gameModel.get();
        boardArea.setContentForLayout(newLayout, model);

        var content = newLayout.getContent(this);

        var controlPane = new BorderPane();

        // Work around bug on macOS where setting the menu bar again causes the
        // menu bar to disappear. Possibly related to native code for system-style
        // menu bars...
        var addMenuBar = SystemUtilities.isMac() && !addedMenuBarOnce || !menuBar.isUseSystemMenuBar();
        if (addMenuBar) {
            controlPane.setTop(menuBar);
        }
        if (!addedMenuBarOnce) {
            addedMenuBarOnce = true;
        }
        controlPane.setCenter(toolBar);

        var container = new BorderPane();
        container.setTop(controlPane);
        container.setCenter(content);

        var currentScene = getScene();

        if (currentScene != null) {
            double currentHeight = currentScene.getHeight();
            double newAspectRatio = newLayout.getPreferredAspectRatio();
            double newWidth = currentHeight * newAspectRatio;

            var newScene = new YiScene(container, newWidth, currentHeight);
            setYiScene(newScene);
            setWidth(newWidth);
        } else {
            var startupSize = newLayout.getMinimumWindowSize();

            double startupWidth = startupSize.getWidth();
            double startupHeight = startupSize.getHeight();

            var newScene = new YiScene(container, startupWidth, startupHeight);
            setYiScene(newScene);
        }

        var minSize = newLayout.getMinimumWindowSize();
        setMinWidth(minSize.getWidth());
        setMinHeight(minSize.getHeight());

        if (getWidth() < minSize.getWidth()) {
            setWidth(minSize.getWidth());
        }
        if (getHeight() < minSize.getHeight()) {
            setHeight(minSize.getHeight());
        }

        this.contentLayout.set(newLayout);
        EditorSettings.general.setCurrentLayout(newLayout);
    }

    /**
     * Wrapper for {@link #setScene(Scene)} but also runs some custom routines.
     * It is highly recommended to use this method rather than the base {@link #setScene(Scene)}
     * so that scenes support as many editor features as possible.
     *
     * @param newScene Scene to set.
     */
    private void setYiScene(@NotNull YiScene newScene) {
        installUndoRedoAccelerators(newScene);
        setScene(newScene);
    }

    private void installUndoRedoAccelerators(YiScene newScene) {
        EditorAcceleratorManager.install(EditorAcceleratorId.UNDO, newScene, boardArea::requestUndo);
        EditorAcceleratorManager.install(EditorAcceleratorId.REDO, newScene, boardArea::requestRedo);
    }

    public Parent getBoardComponent() {
        return boardArea;
    }

    public Parent getTreeComponent() {
        return treeViewer.getComponent();
    }

    public @NotNull EditorPerspective getPerspective() {
        return contentLayout.get();
    }

    public EditorBoardArea getBoardArea() {
        return boardArea;
    }

    public void addContentLayoutChangeListener(PropertyListener<EditorPerspective> listener) {
        contentLayout.addListener(listener);
    }

    public void addGameModelChangeListener(PropertyListener<GameModel> listener) {
        gameModel.addListener(listener);
    }

    public EditorMenuBar getMainMenuBar() {
        return menuBar;
    }
}
