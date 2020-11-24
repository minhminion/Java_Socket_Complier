package socket.client.GUI.components;

import socket.commons.enums.Language;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class CodeEditor extends JRootPane implements HyperlinkListener,
        SyntaxConstants {
    private Language language;
    private RTextScrollPane scrollPane;
    private RSyntaxTextArea textArea;

    public CodeEditor(Language language) {
        this.language = language;
        textArea = createTextArea();
        textArea.setSyntaxEditingStyle(SYNTAX_STYLE_JAVA);
        scrollPane = new RTextScrollPane(textArea, true);
        getContentPane().add(scrollPane);
        ErrorStrip errorStrip = new ErrorStrip(textArea);
        getContentPane().add(errorStrip, BorderLayout.LINE_END);

        setTheme("dark.xml");
    }

    private RSyntaxTextArea createTextArea () {
        RSyntaxTextArea textArea = new RSyntaxTextArea(25, 70);
        textArea.setTabSize(3);
        textArea.setCaretPosition(0);
        textArea.addHyperlinkListener(this);
        textArea.requestFocusInWindow();
        textArea.setMarkOccurrences(true);
        textArea.setCodeFoldingEnabled(true);
        textArea.setClearWhitespaceLinesEnabled(false);

        // ADD MAP ACTION
        // Increase and Decrease font size
        InputMap im = textArea.getInputMap();
        ActionMap am = textArea.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "decreaseFontSize");
        am.put("decreaseFontSize", new RSyntaxTextAreaEditorKit.DecreaseFontSizeAction());
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "increaseFontSize");
        am.put("increaseFontSize", new RSyntaxTextAreaEditorKit.IncreaseFontSizeAction());

        return textArea;
    }



    public void setTheme (String xml) {
        InputStream in = getClass().
                getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/" + xml);
        try {
            Theme theme = Theme.load(in);
            theme.apply(textArea);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void setText(String resource) {
        BufferedReader r;
        try {
//            r = new BufferedReader(new InputStreamReader(
//                    getClass().getResourceAsStream("/"+resource), StandardCharsets.UTF_8));
            r = new BufferedReader(new FileReader(resource));
            textArea.read(r, null);
            r.close();
            textArea.setCaretPosition(0);
            textArea.discardAllEdits();
        } catch (RuntimeException re) {
            throw re; // FindBugs
        } catch (Exception e) { // Never happens
            textArea.setText("Type here to see syntax highlighting");
        }
    }

    public void setLanguage (Language language) {
        this.language = language;
    }

    public Language getLanguage () {
        return language;
    }

    public void setCaretPosition(int pos) {
        textArea.setCaretPosition(pos);
    }

    public void setSyntaxEditingStyle(String style) {
        textArea.setSyntaxEditingStyle(style);
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(String code) {
        this.textArea.setText(code);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {

    }

}
