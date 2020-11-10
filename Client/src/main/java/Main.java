import BUS.EditorHandler;
import GUI.Editor;
import GUI.components.Console;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;

public class Main {
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel( new FlatDarculaLaf() );
            } catch (Exception e) {
                e.printStackTrace(); // Never happens ;))
            }
            Editor editor = new Editor();
            editor.setVisible(true);
            EditorHandler editorHandler = new EditorHandler(editor);
            editorHandler.startConnection("0.0.0.0", 5000);
        });

    }
}
