package musescore2html.gui;

import musescore2html.Arguments;
import musescore2html.Translations;
import musescore2html.Config;
import musescore2html.Version;

import musescore2html.ProcessData;
import musescore2html.ProcessScores;

import java.util.Date;
import javax.swing.UIManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.awt.Desktop;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Component;

import java.awt.Toolkit;
import java.awt.Dimension;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
//import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.SwingWorker;
import javax.swing.JFrame;

import java.util.Vector;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;

public class MainPanel extends JPanel implements ActionListener {

	private Arguments arguments;
	ProcessScoresTask processScoresTask;
	boolean processingScores = false;

	private final String jarDirectory=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	private final String helpDirectory=(new File(jarDirectory)).getParentFile().getParentFile().getParent()+File.separator+"resources"+File.separator+"help";
	private String aboutLink, helpLink;
	public JMenuBar mainJMenuBar;
	private JMenuItem aboutJMenuItem, helpJMenuItem;
	private static final String newline = "\n";

	private String[] languageOptions;
	private ProcessScores.FILE_OPTION fileOption;
	private String[] fileOptions;

	private JButton
		selectMuseScoreJButton,
		selectScoresJButton,
		selectOutputDirectoryJButton,
		processScoresJButton,
		clearLogJButton,
		closeJButton;
	private JComboBox<String>
		languageOptionsJComboBox,
		fileOptionsJComboBox,
		indexFileNameOptionsJComboBox,
		logLevelOptionsJComboBox;
	private JLabel languageJLabel, indexFileNameJLabel, logJLabel;
	private JTextField indexFileNameJTextField;
	private JFileChooser
		museScoreJFileChooser,
		outputDirectoryJFileChooser,
		scoreJFileChooser;
	private FileNameExtensionFilter scoreFileFilter;

	private DefaultTableModel museScoreTableModel, scoresTableModel, outputDirectoryTableModel;
	private JTable museScoreJTable, scoresJTable, outputDirectoryJTable;
	private JScrollPane scoresJScrollPane;
	private String museScore;
	private String[] scores;
	private String museScoreDirectory, lastUsedScoreDirectory, outputDirectory, lastUsedOutputDirectory;
	private boolean generateIndexFile;
	private String indexFileNameOptions[], indexFileName;
	private int indexFileNameOption = 0;
	private Arguments.LOG_LEVEL logLevel = Arguments.LOG_LEVEL.NORMAL;
	private String[] logLevelOptions;
	private JTextArea logJTextArea;
	
	private int errors=0;

