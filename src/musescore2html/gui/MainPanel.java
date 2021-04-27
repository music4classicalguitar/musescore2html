package musescore2html.gui;

import musescore2html.Arguments;
import musescore2html.Config;
import musescore2html.Version;

import musescore2html.ProcessData;
import musescore2html.ProcessScores;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.lang.InterruptedException;

import java.io.File;
import java.io.FilenameFilter;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FileDialog;

import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.Frame;
import java.awt.FlowLayout;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.JCheckBox;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class MainPanel extends JPanel implements ActionListener {

	private Arguments arguments;
	ProcessScoresTask processScoresTask;
	boolean processingScores = false;

	private final String jarDirectory=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	private final String helpDirectory=(new File(jarDirectory)).getParentFile().getParentFile().getParent()+
		File.separator+"resources"+File.separator+"help";
	private String helpLink;
	private JFrame parent;
	private JMenuBar mainJMenuBar;
	private JMenuItem
		outputdirectoryJMenuItem,
		selectscoresJMenuItem,
		addscoresJMenuItem,
		settingsJMenuItem,
		helpJMenuItem;
	private static final String newline = "\n";

	private Arguments.FILE_OPTION fileOption;
	private String[] fileOptions, generateIndexFileOptions, indexFileOptions, logLevelOptions;
	private String[] scores;
	private String lastUsedScoreDirectory, outputdirectory, lastUsedOutputDirectory;

	private JButton
		processScoresJButton,
		clearLogJButton,
		closeJButton;
	private JComboBox<String>
		fileOptionsJComboBox,
		generateIndexFileOptionsJComboBox,
		indexFileOptionsJComboBox,
		logLevelOptionsJComboBox;
	private JLabel
		musescoreJLabel,
		outputdirectoryJLabel,
		scoresJLabel,
		indexFileNameJLabel,
		logJLabel;
	private JTextField musescoreJTextField, outputdirectoryJTextField, indexFileNameJTextField;

	private FileDialog outputdirectoryFileDialog, scoresFileDialog;	
	private FileNameExtensionFilter scoreFileFilter;

	private DefaultTableModel scoresTableModel;
	private JTable scoresJTable;
	private JScrollPane scoresJScrollPane;
	private Arguments.LOG_LEVEL logLevel = Arguments.LOG_LEVEL.NORMAL;
	private JTextArea logJTextArea;
	private JCheckBox fileGenerateHtmlJCheckBox, indexFileGenerateAllJCheckBox, checkOnlyJCheckBox;

	public HelpPanel helpPanel;

	private int errors=0;

	public void setUp() {
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		setName("MainPanel");

		mainJMenuBar = new JMenuBar();

		outputdirectoryJMenuItem = new JMenuItem(arguments.translations.getKey("outputdirectory.select"));
		outputdirectoryJMenuItem.addActionListener(this);
		mainJMenuBar.add(outputdirectoryJMenuItem);

		outputdirectoryJLabel = new JLabel(arguments.translations.getKey("outputdirectory.label"));
		outputdirectoryJLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		selectscoresJMenuItem = new JMenuItem(arguments.translations.getKey("scores.select"));
		selectscoresJMenuItem.addActionListener(this);
		mainJMenuBar.add(selectscoresJMenuItem);

		addscoresJMenuItem = new JMenuItem(arguments.translations.getKey("scores.add"));
		addscoresJMenuItem.addActionListener(this);
		mainJMenuBar.add(addscoresJMenuItem);

		settingsJMenuItem = new JMenuItem(arguments.translations.getKey("settings.label"));
		settingsJMenuItem.addActionListener(this);
		mainJMenuBar.add(settingsJMenuItem);

		helpJMenuItem = new JMenuItem(arguments.translations.getKey("help.label"));
		helpJMenuItem.addActionListener(this);
		mainJMenuBar.add(helpJMenuItem);

		fileGenerateHtmlJCheckBox = new JCheckBox(arguments.translations.getKey("file.generatehtml.label"), arguments.generateHtml);

		fileOptions = new String[] {
			arguments.translations.getKey("file.option.new"),
			arguments.translations.getKey("file.option.newer"),
			arguments.translations.getKey("file.option.replace")
		};
		fileOptionsJComboBox = new JComboBox<String>(fileOptions);
		fileOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		fileOptionsJComboBox.setSelectedIndex(arguments.fileOption.ordinal());

		indexFileNameJLabel = new JLabel(arguments.translations.getKey("indexfilename.label"));
		generateIndexFileOptions = new String[] {
			arguments.translations.getKey("generateindexfile.option.none"),
			arguments.translations.getKey("generateindexfile.option.html.no"),
			arguments.translations.getKey("generateindexfile.option.html.yes")
		};
		generateIndexFileOptionsJComboBox = new JComboBox<String>(generateIndexFileOptions);
		generateIndexFileOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		generateIndexFileOptionsJComboBox.setSelectedIndex(arguments.generateIndexFileOption.ordinal());

		indexFileNameJTextField = new JTextField(arguments.indexFileName!=null?arguments.indexFileName:"");
		indexFileNameJTextField.addActionListener(this);

		indexFileOptions = new String[] {
			arguments.translations.getKey("indexfile.option.new"),
			arguments.translations.getKey("indexfile.option.newer"),
			arguments.translations.getKey("indexfile.option.replace")
		};
		indexFileOptionsJComboBox = new JComboBox<String>(indexFileOptions);
		indexFileOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		indexFileOptionsJComboBox.setSelectedIndex(arguments.indexFileOption.ordinal());

		indexFileGenerateAllJCheckBox = new JCheckBox(arguments.translations.getKey("generateindexfile.generateall.label"), arguments.generateIndexAll);

		logLevelOptions = new String[] {
			arguments.translations.getKey("loglevel.option.silent"),
			arguments.translations.getKey("loglevel.option.quiet"),
			arguments.translations.getKey("loglevel.option.normal"),
			arguments.translations.getKey("loglevel.option.verbose"),
			arguments.translations.getKey("loglevel.option.extreme")
		};
		logLevelOptionsJComboBox = new JComboBox<String>(logLevelOptions);
		logLevelOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		logLevelOptionsJComboBox.setSelectedIndex(arguments.logLevel.ordinal());

		lastUsedScoreDirectory = arguments.config.getLastUsedScoreDirectory();
		lastUsedOutputDirectory = arguments.config.getLastUsedOutputDirectory();

		musescoreJLabel = new JLabel(arguments.translations.getKey("musescore.label"));
		musescoreJTextField = new JTextField(arguments.museScore);

		outputdirectory = arguments.outputDirectory;
		outputdirectoryJTextField = new JTextField(outputdirectory);

		scoresJLabel = new JLabel(arguments.translations.getKey("scores.label"));
		scoresJLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		scoresTableModel = new DefaultTableModel(new String [] {arguments.translations.getKey("scores.label")},0) {
			@Override
			public boolean isCellEditable(int row, int column) { return false; }
		};

		scoresJTable = new JTable(scoresTableModel);
		scoresJTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		scoresJScrollPane = new JScrollPane(scoresJTable);
		scoresJScrollPane.setPreferredSize(new Dimension(100, 100));
		scoresJScrollPane.setColumnHeader(null);
		 
		parent = (JFrame) SwingUtilities.getWindowAncestor(this);
		outputdirectoryFileDialog = new FileDialog(parent, arguments.translations.getKey("outputdirectory.select"), FileDialog.LOAD);
		outputdirectoryFileDialog.setDirectory(lastUsedOutputDirectory);
		outputdirectoryFileDialog.setFilenameFilter(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (dir.isDirectory()) return true;
				return false;
			}
		});
				 
		scoresFileDialog = new FileDialog(parent, arguments.translations.getKey("scores.select"), FileDialog.LOAD);
		scoresFileDialog.setDirectory(lastUsedScoreDirectory);
		scoresFileDialog.setMultipleMode(true);
		scoresFileDialog.setFilenameFilter(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".mscz")) return true;
				if (name.toLowerCase().endsWith(".mscx")) return true;
				return false;
			}
		});

		processScoresJButton = new JButton(arguments.translations.getKey("scores.process"));
		processScoresJButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		processScoresJButton.addActionListener(this);

		checkOnlyJCheckBox = new JCheckBox(arguments.translations.getKey("checkonly.label"), arguments.checkOnly);

		logJLabel = new JLabel(arguments.translations.getKey("logging.label"));
		logJLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		logJTextArea = new JTextArea(15, 50);
		logJTextArea.setText("");
		logJTextArea.setMargin(new Insets(5, 5, 5, 5));
		logJTextArea.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(logJTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		clearLogJButton = new JButton(arguments.translations.getKey("logging.clear"));
		clearLogJButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		clearLogJButton.addActionListener(this);

		closeJButton = new JButton(arguments.translations.getKey("close.label"));
		closeJButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		closeJButton.addActionListener(this);

		add(mainJMenuBar);

		add(musescoreJLabel);
		add(musescoreJTextField);

		add(scoresJLabel);
		add(scoresJScrollPane);
		
		add(fileGenerateHtmlJCheckBox);
		add(fileOptionsJComboBox);

		add(outputdirectoryJLabel);
		add(outputdirectoryJTextField);

		add(indexFileNameJLabel);
		add(indexFileNameJTextField);
		add(indexFileGenerateAllJCheckBox);
		add(generateIndexFileOptionsJComboBox);
		add(indexFileOptionsJComboBox);

		add(logJLabel);
		add(logScrollPane);
		add(logLevelOptionsJComboBox);

		add(clearLogJButton);

		add(processScoresJButton);
		add(checkOnlyJCheckBox);

		add(closeJButton);

		for (int i=0; i<arguments.logging.size(); i++) {
			logJTextArea.append(arguments.logging.get(i).logMessage + newline);
			if (arguments.logging.get(i).logCode>0) errors++;
		}
		if (errors>0) logJTextArea.append(arguments.translations.translate(new String[] {"errors", Integer.toString(errors)}) + newline);

		setVisible(true);
		setEnabled(true);
	}

	public void changeLanguage(String language) {
		arguments.translations.setLanguage(language);
		arguments.language = language;

		outputdirectoryJMenuItem.setText(arguments.translations.getKey("outputdirectory.select"));
		selectscoresJMenuItem.setText(arguments.translations.getKey("scores.select"));
		addscoresJMenuItem.setText(arguments.translations.getKey("scores.add"));
		settingsJMenuItem.setText(arguments.translations.getKey("settings.label"));
		helpJMenuItem.setText(arguments.translations.getKey("help.label"));

		helpLink = "file:"+helpDirectory+File.separator+Version.NAME+"_"+language+".html";

		musescoreJLabel.setText(arguments.translations.getKey("musescore.label"));
		outputdirectoryJLabel.setText(arguments.translations.getKey("outputdirectory.label"));
		indexFileNameJLabel.setText(arguments.translations.getKey("indexfilename.label"));

		//scoresTableModel.setColumnIdentifiers(new String[]{arguments.translations.getKey("scores.label")});
		//scoresTableModel.setColumnIdentifiers(new String[]{});
		scoresJScrollPane.setColumnHeader(null);

		fileGenerateHtmlJCheckBox.setText(arguments.translations.getKey("file.generatehtml.label"));

		int previous = fileOptionsJComboBox.getSelectedIndex();
		fileOptions = new String[] {
			arguments.translations.getKey("file.option.new"),
			arguments.translations.getKey("file.option.newer"),
			arguments.translations.getKey("file.option.replace")
		};
		fileOptionsJComboBox.removeAllItems();
		for (int i=0;i<fileOptions.length; i++) {
			 fileOptionsJComboBox.addItem(fileOptions[i]);
		}
		fileOptionsJComboBox.setSelectedIndex(previous);

		indexFileGenerateAllJCheckBox.setText(arguments.translations.getKey("generateindexfile.generateall.label"));

		previous = generateIndexFileOptionsJComboBox.getSelectedIndex();
		generateIndexFileOptions = new String[] {
			arguments.translations.getKey("generateindexfile.option.none"),
			arguments.translations.getKey("generateindexfile.option.html.no"),
			arguments.translations.getKey("generateindexfile.option.html.yes")
		};
		generateIndexFileOptionsJComboBox.removeAllItems();
		for (int i=0;i<generateIndexFileOptions.length; i++) {
			 generateIndexFileOptionsJComboBox.addItem(generateIndexFileOptions[i]);
		}
		generateIndexFileOptionsJComboBox.setSelectedIndex(previous);

		previous = indexFileOptionsJComboBox.getSelectedIndex();
		indexFileOptions = new String[] {
			arguments.translations.getKey("indexfile.option.new"),
			arguments.translations.getKey("indexfile.option.newer"),
			arguments.translations.getKey("indexfile.option.replace")
		};
		indexFileOptionsJComboBox.removeAllItems();
		for (int i=0;i<indexFileOptions.length; i++) {
			 indexFileOptionsJComboBox.addItem(indexFileOptions[i]);
		}
		indexFileOptionsJComboBox.setSelectedIndex(previous);

		checkOnlyJCheckBox.setText(arguments.translations.getKey("checkonly.label"));

		previous = logLevelOptionsJComboBox.getSelectedIndex();
		logLevelOptions = new String[] {
			arguments.translations.getKey("loglevel.option.silent"),
			arguments.translations.getKey("loglevel.option.quiet"),
			arguments.translations.getKey("loglevel.option.normal"),
			arguments.translations.getKey("loglevel.option.verbose"),
			arguments.translations.getKey("loglevel.option.extreme")
		};
		logLevelOptionsJComboBox.removeAllItems();
		for (int i=0;i<logLevelOptions.length; i++) {
			 logLevelOptionsJComboBox.addItem(logLevelOptions[i]);
		}
		logLevelOptionsJComboBox.setSelectedIndex(previous);

		processScoresJButton.setText(arguments.translations.getKey("scores.process"));
		logJLabel.setText(arguments.translations.getKey("logging.label"));
		clearLogJButton.setText(arguments.translations.getKey("logging.clear"));
		closeJButton.setText(arguments.translations.getKey("close.label"));

		JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
		parent.revalidate();
		parent.repaint();
		parent.pack();

		for (Frame frame : Frame.getFrames()) {
			if (frame.getName().equals("Settings")) {
				frame.setTitle(arguments.translations.getKey("settings.label"));
			}
			if (frame.getName().equals("Help")) {
				frame.setTitle(arguments.translations.getKey("help.label"));
			}
		}
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == selectscoresJMenuItem) {
			getScores(false);
			
		} else if (e.getSource() == addscoresJMenuItem) {
			getScores(true);

		} else if (e.getSource() == outputdirectoryJMenuItem) {
			getOutputDirectory();

		} else if (e.getSource() == processScoresJButton) {
			if (processingScores) {
				processingScores = false;
				processScoresJButton.setText(arguments.translations.getKey("scores.process"));
				processScoresTask.cancel(true);
				processScoresTask = null;
			} else {
				processingScores = true;
				processScoresJButton.setText(arguments.translations.getKey("scores.process.cancel"));
				arguments.outputDirectory = outputdirectory;
				arguments.scores = new ArrayList<String>();
				arguments.scores.addAll(Arrays.asList(scores));
				arguments.fileOption = getFileOption();
				arguments.indexFileName = indexFileNameJTextField.getText().equals("")?null:indexFileNameJTextField.getText();
				arguments.indexFileOption = getIndexFileOption();
				arguments.generateHtml = fileGenerateHtmlJCheckBox.isSelected();
				arguments.generateIndexFileOption = getGenerateIndexFileOption();
				arguments.generateIndexAll = indexFileGenerateAllJCheckBox.isSelected();
				arguments.checkOnly = checkOnlyJCheckBox.isSelected();
				arguments.logLevel = getLogLevel();
				processScoresTask = new ProcessScoresTask(arguments);
				processScoresTask.execute();
			}
		} else if (e.getSource() == clearLogJButton) {
			logJTextArea.selectAll();
			logJTextArea.replaceSelection("");
		} else if (e.getSource() == settingsJMenuItem) {
			showSettings();
		} else if (e.getSource() == helpJMenuItem) {
			showHelp(helpLink);
		} else if (e.getSource() == closeJButton) {
			closeAction();			
		}
	}

	private void getScores(boolean add) {
		if (arguments.config.getOSId()==Config.OSId.OSX) System.setProperty("apple.awt.fileDialogForDirectories","false");
		scoresFileDialog.setVisible(true);
		String action;
		if (add) action = "add";
		else action = "select";
		if (scoresFileDialog.getFiles().length > 0) {
			if (!add) scoresTableModel.setRowCount(0);
			File files[] = scoresFileDialog.getFiles();
			scores = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				scores[i] = files[i].toString();
				try {
					logJTextArea.append(arguments.translations.translate(new String[] {"scores."+action+"ed", scores[i]}) + newline);
					scoresTableModel.addRow(new String[] { scores[i] });
					lastUsedScoreDirectory = (new File(scores[0]).getParent());
				} catch (Exception exc) {
					if (exc.getMessage()==null) logJTextArea.append(arguments.translations.getKey("scores."+action+".error") + newline);
					else logJTextArea.append(arguments.translations.translate(new String[] {"scores."+action+".error.message", exc.getMessage()}) + newline);
					exc.printStackTrace();
				}
			}
		} else {
			logJTextArea.append(arguments.translations.getKey("scores."+action+".canceled") + newline);
		}
		logJTextArea.setCaretPosition(logJTextArea.getDocument().getLength());
	}

	private void getOutputDirectory() {
		if (arguments.config.getOSId()==Config.OSId.OSX) System.setProperty("apple.awt.fileDialogForDirectories","true");
		outputdirectoryFileDialog.setVisible(true);
		if (outputdirectoryFileDialog.getFile() != null) {
			outputdirectory = outputdirectoryFileDialog.getDirectory()+outputdirectoryFileDialog.getFile();
			lastUsedOutputDirectory = outputdirectory;
			outputdirectoryJTextField.setText(outputdirectory);
			logJTextArea.append(arguments.translations.translate(new String[] {"outputdirectory.selected", outputdirectory}) + newline);
		} else {
			logJTextArea.append(arguments.translations.getKey("outputdirectory.select.canceled") + newline);
		}
		logJTextArea.setCaretPosition(logJTextArea.getDocument().getLength());
	}

	private Arguments.FILE_OPTION getFileOption() {
		for (Arguments.FILE_OPTION opt : Arguments.FILE_OPTION.values()) {
			if (opt.ordinal()==fileOptionsJComboBox.getSelectedIndex()) {
				return opt;
			}
		}
		return Arguments.FILE_OPTION.ONLY_IF_NEW;
	}

	private Arguments.INDEX_FILE_OPTION getIndexFileOption() {
		for (Arguments.INDEX_FILE_OPTION opt : Arguments.INDEX_FILE_OPTION.values()) {
			if (opt.ordinal()==indexFileOptionsJComboBox.getSelectedIndex()) {
				return opt;
			}
		}
		return Arguments.INDEX_FILE_OPTION.INDEX_ONLY_IF_NEW;
	}

	private Arguments.GENERATE_INDEX_FILE_OPTION getGenerateIndexFileOption() {
		for (Arguments.GENERATE_INDEX_FILE_OPTION opt : Arguments.GENERATE_INDEX_FILE_OPTION.values()) {
			if (opt.ordinal()==generateIndexFileOptionsJComboBox.getSelectedIndex()) {
				return opt;
			}
		}
		return Arguments.GENERATE_INDEX_FILE_OPTION.NONE;
	}

	private Arguments.LOG_LEVEL getLogLevel() {
		for (Arguments.LOG_LEVEL level : Arguments.LOG_LEVEL.values()) {
			if (level.ordinal()==logLevelOptionsJComboBox.getSelectedIndex()) {
				return level;
			}
		}
		return Arguments.LOG_LEVEL.NORMAL;
	}

	public void changeMuseScore(String musescore) {
		arguments.museScore = musescore;
		musescoreJTextField.setText(musescore);
	}

	private void showSettings() {
		for (Frame frame : Frame.getFrames()) {
			if (frame.getName().equals("Settings")) {
				frame.setVisible(true);
				frame.toFront();
				frame.requestFocus();
				return;
			}
		}

		JFrame jframe = new JFrame(arguments.translations.getKey("settings.label"));
		jframe.setLayout(new FlowLayout());
		jframe.setName("Settings");
		JPanel settings = new SettingsPanel(arguments, MainPanel.this);
		jframe.getContentPane().add(settings);
		jframe.pack();
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true);
	}

	private void showHelp(String link) {
		for (Frame frame : Frame.getFrames()) {
			if (frame.getName().equals("Help")) {
				frame.setVisible(true);
				frame.toFront();
				frame.requestFocus();
				return;
			}
		}

		JFrame jframe = new JFrame(arguments.translations.getKey("help.label"));
		jframe.setLayout(new FlowLayout());
		jframe.setName("Help");
		helpPanel = new HelpPanel(arguments);
		jframe.getContentPane().add(helpPanel);
		jframe.pack();
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true);
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
				ProcessScores processScores = new ProcessScores(arguments, processData);
				ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
				Future<Integer> futureProcessScores=executor.submit(processScores);
				while (!processData.isFinished()) {
					if (isCancelled()) {
						publish(new ProcessData.Data(arguments.translations.getKey("scores.process.canceled"),1));
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
				if (iexc.getMessage()!=null) System.err.println(arguments.translations.translate(new String[] {"scores.process.error.message", iexc.getMessage()}));
				else System.err.println(arguments.translations.translate("scores.process.error"));
			} catch (Exception exc) {
				exc.printStackTrace();
				if (exc.getMessage()!=null) System.err.println(arguments.translations.translate(new String[] {"scores.process.error.message", exc.getMessage()}));
				else System.err.println(arguments.translations.translate("scores.process.error"));
			}
			processingScores = false;
			processScoresJButton.setText(arguments.translations.getKey("scores.process"));
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
			logJTextArea.append(arguments.translations.getKey("scores.process.ready")+newline+newline);
			processingScores = false;
			processScoresJButton.setText(arguments.translations.getKey("scores.process"));
		}
	}

	public void closeAction() {
		arguments.config.setLastUsedMuseScore(arguments.museScore);
		arguments.config.setLastUsedScoreDirectory(lastUsedScoreDirectory);
		arguments.config.setLastUsedOutputDirectory(lastUsedOutputDirectory);
		try {
			arguments.config.writeConfigLastUsed();
			System.exit(0);
		} catch (Exception exc) {};
	}

	public MainPanel() {
		arguments=new Arguments();
		arguments.setMissing();
		setUp();
	}

	public MainPanel(Arguments arguments) {
		this.arguments=arguments;
		if (arguments==null) {
			System.err.println("arguments null");
			this.arguments=new Arguments();
		}
		setUp();
	}
}