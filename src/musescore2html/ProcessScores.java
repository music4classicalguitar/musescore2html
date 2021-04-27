package musescore2html;

import org.apache.tools.ant.MuseScore2HtmlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.text.Normalizer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.CopyOption;
import java.nio.file.StandardCopyOption;

import java.lang.Runtime;
import java.util.concurrent.Callable;

public class ProcessScores implements Callable<Integer>  {

	private ProcessData processData;
	private Translations translations;
	private Arguments arguments;
	private Comparator<Object> naturalOrderComparator = new NaturalOrderComparator(true);

	private final String jarDirectory=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	private final String rscDirectory=(new File(jarDirectory)).getParentFile().getParent();
	private final String xslDirectory=rscDirectory+File.separator+"xsl";
	private final String insDirectory=rscDirectory+File.separator+"install";

	private static final String tempDirectory=System.getProperty("java.io.tmpdir");
	private ArrayList<String[]> scoreParts;
   	String translateCheckOnly="";
   	private int scores_processed=0, scores_skipped=0, files_generated=0, files_installed=0, directories_created=0;

	private final static String extensions[]= {
		".metajson",
		"_space.json",
		".png",
		".ogg",
		".mp3",
		".html"
	};
	private int imageExtension=2;

	private String files2copy[]={
			"css"+File.separator+"ms_player.css",
			"js"+File.separator+"ms_player.js",
			"images"+File.separator+"media-playback-loop.svg",
			"images"+File.separator+"media-playback-metronome.svg",
			"images"+File.separator+"media-playback-pause.svg",
			"images"+File.separator+"media-playback-start.svg",
			"images"+File.separator+"media-skip-backward.svg",
			"images"+File.separator+"media-skip-forward.svg",
			"images"+File.separator+"window-close.svg"
		};

	private String files2copyForIndexFile[]={
			"css"+File.separator+"ms_player_index.css",
			"js"+File.separator+"ms_player_query.js",
			"ShowScore.html"
	};

	private int codes=0, errors=0;

	private void processInfo(Arguments.LOG_LEVEL logLevel, String message, int code) {
		codes=codes|code;
		if (code != 0) errors++;
		try {
			if (logLevel.ordinal()<=arguments.logLevel.ordinal()) {
				if (processData == null) {
					arguments.logging.add(new Arguments.Logging(message, code));
				} else {
					processData.addData(message,code);
				}
			}
		} catch (InterruptedException iexc) {
			iexc.printStackTrace();
			if (iexc.getMessage()!=null) System.err.println(translations.translate(new String[] {"exception.error.message", iexc.getMessage()}));
			else System.err.println(translations.getKey("exception.error"));

		}
	}

