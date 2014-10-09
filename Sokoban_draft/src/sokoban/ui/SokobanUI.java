package sokoban.ui;

import application.Main;
import application.Main.SokobanPropertyType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import sokoban.file.SokobanFileLoader;
import sokoban.game.SokobanGameData;
import sokoban.game.SokobanGameStateManager;
import application.Main.SokobanPropertyType;
import properties_manager.PropertiesManager;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javax.swing.JScrollPane;

public class SokobanUI extends Pane {

    /**
     * The SokobanUIState represents the four screen states that are possible
     * for the Sokoban game application. Depending on which state is in current
     * use, different controls will be visible.
     */
    public enum SokobanUIState {

        SPLASH_SCREEN_STATE, PLAY_GAME_STATE, VIEW_STATS_STATE, VIEW_HELP_STATE,
        HANG1_STATE, HANG2_STATE, HANG3_STATE, HANG4_STATE, HANG5_STATE, HANG6_STATE,
    }

    // mainStage
    private Stage primaryStage;

    // mainPane
    private BorderPane mainPane;
    private BorderPane hmPane;

    // SplashScreen
    private ImageView splashScreenImageView;
    private StackPane splashScreenPane;
    private Label splashScreenImageLabel;
    private FlowPane levelSelectionPane;
    private ArrayList<Button> levelButtons;

    // NorthToolBar
    private HBox northToolbar;
    private Button gameButton;
    private Button statsButton;
    private Button helpButton;
    private Button exitButton;

    // GamePane
    private Label SokobanLabel;
    private BorderPane gamePanel = new BorderPane();

    //StatsPane
    private ScrollPane statsScrollPane;
    private JEditorPane statsPane;

    //HelpPane
    private BorderPane helpPanel;
    private JScrollPane helpScrollPane;
    private JEditorPane helpPane;
    private Button homeButton;
    private Pane workspace;

    // Padding
    private Insets marginlessInsets;

    // Image path
    private String ImgPath = "file:images/";

    // mainPane weight && height
    private int paneWidth;
    private int paneHeigth;

    // THIS CLASS WILL HANDLE ALL ACTION EVENTS FOR THIS PROGRAM
    private SokobanEventHandler eventHandler;
    private SokobanErrorHandler errorHandler;
    private SokobanDocumentManager docManager;
	private SokobanFileLoader fileLoader;

	//Level Data;
	private int numCols;
	private int numRows;
	double cellWidth;
	double cellHeight;
	private int[][] levelData;
	private int[] charPosition = new int[2];
	private ArrayList<int[]> boxPositions = new ArrayList<int[]>();
	private ArrayList<int[]> destinations = new ArrayList<int[]>();


	//Game Renderer
	GameRenderer gameRenderer;
	private GraphicsContext gc;

	//Handlers
	ArrowKeyHandler arrowKeyHandler;


    SokobanGameStateManager gsm;

    public SokobanUI() {
        gsm = new SokobanGameStateManager(this);
        eventHandler = new SokobanEventHandler(this);
        errorHandler = new SokobanErrorHandler(primaryStage);
        docManager = new SokobanDocumentManager(this);
		fileLoader = new SokobanFileLoader(this);
		arrowKeyHandler = new ArrowKeyHandler(this);
        initMainPane();
        initSplashScreen();
    }

    public void SetStage(Stage stage) {
        primaryStage = stage;
    }

    public BorderPane GetMainPane() {
        return this.mainPane;
    }

    public SokobanGameStateManager getGSM() {
        return gsm;
    }

    public SokobanDocumentManager getDocManager() {
        return docManager;
    }

    public SokobanErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public JEditorPane getHelpPane() {
        return helpPane;
    }

    public void initMainPane() {
        marginlessInsets = new Insets(5, 5, 5, 5);
        mainPane = new BorderPane();

        PropertiesManager props = PropertiesManager.getPropertiesManager();
        paneWidth = Integer.parseInt(props
                .getProperty(SokobanPropertyType.WINDOW_WIDTH));
        paneHeigth = Integer.parseInt(props
                .getProperty(SokobanPropertyType.WINDOW_HEIGHT));
        mainPane.resize(paneWidth, paneHeigth);
        mainPane.setPadding(marginlessInsets);
    }

