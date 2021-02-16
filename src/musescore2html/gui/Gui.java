package musescore2html.gui;

import musescore2html.Arguments;
import musescore2html.Translations;
import musescore2html.Config;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JMenuBar;

public class Gui extends JFrame {

	private Arguments arguments;

	private void setUp() {
		//Create and set up the window.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		//Add content to the window.
		//jframe.getContentPane().add(mainPanel.getJPanel());
		getContentPane().add(new MainPanel(arguments));
		//System.out.println("MenuBar arguments");
		//menuBar = new MenuBar(arguments);
		//setJMenuBar(menuBar.mainJMenuBar);
		//setJMenuBar(mainJMenuBar);

		//Display the window.
		pack();
		setVisible(true);
	}

	public void showGui() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				setUp();
			}
		});
	}

	public Gui(Arguments arguments) {
		super("MuseScore2Html");
		this.arguments=arguments;
		this.arguments.setMissing();
		showGui();
	}

	public Gui() {
		super("MuseScore2Html");
		arguments=new Arguments();
		arguments.setMissing();
		showGui();
	}
}