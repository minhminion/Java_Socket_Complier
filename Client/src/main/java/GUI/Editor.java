package GUI;

import BUS.EditorHandler;
import GUI.components.CodeEditor;
import com.complier.socket.commons.enums.Action;
import com.complier.socket.commons.enums.Language;
import com.complier.socket.commons.request.CompileRequest;
import com.complier.socket.commons.request.Request;
import com.complier.socket.commons.response.CompileResponse;
import com.complier.socket.commons.response.MessageResponse;
import com.formdev.flatlaf.FlatDarculaLaf;
import org.apache.commons.lang3.ObjectUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Editor extends JFrame{
    private EditorHandler editorHandler;

    // GUI
    private CodeEditor codeEditor;
    private Language currentLanguage = Language.JAVA;

    public Editor() {
        codeEditor = new CodeEditor();
        setContentPane(codeEditor);
        setJMenuBar(createMenuBar());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        codeEditor.setText("JavaExample.txt");
        pack();
        setTitle("Banana Boys Compiler");
    }

    private void addSyntaxItem(Language language, String name, String res, String style,
                               ButtonGroup bg, JMenu menu) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                new ChangeSyntaxStyleAction(language, name, res, style));
        bg.add(item);
        menu.add(item);
    }

    private void addThemeItem(String name, String themeXml, ButtonGroup bg,
                              JMenu menu) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                new ThemeAction(name, themeXml));
        bg.add(item);
        menu.add(item);
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();

        // Menu File
        JMenu menu =new JMenu("File");
        JMenuItem menuItem;
        KeyStroke keyStroke;
        menuItem=new JMenuItem("Open File");
        keyStroke = KeyStroke.getKeyStroke("control O");
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);

        menuItem=new JMenuItem("New File");
        keyStroke = KeyStroke.getKeyStroke("control N");
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);

        mb.add(menu);

        // Menu Language
        menu = new JMenu("Language");
        ButtonGroup bg = new ButtonGroup();
        addSyntaxItem(Language.JAVA, "Java","JavaExample.txt", SyntaxConstants.SYNTAX_STYLE_JAVA, bg, menu);
        addSyntaxItem(Language.CPP, "C++","CppExample.txt", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS, bg, menu);
        addSyntaxItem(Language.PYTHON, "Python","PythonExample.txt", SyntaxConstants.SYNTAX_STYLE_PYTHON, bg, menu);
        menu.getItem(0).setSelected(true);
        mb.add(menu);

        // Menu themes
        bg = new ButtonGroup();
        menu = new JMenu("Themes");
        addThemeItem("Default", "default.xml", bg, menu);
        addThemeItem("Default (System Selection)", "default-alt.xml", bg, menu);
        addThemeItem("Dark", "dark.xml", bg, menu);
        addThemeItem("Druid", "druid.xml", bg, menu);
        addThemeItem("Monokai", "monokai.xml", bg, menu);
        addThemeItem("Eclipse", "eclipse.xml", bg, menu);
        addThemeItem("IDEA", "idea.xml", bg, menu);
        addThemeItem("Visual Studio", "vs.xml", bg, menu);
        menu.getItem(2).setSelected(true);
        mb.add(menu);

        // Menu Run
        menu = new JMenu("Run");
        menuItem=new JMenuItem(new CompileCodeAction());
        keyStroke = KeyStroke.getKeyStroke("shift F10");
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);

        mb.add(menu);

        return mb;

    }

    public void setHandler(EditorHandler editorHandler) {
        this.editorHandler = editorHandler;
    }

    public void setText(String code) {
        codeEditor.setTextArea(code);
    }

    private class ChangeSyntaxStyleAction extends AbstractAction {

        private String res;
        private String style;
        private Language language;

        ChangeSyntaxStyleAction(Language language,String name, String res, String style) {
            putValue(NAME, name);
            this.res = res;
            this.style = style;
            this.language = language;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            currentLanguage = language;
            codeEditor.setText(res);
            codeEditor.setCaretPosition(0);
            codeEditor.setSyntaxEditingStyle(style);
        }

    }

    private class ThemeAction extends AbstractAction {

        private String xml;

        ThemeAction(String name, String xml) {
            putValue(NAME, name);
            this.xml = xml;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            InputStream in = getClass().
                    getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/" + xml);
            try {
                Theme theme = Theme.load(in);
                theme.apply(codeEditor.getTextArea());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    private class CompileCodeAction extends AbstractAction {

        CompileCodeAction() {
            putValue(NAME, "Compile");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if(editorHandler != null) {
                String code = codeEditor.getTextArea().getText();
                editorHandler.compileCode(currentLanguage, code);
                System.out.println(code);
            }
        }
    }

}
