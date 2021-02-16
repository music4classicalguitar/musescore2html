package musescore2html;

import org.apache.tools.ant.MuseScore2HtmlUtils;

import java.util.ArrayList;
import java.util.Arrays;

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

import java.net.URLEncoder;
import java.util.Map;
import java.util.HashMap;
import java.io.UnsupportedEncodingException;

public class ProcessScores implements Callable<Integer>  {

	private ProcessData processData;
	private Translations translations;
	private Arguments arguments;
	private boolean isCaseSensitive=false;

	private final String jarDirectory=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	private final String rscDirectory=(new File(jarDirectory)).getParentFile().getParent();
	private final String xslDirectory=rscDirectory+File.separator+"xsl";
	private final String insDirectory=rscDirectory+File.separator+"install";

	private static final String tempDirectory=System.getProperty("java.io.tmpdir");

	public static enum FILE_OPTION { ONLY_IF_NEW, ONLY_IF_NEWER, REPLACE }
	FILE_OPTION fileOption = FILE_OPTION.ONLY_IF_NEW;
	public static enum INDEX_FILE_OPTION { HTML_ONLY, HTML_AND_INDEX, INDEX_ONLY }
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
			"js"+File.separator+"ms_player.js",
			"css"+File.separator+"ms_player.css",
			"images"+File.separator+"media-playback-loop.svg",
			"images"+File.separator+"media-playback-metronome.svg",
			"images"+File.separator+"media-playback-pause.svg",
			"images"+File.separator+"media-playback-start.svg",
			"images"+File.separator+"media-skip-backward.svg",
			"images"+File.separator+"media-skip-forward.svg",
			"images"+File.separator+"window-close.svg"
    	};

	private String files2copyForIndexFile[]={
			"js"+File.separator+"ms_player_query.js",
			"ShowScore.html"
	};

	private int codes=0, errors=0;

	private void processInfo(Arguments.LOG_LEVEL logLevel, String message, int code) {
		codes=codes|code;
		if (code != 0) errors++;
		try {
			if (logLevel.ordinal()<=arguments.logLevel.ordinal()) processData.addData(message,code);
		} catch (InterruptedException iexc) {
			iexc.printStackTrace();
			System.err.println(translations.getKey("Error")+": '"+iexc.getMessage());
		}
	}

	private String validateMuseScore() {
		String foundMuseScore[]=MuseScore2HtmlUtils.findItems(arguments.museScore, MuseScore2HtmlUtils.TYPE.FILE, isCaseSensitive);
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
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"musescore.not.excutable",arguments.museScore}),2048);
			return foundMuseScore[0];
		}
		if (arguments.config.getOSId()==Config.OSId.OSX) {
			foundMuseScore[0]=Normalizer.normalize(foundMuseScore[0], Normalizer.Form.NFD);
		}
		return foundMuseScore[0];
	}

	private String validateOutputDirectory() {
		String foundDirectories[]=MuseScore2HtmlUtils.findItems(arguments.outputDirectory, MuseScore2HtmlUtils.TYPE.DIRECTORY, isCaseSensitive);
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
		switch (arguments.indexFileOption) {
			case HTML_ONLY:
				break;
			default:
				if (arguments.indexFileName==null||arguments.indexFileName.equals("")) {
					processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.not.specified"}),2048);					
				} else {
					File f = new File(arguments.indexFileName);
					if (f.getParent()!=null) {
						processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.not.afilename",arguments.indexFileName}),1);
						return;
					}
					String foundFiles[]=MuseScore2HtmlUtils.findItems(arguments.outputDirectory+File.separator+arguments.indexFileName,
					MuseScore2HtmlUtils.TYPE.DIRECTORY, isCaseSensitive);
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
				break;
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
		String foundScores[]=MuseScore2HtmlUtils.findItems(scoreParts[0], MuseScore2HtmlUtils.TYPE.FILE, isCaseSensitive);
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
			if (fileOption==FILE_OPTION.REPLACE||(fileOption==FILE_OPTION.ONLY_IF_NEWER&&srcLastModified>tgtLastModified)) {
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
			String foundFiles[]=MuseScore2HtmlUtils.findItems(outputDirectory+File.separator+fileName+s+extensions[e], MuseScore2HtmlUtils.TYPE.FILE, isCaseSensitive);
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
			String foundFiles[]=MuseScore2HtmlUtils.findItems(outputDirectory+File.separator+name+"-*"+extension, MuseScore2HtmlUtils.TYPE.FILE, isCaseSensitive);
 			files_generated+=foundFiles.length;
		} else files_generated++;
	}

    private String encode(String component) {
    	try {
    		String encodedComponent = URLEncoder.encode(component, "UTF-8");
    		Map<String, String> nonEncodedChars = new HashMap<String, String>();
    		nonEncodedChars.put("~", "%7E");
    		nonEncodedChars.put("!","%21");
    		nonEncodedChars.put("(","%28");
    		nonEncodedChars.put(")","%29");
    		nonEncodedChars.put("\'","%27");
    		//Map.of("~", "%7E", "!","%21","(","%28",")","%29","\'","%27");
    		for (Map.Entry<String, String> c : nonEncodedChars.entrySet()) {
    			encodedComponent = encodedComponent.replaceAll(c.getValue(), c.getKey());
    		}
    		encodedComponent = encodedComponent.replaceAll("\\+","%20");
    		return encodedComponent;
    	} catch (UnsupportedEncodingException e) {
    		return component;
    	}
    }

    private void generateIndexHtml(String fileName) throws IOException {
    	String foundFiles[]=MuseScore2HtmlUtils.findItems(arguments.outputDirectory+File.separator+"*.metajson", MuseScore2HtmlUtils.TYPE.FILE, isCaseSensitive);
    	if (foundFiles.length==0) {
    		processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.no.files", arguments.outputDirectory}),2048);
    		return;
    	}
    	Arrays.sort(foundFiles);
   		PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), StandardCharsets.UTF_8));
  		output.println("<!DOCTYPE html>");
  		output.println("<html lang=\"en\">");
  		output.println("  <head>");
  		output.println("    <meta charset=\"UTF-8\">");
  		output.println("    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />");
  		output.println("    <title>"+translations.getKey("gui.scorestable.columntitle")+"</title>");
  		output.println("  </head>");
  		output.println("  <body>");
  		output.println("    <ul>");
  		for (int i=0;i<foundFiles.length;i++) {
  			String nameParts[]=splitPath(foundFiles[i]);
  			//String name=URLEncoder.encode(nameParts[2],"UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
  			output.println("      <li><a href=\"ShowScore.html?name="+encode(nameParts[2])+"\">"+nameParts[2]+"</a></li>");
  		}
   		//output.println("    <!-- link for this page <a href=\""+title+".html"+"\">"+title+"</a> -->");
  		output.println("    </ul>");
  		output.println("  </body>");
  		output.println("</html>");
	    output.close();
	    files_generated++;
    }

    private void generateIndexHtmlLinks2HtmlFiles(String fileName) throws IOException {
    	String foundFiles[]=MuseScore2HtmlUtils.findItems(arguments.outputDirectory+File.separator+"*.metajson", MuseScore2HtmlUtils.TYPE.FILE, isCaseSensitive);
    	if (foundFiles.length==0) {
    		processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.no.files", arguments.outputDirectory}),2048);
    		return;
    	}
    	Arrays.sort(foundFiles);
    	String s = arguments.outputDirectory;
    	if (!s.endsWith(File.separator)) s = s+File.separator;
    	int l = s.length();
    	ArrayList<String> foundHtmlFiles = new ArrayList<String>();
    	for (int i=0; i<foundFiles.length; i++) {
    		String htmlFile = foundFiles[i].substring(0,foundFiles[i].length()-8)+"html";
    		File f = new File(htmlFile);
    		if (f.exists()) {
    			foundHtmlFiles.add(htmlFile.substring(l));
    		}
    	}
   		PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), StandardCharsets.UTF_8));
  		output.println("<!DOCTYPE html>");
  		output.println("<html lang=\"en\">");
  		output.println("  <head>");
  		output.println("    <meta charset=\"UTF-8\">");
  		output.println("    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />");
  		output.println("    <title>"+translations.getKey("gui.scorestable.columntitle")+"</title>");
  		output.println("  </head>");
  		output.println("  <body>");
  		output.println("    <ul>");
  		for (int i=0;i<foundHtmlFiles.size();i++) {
  			output.println("      <li><a href=\""+foundHtmlFiles.get(i)+"</a></li>");
  		}
   		//output.println("    <!-- link for this page <a href=\""+title+".html"+"\">"+title+"</a> -->");
  		output.println("    </ul>");
  		output.println("  </body>");
  		output.println("</html>");
	    output.close();
	    files_generated++;
    }

    private void generateHtml(String fileName, String title) throws IOException {
   		PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), StandardCharsets.UTF_8));
  		output.println("<!DOCTYPE html>");
  		output.println("<html lang=\"en\">");
  		output.println("  <head>");
  		output.println("    <meta charset=\"UTF-8\">");
  		output.println("    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />");
  		output.println("    <script type=\"text/javascript\" src=\"js/ms_player.js\"></script>");
  		output.println("    <title>"+title+"</title>");
  		output.println("  </head>");
  		output.println("  <body>");
   		//output.println("    <!-- link for this page <a href=\""+title+".html"+"\">"+title+"</a> -->");
  		output.println("    <script type=\"text/javascript\">");
  		output.println("      ms_player.load(\""+title+"\");");
  		output.println("    </script>");
  		output.println("  </body>");
  		output.println("</html>");
	    output.close();
	    files_generated++;
    }

    private void copyFiles(String outputDirectory, String[] files2copy, String actionPart) throws IOException {
    	try {
    		String action=actionPart+".installed";
     		String lastSubdir="";
    		for (int i=0; i<files2copy.length; i++) {
    			String srcFile=insDirectory+File.separator+files2copy[i], tgtFile=outputDirectory+File.separator+files2copy[i];
    			File src = new File(srcFile), tgt = new File(tgtFile);
    			if (tgt.exists()) {
					if (fileOption==FILE_OPTION.ONLY_IF_NEW||fileOption==FILE_OPTION.ONLY_IF_NEWER&&src.lastModified()<=tgt.lastModified()) {
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
    					processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"directory.created"+translateCheckOnly,subDir}),0);
    					directories_created++;
    				}
    			}
    			lastSubdir=subDir;
    			if (!arguments.checkOnly) {
					Path srcPath=FileSystems.getDefault().getPath(srcFile), tgtPath=FileSystems.getDefault().getPath(tgtFile);
					CopyOption[] copyOptions={StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING};//,StandardCopyOption.ATOMIC_MOVE
    				Files.copy(srcPath, tgtPath, copyOptions);
    				files_installed++;
    			}
	    		processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {action+translateCheckOnly,files2copy[i]}),0);
    		}
    	} catch (Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[]{"exception.error.message", exc.getMessage()}),99);
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
			foundFiles=MuseScore2HtmlUtils.findItems(arguments.outputDirectory+File.separator+fileName+sf+extensions[e], MuseScore2HtmlUtils.TYPE.FILE, isCaseSensitive);
			if (foundFiles.length>0) {
				action="file.replaced";
				if (fileOption==FILE_OPTION.ONLY_IF_NEW) {
					processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {"file.already.exists"+translateCheckOnly,fileName+sf+extensions[e]}),0);
					continue;
				} else if (fileOption==FILE_OPTION.ONLY_IF_NEWER) {
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
				}
				processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {action+translateCheckOnly,fileName+sf+extensions[e]}),0);
				filesGenerated++;
			} else if (extensions[e].equals(".html")) {
				if (arguments.generateHtml) {
					if (!arguments.checkOnly) generateHtml(arguments.outputDirectory+File.separator+fileName+".html",fileName);
					processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {action+translateCheckOnly,fileName+sf+extensions[e]}),0);
					filesGenerated++;
				}
			} else {
				if (!arguments.checkOnly) callMuseScore(museScore, score, outputDirectory, fileName, extensions[e]);
				processInfo(Arguments.LOG_LEVEL.EXTREME, translations.translate(new String[] {action+translateCheckOnly,fileName+sf+extensions[e]}),0);
				filesGenerated++;

			}
		}
		if (filesGenerated>0) {
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"score.processed"+translateCheckOnly,score}),0);
			scores_processed++;
		} else scores_skipped++;
    }

	public ProcessScores(ProcessData processData, Arguments arguments) {
		this.processData = processData;
		this.translations = arguments.translations;
		this.arguments = arguments;
	}

	public Integer call() {
		try {
   			translateCheckOnly = arguments.checkOnly?".checkonly":"";
			processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate("info.generate.start"),0);
			if (arguments.errors>0) {
				processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate("process.error.arguments"),0);
				processData.setFinished();
				return errors;
			}

			fileOption=arguments.fileOption;

			arguments.museScore=validateMuseScore();
			//if (errors>0) { processData.setFinished(); return errors; }

			arguments.outputDirectory=validateOutputDirectory();
			//if (errors>0) { processData.setFinished(); return errors; }

			validateIndexFile();

			ArrayList<String> scores = new ArrayList<String>();
			for (int i=0;i<arguments.scores.size();i++) {
				String result[] = MuseScore2HtmlUtils.findItems(arguments.scores.get(i), MuseScore2HtmlUtils.TYPE.FILE, isCaseSensitive);
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

			ArrayList<String[]> scoreParts = new ArrayList<String[]>();
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
			
			if (errors>0) { processData.setFinished(); return errors; }

			for (int i=0;i<scoreParts.size();i++) {
				String score[]=scoreParts.get(i);
				try {
					generateFiles(arguments.museScore, score[0], arguments.outputDirectory, score[2]);
					if (errors>0) break;
				} catch (Exception exc) {
					exc.printStackTrace();
					if (exc.getMessage()!=null) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[]{"score.error.generate.message", score[2],exc.getMessage()}),99);
					else processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[]{"score.error.generate", score[2]}),99);
					processData.setFinished();
					return errors;
				}
			}

			String foundFiles[] = MuseScore2HtmlUtils.findItems(arguments.outputDirectory+File.separator+"*.metajson", MuseScore2HtmlUtils.TYPE.FILE, isCaseSensitive);

			if (foundFiles.length>0) {
				try {
					copyFiles(arguments.outputDirectory, files2copy, "file");
				} catch (Exception exc) {
					exc.printStackTrace();
					if (exc.getMessage()!=null) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[]{"exception.error.message",exc.getMessage()}),99);
					else processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[]{"exception.error"}),99);
					processData.setFinished();
					return errors;
				}
			}

			if (arguments.indexFileName!=null) {
				File src = new File(arguments.outputDirectory+File.separator+arguments.indexFileName);
				String action = "indexfile.generated";
				boolean createIndexFile = false;
				if (src.exists()) {
					if (fileOption==FILE_OPTION.ONLY_IF_NEW) {
						processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.already.exists"+translateCheckOnly,arguments.indexFileName}),0);
					} else if (fileOption==FILE_OPTION.ONLY_IF_NEWER) {
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
						if (foundFiles.length>0&&!createIndexFile) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {"indexfile.is.newer"+translateCheckOnly,arguments.indexFileName}), 0);
					} else if (fileOption==FILE_OPTION.REPLACE) {
						if (foundFiles.length>0) {
							action="indexfile.replaced";
							createIndexFile = true;
						}
					}
				} else if (foundFiles.length>0) createIndexFile = true;

				if (createIndexFile) {
					if (!arguments.checkOnly) {
						try {
							switch (arguments.indexFileOption) {
								case HTML_AND_INDEX:
									generateIndexHtmlLinks2HtmlFiles(arguments.outputDirectory+File.separator+arguments.indexFileName);
									break;
								case INDEX_ONLY:
									generateIndexHtml(arguments.outputDirectory+File.separator+arguments.indexFileName);
									copyFiles(arguments.outputDirectory, files2copyForIndexFile, "indexfile");
									break;
								default:
									break;
							}
						} catch (Exception exc) {
							exc.printStackTrace();
							if (exc.getMessage()!=null) processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[]{"exception.error.message",exc.getMessage()}),99);
							else processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[]{"exception.error"}),99);
							processData.setFinished();
							return errors;
						}
						processInfo(Arguments.LOG_LEVEL.NORMAL, translations.translate(new String[] {action+translateCheckOnly,arguments.indexFileName}),0);
					}
				}
			}
			
			processInfo(Arguments.LOG_LEVEL.QUIET, translations.translate(new String[]{"summary"+translateCheckOnly,
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