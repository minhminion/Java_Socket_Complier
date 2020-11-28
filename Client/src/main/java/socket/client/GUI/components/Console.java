package socket.client.GUI.components;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Console extends JPanel {
    private JTextArea textArea;
    private JLabel myLabel;
    public Console() {
        //this.setLayout(new GridLayout(1, 2));
        this.setBackground(Color.BLACK);
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);

        this.setLayout(flowLayout);


        textArea = new JTextArea();
//        textArea.setRows(24);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.LIGHT_GRAY);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setEditable(false);
        //textArea.append("\n");
//        System.setOut(new PrintStream(new OutputStream() {
//            @Override
//            public void write(int b) throws IOException {
//                textArea.append(String.valueOf((char) b));
//            }
//        }));

        ImageIcon imageIcon = new ImageIcon((getClass().getResource("/Images/2.gif")));
        myLabel = new JLabel(imageIcon);
        add(myLabel);
        myLabel.setVisible(false);

        add(textArea);
    }

    public void addText (String message) {
        textArea.append(message+"\n");
    }
    public void addTextSuccess () {
        JLabel label = new JLabel("\n"+"Compile Success");
        label.setForeground(Color.GREEN);
        addText(String.valueOf(label));
    }

    public void clearScreen () {
        textArea.setText("");
    }
    public void showLoading(){
        myLabel.setVisible(true);
    }
    public void disShowLoading(){
        myLabel.setVisible(false);
    }
}
