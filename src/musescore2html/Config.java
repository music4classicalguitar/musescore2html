package musescore2html;

import org.apache.tools.ant.MuseScore2HtmlUtils;

import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.FileSystems;

import javax.swing.UIManager;

public class Config {

	private Translations translations;

	public static enum OSId { WINDOWS, OSX, UNIX };
	private OSId osId;
	private String language;
	private boolean hasCaseSensitiveFileSystem = MuseScore2HtmlUtils.isCaseSensitiveFileSystem(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir")));

	private static final String userName = System.getProperty("user.name");
	private static final String userHomeDirectory = System.getProperty("user.home");

	private String defaultConfigDirectory, configDirectory;
	private String configFileName = Version.APPLICATION_ID+".xml", configPath, configLastUsedFileName = Version.APPLICATION_ID+".LastUsed.xml", configLastUsedPath;
	private File configFile, defaultConfigFile, configLastUsedFile;

	private String museScore1="", museScore2="",museScore3="", scoreDirectory1="", scoreDirectory2="", scoreDirectory3="";
	private String defaultMuseScore="", museScore, lastUsedMuseScore, scoreDirectory, lastUsedScoreDirectory, outputDirectory, lastUsedOutputDirectory;
	private String[] museScoresFound ;
	private String lookandfeel = UIManager.getSystemLookAndFeelClassName();
	private ArrayList<String> temp;

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

	private void setDefaultMuseScoreSettingsWindows() {
		String[] dirs, foundFiles;
		dirs=MuseScore2HtmlUtils.findItems("C:\\Program Files*", MuseScore2HtmlUtils.TYPE.DIRECTORY, false);
		if (dirs.length>0) {
			temp = new ArrayList<String>();
			for (int i=0; i<dirs.length; i++) {
				foundFiles=MuseScore2HtmlUtils.findItems(dirs[i]+"\\MuseScore*", MuseScore2HtmlUtils.TYPE.DIRECTORY, false);
				temp.addAll(Arrays.asList(foundFiles));
			}
			if (temp.size()>0) {
				dirs=temp.toArray(new String[] {});
				temp = new ArrayList<String>();
				for (int i=0; i<dirs.length; i++) {
					foundFiles=MuseScore2HtmlUtils.findItems(dirs[i]+"\\bin\\M*Score*.exe", MuseScore2HtmlUtils.TYPE.FILE, false);
					for (int j=0; j<foundFiles.length; j++) {
						if (!foundFiles[j].toLowerCase().endsWith("-crash-reporter.exe")) temp.add(foundFiles[j]);
					}
				}
				if (temp.size()>0) {
					foundFiles=temp.toArray(new String[] {});
					if (foundFiles.length>0) {
						for (int i=0;i<foundFiles.length;i++) {
							if (foundFiles[i].indexOf("MuseScore 3")>=0) museScore3 = foundFiles[i];
							else if (foundFiles[i].indexOf("MuseScore 2")>=0) museScore2 = foundFiles[i];
							else museScore1 = foundFiles[i];
						}
					}
				}
			}
		}
	}

	private void setDefaultMuseScoreSettings() {
		String foundFiles[];
		switch (osId) {
			case WINDOWS:
				// MuseScore 1 : mscore.exe, 2 : MuseScore.exe, 3 : MuseScore3.exe
				/*
				dir "C:\\Program Files*\\MuseScore*\\bin\\M*Score.exe"
				gives error : The filename, directory name, or volume label syntax is incorrect.
				foundFiles=MuseScore2HtmlUtils.findItems("C:\\Program Files*\\MuseScore*\\bin\\M*Score.exe", MuseScore2HtmlUtils.TYPE.FILE, false);
				if (foundFiles.length>0) {
					for (int i=0;i<foundFiles.length;i++) {
						if (foundFiles[i].indexOf("MuseScore 3")>=0) museScore3 = foundFiles[i];
						else if (foundFiles[i].indexOf("MuseScore 2")>=0) museScore2 = foundFiles[i];
						else museScore1 = foundFiles[i];
					}
				}
				*/
				setDefaultMuseScoreSettingsWindows();
				foundFiles=MuseScore2HtmlUtils.findItems(userHomeDirectory+File.separator+"Documents"+File.separator+"MuseScore*", MuseScore2HtmlUtils.TYPE.FILE, false);
				if (foundFiles.length>0) {
					for (int i=0;i<foundFiles.length;i++) {
						if (foundFiles[i].indexOf("MuseScore3")>=0) scoreDirectory3 = foundFiles[i];
						else if (foundFiles[i].indexOf("MuseScore2")>=0) scoreDirectory2 = foundFiles[i];
						else scoreDirectory1 = foundFiles[i];
					}
				} else scoreDirectory = userHomeDirectory+File.separator+"Documents";
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
					scoreDirectory = foundFiles[foundFiles.length-1];
					for (int i=0;i<foundFiles.length;i++) {
						if (foundFiles[i].indexOf("MuseScore3")>=0) scoreDirectory3 = foundFiles[i];
						else if (foundFiles[i].indexOf("MuseScore2")>=0) scoreDirectory2 = foundFiles[i];
						else scoreDirectory1 = foundFiles[i];
					}
				} else scoreDirectory = userHomeDirectory+File.separator+"Documents";
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
		defaultConfigFile = new File(defaultConfigDirectory+configFileName);
	}

	public void setDefaults() throws Exception {
		osId = getOSId();

		setDefaultMuseScoreSettings();
		if (!museScore3.equals("")) defaultMuseScore=museScore3;
		else if (!museScore2.equals("")) defaultMuseScore=museScore2;
		else if (!museScore1.equals("")) defaultMuseScore=museScore1;
		temp = new ArrayList<String>();
		if (!museScore3.equals("")) temp.add(museScore3);
		if (!museScore2.equals("")) temp.add(museScore2);
		if (!museScore1.equals("")) temp.add(museScore1);
		museScoresFound = temp.toArray(new String[] {});

		if (!scoreDirectory3.equals("")) scoreDirectory=scoreDirectory3;
		else if (!scoreDirectory2.equals("")) scoreDirectory=scoreDirectory2;
		else if (!scoreDirectory1.equals("")) scoreDirectory=scoreDirectory1;
		temp = new ArrayList<String>();

		if (defaultMuseScore.equals("")) throw new Exception(translations.translate("musescore.not.specified"));
		File exec = new File(defaultMuseScore);
		if (!exec.exists()) throw new Exception(translations.translate(new String[] {"musescore.not.found", defaultMuseScore}));
		if (!exec.canExecute()) throw new Exception(translations.translate(new String[] {"musescore.not.executable", defaultMuseScore}));
		museScore = defaultMuseScore;

		configDirectory = defaultConfigDirectory;
		configPath = configDirectory+File.separator+configFileName;
		configFile = new File(configPath);

		configLastUsedPath = configDirectory+File.separator+configLastUsedFileName;
		configLastUsedFile = new File(configLastUsedPath);

		outputDirectory = System.getProperty("user.home");

		lastUsedMuseScore = museScore;
		lastUsedOutputDirectory = userHomeDirectory;
		lastUsedScoreDirectory = scoreDirectory;

		if (translations==null) {
			translations = new Translations();
			language = translations.getLanguage();
		}
		
		readConfigLastUsed();

		museScore = lastUsedMuseScore;
		outputDirectory = lastUsedOutputDirectory;
		scoreDirectory = lastUsedScoreDirectory;
	}

	public Translations getTranslations() {
		return translations;
	}

	public boolean isCaseSensitive() {
		return hasCaseSensitiveFileSystem;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String value) {
		language = value;
	}

	public String getUserName() {
		return userName;
	}

	public String getDefaultMuseScore() {
		return defaultMuseScore;
	}

	public String getLookAndFeel() {
		return lookandfeel;
	}

	public void setLookAndFeel(String value) {
		lookandfeel = value;
	}

	public String getMuseScore() {
		return museScore;
	}

	public void setMuseScore(String value) {
		museScore = value;
	}

	public String[] getMuseScoresFound() {
		return museScoresFound.clone();
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

	public String getDefaultConfigFile() {
		return defaultConfigFile.toString();
	}

	public String getConfigFile() {
		return configFile.toString();
	}

	public void readConfig(File configFile) throws Exception {
		if (configFile==null) throw new Exception(translations.translate("configfile.error.null"));
		if (!configFile.exists()) throw new Exception(translations.translate(new String[] {"configfile.not.exists", configFile.toString()}));
		FileInputStream fis = null;
		Properties prop = null;
		String value;
		try {
			fis = new FileInputStream(configFile);
			prop = new Properties();
			prop.loadFromXML(fis);
			value = prop.getProperty("lookandfeel");
			if (value!=null) lookandfeel = value;
			value = prop.getProperty("language");
			if (value!=null) {
				language = value;
				translations.setLanguage(language);
			}
			value = prop.getProperty("museScore");
			if (value!=null) museScore = value;
		} catch(FileNotFoundException exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[] {"configfile.error.read.message", configFile.toString(),exc.getMessage()}));
			else throw new Exception(translations.translate(new String[] {"configfile.error.read", configFile.toString()}));
		} catch(IOException exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[] {"configfile.error.read.message", configFile.toString(),exc.getMessage()}));
			else throw new Exception(translations.translate(new String[] {"configfile.error.read", configFile.toString()}));
		} finally {
			if (fis!=null) fis.close();
		}
		boolean found = false;
		for (int i=0; i<museScoresFound.length; i++) {
			if (museScore.equals(museScoresFound[i])) {
				found = true;
				break;
			}
		}
		if (!found) {
			temp = new ArrayList<String>();
			temp.add(museScore);
			for (int i=0; i<museScoresFound.length; i++) {
				temp.add(museScoresFound[i]);
			}
			museScoresFound = temp.toArray(new String[] {});
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
	}

	public void writeDefaultConfig() throws Exception {
		configDirectory = defaultConfigDirectory;
		configPath = configDirectory+File.separator+configFileName;
		configFile = new File(configPath);
		writeConfig();
	}

	public void writeConfig(String configPath) throws Exception {
		this.configPath = configPath;
		configFile = new File(configPath);
		configDirectory = configFile.getParent();
		configFileName = configFile.getName();
		writeConfig();
	}
	
	public void writeConfig() throws Exception {
		if (!configFile.exists()) {
			try {
				File configDir = new File(configDirectory);
				if (!configDir.exists()) {
					if (!configDir.mkdirs()) {
						if (!configDir.exists()) {
							throw new Exception(translations.translate(new String[] {"configfile.error.creatingdirectory", configDirectory}));
						}
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
				if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[] {"configfile.error.creatingdirectory.message", configDirectory,exc.getMessage()}));
				else throw new Exception(translations.translate(new String[] {"configfile.error.creatingdirectory", configDirectory}));
			}
		}
		FileOutputStream fos = null;
		Properties props = null;
		try {
			fos = new FileOutputStream(configFile);
			props = new Properties();
			if (language!=null) props.setProperty("language", language);
			if (lookandfeel!=null) props.setProperty("lookandfeel", lookandfeel);
			if (museScore!=null) props.setProperty("museScore", museScore);
			props.storeToXML(fos,translations.translate("configuration"),"UTF-8");
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[] {"configfile.error.write.message", configFile.toString(),exc.getMessage()}));
			else throw new Exception(translations.translate(new String[] {"configfile.error.write", configFile.toString()}));
		} finally {
			if (fos!=null) fos.close();
		}
	}

    public Config(String cfgPath) throws Exception {
    	getConfig(cfgPath);
    }

    public Config(Translations translations, String cfgPath) throws Exception {
    	this.translations = translations;
    	this.language = this.translations.getLanguage();
    	getConfig(cfgPath);
    }

    public Config() throws Exception {
    	this.translations = new Translations();
    	this.language = this.translations.getLanguage();
    	getConfig();
    }

    public Config(Translations translations) throws Exception {
    	this.translations = translations;
    	this.language = this.translations.getLanguage();
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
			prop.loadFromXML(fis);
			value = prop.getProperty("lastUsedMuseScore");
			if (value!=null) lastUsedMuseScore = value;
			value = prop.getProperty("lastUsedOutputDirectory");
			if (value!=null) lastUsedOutputDirectory = value;
			value = prop.getProperty("lastUsedScoreDirectory");
			if (value!=null) lastUsedScoreDirectory = value;
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[] {"configlastusedfile.error.read.message", configLastUsedFile.toString(),exc.getMessage()}));
			else throw new Exception(translations.translate(new String[] {"configlastusedfile.error.read", configLastUsedFile.toString()}));
		} finally {
			if (fis!=null) fis.close();
		}
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
							throw new Exception(translations.translate(new String[] {"configlastusedfile.error.creatingdirectory", configDirectory}));
						}
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
				if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[] {"configlastusedfile.error.creatingdirectory.message", configLastUsedFile.toString(),exc.getMessage()}));
				else throw new Exception(translations.translate(new String[] {"configlastusedfile.error.creatingdirectory", configLastUsedFile.toString()}));
			}
		}
		FileOutputStream fos = null;
		Properties props = null;
		String value;
		try {
			fos = new FileOutputStream(configLastUsedFile);
			props = new Properties();
			if (lastUsedMuseScore!=null) props.setProperty("lastUsedMuseScore", lastUsedMuseScore);
			if (lastUsedScoreDirectory!=null) props.setProperty("lastUsedScoreDirectory", lastUsedScoreDirectory);
			if (lastUsedOutputDirectory!=null) props.setProperty("lastUsedOutputDirectory", lastUsedOutputDirectory);
			props.storeToXML(fos,"last used values","UTF-8");
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[] {"configlastusedfile.error.write.message", configLastUsedFile.toString(),exc.getMessage()}));
			else throw new Exception(translations.translate(new String[] {"configlastusedfile.error.write", configLastUsedFile.toString()}));
		} finally {
			if (fos!=null) fos.close();
		}
	}
	
	private void showSystemProperties() {
		Properties properties = System.getProperties();
		properties.forEach((k, v) -> System.out.println(k + ": '" + v +"'"));
	}
}
