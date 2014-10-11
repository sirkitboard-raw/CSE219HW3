package sokoban.ui;

import application.Main.SokobanPropertyType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JEditorPane;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.util.Duration;
import sokoban.file.SokobanFileLoader;
import sokoban.game.SokobanGameData;
import sokoban.game.SokobanGameStateManager;
import properties_manager.PropertiesManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;

import javafx.scene.paint.Color;
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
	private int xOffset;
	private int yOffset;
	// SplashScreen
	private ImageView splashScreenImageView;
	private StackPane splashScreenPane;
	private Label splashScreenImageLabel;
	private FlowPane levelSelectionPane;
	private ArrayList<Button> levelButtons;

	// NorthToolBar
	private HBox northToolbar;
	private Button backButton;
	private Button statsButton;
	private Button undoButton;
	private Button timeButton;

	// GamePane
	private Label SokobanLabel;
	private BorderPane gamePanel = new BorderPane();
	private BorderPane statsPanel = new BorderPane();

	//StatsPane
	private ScrollPane statsScrollPane;
	private WebView statsPane;

	//HelpPane
	private BorderPane helpPanel;
	private ScrollPane helpScrollPane;
	private JEditorPane helpPane;
	private Button homeButton;
	private Pane workspace;
	private StackPane gameStack;

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
	public double cellWidth;
	public double cellHeight;
	private int[][] levelData;
	private int[] charPosition = new int[2];
	private ArrayList<int[]> boxPositions = new ArrayList<int[]>();
	private ArrayList<int[]> destinations = new ArrayList<int[]>();
	//Game Renderer
	GameRenderer gameRenderer;
	private GraphicsContext gc;
	private Stack<int[]> charMoves;
	private Stack<int[][]> boxPositionsStack;
	//Handlers
	ArrowKeyHandler arrowKeyHandler;
	MouseHandler mouseHandler;
	private Integer level;
	private static final Integer STARTTIMESECONDS = 0;
	private Timeline timeline;
	private IntegerProperty timeSeconds = new SimpleIntegerProperty(STARTTIMESECONDS);

	SokobanGameStateManager gsm;

	//AudioFiles
	AudioClip intro;
	AudioClip move;
	AudioClip win;
	AudioClip lose;
	AudioClip bump;
	public SokobanUI() {
		gsm = new SokobanGameStateManager(this);
		eventHandler = new SokobanEventHandler(this);
		errorHandler = new SokobanErrorHandler(primaryStage);
		docManager = new SokobanDocumentManager(this);
		fileLoader = new SokobanFileLoader(this);
		arrowKeyHandler = new ArrowKeyHandler(this);
		mouseHandler = new MouseHandler(this);
		initMainPane();
		initAudio();
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

	public SokobanFileLoader getFileLoader() {
		return fileLoader;
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

	public void initAudio() {
		try {
			File file = new File("audio/intro.mp3");
			intro = new AudioClip(file.toURI().toURL().toString());
			move = new AudioClip(new File("audio/charmoved.wav").toURI().toURL().toString());
			bump = new AudioClip(new File("audio/invalidmove.wav").toURI().toURL().toString());
			win = new AudioClip(new File("audio/youwin.wav").toURI().toURL().toString());
			lose = new AudioClip(new File("audio/failsound.mp3").toURI().toURL().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void initSplashScreen() {
		intro.play();
		mainPane.getChildren().clear();
		// INIT THE SPLASH SCREEN CONTROLS
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		String splashScreenImagePath = props
				.getProperty(SokobanPropertyType.SPLASH_SCREEN_IMAGE_NAME);
		props.addProperty(SokobanPropertyType.INSETS, "5");

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
			int levelNum = i;
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
					SokobanUI.this.level = levelNum;
					intro.stop();
					eventHandler.respondToNewGameRequest(levelNum+1);
					eventHandler.respondToSelectLevelRequest(levelFile);
					initSokobanUI();
				}
			});
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
		backButton = initToolbarButton(northToolbar,
				SokobanPropertyType.HOME_BUTTON);
		backButton.setStyle("-fx-background-color: transparent");
		//backButton.setTooltip(new Tooltip(SokobanPropertyType.GAME_TOOLTIP.toString()));
		//setTooltip(backButton, SokobanPropertyType.GAME_TOOLTIP);
		backButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				eventHandler.respondToExitRequest(primaryStage);
			}
		});

		// MAKE AND INIT THE STATS BUTTON
		statsButton = initToolbarButton(northToolbar,
				SokobanPropertyType.STAT_BUTTON);
		//setTooltip(statsButton, SokobanPropertyType.STATS_TOOLTIP);
		statsButton.setStyle("-fx-background-color: transparent");
		statsButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (statsPanel == mainPane.getCenter()) {
					statsButton.setGraphic(new ImageView(loadImage("stats.png")));
					eventHandler.respondToSwitchScreenRequest(SokobanUIState.PLAY_GAME_STATE);
				} else {
					statsButton.setGraphic(new ImageView(loadImage("back.png")));
					eventHandler
							.respondToSwitchScreenRequest(SokobanUIState.VIEW_STATS_STATE);
				}
			}

		});
		// MAKE AND INIT THE HELP BUTTON
		undoButton = initToolbarButton(northToolbar,
				SokobanPropertyType.UNDO_BUTTON);
		//setTooltip(undoButton, SokobanPropertyType.HELP_TOOLTIP);
		undoButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				undo();
			}

		});
		undoButton.setStyle("-fx-background-color: transparent");
		// MAKE AND INIT THE EXIT BUTTON
		// MAKE THE BUTTON
		timeButton = new Button();
		timeButton.setPadding(marginlessInsets);

		// PUT IT IN THE TOOLBAR
		northToolbar.getChildren().add(timeButton);
		timeButton.setMinHeight(79);
		timeButton.setMinWidth(309);
		timeButton.textProperty().bind(timeSeconds.asString());
		timeButton.setTextFill(Color.DARKGRAY);
		timeButton.setStyle("-fx-background-image: url(\'http://i.imgur.com/xcNLfql.png\');-fx-font-size: 4em;");
		//setTooltip(exitButton, SokobanPropertyType.EXIT_TOOLTIP);
		if (timeline != null) {
			timeline.stop();
		}
		timeSeconds.set(STARTTIMESECONDS);
		timeline = new Timeline();
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.seconds(101),
						new KeyValue(timeSeconds, 100))
		);
		timeline.playFromStart();
		// AND NOW PUT THE NORTH TOOLBAR IN THE FRAME
		northToolbar.setStyle("-fx-background-color: #3b3c51");
		mainPane.setTop(northToolbar);
		//mainPane.getChildren().add(northToolbar);
	}

	/**
	 * This method helps to initialize buttons for a simple toolbar.
	 *
	 * @param toolbar The toolbar for which to add the button.
	 * @param prop    The property for the button we are building. This will
	 *                dictate which image to use for the button.
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
			destinations.clear();
			boxPositions.clear();
			numCols = levelData.length;
			numRows = levelData[0].length;
			for (int i = 0; i < levelData.length; i++) {
				for (int j = 0; j < levelData[i].length; j++) {
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
			charMoves = new Stack<int[]>();
			boxPositionsStack = new Stack<int[][]>();
			gameStack = new StackPane();
			gameRenderer = new GameRenderer();
			gameStack.getChildren().add(gameRenderer);
			gamePanel.setCenter(gameStack);
			arrowKeyHandler.enabled = true;
			mouseHandler.enabled = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			errorHandler.processError(SokobanPropertyType.ERROR_INVALID_FILE);
		}

	}

	public void initStatsPane() {
		statsPane = new WebView();
		WebEngine engine = statsPane.getEngine();
		helpScrollPane = new ScrollPane();

		// MAKE THE HELP BUTTON
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		// WE'LL PUT THE HOME BUTTON IN A TOOLBAR IN THE NORTH OF THIS SCREEN,
		// UNDER THE NORTH TOOLBAR THAT'S SHARED BETWEEN THE THREE SCREENS
		Pane helpToolbar = new Pane();
		helpPanel = new BorderPane();
		//helpPanel.setLayout(new BorderLayout());
		helpPanel.setTop(helpToolbar);

		statsPanel.setCenter(statsPane);

		// LOAD THE HELP PAGE
		loadPage(engine,"statsHTML.html");
		// LET OUR HELP PAGE GO HOME VIA THE HOME BUTTON
	}

	public void initHandlers() {
		mainPane.setOnKeyPressed((EventHandler<KeyEvent>) ke -> {
			arrowKeyHandler.keyPressed(ke);
		});
		//gameRenderer.setOnMouseClicked((EventHandler<MouseEvent>)e -> mouseHandler.mouseClicked(e));
		gameRenderer.setOnMouseDragged((EventHandler<MouseEvent>) e -> mouseHandler.mouseDragged(e));
		gameRenderer.setOnMouseReleased((EventHandler<MouseEvent>) e -> mouseHandler.endDrag(e));
	}

	public void loadPage(WebEngine jep, String fileName) {
		// GET THE FILE NAME
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		System.out.println(fileName);
		try {
			jep.load(new File("data/"+fileName).toURI().toURL().toExternalForm());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void moveCharacterLeft() {
		int[][] temp = new int[numCols][numRows];
		for (int i = 0; i < numCols; i++) {
			for (int j = 0; j < numRows; j++) {
				temp[i][j] = levelData[i][j];
			}
		}
		int[] temp2 = {charPosition[0], charPosition[1]};
		charMoves.push(temp2);
		boxPositionsStack.push(temp);
		if (levelData[charPosition[0] - 1][charPosition[1]] == 5 || levelData[charPosition[0] - 1][charPosition[1]] == 3) {
			arrowKeyHandler.enabled = false;
			mouseHandler.enabled = false;
			levelData[charPosition[0]][charPosition[1]] = 5;
			Image image = loadImage("Sokoban.png");
			ImageView soko = new ImageView(image);
			System.out.print(soko.getX() + " ");
			System.out.print(soko.getY());
			soko.setFitHeight(cellHeight);
			soko.setFitWidth(cellWidth);
			gameStack.getChildren().add(soko);
			TranslateTransition tt = new TranslateTransition(Duration.millis(500), soko);
			int x = (int)(charPosition[0]*cellWidth - gameStack.getWidth()/2+(cellWidth/2) + xOffset);
			int y = (int)(charPosition[1]*cellHeight - gameStack.getHeight()/2+ (cellHeight/2) + yOffset);
			int tox = (int)((charPosition[0]-1)*cellWidth - gameStack.getWidth()/2 + cellWidth/2 + xOffset);
			System.out.print(x + " " + y + " " + tox);
			tt.setFromX(x);
			tt.setFromY(y);
			tt.setToX(tox);
			tt.setToY(y);
			tt.play();
			tt.setOnFinished(e -> {
				charPosition[0] -= 1;
				levelData[charPosition[0]][charPosition[1]] = 4;
				gameStack.getChildren().remove(soko);
				gameRenderer.repaint();
				checkWin();
				checkLose();
				arrowKeyHandler.enabled = true;
				mouseHandler.enabled = true;
			});
			move.play();
		} else if (levelData[charPosition[0] - 1][charPosition[1]] == 1) {
			bump.play();
		} else if (levelData[charPosition[0] - 1][charPosition[1]] == 2) {
			if (levelData[charPosition[0] - 2][charPosition[1]] == 5 || levelData[charPosition[0] - 2][charPosition[1]] == 3) {
				arrowKeyHandler.enabled = false;
				mouseHandler.enabled = false;
				levelData[charPosition[0]][charPosition[1]] = 5;
				levelData[charPosition[0]-1][charPosition[1]] = 5;
				Image image = loadImage("Sokoban.png");
				ImageView soko = new ImageView(image);
				ImageView box = new ImageView(loadImage("box.png"));
				System.out.print(soko.getX() + " ");
				System.out.print(soko.getY());
				soko.setFitHeight(cellHeight);
				soko.setFitWidth(cellWidth);
				box.setFitHeight(cellHeight);
				box.setFitWidth(cellWidth);
				gameStack.getChildren().add(soko);
				gameStack.getChildren().add(box);
				TranslateTransition tt = new TranslateTransition(Duration.millis(500), soko);
				TranslateTransition tt2 = new TranslateTransition(Duration.millis(500), box);
				int x = (int)(charPosition[0]*cellWidth - gameStack.getWidth()/2+(cellWidth/2) + xOffset);
				int y = (int)(charPosition[1]*cellHeight - gameStack.getHeight()/2+ (cellHeight/2) + yOffset);
				int tox = (int)((charPosition[0]-1)*cellWidth - gameStack.getWidth()/2 + cellWidth/2 + xOffset);
				System.out.print(x + " " + y + " " + tox);
				tt.setFromX(x);
				tt.setFromY(y);
				tt.setToX(tox);
				tt.setToY(y);
				tt2.setFromX(x - cellWidth);
				tt2.setFromY(y);
				tt2.setToX(tox - cellWidth);
				tt2.setToY(y);
				tt.play();
				tt2.play();
				tt.setOnFinished(e -> {
					levelData[charPosition[0]-2][charPosition[1]] = 2;
					charPosition[0] -= 1;
					levelData[charPosition[0]][charPosition[1]] = 4;
					gameStack.getChildren().remove(soko);
					gameStack.getChildren().remove(box);
					gameRenderer.repaint();
					checkWin();
					checkLose();
					arrowKeyHandler.enabled = true;
					mouseHandler.enabled = true;
				});
				move.play();
			}
			else {
				bump.play();
			}

		}
		else {
			bump.play();
		}
		gameRenderer.repaint();
	}

	public void moveCharacterRight() {
		int[][] temp = new int[numCols][numRows];
		for (int i = 0; i < numCols; i++) {
			for (int j = 0; j < numRows; j++) {
				temp[i][j] = levelData[i][j];
			}
		}
		int[] temp2 = {charPosition[0], charPosition[1]};
		charMoves.push(temp2);
		boxPositionsStack.push(temp);
		if (levelData[charPosition[0] + 1][charPosition[1]] == 5 || levelData[charPosition[0] + 1][charPosition[1]] == 3) {
			levelData[charPosition[0]][charPosition[1]] = 5;
			arrowKeyHandler.enabled = false;
			mouseHandler.enabled = false;
			levelData[charPosition[0]][charPosition[1]] = 5;
			Image image = loadImage("Sokoban.png");
			ImageView soko = new ImageView(image);
			System.out.print(soko.getX() + " ");
			System.out.print(soko.getY());
			soko.setFitHeight(cellHeight);
			soko.setFitWidth(cellWidth);
			gameStack.getChildren().add(soko);
			TranslateTransition tt = new TranslateTransition(Duration.millis(500), soko);
			int x = (int)(charPosition[0]*cellWidth - gameStack.getWidth()/2+(cellWidth/2) + xOffset);
			int y = (int)(charPosition[1]*cellHeight - gameStack.getHeight()/2+ (cellHeight/2) + yOffset);
			int tox = (int)((charPosition[0]+1)*cellWidth - gameStack.getWidth()/2 + cellWidth/2 + xOffset);
			System.out.print(x + " " + y + " " + tox);
			tt.setFromX(x);
			tt.setFromY(y);
			tt.setToX(tox);
			tt.setToY(y);
			tt.play();
			tt.setOnFinished(e -> {
				charPosition[0] += 1;
				levelData[charPosition[0]][charPosition[1]] = 4;
				gameStack.getChildren().remove(soko);
				gameRenderer.repaint();
				checkWin();
				checkLose();
				arrowKeyHandler.enabled = true;
				mouseHandler.enabled = true;
			});
			move.play();
		} else if (levelData[charPosition[0] + 1][charPosition[1]] == 1) {
			bump.play();
		} else if (levelData[charPosition[0] + 1][charPosition[1]] == 2) {
			if (levelData[charPosition[0] + 2][charPosition[1]] == 5 || levelData[charPosition[0] + 2][charPosition[1]] == 3) {
				arrowKeyHandler.enabled = false;
				mouseHandler.enabled = false;
				levelData[charPosition[0]][charPosition[1]] = 5;
				levelData[charPosition[0]+1][charPosition[1]] = 5;
				Image image = loadImage("Sokoban.png");
				ImageView soko = new ImageView(image);
				ImageView box = new ImageView(loadImage("box.png"));
				System.out.print(soko.getX() + " ");
				System.out.print(soko.getY());
				soko.setFitHeight(cellHeight);
				soko.setFitWidth(cellWidth);
				box.setFitHeight(cellHeight);
				box.setFitWidth(cellWidth);
				gameStack.getChildren().add(soko);
				gameStack.getChildren().add(box);
				TranslateTransition tt = new TranslateTransition(Duration.millis(500), soko);
				TranslateTransition tt2 = new TranslateTransition(Duration.millis(500), box);
				int x = (int)(charPosition[0]*cellWidth - gameStack.getWidth()/2+(cellWidth/2) + xOffset);
				int y = (int)(charPosition[1]*cellHeight - gameStack.getHeight()/2+ (cellHeight/2) + yOffset);
				int tox = (int)((charPosition[0]+1)*cellWidth - gameStack.getWidth()/2 + cellWidth/2 + xOffset);
				System.out.print(x + " " + y + " " + tox);
				tt.setFromX(x);
				tt.setFromY(y);
				tt.setToX(tox);
				tt.setToY(y);
				tt2.setFromX(x + cellWidth);
				tt2.setFromY(y);
				tt2.setToX(tox + cellWidth);
				tt2.setToY(y);
				tt.play();
				tt2.play();
				tt.setOnFinished(e -> {
					levelData[charPosition[0]+2][charPosition[1]] = 2;
					charPosition[0] += 1;
					levelData[charPosition[0]][charPosition[1]] = 4;
					gameStack.getChildren().remove(soko);
					gameStack.getChildren().remove(box);
					gameRenderer.repaint();
					checkWin();
					checkLose();
					arrowKeyHandler.enabled = true;
					mouseHandler.enabled = true;
				});
				move.play();
			}

			else {
				bump.play();
			}

		}

		else {
			bump.play();
		}
		gameRenderer.repaint();
	}

	public void moveCharacterUp() {
		int[][] temp = new int[numCols][numRows];
		for (int i = 0; i < numCols; i++) {
			for (int j = 0; j < numRows; j++) {
				temp[i][j] = levelData[i][j];
			}
		}
		int[] temp2 = {charPosition[0], charPosition[1]};
		charMoves.push(temp2);
		boxPositionsStack.push(temp);
		if (levelData[charPosition[0]][charPosition[1] - 1] == 5 || levelData[charPosition[0]][charPosition[1] - 1] == 3) {
			arrowKeyHandler.enabled = false;
			mouseHandler.enabled = false;
			levelData[charPosition[0]][charPosition[1]] = 5;
			Image image = loadImage("Sokoban.png");
			ImageView soko = new ImageView(image);
			System.out.print(soko.getX() + " ");
			System.out.print(soko.getY());
			soko.setFitHeight(cellHeight);
			soko.setFitWidth(cellWidth);
			gameStack.getChildren().addAll(soko);
			TranslateTransition tt = new TranslateTransition(Duration.millis(500), soko);
			int x = (int)(charPosition[0]*cellWidth - gameStack.getWidth()/2+(cellWidth/2) + xOffset);
			int y = (int)(charPosition[1]*cellHeight - gameStack.getHeight()/2+ (cellHeight/2) + yOffset);
			int toy = (int)((charPosition[1]-1)*cellHeight- gameStack.getHeight()/2 + cellHeight/2 + yOffset);
			System.out.print(x + " " + y + " " + toy);
			tt.setFromX(x);
			tt.setFromY(y);
			tt.setToX(x);
			tt.setToY(toy);
			tt.play();
			tt.setOnFinished(e -> {
				charPosition[1] -= 1;
				levelData[charPosition[0]][charPosition[1]] = 4;
				gameStack.getChildren().remove(soko);
				gameRenderer.repaint();
				checkWin();
				checkLose();
				arrowKeyHandler.enabled = true;
				mouseHandler.enabled = true;
			});
			move.play();
		} else if (levelData[charPosition[0]][charPosition[1] - 1] == 1) {
			bump.play();
		} else if (levelData[charPosition[0]][charPosition[1] - 1] == 2) {
			if (levelData[charPosition[0]][charPosition[1] - 2] == 5 || levelData[charPosition[0]][charPosition[1] - 2] == 3) {
				arrowKeyHandler.enabled = false;
				mouseHandler.enabled = false;
				levelData[charPosition[0]][charPosition[1]] = 5;
				levelData[charPosition[0]][charPosition[1]-1] = 5;
				Image image = loadImage("Sokoban.png");
				ImageView imageView = new ImageView(image);
				ImageView box = new ImageView(loadImage("box.png"));
				box.setFitWidth(cellWidth);
				box.setFitHeight(cellHeight);
				System.out.print(imageView.getX() + " ");
				System.out.print(imageView.getY());
				imageView.setFitHeight(cellHeight);
				imageView.setFitWidth(cellWidth);
				gameStack.getChildren().addAll(imageView,box);
				TranslateTransition tt = new TranslateTransition(Duration.millis(500), imageView);
				TranslateTransition tt2 = new TranslateTransition(Duration.millis(500), box);
				int x = (int)(charPosition[0]*cellWidth - gameStack.getWidth()/2+(cellWidth/2) + xOffset);
				int y = (int)(charPosition[1]*cellHeight - gameStack.getHeight()/2+ (cellHeight/2) + yOffset);
				int toy = (int)((charPosition[1]-1)*cellHeight- gameStack.getHeight()/2 + cellHeight/2 + yOffset);
				System.out.print(x + " " + y + " " + toy);
				tt.setFromX(x);
				tt.setFromY(y);
				tt.setToX(x);
				tt.setToY(toy);
				tt2.setFromX(x);
				tt2.setFromY(y-cellHeight);
				tt2.setToX(x);
				tt2.setToY(toy-cellHeight);
				tt.play();
				tt2.play();
				tt.setOnFinished(e -> {
					levelData[charPosition[0]][charPosition[1] - 2] = 2;
					charPosition[1] -= 1;
					levelData[charPosition[0]][charPosition[1]] = 4;
					gameStack.getChildren().remove(imageView);
					gameStack.getChildren().remove(box);
					gameRenderer.repaint();
					checkWin();
					checkLose();
					arrowKeyHandler.enabled = true;
					mouseHandler.enabled = true;
				});

				move.play();
			}
			else {
				bump.play();
			}
		}

		else {
			bump.play();
		}
		gameRenderer.repaint();
	}

	public void moveCharacterDown() {
		int[][] temp = new int[numCols][numRows];
		for (int i = 0; i < numCols; i++) {
			for (int j = 0; j < numRows; j++) {
				temp[i][j] = levelData[i][j];
			}
		}
		int[] temp2 = {charPosition[0], charPosition[1]};
		charMoves.push(temp2);
		boxPositionsStack.push(temp);
		if (levelData[charPosition[0]][charPosition[1] + 1] == 5 || levelData[charPosition[0]][charPosition[1] + 1] == 3) {
			arrowKeyHandler.enabled = false;
			mouseHandler.enabled = false;
			levelData[charPosition[0]][charPosition[1]] = 5;
			Image image = loadImage("Sokoban.png");
			ImageView soko = new ImageView(image);
			System.out.print(soko.getX() + " ");
			System.out.print(soko.getY());
			soko.setFitHeight(cellHeight);
			soko.setFitWidth(cellWidth);
			gameStack.getChildren().addAll(soko);
			TranslateTransition tt = new TranslateTransition(Duration.millis(500), soko);
			int x = (int)(charPosition[0]*cellWidth - gameStack.getWidth()/2+(cellWidth/2) + xOffset);
			int y = (int)(charPosition[1]*cellHeight - gameStack.getHeight()/2+ (cellHeight/2) + yOffset);
			int toy = (int)((charPosition[1]+1)*cellHeight- gameStack.getHeight()/2 + cellHeight/2 + yOffset);
			System.out.print(x + " " + y + " " + toy);
			tt.setFromX(x);
			tt.setFromY(y);
			tt.setToX(x);
			tt.setToY(toy);
			tt.play();
			tt.setOnFinished(e -> {
				charPosition[1] += 1;
				levelData[charPosition[0]][charPosition[1]] = 4;
				gameStack.getChildren().remove(soko);
				gameRenderer.repaint();
				checkWin();
				checkLose();
				arrowKeyHandler.enabled = true;
				mouseHandler.enabled = true;
			});
		} else if (levelData[charPosition[0]][charPosition[1] + 1] == 1) {
			bump.play();
		} else if (levelData[charPosition[0]][charPosition[1] + 1] == 2) {
			if (levelData[charPosition[0]][charPosition[1] + 2] == 5 || levelData[charPosition[0]][charPosition[1] + 2] == 3) {
				arrowKeyHandler.enabled = false;
				mouseHandler.enabled = false;
				levelData[charPosition[0]][charPosition[1]] = 5;
				levelData[charPosition[0]][charPosition[1]+1] = 5;
				Image image = loadImage("Sokoban.png");
				ImageView imageView = new ImageView(image);
				ImageView box = new ImageView(loadImage("box.png"));
				box.setFitWidth(cellWidth);
				box.setFitHeight(cellHeight);
				System.out.print(imageView.getX() + " ");
				System.out.print(imageView.getY());
				imageView.setFitHeight(cellHeight);
				imageView.setFitWidth(cellWidth);
				gameStack.getChildren().addAll(imageView,box);
				TranslateTransition tt = new TranslateTransition(Duration.millis(500), imageView);
				TranslateTransition tt2 = new TranslateTransition(Duration.millis(500), box);
				int x = (int)(charPosition[0]*cellWidth - gameStack.getWidth()/2+(cellWidth/2) + xOffset);
				int y = (int)(charPosition[1]*cellHeight - gameStack.getHeight()/2+ (cellHeight/2) + yOffset);
				int toy = (int)((charPosition[1]+1)*cellHeight- gameStack.getHeight()/2 + cellHeight/2 + yOffset);
				System.out.print(x + " " + y + " " + toy);
				tt.setFromX(x);
				tt.setFromY(y);
				tt.setToX(x);
				tt.setToY(toy);
				tt2.setFromX(x);
				tt2.setFromY(y+cellHeight);
				tt2.setToX(x);
				tt2.setToY(toy+cellHeight);
				tt.play();
				tt2.play();
				tt.setOnFinished(e -> {
					levelData[charPosition[0]][charPosition[1] + 2] = 2;
					charPosition[1] += 1;
					levelData[charPosition[0]][charPosition[1]] = 4;
					gameStack.getChildren().remove(imageView);
					gameStack.getChildren().remove(box);
					gameRenderer.repaint();
					checkWin();
					checkLose();
					arrowKeyHandler.enabled = true;
					mouseHandler.enabled = true;
				});
				move.play();
			}
			else {
				bump.play();
			}
		}
		else {
			bump.play();
		}
		gameRenderer.repaint();
	}

	public void mouseClicked(MouseEvent me) {
		int gridx = (int) (me.getX());
		gridx /= cellWidth;
		int gridy = (int) (me.getY());
		gridy /= cellHeight;
		if (charPosition[0] == (gridx + 1) && charPosition[1] == gridy) {
			moveCharacterLeft();
		} else if (charPosition[0] == (gridx - 1) && charPosition[1] == gridy) {
			moveCharacterRight();
		} else if (charPosition[0] == (gridx) && charPosition[1] == gridy + 1) {
			moveCharacterUp();
		} else if (charPosition[0] == (gridx) && charPosition[1] == gridy - 1) {
			moveCharacterDown();
		}
	}

	public void loseDialog() {
		lose.play();
		arrowKeyHandler.enabled = false;
		mouseHandler.enabled = false;
		String options[] = new String[]{"OK"};
		// FIRST MAKE SURE THE USER REALLY WANTS TO EXIT
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(primaryStage);
		BorderPane exitPane = new BorderPane();
		HBox optionPane = new HBox();
		Button yesButton = new Button(options[0]);
		Button noButton = new Button("Undo");
		optionPane.setSpacing(10.0);
		optionPane.getChildren().addAll(yesButton, noButton);
		optionPane.setAlignment(Pos.CENTER);
		Label exitLabel = new Label("You Lost :( !\nHead Back to the Main screen?");
		Image losei = loadImage("lose.png");
		ImageView losev = new ImageView(losei);
		losev.setFitHeight(300);
		losev.setFitWidth(300);
		exitPane.setTop(exitLabel);
		exitPane.setCenter(losev);
		exitPane.setBottom(optionPane);
		Scene scene = new Scene(exitPane, 300, 400);
		exitPane.setAlignment(exitLabel, Pos.CENTER);
		exitPane.setAlignment(optionPane, Pos.CENTER);
		dialogStage.setScene(scene);
		dialogStage.show();

		// WHAT'S THE USER'S DECISION?
		yesButton.setOnAction(e -> {
			// YES, LET'S EXIT
			initSplashScreen();
			dialogStage.close();
		});
		noButton.setOnAction(e -> {
			arrowKeyHandler.enabled = true;
			mouseHandler.enabled = true;
			undo();
			lose.stop();
			timeline.play();
			dialogStage.close();
		});
	}
	public void winDialog() {
		win.play();
		arrowKeyHandler.enabled = false;
		mouseHandler.enabled = false;
		String options[] = new String[]{"OK"};
		// FIRST MAKE SURE THE USER REALLY WANTS TO EXIT
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(primaryStage);
		BorderPane exitPane = new BorderPane();
		HBox optionPane = new HBox();
		Button yesButton = new Button(options[0]);
		optionPane.setSpacing(10.0);
		optionPane.getChildren().addAll(yesButton);
		StackPane textPane = new StackPane();
		textPane.setPadding(marginlessInsets);
		Text exitLabel = new Text("YOU WIN!\nHead Back to the Main screen?");
		exitLabel.setFont(Font.font("Calibri"));
		exitLabel.setStyle("-fx-text-alignment:center");
		Image wini = new Image(ImgPath+"trophy.png");
		ImageView winv = new ImageView(wini);
		winv.setFitWidth(200);
		winv.setFitHeight(300);
		textPane.getChildren().add(exitLabel);
		exitPane.setTop(textPane);
		exitPane.setCenter(winv);
		exitPane.setBottom(yesButton);
		Scene scene = new Scene(exitPane, 300, 400);
		exitPane.setAlignment(yesButton,Pos.CENTER);
		dialogStage.setScene(scene);
		dialogStage.show();
		// WHAT'S THE USER'S DECISION?
		yesButton.setOnAction(e -> {
			// YES, LET'S EXIT
			initSplashScreen();
			win.stop();
			dialogStage.close();
		});
	}

	public void mouseDragged(MouseEvent me1, MouseEvent me2) {
		int gridx1 = (int) (me1.getX());
		gridx1 /= cellWidth;
		gridx1 /= cellWidth;
		int gridy1 = (int) (me1.getY());
		gridy1 /= cellHeight;
		int gridx2 = (int) (me2.getX());
		gridx2 /= cellWidth;
		int gridy2 = (int) (me2.getY());
		gridy2 /= cellHeight;
		if (charPosition[0] == gridx1 && charPosition[1] == gridy1) {
			if (charPosition[0] == (gridx2 + 1) && charPosition[1] == gridy2) {
				moveCharacterLeft();
			} else if (charPosition[0] == (gridx2 - 1) && charPosition[1] == gridy2) {
				moveCharacterRight();
			} else if (charPosition[0] == (gridx2) && charPosition[1] == gridy2 + 1) {
				moveCharacterUp();
			} else if (charPosition[0] == (gridx2) && charPosition[1] == gridy2 - 1) {
				moveCharacterDown();
			}
		}
	}

	public void checkWin() {
		boolean won = true;
		for (int i = 0; i < destinations.size(); i++) {
			int[] destData = destinations.get(i);
			int[] blockData = boxPositions.get(i);
			if (destData[0] != blockData[0] || destData[1] != blockData[1]) {
				won = false;
			}
		}
		if (won) {
			timeline.stop();
			SokobanGameData data = gsm.getGameInProgress();
			data.giveUp();
			int time = (int)data.getTimeOfGame();
			fileLoader.addToStats(data.getLevel(), time);
			winDialog();
		}
	}

	public void checkLose() {
		boolean lost = false;
		A: for (int i = 0; i < boxPositions.size(); i++) {
			int[] blockData = boxPositions.get(i);
			for(int j=0;j<boxPositions.size();j++) {
				int[] destData = destinations.get(j);
				if(destData[0] == blockData[0] && destData[1] == blockData[1])
					continue A;
			}
				if (levelData[blockData[0]-1][blockData[1]] == 1 && levelData[blockData[0]][blockData[1]+1] == 1) {
					lost = true;
				}
				if (levelData[blockData[0]][blockData[1]+1] == 1 && levelData[blockData[0]+1][blockData[1]] == 1) {
					lost = true;
				}
				if (levelData[blockData[0]+1][blockData[1]] == 1 && levelData[blockData[0]][blockData[1]-1] == 1) {
					lost = true;
				}
				if (levelData[blockData[0]][blockData[1]-1] == 1 && levelData[blockData[0]-1][blockData[1]] == 1) {
					lost = true;

			}
		}

		if(lost) {
			timeline.pause();
			SokobanGameData data = gsm.getGameInProgress();
			data.giveUp();
			int time = (int)data.getTimeOfGame();
			time*=-1;
			fileLoader.addToStats(data.getLevel(), time);
			loseDialog();
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
			this.setHeight(720);
			cellHeight = this.getHeight() / numRows;
			cellWidth = this.getWidth() / numCols;
			cellWidth = (cellHeight < cellWidth) ? cellHeight : cellWidth;
			cellHeight = (cellHeight < cellWidth) ? cellHeight : cellWidth;
			repaint();

		}

		public void repaint() {
			gc = this.getGraphicsContext2D();
			gc.clearRect(0, 0, this.getWidth(), this.getHeight());
			xOffset = (int) ((this.getWidth() - (numCols * cellWidth)) / 2);

			gc.setFill(Color.DARKORANGE);
			gc.fillRect(0, 0, this.getWidth(), this.getHeight());
			gc.setFill(Color.WHITE);
			boxPositions.clear();
			for (int i = 0; i < numCols; i++) {
				yOffset = (int) ((this.getHeight() - (numRows * cellHeight)) / 2);
				for (int j = 0; j < numRows; j++) {
					if (levelData[i][j] != 0) {
						gc.setFill(Color.WHITE);
						gc.fillRect(xOffset, yOffset, cellWidth, cellHeight);
					}
					switch (levelData[i][j]) {
						case 1:
							gc.drawImage(wallImage, xOffset, yOffset, cellWidth, cellHeight);
							break;
						case 2:
							int[] boxPosition = new int[2];
							boxPosition[0] = i;
							boxPosition[1] = j;
							boxPositions.add(boxPosition);
							gc.drawImage(boxImage, xOffset, yOffset, cellWidth, cellHeight);
							break;
						case 3:
							gc.drawImage(placeImage, xOffset, yOffset, cellWidth, cellHeight);
							break;
						case 4:
							gc.drawImage(sokobanImage, xOffset, yOffset, cellWidth, cellHeight);
							break;
						case 5:
							gc.setFill(Color.WHITE);
							gc.fillRect(xOffset, yOffset, cellWidth, cellHeight);
					}
					String numToDraw = "" + levelData[i][j];
					yOffset += cellHeight;
				}
				xOffset += cellWidth;
			}
			xOffset = (int) ((this.getWidth() - (numCols * cellWidth)) / 2);
			yOffset = (int) ((this.getHeight() - (numRows * cellHeight)) / 2);
			for (int i = 0; i < destinations.size(); i++) {
				int[] data = destinations.get(i);
				if (levelData[data[0]][data[1]] == 5) {
					int xcoord = xOffset + (int) (data[0] * cellWidth);
					int ycoord = yOffset + data[1] * (int) cellHeight;
					gc.drawImage(placeImage, xcoord, ycoord, cellWidth, cellHeight);
				} else if (levelData[data[0]][data[1]] == 4) {
					int xcoord = xOffset + (int) (data[0] * cellWidth);
					int ycoord = yOffset + data[1] * (int) cellHeight;
					gc.drawImage(placeImage, xcoord, ycoord, cellWidth, cellHeight);
					gc.drawImage(sokobanImage, xcoord, ycoord, cellWidth, cellHeight);
				} else if (levelData[data[0]][data[1]] == 2) {
					int xcoord = xOffset + (int) (data[0] * cellWidth);
					int ycoord = yOffset + data[1] * (int) cellHeight;
					gc.drawImage(placeImage, xcoord, ycoord, cellWidth, cellHeight);
					gc.drawImage(boxImage, xcoord, ycoord, cellWidth, cellHeight);
				}
			}
		}

		public void drawCharacter(int x, int y) {
			gc.drawImage(sokobanImage, x, y, cellWidth, cellHeight);
		}
	}

	public Image loadImage(String imageName) {
		Image img = new Image(ImgPath + imageName);
		return img;
	}

	public void undo() {
		if (charMoves.isEmpty()) {
			errorHandler.processError(SokobanPropertyType.ERROR_NO_MORE_UNDOS);
		} else

		{
			levelData = boxPositionsStack.pop().clone();
			charPosition = charMoves.pop().clone();
			gameRenderer.repaint();
		}
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
				initStatsPane();
				mainPane.setCenter(statsPanel);
				break;
			default:
		}

	}


}
