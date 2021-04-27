package musescore2html.gui;

import musescore2html.Arguments;

import java.util.ArrayList;

import java.net.MalformedURLException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Desktop;

import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.BoxLayout;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HelpPanel extends JPanel implements ActionListener {
	private final String jarDirectory=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	private final String helpDirectory=(new File(jarDirectory)).getParentFile().getParentFile().getParent()+File.separator+"resources"+File.separator+"help";

	private JScrollPane jScrollPane;
	private JEditorPane jEditorPane;
	private JMenuBar helpJMenuBar;
	private JMenuItem backJMenuItem, forwardJMenuItem;
	private URL url;
	private ArrayList<String> links = new ArrayList<String>();
	private int linkIndex = 0, linkLast = 0;
	private Arguments arguments;

	public HelpPanel(Arguments arguments) {
		this.arguments = arguments;
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		setName("HelpPanel");

		helpJMenuBar = new JMenuBar();
		
		backJMenuItem = new JMenuItem("<");
		backJMenuItem.setMnemonic(KeyEvent.VK_LEFT);
		backJMenuItem.addActionListener(this);
		backJMenuItem.setEnabled(false);
		helpJMenuBar.add(backJMenuItem);

		forwardJMenuItem = new JMenuItem(">");
		forwardJMenuItem.setMnemonic(KeyEvent.VK_RIGHT);
		forwardJMenuItem.addActionListener(this);
		forwardJMenuItem.setEnabled(false);
		helpJMenuBar.add(forwardJMenuItem);
		
		helpJMenuBar.setVisible (true);

		jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		try {
			links.add("file:"+helpDirectory+File.separator+"MuseScore2Html_"+arguments.language+".html");
			url = new URL(links.get(linkIndex));
			jEditorPane.setPage(url);
		} catch (IOException e) {
			jEditorPane.setContentType("text/html");
			jEditorPane.setText("<html><p>Page "+url.toString()+" not found.</p><p>"+e.getMessage()+"</p></html>");
		}
		jEditorPane.addHyperlinkListener(new HelpHyperlinkListener());
		jScrollPane = new JScrollPane(jEditorPane);
		jScrollPane.setPreferredSize(new Dimension(540,400));
				
		add(helpJMenuBar);
		add(jScrollPane);
 	}

	public void actionPerformed(ActionEvent e) {

		//Handle open file(s) button action.
		if (e.getSource() == backJMenuItem) {
			if (linkIndex>0) {
				linkIndex--;
				setURL(links.get(linkIndex));
			}
		} else if (e.getSource() == forwardJMenuItem) {
			if (linkIndex<linkLast) {
				linkIndex++;
				setURL(links.get(linkIndex));				
			}
		}
		if (linkIndex>0) backJMenuItem.setEnabled(true);
		else backJMenuItem.setEnabled(false);
		if (linkIndex<linkLast) forwardJMenuItem.setEnabled(true);
		else forwardJMenuItem.setEnabled(false);
	}
	
	private void setURL(String link) {
		try {
			HTMLDocument doc = (HTMLDocument)jEditorPane.getDocument();
			doc.putProperty(HTMLDocument.StreamDescriptionProperty, null);
			//URL url = new URL(link.substring(0, link.indexOf("?")));
			jEditorPane.setPage(link);
			String ref = (new URL(link)).getRef();
			if (ref != null) scrollToReference(ref);
		} catch (Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) System.err.println(arguments.translations.translate(new String[]{"exception.error.message",exc.getMessage()}));
			else System.err.println(arguments.translations.translate(new String[]{"exception.error"}));
		}
	}
 	
 	private void scrollToReference(String reference) {
 		HTMLDocument doc = (HTMLDocument)jEditorPane.getDocument();
 		Element elementById = doc.getElement(reference);
 		if (elementById != null) {
 			try {
 				int pos = elementById.getStartOffset();
 				Rectangle r = jEditorPane.modelToView(pos);
 				if (r != null) {
 					r.height = jEditorPane.getVisibleRect().height;
 					jEditorPane.scrollRectToVisible(r);
 					jEditorPane.setCaretPosition(pos);
 				}
 			} catch (BadLocationException e) {
 				System.err.println("BadLocationException "+e.getMessage());
 				return;
 			}
 			jEditorPane.scrollToReference(reference);			
 		}
 	}
 
 	private class HelpHyperlinkListener implements HyperlinkListener {
 		public void hyperlinkUpdate(HyperlinkEvent e) {
 			URL link;
 			String protocol;
 			String ref;
 			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
  				JEditorPane pane = (JEditorPane) e.getSource();
 				if (e instanceof HTMLFrameHyperlinkEvent) {
  					HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
 					HTMLDocument doc = (HTMLDocument)pane.getDocument();
 					doc.processHTMLFrameHyperlinkEvent(evt);
 					ref = e.getURL().getRef();
 					protocol = e.getURL().getProtocol();
 					if (!protocol.equals("file")) {
 						useDesktop(e.getURL());
 						return;
 					}
					try {
 						link = new URL(evt.getTarget());
  						if (link.getPath().equals(url.getPath())) {
  							setLink(link.toString());
 							scrollToReference(ref);
 						}
					} catch (MalformedURLException exc) {}
  				} else {
 					try {
 						protocol = e.getURL().getProtocol();
 						if (!protocol.equals("file")) {
 							useDesktop(e.getURL());
 							return;
 						}
 						link = e.getURL();
 						ref = e.getURL().getRef();
 						
 						if (link.getProtocol().equals("")) link = new URL("file:"+e.getURL());
 						setLink(link.toString());
 						if (link.getPath().equals(url.getPath())) {
 							scrollToReference(ref);
 						} else {
							pane.setPage(link);
							scrollToReference(ref);
  							url = new URL(link.getProtocol()+":"+link.getPath());
 						}
 					} catch (Throwable t) {
 						t.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 	
 	private void setLink(String link) {
 		if (link.equals(links.get(linkIndex))) return;
 		linkIndex++;
		if (linkIndex<links.size()) {
			links.set(linkIndex,link);
		} else {
			links.add(link);
		}
		backJMenuItem.setEnabled(true);
		if (linkIndex>=linkLast) forwardJMenuItem.setEnabled(false);
		linkLast = linkIndex;
 	}
 	
 	private void useDesktop(URL url) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(url.toURI());
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
 	}
	
	public void changeLanguage(String language) {
		for (int i=0; i<links.size(); i++) {
			links.set(i,links.get(i).replaceAll("_[^._]+[.]html","_"+language+".html"));
		}
		setURL(links.get(linkIndex));
	}

}