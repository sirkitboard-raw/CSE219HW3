package sokoban.file;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

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
	public ArrayList<ArrayList<Integer>> getStats() {
		try {
			File file = new File("data/stats.sokstat");
			FileInputStream fis = new FileInputStream(file.getPath());
			byte[] buffer = new byte[(int)file.length()];
			DataInputStream dis = new DataInputStream(fis);
			dis.readFully(buffer);
			ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
			dis = new DataInputStream(bis);
			ArrayList<ArrayList<Integer>> levelStats= new ArrayList<ArrayList<Integer>>();
			for(int i=0;i<7;i++) {
				levelStats.add(new ArrayList<Integer>());
			}
			for(int i=0;i<7;i++) {
				int num = dis.readInt();
				for(int j=0;j<num;j++) {
					levelStats.get(i).add(dis.readInt());
				}
			}
			return levelStats;
		} catch (IOException e) {
			ui.getErrorHandler().processError(SokobanPropertyType.ERROR_INVALID_FILE);
			try {
				File file = new File("data/stats.sokstat");
				file.delete();
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos = new DataOutputStream(fos);
				// FIRST WRITE THE DIMENSIONS
				for(int i=0;i<7;i++){
					dos.writeInt(0);
				}
			} catch (IOException e1) {
				e.printStackTrace();
			}
		}
		return new ArrayList<ArrayList<Integer>>(7);
	}
	public void addToStats(int level, int time) {
		try {
			File file = new File("data/stats.sokstat");
			FileInputStream fis = new FileInputStream(file.getPath());
			byte[] buffer = new byte[(int)file.length()];
			DataInputStream dis = new DataInputStream(fis);
			dis.readFully(buffer);
			ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
			dis = new DataInputStream(bis);
			ArrayList<ArrayList<Integer>> levelStats= new ArrayList<ArrayList<Integer>>();
			for(int i=0;i<7;i++) {
				levelStats.add(new ArrayList<Integer>());
			}
			for(int i=0;i<7;i++) {
				int num = dis.readInt();
				for(int j=0;j<num;j++) {
					levelStats.get(i).add(dis.readInt());
				}
			}
			file.delete();
			FileOutputStream fos = new FileOutputStream("data/stats.sokstat");
			DataOutputStream dos = new DataOutputStream(fos);
			levelStats.get(level-1).add(time);
			for(int i=0;i<7;i++) {
				dos.writeInt(levelStats.get(i).size());
				for(int j=0;j<levelStats.get(i).size();j++) {
					dos.writeInt(levelStats.get(i).get(j));
				}
			}
			file = new File("data/statsHTML_base.html");
			Scanner fileReader = new Scanner(file);
			String htmlCode = "";
			while(fileReader.hasNext()) {
				htmlCode+= fileReader.nextLine() + "\n";
			}
			for(int i=0;i<7;i++) {
				int wins = 0;
				int played = levelStats.get(i).size();
				htmlCode = htmlCode.replaceAll("PLACEHOLDER_"+(i+1)+"_PLAYED", played+"");
				for(int j=0;j<played;j++) {
					if(levelStats.get(i).get(j) > 0)
						wins++;
				}
				htmlCode = htmlCode.replaceAll("PLACEHOLDER_"+(i+1)+"_WON", wins+"");
				if(played!=0) {
					htmlCode = htmlCode.replaceAll("PLACEHOLDER_" + (i + 1) + "_PERCENTAGE", (((double) wins / played)*100) + "%");
				}
				else {
					htmlCode = htmlCode.replaceAll("PLACEHOLDER_" + (i + 1) + "_PERCENTAGE", "-");
				}
				if (wins!=0){
					int fastest = Integer.MAX_VALUE;
					for(int j=0;j<played;j++) {
						if(levelStats.get(i).get(j) > 0 && levelStats.get(i).get(j) < fastest) {
							fastest = levelStats.get(i).get(j);
						}
					}
					htmlCode = htmlCode.replaceAll("PLACEHOLDER_" + (i + 1) + "_FASTEST", fastest + " seconds");
				}
				else htmlCode = htmlCode.replaceAll("PLACEHOLDER_" + (i + 1) + "_FASTEST", "-");
			}

			File logFile=new File("data/statsHTML.html");
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(htmlCode);
			writer.flush();
		} catch (IOException e) {
			ui.getErrorHandler().processError(SokobanPropertyType.ERROR_INVALID_FILE);
			ui.getErrorHandler().processError(SokobanPropertyType.ERROR_INVALID_FILE);
			try {
				File file = new File("data/stats.sokstat");
				file.delete();
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos = new DataOutputStream(fos);
				// FIRST WRITE THE DIMENSIONS
				for(int i=0;i<7;i++){dos.writeInt(0);}
			} catch (IOException e1) {
				e.printStackTrace();
			}
		}
	}

}
