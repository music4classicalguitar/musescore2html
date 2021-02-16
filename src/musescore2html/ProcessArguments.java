package musescore2html;

import java.util.ArrayList;
import java.lang.Thread;

import java.util.concurrent.Callable;

public class ProcessArguments implements Callable<Arguments> {

	private ProcessData processData;
	private Translations translations;
	private String[] args;
	private Arguments.LOG_LEVEL logLevel = Arguments.LOG_LEVEL.NORMAL;

	private Arguments arguments;

	private static void processInfo(ProcessData processData, Translations translations, Arguments arguments, Arguments.LOG_LEVEL logLevel, String message, int code) {
		arguments.errors=arguments.errors|code;
		try {
			if (logLevel.ordinal()>arguments.logLevel.ordinal()) processData.addData(message,code);
		} catch (InterruptedException iexc) {
			System.err.println(translations.getKey("exception.error")+": '"+iexc.getMessage());
			iexc.printStackTrace();
		}
	}

	public ProcessArguments(ProcessData processData, Translations translations, String[] args, Arguments.LOG_LEVEL logLevel) {
		this.processData = processData;
		this.translations = translations;
		this.args = args.clone();
		this.logLevel = logLevel;
		arguments=new Arguments();
		for (int i=0;i<args.length;i++) {
			switch (args[i]) {
				case "-C":
					if (i+1<args.length) {
						if (!args[i+1].substring(0,1).equals("-")) arguments.cfgPath=args[++i];
					}
					break;
				case "-l":
					if (i+1<args.length) {
						if (!args[i+1].substring(0,1).equals("-")) arguments.language=args[++i];
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
				case "-e":
					logLevel = Arguments.LOG_LEVEL.EXTREME;
					break;
				default:
					break;
			}
		}
		try {
			Config config;
			if (arguments.cfgPath!=null) {
				if (arguments.language!=null) config=new Config(new Translations(arguments.language), arguments.cfgPath);
				else config=new Config(arguments.cfgPath);
			} else {
				if (arguments.language!=null) config=new Config(new Translations(arguments.language));
				else config=new Config();
			}
			arguments.config=config;
			if (arguments.config!=null)
			arguments.translations=arguments.config.getTranslations();
			arguments.language = arguments.translations.getLanguage();
			arguments.museScore = config.getMuseScore();
			arguments.outputDirectory = config.getOutputDirectory();
		} catch (Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) System.err.println(translations.translate(new String[]{"exception.config.error.message", exc.getMessage()}));
			else System.err.println(translations.translate(new String[]{"exception.config.error"}));
			processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"info",exc.getMessage()}),0);
		}
	}

	public Arguments call() throws Exception {
		int oi=0, oI=0, oc=0, om=0, oo=0, of=0, ol=0, oq=0;
		processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"info.parsing.start"}),0);

		for (int i=0;i<args.length;i++) {
			switch (args[i]) {
				case "-cli":
					oi++;
					if (oi>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.interface.multiple", args[i]}),1);
					}
					arguments.interfaceType=Arguments.INTERFACE_TYPE.CLI;
					break;
				case "-gui":
					oi++;
					if (oi>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.interface.multiple", args[i]}),1);
					}
					arguments.interfaceType=Arguments.INTERFACE_TYPE.GUI;
					break;
				case "-C":
					oc++;
					if (oc>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.config.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else arguments.cfgPath=args[++i];
					} else {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-l":
					ol++;
					if (ol>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.language.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else arguments.language=args[++i];
					} else {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-m":
					om++;
					if (om>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.musescore.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else arguments.museScore=args[++i];
					} else {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-o":
					oo++;
					if (oo>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.config.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else arguments.outputDirectory=args[++i];
					} else {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-i":
					oI++;
					if (oI>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.indexfile.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else {
							arguments.indexFileName=args[++i];
							arguments.generateHtml=false;
						}
					} else {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-p":
					of++;
					if (of>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.file.multiple", args[i]}),1);
					}
					arguments.fileOption=ProcessScores.FILE_OPTION.ONLY_IF_NEW;
					break;
				case "-n":
					of++;
					if (of>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.file.multiple", args[i]}),1);
					}
					arguments.fileOption=ProcessScores.FILE_OPTION.ONLY_IF_NEWER;
					break;
				case "-r":
					of++;
					if (of>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.file.multiple", args[i]}),1);
					}
					arguments.fileOption=ProcessScores.FILE_OPTION.REPLACE;
					break;
				case "-c":
					arguments.checkOnly=true;
					break;
				case "-s":
					oq++;
					if (oq>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.verbose.multiple", args[i]}),1);
					}
					arguments.logLevel = Arguments.LOG_LEVEL.SILENT;
					break;
				case "-q":
					oq++;
					if (oq>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.verbose.multiple", args[i]}),1);
					}
					arguments.logLevel = Arguments.LOG_LEVEL.QUIET;
					break;
				case "-v":
					oq++;
					if (oq>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.verbose.multiple", args[i]}),1);
					}
					arguments.logLevel = Arguments.LOG_LEVEL.VERBOSE;
					break;
				case "-e":
					oq++;
					if (oq>1) {
						processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.verbose.multiple", args[i]}),1);
					}
					arguments.logLevel = Arguments.LOG_LEVEL.EXTREME;
					break;
				case "-V":
					arguments.showVersion=true;
					break;
				case "-h":
					arguments.showHelp=true;
					break;
				default:
					if (args[i].startsWith("-")) {
						String a=args[i].toLowerCase();
						if (a.endsWith(".mscz")||a.endsWith(".mscx")) arguments.scores.add(args[i]);
						else processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"option.unknown", args[+i]}),1);
					} else arguments.scores.add(args[i]);
					break;
			}
		}
		processInfo(processData, translations, arguments, logLevel, translations.translate(new String[] {"info.parsing.ready"}),0);
		try {
			processData.setFinished();
		} catch (InterruptedException iexc) {
			iexc.printStackTrace();
			if (iexc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"exception.error.message", iexc.getMessage()}));
			else throw new Exception(translations.translate(new String[]{"exception.error"}));
		} catch (Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) throw new Exception(translations.translate(new String[]{"exception.error.message", exc.getMessage()}));
			else throw new Exception(translations.translate(new String[]{"exception.error"}));
		} finally {
			processData.setFinished();
		}
		return arguments;
	}

}