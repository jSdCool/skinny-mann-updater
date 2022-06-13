import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JLabel;
/**the thread class responsible for actually copying the files
 * 
 * @author jSdCool
 *
 */
public class CopyThread extends Thread{
static int numOfinstances=0;
int threadNumber;
JLabel label;
	CopyThread(JLabel label){
		super("copying Thread "+(numOfinstances+1));
		threadNumber=numOfinstances+1;
		numOfinstances++;
		this.label=label;
	}
ArrayList<String> toCopy=new ArrayList<>();
boolean shouldRun=true,working=false,endReaddy=false;
	public void run() {
		while(shouldRun) {
			Math.random();//prevent this thread from being put to sleep for being "inactive"
			if(toCopy.size()>0) {
				copy(0);
				label.setText(Main.source+"/"+toCopy.get(0)+" >>>> "+Main.gameLocation+"/"+toCopy.get(0));
				Main.panel.repaint();
				Main.completed++;//increase the number of of coppies completed
				double precent=((int)((Main.completed*0.1/Main.total)*10000))/10.0;//calculate the completion percent 
				System.out.println("(%"+precent+") "+Main.source+"/"+toCopy.get(0)+" >>>> "+Main.gameLocation+"/"+toCopy.get(0));
				toCopy.remove(0);
				Main.generalStatus.setText("status: copying files ("+precent+") ");
				Main.panel.repaint();
				
				
			}else {
				if(endReaddy)//if there are not more files that need to be coppied then kill the thread
					return;
			}
			if(working&&toCopy.size()==0)
				working=false;
		}
	
	}
	
	void copy(int times) {
		if(times==10)
			return;
		try {
			String[] newDir=(Main.gameLocation+"/"+toCopy.get(0)).split("\\\\|/");
			String destDir="";
			for(int i=0;i<newDir.length-1;i++) {//get the path to the current file 
				destDir+=newDir[i]+"/";
			}
			new File(destDir).mkdirs();//make the parent folder if it dosen't exist
			File dest=new File(Main.gameLocation+"/"+toCopy.get(0));
			if(dest.exists()) {//if the file already exists in the new location then delete the current version
				dest.delete();
			}
			java.nio.file.Files.copy(new File(Main.source+"/"+toCopy.get(0)).toPath(),dest.toPath());//copy the file `
		} catch (IOException e) {//if it fails
			e.printStackTrace();//print the stactrace
			System.out.println(toCopy.get(0));
			copy(times+1);//try again
			 
		}
	}
}
