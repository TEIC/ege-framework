package pl.psnc.dl.ege.webapp.config;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides mime to file extension map.
 * Map parameters are read from .xml configuration file.
 * 
 * @author mariuszs
 *
 */
public final class MimeExtensionProvider extends DefaultHandler
{
	private static MimeExtensionProvider instance = null;
	
	private static final Map<String,String> MIME_EXT_MAP = new HashMap<String,String>();
	
	private MimeExtensionProvider(String configFile){
		try{
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			SAXParser parser = parserFactory.newSAXParser();
			XMLReader xmlReader = parser.getXMLReader();
			xmlReader.setContentHandler(this);
			xmlReader.parse(new InputSource(new FileInputStream(new File(configFile))));
		}catch(Throwable e){
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static MimeExtensionProvider getInstance(String configPath){
		if(instance == null){
			return new MimeExtensionProvider(configPath);
		}
		return instance;
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes)
		throws SAXException
	{
		String mime = attributes.getValue("mime");
		String fileExt = attributes.getValue("ext");
		MIME_EXT_MAP.put(mime, fileExt);
	}
	
	public String getFileExtension(String mimeType){
		String response = MIME_EXT_MAP.get(mimeType);
		if(response == null){
			return "";
		}
		return response;
	}
}
