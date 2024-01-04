import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

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

    boolean gameOver = false;

    SnakeGame(int width, int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(this.width, this.height));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();

        food = new Tile(10, 10);

        random = new Random();
        foodLocation();

        moveOnX = 0;
        moveOnY = 0;

        gaTimer = new Timer(100, this);
        gaTimer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        // grids on the board
        // x1,y1,x2,y2
        for (int i = 0; i < width / tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, height);
            g.drawLine(0, i * tileSize, width, i * tileSize);
            g.setColor(getBackground());
        }

        // creating food
        Color foodColor = new Color(0, 127, 0);
        g.setColor(foodColor);
        g.fillRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize);

        // snake
        g.setColor(Color.BLUE);
        g.fillRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize);

        // snake body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile restBody = snakeBody.get(i);
            g.fillRect(restBody.x * tileSize, restBody.y * tileSize, tileSize, tileSize);
        }

        g.setFont(new Font("Arial", Font.BOLD, 20));
        if (gameOver) {
            g.setColor(Color.RED);
            g.drawString("Game Over", width / 2 - 100, height / 2);
            g.drawString("Score: " + snakeBody.size(), width / 2 - 100, height / 2 + 50);
        } else {
            g.setColor(Color.WHITE);
            g.drawString("Score: " + snakeBody.size(), tileSize - 16, tileSize);
        }
    }

    public void foodLocation() {
        food.x = random.nextInt(width / tileSize);
        food.y = random.nextInt(height / tileSize);
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
        if (snakeHead.x * tileSize < 0 || snakeHead.x * tileSize > width || snakeHead.y * tileSize < 0
                || snakeHead.y * tileSize > height) {
            gameOver = true;
        }
    }

    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gaTimer.stop();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W && moveOnY != 1) {
            moveOnX = 0;
            moveOnY = -1;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S && moveOnY != -1) {
            moveOnX = 0;
            moveOnY = 1;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A && moveOnX != 1) {
            moveOnX = -1;
            moveOnY = 0;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D && moveOnX != -1) {
            moveOnX = 1;
            moveOnY = 0;
        }
    }

    // we don't need these two methods for this game but we can't delete or comment
    // them because we are implementing KeyListener
    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
}
