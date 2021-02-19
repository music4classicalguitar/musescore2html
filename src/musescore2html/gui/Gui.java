package musescore2html.gui;

import musescore2html.Arguments;
import musescore2html.Translations;
import musescore2html.Config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
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
		
		setLayout(new FlowLayout());
		//Add content to the window.
		MainPanel mainpanel = new MainPanel(arguments);
		getContentPane().add(mainpanel);	
		pack();

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (int) ((dimension.getWidth() - this.getContentPane().getWidth()) / 2);
 		int y = (int) ((dimension.getHeight() - this.getContentPane().getHeight()) / 2);
 		setLocation(x, y);
		
		//Display the window.
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