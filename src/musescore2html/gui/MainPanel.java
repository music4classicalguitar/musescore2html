package musescore2html.gui;

import musescore2html.Arguments;
import musescore2html.Translations;
import musescore2html.Config;
import musescore2html.Version;

import musescore2html.ProcessData;
import musescore2html.ProcessScores;

import java.util.Date;

import java.util.Vector;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import java.lang.InterruptedException;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.Container;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

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

import javax.swing.MenuElement;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuKeyEvent;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JCheckBox;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.SwingWorker;
import javax.swing.JFrame;

public class MainPanel extends JPanel implements ActionListener, MenuKeyListener {

	private Arguments arguments;
	ProcessScoresTask processScoresTask;
	boolean processingScores = false;

	private final String jarDirectory=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	private final String helpDirectory=(new File(jarDirectory)).getParentFile().getParentFile().getParent()+
		File.separator+"resources"+File.separator+"help";
	private String helpLink;
	public JMenuBar mainJMenuBar;
	private JMenuItem
		musescoreJMenuItem,
		outputdirectoryJMenuItem,
		scoresJMenuItem,
		helpJMenuItem;
	private static final String newline = "\n";

	private Arguments.FILE_OPTION fileOption;
	private String[] languageOptions, fileOptions, generateIndexFileOptions, indexFileOptions, logLevelOptions;
	private String museScore;
	private String[] scores;
	private String museScoreDirectory, lastUsedScoreDirectory, outputDirectory, lastUsedOutputDirectory;

	private JButton
		processScoresJButton,
		clearLogJButton,
		closeJButton;
	private JComboBox<String>
		languageOptionsJComboBox,
		fileOptionsJComboBox,
		generateIndexFileOptionsJComboBox,
		indexFileOptionsJComboBox,
		logLevelOptionsJComboBox;
	private JLabel
		musescoreJLabel,
		outputdirectoryJLabel,
		scoresJLabel,
		languageJLabel,
		indexFileNameJLabel,
		logJLabel;
	private JTextField indexFileNameJTextField;
	private JFileChooser
		musescoresJFileChooser,
		outputdirectoryJFileChooser,
		scoresJFileChooser;
	private FileNameExtensionFilter scoreFileFilter;

	private DefaultTableModel museScoreTableModel, scoresTableModel, outputDirectoryTableModel;
	private JTable musescoreJTable, scoresJTable, outputdirectoryJTable;
	private JScrollPane scoresJScrollPane;
	private Arguments.LOG_LEVEL logLevel = Arguments.LOG_LEVEL.NORMAL;
	private JTextArea logJTextArea;
	private JCheckBox fileGenerateHtmlJCheckBox, indexFileGenerateAllJCheckBox, checkOnlyJCheckBox;

	private int errors=0;

	public void setUp() {
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);

		mainJMenuBar = new JMenuBar();

		musescoreJMenuItem = new JMenuItem(arguments.translations.getKey("musescore.select"));
		musescoreJMenuItem.setMnemonic(KeyEvent.VK_M);
		musescoreJMenuItem.addActionListener(this);
		musescoreJMenuItem.addMenuKeyListener(this);
		mainJMenuBar.add(musescoreJMenuItem);

		musescoreJLabel = new JLabel(arguments.translations.getKey("musescore.label"));
		musescoreJLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		outputdirectoryJMenuItem = new JMenuItem(arguments.translations.getKey("outputdirectory.select"));
		outputdirectoryJMenuItem.setMnemonic(KeyEvent.VK_O);
		outputdirectoryJMenuItem.addActionListener(this);
		outputdirectoryJMenuItem.addMenuKeyListener(this);
		mainJMenuBar.add(outputdirectoryJMenuItem);

		outputdirectoryJLabel = new JLabel(arguments.translations.getKey("outputdirectory.label"));
		outputdirectoryJLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		scoresJMenuItem = new JMenuItem(arguments.translations.getKey("scores.select"));
		scoresJMenuItem.setMnemonic(KeyEvent.VK_S);
		scoresJMenuItem.addActionListener(this);
		scoresJMenuItem.addMenuKeyListener(this);
		mainJMenuBar.add(scoresJMenuItem);
		scoresJLabel = new JLabel(arguments.translations.getKey("scores.label"));
		scoresJLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		helpJMenuItem = new JMenuItem(arguments.translations.getKey("help.label"));
		helpJMenuItem.setMnemonic(KeyEvent.VK_H);
		helpJMenuItem.addActionListener(this);
		mainJMenuBar.add(helpJMenuItem);

