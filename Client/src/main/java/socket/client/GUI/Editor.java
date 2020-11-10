package socket.client.GUI;

import socket.client.BUS.EditorHandler;
import socket.client.GUI.components.AddTabDialog;
import socket.client.GUI.components.CodeEditor;
import socket.client.GUI.components.Console;
import socket.client.GUI.components.CustomTab;
import socket.commons.enums.Language;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;

public class Editor extends JFrame{
    private EditorHandler editorHandler;

    // GUI
    private int numTabs = 0;
    public JTabbedPane tabbedPane;

    private CodeEditor codeEditor;
    private Console console;
    private Language currentLanguage = Language.JAVA;
    private JMenu menuLanguage;

    private AddTabDialog addTabDialog;
    private JMenuBar menuBar;

    public Editor() {

        addTabDialog = new AddTabDialog(this);
        addTabDialog.setLocationRelativeTo(this);

        menuBar = createMenuBar();
        codeEditor = new CodeEditor(Language.JAVA);
        console = new Console();

        createTabbedPane();
        console.addText("Connecting to server ...");

        JSplitPane splitPane = new JSplitPane(SwingConstants.HORIZONTAL, tabbedPane,console);
        splitPane.setResizeWeight(0.7);

        getContentPane().add(splitPane);

        setJMenuBar(menuBar);
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

        menuItem =new JMenuItem("New File");
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
        addSyntaxItem(Language.CSHARP, "C#","CSharpExample.txt", SyntaxConstants.SYNTAX_STYLE_CSHARP, bg, menu);
        menu.getItem(0).setSelected(true);

        mb.add(menu);
        this.menuLanguage = menu;

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

    ChangeListener changeListener = new ChangeListener() {

        public void stateChanged(ChangeEvent changeEvent) {
            JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
            int index = sourceTabbedPane.getSelectedIndex();
            Component component = sourceTabbedPane.getComponentAt(index);
            if(component == null) {
//                addTab();
                openAddTabDialog();
            } else {
                codeEditor = (CodeEditor) component;
                setSelectMenuLanguage(codeEditor.getLanguage());
            }
        }
    };

    private void createTabbedPane () {
        this.tabbedPane = new JTabbedPane();
        JComponent panel1 = codeEditor;

        tabbedPane.add(panel1, "Java 1", numTabs++);
        tabbedPane.setTabComponentAt(0, new CustomTab(tabbedPane, this));

        tabbedPane.add(null, "+", numTabs++);

        tabbedPane.addChangeListener(changeListener);
    }

    private void openAddTabDialog () {

        // set size of dialog
        addTabDialog.setSize(400, 200);

        // set visibility of dialog
        addTabDialog.setVisible(true);
    }

    public void addTab (String name, Language language, String style, String res) {

        CodeEditor codeEditor = new CodeEditor(language);
        codeEditor.setSyntaxEditingStyle(style);
        int index = numTabs - 1;
        if (tabbedPane.getSelectedIndex() == index) { /* if click new tab */
            /* add new tab */
            tabbedPane.add(codeEditor, name, index);
            /* set tab is custom tab */
            tabbedPane.removeChangeListener(changeListener);
            tabbedPane.setSelectedIndex(index);
            tabbedPane.addChangeListener(changeListener);
            tabbedPane.setTabComponentAt(index, new CustomTab(this.tabbedPane, this));
            numTabs++;


            this.currentLanguage = language;
            setSelectMenuLanguage(language);
            codeEditor.setText(res);
            this.codeEditor = codeEditor;
        }
    }

    public void removeTab(int index) {
        numTabs--;

        if (index == numTabs - 1 && index > 0) {
            tabbedPane.setSelectedIndex(numTabs - 2);
        } else {
            tabbedPane.setSelectedIndex(index);
        }

        tabbedPane.remove(index);
    }

    public void setSelectMenuLanguage (Language language) {
        int index = -1;
        switch (language.toString()) {
            case "JAVA":
                index = 0;
                break;
            case "CPP":
                index = 1;
                break;
            case "PYTHON":
                index = 2;
                break;
            case "CSHARP":
                index = 3;
                break;
            default: break;
        }
        if (index != -1) {
            menuLanguage.getItem(index).setSelected(true);
        }
    }

    public void setHandler(EditorHandler editorHandler) {
        this.editorHandler = editorHandler;
        this.console.addText("Server Connected !!");
    }

    public int getNumTabs () {
        return numTabs;
    }

    public Console getConsole () {
        return console;
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
            codeEditor.setLanguage(language);
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
            }
        }
    }


}
