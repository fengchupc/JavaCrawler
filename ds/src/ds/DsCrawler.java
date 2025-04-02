package ds;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * @author fengc
 *
 */
public class DsCrawler {
	private String url = "https://www.ds44.xyz";
	private final String URLEXTEND = "/index.php?page=";
	private final String USERAGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36";
	
	private int pages = 0;
	private String spliter = ";";
	private String[] keywords = {};
	private LinkedHashMap<String, String> linkAddresses = new LinkedHashMap<String, String>();
	private DateFormat runDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S");
	private DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
    private Date date = new Date();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DsCrawler dc = new DsCrawler();
		try {
			dc.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void run() throws HttpStatusException, IOException, InterruptedException {
        print("Start: " + this.runDateFormat.format(this.date)); //2016/11/16 12:08:43.456
        
        this.readProperties();
        
        for(int page = 1; page <= this.pages; page++) {
			processWebPage(this.url + this.URLEXTEND + page, page);
        }
        
        this.saveResult();
        this.date = new Date();
        print("End: " + this.runDateFormat.format(this.date));
	}
	
	private void print(Object s) {
        System.out.println(s);
    }
	
	private void readProperties() throws IOException {
		Properties prop = new Properties();
		InputStream in = new BufferedInputStream (new FileInputStream("resource/config.properties"));
		prop.load(new InputStreamReader(in, "utf-8"));
		
		String keywords = (String) prop.get("keywords");
		this.keywords = keywords.split(",");
		this.spliter = (String) prop.get("spliter");
		this.pages = Integer.parseInt((String) prop.get("pages"));
	}
	private void processWebPage(String url, int page) throws IOException, HttpStatusException, InterruptedException {
		 print(url);
		 String strHead = "document.write(d('";
		 String strTail = "'));";
		 int startIdx = strHead.length();
		 Elements liEle = null;
		 Elements scriptEle = null;
		 String mapKey, mapValue;
		 String base64Str = null;
		 System.setProperty("https.protocols", "TLSv1");
	     Response searchResponse = Jsoup.connect(url).method(Connection.Method.GET)
	    		.header("Host", "ds42.xyz")
                .header("User-Agent", this.USERAGENT)
				.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
				.header("accept-language", "zh-CN")
				.header("cache-control", "no-cache")
				.header("cookie", "__cfduid=d05f6db98d33838bcf897505a0b2616981586249091; HstCfa4354477=1586249092710; HstCmu4354477=1586249092710; c_ref_4354477=http%3A%2F%2Fds44.xyz%2F; HstCla4354477=1586250573517; HstPn4354477=5; HstPt4354477=5; HstCnv4354477=1; HstCns4354477=2; __dtsu=6D0015862490942C0BD2CEA32F18C022")
				.header("dnt", "1")
				.header("pragma", "no-cache")
				.header("referer", "http://ds42.xyz/")
				.header("upgrade-insecure-requests", "1")
				.execute();
	     Document parseResult = searchResponse.parse();
	     
	     if(parseResult.select("body").first().childNodeSize() == 0) {
	    	 String str = parseResult.select("head script").html();
	    	 String pattern = "(http|https)://(www.)?(\\w+(\\.)?)+";
	    	 Pattern p = Pattern.compile(pattern);
	    	 Matcher matcher = p.matcher(str);
	    	 matcher.find();
	    	 this.url = matcher.group();
	    	 this.processWebPage(this.url + this.URLEXTEND + page, page);	    	 
	     }
	     liEle = parseResult.select("ul.list li");
	     for (Element li : liEle) {
			 scriptEle = li.select("a").first().children();
	    	 if (scriptEle.hasAttr("type")) {
				String linkDate = li.text();
	    		 mapKey = this.url + li.select("a").attr("href");
	    		 base64Str = scriptEle.html().substring(scriptEle.html().indexOf(strHead) + startIdx, scriptEle.html().lastIndexOf(strTail));
				 mapValue = new String(Base64.getDecoder().decode(base64Str), "UTF8");
				 mapValue = mapValue + this.spliter + linkDate;
	    		 this.linkAddresses.put(mapKey, mapValue);
	    	 }
	     }
	}
	
	private void saveResult() throws IOException {
		this.date = new Date();
		print("File write start:"+this.date);

		Iterator<Map.Entry<String, String>> iterator = this.linkAddresses.entrySet().iterator();
		String fileName = "result_" + this.fileDateFormat.format(this.date) + ".csv";
		File file = new File(fileName);
		File searchResultFile = new File("search_"+fileName);
		Writer csvWriter,resultCSVWrite;
		
		if(!file.exists() || !searchResultFile.exists()) {
			csvWriter = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8);
			resultCSVWrite = new OutputStreamWriter(new FileOutputStream(searchResultFile, true), StandardCharsets.UTF_8);
			while (iterator.hasNext()) {
			    Map.Entry<String, String> entry = iterator.next();
			    csvWriter.append(entry.getKey());
			    csvWriter.append(this.spliter);
        		csvWriter.append(entry.getValue());
        		csvWriter.append("\n");
        		for(String keyword : this.keywords) {
        			if(entry.getValue().indexOf(keyword) > 0) {
        				resultCSVWrite.append(entry.getKey());
        				resultCSVWrite.append(this.spliter);
        				resultCSVWrite.append(entry.getValue());
        				resultCSVWrite.append("\n");
        			}
        		}
			}
			csvWriter.flush();
	        csvWriter.close();
	        
	        resultCSVWrite.flush();
	        resultCSVWrite.close();
		}
		this.date = new Date();
		print("File write done at: "+this.date);
	}
}