		languageJLabel = new JLabel(arguments.translations.getKey("language.label"));
		languageOptions = arguments.translations.getLanguages();
		languageOptionsJComboBox = new JComboBox<String>(languageOptions);
		languageOptionsJComboBox.setMaximumSize(languageOptionsJComboBox.getPreferredSize());
		languageOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// adapt before setting addActionListener
		languageOptionsJComboBox.setSelectedIndex(Arrays.asList(languageOptions).indexOf(arguments.language));
		languageOptionsJComboBox.addActionListener(this);

		fileGenerateHtmlJCheckBox = new JCheckBox(arguments.translations.getKey("file.generatehtml.label"), arguments.generateHtml);

		fileOptions = new String[] {
			arguments.translations.getKey("file.option.new"),
			arguments.translations.getKey("file.option.newer"),
			arguments.translations.getKey("file.option.replace")
		};
		fileOptionsJComboBox = new JComboBox<String>(fileOptions);
		fileOptionsJComboBox.addActionListener(this);
		fileOptionsJComboBox.setMaximumSize(fileOptionsJComboBox.getPreferredSize());
		fileOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		fileOptionsJComboBox.setSelectedIndex(arguments.fileOption.ordinal());

		indexFileNameJLabel = new JLabel(arguments.translations.getKey("indexfilename.label"));
		generateIndexFileOptions = new String[] {
			arguments.translations.getKey("generateindexfile.option.none"),
			arguments.translations.getKey("generateindexfile.option.html.no"),
			arguments.translations.getKey("generateindexfile.option.html.yes")
		};
		generateIndexFileOptionsJComboBox = new JComboBox<String>(generateIndexFileOptions);
		generateIndexFileOptionsJComboBox.addActionListener(this);
		generateIndexFileOptionsJComboBox.setMaximumSize(generateIndexFileOptionsJComboBox.getPreferredSize());
		generateIndexFileOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		generateIndexFileOptionsJComboBox.setSelectedIndex(arguments.generateIndexFileOption.ordinal());

		indexFileNameJTextField=new JTextField(arguments.indexFileName!=null?arguments.indexFileName:"");
		indexFileNameJTextField.addActionListener(this);

