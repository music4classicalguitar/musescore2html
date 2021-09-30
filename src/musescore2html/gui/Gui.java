package musescore2html.gui;

import musescore2html.Arguments;
import musescore2html.Config;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.FlowLayout;

import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Gui extends JFrame {

	private Arguments arguments;
	private MainPanel mainPanel;
	private ConfigErrorPanel configErrorPanel;
	private String[] errors;

	private void setUp() {
		//Create and set up the window.
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.setLayout(new FlowLayout());
		//Add content to the window.
		mainPanel = new MainPanel(arguments);
		this.getContentPane().add(mainPanel);	
		this.pack();

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (int) ((dimension.getWidth() - this.getContentPane().getWidth()) / 2);
 		int y = (int) ((dimension.getHeight() - this.getContentPane().getHeight()) / 2);
 		this.setLocation(x, y);
 		
		this.setName("MuseScore2Html");
		
		WindowListener exitListener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mainPanel.closeAction();
			}
		};
		this.addWindowListener(exitListener);

		//Display the window.
		this.setVisible(true);
	}

	private void setUpShowErrors() {
		//Create and set up the window.
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.setLayout(new FlowLayout());
		//Add content to the window.
		configErrorPanel = new ConfigErrorPanel(errors);

		this.getContentPane().add(configErrorPanel);	
		this.pack();

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (int) ((dimension.getWidth() - this.getContentPane().getWidth()) / 2);
 		int y = (int) ((dimension.getHeight() - this.getContentPane().getHeight()) / 2);
 		this.setLocation(x, y);
 		
		this.setName("MuseScore2Html");
		
		/*
		WindowListener exitListener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		};
		this.addWindowListener(exitListener);
		*/

		//Display the window.
		this.setVisible(true);

	}

	public void setDefaultLookAndFeel() {
		String uiClassName = arguments.config.getLookAndFeel();
		if (uiClassName == null) {
			if (arguments.config.getOSId()==Config.OSId.WINDOWS) {
				uiClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			} else if (arguments.config.getOSId()==Config.OSId.OSX) {
				uiClassName = "com.apple.laf.AquaLookAndFeel";
			} else if (arguments.config.getOSId()==Config.OSId.UNIX) {
				uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel";
			} else {
				uiClassName = UIManager.getSystemLookAndFeelClassName();
			}
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
	
	public void showGui(boolean hasErrors) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (hasErrors) setUpShowErrors();
				else {
					setDefaultLookAndFeel();
					//Turn off metal's use of bold fonts
					UIManager.put("swing.boldMetal", Boolean.FALSE);
					setUp();
				}
			}
		});
	}
	
	public Gui(String[] args) {
		this.errors = args;
		this.showGui(true);
	}
	
	public Gui(Arguments arguments) {
		//super("MuseScore2Html");
		if (arguments==null) {
			System.err.println("arguments null");
			this.arguments=new Arguments();
		} else this.arguments=arguments;
		this.arguments.setMissing();
		this.showGui(false);
	}

	public Gui() {
		//super("MuseScore2Html");
		this.arguments=new Arguments();
		this.arguments.setMissing();
		this.showGui(false);
	}
	
	public static void main(String[] args) {
		new Gui();
	}

}