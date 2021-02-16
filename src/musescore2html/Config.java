package musescore2html;

import org.apache.tools.ant.MuseScore2HtmlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.FileSystems;

public class Config {

	private Translations translations;

	public static enum OSId { WINDOWS, OSX, UNIX };
	private OSId osId;
	private String language;

	private static final String userName = System.getProperty("user.name");
	private static final String userHomeDirectory = System.getProperty("user.home");

	private String defaultConfigDirectory, configDirectory;
	private String configFileName = Version.APPLICATION_ID+".xml", configPath, configLastUsedFileName = Version.APPLICATION_ID+".LastUsed.xml", configLastUsedPath;
	private File configFile, configLastUsedFile;

	private String museScore1="", museScore2="",museScore3="", scoreDirectory1="", scoreDirectory2="", scoreDirectory3="";
	private String defaultMuseScore="", museScore, lastUsedMuseScore, lastUsedMuseScoreDirectory, defaultScoreDirectory, lastUsedScoreDirectory, outputDirectory, lastUsedOutputDirectory;

	public static String getVendorId() {
		return Version.VENDOR_ID;
	}

	public static String getApplicationId() {
		return Version.APPLICATION_ID;
	}

	public static String getVersion() {
		return Version.VERSION;
	}

	public static OSId getOSId() {
		OSId osId = OSId.UNIX;
		String osName = System.getProperty("os.name");
		if (osName != null) {
			if (osName.toLowerCase().startsWith("mac os x")) {
				osId = OSId.OSX;
			} else if (osName.contains("Windows")) {
				osId = OSId.WINDOWS;
			}
		}
		return osId;
	}

	private void setDefaultMuseScoreSettings() {
		String foundFiles[];
		switch (osId) {
			case WINDOWS:
				// MuseScore 1 : mscore.exe, 2 : MuseScore.exe, 3 : MuseScore3.exe
				foundFiles=MuseScore2HtmlUtils.findItems("C:\\Program Files*\\MuseScore*\\bin\\M*Score.exe", MuseScore2HtmlUtils.TYPE.FILE, false);
				if (foundFiles.length>0) {
					for (int i=0;i<foundFiles.length;i++) {
						if (foundFiles[i].indexOf("MuseScore 3")>=0) museScore3 = foundFiles[i];
						else if (foundFiles[i].indexOf("MuseScore 2")>=0) museScore2 = foundFiles[i];
						else museScore1 = foundFiles[i];
					}
				}
				foundFiles=MuseScore2HtmlUtils.findItems(userHomeDirectory+File.separator+"Documents"+File.separator+"MuseScore*", MuseScore2HtmlUtils.TYPE.FILE, false);
				if (foundFiles.length>0) {
					for (int i=0;i<foundFiles.length;i++) {
						if (foundFiles[i].indexOf("MuseScore3")>=0) scoreDirectory3 = foundFiles[i];
						else if (foundFiles[i].indexOf("MuseScore2")>=0) scoreDirectory2 = foundFiles[i];
						else scoreDirectory1 = foundFiles[i];
					}
				} else defaultScoreDirectory = userHomeDirectory+File.separator+"Documents";
				defaultConfigDirectory = userHomeDirectory + File.separator + "Application Data" + 
					File.separator + Version.VENDOR_ID  + File.separator + Version.APPLICATION_ID  + File.separator;
				break;
			case OSX:
				foundFiles=MuseScore2HtmlUtils.findItems("/Applications/MuseScore*.app/Contents/MacOS/mscore", MuseScore2HtmlUtils.TYPE.FILE, false);
				if (foundFiles.length>0) {
					for (int i=0;i<foundFiles.length;i++) {
						if (foundFiles[i].indexOf("MuseScore 3")>=0) museScore3 = foundFiles[i];
						else if (foundFiles[i].indexOf("MuseScore 2")>=0) museScore2 = foundFiles[i];
						else museScore1 = foundFiles[i];
					}
				}
				foundFiles=MuseScore2HtmlUtils.findItems(userHomeDirectory+File.separator+"Documents"+File.separator+"MuseScore*", MuseScore2HtmlUtils.TYPE.FILE, false);
				if (foundFiles.length>0) {
					defaultScoreDirectory = foundFiles[foundFiles.length-1];
					for (int i=0;i<foundFiles.length;i++) {
						if (foundFiles[i].indexOf("MuseScore3")>=0) scoreDirectory3 = foundFiles[i];
						else if (foundFiles[i].indexOf("MuseScore2")>=0) scoreDirectory2 = foundFiles[i];
						else scoreDirectory1 = foundFiles[i];
					}
				} else defaultScoreDirectory = userHomeDirectory+File.separator+"Documents";
				defaultConfigDirectory = userHomeDirectory + File.separator + "Library" + File.separator + "Application Support" +
					File.separator + Version.VENDOR_ID  + File.separator + Version.APPLICATION_ID  + File.separator;
				break;
			case UNIX:
				foundFiles=MuseScore2HtmlUtils.findItems(userHomeDirectory+File.separator+".local"+File.separator+"bin"+File.separator+"MuseScore*.AppImage", MuseScore2HtmlUtils.TYPE.FILE, false);
				if (foundFiles.length>0) {
					for (int i=0;i<foundFiles.length;i++) {
						if (foundFiles[i].indexOf("MuseScore-3")>=0) museScore3 = foundFiles[i];
						else if (foundFiles[i].indexOf("MuseScore-2")>=0) museScore2 = foundFiles[i];
						else museScore1 = foundFiles[i];
					}
				} else museScore3 = "/usr/bin/mscore";
				scoreDirectory3 = userHomeDirectory;
				defaultConfigDirectory = userHomeDirectory + File.separator + ".config" +
					File.separator + Version.VENDOR_ID  + File.separator + Version.APPLICATION_ID  + File.separator;
				break;
		}
	}

