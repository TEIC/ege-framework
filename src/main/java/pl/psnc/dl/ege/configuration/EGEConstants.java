package pl.psnc.dl.ege.configuration;

import java.io.File;
import java.util.Properties;
import java.io.FileInputStream;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/*
 * Additional useful static data.
 * 
 * @author mariuszs
 */


public final class  EGEConstants {
        public static final Properties oxgProps = new Properties();
        static String whereami = EGEConstants.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        static String PATH = whereami.substring(0, whereami.lastIndexOf(File.separator));
	private final static Logger LOGGER = LogManager.getLogger(EGEConfigurationManager.class.getName());
	    
        static {
            try {
                LOGGER.debug(PATH);
				oxgProps.load(new FileInputStream(PATH + File.separator + "oxgarage.properties"));
	    } catch (java.io.IOException e) {
                try {
                    oxgProps.load(new FileInputStream(PATH + File.separator + "oxgarage.properties"));
                } catch (java.io.IOException ex) {
                    LOGGER.error("Could not read file /etc/oxgarage.properties or " + PATH 
                            + File.separator + "oxgarage.properties" );
                }
            }
	    
	}

	
        /**
	 * EGE temporary files directory
	 */
        public static final String OXGAPP = oxgProps.getProperty("OXGARAGE","/var/cache/oxgarage/");
        public static final String TEIROOT = oxgProps.getProperty("TEI","/usr/share/xml/tei/");
        public static final String OpenOfficeConfig = oxgProps.getProperty("OpenOfficeConfig","/usr/lib/libreoffice/");
        public static final String DEFAULT_LOCALE = oxgProps.getProperty("defaultLocale","en"); 
        public static final String DEFAULT_PROFILE = oxgProps.getProperty("defaultProfile","default");
		public static final String MEIROOT = oxgProps.getProperty("MEI","/usr/share/xml/mei/");
	// name for document family consisting of text documents
	public static final String TEXTFAMILY = "Documents";
	public static final String TEXTFAMILYCODE = "text";

	// name for document family consisting of spreadsheet documents
	public static final String SPREADSHEETFAMILY = "Spreadsheets";
	public static final String SPREADSHEETFAMILYCODE = "spreadsheet";

	// name for document family consisting of presentation documents
	public static final String PRESENTATIONFAMILY = "Presentations";
	public static final String PRESENTATIONFAMILYCODE = "presentation";

	// default name for documents from unrecognized family
	public static final String DEFAULTFAMILY = "Other documents";

	public static final String TEMP_PATH = OXGAPP + "temp";
	public static final String EGE_EXT_DIRECTORY = OXGAPP + "extensions";
	
	/**
	 * EGE data buffer temporary files directory 
	 */
	public static final String BUFFER_TEMP_PATH = TEMP_PATH + File.separator + "buff";

	static {
	    boolean success = new File(BUFFER_TEMP_PATH).mkdirs();
	    if (!success) {
			LOGGER.error("Could not create dir " + BUFFER_TEMP_PATH);
	    }
	}
	static {
	    boolean success = new File(EGE_EXT_DIRECTORY).mkdirs();
	    if (!success) {
			LOGGER.error("Could not create dir " + EGE_EXT_DIRECTORY);
	    }
	}
		

	/**
	 * Returns appropriate name of text family based on its code name
	 */
	public static String getType(String typeCode) {
		if(typeCode.equals(TEXTFAMILYCODE))         return TEXTFAMILY;
		if(typeCode.equals(SPREADSHEETFAMILYCODE))  return SPREADSHEETFAMILY;
		if(typeCode.equals(PRESENTATIONFAMILYCODE)) return PRESENTATIONFAMILY;
		return DEFAULTFAMILY;
	}
}