package socket.client.GUI.components;

import socket.client.GUI.Editor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class CustomTab extends JPanel {

    JTabbedPane customJTabbedPane;
    Editor editor;

    /** JPanel contain a JLabel and a JButton to close */
    public CustomTab(JTabbedPane customJTabbedPane, Editor editor) {
        this.customJTabbedPane = customJTabbedPane;
        this.editor = editor;
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(new EmptyBorder(5, 2, 2, 2));
        setOpaque(false);
        addLabel();
        add(new CustomButton("x"));
    }

    private void addLabel() {
        JLabel label = new JLabel() {
            /** set text for JLabel, it will title of tab */
            public String getText() {
                int index = customJTabbedPane
                        .indexOfTabComponent(CustomTab.this);
                if (index != -1) {
                    return customJTabbedPane.getTitleAt(index);
                }
                return null;
            }
        };
        /** add more space between the label and the button */
        label.setBorder(new EmptyBorder(0, 0, 0, 10));
        add(label);
    }

    class CustomButton extends JButton implements MouseListener {
        private boolean mouseOver = false;
        private boolean mousePressed = false;
        Color textColor = Color.decode("#ffffff");
        Color hoverTextColor = Color.decode("#00aced");
        Color hoverBackgroundColor = Color.decode("#00aced");


        public CustomButton(String text) {
            int size = 20;
            setText(text);

            /** set size for button close */
            setPreferredSize(new Dimension(size, size));
            setForeground(textColor);
            setOpaque(true);

            /** set transparent */
            setContentAreaFilled(false);

            setToolTipText("close the Tab");

            /** set border for button */
            setBorder(null);
            /** don't show border */
            setBorderPainted(false);

            setFocusable(false);

            /** add event with mouse */
            addMouseListener(this);
        }


        /** when click button, tab will close */
        @Override
        public void mouseClicked(MouseEvent e) {
            int index = customJTabbedPane
                    .indexOfTabComponent(CustomTab.this);
            if (index != -1) {
                editor.removeTab(index);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        /** show border button when mouse hover */
        @Override
        public void mouseEntered(MouseEvent e) {
//            setContentAreaFilled(true);
             setForeground(Color.black);
        }

        /** hide border when mouse not hover */
        @Override
        public void mouseExited(MouseEvent e) {
            setForeground(textColor);
        }
    }
}
