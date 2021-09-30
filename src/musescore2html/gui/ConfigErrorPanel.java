package musescore2html.gui;

import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ConfigErrorPanel extends JPanel {

	private String[] errors;
	private JPanel errorJPanel;
	private JTextArea logJTextArea;
	private static final String newline = "\n";
		
	public ConfigErrorPanel(String[] args) {
		this.errors=args;

		//LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		//setLayout(layout);
		setName("ConfigErrorPanel");
		errorJPanel = new JPanel();
		logJTextArea = new JTextArea(15, 50);
		logJTextArea.setText("");
		logJTextArea.setMargin(new Insets(5, 5, 5, 5));
		logJTextArea.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(logJTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		for (int i=0; i<args.length; i++) {
			logJTextArea.append(args[i] + newline);
		}
		add(logScrollPane);
		setVisible(true);
		setEnabled(true);
	}
}