	public void setDefaults() throws Exception {
		osId = getOSId();

		setDefaultMuseScoreSettings();

		if (!museScore3.equals("")) defaultMuseScore=museScore3;
		else if (!museScore2.equals("")) defaultMuseScore=museScore2;
		else if (!museScore1.equals("")) defaultMuseScore=museScore1;

		if (!scoreDirectory3.equals("")) defaultScoreDirectory=scoreDirectory3;
		else if (!scoreDirectory2.equals("")) defaultScoreDirectory=scoreDirectory2;
		else if (!scoreDirectory1.equals("")) defaultScoreDirectory=scoreDirectory1;

		if (defaultMuseScore.equals("")) throw new Exception(translations.translate("musescore.not.specified"));
		File exec = new File(defaultMuseScore);
		if (!exec.exists()) throw new Exception(translations.translate(new String[]{"musescore.not.found", defaultMuseScore}));
		if (!exec.canExecute()) throw new Exception(translations.translate(new String[]{"musescore.not.executable", defaultMuseScore}));
		museScore = defaultMuseScore;

		configDirectory = defaultConfigDirectory;
		configPath = configDirectory+File.separator+configFileName;
		configFile = new File(configPath);

		configLastUsedPath = configDirectory+File.separator+configLastUsedFileName;
		configLastUsedFile = new File(configLastUsedPath);

		outputDirectory = System.getProperty("user.dir");

		lastUsedMuseScore = museScore;
		lastUsedMuseScoreDirectory = FileSystems.getDefault().getPath(museScore).toString();
		lastUsedOutputDirectory = userHomeDirectory;
		lastUsedScoreDirectory = defaultScoreDirectory;

		translations = new Translations();
		language = translations.getLanguage();
	}

	public Translations getTranslations() {
		return translations;
	}

	public String getLanguage() {
		return language;
	}

	public String getUserName() {
		return userName;
	}

	public String getDefaultMuseScore() {
		return defaultMuseScore;
	}

	public String getMuseScore() {
		return museScore;
	}