    public void initSplashScreen() {

        // INIT THE SPLASH SCREEN CONTROLS
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String splashScreenImagePath = props
                .getProperty(SokobanPropertyType.SPLASH_SCREEN_IMAGE_NAME);
        props.addProperty(SokobanPropertyType.INSETS, "5");
        String str = props.getProperty(SokobanPropertyType.INSETS);

        splashScreenPane = new StackPane();

        Image splashScreenImage = loadImage(splashScreenImagePath);
        splashScreenImageView = new ImageView(splashScreenImage);

        splashScreenImageLabel = new Label();
        splashScreenImageLabel.setGraphic(splashScreenImageView);
        // move the label position to fix the pane
        splashScreenImageLabel.setLayoutX(-45);
        splashScreenPane.getChildren().add(splashScreenImageLabel);

        // GET THE LIST OF LEVEL OPTIONS
        ArrayList<String> levels = props
                .getPropertyOptionsList(SokobanPropertyType.LEVEL_OPTIONS);
        ArrayList<String> levelImages = props
                .getPropertyOptionsList(SokobanPropertyType.LEVEL_IMAGE_NAMES);
        ArrayList<String> levelFiles = props
                .getPropertyOptionsList(SokobanPropertyType.LEVEL_FILES);

        levelSelectionPane = new FlowPane();
        //levelSelectionPane.setSpacing(10.0);
		levelSelectionPane.setHgap(10.0);
		levelSelectionPane.setVgap(10.0);
        levelSelectionPane.setAlignment(Pos.CENTER);
        // add key listener
        levelButtons = new ArrayList<Button>();
        for (int i = 0; i < levels.size(); i++) {

            // GET THE LIST OF LEVEL OPTIONS
            String level = levels.get(i);
			String levelFile = levelFiles.get(i);
            String levelImageName = levelImages.get(i);
            Image levelImage = loadImage(levelImageName);
            ImageView levelImageView = new ImageView(levelImage);

            // AND BUILD THE BUTTON
            Button levelButton = new Button();
			levelButton.setStyle("-fx-background-color: transparent");
            levelButton.setGraphic(levelImageView);
            // CONNECT THE BUTTON TO THE EVENT HANDLER
            levelButton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					// TODO
					initSokobanUI();
					gsm.startNewGame();
					eventHandler.respondToSelectLevelRequest(levelFile);
				}
			});
            // TODO
            //levelSelectionPane.getChildren().add(levelButton);
            // TODO: enable only the first level
            //levelButton.setDisable(true);
			levelButton.setMinHeight(160);
			levelButton.setMinHeight(160);
			levelButton.setMaxWidth(160);
			levelButton.setMaxHeight(160);

			levelImageView.fitWidthProperty().bind(levelButton.maxWidthProperty());
			levelImageView.fitHeightProperty().bind(levelButton.maxHeightProperty());
			levelSelectionPane.getChildren().add(levelButton);
        }

        mainPane.setCenter(splashScreenPane);
		splashScreenPane.getChildren().add(levelSelectionPane);
    }

    /**
     * This method initializes the language-specific game controls, which
     * includes the three primary game screens.
     */
    public void initSokobanUI() {
        // FIRST REMOVE THE SPLASH SCREEN
        mainPane.getChildren().clear();

        // GET THE UPDATED TITLE
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String title = props.getProperty(SokobanPropertyType.GAME_TITLE_TEXT);
        primaryStage.setTitle(title);

        // THEN ADD ALL THE STUFF WE MIGHT NOW USE
        initNorthToolbar();

        // OUR WORKSPACE WILL STORE EITHER THE GAME, STATS,
        // OR HELP UI AT ANY ONE TIME
        initWorkspace();
        initGameScreen();
		initHandlers();
        //initStatsPane();
        //initHelpPane();

        // WE'LL START OUT WITH THE GAME SCREEN
        changeWorkspace(SokobanUIState.PLAY_GAME_STATE);

    }

    /**
     * This function initializes all the controls that go in the north toolbar.
     */

    private void initNorthToolbar() {
        // MAKE THE NORTH TOOLBAR, WHICH WILL HAVE FOUR BUTTONS
        northToolbar = new HBox();
        northToolbar.setStyle("-fx-background-color:lightgray");
        northToolbar.setAlignment(Pos.CENTER);
        northToolbar.setPadding(marginlessInsets);
        northToolbar.setSpacing(10.0);

        // MAKE AND INIT THE GAME BUTTON
        gameButton = initToolbarButton(northToolbar,
                SokobanPropertyType.GAME_IMG_NAME);
		//gameButton.setTooltip(new Tooltip(SokobanPropertyType.GAME_TOOLTIP.toString()));
        //setTooltip(gameButton, SokobanPropertyType.GAME_TOOLTIP);
        gameButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                eventHandler
                        .respondToSwitchScreenRequest(SokobanUIState.PLAY_GAME_STATE);
            }
        });

        // MAKE AND INIT THE STATS BUTTON
        statsButton = initToolbarButton(northToolbar,
                SokobanPropertyType.STATS_IMG_NAME);
        //setTooltip(statsButton, SokobanPropertyType.STATS_TOOLTIP);

        statsButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                eventHandler
                        .respondToSwitchScreenRequest(SokobanUIState.VIEW_STATS_STATE);
            }

        });
        // MAKE AND INIT THE HELP BUTTON
        helpButton = initToolbarButton(northToolbar,
                SokobanPropertyType.HELP_IMG_NAME);
        //setTooltip(helpButton, SokobanPropertyType.HELP_TOOLTIP);
        helpButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                eventHandler
                        .respondToSwitchScreenRequest(SokobanUIState.VIEW_HELP_STATE);
            }

        });

        // MAKE AND INIT THE EXIT BUTTON
        exitButton = initToolbarButton(northToolbar,
                SokobanPropertyType.EXIT_IMG_NAME);
        //setTooltip(exitButton, SokobanPropertyType.EXIT_TOOLTIP);
        exitButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                eventHandler.respondToExitRequest(primaryStage);
            }

        });

        // AND NOW PUT THE NORTH TOOLBAR IN THE FRAME
        mainPane.setTop(northToolbar);
        //mainPane.getChildren().add(northToolbar);
    }

    /**
     * This method helps to initialize buttons for a simple toolbar.
     *
     * @param toolbar The toolbar for which to add the button.
     *
     * @param prop The property for the button we are building. This will
     * dictate which image to use for the button.
     *
     * @return A constructed button initialized and added to the toolbar.
     */
    private Button initToolbarButton(HBox toolbar, SokobanPropertyType prop) {
        // GET THE NAME OF THE IMAGE, WE DO THIS BECAUSE THE
        // IMAGES WILL BE NAMED DIFFERENT THINGS FOR DIFFERENT LANGUAGES
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String imageName = props.getProperty(prop);

        // LOAD THE IMAGE
        Image image = loadImage(imageName);
        ImageView imageIcon = new ImageView(image);

        // MAKE THE BUTTON
        Button button = new Button();
        button.setGraphic(imageIcon);
        button.setPadding(marginlessInsets);

        // PUT IT IN THE TOOLBAR
        toolbar.getChildren().add(button);

        // AND SEND BACK THE BUTTON
        return button;
    }

    /**
     * The workspace is a panel that will show different screens depending on
     * the user's requests.
     */
    private void initWorkspace() {
        // THE WORKSPACE WILL GO IN THE CENTER OF THE WINDOW, UNDER THE NORTH
        // TOOLBAR
        workspace = new Pane();
        mainPane.setCenter(workspace);
        System.out.println("in the initWorkspace");
    }
	private void initGameScreen() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		changeWorkspace(SokobanUIState.PLAY_GAME_STATE);
	}
	public void initLevel(String level) {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		try {
			levelData = fileLoader.loadLevel(level);
			numCols = levelData.length;
			numRows = levelData[0].length;
			for(int i=0;i<levelData.length;i++) {
				for(int j=0;j<levelData[i].length;j++) {
					switch (levelData[i][j]) {
						case 3:
							int[] destination = new int[2];
							destination[0] = i;
							destination[1] = j;
							destinations.add(destination);
							break;
						case 4:
							charPosition[0] = i;
							charPosition[1] = j;
							break;
					}
					System.out.print(levelData[i][j] + " ");
				}
				System.out.println();
			}
			gameRenderer = new GameRenderer();
			gamePanel.setCenter(gameRenderer);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			errorHandler.processError(SokobanPropertyType.ERROR_INVALID_FILE);
		}

	}

	public void initHandlers() {
		mainPane.setOnKeyPressed((EventHandler<KeyEvent>)ke -> {
			arrowKeyHandler.keyPressed(ke);
		});
	}

	public void moveCharacterLeft() {
		if(levelData[charPosition[0]-1][charPosition[1]] == 5 || levelData[charPosition[0]-1][charPosition[1]] == 3 ) {
			levelData[charPosition[0]][charPosition[1]] = 5;
			/*for(int i=0;i<cellWidth;i++) {
				try {
					gameRenderer.repaint();
					int xcoord = (int)(charPosition[0] * cellWidth) - i;
					int ycoord = (int)(charPosition[1]*cellHeight);
					gameRenderer.drawCharacter(xcoord, ycoord);
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}*/
			charPosition[0] -=1;
			levelData[charPosition[0]][charPosition[1]] = 4;
		}
		else if(levelData[charPosition[0]-1][charPosition[1]] == 1) {

		}
		else if (levelData[charPosition[0]-1][charPosition[1]] == 2) {
			if(levelData[charPosition[0]-2][charPosition[1]] == 5 || levelData[charPosition[0]-2][charPosition[1]] == 3 ) {
				levelData[charPosition[0]-2][charPosition[1]] = 2;
				levelData[charPosition[0]][charPosition[1]] = 5;
				charPosition[0] -=1;
				levelData[charPosition[0]][charPosition[1]] = 4;
			}
		}
		gameRenderer.repaint();
		checkWin();
	}

	public void moveCharacterRight() {
		if(levelData[charPosition[0]+1][charPosition[1]] == 5 || levelData[charPosition[0]+1][charPosition[1]] == 3 ) {
			levelData[charPosition[0]][charPosition[1]] = 5;
			charPosition[0] +=1;
			levelData[charPosition[0]][charPosition[1]] = 4;
		}
		else if(levelData[charPosition[0]+1][charPosition[1]] == 1) {

		}
		else if (levelData[charPosition[0]+1][charPosition[1]] == 2) {
			if(levelData[charPosition[0]+2][charPosition[1]] == 5 || levelData[charPosition[0]+2][charPosition[1]] == 3 ) {
				levelData[charPosition[0]+2][charPosition[1]] = 2;
				levelData[charPosition[0]][charPosition[1]] = 5;
				charPosition[0] +=1;
				levelData[charPosition[0]][charPosition[1]] = 4;
			}
		}
		gameRenderer.repaint();
		checkWin();
	}

	public void moveCharacterUp() {
		if(levelData[charPosition[0]][charPosition[1]-1] == 5  || levelData[charPosition[0]][charPosition[1]-1] == 3) {
			levelData[charPosition[0]][charPosition[1]] = 5;
			charPosition[1] -=1;
			levelData[charPosition[0]][charPosition[1]] = 4;
		}
		else if(levelData[charPosition[0]][charPosition[1]-1] == 1) {

		}
		else if (levelData[charPosition[0]][charPosition[1]-1] == 2) {
			if(levelData[charPosition[0]][charPosition[1]-2] == 5 || levelData[charPosition[0]][charPosition[1]-2] == 3 ) {
				levelData[charPosition[0]][charPosition[1]-2] = 2;
				levelData[charPosition[0]][charPosition[1]] = 5;
				charPosition[1] -=1;
				levelData[charPosition[0]][charPosition[1]] = 4;
			}
		}
		gameRenderer.repaint();
		checkWin();
	}

	public void moveCharacterDown() {
		if(levelData[charPosition[0]][charPosition[1]+1] == 5  || levelData[charPosition[0]][charPosition[1]+1] == 3 ) {
			levelData[charPosition[0]][charPosition[1]] = 5;
			charPosition[1] +=1;
			levelData[charPosition[0]][charPosition[1]] = 4;
		}
		else if(levelData[charPosition[0]][charPosition[1]+1] == 1) {

		}
		else if (levelData[charPosition[0]][charPosition[1]+1] == 2) {
			if(levelData[charPosition[0]][charPosition[1]+2] == 5 || levelData[charPosition[0]][charPosition[1]+2] == 3 ) {
				levelData[charPosition[0]][charPosition[1]+2] = 2;
				levelData[charPosition[0]][charPosition[1]] = 5;
				charPosition[1] +=1;
				levelData[charPosition[0]][charPosition[1]] = 4;
			}
		}
		gameRenderer.repaint();
		checkWin();
	}

	public void checkWin() {
		boolean won = true;
		for(int i=0;i<destinations.size();i++) {
			int[] destData = destinations.get(i);
			int[] blockData = boxPositions.get(i);
			if(destData[0]!= blockData[0] || destData[1]!= blockData[1]) {
				won = false;
			}
		}
		if(won) {
			System.out.print("YOU WIN!");
		}
	}

	class GameRenderer extends Canvas {

		PropertiesManager props = PropertiesManager.getPropertiesManager();
		Image wallImage = new Image("file:images/wall.png");
		Image boxImage = new Image("file:images/box.png");
		Image placeImage = new Image("file:images/place.png");
		Image sokobanImage = new Image("file:images/Sokoban.png");

		public GameRenderer() {
			this.setWidth(800);
			this.setHeight(800);
			cellHeight = this.getHeight() / numRows;
			cellWidth = this.getWidth() / numCols;
			cellWidth = (cellHeight<cellWidth)?cellHeight:cellWidth;
			cellHeight = (cellHeight<cellWidth)?cellHeight:cellWidth;
			repaint();

		}

		public void repaint() {
			gc = this.getGraphicsContext2D();
			gc.clearRect(0,0,this.getWidth(), this.getHeight());
			int x = 0, y=0;
			x= (int)((this.getWidth() - (numCols*cellWidth))/2);

			gc.setFill(Color.DARKORANGE);
			gc.fillRect(0,0,this.getWidth(), this.getHeight());
			gc.setFill(Color.WHITE);
			boxPositions.clear();
			for(int i=0;i<numCols;i++) {
				y= (int)((this.getHeight() - (numRows*cellHeight))/2);
				for(int j=0;j<numRows;j++) {
					if(levelData[i][j] != 0) {
						gc.setFill(Color.WHITE);
						gc.fillRect(x,y,cellWidth,cellHeight);
					}
					switch (levelData[i][j]) {
						case 1:
							gc.drawImage(wallImage,x,y,cellWidth,cellHeight);
							break;
						case 2:
							int[] boxPosition = new int[2];
							boxPosition[0] = i;
							boxPosition[1] = j;
							boxPositions.add(boxPosition);
							gc.drawImage(boxImage, x, y, cellWidth, cellHeight);
							break;
						case 3:
							gc.drawImage(placeImage, x, y, cellWidth, cellHeight);
							break;
						case 4:
							gc.drawImage(sokobanImage,x,y,cellWidth,cellHeight);
							break;
						case 5:
							gc.setFill(Color.WHITE);
							gc.fillRect(x,y,cellWidth,cellHeight);
					}
					String numToDraw = "" + levelData[i][j];
					y+=cellHeight;
				}
				x+=cellWidth;
			}
			x= (int)((this.getWidth() - (numCols*cellWidth))/2);
			y=0;
			for(int i=0;i<destinations.size();i++) {
				int[] data = destinations.get(i);
				if(levelData[data[0]][data[1]] == 5) {
					int xcoord = x + (int) (data[0] * cellWidth);
					int ycoord = y + data[1] * (int) cellHeight;
					gc.drawImage(placeImage, xcoord, ycoord, cellWidth, cellHeight);
				}
				else if(levelData[data[0]][data[1]] == 4) {
					int xcoord = x + (int) (data[0] * cellWidth);
					int ycoord = y + data[1] * (int) cellHeight;
					gc.drawImage(placeImage, xcoord, ycoord, cellWidth, cellHeight);
					gc.drawImage(sokobanImage, xcoord, ycoord, cellWidth, cellHeight);
				}
				else if(levelData[data[0]][data[1]] == 2) {
					int xcoord = x + (int) (data[0] * cellWidth);
					int ycoord = y + data[1] * (int) cellHeight;
					gc.drawImage(placeImage, xcoord, ycoord, cellWidth, cellHeight);
					gc.drawImage(boxImage, xcoord, ycoord, cellWidth, cellHeight);
				}
			}
		}
		/*public void drawCharacter(int x, int y) {
			gc.drawImage(sokobanImage,x,y, cellWidth, cellHeight);
		}*/
	}

    public Image loadImage(String imageName) {
        Image img = new Image(ImgPath + imageName);
        return img;
    }

    /**
     * This function selects the UI screen to display based on the uiScreen
     * argument. Note that we have 3 such screens: game, stats, and help.
     *
     * @param uiScreen The screen to be switched to.
     */
    public void changeWorkspace(SokobanUIState uiScreen) {
        switch (uiScreen) {
            case VIEW_HELP_STATE:
                mainPane.setCenter(helpPanel);
                break;
            case PLAY_GAME_STATE:
                mainPane.setCenter(gamePanel);
                break;
            case VIEW_STATS_STATE:
                mainPane.setCenter(statsScrollPane);
                break;
            default:
        }

    }


}
