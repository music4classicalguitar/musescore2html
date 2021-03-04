package musescore2html;

import musescore2html.gui.Gui;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;

import java.lang.Runtime;

public class MuseScore2Html {

	private Arguments arguments;
	private int errors=0;

	private	static Integer preProcessScores(Arguments arguments) {
		int rc=0;
		try {
			ProcessScores processScores = new ProcessScores(arguments, null);
			rc = processScores.validateArguments();
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new RuntimeException(arguments.translations.translate(new String[]{"scores.process.error.message", exc.getMessage()}));
			else throw new RuntimeException(arguments.translations.translate("scores.process.error"));
		}
		return rc;
	}

	private	static Integer processScores(Arguments arguments) {
		int code=0,errors=0;
		try {
			ProcessData processDataScores = new ProcessData();
			ProcessInfo processInfoScores = new ProcessInfo(processDataScores, arguments.translations);
			ProcessScores processScores = new ProcessScores(arguments, processDataScores);
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
			Future<Integer> futureProcessInfoScores=executor.submit(processInfoScores);
			Future<Integer> futureProcessScores=executor.submit(processScores);
			errors=futureProcessScores.get();
			code=futureProcessInfoScores.get();
			executor.shutdown();
		} catch(Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new RuntimeException(arguments.translations.translate(new String[]{"scores.process.error.message", exc.getMessage()}));
			else throw new RuntimeException(arguments.translations.translate("scores.process.error"));
		}
		return code|errors;
	}

	private MuseScore2Html(String[] args)  {
		arguments = new Arguments();
		ProcessArguments processArguments = new ProcessArguments(args, arguments, null);
		processArguments.parseArguments();
		if (arguments.showVersion) System.out.println(arguments.translations.translate(new String[] {"version", Version.NAME, Version.VERSION}));
		if (arguments.showHelp) System.out.println("Help needed");
		else {
			if (arguments.errors==0) {
				ProcessScores processScoresValidate = new ProcessScores(arguments, null);
				if (arguments.interfaceType==Arguments.INTERFACE_TYPE.GUI) {
					processScoresValidate.validateArguments();
					new Gui(arguments);
				} else {
					errors=processScores(arguments);
				}
			}
		}
	}

	public static void main(String[] args) {
		MuseScore2Html museScore2Html=new MuseScore2Html(args);
		if (museScore2Html.errors>0) System.exit(museScore2Html.errors);
	}
}