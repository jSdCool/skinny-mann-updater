import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class DownloadFile {
	static void download(String link,String fileName) throws IOException {

		URL url = new URL(link);
		URLConnection c = url.openConnection();
		c.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
		Main.totalDownloadSize = c.getContentLengthLong();
		InputStream input;
		input = c.getInputStream();
		byte[] buffer = new byte[4096];
		int n = -1;

		OutputStream output = new FileOutputStream(new File(fileName));
		while ((n = input.read(buffer)) != -1) {
		    if (n > 0) {
		        output.write(buffer, 0, n);
		        Main.completedDownload += buffer.length;
		        Main.downloadPercent = ((double)Main.completedDownload / Main.totalDownloadSize);
		        Main.downloadProgreeBar.setValue((int)(Main.downloadPercent*100));
		        //Main.downloadProgreeBar.repaint();
		    }
		}
		output.close();
	}
	
	static String readFileFromGithub(String link)throws Throwable {//used to read text files from github, used for getting the latesed verion  of the game and for outher update functions
	  URL url =new URL(link);//get as URL
	  HttpURLConnection Http = (HttpURLConnection) url.openConnection();//open the url
	  Map<String, List<String>> Header = Http.getHeaderFields();//redirection shit I dont understand
	  for (String header : Header.get(null)) {
	    if (header.contains(" 302 ") || header.contains(" 301 ")) {
	      link = Header.get("Location").get(0);
	      url = new URL(link);
	      Http = (HttpURLConnection) url.openConnection();
	      Header = Http.getHeaderFields();
	    }
	  }
	
	  InputStream I_Stream = Http.getInputStream();//get the incomeing stream of html data
	  String Response = GetStringFromStream(I_Stream);//get a raw string from the data
	
	  //System.out.println(Response);
	  return Response;
	}
	
	private static String GetStringFromStream(InputStream Stream) throws IOException {//turns the raw html data into a string java can understand
	  if (Stream != null) {
	    Writer writer = new StringWriter();
	    char[] Buffer = new char[2048];
	    try {
	      Reader reader = new BufferedReader(new InputStreamReader(Stream, "UTF-8"));
	      int counter;
	      while ((counter = reader.read(Buffer)) != -1) {
	        writer.write(Buffer, 0, counter);
	      }
	    }
	    finally {
	      Stream.close();
	    }
	    return writer.toString();
	  } else {
	    return "No Contents";
	  }
	}
}
