package socket.client.GUI.components;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Console extends JPanel {
    private JTextArea textArea;
    public Console() {
        this.setLayout(new GridLayout(0, 1));
        textArea = new JTextArea();
        textArea.setRows(24);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.LIGHT_GRAY);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
//        System.setOut(new PrintStream(new OutputStream() {
//            @Override
//            public void write(int b) throws IOException {
//                textArea.append(String.valueOf((char) b));
//            }
//        }));
        textArea.setEditable(false);
        add(textArea);
    }

    public void addText (String message) {
        textArea.append(message+"\n");
    }
}
