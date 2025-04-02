/**
 * 
 */
package esetkey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * @author fengc
 *
 */
public class EsetKey {
	private final String FILENAME = "ESETKEY";
	private final String FILEEXT = "html";
	
	private String fetchurl = "https://t2bot.ru/keys/nkeys.php";
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			EsetKey ek = new EsetKey();
			ek.getViaURL();
		} catch (Exception e) {
			print(e);
		}
	}
	private void getViaURL() throws Exception {
		print("Start");
		String out = new Scanner(new URL(fetchurl).openStream(), "UTF-8").useDelimiter("\\A").next();
		out = parseStrToXML(out);
		saveResult(out);
		print("Done!");
	}
	private void saveResult(String input) throws IOException {
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S"); 
		File file = new File(FILENAME + "." + FILEEXT);
		if ( !file.exists() ) {
			String startStr = "<html><head></head><body>";
			String endStr = "<div>Fetched on: " + format.format(date) + "</div></body></html>";
			Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8);
			
			fileWriter.append(startStr);
			fileWriter.append(input);
			fileWriter.append(endStr);
			
			fileWriter.flush();
			fileWriter.close();
			print("File: " + FILENAME + "." + FILEEXT + " Saved.");
		} else {
			format = new SimpleDateFormat("yyyyMMddHHmm");
			File oldFile = new File(FILENAME + "_" + format.format(date) + "." + FILEEXT);
			file.renameTo(oldFile);
			print("File: " + FILENAME + "_" + format.format(date) + "." + FILEEXT + " Saved.");
			saveResult(input);
		}
		
	}
	private String parseStrToXML(String inputStr) throws Exception {
		Document doc = Jsoup.parse(inputStr);
		doc.select(".b600").remove();
		doc.select("script").remove();
		return doc.toString();
	}

	private static void print(Object s) {
        System.out.println(s);
    }
}
