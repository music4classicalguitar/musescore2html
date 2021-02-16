package musescore2html.gui;

import musescore2html.Arguments;
import musescore2html.Config;

import java.io.File;
import java.io.IOException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import java.net.URL;

import javax.swing.text.html.HTMLDocument;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Help {
	private final String jarDirectory=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	private final String helpDirectory=(new File(jarDirectory)).getParentFile().getParentFile().getParent()+File.separator+"resources"+File.separator+"help";

	public Help(Arguments arguments) {     
		JFrame jframe = new JFrame(arguments.translations.getKey("menu.help"));
		JPanel panel = new JPanel();
		LayoutManager layout = new FlowLayout();
		panel.setLayout(layout);

		JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		try {
			URL url = new URL("file:"+helpDirectory+File.separator+"MuseScore2Html.html");
			jEditorPane.setPage(url);
		} catch (IOException e) {
			jEditorPane.setContentType("text/html");
			jEditorPane.setText("<html><p>Page "+"file:/"+helpDirectory+File.separator+"MuseScore2Html.html"+" not found.</p><p>"+e.getMessage()+"</p></html>");
		}
		jEditorPane.addHyperlinkListener(new HelpHyperlinkListener());
		JScrollPane jScrollPane = new JScrollPane(jEditorPane);
		jScrollPane.setPreferredSize(new Dimension(540,400));
		panel.add(jScrollPane);
		jframe.getContentPane().add(panel, BorderLayout.CENTER);
		jframe.setSize(560, 450);
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true);
 	}
 
 	private static class HelpHyperlinkListener implements HyperlinkListener {
 
 		public void hyperlinkUpdate(HyperlinkEvent e) {
 			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 				System.out.println("Link "+e.getDescription());
 				JEditorPane pane = (JEditorPane) e.getSource();
 				if (e instanceof HTMLFrameHyperlinkEvent) {
 					HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
 					HTMLDocument doc = (HTMLDocument)pane.getDocument();
 					doc.processHTMLFrameHyperlinkEvent(evt);
 					//if (evt.getTarget().startsWith("#")) {
 						//URL url = new URL(evt.getTarget())
 						//doc.getParent().scrollToReference(new URL(evt.getTarget()).getRef());
 					//}
 					System.out.println("Link "+evt.getTarget());
 				} else {
 					try {
 						pane.setPage(e.getURL());
 					} catch (Throwable t) {
 					t.printStackTrace();
 					}
 				}
 			}
 		}
 
 	}
}