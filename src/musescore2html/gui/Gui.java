package musescore2html.gui;

import musescore2html.Arguments;
import musescore2html.Config;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JMenuBar;

public class Gui extends JFrame {

	private Arguments arguments;

	private void setUp() {
		//Create and set up the window.
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setLayout(new FlowLayout());
		//Add content to the window.
		MainPanel mainpanel = new MainPanel(arguments);
		this.setJMenuBar(mainpanel.mainJMenuBar);
		this.getContentPane().add(mainpanel);	
		this.pack();

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (int) ((dimension.getWidth() - this.getContentPane().getWidth()) / 2);
 		int y = (int) ((dimension.getHeight() - this.getContentPane().getHeight()) / 2);
 		this.setLocation(x, y);
 		
		//Display the window.
		this.setVisible(true);
	}

	public void setDefaultLookAndFeel() {
		String uiClassName;
		if (arguments.config.getOSId()==Config.OSId.WINDOWS) {
			uiClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
		} else if (arguments.config.getOSId()==Config.OSId.OSX) {
			uiClassName = "com.apple.laf.AquaLookAndFeel";
		} else if (arguments.config.getOSId()==Config.OSId.UNIX) {
			uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel";
		} else {
			uiClassName = UIManager.getSystemLookAndFeelClassName();
		}
		if (uiClassName.equals(UIManager.getLookAndFeel().getClass().getName())) {
			//Desired L&F is already set
			return;
		}
		try {
			UIManager.setLookAndFeel(uiClassName);
		} catch (Exception ex) {
			System.err.println("Cannot set L&F " + uiClassName);
			System.err.println("Exception:" + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	public void showGui() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setDefaultLookAndFeel();
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
		this.showGui();
	}

	public Gui() {
		super("MuseScore2Html");
		this.arguments=new Arguments();
		this.arguments.setMissing();
		this.showGui();
	}
}