	public void setUp() {
		mainJMenuBar = new JMenuBar();

		helpJMenuItem = new JMenuItem(arguments.translations.getKey("menu.help"));
		helpJMenuItem.setMnemonic(KeyEvent.VK_H);
		helpJMenuItem.addActionListener(this);
		mainJMenuBar.add(helpJMenuItem);

		aboutJMenuItem = new JMenuItem(arguments.translations.getKey("menu.about"));
		aboutJMenuItem.setMnemonic(KeyEvent.VK_A);
		aboutJMenuItem.addActionListener(this);
		mainJMenuBar.add(aboutJMenuItem);

		languageJLabel = new JLabel(arguments.translations.getKey("language"));
		languageOptions = arguments.translations.getLanguages();
		languageOptionsJComboBox = new JComboBox<String>(languageOptions);
		languageOptionsJComboBox.addActionListener(this);
		languageOptionsJComboBox.setMaximumSize(languageOptionsJComboBox.getPreferredSize());

		fileOptions = new String[] {
			arguments.translations.getKey("gui.file.option.new"),
			arguments.translations.getKey("gui.file.option.newer"),
			arguments.translations.getKey("gui.file.option.replace")
		};
		fileOptionsJComboBox = new JComboBox<String>(fileOptions);
		fileOptionsJComboBox.addActionListener(this);
		fileOptionsJComboBox.setMaximumSize(fileOptionsJComboBox.getPreferredSize());
		fileOptionsJComboBox.setSelectedIndex(arguments.fileOption.ordinal());

		indexFileNameJLabel = new JLabel(arguments.translations.getKey("gui.indexfilename.label"));
		indexFileNameOptions = new String[] {
			arguments.translations.getKey("gui.indexfilename.option.no"),
			arguments.translations.getKey("gui.indexfilename.option.yes.html"),
			arguments.translations.getKey("gui.indexfilename.option.yes")
		};
		indexFileNameOptionsJComboBox = new JComboBox<String>(indexFileNameOptions);
		indexFileNameOptionsJComboBox.addActionListener(this);
		indexFileNameOptionsJComboBox.setMaximumSize(indexFileNameOptionsJComboBox.getPreferredSize());
		indexFileNameOptionsJComboBox.setSelectedIndex(indexFileNameOption);

		indexFileNameJTextField=new JTextField();
		indexFileNameJTextField.addActionListener(this);

		logLevelOptions = new String[] {
			arguments.translations.getKey("gui.loglevel.option.silent"),
			arguments.translations.getKey("gui.loglevel.option.quiet"),
			arguments.translations.getKey("gui.loglevel.option.normal"),
			arguments.translations.getKey("gui.loglevel.option.verbose"),
			arguments.translations.getKey("gui.loglevel.option.extreme")
		};
		logLevelOptionsJComboBox = new JComboBox<String>(logLevelOptions);
		logLevelOptionsJComboBox.addActionListener(this);
		logLevelOptionsJComboBox.setMaximumSize(logLevelOptionsJComboBox.getPreferredSize());
		logLevelOptionsJComboBox.setSelectedIndex(arguments.logLevel.ordinal());

		museScore = arguments.config.getMuseScore();
		museScoreDirectory = arguments.config.getLastUsedMuseScoreDirectory();
		lastUsedScoreDirectory = arguments.config.getLastUsedScoreDirectory();
		outputDirectory = arguments.outputDirectory;
		lastUsedOutputDirectory = arguments.config.getLastUsedOutputDirectory();
		museScoreTableModel = new DefaultTableModel(1, 1) {
			@Override
			public boolean isCellEditable(int row, int column) { return false; }
		};
		scoresTableModel = new DefaultTableModel(new String[]{arguments.translations.getKey("gui.scorestable.columntitle")}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) { return false; }
		};
		outputDirectoryTableModel = new DefaultTableModel(1, 1) {
			@Override
			public boolean isCellEditable(int row, int column) { return false; }
		};

		museScoreJTable = new JTable(museScoreTableModel);
		scoresJTable = new JTable(scoresTableModel);
		outputDirectoryJTable = new JTable(outputDirectoryTableModel);

		// Create a musescore chooser
		museScoreJFileChooser = new JFileChooser();
		museScoreJFileChooser.setMultiSelectionEnabled(false);
		museScoreJFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		museScoreJFileChooser.setFileSystemView(FileSystemView.getFileSystemView());

		selectMuseScoreJButton = new JButton(arguments.translations.getKey("gui.button.select.musescore"));
		selectMuseScoreJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectMuseScoreJButton.addActionListener(this);

		museScoreTableModel.setRowCount(0);
		museScoreTableModel.addRow(new String[] { museScore.toString() });
		scoresJScrollPane = new JScrollPane(scoresJTable);

		// Create a directory chooser
		outputDirectoryJFileChooser = new JFileChooser();
		outputDirectoryJFileChooser.setAcceptAllFileFilterUsed(false);
		outputDirectoryJFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		FileFilter outputDirectoryFileFilter = new FileFilter() {
			//private final String[] supportedExtensions = new String[]{".*"};
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
			@Override
			public String getDescription() {
				return arguments.translations.getKey("gui.filefilter.alldirectories");
			}
		};
		outputDirectoryJFileChooser.setFileFilter(outputDirectoryFileFilter);
		outputDirectoryJFileChooser.setCurrentDirectory(new File(lastUsedOutputDirectory));

		outputDirectoryTableModel.setRowCount(0);
		outputDirectoryTableModel.addRow(new String[] { outputDirectory });

		selectOutputDirectoryJButton = new JButton(arguments.translations.getKey("gui.button.select.outputdirectory"));
		selectOutputDirectoryJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectOutputDirectoryJButton.addActionListener(this);

