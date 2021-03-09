package musescore2html;

public class ProcessArguments {

	private static Arguments.LOG_LEVEL logLevel = Arguments.LOG_LEVEL.NORMAL;

	private Arguments arguments;
	private ProcessData processData;
	private String args[];

	private void processInfo(Arguments.LOG_LEVEL useLogLevel, String message, int code) {
		arguments.errors=arguments.errors|code;			
		if (arguments.logLevel.ordinal()>=useLogLevel.ordinal()) {
			if (processData == null && arguments.interfaceType == Arguments.INTERFACE_TYPE.CLI) {
				if (code>0) System.err.println(message);
				else System.out.println(message);
			} else arguments.logging.add(new Arguments.Logging(message,code));
		}
	}

	public void showArguments() {
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.getKey("info.arguments"), 0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.osid", String.valueOf(arguments.config.getOSId())}), 0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.language", arguments.language}), 0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.loglevel", String.valueOf(arguments.logLevel)}), 0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.interface", String.valueOf(arguments.interfaceType)}), 0);
		if (arguments.cfgPath!=null) processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.configuration.path", arguments.cfgPath}), 0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.configuration", String.valueOf(arguments.config!=null)}), 0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.musescore", arguments.museScore}), 0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.outputdirectory", arguments.outputDirectory}), 0);
		if (arguments.scores.size()>0) {
			for (int i=0; i<arguments.scores.size(); i++) {
				processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"score.selected", arguments.scores.get(i)}), 0);
			}
		}
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.fileoption", String.valueOf(arguments.fileOption)}), 0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.generatehtml", String.valueOf(arguments.generateHtml)}), 0);
		if (arguments.indexFileName!=null) {
			processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.indexfilename", arguments.indexFileName}), 0);
		}
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.indexfile.all", String.valueOf(arguments.generateIndexAll)}), 0);
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.indexfileoption", String.valueOf(arguments.indexFileOption)}), 0);
		if (arguments.errors>0) processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.errors", String.valueOf(arguments.errors)}), 0);
		if (arguments.checkOnly) processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.checkonly", String.valueOf(arguments.checkOnly)}), 0);
	}

	public ProcessArguments(String[] args, Arguments arguments, ProcessData processData) {
		this.args = args.clone();
		this.arguments = arguments;
		this.processData = processData;
		boolean cli = false, gui = false;
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
					arguments.logLevel = Arguments.LOG_LEVEL.SILENT;
					break;
				case "-q":
					arguments.logLevel = Arguments.LOG_LEVEL.QUIET;
					break;
				case "-v":
					arguments.logLevel = Arguments.LOG_LEVEL.VERBOSE;
					break;
				case "-e":
					arguments.logLevel = Arguments.LOG_LEVEL.EXTREME;
					break;
				case "-x":
					arguments.generateHtml = false;
					break;
				case "-cli":
					cli = true;
					break;
				case "-gui":
					gui = true;
					break;
				default:
					break;
			}
		}
		if (cli && ! gui) arguments.interfaceType = Arguments.INTERFACE_TYPE.CLI;
		else arguments.interfaceType = Arguments.INTERFACE_TYPE.GUI;
		
		try {
			Config config;
			if (arguments.cfgPath != null) {
				if (arguments.language!=null) config = new Config(new Translations(arguments.language), arguments.cfgPath);
				else config = new Config(arguments.cfgPath);
				arguments.translations = config.getTranslations();
			} else {
				if (arguments.language != null) {
					config = new Config(new Translations(arguments.language));
					arguments.translations = config.getTranslations();
				} else {
					config = new Config();
					arguments.translations = config.getTranslations();
				}
			}
			arguments.config=config;
			if (arguments.config!=null) {
				arguments.translations=arguments.config.getTranslations();
				arguments.language = arguments.translations.getLanguage();
				arguments.museScore = config.getMuseScore();
				arguments.outputDirectory = config.getOutputDirectory();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) System.err.println(arguments.translations.translate(new String[] {"exception.config.error.message", exc.getMessage()}));
			else System.err.println(arguments.translations.translate(new String[] {"exception.config.error"}));
			processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"info",exc.getMessage()}),0);
		}
	}

	public void parseArguments() {
		int oi=0, oI=0, oc=0, om=0, oo=0, of=0, oF=0, ol=0, oq=0;
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.parsing.start"}),0);

		for (int i=0;i<args.length;i++) {
			switch (args[i]) {
				case "":
					processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.getKey("option.empty"),1);
					break;
				case "-cli":
					oi++;
					if (oi>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.interface.multiple", args[i]}),1);
					}
					arguments.interfaceType=Arguments.INTERFACE_TYPE.CLI;
					break;
				case "-gui":
					oi++;
					if (oi>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.interface.multiple", args[i]}),1);
					}
					arguments.interfaceType=Arguments.INTERFACE_TYPE.GUI;
					break;
				case "-C":
					oc++;
					if (oc>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.config.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else arguments.cfgPath=args[++i];
					} else {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-l":
					ol++;
					if (ol>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.language.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else arguments.language=args[++i];
					} else {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-m":
					om++;
					if (om>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.musescore.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else arguments.museScore=args[++i];
					} else {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-o":
					oo++;
					if (oo>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.config.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else arguments.outputDirectory=args[++i];
					} else {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-p":
					of++;
					if (of>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.file.multiple", args[i]}),1);
					}
					arguments.fileOption=Arguments.FILE_OPTION.ONLY_IF_NEW;
					break;
				case "-n":
					of++;
					if (of>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.file.multiple", args[i]}),1);
					}
					arguments.fileOption=Arguments.FILE_OPTION.ONLY_IF_NEWER;
					break;
				case "-r":
					of++;
					if (of>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.file.multiple", args[i]}),1);
					}
					arguments.fileOption=Arguments.FILE_OPTION.REPLACE;
					break;
				case "-i":
					oI++;
					if (oI>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.indexfile.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else {
							arguments.generateHtml = true;
							arguments.indexFileName=args[++i];
							arguments.generateIndexFileOption = Arguments.GENERATE_INDEX_FILE_OPTION.INDEX_TO_HTML;
							if (!arguments.generateHtml) processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.indexfile.nohtml", args[i]}),1);
						}
					} else {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-I":
					oI++;
					if (oI>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.indexfile.multiple", args[i]}),1);
					}
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) {
							processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
						} else {
							arguments.generateHtml = false;
							arguments.indexFileName=args[++i];
							arguments.generateIndexFileOption = Arguments.GENERATE_INDEX_FILE_OPTION.INDEX_NO_HTML;
						}
					} else {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.missing.argument", args[i]}),1);
					}
					break;
				case "-a":
					arguments.generateIndexAll = true;
					break;
				case "-P":
					oF++;
					if (of>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.indexfile.multiple", args[i]}),1);
					}
					arguments.indexFileOption=Arguments.INDEX_FILE_OPTION.INDEX_ONLY_IF_NEW;
					break;
				case "-N":
					oF++;
					if (of>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.indexfile.multiple", args[i]}),1);
					}
					arguments.indexFileOption=Arguments.INDEX_FILE_OPTION.INDEX_ONLY_IF_NEWER;
					break;
				case "-R":
					oF++;
					if (of>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.indexfile.multiple", args[i]}),1);
					}
					arguments.indexFileOption=Arguments.INDEX_FILE_OPTION.INDEX_REPLACE;
					break;
				case "-c":
					arguments.checkOnly=true;
					break;
				case "-s":
					oq++;
					if (oq>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.verbose.multiple", args[i]}),1);
					}
					arguments.logLevel = Arguments.LOG_LEVEL.SILENT;
					break;
				case "-q":
					oq++;
					if (oq>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.verbose.multiple", args[i]}),1);
					}
					arguments.logLevel = Arguments.LOG_LEVEL.QUIET;
					break;
				case "-v":
					oq++;
					if (oq>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.verbose.multiple", args[i]}),1);
					}
					arguments.logLevel = Arguments.LOG_LEVEL.VERBOSE;
					break;
				case "-e":
					oq++;
					if (oq>1) {
						processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.verbose.multiple", args[i]}),1);
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
						if (a.endsWith(".mscz")||a.endsWith(".mscx")) {
							arguments.scores.add(args[i]);
						} else processInfo(Arguments.LOG_LEVEL.QUIET, arguments.translations.translate(new String[] {"option.unknown", args[+i]}),0);
					} else {
						arguments.scores.add(args[i]);
					}
					break;
			}
		}
		showArguments();
		processInfo(Arguments.LOG_LEVEL.EXTREME, arguments.translations.translate(new String[] {"info.parsing.ready"}),0);
	}
}