	public void setMuseScore(String mscore) {
		museScore = mscore;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public String getDefaultConfigDirectory() {
		return defaultConfigDirectory;
	}

	public String getConfigDirectory() {
		return configDirectory;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public String getConfigFile() {
		return configFile.toString();
	}

	public void readConfig(File configFile) throws Exception {
		if (configFile==null) throw new Exception(translations.translate("configfile.error.null"));
		if (!configFile.exists()) throw new Exception(translations.translate(new String[]{"configfile.not.exists", configFile.toString()}));
		FileInputStream fis = null;
		Properties prop = null;
		String value;
		try {
			fis = new FileInputStream(configFile);
			prop = new Properties();
			prop.load(fis);
			value = prop.getProperty("museScore");
			if (value!=null) museScore = value;
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"configfile.error.read.message", configFile.toString(),exc.getMessage()}));
			else throw new Exception(translations.translate(new String[]{"configfile.error.read", configFile.toString()}));
		} finally {
			if (fis!=null) fis.close();
		}
	}

	public void readConfig(String cfgPath) throws Exception {
		setDefaults();
		readConfig(new File(cfgPath));
	}

	public void getConfig(String cfgPath) throws Exception {
		setDefaults();
		readConfig(cfgPath);
	}

	public void getConfig() throws Exception {
		setDefaults();
		if (configFile.exists()) readConfig(configFile);
		readConfigLastUsed();
	}

	public void writeConfig() throws Exception {
		if (!configFile.exists()) {
			try {
				File configDir = new File(configDirectory);
				if (!configDir.exists()) {
					if (!configDir.mkdirs()) {
						if (!configDir.exists()) {
							throw new Exception(translations.translate(new String[]{"configfile.error.createdirectory", configDirectory}));
						}
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
				if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"configfile.error.createdirectory.message", configDirectory,exc.getMessage()}));
				else throw new Exception(translations.translate(new String[]{"configfile.error.createdirectory", configDirectory}));
			}
		}
		FileOutputStream fos = null;
		Properties props = null;
		String value;
		try {
			fos = new FileOutputStream(configFile);
			props = new Properties();
			if (language!=null) props.setProperty("language", language);
			if (museScore!=null) props.setProperty("museScore", museScore);
			props.storeToXML(fos,translations.translate("configuration"),"UTF-8");
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"configfile.error.write", configFile.toString(),exc.getMessage()}));
			else throw new Exception(translations.translate(new String[]{"configfile.error.write", configFile.toString()}));
		} finally {
			if (fos!=null) fos.close();
		}
	}

    public Config(String cfgPath) throws Exception {
    	translations = new Translations();
    	language = translations.getLanguage();
    	getConfig(cfgPath);
    }

    public Config(Translations translations, String cfgPath) throws Exception {
    	this.translations = translations;
    	language = translations.getLanguage();
    	getConfig(cfgPath);
    }

    public Config() throws Exception {
    	translations = new Translations();
    	getConfig();
    }

    public Config(Translations translations) throws Exception {
    	this.translations = translations;
    	getConfig();
    }

	public void readConfigLastUsed() throws Exception {
		if (!configLastUsedFile.exists()) return;
		FileInputStream fis = null;
		Properties prop = null;
		String value;
		try {
			fis = new FileInputStream(configLastUsedFile);
			prop = new Properties();
			prop.load(fis);
			value = prop.getProperty("lastUsedOutputDirectory");
			if (value!=null) lastUsedOutputDirectory = value;
			value = prop.getProperty("lastUsedScoreDirectory");
			if (value!=null) lastUsedScoreDirectory = value;
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"configlastusedfile.error.read.message", configLastUsedFile.toString(),exc.getMessage()}));
			else throw new Exception(translations.translate(new String[]{"configlastusedfile.error.read", configLastUsedFile.toString()}));
		} finally {
			if (fis!=null) fis.close();
		}
	}

	public String getLastUsedMuseScoreDirectory() {
		return lastUsedMuseScoreDirectory;
	}

	public void setLastUsedMuseScoreDirectory(String mscoreDir) {
		lastUsedMuseScoreDirectory = mscoreDir;
	}

	public String getLastUsedMuseScore() {
		return lastUsedMuseScore;
	}

	public void setLastUsedMuseScore(String mscore) {
		lastUsedMuseScore = mscore;
	}

	public String getLastUsedScoreDirectory() {
		return lastUsedScoreDirectory;
	}

	public void setLastUsedScoreDirectory(String dir) {
		lastUsedScoreDirectory = dir;
	}

	public String getLastUsedOutputDirectory() {
		return lastUsedOutputDirectory;
	}

	public void setLastUsedOutputDirectory(String dir) {
		lastUsedOutputDirectory = dir;
	}

	public void writeConfigLastUsed() throws Exception {
		if (!configLastUsedFile.exists()) {
			try {
				File configDir = new File(configDirectory);
				if (!configDir.exists()) {
					if (!configDir.mkdirs()) {
						if (!configDir.exists()) {
							throw new Exception(translations.translate(new String[]{"configlastusedfile.error.createdirectory", configDirectory}));
						}
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
				if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"configlastusedfile.error.createdirectory.message", configLastUsedFile.toString(),exc.getMessage()}));
				else throw new Exception(translations.translate(new String[]{"configlastusedfile.error.createdirectory", configLastUsedFile.toString()}));
			}
		}
		FileOutputStream fos = null;
		Properties props = null;
		String value;
		try {
			fos = new FileOutputStream(configLastUsedFile);
			props = new Properties();
			if (lastUsedMuseScore!=null) props.setProperty("lastUsedMuseScore", lastUsedMuseScore);
			if (lastUsedOutputDirectory!=null) props.setProperty("lastUsedOutputDirectory", lastUsedOutputDirectory);
			if (lastUsedScoreDirectory!=null) props.setProperty("lastUsedScoreDirectory", lastUsedScoreDirectory);
			props.storeToXML(fos,"last used values","UTF-8");
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"configlastusedfile.error.write", configLastUsedFile.toString(),exc.getMessage()}));
			else throw new Exception(translations.translate(new String[]{"configlastusedfile.error.write", configLastUsedFile.toString()}));
		} finally {
			if (fos!=null) fos.close();
		}
	}
}