		indexFileOptions = new String[] {
			arguments.translations.getKey("indexfile.option.new"),
			arguments.translations.getKey("indexfile.option.newer"),
			arguments.translations.getKey("indexfile.option.replace")
		};
		indexFileOptionsJComboBox = new JComboBox<String>(indexFileOptions);
		indexFileOptionsJComboBox.addActionListener(this);
		indexFileOptionsJComboBox.setMaximumSize(indexFileOptionsJComboBox.getPreferredSize());
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
		logLevelOptionsJComboBox.addActionListener(this);
		logLevelOptionsJComboBox.setMaximumSize(logLevelOptionsJComboBox.getPreferredSize());
		logLevelOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

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
		scoresTableModel = new DefaultTableModel(new String[]{arguments.translations.getKey("scores.label")}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) { return false; }
		};
		outputDirectoryTableModel = new DefaultTableModel(1, 1) {
			@Override
			public boolean isCellEditable(int row, int column) { return false; }
		};

		musescoreJTable = new JTable(museScoreTableModel);

		outputdirectoryJTable = new JTable(outputDirectoryTableModel);

		// Create a musescore chooser
		musescoresJFileChooser = new JFileChooser();
		musescoresJFileChooser.setMultiSelectionEnabled(false);
		musescoresJFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		musescoresJFileChooser.setFileSystemView(FileSystemView.getFileSystemView());

		museScoreTableModel.setRowCount(0);
		museScoreTableModel.addRow(new String[] { museScore.toString() });

		// Create a directory chooser
		outputdirectoryJFileChooser = new JFileChooser();
		outputdirectoryJFileChooser.setAcceptAllFileFilterUsed(false);
		outputdirectoryJFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		scoresJTable = new JTable(scoresTableModel);
		scoresJTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		scoresJScrollPane = new JScrollPane(scoresJTable);
		scoresJScrollPane.setPreferredSize(new Dimension(100, 100));

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
		outputdirectoryJFileChooser.setFileFilter(outputDirectoryFileFilter);
		outputdirectoryJFileChooser.setCurrentDirectory(new File(lastUsedOutputDirectory));

		outputDirectoryTableModel.setRowCount(0);
		outputDirectoryTableModel.addRow(new String[] { outputDirectory });

		// Create a score file chooser
		scoresJFileChooser = new JFileChooser();
		scoresJFileChooser.setMultiSelectionEnabled(true);
		scoresJFileChooser.setAcceptAllFileFilterUsed(false);
		scoresJFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		scoreFileFilter = new FileNameExtensionFilter(arguments.translations.getKey("scores.label"), "mscz", "mscx");
		scoresJFileChooser.setFileFilter(scoreFileFilter);
		scoresJFileChooser.setCurrentDirectory(new File(lastUsedScoreDirectory));

		for (int i=0; i<arguments.scores.size(); i++) {
			scoresTableModel.addRow(new String[]{arguments.scores.get(i)});
		}
		scores=arguments.scores.toArray(new String[0]);

		processScoresJButton = new JButton(arguments.translations.getKey("scores.process"));
		processScoresJButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		processScoresJButton.addActionListener(this);

		checkOnlyJCheckBox = new JCheckBox(arguments.translations.getKey("checkonly.label"), arguments.checkOnly);

		logJLabel = new JLabel(arguments.translations.getKey("logging.label"));
		logJLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		logJTextArea = new JTextArea(5, 50);
		logJTextArea.setMargin(new Insets(5, 5, 5, 5));
		logJTextArea.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(logJTextArea);

		clearLogJButton = new JButton(arguments.translations.getKey("logging.clear"));
		clearLogJButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		clearLogJButton.addActionListener(this);

		closeJButton = new JButton(arguments.translations.getKey("close.label"));
		closeJButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		closeJButton.addActionListener(e -> System.exit(0));

		add(mainJMenuBar);

		add(languageJLabel);
		add(languageOptionsJComboBox);

		add(musescoreJLabel);
		add(musescoreJTable);

		add(outputdirectoryJLabel);
		add(outputdirectoryJTable);

		add(scoresJLabel);
		add(scoresJScrollPane);
		add(fileGenerateHtmlJCheckBox);
		add(fileOptionsJComboBox);

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

		musescoreJMenuItem.setText(arguments.translations.getKey("musescore.select"));
		outputdirectoryJMenuItem.setText(arguments.translations.getKey("outputdirectory.select"));
		scoresJMenuItem.setText(arguments.translations.getKey("scores.select"));
		helpJMenuItem.setText(arguments.translations.getKey("help.label"));
		
		helpLink = "file:"+helpDirectory+File.separator+Version.NAME+"_"+language+".html";

		languageJLabel.setText(arguments.translations.getKey("language.label"));
		musescoreJLabel.setText(arguments.translations.getKey("musescore.label"));
		outputdirectoryJLabel.setText(arguments.translations.getKey("outputdirectory.label"));
		indexFileNameJLabel.setText(arguments.translations.getKey("indexfilename.label"));
	
		scoresTableModel.setColumnIdentifiers(new String[]{arguments.translations.getKey("scores.label")});
		
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
		fileOptionsJComboBox.setMaximumSize(fileOptionsJComboBox.getPreferredSize());
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
		generateIndexFileOptionsJComboBox.setMaximumSize(generateIndexFileOptionsJComboBox.getPreferredSize());
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
		indexFileOptionsJComboBox.setMaximumSize(indexFileOptionsJComboBox.getPreferredSize());
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
		logLevelOptionsJComboBox.setMaximumSize(logLevelOptionsJComboBox.getPreferredSize());
		logLevelOptionsJComboBox.setSelectedIndex(previous);

		scoreFileFilter = new FileNameExtensionFilter(arguments.translations.getKey("scores.label"), "mscz", "mscx");
		scoresJFileChooser.setFileFilter(scoreFileFilter);

		processScoresJButton.setText(arguments.translations.getKey("scores.process"));
		logJLabel.setText(arguments.translations.getKey("logging.label"));
		clearLogJButton.setText(arguments.translations.getKey("logging.clear"));
		closeJButton.setText(arguments.translations.getKey("close.label"));

		JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
		parent.revalidate();
		parent.repaint();
		parent.pack();
	}

	public void actionPerformed(ActionEvent e) {

		//Handle open file(s) button action.
		if (e.getSource() == scoresJMenuItem) {
			scoresJFileChooser.setCurrentDirectory(new File(lastUsedScoreDirectory));
			int returnVal = scoresJFileChooser.showOpenDialog(MainPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				scoresTableModel.setRowCount(0);
				File files[] = scoresJFileChooser.getSelectedFiles();
				scores = new String[files.length];
				for (int i = 0; i < files.length; i++) {
					scores[i] = files[i].toString();
					try {
						logJTextArea.append(arguments.translations.translate(new String[] {"score.selected", files[i].getName()}) + newline);
						scoresTableModel.addRow(new String[] {
							files[i].getName()
						});
						lastUsedScoreDirectory = (new File(scores[0]).getAbsolutePath());
					} catch (Exception exc) {
						logJTextArea.append(arguments.translations.getKey("scores.select.canceled") + newline);
					}
				}
			} else {
				logJTextArea.append(arguments.translations.getKey("scores.select.canceled") + newline);
			}
			logJTextArea.setCaretPosition(logJTextArea.getDocument().getLength());

			//Handle open directory button action.
		} else if (e.getSource() == outputdirectoryJMenuItem) {
			outputdirectoryJFileChooser.setCurrentDirectory(new File(lastUsedOutputDirectory));
			int returnVal = outputdirectoryJFileChooser.showOpenDialog(MainPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				outputDirectory = outputdirectoryJFileChooser.getSelectedFile().toString();
				lastUsedOutputDirectory = outputDirectory;
				outputDirectoryTableModel.setRowCount(0);
				outputDirectoryTableModel.addRow(new String[] {
					outputDirectory
				});
				logJTextArea.append(arguments.translations.translate(new String[] {"outputdirectory.selected", outputDirectory}) + newline);
			} else {
				logJTextArea.append(arguments.translations.getKey("outputdirectory.select.canceled") + newline);
			}
			logJTextArea.setCaretPosition(logJTextArea.getDocument().getLength());

			//Handle select musescore button action.
		} else if (e.getSource() == musescoreJMenuItem) {
			musescoresJFileChooser.setCurrentDirectory(new File(museScoreDirectory));
			int returnVal = musescoresJFileChooser.showOpenDialog(MainPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File mscore = musescoresJFileChooser.getSelectedFile();
				validateMuseScore(mscore);
				museScoreTableModel.setRowCount(0);
				museScoreTableModel.addRow(new String[] { museScore });
				logJTextArea.append(arguments.translations.translate(new String[] {"musescore.selected", mscore.getName()}) + newline);
			} else {
				logJTextArea.append(arguments.translations.getKey("musescore.select.canceled") + newline);
			}
			logJTextArea.setCaretPosition(logJTextArea.getDocument().getLength());

			//Handle select process scores button action.
		} else if (e.getSource() == processScoresJButton) {
			if (processingScores) {
				processingScores = false;
				processScoresJButton.setText(arguments.translations.getKey("scores.process"));
				processScoresTask.cancel(true);
				processScoresTask = null;
			} else {
				processingScores = true;
				processScoresJButton.setText(arguments.translations.getKey("scores.process.cancel"));
				arguments.language = languageOptions[languageOptionsJComboBox.getSelectedIndex()];
				arguments.translations.setLanguage(arguments.language);
				arguments.museScore = museScore;
				arguments.outputDirectory = outputDirectory;
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
		//} else if (e.getSource() == fileOptionsJComboBox) {
		//} else if (e.getSource() == logLevelOptionsJComboBox) {
		//} else if (e.getSource() == indexFileOptionsJComboBox) {
		} else if (e.getSource() == languageOptionsJComboBox) {
			String s = (String) languageOptionsJComboBox.getSelectedItem();
			changeLanguage(s);
		} else if (e.getSource() == clearLogJButton) {
			logJTextArea.selectAll();
			logJTextArea.replaceSelection("");
		} else if (e.getSource() == helpJMenuItem) {
			showHelp(helpLink);
		}
	}

	public void menuKeyPressed(MenuKeyEvent e) {
		System.out.println("keyPressed "+e.getKeyCode());
		switch (e.getKeyCode()) {
			case KeyEvent.VK_M:
				break;
		}
		MenuElement[] path = e.getPath();
 		JMenuItem item = (JMenuItem) path[path.length-1];
		System.out.println("Key typed: "+e.getKeyChar()
		 + ", "+e.getKeyText(e.getKeyCode())
		 + " on "+item.getText());	}

	public void menuKeyReleased(MenuKeyEvent e) {
		MenuElement[] path = e.getPath();
		JMenuItem item = (JMenuItem) path[path.length-1];
		System.out.println("Key pressed: "+e.getKeyChar()
			+ ", "+e.getKeyText(e.getKeyCode())
			+ " on "+item.getText());
		System.out.println("keyReleased "+e.getKeyCode());
	}

	public void menuKeyTyped(MenuKeyEvent e) {
		System.out.println("keyTyped "+e.getKeyCode());
		switch (e.getKeyCode()) {
			case KeyEvent.VK_M:
				break;
		}
	  MenuElement[] path = e.getPath();
	  JMenuItem item = (JMenuItem) path[path.length-1];
	  System.out.println("Key released: "+e.getKeyChar()
		 + ", "+e.getKeyText(e.getKeyCode())
		 + " on "+item.getText());
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

	private void showHelp(String link) {
		JFrame jframe = new JFrame(arguments.translations.getKey("help.label"));
		//jframe.setLayout(new BorderLayout());
		Help help = new Help(arguments);
		jframe.getContentPane().add(help);
		jframe.setSize(560, 450);
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true);
		//new Help(arguments);
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