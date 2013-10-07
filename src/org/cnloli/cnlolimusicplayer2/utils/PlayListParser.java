package org.cnloli.cnlolimusicplayer2.utils;
/*
 * CNLOLI.NET
 * Smeister
 * 
 * PlayList Parser
 * 
 * Public function: getPlayList(), returns: String[][], where: 0=Path, 1=Name
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class PlayListParser {
	
	public String[][] getPlayList()
	{
		try
		{
			Document doc = this.getListDoc(this.convertPlayListXMLInputStream(this.getPlayListXML()));
			
			NodeList nsPath = this.getListNS(doc, "//list/m[1]/m/@src");
			NodeList nsName = this.getListNS(doc, "//list/m[1]/m/@label");
			
			String list[][] = new String[nsPath.getLength()][2];

			for(int i=0;i<nsPath.getLength();i++)
			 {
				  list[i][0] = nsPath.item(i).getNodeValue();
				  list[i][1] = nsName.item(i).getNodeValue();
			 }
			
			return list;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	private Document getListDoc(InputStream Listxml)
	{
		try
		{
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		    domFactory.setNamespaceAware(false); 
		    DocumentBuilder builder = domFactory.newDocumentBuilder();
		    Document doc = builder.parse(Listxml);
		    
		    return doc;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	private NodeList getListNS(Document doc, String xpathString)
	{
		try
		{
			 XPath xpath = XPathFactory.newInstance().newXPath();
			 XPathExpression expr;
			 Object result; 
			 
			 expr = xpath.compile(xpathString);
			 result = expr.evaluate(doc, XPathConstants.NODESET);
			 
			 NodeList ns = (NodeList) result;
			 
			 return ns;	
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	private InputStream convertPlayListXMLInputStream(String Listxml)
	{
		try {
			return new ByteArrayInputStream(Listxml.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String getPlayListXML()
	{
		try {
    		String rssURI = "http://music.cnloli.net/mlist/list.xml";
			URL url = new URL(rssURI);
			// 打开http连接
			HttpURLConnection urlConn=(HttpURLConnection)url.openConnection(); 
			// to escape unauthed check
			urlConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			// 得到读取的内容(流)  
            InputStreamReader in = new InputStreamReader(urlConn.getInputStream(),"UTF-8");  
            // 为输出创建BufferedReader  
            BufferedReader buffer = new BufferedReader(in);  
            String inputLine = null;
            String resultData = "";
            StringBuilder resultBuilder = new StringBuilder();
            // 使用循环来读取获得的数据  
            while (((inputLine = buffer.readLine()) != null))  
            {  
                // 在每一行后面加上一个"\n"来换行  
            	resultBuilder.append(inputLine + "\n");
                //resultData += inputLine + "\n";  
            }           
            //关闭InputStreamReader  
            in.close();  
            //关闭http连接  
            urlConn.disconnect(); 

            resultData = resultBuilder.toString();
			return resultData.replace("&",	"&amp;");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return "RSS Not found!";
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return "Cannot establish connection!\n" +
					e.toString();
		}  
    	 catch (Exception e)
 		{
// 			e.printStackTrace();
 			return "Unknown exceptions";
 		}
	}
}
