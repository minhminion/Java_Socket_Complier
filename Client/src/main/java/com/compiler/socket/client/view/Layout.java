package com.compiler.socket.client.view;

import com.compiler.socket.client.view.component.CodeEditor;
import com.complier.socket.commons.enums.Action;
import com.complier.socket.commons.enums.Language;
import com.complier.socket.commons.request.CompileRequest;
import com.complier.socket.commons.request.Request;
import com.complier.socket.commons.response.CompileResponse;
import com.complier.socket.commons.response.MessageResponse;
import com.formdev.flatlaf.FlatIntelliJLaf;
import org.apache.commons.lang3.ObjectUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Layout extends JFrame{
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // GUI
    private CodeEditor codeEditor;
    private Language currentLanguage = Language.JAVA;

    public Layout () {
        codeEditor = new CodeEditor();
        setContentPane(codeEditor);
        setJMenuBar(createMenuBar());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        codeEditor.setText("JavaExample.txt");
        pack();
        setTitle("Banana Boys Compiler");
    }

    public void start(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            new ResponseProcess().start();
            setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest (Request req) throws IOException {
        this.out.writeObject(req);
        this.out.flush();
    }

    private class ResponseProcess extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    Object object = in.readObject();
                    if (ObjectUtils.isEmpty(object)) {
                        continue;
                    }

                    if (object instanceof MessageResponse) {
                        MessageResponse messageResponse = (MessageResponse) object;
                        System.out.println("Message from server " + messageResponse.getMessage());
                    } else if (object instanceof CompileResponse) {
                        CompileResponse compileResponse = (CompileResponse) object;
                        System.out.println( "After Formatter \n" +
                                "=============== \n"+
                                compileResponse.getCode()+"\n"+
                                "===== [OUTPUT] =====\n"+
                                compileResponse.getOutput()
                        );
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
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
            putValue(NAME, "Run");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            // TODO:
            // Send Request over here
            String value = codeEditor.getTextArea().getText();
            try {
                sendRequest(CompileRequest
                        .builder()
                        .action(Action.COMPILE_CODE)
                        .code(value)
                        .language(currentLanguage)
                        .build());
                System.out.println(value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main (String arg[]) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel( new FlatIntelliJLaf() );
            } catch (Exception e) {
                e.printStackTrace(); // Never happens ;))
            }
            Layout client = new Layout();
            client.start("0.0.0.0", 5000);
        });
    }
}
