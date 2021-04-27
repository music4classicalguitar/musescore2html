package musescore2html.gui;

import musescore2html.Arguments;

import java.util.Arrays;
import java.util.ArrayList;

import java.io.File;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.FileDialog;
import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Window;
import java.awt.Frame;

import javax.swing.UIManager;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

public class SettingsPanel extends JPanel implements ActionListener {

	private Arguments arguments;

	private static UIManager.LookAndFeelInfo lookandfeelChoices[] = UIManager.getInstalledLookAndFeels();
	private String[] lookandfeelOptions, languageOptions, musescoreOptions;
	private String musescore, musescoreDirectory, message;

	private JTextField messageJTextField;

	private JMenuBar mainJMenuBar;
	private JMenuItem musescoreJMenuItem;

	private DefaultTableModel musescoreTableModel;
	private JTable musescoreJTable;

	private JLabel lookandfeelJLabel, languageJLabel, musescoreJLabel, messageJLabel;

	private JComboBox<String> lookandfeelOptionsJComboBox, languageOptionsJComboBox, musescoreOptionsJComboBox;
	private JButton openJButton, saveAsDefaultJButton, saveJButton;
	private MainPanel mainPanel;
	private FileDialog openFileDialog, saveFileDialog;
	
	public SettingsPanel(Arguments arguments, MainPanel mainPanel) {
		this.arguments = arguments;
		this.mainPanel = mainPanel;

		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		setName(arguments.translations.getKey("settings.label"));

		mainJMenuBar = new JMenuBar();

		musescoreJMenuItem = new JMenuItem(arguments.translations.getKey("musescore.select"));
		musescoreJMenuItem.setMnemonic(KeyEvent.VK_M);
		musescoreJMenuItem.addActionListener(this);
		mainJMenuBar.add(musescoreJMenuItem);

		musescore = arguments.config.getMuseScore();
		musescoreJLabel = new JLabel(arguments.translations.getKey("musescore.label"));
		musescoreJLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		musescoreTableModel = new DefaultTableModel(1, 1) {
			@Override
			public boolean isCellEditable(int row, int column) { return false; }
		};
		musescoreJTable = new JTable(musescoreTableModel);
		musescoreTableModel.setRowCount(0);
		musescoreTableModel.addRow(new String[] { musescore });

		musescoreOptions = arguments.config.getMuseScoresFound();
		musescoreOptionsJComboBox = new JComboBox<String>(musescoreOptions);
		musescoreOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		musescoreOptionsJComboBox.setSelectedIndex(Arrays.asList(musescoreOptions).indexOf(arguments.museScore));
		musescoreOptionsJComboBox.addActionListener(this);

		musescoreTableModel.setRowCount(0);
		musescoreTableModel.addRow(new String[] { arguments.museScore });

		lookandfeelJLabel = new JLabel(arguments.translations.getKey("lookandfeel.label"));
		lookandfeelOptions = new String[lookandfeelChoices.length];
		String s = arguments.config.getLookAndFeel();
		int index = 0;
		for (int i=0; i<lookandfeelChoices.length; i++) {
			if (s.equals(lookandfeelChoices[i].getClassName())) index=i;
			lookandfeelOptions[i] = lookandfeelChoices[i].getName();
		}
		lookandfeelOptionsJComboBox = new JComboBox<String>(lookandfeelOptions);
		lookandfeelOptionsJComboBox.setSelectedIndex(index);
		lookandfeelOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		lookandfeelOptionsJComboBox.addActionListener(this);

		languageJLabel = new JLabel(arguments.translations.getKey("language.label"));
		languageOptions = arguments.translations.getLanguages();
		languageOptionsJComboBox = new JComboBox<String>(languageOptions);
		languageOptionsJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		languageOptionsJComboBox.setSelectedIndex(Arrays.asList(languageOptions).indexOf(arguments.language));
		languageOptionsJComboBox.addActionListener(this);

		messageJLabel = new JLabel(arguments.translations.getKey("settings.status"));
		messageJTextField = new JTextField("");

		openJButton = new JButton(arguments.translations.getKey("configfile.open"));
		openJButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		openJButton.addActionListener(this);

		saveJButton = new JButton(arguments.translations.getKey("configfile.save"));
		saveJButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		saveJButton.addActionListener(this);

		saveAsDefaultJButton = new JButton(arguments.translations.getKey("configfile.save.asdefault"));
		saveAsDefaultJButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		saveAsDefaultJButton.addActionListener(this);

		add(mainJMenuBar);

		add(musescoreJLabel);
		add(musescoreJTable);
		add(musescoreOptionsJComboBox);

		add(languageJLabel);
		add(languageOptionsJComboBox);

		add(lookandfeelJLabel);
		add(lookandfeelOptionsJComboBox);
		add(openJButton);
		add(saveJButton);
		add(saveAsDefaultJButton);
		add(messageJLabel);
		add(messageJTextField);
		changeMuseScore(musescore);
		
		JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
		openFileDialog = new FileDialog(parent, arguments.translations.getKey("configfile.open"), FileDialog.LOAD);
		openFileDialog.setDirectory(arguments.config.getDefaultConfigDirectory());

		saveFileDialog = new FileDialog(parent, arguments.translations.getKey("configfile.save.choose.name"), FileDialog.SAVE);
		saveFileDialog.setDirectory(arguments.config.getDefaultConfigDirectory());

 	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == languageOptionsJComboBox) {
			changeLanguage((String) languageOptionsJComboBox.getSelectedItem());
			updateViews();

		} else if (e.getSource() == lookandfeelOptionsJComboBox) {
			updateLAF(lookandfeelChoices[lookandfeelOptionsJComboBox.getSelectedIndex()].getClassName());
			updateViews();

		} else if (e.getSource() == musescoreOptionsJComboBox) {
			musescore = (String) musescoreOptionsJComboBox.getSelectedItem();
			musescoreTableModel.setRowCount(0);
			musescoreTableModel.addRow(new String[] { musescore });
			arguments.museScore = musescore;
			((MainPanel) mainPanel).changeMuseScore(musescore);

		} else if (e.getSource() == musescoreJMenuItem) {
			getMuseScore();

		} else if (e.getSource() == openJButton) {
			openFile();

		} else if (e.getSource() == saveJButton) {
			saveFile();

		} else if (e.getSource() == saveAsDefaultJButton) {
			saveAsDefaultFile();
		}
	}

	private void updateViews() {
		updateView(this);
		updateView(mainPanel);
		if (((MainPanel) mainPanel).helpPanel != null) {
			updateView((HelpPanel) ((MainPanel) mainPanel).helpPanel);
		}
	}

	private void getMuseScore() {
		JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
		FileDialog fd = new FileDialog(parent, "Choose a file", FileDialog.LOAD);
		fd.setDirectory((new File(musescore)).getPath().toString());
		fd.setVisible(true);
		if (fd.getFile() != null) {
			String mscore = fd.getDirectory()+fd.getFile();
			changeMuseScore(mscore);
		}
	}

	private void validateMuseScore(String mscore) {
		switch (arguments.config.getOSId()) {
			case OSX:
				if (mscore.endsWith(".app")) musescore=mscore+File.separator+"Contents"+File.separator+"MacOS"+File.separator+"mscore";
				else if ((new File(mscore)).getName().equals("mscore")) musescore=mscore;
				else {
					messageJTextField.setText(arguments.translations.translate(new String[] {"musescore.not.recognized",mscore}));
				}
				break;
			case UNIX:
				if ((new File(mscore)).getName().equals("mscore")) musescore=mscore;
				else if (mscore.endsWith(".AppImage")&&mscore.indexOf("MuseScore")>=0) musescore=mscore;
				else {
					messageJTextField.setText(arguments.translations.translate(new String[] {"musescore.not.recognized",mscore}));
				}
				break;
			case WINDOWS:
				if ((new File(mscore)).getName().indexOf("MuseScore")>=0) musescore=mscore.toString();
				else {
					messageJTextField.setText(arguments.translations.translate(new String[] {"musescore.not.recognized",mscore}));
				}
				break;
		}
	}
	
	private void changeMuseScore(String mscore) {
		validateMuseScore(mscore);
		if (!musescore.equals(mscore)) return;
		musescoreTableModel.setRowCount(0);
		musescoreTableModel.addRow(new String[] { musescore });
		arguments.museScore = musescore;
		((MainPanel) mainPanel).changeMuseScore(musescore);
		int index = -1;
		for (int i=0; i<musescoreOptions.length; i++) {
			if (mscore.toString().equals(musescoreOptions[i])) { index = i ; break; }
		}
		if (index == -1) {
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(musescore);
			index = 0;
			for (int i=0; i<musescoreOptions.length; i++) {
				temp.add(musescoreOptions[i]);
			}
			musescoreOptions = temp.toArray(new String[] {});
			musescoreOptionsJComboBox.removeAllItems();
			for (int i=0; i<musescoreOptions.length; i++) {
				musescoreOptionsJComboBox.addItem(musescoreOptions[i]);
			}
		}
		musescoreOptionsJComboBox.setSelectedIndex(index);
	}
	
	private void changeLanguage(String language) {
		((MainPanel) mainPanel).changeLanguage(language);
		updateView(mainPanel);
		if (((MainPanel) mainPanel).helpPanel != null) {
			(((MainPanel) mainPanel).helpPanel).changeLanguage(language);
			updateView((HelpPanel) ((MainPanel) mainPanel).helpPanel);
		}

		arguments.language = language;
		arguments.translations.setLanguage(language);

		setName(arguments.translations.getKey("settings.label"));
		musescoreJMenuItem.setText(arguments.translations.getKey("musescore.select"));
		musescoreJLabel.setText(arguments.translations.getKey("musescore.label"));
		languageJLabel.setText(arguments.translations.getKey("language.label"));
		lookandfeelJLabel.setText(arguments.translations.getKey("lookandfeel.label"));
	}

	private void updateView(JPanel panel) {
		JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(panel);
		parent.revalidate();
		parent.pack();
		parent.repaint();
	}

	private static void updateLAF(String value) {
		if (UIManager.getLookAndFeel().getClass().getName().equals(value)) {
			return;
		}
		try {
			UIManager.setLookAndFeel(value);
			for (Frame frame : Frame.getFrames()) {
				updateLAFRecursively(frame);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	public static void updateLAFRecursively(Window window) {
		for (Window childWindow : window.getOwnedWindows()) {
			updateLAFRecursively(childWindow);
		}
		SwingUtilities.updateComponentTreeUI(window);
	}

	private void openFile() {
		openFileDialog.setFile(arguments.config.getConfigFileName());
		openFileDialog.setVisible(true);
		String filename = openFileDialog.getDirectory()+File.separator+openFileDialog.getFile();
		if (openFileDialog.getFile() == null) messageJTextField.setText(arguments.translations.getKey("configfile.open.canceled"));
		else {
			try {
				arguments.config.getConfig(filename);
				updateLAF(arguments.config.getLookAndFeel());
				updateViews();
				changeLanguage(arguments.config.getLanguage());
				updateViews();
				messageJTextField.setText("");
			} catch (Exception exc) {
				if (exc.getMessage()==null)
				messageJTextField.setText(arguments.translations.translate(new String[] {"configfile.error.read",
				filename}));
				else messageJTextField.setText(arguments.translations.translate(new String[] {"configfile.error.read.message",
				filename, exc.getMessage()}));
			}
		}
	}

	private void saveFile() {
		saveFileDialog.setFile(arguments.config.getConfigFileName());
		saveFileDialog.setVisible(true);
		if (saveFileDialog.getFile() == null) messageJTextField.setText(arguments.translations.getKey("configfile.save.canceled"));
		else {
			String filename = saveFileDialog.getDirectory()+File.separator+saveFileDialog.getFile();
			try {
				arguments.config.setLookAndFeel(lookandfeelChoices[lookandfeelOptionsJComboBox.getSelectedIndex()].getClassName());
				arguments.config.setLanguage((String) languageOptionsJComboBox.getSelectedItem());
				arguments.config.setMuseScore(musescore);
				arguments.config.writeConfig(filename);
				messageJTextField.setText(arguments.translations.translate(new String[] {"configfile.saved",arguments.config.getConfigFile()}));
			} catch (Exception exc) {
				if (exc.getMessage()==null)
				messageJTextField.setText(arguments.translations.translate(new String[] {"configfile.error.write",
				filename}));
				else messageJTextField.setText(arguments.translations.translate(new String[] {"configfile.error.write.message",
				filename, exc.getMessage()}));
			}
		}
	}

	private void saveAsDefaultFile() {
		arguments.config.setLookAndFeel(lookandfeelChoices[lookandfeelOptionsJComboBox.getSelectedIndex()].getClassName());
		arguments.config.setLanguage((String) languageOptionsJComboBox.getSelectedItem());
		arguments.config.setMuseScore(musescore);
		try {
			arguments.config.writeDefaultConfig();
			messageJTextField.setText(arguments.translations.translate(new String[] {"configfile.saved",arguments.config.getDefaultConfigFile()}));
		} catch (Exception exc) {
			if (exc.getMessage()==null)
			messageJTextField.setText(arguments.translations.translate(new String[] {"configfile.error.write",
			arguments.config.getDefaultConfigFile()}));
			else messageJTextField.setText(arguments.translations.translate(new String[] {"configfile.error.write.message",
			arguments.config.getDefaultConfigFile(), exc.getMessage()}));
		}
	}
}