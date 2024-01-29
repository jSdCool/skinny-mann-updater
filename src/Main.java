import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main implements ActionListener, WindowListener, Runnable {
	static JFrame frame;
	static JPanel panel;
	static JLabel title,generalStatus,currentTask;
	static JLabel CThread[]=new JLabel[4];
	static JButton updateButton;
	static JComboBox<String> versions;
	
	static String downloadLink="",newGameVersion="",gameLocation="",source="temp";
	static boolean everyThingOk=true;
	static ArrayList<String> fileIndex = new ArrayList<>();
	static ArrayList<CopyThread> threads=new ArrayList<>();
	static int completed, total;
	static long totalDownloadSize,completedDownload;
	static double downloadPercent;
	static HashMap<OS,String> exeFileNames = new HashMap<>();
	static OS currentOS;
	static JProgressBar downloadProgreeBar;
	
	static {
		exeFileNames.put(OS.WINDOWS, "skiny_mann.exe");
		exeFileNames.put(OS.LINUX, "skiny_mann");
		exeFileNames.put(OS.MACOS, "skiny_mann.app");
	}
	
	public static void main(String[] args) {
		detectOS();
		
		Scanner fileReader;
		try {
			fileReader = new Scanner(new File("downloadInfo.txt"));
			downloadLink=fileReader.nextLine();
			newGameVersion=fileReader.nextLine();
			gameLocation=fileReader.nextLine();
			fileReader.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			//return;
			downloadLink="https://github.com/jSdCool/skinny-mann/releases/download/v0.7.0/skinny.mann.win64.zip";
			newGameVersion="tmp";
			gameLocation="tmp";
		}
		
		
		
		new Main();

		//downloadAndInstallUpdate();
	}
	public Main() {
		frame= new JFrame();
		frame.setSize(600, 400);
		
		panel= new JPanel();
		frame.add(panel);
		frame.setVisible(true);
		frame.addWindowListener(this);
		panel.setLayout(null);
		frame.setTitle("Skinny Mann updater");
		title=new JLabel("update skinny mann to version "+newGameVersion);
		title.setBounds(10, 20, 300, 25);
		panel.add(title);		
		updateButton=new JButton("Update");
		updateButton.setBounds(10,40,200,25);
		updateButton.addActionListener(this);
		panel.add(updateButton);
		generalStatus=new JLabel("status: ");
		generalStatus.setBounds(10,60,500,25);
		generalStatus.setVisible(false);
		panel.add(generalStatus);
		currentTask=new JLabel("");
		currentTask.setBounds(10,80,500,60);
		panel.add(currentTask);
		CThread[0]=new JLabel("");
		CThread[0].setBounds(10,80,500,25);
		CThread[0].setVisible(false);
		panel.add(CThread[0]);
		CThread[1]=new JLabel("");
		CThread[1].setBounds(10,100,500,25);
		CThread[1].setVisible(false);
		panel.add(CThread[1]);
		CThread[2]=new JLabel("");
		CThread[2].setBounds(10,120,500,25);
		CThread[2].setVisible(false);
		panel.add(CThread[2]);
		CThread[3]=new JLabel("");
		CThread[3].setBounds(10,140,500,25);
		CThread[3].setVisible(false);
		panel.add(CThread[3]);
		
		versions = new JComboBox<String>(getVersions());
		
		versions.setBounds(10,70,200,25);
		panel.add(versions);
		
		downloadProgreeBar = new JProgressBar();
		downloadProgreeBar.setBounds(10, 80, 400, 25);
		downloadProgreeBar.setValue(0);
		downloadProgreeBar.setVisible(false);
		panel.add(downloadProgreeBar);
		
		panel.repaint();
	}

	private static void downloadAndInstallUpdate() {
		System.out.println("attempting to download");
		//*
		try {
			DownloadFile.download(downloadLink, "temp.zip");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			generalStatus.setText("something went wrong, please try again");
			panel.repaint();
			e.printStackTrace();
			everyThingOk=false;
		}//*/
		generalStatus.setText("status: extracting files");
		currentTask.setVisible(true);
		panel.repaint();
		System.out.println("done downloading\nstarting extraction");
		extractZip();
		System.out.println("extraction finished");
		String tempPath=findExe("temp","");
		System.out.println(tempPath);
		currentTask.setVisible(false);
		CThread[0].setVisible(true);
		CThread[1].setVisible(true);
		CThread[2].setVisible(true);
		CThread[3].setVisible(true);
		generalStatus.setText("status: copying files");
		panel.repaint();
		source=tempPath;
		copyFiles();
		CThread[0].setVisible(false);
		CThread[1].setVisible(false);
		CThread[2].setVisible(false);
		CThread[3].setVisible(false);
		generalStatus.setText("status: Done. Update complete you can now launch the game");
		panel.repaint();
		
	}

	private static void extractZip() {
		String zipFileLoctaion="temp.zip";
		File destDir=new File("temp");
		try {
			byte[] buffer = new byte[1024];
			@SuppressWarnings("resource")
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileLoctaion));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				System.out.println("extracting "+zipEntry.getName());
				currentTask.setText("extracting: "+zipEntry.getName());
				panel.repaint();
				File newFile = newFile(destDir, zipEntry);
			     if (zipEntry.isDirectory()) {
			         if (!newFile.isDirectory() && !newFile.mkdirs()) {
			             throw new IOException("Failed to create directory " + newFile);
			         }
			     } else {
			         // fix for Windows-created archives
			         File parent = newFile.getParentFile();
			         if (!parent.isDirectory() && !parent.mkdirs()) {
			             throw new IOException("Failed to create directory " + parent);
			         }
			         
			         // write file content
			         FileOutputStream fos = new FileOutputStream(newFile);
			         int len;
			         while ((len = zis.read(buffer)) > 0) {
			             fos.write(buffer, 0, len);
			         }
			         fos.close();
			     }
			 zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		}catch(IOException i) {
			i.printStackTrace();
			generalStatus.setText("something went wrong, please try again");
			panel.repaint();
			everyThingOk=false;
		}
	}
	
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
	    File destFile = new File(destinationDir, zipEntry.getName());

	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();

	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }

	    return destFile;
	}
	
	public void run() {
		downloadAndInstallUpdate();
		if(!everyThingOk) {
			updateButton.setVisible(true);
			panel.repaint();
			return;
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource().equals(updateButton)) {
			updateButton.setVisible(false);
			generalStatus.setVisible(true);
			versions.setVisible(false);
			downloadProgreeBar.setVisible(true);
			generalStatus.setText("status: downloading update. this may take a while");
			panel.repaint();
			everyThingOk=true;
			Thread doUpdate=new Thread(this);
			doUpdate.start();

		}
				
	
	}
	
	/**recusivly look for the folder the exe is in 
	 * 
	 * @param parentPath the initial path to search
	 * @param subPath only pass in "" 
	 * @return the path of the folder the exe resides in
	 */
	static String findExe(String parentPath,String subPath) {
		String[] files=new File(parentPath+"/"+subPath).list();//get a list of all things in the current folder
		for(int i=0;i<files.length;i++) {//loop through all the things in the current folder

			if(new File(parentPath+"/"+subPath+"/"+files[i]).list()!=null) {//check weather the current thing is a folder or a file 
				findExe(parentPath,subPath+"/"+files[i]);//if it is a folder then scan through that folder for more files

			}else {//if it is a file
				if(currentOS != OS.LINUX || new File(files[i]).isFile())//if on linux make sure it is in fact a file since there is no extention
					if(files[i].equals(exeFileNames.get(currentOS))) {
						yesPoint= parentPath+subPath;
					}
			}
		}
		return yesPoint;
	}
	static String yesPoint=null;
	
	static void copyFiles() {
		threads=new ArrayList<>();
		
		int numOfThreads=4;
		System.out.println("scanning for files");
		scanForFiles(source,"");//Discover all the file that need to be copied
		total=fileIndex.size();//note how many file there are
		System.out.println("found "+total+" files");
		for(int i=0;i<numOfThreads;i++) {//create all the requested threads
			threads.add(new CopyThread(CThread[i]));
			threads.get(i).start();
		}
		while(fileIndex.size()>0) {//while there are still more unassigned files that need to be copied 
			for(int i=0;i<threads.size();i++) {//check all the threads
				if(!threads.get(i).isAlive()) {//restart the thread if it died
					threads.set(i, new CopyThread(CThread[i]));
					threads.get(i).start();
					System.out.println("repalced thread "+i);
				}
				if(!threads.get(i).working) {//if the thread needs more work to do then give it more work
					threads.get(i).toCopy=createNextJob();
					threads.get(i).working=true;
				}
			}
		}
		for(int i=0;i<threads.size();i++) {//tell all threads that there will be no more work once they finish
			threads.get(i).endReaddy=true;
		}
			System.out.println("file assignment finished");
		while(threadsRunning()) {//wait for all the threads to finish copying files
			
		}
	}
	
	/**Recursively scan folders for files to copy
	 * 
	 * @param parentPath the root path of the folder that is being copied 
	 * @param subPath the path of the current sub folder that is being looked through
	 */
	public static void scanForFiles(String parentPath,String subPath) {
		String[] files=new File(parentPath+"/"+subPath).list();//get a list of all things in the current folder
		for(int i=0;i<files.length;i++) {//loop through all the things in the current folder

			if(new File(parentPath+"/"+subPath+"/"+files[i]).list()!=null) {//check weather the current thing is a folder or a file 
				scanForFiles(parentPath,subPath+"/"+files[i]);//if it is a folder then scan through that folder for more files

			}else {//if it is a file
				//add this file to the to copy index
				if(subPath.equals("")) {
					fileIndex.add(files[i]);
				}else {
					fileIndex.add(subPath+"/"+files[i]);
				}
				System.out.println(fileIndex.get(fileIndex.size()-1));
			}
		}
	
	}
	
	/**gets a list of files that need to be copied to send to a thread
	 * 
	 * @return an array list of file paths that need to be copied
	 */
	static ArrayList<String> createNextJob(){
		int batchSize=10;
		ArrayList<String> batch=new ArrayList<>();
		for(int i=0;i<batchSize&&fileIndex.size()>0;i++) {//use the batch size to determine the number of items to send to each thread
			batch.add(fileIndex.remove(0));
		}
		
		return batch;
	
	}
	
	/**
	 * 
	 * @return weather any thread is sill running 
	 */
	static boolean threadsRunning() {
		for(int i=0;i<threads.size();i++) {
			if(threads.get(i).isAlive())
				return true;
		}
		return false;
	}
	
	
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		System.exit(0);
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		System.exit(0);
	}
	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public enum OS{
		WINDOWS,LINUX,MACOS;
	}
	
	static void detectOS() {
		String name = System.getProperty("os.name");
		name=name.toLowerCase();
		if(name.contains("windows")) {
			currentOS = OS.WINDOWS;
			return;
		}
		if(name.contains("linux")) {
			currentOS=OS.LINUX;
			return;
		}
		if(name.contains("macos")) {
			currentOS=OS.MACOS;
			return;
		}
	}
	
	public String[] getVersions() {
		return new String[] {"1.0.0","1.1.1","0.0.0"};
	}

}
