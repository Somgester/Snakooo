import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class App {
        public static void main(String[] args) throws Exception {
            int boardWidth = 600;
            int boardHeight = boardWidth;

            JFrame jf = new JFrame("Snake Game");
            jf.setLayout(new BorderLayout());
            jf.setResizable(false);
            jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ImageIcon gameIcon = new ImageIcon(ClassLoader.getSystemResource("images/gameIcon.jpg"));
            Image icon = gameIcon.getImage();
            jf.setIconImage(icon);

            SnakeGame sg = new SnakeGame(boardWidth, boardHeight);

            JPanel controls = new JPanel(new BorderLayout());
            controls.setBackground(new Color(20, 20, 20));
            controls.setBorder(new EmptyBorder(8, 12, 8, 12));

            JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            leftControls.setOpaque(false);

            JPanel rightControls = new JPanel(new GridLayout(2, 1, 0, 2));
            rightControls.setOpaque(false);

            JLabel difficultyLabel = new JLabel("Difficulty:");
            difficultyLabel.setForeground(new Color(225, 225, 225));
            difficultyLabel.setFont(new Font("Arial", Font.BOLD, 15));

            JLabel scoreLabel = new JLabel("Score: 0", SwingConstants.RIGHT);
            scoreLabel.setForeground(new Color(225, 225, 225));
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 15));

            JLabel highLabel = new JLabel("High: 0", SwingConstants.RIGHT);
            highLabel.setForeground(new Color(225, 225, 225));
            highLabel.setFont(new Font("Arial", Font.BOLD, 15));

            String[] levels = { "Easy", "Medium", "Hard" };
            JComboBox<String> difficultyPicker = new JComboBox<>(levels);
            difficultyPicker.setSelectedItem("Medium");
            difficultyPicker.setFont(new Font("Arial", Font.BOLD, 14));
            difficultyPicker.setMaximumRowCount(3);
            difficultyPicker.setFocusable(false);
            difficultyPicker.setPreferredSize(new Dimension(118, 30));
            difficultyPicker.setBackground(new Color(24, 24, 24));
            difficultyPicker.setForeground(new Color(230, 230, 230));

            difficultyPicker.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel item = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                            cellHasFocus);
                    item.setFont(new Font("Arial", Font.BOLD, 14));
                    if (isSelected) {
                        item.setBackground(new Color(52, 104, 170));
                        item.setForeground(Color.WHITE);
                    } else {
                        item.setBackground(new Color(24, 24, 24));
                        item.setForeground(new Color(225, 225, 225));
                    }
                    return item;
                }
            });

            difficultyPicker.addActionListener(e -> {
                sg.setDifficulty((String) difficultyPicker.getSelectedItem());
                sg.requestFocusInWindow();
            });

            sg.setScoreListener((score, high) -> {
                scoreLabel.setText("Score: " + score);
                highLabel.setText("High: " + high);
            });

            leftControls.add(difficultyLabel);
            leftControls.add(difficultyPicker);

            rightControls.add(scoreLabel);
            rightControls.add(highLabel);

            controls.add(leftControls, BorderLayout.WEST);
            controls.add(rightControls, BorderLayout.EAST);

            jf.add(controls, BorderLayout.NORTH);
            jf.add(sg, BorderLayout.CENTER);
            jf.pack();
            jf.setLocationRelativeTo(null);
            jf.setVisible(true);
            sg.requestFocusInWindow();
        }
    }
