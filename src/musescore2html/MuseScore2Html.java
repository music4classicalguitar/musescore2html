package musescore2html;

import musescore2html.gui.Gui;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;

import java.lang.Runtime;

public class MuseScore2Html {

	private Translations translations;
	private String language;
	private Arguments.LOG_LEVEL logLevel = Arguments.LOG_LEVEL.NORMAL;
	private int errors=0;

	private void preProcessArguments(String[] args) {
		for (int i=0;i<args.length;i++) {
			switch (args[i]) {
				case "-l":
					if (i+1<args.length) {
						if (!args[i+1].substring(0,1).equals("-")) language=args[++i];
					}
					break;
				case "-s":
					logLevel = Arguments.LOG_LEVEL.SILENT;
					break;
				case "-q":
					logLevel = Arguments.LOG_LEVEL.QUIET;
					break;
				case "-v":
					logLevel = Arguments.LOG_LEVEL.VERBOSE;
					break;
				default:
					break;
			}
		}
	}

	private	static Integer processScores(Arguments arguments) {
		int code=0,errors=0;
		try {
			ProcessData processDataScores = new ProcessData();
			ProcessInfo processInfoScores = new ProcessInfo(processDataScores, arguments.translations);
			ProcessScores processScores = new ProcessScores(processDataScores, arguments);
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
			Future<Integer> futureProcessInfoScores=executor.submit(processInfoScores);
			Future<Integer> futureProcessScores=executor.submit(processScores);
			errors=futureProcessScores.get();
			code=futureProcessInfoScores.get();
			executor.shutdown();
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new RuntimeException(arguments.translations.translate(new String[]{"process.error.message", exc.getMessage()}));
			else throw new RuntimeException(arguments.translations.translate("process.error"));
		}
		return code|errors;
	}

	private Arguments parseArguments(String[] args) throws Exception {
		//if (verbose) System.err.println("MuseScore2Html parseArguments");
		ProcessData processData = new ProcessData();
		translations = (language==null)?new Translations():new Translations(language);
		ProcessInfo processInfo = new ProcessInfo(processData, translations);
		ProcessArguments processArguments = new ProcessArguments(processData, translations, args, logLevel);
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        Arguments arguments;
        int code=0;
		try {
			Future<Arguments> futureProcessArguments=executor.submit(processArguments);
			Future<Integer> futureProcessInfo=executor.submit(processInfo);
			arguments=futureProcessArguments.get();
			code=futureProcessInfo.get();
			executor.shutdown();
			if (code==0) {
				arguments.translations=translations;
			}
		} catch(InterruptedException | ExecutionException iexc) {
			iexc.printStackTrace();
			if (iexc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"process.error.message", iexc.getMessage()}));
			else throw new RuntimeException(translations.translate("process.error"));
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"process.error.message", exc.getMessage()}));
			else throw new RuntimeException(translations.translate("process.error"));
		}
		return arguments;
	}

	private MuseScore2Html(String[] args)  {
		preProcessArguments(args);
		try {
			Arguments arguments=parseArguments(args);
			/*
			if (verbose) {
				System.out.println("MuseScore2Html");
				arguments.show();
				String
					free=String.valueOf(Runtime.getRuntime().freeMemory()),
					max=String.valueOf(Runtime.getRuntime().maxMemory()),
					total=String.valueOf(Runtime.getRuntime().totalMemory());
				System.out.println(translations.translate(new String[] {"info.memory", free, max, total}));
			}
			*/
			if (arguments.showVersion) System.out.println(arguments.translations.translate(new String[] {"version", Version.NAME, Version.VERSION}));
			if (arguments.showHelp) System.out.println("Help needed");
			else {
				if (arguments.interfaceType==Arguments.INTERFACE_TYPE.GUI) {
					//if (verbose) System.out.println("MuseScore2Html GUI");
					//arguments.show();
					new Gui(arguments);
				} else {
					//if (verbose) System.out.println("MuseScore2Html CLI");
					errors=processScores(arguments);
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			System.err.println("Exception: '"+exc.getMessage()+"'");
			errors=errors|1;
		}
	}

	public static void main(String[] args) {
		MuseScore2Html museScore2Html=new MuseScore2Html(args);
		if (museScore2Html.errors>0) System.exit(museScore2Html.errors);
	}
}