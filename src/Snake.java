import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

public class Snake extends JPanel implements KeyListener {
    int WIDTH, HEIGHT, ROWS, COLS;
    float gridWidth, gridHeight, speed;
    JFrame frame;
    BufferedImage gridImage = null;
    Grid food;
    ArrayList<Grid> snakeBody;
    Direction direction;
    boolean drawGrid, gameOver;
    int score;

    public Snake(int width, int height, int rows, int cols) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.ROWS = rows;
        this.COLS = cols;
        this.gridWidth = width / (float) rows;
        this.gridHeight = height / (float) cols;
        this.speed = 100f;
        drawGrid = false;
        gameOver = false;
        score = 0;

        this.snakeBody = new ArrayList<>();
        this.snakeBody.add(new Grid(5, 5, Direction.UP, this));
        this.snakeBody.add(new Grid(5, 6, Direction.UP, this));
        this.snakeBody.add(new Grid(5, 7, Direction.UP, this));

        food = randomFoodGrid();

        this.direction = Direction.UP;

        this.frame = new JFrame("Snake in Java");
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.frame.setResizable(false);
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.addKeyListener(this);

        this.frame.add(this);
        this.frame.setVisible(true);
    }

    private enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT,
        FOOD
    }

    private class Grid {
        float x, y, width, height;
        float centerX, centerY;
        Direction direction;
        Snake snake;

        public Grid(float x, float y, Direction direction, Snake snake) {
            this.x = x;
            this.y = y;
            this.width = snake.gridWidth;
            this.height = snake.gridHeight;

            this.centerX = x + width / 2;
            this.centerY = y + height / 2;
            this.snake = snake;
            this.direction = direction;
        }

        public float getX() {
            return this.x * snake.gridWidth;
        }

        public float getY() {
            return y * snake.gridHeight;
        }

        public void setWidth(float width) {
            this.width = width;
            this.centerX = getX() + gridWidth / 2 - width / 2;
        }

        public void setHeight(float height) {
            this.height = height;
            this.centerY = getY() + gridHeight / 2 - height / 2;
        }

        public float getDrawX() {
            return centerX;
        }

        public float getDrawY() {
            return centerY;
        }
    }

    private Grid randomFoodGrid() {
        boolean alreadyExists;
        int randomX, randomY;
        do {
            alreadyExists = false;
            randomX = (int) (Math.random() * ROWS);
            randomY = (int) (Math.random() * COLS);
            System.out.println("Random: " + randomX + "," + randomY);
            for (Grid grid : snakeBody) {
                if (grid.x == randomX && grid.y == randomY) {
                    alreadyExists = true;
                    break;
                }
            }
        } while (alreadyExists);
        Grid newGrid = new Grid(randomX, randomY, Direction.FOOD, this);
        newGrid.setWidth(gridWidth * 0.6f);
        newGrid.setHeight(gridHeight * 0.6f);
        return newGrid;
    }

    private void moveSnakeGrid(Grid grid) {
        switch (grid.direction) {
            case UP:
                grid.y--;
                break;
            case DOWN:
                grid.y++;
                break;
            case RIGHT:
                grid.x++;
                break;
            case LEFT:
                grid.x--;
                break;
        }
    }

    private void isGameOver() {
        if (gameOver) {
            this.snakeBody = new ArrayList<>();
            this.snakeBody.add(new Grid(5, 5, Direction.UP, this));
            this.snakeBody.add(new Grid(5, 6, Direction.UP, this));
            this.snakeBody.add(new Grid(5, 7, Direction.UP, this));
            this.direction = Direction.UP;
            score = 0;

            try {
                TimeUnit.SECONDS.sleep(1);

                food = randomFoodGrid();
                gameOver = false;
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    private void growSnake() {
        Grid lastGrid = snakeBody.get(snakeBody.size() - 1);
        Grid grid = new Grid(lastGrid.x, lastGrid.y, lastGrid.direction, this);

        snakeBody.add(grid);
    }

    public void updateSnake() {
        ArrayList<Grid> temp = new ArrayList<>();
        boolean toGrowSnake = false;

        for (Grid grid : snakeBody) {
            temp.add(new Grid(grid.x, grid.y, grid.direction, this));
        }

        snakeBody.get(0).direction = this.direction;
        moveSnakeGrid(snakeBody.get(0));
        if (snakeBody.get(0).x == food.x && snakeBody.get(0).y == food.y) {
            score++;
            food = randomFoodGrid();
            toGrowSnake = true;
        }

        if (snakeBody.get(0).getX() > WIDTH || snakeBody.get(0).getY() > HEIGHT ||
                snakeBody.get(0).getX() < 0 || snakeBody.get(0).getY() < 0) {
            System.out.println("Game over, off screen");
            gameOver = true;
        }

        for (int i = 1; i < snakeBody.size(); i++) {
            if(toGrowSnake && i == snakeBody.size() - 1) {
                snakeBody.get(i).direction = temp.get(i - 1).direction;
                growSnake();
                moveSnakeGrid(snakeBody.get(i));
                return;
            }
            snakeBody.get(i).direction = temp.get(i - 1).direction;
            moveSnakeGrid(snakeBody.get(i));
            if (snakeBody.get(0).x == snakeBody.get(i).x && snakeBody.get(0).y == snakeBody.get(i).y) {
                System.out.println("Game over!");
                gameOver = true;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("TimesRoman", Font.PLAIN, HEIGHT / 15));
        g.drawString(String.valueOf(score), WIDTH / 3 * 2, HEIGHT / 15);

        if (drawGrid) {
            if (gridImage == null) {
                gridImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Graphics2D temp = (Graphics2D) gridImage.getGraphics();
                temp.setColor(Color.WHITE);

                for (int i = 0; i < this.ROWS; i++) {
                    for (int j = 0; j < this.COLS; j++) {
                        Rectangle2D rect = new Rectangle2D.Float(i * this.gridWidth, j * this.gridHeight, this.gridWidth, this.gridHeight);
                        temp.draw(rect);
                    }
                }
            } else {
                g.drawImage(gridImage, 0, 0, this.frame);
            }
        }

        if(gameOver) {
            //g.drawString("Game over!", WIDTH / 2, HEIGHT / 15);
            isGameOver();
        }

        Graphics2D gg = (Graphics2D) g;
        Rectangle2D rect;

        if (food != null) {
            rect = new Rectangle2D.Float(food.getDrawX(), food.getDrawY(), food.width, food.height);
            gg.setColor(Color.YELLOW);
            gg.fill(rect);
        }

        gg.setColor(Color.WHITE);
        for (Grid grid : snakeBody) {
            rect = new Rectangle2D.Float(grid.getX(), grid.getY(), gridWidth, gridHeight);
            gg.fill(rect);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(!gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                this.direction = Direction.UP;
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                this.direction = Direction.DOWN;
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                this.direction = Direction.RIGHT;
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                this.direction = Direction.LEFT;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    private void updateGame() {
        if(!gameOver) {
            updateSnake();
            frame.repaint();
        }
    }

    public static void main(String[] args) {
        Snake snake = new Snake(800, 800, 20, 20);
        snake.drawGrid = false;
        long prevTime = System.currentTimeMillis();
        long deltaT = 0;

        while (true) {
            deltaT = System.currentTimeMillis() - prevTime;
            if (deltaT >= snake.speed) {
                snake.updateGame();
                prevTime += deltaT;
            }
        }
    }

}
