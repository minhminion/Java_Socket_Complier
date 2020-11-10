package socket.client.GUI.components;

import socket.client.GUI.Editor;
import socket.commons.enums.Language;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.event.*;

public class AddTabDialog extends JDialog {
    private Editor editor;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JComboBox comboBox1;

    public AddTabDialog(Editor editor) {
        this.editor = editor;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        textField1.setText("New File");

        comboBox1.addItem(new ComboItem(Language.JAVA, SyntaxConstants.SYNTAX_STYLE_JAVA,  "Java", "JavaExample.txt"));
        comboBox1.addItem(new ComboItem(Language.CPP, SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS, "C++", "CppExample.txt"));
        comboBox1.addItem(new ComboItem(Language.PYTHON, SyntaxConstants.SYNTAX_STYLE_PYTHON, "Python", "PythonExample.txt"));
        comboBox1.addItem(new ComboItem(Language.CSHARP, SyntaxConstants.SYNTAX_STYLE_CSHARP, "C#", "CSharpExample.txt"));

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        ComboItem selectItem = (ComboItem) comboBox1.getSelectedItem();
        Language language = selectItem.getLanguage();
        String style = selectItem.getStyle();
        String res = selectItem.getRes();
        String name = textField1.getText();
        editor.addTab(name, language, style, res);
        closeDialog();
    }

    private void onCancel() {
        // add your code here if necessary
        editor.tabbedPane.setSelectedIndex(editor.getNumTabs() - 2);
        closeDialog();
    }

    private void closeDialog() {
        textField1.setText("New File");
        comboBox1.setSelectedIndex(0);
        dispose();
    }

    class ComboItem {
        private Language language;
        private String name;
        private String style;
        private String res;

        public ComboItem(Language language, String style, String name, String res) {
            this.language = language;
            this.name = name;
            this.style = style;
            this.res = res;
        }

        public String getStyle() { return this.style; }

        public Language getLanguage() {
            return this.language;
        }

        public String getRes() {
            return this.res;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