	private String validateMuseScore() {
		String foundMuseScore[]=MuseScore2HtmlUtils.findItems(arguments.museScore, MuseScore2HtmlUtils.TYPE.FILE, arguments.isCaseSensitive);
		if (foundMuseScore.length==0) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"musescore.not.found", arguments.museScore}),256);
			return "?";
		} else if (foundMuseScore.length>1) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"musescore.not.unique",arguments.museScore}),512);
			return "?";
		}
		File f = new File(foundMuseScore[0]);
		if (!f.exists()) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"musescore.not.exists",arguments.museScore}),1024);
			return foundMuseScore[0];
		} else if (!f.canRead()) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"musescore.not.readable",arguments.museScore}),2048);
			return foundMuseScore[0];
		} else if (!f.canExecute()) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"musescore.not.executable",arguments.museScore}),2048);
			return foundMuseScore[0];
		}
		if (arguments.config.getOSId()==Config.OSId.OSX) {
			foundMuseScore[0]=Normalizer.normalize(foundMuseScore[0], Normalizer.Form.NFD);
		}
		return foundMuseScore[0];
	}

	private String validateOutputDirectory() {
		String foundDirectories[]=MuseScore2HtmlUtils.findItems(arguments.outputDirectory, MuseScore2HtmlUtils.TYPE.DIRECTORY, arguments.isCaseSensitive);
		if (foundDirectories.length==0) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"outputdirectory.not.found",arguments.outputDirectory}),256);
			return "?";
		} else if (foundDirectories.length>1) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"outputdirectory.not.unique",arguments.outputDirectory}),512);
			return "?";
		}
		File f = new File(foundDirectories[0]);
		if (!f.exists()) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"outputdirectory.not.exists",arguments.outputDirectory}),1024);
			return foundDirectories[0];
		} else if (!f.canWrite()) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"outputdirectory.not.writable",arguments.outputDirectory}),2048);
			return foundDirectories[0];
		}
		if (arguments.config.getOSId()==Config.OSId.OSX) {
			foundDirectories[0]=Normalizer.normalize(foundDirectories[0], Normalizer.Form.NFD);
		}
		return foundDirectories[0];
	}

	private void validateIndexFile() {
		if (arguments.generateIndexFileOption==Arguments.GENERATE_INDEX_FILE_OPTION.NONE) return ;
		if (arguments.indexFileName==null||arguments.indexFileName.equals("")) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.not.specified"}),2048);					
		} else {
			File f = new File(arguments.indexFileName);
			if (f.getParent()!=null) {
				processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.not.afilename",arguments.indexFileName}),1);
				return;
			}
			String foundFiles[]=MuseScore2HtmlUtils.findItems(arguments.outputDirectory+File.separator+arguments.indexFileName,
			MuseScore2HtmlUtils.TYPE.DIRECTORY, arguments.isCaseSensitive);
			if (foundFiles.length>1) {
				processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.not.unique",arguments.indexFileName}),512);
				f = new File(arguments.outputDirectory+File.separator+arguments.indexFileName);
				if (f.exists()) {
					if (!f.canWrite()) {
						processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"file.not.writable",arguments.indexFileName}),2048);
					}
				}
			}
		}
	}

	private String[] splitPath(String filePath) {
		if (arguments.config.getOSId()==Config.OSId.OSX) filePath=Normalizer.normalize(filePath, Normalizer.Form.NFC);
  		File f = new File(filePath);
  		String directory, name, filename, extension="";

  		// if it's a directory, don't remove the extention
 		if (f.isDirectory()) {
 			if (filePath.endsWith(File.separator)) return new String[] {filePath, "", "", ""};
 			else return new String[] {filePath+File.separator, "", "", ""};
 		} else {
 			directory=f.getParent();
 			if (!directory.endsWith(File.separator)) directory=directory+File.separator;
 		}

  		name = f.getName();

 		// Now we know it's a file - don't need to do any special hidden
  		// checking or contains() checking because of:
  		final int lastPeriodPos = name.lastIndexOf('.');
  		if (lastPeriodPos <= 0) {
	  		// No period after first character - return name as it was passed in
	   		return new String[] {directory, name, ""};
  		} else {
	  		// Remove the last period and everything after it
	   		return new String[] {filePath, directory, name.substring(0, lastPeriodPos), name.substring(lastPeriodPos+1)};
  		}
	}

	private void validateScore(String[] scoreParts) {
		switch (scoreParts[3].toLowerCase()) {
			case "mscz":
			case "mscx" :
				break;
			default:
				processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.extension.error", scoreParts[0],scoreParts[3]}),8);
				break;
		}
		String foundScores[]=MuseScore2HtmlUtils.findItems(scoreParts[0], MuseScore2HtmlUtils.TYPE.FILE, arguments.isCaseSensitive);
		if (foundScores.length==0) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.not.found",scoreParts[0]}),16);
			return;
		} else if (foundScores.length>1) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.not.unique",scoreParts[0]}),32);
			return;
		}
		File f = new File(foundScores[0]);
		if (!f.exists()) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.not.exists",foundScores[0]}),64);
		else if (!f.canRead()) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.not.readable",foundScores[0]}),128);
	}

	private boolean validateAction(String srcFile, String tgtFile) {
		File src = new File(srcFile), tgt = new File(tgtFile);
		if (tgt.exists()) {
 			long srcLastModified = src.lastModified();
 			long tgtLastModified = tgt.lastModified();
			if (arguments.fileOption==Arguments.FILE_OPTION.REPLACE||(arguments.fileOption==Arguments.FILE_OPTION.ONLY_IF_NEWER&&srcLastModified>tgtLastModified)) {
 				if (!tgt.canWrite()) {
					processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"file.not.writable",tgtFile}),2048);
				}
				return true;
			} else return false;
		} else return true;
	}

	private void validateOutputFiles(String score, String outputDirectory, String fileName) throws Exception {
		for (int e=0;e<extensions.length;e++) {
			String s;
			boolean action=false;
			if (e==imageExtension) s="-*"; else s=""; // s="-[0-9]"; else s="";
			String foundFiles[]=MuseScore2HtmlUtils.findItems(outputDirectory+File.separator+fileName+s+extensions[e], MuseScore2HtmlUtils.TYPE.FILE, arguments.isCaseSensitive);
			if (foundFiles.length>0) {
				for (int i=0;i<foundFiles.length;i++) {
					action = action || validateAction(score, foundFiles[i]);
				}
			}
		}
	}

	private void validateCopyFiles() {
		for (int i=0; i<files2copy.length;i++) {
			validateAction(insDirectory+File.separator+files2copy[i], arguments.outputDirectory+File.separator+files2copy[i]);
		}
		for (int i=0; i<files2copyForIndexFile.length;i++) {
			validateAction(insDirectory+File.separator+files2copyForIndexFile[i], arguments.outputDirectory+File.separator+files2copyForIndexFile[i]);
		}
	}

	private void callMuseScore(String museScore, String score, String outputDirectory, String name, String extension) throws Exception {
		Runtime runTime=Runtime.getRuntime();
		String outputFile=outputDirectory+File.separator+name+extension;
		String cmdarray[]={ museScore, "-cli", "-o", outputFile, score};
		StringBuffer sb=new StringBuffer();
		for (int i=0;i<cmdarray.length;i++) sb.append("'"+cmdarray[i]+"' ");
		String s=sb.toString();
		Process process=runTime.exec(cmdarray);
		process.waitFor();
		int rc=process.exitValue();
		if (rc!=0) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"musescore.error", Integer.toString(rc), s}),2048);
			throw new Exception(translations.translate(new String[] {"musescore.error", Integer.toString(rc), s}));
		}
		process.destroy();
		if (extension.equals(".png")) {
			String foundFiles[]=MuseScore2HtmlUtils.findItems(outputDirectory+File.separator+name+"-*"+extension, MuseScore2HtmlUtils.TYPE.FILE, arguments.isCaseSensitive);
 			files_generated+=foundFiles.length;
		} else files_generated++;
	}

	private void generateIndexHtml(String fileName, String[] fileNames) throws IOException {
   		PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), StandardCharsets.UTF_8));
  		output.println("<!DOCTYPE html>");
  		output.println("<html lang=\"en\">");
  		output.println("	<head>");
  		output.println("		<meta charset=\"UTF-8\"/>");
  		output.println("		<meta http-equiv=\"Content-Style-Type\" content=\"text/css\"/>");
  		output.println("		<link rel=\"stylesheet\" type=\"text/css\" href=\"css/ms_player_index.css\"/>");
  		output.println("		<title>"+translations.getKey("scores.label")+"</title>");
  		output.println("	</head>");
  		output.println("	<body>");
  		output.println("		<table border=\"1\">");
 		output.println("			<tr><th>"+translations.getKey("scores.label")+"</th></tr>");
 		for (int i=0;i<fileNames.length;i++) {
   			output.println("			<tr><td><a href=\"ShowScore.html?name="+fileNames[i]+"\">"+fileNames[i]+"</a></td></tr>");
  		}
   		output.println("		</table>");
  		output.println("	</body>");
  		output.println("</html>");
		output.close();
		files_generated++;
	}

	private void generateIndexHtmlLinks2HtmlFiles(String fileName, String[] fileNames) throws IOException {
   		PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), StandardCharsets.UTF_8));
  		output.println("<!DOCTYPE html>");
  		output.println("<html lang=\"en\">");
  		output.println("	<head>");
  		output.println("		<meta charset=\"UTF-8\"/>");
  		output.println("		<meta http-equiv=\"Content-Style-Type\" content=\"text/css\"/>");
  		output.println("		<link rel=\"stylesheet\" type=\"text/css\" href=\"css/ms_player_index.css\"/>");
  		output.println("		<title>"+translations.getKey("scores.label")+"</title>");
  		output.println("	</head>");
  		output.println("	<body border=\"1\">");
  		output.println("		<h1>"+translations.getKey("scores.label")+"</h1>");
   		output.println("		<table>");
  		output.println("			<tr><th>"+translations.getKey("scores.label")+"</th></tr>");
 		for (int i=0;i<fileNames.length;i++) {
  			output.println("			<tr><td><a href=\""+fileNames[i]+".html\">"+fileNames[i]+"</a></td></tr>");
  		}
  		output.println("		</table>");
   		output.println("  </body>");
  		output.println("</html>");
		output.close();
		files_generated++;
	}

	private void generateHtml(String fileName, String title) throws IOException {
   		PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), StandardCharsets.UTF_8));
  		output.println("<!DOCTYPE html>");
  		output.println("<html lang=\"en\">");
  		output.println("	<head>");
  		output.println("		<meta charset=\"UTF-8\"/>");
  		output.println("		<meta http-equiv=\"Content-Style-Type\" content=\"text/css\"/>");
  		output.println("		<script type=\"text/javascript\" src=\"js/ms_player.js\"></script>");
  		output.println("		<title>"+title+"</title>");
  		output.println("	</head>");
  		output.println("	<body>");
  		output.println("		<script type=\"text/javascript\">");
  		output.println("			ms_player.load(\""+title+"\");");
  		output.println("		</script>");
  		output.println("	</body>");
  		output.println("</html>");
		output.close();
		files_generated++;
	}

	private void copyFiles(String outputDirectory, String[] files2copy, String actionPart) throws IOException {
		try {
			String action=actionPart+".installed";
	 		String lastSubdir="";
	 		int first=0, len=files2copy.length;
	 		if (actionPart.equals("indexfile")) {
	 			if (arguments.generateIndexFileOption == Arguments.GENERATE_INDEX_FILE_OPTION.INDEX_TO_HTML) {
	 				first = 0;
	 				len = 1;
	 			}
	 		}
			for (int i=first; i<len; i++) {
				String srcFile=insDirectory+File.separator+files2copy[i], tgtFile=outputDirectory+File.separator+files2copy[i];
				File src = new File(srcFile), tgt = new File(tgtFile);
				if (tgt.exists()) {
					if (arguments.fileOption==Arguments.FILE_OPTION.ONLY_IF_NEW||arguments.fileOption==Arguments.FILE_OPTION.ONLY_IF_NEWER&&src.lastModified()<=tgt.lastModified()) {
						processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {actionPart+".already.exists"+translateCheckOnly,files2copy[i]}),0);
						continue;
					}
					action = actionPart+".replaced";
				}
				String subDir=(new File(files2copy[i])).getParent();
				if (subDir!=null&&!subDir.equals(lastSubdir)) {
					File subDirectory=new File(outputDirectory+File.separator+subDir);
					if (!subDirectory.exists()) {
						if (!arguments.checkOnly) {
							boolean success=subDirectory.mkdir();
							if (!success) {
								processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"directory.created.error",subDir}),1);
	 							return;
							}
						}
						directories_created++;
						processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"directory.created"+translateCheckOnly,subDir}),0);
					}
				}
				lastSubdir=subDir;
				if (!arguments.checkOnly) {
					Path srcPath=FileSystems.getDefault().getPath(srcFile), tgtPath=FileSystems.getDefault().getPath(tgtFile);
					CopyOption[] copyOptions={StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING};//,StandardCopyOption.ATOMIC_MOVE
					Files.copy(srcPath, tgtPath, copyOptions);
				}
				files_installed++;
				processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {action+translateCheckOnly,files2copy[i]}),0);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"exception.error.message", exc.getMessage()}),99);
			else processInfo(Arguments.LOG_LEVEL.NORMAL, translations.getKey("exception.error"),99);
		}
	}

	private void generateFiles(String museScore, String score, String outputDirectory, String fileName) throws Exception {
		int filesGenerated = 0;
		for (int e=0;e<extensions.length;e++) {
			String sf;
			if (e==imageExtension) sf="-*"; else sf="";
			String foundFiles[];
			String action="file.generated";
			foundFiles=MuseScore2HtmlUtils.findItems(arguments.outputDirectory+File.separator+fileName+sf+extensions[e], MuseScore2HtmlUtils.TYPE.FILE, arguments.isCaseSensitive);
			if (foundFiles.length>0) {
				action="file.replaced";
				if (arguments.fileOption==Arguments.FILE_OPTION.ONLY_IF_NEW) {
					processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"file.already.exists"+translateCheckOnly,fileName+sf+extensions[e]}),0);
					continue;
				} else if (arguments.fileOption==Arguments.FILE_OPTION.ONLY_IF_NEWER) {
					boolean scoreIsNewer = false;
					for (int i=0; i<foundFiles.length; i++) {
						scoreIsNewer = scoreIsNewer || validateAction(score, foundFiles[i]);
					}
					if (!scoreIsNewer) {
						processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"file.is.newer"+translateCheckOnly,fileName+sf+extensions[e]}), 0);
						continue;
					}
				}
			}
			String s;
			if (e==imageExtension) s="-[0-9]*"; else s="";
			if (extensions[e].equals("_space.json")) {
				if (!arguments.checkOnly) {
					callMuseScore(museScore, score, tempDirectory, fileName, ".mpos");
					XmlXslTransformer.doTransform(tempDirectory+File.separator+fileName+".mpos",xslDirectory+File.separator+"musescore_mpos.xsl",outputDirectory+File.separator+fileName+extensions[e]);
				} else files_generated++;
				processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {action+translateCheckOnly,fileName+sf+extensions[e]}),0);
				filesGenerated++;
			} else if (extensions[e].equals(".html")) {
				if (arguments.generateHtml) {
					if (!arguments.checkOnly) generateHtml(arguments.outputDirectory+File.separator+fileName+".html",fileName);
					else files_generated++;
					processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {action+translateCheckOnly,fileName+sf+extensions[e]}),0);
					filesGenerated++;
				}
			} else {
				if (!arguments.checkOnly) callMuseScore(museScore, score, outputDirectory, fileName, extensions[e]);
				else files_generated++;
				processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {action+translateCheckOnly,fileName+sf+extensions[e]}),0);
				filesGenerated++;
			}
		}
		if (filesGenerated>0) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.processed"+translateCheckOnly,score}),0);
			scores_processed++;
		} else scores_skipped++;
	}

	public ProcessScores(Arguments arguments, ProcessData processData) {
		this.arguments = arguments;
		this.translations = arguments.translations;
		this.processData = processData;
	}

	public int validateArguments() {
		try {
   			translateCheckOnly = arguments.checkOnly?".checkonly":"";
			if (arguments.errors>0) {
				processData.setFinished();
				return errors;
			}

			arguments.museScore=validateMuseScore();
			//if (errors>0) { processData.setFinished(); return errors; }

			arguments.outputDirectory=validateOutputDirectory();
			//if (errors>0) { processData.setFinished(); return errors; }

			validateIndexFile();
			//if (errors>0) { processData.setFinished(); return errors; }

			ArrayList<String> scores = new ArrayList<String>();
			for (int i=0;i<arguments.scores.size();i++) {
				String result[] = MuseScore2HtmlUtils.findItems(arguments.scores.get(i), MuseScore2HtmlUtils.TYPE.FILE, arguments.isCaseSensitive);
				if (result.length==0) {
					processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.not.found",arguments.scores.get(i)}), 4);
				} else {
					scores.addAll(Arrays.asList(result));
				}
			}

			/* Remove duplicate scores which may be caused by a caseinsensitive filesystem e.g. parameters : b*.mscz and B*.mscz */
			ArrayList<String> uniqueScores = new ArrayList<String>();
			for (int i=0;i<scores.size();i++) {
				boolean found=false;
				for (int j=0;j<uniqueScores.size(); j++) {
					if (scores.get(i).equals(uniqueScores.get(j))) { found=true; break; }
				}
				if (!found) uniqueScores.add(scores.get(i));
			}
			scores=uniqueScores;
			arguments.scores = uniqueScores;

			scoreParts = new ArrayList<String[]>();
			for (int i=0;i<scores.size();i++) {
				scoreParts.add(splitPath(scores.get(i)));
			}

			for (int i=0;i<scoreParts.size()-1;i++) {
				for (int j=i+1;j<scoreParts.size();j++) {
					if (scoreParts.get(i)[2].equals(scoreParts.get(j)[2])) {
						processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.duplicate.names", scoreParts.get(i)[0], scoreParts.get(j)[0]}), 5);
					}
				}
			}
			//if (errors>0) { processData.setFinished(); return errors; }

			for (int i=0;i<scoreParts.size();i++) {
				validateScore(scoreParts.get(i));
			}
		} catch (InterruptedException exc) {
			exc.printStackTrace();
			System.err.println(translations.translate(new String[] {"exception.error", exc.getMessage()}));
		}
		return errors;
	}
	
	public void showArguments() {
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.getKey("info.arguments"),0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.osid", String.valueOf(arguments.config.getOSId())}),0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.language", arguments.config.getLanguage()}),0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.interface", String.valueOf(arguments.interfaceType)}),0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.loglevel", String.valueOf(arguments.logLevel)}),0);
		if (arguments.cfgPath!=null) processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.configuration.path", arguments.cfgPath}),0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.configuration", String.valueOf(arguments.config!=null)}),0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.musescore", arguments.museScore}),0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.outputdirectory", arguments.outputDirectory}),0);
		if (arguments.indexFileName!=null) {
			processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.indexfilename", arguments.indexFileName}),0);
			processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.generatehtml", String.valueOf(arguments.generateHtml)}),0);
		}
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.indexfileoption", String.valueOf(arguments.indexFileOption)}),0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.indexfile.all", String.valueOf(arguments.generateIndexAll)}),0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.fileoption", String.valueOf(arguments.fileOption)}),0);
		for (int i=0;i<arguments.scores.size();i++) {
			processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info.score", arguments.scores.get(i)}),0);
		}
	}

	public Integer call() {
		try {
			if (arguments.logLevel==Arguments.LOG_LEVEL.EXTREME) showArguments();
			
			int validateErrors = validateArguments();
			if (validateErrors>0) {
				processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate("exception.error"),0);
				processData.setFinished();
				return validateErrors;
			}
			
   			translateCheckOnly = arguments.checkOnly?".checkonly":"";
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate("info.generate.start"),0);

			for (int i=0;i<scoreParts.size();i++) {
				String score[]=scoreParts.get(i);
				try {
					generateFiles(arguments.museScore, score[0], arguments.outputDirectory, score[2]);
					if (errors>0) break;
				} catch (Exception exc) {
					exc.printStackTrace();
					if (exc.getMessage()!=null) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.error.generate.message", score[2],exc.getMessage()}),99);
					else processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.error.generate", score[2]}),99);
					processData.setFinished();
					return errors;
				}
			}

			if (files_generated>0) {
				try {
					copyFiles(arguments.outputDirectory, files2copy, "file");
				} catch (Exception exc) {
					exc.printStackTrace();
					if (exc.getMessage()!=null) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"exception.error.message",exc.getMessage()}),99);
					else processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"exception.error"}),99);
					processData.setFinished();
					return errors;
				}
			}
			
			if (arguments.generateIndexFileOption != Arguments.GENERATE_INDEX_FILE_OPTION.NONE) {
				String foundFiles[] = new String[0] ;
				String fileNames[] = new String[0];
				if (arguments.generateIndexAll) {
					foundFiles = MuseScore2HtmlUtils.findItems(arguments.outputDirectory+File.separator+"*.metajson", MuseScore2HtmlUtils.TYPE.FILE, arguments.isCaseSensitive);
					if (arguments.generateIndexFileOption == Arguments.GENERATE_INDEX_FILE_OPTION.INDEX_NO_HTML) {
						fileNames = new String[foundFiles.length];
						for (int i=0; i<foundFiles.length; i++) {
							fileNames[i] = (new File(foundFiles[i])).getName().replaceAll(".metajson", "");
						}
					} else if (arguments.generateIndexFileOption == Arguments.GENERATE_INDEX_FILE_OPTION.INDEX_TO_HTML) {
						ArrayList<String> htmlFiles = new ArrayList<String>();
						for (int i=0; i<foundFiles.length; i++) {
							String htmlFile = foundFiles[i].replaceAll(".metajson", ".html");
							File tgt = new File(htmlFile);
							if (tgt.exists()) htmlFiles.add(htmlFile);
						}
						fileNames = new String[htmlFiles.size()];
						for (int i=0; i<htmlFiles.size(); i++) {
							fileNames[i] = (new File(htmlFiles.get(i))).getName().replaceAll(".html", "");
						}
					}
				} else {
					foundFiles = new String[scoreParts.size()];
					fileNames = new String[scoreParts.size()];
					for (int i=0;i<scoreParts.size();i++) {
						foundFiles[i] = arguments.outputDirectory + File.separator + scoreParts.get(i)[2] + ".metajson";
						fileNames[i] = scoreParts.get(i)[2];
					}
				}
				
				boolean createIndexFile = false;
				if (foundFiles.length>0) {
					/*
					if (arguments.config.getOSId()==Config.OSId.OSX) {
						for (int i=0; i<fileNames.length; i++) {
							fileNames[i]=Normalizer.normalize(fileNames[i], Normalizer.Form.NFC);
						}
					}
					*/
					File src = new File(arguments.outputDirectory+File.separator+arguments.indexFileName);
					String action = "indexfile.generated";
					if (arguments.indexFileOption==Arguments.INDEX_FILE_OPTION.INDEX_REPLACE) createIndexFile = true;
					else if (src.exists()) {
						if (arguments.indexFileOption==Arguments.INDEX_FILE_OPTION.INDEX_ONLY_IF_NEW) {
							processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.already.exists",arguments.indexFileName}),0);
						} else if (arguments.indexFileOption==Arguments.INDEX_FILE_OPTION.INDEX_ONLY_IF_NEWER) {
							long srcLastModified = src.lastModified();
							if (foundFiles.length>0) {
								for (int i=0; i<foundFiles.length; i++) {
									File tgt = new File(foundFiles[i]);
									long tgtLastModified = tgt.lastModified();
									if (srcLastModified<tgtLastModified) {
										action="indexfile.replaced";
										createIndexFile = true;
										break;
									}
								}
								
							}
							if (!createIndexFile) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.is.newer"+translateCheckOnly,arguments.indexFileName}), 0);
						}
					} else createIndexFile = true;

					processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"info",
						 "indexfile '"+arguments.indexFileName+"' found files "+
						 Integer.toString(foundFiles.length)+" create: "+String.valueOf(createIndexFile)+" "+String.valueOf(arguments.generateIndexFileOption)}),0);
				 
					if (createIndexFile) {
						Arrays.sort(fileNames, naturalOrderComparator);
						if (!arguments.checkOnly) {
							try {
								switch (arguments.generateIndexFileOption) {
									case INDEX_TO_HTML:
										generateIndexHtmlLinks2HtmlFiles(arguments.outputDirectory+File.separator+arguments.indexFileName, fileNames);
										break;
									case INDEX_NO_HTML:
										generateIndexHtml(arguments.outputDirectory+File.separator+arguments.indexFileName, fileNames);
										break;
									default:
										break;
								}
								copyFiles(arguments.outputDirectory, files2copyForIndexFile, "indexfile");
							} catch (Exception exc) {
								exc.printStackTrace();
								if (exc.getMessage()!=null) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"exception.error.message",exc.getMessage()}),99);
								else processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"exception.error"}),99);
								processData.setFinished();
								return errors;
							}
							processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {action+translateCheckOnly,arguments.indexFileName}),0);
						}
					}
				}
			}
			
			processInfo(Arguments.LOG_LEVEL.QUIET, translations.translate(new String[] {"summary"+translateCheckOnly,
				Integer.toString(scores_processed),
				Integer.toString(scores_skipped),
				Integer.toString(files_generated),
				Integer.toString(files_installed),
				Integer.toString(directories_created)
			}),0);
			
			processInfo(Arguments.LOG_LEVEL.QUIET, translations.translate("info.generate.ready"),0);
			processData.setFinished();
		} catch (InterruptedException exc) {
			exc.printStackTrace();
			System.err.println(translations.translate(new String[] {"exception.error", exc.getMessage()}));
		}
		return errors;
	}
}