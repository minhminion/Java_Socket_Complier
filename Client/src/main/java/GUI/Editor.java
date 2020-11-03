package GUI;

import BUS.EditorHandler;
import GUI.components.CodeEditor;
import GUI.components.Console;
import GUI.components.CustomTab;
import com.complier.socket.commons.enums.Language;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Editor extends JFrame{
    private EditorHandler editorHandler;

    // GUI
    private int numTabs = 0;
    private JTabbedPane tabbedPane;

    private CodeEditor codeEditor;
    private Console console;
    private Language currentLanguage = Language.JAVA;
    private JMenu menuLanguage;

    private JDialog dialog;
    private JMenuBar menuBar;

    public Editor() {

        dialog = new JDialog(this,"Confirm");
        dialog.pack();

        menuBar = createMenuBar();
        codeEditor = new CodeEditor(Language.JAVA);
        console = new Console();

        createTabbedPane();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0,1));
        mainPanel.add(tabbedPane);
        mainPanel.add(console);
        console.addText("Connecting to server ...");

        getContentPane().add(mainPanel);

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
        JPanel p = new JPanel();

        // create a label
        JLabel l = new JLabel("this is first dialog box");
        p.add(l);

        // create input name
        JTextField nameTxt = new JTextField("Java_"+numTabs);
        p.add(nameTxt);

        // create checkbox
        ComboItem[] listItem = new ComboItem[] {
            new ComboItem(Language.JAVA, "Java", "JavaExample.txt"),
            new ComboItem(Language.CPP, "C++", "CppExample.txt"),
            new ComboItem(Language.PYTHON, "Python", "PythonExample.txt"),
            new ComboItem(Language.CSHARP, "C#", "CSharpExample.txt"),
        };

        JComboBox<ComboItem> c1 = new JComboBox<ComboItem>(listItem);
        p.add(c1);

        // create a button
        JButton yesBtn = new JButton("Yes");
        p.add(yesBtn);
        JButton closeBtn = new JButton("Close");
        p.add(closeBtn);

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String btn = e.getActionCommand();
                ComboItem selectItem = (ComboItem) c1.getSelectedItem();
                Language language = selectItem.getLanguage();
                String res = selectItem.getRes();
                String name = nameTxt.getText();
                switch (btn) {
                    case "Yes":
                        addTab(name, language, res);
                        break;
                    case "Close":

                        break;
                }
                dialog.dispose();
            }
        };

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tabbedPane.setSelectedIndex(numTabs - 2);
                dialog = new JDialog();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                tabbedPane.setSelectedIndex(numTabs - 2);
                dialog = new JDialog();
            }
        });

        // add Action Listener
        yesBtn.addActionListener(actionListener);
        closeBtn.addActionListener(actionListener);
        // add panel to dialog
        dialog.add(p);
        // set size of dialog
        dialog.setSize(200, 200);

        // set visibility of dialog
        dialog.setVisible(true);
    }

    public void addTab (String name, Language language, String res) {

        CodeEditor codeEditor = new CodeEditor(language);
        int index = numTabs - 1;
        if (tabbedPane.getSelectedIndex() == index) { /* if click new tab */
            /* add new tab */
            tabbedPane.add(codeEditor, name, index);
            /* set tab is custom tab */
            tabbedPane.setTabComponentAt(index, new CustomTab(this.tabbedPane, this));
            tabbedPane.removeChangeListener(changeListener);
            tabbedPane.setSelectedIndex(index);
            tabbedPane.addChangeListener(changeListener);
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

    public Console getConsole () {
        return console;
    }

    public void setText(String code) {
        codeEditor.setTextArea(code);
    }

    class ComboItem {
        private Language language;
        private String name;
        private String res;

        public ComboItem(Language language, String name, String res) {
            this.language = language;
            this.name = name;
            this.res = res;
        }

        public Language getLanguage () {
            return this.language;
        }

        public String getRes () {
            return this.res;
        }

        @Override
        public String toString() {
            return name;
        }
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
