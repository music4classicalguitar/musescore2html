package musescore2html;

import musescore2html.gui.Gui;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class MuseScore2Html {

	private final String jarDirectory=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	private final String manDirectory=(new File(jarDirectory)).getParentFile().getParentFile().getParent()+File.separator+"resources"+File.separator+"man";

	private Arguments arguments;
	private int errors=0;

	private	void showHelp() {
		try {
			Path path = FileSystems.getDefault().getPath(manDirectory+File.separator+"m2h_"+arguments.language+".txt");
			BufferedReader read = Files.newBufferedReader(path, Charset.forName("UTF-8"));
			String line = null;
			while ((line = read.readLine()) != null) System.out.println(line);
			read.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private	static Integer preProcessScores(Arguments arguments) {
		int rc=0;
		ProcessScores processScores = new ProcessScores(arguments, null);
		rc = processScores.validateArguments();
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
		} catch (InterruptedException exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) System.err.println(arguments.translations.translate(new String[] {"scores.process.error.message", exc.getMessage()}));
			else System.err.println(arguments.translations.translate("scores.process.error"));
		} catch (ExecutionException exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) System.err.println(arguments.translations.translate(new String[] {"scores.process.error.message", exc.getMessage()}));
			else System.err.println(arguments.translations.translate("scores.process.error"));
		}
		return code|errors;
	}

	private MuseScore2Html(String[] args)  {
		arguments = new Arguments();
		ProcessArguments processArguments = new ProcessArguments(args, arguments, null);
		processArguments.parseArguments();
		if (arguments.showVersion) {
			System.out.println(arguments.translations.translate(new String[] {"version", Version.NAME, Version.VERSION}));
			return;
		} else if (arguments.showHelp) {
			showHelp();
			return;
		} else {
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