package sokoban.ui;

/**
 * Created by Aditya Balwani on 10/9/2014.
 * SBUID : 109353920
 */

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class ArrowKeyHandler{
	private SokobanUI ui;
	public ArrowKeyHandler(SokobanUI initUI){
		ui = initUI;
	}

	public void keyPressed(KeyEvent ke)
	{
		// GET THE KEY THAT WAS PRESSED IN ASSOCIATION WITH
		// THIS METHOD CALL.
		//int keyCode = ke.getKeyCode();
		KeyCode keyCode = ke.getCode();

		// IS CONTROL-C PRESSED?
		if (keyCode == KeyCode.LEFT) {
			// A CHEAT TO DISPLAY THE SECRET WORD
			ui.moveCharacterLeft();
		}
		else if(keyCode == KeyCode.RIGHT) {
			ui.moveCharacterRight();
		}
		else if(keyCode == KeyCode.UP) {
			ui.moveCharacterUp();
		}
		else if(keyCode == KeyCode.DOWN) {
			ui.moveCharacterDown();
		}
	}
}
