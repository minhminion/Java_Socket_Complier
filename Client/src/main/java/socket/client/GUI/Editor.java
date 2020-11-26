package socket.client.GUI;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import socket.client.BUS.EditorHandler;
import socket.client.GUI.components.*;
import socket.commons.enums.Language;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import socket.commons.helpers.CommonHelpers;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class Editor extends JFrame{
    private EditorHandler editorHandler;
    private static String directoryPath = System.getProperty("user.dir")+"/Client/src/main/resources/";
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
        codeEditor = new CodeEditor(Language.JAVA, new CompileCodeAction(), new FormatCodeAction());
        console = new Console();

        createTabbedPane();
        console.addText("Connecting to server ...");

        JSplitPane splitPane = new JSplitPane(SwingConstants.HORIZONTAL, tabbedPane,console);
        splitPane.setResizeWeight(0.7);

        getContentPane().add(splitPane);

        setJMenuBar(menuBar);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        codeEditor.setText(directoryPath+"JavaExample.txt");
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
        JMenu menu = new JMenu("File");
        JMenuItem menuItem;
        KeyStroke keyStroke;

        menuItem = new JMenuItem(new OpenFileAction());
        keyStroke = KeyStroke.getKeyStroke("control O");
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);

        menuItem = new JMenuItem(new NewFileAction());
        keyStroke = KeyStroke.getKeyStroke("control N");
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);

        menuItem = new JMenuItem(new SaveFileAction());
        keyStroke = KeyStroke.getKeyStroke("control S");
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);

        mb.add(menu);

        // Menu Language
        menu = new JMenu("Language");
        ButtonGroup bg = new ButtonGroup();
        addSyntaxItem(Language.JAVA, "Java",directoryPath+"JavaExample.txt", SyntaxConstants.SYNTAX_STYLE_JAVA, bg, menu);
        addSyntaxItem(Language.CPP, "C++",directoryPath+"CppExample.txt", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS, bg, menu);
        addSyntaxItem(Language.PYTHON, "Python",directoryPath+"PythonExample.txt", SyntaxConstants.SYNTAX_STYLE_PYTHON, bg, menu);
        addSyntaxItem(Language.CSHARP, "C#",directoryPath+"CSharpExample.txt", SyntaxConstants.SYNTAX_STYLE_CSHARP, bg, menu);
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
        menuItem = new JMenuItem(new CompileCodeAction());
        keyStroke = KeyStroke.getKeyStroke("shift F10");
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);

        menuItem = new JMenuItem(new FormatCodeAction());
        keyStroke = KeyStroke.getKeyStroke("shift F9");
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

        tabbedPane.add(panel1, "Java 1.java", numTabs++);
        codeEditor.setNameFile("Java 1.java");
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

        CodeEditor codeEditor = new CodeEditor(language, new CompileCodeAction(), new FormatCodeAction());
        codeEditor.setSyntaxEditingStyle(style);
        int index = numTabs - 1;
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
        codeEditor.setNameFile(name);
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

    public class CompileCodeAction extends AbstractAction {

        CompileCodeAction() {
            putValue(NAME, "Compile");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if(editorHandler != null) {
                String code = codeEditor.getTextArea().getText();
                console.clearScreen();
                console.addText("Compiling........");
                editorHandler.compileCode(currentLanguage, code);
            }
        }
    }
    public class FormatCodeAction extends AbstractAction {

        FormatCodeAction() {
            putValue(NAME, "Format Code");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if(editorHandler != null) {
                String code = codeEditor.getTextArea().getText();
                console.clearScreen();
                console.addText("Formating........");
//                editorHandler.compileCode(currentLanguage, code);
                editorHandler.formatCode(currentLanguage, code);
            }
        }
    }

    public class OpenFileAction extends AbstractAction {

        public OpenFileAction() {
            putValue(NAME, "Open File");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JFrame parentFrame = new JFrame();

            FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
                    "Choose a programming language file",
                    "cs",
                    "java",
                    "py",
                    "c",
                    "cpp");

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose a file to open");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(fileFilter);

            int userSelection = fileChooser.showSaveDialog(parentFrame);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                String newFileAbsolutePath = fileToSave.getAbsolutePath();
                Language newFileLanguage = CommonHelpers.getLanguageFromFilePath(newFileAbsolutePath);
                String newFileSyntaxStyle = getLanguageSyntaxStyle(newFileLanguage);

                /** Create new tab for new file */
                addTab(
                        fileToSave.getName(),
                        newFileLanguage,
                        newFileSyntaxStyle,
                        newFileAbsolutePath);
            }
        }
    }

    private class NewFileAction extends AbstractAction {

        NewFileAction() {
            putValue(NAME, "New File");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            openAddTabDialog();
        }
    }
    private class SaveFileAction extends AbstractAction {

        SaveFileAction() {
            putValue(NAME, "Save File");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
//            fileChooser.setSelectedFile(new File("fileToSave.txt"));
            fileChooser.setSelectedFile(new File(codeEditor.getNameFile()));

            fileChooser.setFileFilter(new FileTypeFilter(".java", "Java"));
            fileChooser.setFileFilter(new FileTypeFilter(".cpp", "C++"));
            fileChooser.setFileFilter(new FileTypeFilter(".py", "Python"));
            fileChooser.setFileFilter(new FileTypeFilter(".cs", "C#"));
            fileChooser.setFileFilter(new FileTypeFilter(".txt", "Text File"));

            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {

                File fi = fileChooser.getSelectedFile();
                try {
                    String code = codeEditor.getTextArea().getText();

                    FileWriter fw = new FileWriter(fi.getPath());
                    fw.write(code);
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                }

            }
        }
    }

    public String getLanguageSyntaxStyle(Language language)
    {
        String style = null;

        switch (language)
        {
            case JAVA: {
                style = SyntaxConstants.SYNTAX_STYLE_JAVA;
                break;
            }

            case CPP: {
                style = SyntaxConstants.SYNTAX_STYLE_C;
                break;
            }
            case PYTHON: {
                style = SyntaxConstants.SYNTAX_STYLE_PYTHON;
                break;
            }
            case CSHARP: {
                style = SyntaxConstants.SYNTAX_STYLE_CSHARP;
                break;
            }

            default: break;
        }

        return style;
    }
}
