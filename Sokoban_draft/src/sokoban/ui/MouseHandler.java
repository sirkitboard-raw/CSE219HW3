package sokoban.ui;

import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

/**
 * Created by Aditya Balwani on 10/9/2014.
 * SBUID : 109353920
 */
public class MouseHandler {
	private SokobanUI ui;
	ArrayList<MouseEvent> drag = new ArrayList<>();
	boolean enabled;
	public MouseHandler(SokobanUI initUI){
		ui = initUI;
	}
	public void mouseClicked(MouseEvent me) {
		ui.mouseClicked(me);
	}
	public void mouseDragged(MouseEvent me) {
		drag.add(me);
	}
	public void endDrag(MouseEvent me) {
		if(enabled) {
			if (drag.size() < 3) {
				ui.mouseClicked(me);
			} else {
				MouseEvent mouseEvent = drag.get(0);
				MouseEvent mouseEvent1 = drag.get(drag.size() - 1);
				drag.clear();
				ui.mouseDragged(mouseEvent, mouseEvent1);
			}
		}
	}
}
