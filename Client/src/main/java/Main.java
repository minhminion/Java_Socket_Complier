import socket.client.BUS.EditorHandler;
import socket.client.GUI.Editor;
import com.formdev.flatlaf.FlatDarculaLaf;

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