		// Create a score file chooser
		scoreJFileChooser = new JFileChooser();
		scoreJFileChooser.setMultiSelectionEnabled(true);
		scoreJFileChooser.setAcceptAllFileFilterUsed(false);
		scoreJFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		scoreFileFilter = new FileNameExtensionFilter(arguments.translations.getKey("gui.musescore.files"), "mscz", "mscx");
		scoreJFileChooser.setFileFilter(scoreFileFilter);
		scoreJFileChooser.setCurrentDirectory(new File(lastUsedScoreDirectory));

		selectScoresJButton = new JButton(arguments.translations.getKey("gui.button.select.scores"));
		selectScoresJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectScoresJButton.addActionListener(this);

		//scoresTableModel = new DefaultTableModel(new String[]{arguments.translations.getKey("gui.scorestable.columntitle")},1);
		scoresJTable = new JTable(scoresTableModel);
		scoresJTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		scoresJScrollPane.setPreferredSize(new Dimension(100, 100));
		for (int i=0; i<arguments.scores.size(); i++) {
			scoresTableModel.addRow(new String[]{arguments.scores.get(i)});
		}
		scores=arguments.scores.toArray(new String[0]);

		processScoresJButton = new JButton(arguments.translations.getKey("gui.button.process.scores"));
		processScoresJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		processScoresJButton.addActionListener(this);

		logJLabel = new JLabel(arguments.translations.getKey("gui.label.logging"));
		logJLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		logJTextArea = new JTextArea(5, 50);
		logJTextArea.setMargin(new Insets(5, 5, 5, 5));
		logJTextArea.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(logJTextArea);

		clearLogJButton = new JButton(arguments.translations.getKey("gui.button.clearlog"));
		clearLogJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		clearLogJButton.addActionListener(this);

		closeJButton = new JButton(arguments.translations.getKey("gui.button.close"));
		closeJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		closeJButton.addActionListener(e -> System.exit(0));

		LayoutManager layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		setLayout(layout);

		add(mainJMenuBar);

		add(languageJLabel);
		add(languageOptionsJComboBox);
		add(selectMuseScoreJButton);
		add(museScoreJTable);
		add(selectOutputDirectoryJButton);
		add(outputDirectoryJTable);
		add(fileOptionsJComboBox);
		add(indexFileNameOptionsJComboBox);
		add(indexFileNameJLabel);
		add(indexFileNameJTextField);
		add(selectScoresJButton);
		add(scoresJScrollPane);
		add(processScoresJButton);
		add(logLevelOptionsJComboBox);
		add(logJLabel);
		add(logScrollPane);
		add(clearLogJButton);
		add(closeJButton);

		languageOptionsJComboBox.setSelectedIndex(Arrays.asList(languageOptions).indexOf(arguments.language));

		for (int i=0; i<arguments.logging.size(); i++) {
			logJTextArea.append(arguments.logging.get(i).logMessage + newline);
			if (arguments.logging.get(i).logCode>0) errors++;
		}
		if (errors>0) logJTextArea.append(arguments.translations.translate(new String[]{"errors", Integer.toString(errors)}) + newline);

