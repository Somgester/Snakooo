import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import javax.swing.border.EmptyBorder;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    private static final Path SCORE_FILE = Paths.get("db.score");

    private enum Difficulty {
        EASY(140),
        MEDIUM(100),
        HARD(70);

        final int delayMs;

        Difficulty(int delayMs) {
            this.delayMs = delayMs;
        }
    }

    private class Tile {
        int x;
        int y;

        public Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    int width;
    int height;
    int tileSize = 35;

    Tile snakeHead; // snake
    ArrayList<Tile> snakeBody; // snake body

    Tile food;
    Random random;

    // logic
    int moveOnX;
    int moveOnY;
    Timer gaTimer;
    Difficulty difficulty = Difficulty.MEDIUM;
    Consumer<Boolean> gameOverListener;
    BiConsumer<Integer, Integer> scoreListener;
    int highScore;
    JButton restartOverlayButton;
    boolean newHighScoreMade;
    boolean paused;

    boolean gameOver = false;

    SnakeGame(int width, int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(this.width, this.height));
        setBackground(new Color(12, 12, 12));
        setLayout(null);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();

        food = new Tile(10, 10);

        random = new Random();
        highScore = loadHighScore();
        foodLocation();

        moveOnX = 0;
        moveOnY = 0;
        newHighScoreMade = false;
        paused = false;

        restartOverlayButton = new JButton("Restart");
        restartOverlayButton.setBounds(width / 2 - 85, height / 2 + 98, 170, 40);
        restartOverlayButton.setVisible(false);
        restartOverlayButton.setFocusPainted(false);
        restartOverlayButton.setBorderPainted(false);
        restartOverlayButton.setOpaque(true);
        restartOverlayButton.setBackground(new Color(66, 166, 245));
        restartOverlayButton.setForeground(Color.WHITE);
        restartOverlayButton.setFont(new Font("Arial", Font.BOLD, 15));
        restartOverlayButton.setBorder(new EmptyBorder(6, 18, 6, 18));
        restartOverlayButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        restartOverlayButton.addActionListener(e -> {
            restartGame();
            requestFocusInWindow();
        });
        add(restartOverlayButton);

        gaTimer = new Timer(difficulty.delayMs, this);
        gaTimer.start();

        notifyScoreState();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // grids on the board
        // x1,y1,x2,y2
        g2d.setColor(new Color(28, 28, 28));
        for (int i = 0; i < width / tileSize; i++) {
            g2d.drawLine(i * tileSize, 0, i * tileSize, height);
            g2d.drawLine(0, i * tileSize, width, i * tileSize);
        }

        // creating food
        Color foodColor = new Color(48, 176, 68);
        g2d.setColor(foodColor);
        g2d.fillRoundRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, 10, 10);

        // snake
        g2d.setColor(new Color(66, 166, 245));
        g2d.fillRoundRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, 10, 10);

        // snake body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile restBody = snakeBody.get(i);
            g2d.setColor(new Color(42, 122, 196));
            g2d.fillRoundRect(restBody.x * tileSize, restBody.y * tileSize, tileSize, tileSize, 10, 10);
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        if (gameOver) {
            positionRestartButton();
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(0, 0, width, height);

            int centerX = width / 2;
            g2d.setColor(new Color(255, 82, 82));
            drawCenteredString(g2d, "Game Over", centerX, height / 2 - 10);

            g2d.setColor(Color.WHITE);
            drawCenteredString(g2d, "Score: " + snakeBody.size(), centerX, height / 2 + 25);
            drawCenteredString(g2d, "High Score: " + highScore, centerX, height / 2 + 50);

            if (newHighScoreMade) {
                g2d.setFont(new Font("Arial", Font.BOLD, 15));
                g2d.setColor(new Color(255, 215, 64));
                drawCenteredString(g2d, "Congrats! New high score unlocked, no cap.", centerX, height / 2 + 75);
            }
        } else {
            if (paused) {
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.setColor(new Color(255, 255, 255));
                drawCenteredString(g2d, "Paused", width / 2, height / 2 - 10);
                g2d.setFont(new Font("Arial", Font.PLAIN, 15));
                g2d.setColor(new Color(200, 200, 200));
                drawCenteredString(g2d, "Press P to resume", width / 2, height / 2 + 18);
                return;
            }

            if (moveOnX == 0 && moveOnY == 0) {
                g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawString("Use Arrow Keys or WASD to start", width / 2 - 125, height / 2);
            }
        }
    }

    public void foodLocation() {
        do {
            food.x = random.nextInt(width / tileSize);
            food.y = random.nextInt(height / tileSize);
        } while (collision(snakeHead, food) || isFoodOnBody());
    }

    private boolean isFoodOnBody() {
        for (int i = 0; i < snakeBody.size(); i++) {
            if (collision(snakeBody.get(i), food)) {
                return true;
            }
        }
        return false;
    }

    public boolean collision(Tile t1, Tile t2) {
        if (t1.x == t2.x && t1.y == t2.y) {
            return true;
        }
        return false;
    }

    public void move() {
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            foodLocation();
        }
        // moving body
        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile restBody = snakeBody.get(i);
            if (i == 0) {
                restBody.x = snakeHead.x;
                restBody.y = snakeHead.y;
            } else {
                Tile prev = snakeBody.get(i - 1);
                restBody.x = prev.x;
                restBody.y = prev.y;
            }
        }
        // head
        snakeHead.x += moveOnX;
        snakeHead.y += moveOnY;

        // game over
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile restBody = snakeBody.get(i);
            if (collision(snakeHead, restBody)) {
                gameOver = true;
            }
        }
        if (snakeHead.x < 0 || snakeHead.x >= width / tileSize || snakeHead.y < 0 || snakeHead.y >= height / tileSize) {
            gameOver = true;
        }
    }

    public void actionPerformed(ActionEvent e) {
        boolean wasGameOver = gameOver;
        move();
        restartOverlayButton.setVisible(gameOver);
        if (!wasGameOver && gameOver) {
            updateHighScore();
            notifyGameOverState();
        }
        notifyScoreState();
        repaint();
        if (gameOver) {
            gaTimer.stop();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P && !gameOver) {
            togglePause();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER && gameOver) {
            restartGame();
            requestFocusInWindow();
            return;
        }

        if (paused || gameOver) {
            return;
        }

        if ((e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) && moveOnY != 1) {
            moveOnX = 0;
            moveOnY = -1;
        }
        if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) && moveOnY != -1) {
            moveOnX = 0;
            moveOnY = 1;
        }
        if ((e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) && moveOnX != 1) {
            moveOnX = -1;
            moveOnY = 0;
        }
        if ((e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) && moveOnX != -1) {
            moveOnX = 1;
            moveOnY = 0;
        }
    }

    public void setDifficulty(String level) {
        if (level == null) {
            return;
        }

        if (level.equalsIgnoreCase("easy")) {
            difficulty = Difficulty.EASY;
        } else if (level.equalsIgnoreCase("hard")) {
            difficulty = Difficulty.HARD;
        } else {
            difficulty = Difficulty.MEDIUM;
        }

        gaTimer.setDelay(difficulty.delayMs);
        gaTimer.setInitialDelay(difficulty.delayMs);
    }

    public void restartGame() {
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        moveOnX = 0;
        moveOnY = 0;
        gameOver = false;
        paused = false;
        newHighScoreMade = false;
        restartOverlayButton.setVisible(false);
        foodLocation();
        gaTimer.restart();
        notifyGameOverState();
        notifyScoreState();
        repaint();
    }

    public void setScoreListener(BiConsumer<Integer, Integer> listener) {
        this.scoreListener = listener;
        notifyScoreState();
    }

    public void setGameOverListener(Consumer<Boolean> listener) {
        this.gameOverListener = listener;
        notifyGameOverState();
    }

    private void notifyGameOverState() {
        if (gameOverListener != null) {
            gameOverListener.accept(gameOver);
        }
    }

    private void notifyScoreState() {
        if (scoreListener != null) {
            scoreListener.accept(snakeBody.size(), highScore);
        }
    }

    private void togglePause() {
        paused = !paused;
        if (paused) {
            gaTimer.stop();
        } else {
            gaTimer.start();
        }
        repaint();
    }

    private void drawCenteredString(Graphics2D g2d, String text, int centerX, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        int x = centerX - metrics.stringWidth(text) / 2;
        g2d.drawString(text, x, y);
    }

    private void positionRestartButton() {
        int buttonWidth = 170;
        int buttonHeight = 40;
        int buttonX = width / 2 - buttonWidth / 2;
        int buttonY = height / 2 + 98;
        restartOverlayButton.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
    }

    private int loadHighScore() {
        try {
            if (!Files.exists(SCORE_FILE)) {
                Files.write(SCORE_FILE, "0\n".getBytes(StandardCharsets.UTF_8));
                return 0;
            }

            String content = new String(Files.readAllBytes(SCORE_FILE), StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return 0;
            }

            return Math.max(0, Integer.parseInt(content));
        } catch (Exception ex) {
            return 0;
        }
    }

    private void updateHighScore() {
        int score = snakeBody.size();
        newHighScoreMade = false;
        if (score > highScore) {
            highScore = score;
            newHighScoreMade = true;
            try {
                Files.write(SCORE_FILE, (highScore + "\n").getBytes(StandardCharsets.UTF_8));
            } catch (Exception ex) {
            }
        }
    }

    // we don't need these two methods for this game but we can't delete or comment
    // them because we are implementing KeyListener
    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
}
