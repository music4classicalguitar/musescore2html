package musescore2html;

import java.util.ArrayList;

public class Arguments {

	public static class Logging {
		public String logMessage;
		public int logCode;

		public Logging(String message, int code) {
			logMessage = message;
			logCode = code;
		}
	}

	public Config config;
	public String language;
	public Translations translations;
	
	public static enum INTERFACE_TYPE { CLI, GUI };
	public INTERFACE_TYPE interfaceType = INTERFACE_TYPE.GUI;
	
	public static enum FILE_OPTION { ONLY_IF_NEW, ONLY_IF_NEWER, REPLACE }
	public FILE_OPTION fileOption = FILE_OPTION.ONLY_IF_NEW;
	
	public static enum INDEX_FILE_OPTION { INDEX_ONLY_IF_NEW, INDEX_ONLY_IF_NEWER, INDEX_REPLACE }
	public INDEX_FILE_OPTION indexFileOption = INDEX_FILE_OPTION.INDEX_ONLY_IF_NEW;
	
	public static enum GENERATE_INDEX_FILE_OPTION { NONE, INDEX_NO_HTML, INDEX_TO_HTML }
	public GENERATE_INDEX_FILE_OPTION generateIndexFileOption = GENERATE_INDEX_FILE_OPTION.NONE;

	public static enum LOG_LEVEL { SILENT, QUIET, NORMAL, VERBOSE, EXTREME };
	public LOG_LEVEL logLevel = LOG_LEVEL.NORMAL;
	
	public boolean isCaseSensitive = true;
	public String cfgPath, museScore, outputDirectory, indexFileName;
	public ArrayList<String> scores = new ArrayList<String>();
	public ArrayList<Logging> logging = new ArrayList<Logging>();
	public boolean generateHtml = true;
	public boolean generateIndexAll = false;
	public boolean checkOnly = false;
	public boolean showVersion = false;
	public boolean showHelp = false;
	public int errors = 0;

	public void setMissing() {
		try {
			if (translations==null) {
				if (config==null) config = new Config();
				translations = config.getTranslations();
			} else {
				if (config==null) config=new Config(translations);
				translations = config.getTranslations();
			}
			language=translations.getLanguage();
			isCaseSensitive = config.isCaseSensitive();
		} catch (Exception exc) {
			exc.printStackTrace();
			System.err.println("Exception: '"+exc.getMessage()+"'");
		}
	}

}