		setVisible(true);
		setEnabled(true);
	}

	public void changeLanguage(String language) {
		arguments.translations.setLanguage(language);
		languageJLabel.setText(arguments.translations.getKey("language"));

		int previous = fileOptionsJComboBox.getSelectedIndex();
		fileOptions = new String[] {
			arguments.translations.getKey("gui.file.option.new"),
			arguments.translations.getKey("gui.file.option.newer"),
			arguments.translations.getKey("gui.file.option.replace")
		};
		fileOptionsJComboBox.removeAllItems();
		for (int i=0;i<fileOptions.length; i++) {
			 fileOptionsJComboBox.addItem(fileOptions[i]);
		}
		fileOptionsJComboBox.setMaximumSize(fileOptionsJComboBox.getPreferredSize());
		fileOptionsJComboBox.setSelectedIndex(previous);
		
		previous = indexFileNameOptionsJComboBox.getSelectedIndex();
		indexFileNameOptions = new String[] {
			arguments.translations.getKey("gui.indexfilename.option.no"),
			arguments.translations.getKey("gui.indexfilename.option.yes.html"),
			arguments.translations.getKey("gui.indexfilename.option.yes")
		};
		indexFileNameOptionsJComboBox.removeAllItems();
		for (int i=0;i<indexFileNameOptions.length; i++) {
			 indexFileNameOptionsJComboBox.addItem(indexFileNameOptions[i]);
		}
		indexFileNameOptionsJComboBox.setMaximumSize(indexFileNameOptionsJComboBox.getPreferredSize());
		indexFileNameOptionsJComboBox.setSelectedIndex(previous);

		previous = logLevelOptionsJComboBox.getSelectedIndex();
		logLevelOptions = new String[] {
			arguments.translations.getKey("gui.loglevel.option.silent"),
			arguments.translations.getKey("gui.loglevel.option.quiet"),
			arguments.translations.getKey("gui.loglevel.option.normal"),
			arguments.translations.getKey("gui.loglevel.option.verbose"),
			arguments.translations.getKey("gui.loglevel.option.extreme")
		};
		logLevelOptionsJComboBox.removeAllItems();
		for (int i=0;i<logLevelOptions.length; i++) {
			 logLevelOptionsJComboBox.addItem(logLevelOptions[i]);
		}
		logLevelOptionsJComboBox.setMaximumSize(logLevelOptionsJComboBox.getPreferredSize());
		logLevelOptionsJComboBox.setSelectedIndex(previous);
		
		scoreFileFilter = new FileNameExtensionFilter(arguments.translations.getKey("gui.musescore.files"), "mscz", "mscx");
		scoreJFileChooser.setFileFilter(scoreFileFilter);

		selectMuseScoreJButton.setText(arguments.translations.getKey("gui.button.select.musescore"));
		selectOutputDirectoryJButton.setText(arguments.translations.getKey("gui.button.select.outputdirectory"));
		processScoresJButton.setText(arguments.translations.getKey("gui.button.process.scores"));
		logJLabel.setText(arguments.translations.getKey("gui.label.logging"));
		clearLogJButton.setText(arguments.translations.getKey("gui.button.clearlog"));
		closeJButton.setText(arguments.translations.getKey("gui.button.close"));

		aboutJMenuItem.setText(arguments.translations.getKey("menu.about"));
		helpJMenuItem.setText(arguments.translations.getKey("menu.help"));
		aboutLink =	"file:"+helpDirectory+File.separator+Version.NAME+"_About_"+language+".html";
		helpLink = "file:"+helpDirectory+File.separator+Version.NAME+"_"+language+".html";
	}

	public void actionPerformed(ActionEvent e) {

		//Handle open file(s) button action.
		if (e.getSource() == selectScoresJButton) {
			scoreJFileChooser.setCurrentDirectory(new File(lastUsedScoreDirectory));
			int returnVal = scoreJFileChooser.showOpenDialog(MainPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				scoresTableModel.setRowCount(0);
				File files[] = scoreJFileChooser.getSelectedFiles();
				scores = new String[files.length];
				for (int i = 0; i < files.length; i++) {
					scores[i] = files[i].toString();
					//String name[] = utils.splitPath(files[i].toString());
					try {
						//utils.validateScore(files[i].getName(), name);
						logJTextArea.append(arguments.translations.translate(new String[] {"gui.score.selected", files[i].getName()}) + newline);
						scoresTableModel.addRow(new String[] {
							files[i].getName()
						});
						lastUsedScoreDirectory = (new File(scores[0]).getAbsolutePath());
					} catch (Exception exc) {
						logJTextArea.append(arguments.translations.getKey("gui.score.select.canceled") + newline);
					}
				}
			} else {
				logJTextArea.append(arguments.translations.getKey("gui.score.select.canceled") + newline);
			}
			logJTextArea.setCaretPosition(logJTextArea.getDocument().getLength());

			//Handle open directory button action.
		} else if (e.getSource() == selectOutputDirectoryJButton) {
			outputDirectoryJFileChooser.setCurrentDirectory(new File(lastUsedOutputDirectory));
			int returnVal = outputDirectoryJFileChooser.showOpenDialog(MainPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				outputDirectory = outputDirectoryJFileChooser.getSelectedFile().toString();
				lastUsedOutputDirectory = outputDirectory;
				outputDirectoryTableModel.setRowCount(0);
				outputDirectoryTableModel.addRow(new String[] {
					outputDirectory
				});
				logJTextArea.append(arguments.translations.translate(new String[] {"gui.outputdirectory.selected", outputDirectory}) + newline);
			} else {
				logJTextArea.append(arguments.translations.getKey("gui.outputdirectory.select.canceled") + newline);
			}
			logJTextArea.setCaretPosition(logJTextArea.getDocument().getLength());

			//Handle select musescore button action.
		} else if (e.getSource() == selectMuseScoreJButton) {
			museScoreJFileChooser.setCurrentDirectory(new File(museScoreDirectory));
			int returnVal = museScoreJFileChooser.showOpenDialog(MainPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File mscore = museScoreJFileChooser.getSelectedFile();
				museScore=mscore.toString();
				museScoreTableModel.setRowCount(0);
				museScoreTableModel.addRow(new String[] { museScore });
				logJTextArea.append(arguments.translations.translate(new String[] {"gui.musescore.selected", mscore.getName()}) + newline);
			} else {
				logJTextArea.append(arguments.translations.getKey("gui.musescore.select.canceled") + newline);
			}
			logJTextArea.setCaretPosition(logJTextArea.getDocument().getLength());

			//Handle select process scores button action.
		} else if (e.getSource() == processScoresJButton) {
			if (processingScores) {
				processingScores = false;
				processScoresJButton.setText(arguments.translations.getKey("gui.button.process.scores"));
				processScoresTask.cancel(true);
				processScoresTask = null;
			} else {
				processingScores = true;
				processScoresJButton.setText(arguments.translations.getKey("gui.button.process.scores.cancel"));
				arguments.museScore = museScore;
				arguments.outputDirectory = outputDirectory;
				arguments.scores = new ArrayList<String>();
				arguments.scores.addAll(Arrays.asList(scores));
				arguments.fileOption = getFileOption();
				indexFileName = indexFileNameJTextField.getText().equals("")?null:indexFileNameJTextField.getText();
				arguments.indexFileName = indexFileName;
				arguments.generateHtml = indexFileNameOptionsJComboBox.getSelectedIndex()<2;
				arguments.logLevel = getLogLevel();
				processScoresTask = new ProcessScoresTask(arguments);
				processScoresTask.execute();
			}
		} else if (e.getSource() == fileOptionsJComboBox) {
			String s = (String) fileOptionsJComboBox.getSelectedItem();
			for (ProcessScores.FILE_OPTION opt : ProcessScores.FILE_OPTION.values()) {
				if (opt.ordinal()==fileOptionsJComboBox.getSelectedIndex()) {
					fileOption=opt;
					break ;
				}
			}
		} else if (e.getSource() == logLevelOptionsJComboBox) {
			String s = (String) logLevelOptionsJComboBox.getSelectedItem();
			for (Arguments.LOG_LEVEL level : Arguments.LOG_LEVEL.values()) {
				if (level.ordinal()==logLevelOptionsJComboBox.getSelectedIndex()) {
					logLevel=level;
					break ;
				}
			}
		} else if (e.getSource() == indexFileNameOptionsJComboBox) {
			generateIndexFile=indexFileNameOptionsJComboBox.getSelectedIndex()==1?true:false;
		} else if (e.getSource() == languageOptionsJComboBox) {
			String s = (String) languageOptionsJComboBox.getSelectedItem();
			changeLanguage(s);
		} else if (e.getSource() == clearLogJButton) {
			logJTextArea.selectAll();
			logJTextArea.replaceSelection("");
		} else if (e.getSource() == aboutJMenuItem) {
			showHelp(aboutLink);
		} else if (e.getSource() == helpJMenuItem) {
			showHelp(helpLink);
		}
	}

	private ProcessScores.FILE_OPTION getFileOption() {
		for (ProcessScores.FILE_OPTION opt : ProcessScores.FILE_OPTION.values()) {
			if (opt.ordinal()==fileOptionsJComboBox.getSelectedIndex()) {
				return opt;
			}
		}
		return ProcessScores.FILE_OPTION.ONLY_IF_NEW;
	}

	private Arguments.LOG_LEVEL getLogLevel() {
		for (Arguments.LOG_LEVEL level : Arguments.LOG_LEVEL.values()) {
			if (level.ordinal()==logLevelOptionsJComboBox.getSelectedIndex()) {
				return level;
			}
		}
		return Arguments.LOG_LEVEL.NORMAL;
	}

	private void showHelp(String link) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				URL url = new URL(link);
				desktop.browse(url.toURI());
			} catch (Exception e) {
				new Help(arguments);
			}
		}
	}

	private void validateMuseScore(File mscore) {
		String warning="";
		switch (arguments.config.getOSId()) {
			case OSX:
				if (mscore.toString().endsWith(".app")) museScore=mscore.toString()+File.separator+"Contents"+File.separator+"MacOS"+File.separator+"mscore";
				else if (mscore.getName().equals("mscore")) museScore=mscore.toString();
				else {
					museScore=mscore.toString();
					warning=arguments.translations.translate(new String[] {"musescore.not.recognized",museScore});
				}
				break;
			case UNIX:
				if (mscore.getName().equals("mscore")) museScore=mscore.toString();
				else if (mscore.toString().endsWith(".AppImage")&&mscore.toString().indexOf("MuseScore")>=0) museScore=mscore.toString();
				else {
					museScore=mscore.toString();
					warning=arguments.translations.translate(new String[] {"musescore.not.recognized",museScore});
				}
				break;
			case WINDOWS:
				if (mscore.getName().indexOf("MuseScore")>=0) museScore=mscore.toString();
				else if (mscore.getName().indexOf("mscore")>=0) museScore=mscore.toString();
				else {
					museScore=mscore.toString();
					warning=arguments.translations.translate(new String[] {"musescore.not.recognized",museScore});
				}
				break;
		}
		if (!warning.equals("")) logJTextArea.append(warning);
	}

	class ProcessScoresTask extends SwingWorker<Void, ProcessData.Data> {
		private Arguments arguments;
		boolean terminate = true ;

		public ProcessScoresTask(Arguments arguments) {
			this.arguments = arguments;
		}

        @Override
        protected Void doInBackground() {
			try {
				ProcessData processData = new ProcessData();
				ProcessScores processScores = new ProcessScores(processData, arguments);
				ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
				Future<Integer> futureProcessScores=executor.submit(processScores);
				while (!processData.isFinished()) {
					if (isCancelled()) {
						publish(new ProcessData.Data(arguments.translations.getKey("gui.process.scores.canceled"),1));
						if (terminate) executor.shutdownNow();
						terminate = false;
					}
					while (processData.hasData()) {
						ProcessData.Data data[]=processData.getData();
						for (int i=0; i<data.length; i++) {
							publish(data[i]);
						}
					}
				}
				errors = futureProcessScores.get();
			} catch (InterruptedException iexc) {
				iexc.printStackTrace();
				if (iexc.getMessage()!=null) System.err.println(arguments.translations.translate(new String[]{"processinfo.error.message", iexc.getMessage()}));
				else System.err.println(arguments.translations.translate("processinfo.error"));
			} catch (Exception exc) {
				exc.printStackTrace();
				if (exc.getMessage()!=null) System.err.println(arguments.translations.translate(new String[]{"processinfo.error.message", exc.getMessage()}));
				else System.err.println(arguments.translations.translate("processinfo.error"));
			}
			processingScores = false;
			processScoresJButton.setText(arguments.translations.getKey("gui.button.process.scores"));
			return null;
		}

        @Override
        protected void process(List<ProcessData.Data> data) {
        	for (int i=0; i<data.size(); i++) {
        		logJTextArea.append(data.get(i).message+newline);
        	}
        }

        @Override
        protected void done() {
        	logJTextArea.append(arguments.translations.getKey("gui.process.scores.ready")+newline+newline);
			processingScores = false;
			processScoresJButton.setText(arguments.translations.getKey("gui.button.process.scores"));
        }
	}

	public MainPanel() {
		arguments=new Arguments();
		arguments.setMissing();
		setUp();
	}

	public MainPanel(Arguments arguments) {
		this.arguments=arguments;
		setUp();
	}
}