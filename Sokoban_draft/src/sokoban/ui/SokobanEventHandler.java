package sokoban.ui;

import java.io.IOException;
import java.util.ArrayList;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import application.Main.SokobanPropertyType;
import properties_manager.PropertiesManager;
import sokoban.game.SokobanGameData;
import xml_utilities.InvalidXMLFileFormatException;
import sokoban.file.SokobanFileLoader;
import sokoban.game.SokobanGameStateManager;

public class SokobanEventHandler {

    private SokobanUI ui;

    /**
     * Constructor that simply saves the ui for later.
     *
     * @param initUI
     */
    public SokobanEventHandler(SokobanUI initUI) {
        ui = initUI;
    }

    /**
     * This method responds to when the user wishes to switch between the Game,
     * Stats, and Help screens.
     *
     * @param uiState The ui state, or screen, that the user wishes to switch
     * to.
     */
    public void respondToSwitchScreenRequest(SokobanUI.SokobanUIState uiState) {

        ui.changeWorkspace(uiState);
    }

    /**
     * This method responds to when the user presses the new game method.
     */
    public void respondToNewGameRequest(int level) {
        SokobanGameStateManager gsm = ui.getGSM();
        gsm.startNewGame(level);
    }

	public void respondToSelectLevelRequest(String level) {
		ui.initLevel(level);
	}

    /**
     * This method responds to when the user requests to exit the application.
     *
     *
     */



    public void respondToExitRequest(Stage primaryStage) {
        // ENGLIS IS THE DEFAULT
        String options[] = new String[]{"Yes", "No"};
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        options[0] = props.getProperty(SokobanPropertyType.DEFAULT_YES_TEXT);
        options[1] = props.getProperty(SokobanPropertyType.DEFAULT_NO_TEXT);
        String verifyExit = props.getProperty(SokobanPropertyType.DEFAULT_EXIT_TEXT);

        // NOW WE'LL CHECK TO SEE IF LANGUAGE SPECIFIC VALUES HAVE BEEN SET
        if (props.getProperty(SokobanPropertyType.YES_TEXT) != null) {
            options[0] = props.getProperty(SokobanPropertyType.YES_TEXT);
            options[1] = props.getProperty(SokobanPropertyType.NO_TEXT);
            verifyExit = props.getProperty(SokobanPropertyType.EXIT_REQUEST_TEXT);
        }

        // FIRST MAKE SURE THE USER REALLY WANTS TO EXIT
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        BorderPane exitPane = new BorderPane();
        HBox optionPane = new HBox();
        Button yesButton = new Button(options[0]);
        Button noButton = new Button(options[1]);
        optionPane.setSpacing(10.0);
        optionPane.getChildren().addAll(yesButton, noButton);
        Label exitLabel = new Label(verifyExit);
        exitPane.setCenter(exitLabel);
        exitPane.setBottom(optionPane);
        Scene scene = new Scene(exitPane, 200, 100);
        dialogStage.setScene(scene);
        dialogStage.show();
        // WHAT'S THE USER'S DECISION?
        yesButton.setOnAction(e -> {
            // YES, LET'S EXIT
            ui.initSplashScreen();
			SokobanGameData data = ui.getGSM().getGameInProgress();
			data.giveUp();
			int time = (int)data.getTimeOfGame();
			ui.getFileLoader().addToStats(data.getLevel(), time);
			dialogStage.close();
        });
        noButton.setOnAction(e -> {
            dialogStage.close();
        });
    }

	/*public void respondToBackRequest(Stage primaryStage) {
		// ENGLIS IS THE DEFAULT
		String options[] = new String[]{"Yes", "No"};
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		options[0] = props.getProperty(SokobanPropertyType.DEFAULT_YES_TEXT);
		options[1] = props.getProperty(SokobanPropertyType.DEFAULT_NO_TEXT);
		String verifyExit = "Are you sure you want to go back to Main Menu?";

		// FIRST MAKE SURE THE USER REALLY WANTS TO EXIT
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(primaryStage);
		BorderPane exitPane = new BorderPane();
		HBox optionPane = new HBox();
		Button yesButton = new Button(options[0]);
		Button noButton = new Button(options[1]);
		optionPane.setSpacing(10.0);
		optionPane.getChildren().addAll(yesButton, noButton);
		Label exitLabel = new Label(verifyExit);
		exitPane.setCenter(exitLabel);
		exitPane.setBottom(optionPane);
		Scene scene = new Scene(exitPane, 300, 100);
		dialogStage.setScene(scene);
		dialogStage.show();
		// WHAT'S THE USER'S DECISION?
		yesButton.setOnAction(e -> {
			// YES, LET'S EXIT
			ui.initSplashScreen();
			dialogStage.close();
		});
		noButton.setOnAction(e -> {
			dialogStage.close();
		});

	}*/

}
