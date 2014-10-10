package sokoban.file;

import java.io.*;

import application.Main.SokobanPropertyType;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import properties_manager.PropertiesManager;
import sokoban.ui.SokobanUI;

public class SokobanFileLoader {
	SokobanUI ui;
	public SokobanFileLoader(SokobanUI ui) {
		this.ui = ui;
	}

	public int[][] loadLevel(String fileName) throws FileNotFoundException, IOException{
			File file = new File("data/"+fileName);
			FileInputStream fis = new FileInputStream(file.getPath());
			byte[] buffer = new byte[(int)file.length()];
			DataInputStream dis = new DataInputStream(fis);
			dis.readFully(buffer);
			ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
			dis = new DataInputStream(bis);
			int cols = dis.readInt();
			int rows = dis.readInt();
			int[][] levelData = new int[cols][rows];
			for(int i=0;i<cols;i++) {
				for (int j=0;j<rows;j++) {
					levelData[i][j] = dis.readInt();
				}
			}
			return levelData;
	}
	
}
