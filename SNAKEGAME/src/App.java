import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

public class App {
        public static void main(String[] args) throws Exception {
            int boardWidth = 600;
            int boardHeight = boardWidth;

            JFrame jf = new JFrame("Snake Game");
            jf.setSize(boardWidth, boardHeight);
            jf.setLocationRelativeTo(null);
            jf.setResizable(false);
            jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jf.setVisible(true);

            ImageIcon gameIcon = new ImageIcon(ClassLoader.getSystemResource("images/gameIcon.jpg"));
            Image icon = gameIcon.getImage();
            jf.setIconImage(icon);

            SnakeGame sg = new SnakeGame(600, 600);
            jf.add(sg);
            jf.pack();
            sg.requestFocus();
        }
    }
