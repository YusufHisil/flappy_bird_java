import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.abs;

/**
 * A class demonstrating a simple Flappy Bird copy using java swing.
 */
public class FlappyBird extends JPanel implements KeyListener {
    /**
     * variable used for the background image saved as an asset of the game
     */
    private Image backgroundImage;
    /**
     * variable used for the bird image saved as an asset of the game
     */
    private Image birdImage;
    /**
     * variables used for the bird's starting x coord
     */
    private int birdX;
    /**
     * variables used for the bird's starting y coord
     */
    private int birdY;
    /**
     * variable for speed at which the bird travels up
     */
    private int jumpVelocity;
    /**
     * variable for speed at which the bird travels down
     */
    private int gravityVelocity;
    /**
     * variable for the highest the bird can jump
     */
    private int maxElevation;
    /**
     * variable for checking whether the bird is in the middle of a jump
     */
    private boolean jumping;
    /**
     * timer used so that the jump goes smoothly over a short period of time
     */
    private Timer gravityTimer;
    /**
     * array where the obstacles sizes and coordinates are kept for drawing
     */
    private ArrayList<Rectangle> obstacles;
    /**
     * variable for the obstacle width
     */
    private int obstacleWidth;
    /**
     * variable for the gap through which the bird
     * has to pass
     */
    private int obstacleGap;
    /**
     * variable is the obstacle moving speed
     */
    private int obstacleSpeed;
    /**
     * variable for generating the obstacle height pseudorandomly
     */
    private Random random;
    /**
     * variable for keeping score
     */
    private int score;
    /**
     * state variable to check gamestate and show game over widnow accordingly
     */
    private boolean lost = false;
    /**
     * Constructs the Flappy Bird game.
     * Initializes all the private variables (info about the bird's and obstacle dimensions
     * and moving speed, as well as initilaizes the non-resizable window as a 720x1080
     * with a score panel at the top.
     */
    public FlappyBird() {
        addKeyListener(this);
        setFocusable(true);

        try {
            backgroundImage = ImageIO.read(new File("background.png"));
            birdImage = ImageIO.read(new File("bird.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        birdX = 400;
        birdY = 300;
        jumpVelocity = 10;
        gravityVelocity = 5;
        maxElevation = 90;
        jumping = false;

        // Gravity simulation using a timer
        gravityTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!jumping && birdY < getHeight() - 45) {
                    birdY += gravityVelocity;
                    repaint();
                }
            }
        });
        gravityTimer.start();

        obstacles = new ArrayList<>();
        obstacleWidth = 80;
        obstacleGap = 200;
        obstacleSpeed = 5;
        random = new Random();

        generateObstacles();

        JPanel scorePanel = new JPanel();
        JLabel scoreLabel = new JLabel("Score: " + score/2);
        scorePanel.add(scoreLabel);

        // Set layout and position for score panel
        setLayout(new BorderLayout());
        add(scorePanel, BorderLayout.NORTH);

    }

    /**
     * Generates 100 obstacles and adds them to the obstacles List.
     */
    private void generateObstacles() {
        int maxHeight = 720 - obstacleGap - 100;
        for (int i = 0; i < 5; i++) {

            int upperHeight = abs(random.nextInt(maxHeight)%maxHeight);

            obstacles.add(new Rectangle(1080 + (i+1) * 300, 0, obstacleWidth, upperHeight)); // Upper pipe
            System.out.println(obstacles.get(2*i).toString());
            obstacles.add(new Rectangle(1080 + (i+1) * 300, upperHeight + obstacleGap, obstacleWidth, 720 - obstacleGap - upperHeight));// Lower pipe
            System.out.println(obstacles.get(2*i+1).toString());
        }
    }

    /**
     * Checks for collisions between the bird and obstacles.
     * Also checks if the bird has fallen out of window bounds.
     * @return true if collision occurs, otherwise false.
     */
    private boolean checkCollisions() {
        Rectangle birdBounds = new Rectangle(birdX, birdY, 80, 45);

        // Check collision with obstacles
        for (int i = 0; i < obstacles.size(); i += 2) {
            Rectangle upper = obstacles.get(i);
            Rectangle lower = obstacles.get(i + 1);

            // Check if the bird intersects with any obstacle
            if (birdBounds.intersects(upper) || birdBounds.intersects(lower)) {
                return true;
            }
        }
        if(birdBounds.y > 634) gameOver();
        return false;
    }

    /**
     * Displays game over dialog when the game ends with the total score and the option to restart the game.
     */
    private void gameOver() {
        String message = "Game Over! Your Score: " + score/2;

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
                Window window = SwingUtilities.getWindowAncestor(restartButton);
                if (window != null) {
                    window.dispose();
                }
            }
        });

        Object[] options = {restartButton};

        JOptionPane.showOptionDialog(
                this,
                message,
                "Game Over",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                restartButton
        );
    }

    /**
     * Restarts the game when the restart button is clicked. Resets all values to the starting ones in the constructor.
     */
    private void restartGame() {
        obstacles.clear();
        birdX = 400;
        birdY = 300;
        jumpVelocity = 10;
        gravityVelocity = 5;
        maxElevation = 90;
        jumping = false;
        score = 0;
        lost = false;
        generateObstacles();
    }

    /**
     * Moves the obstacles in the game.
     */
    private void moveObstacles() {
        for (Rectangle rect : obstacles) {
            rect.x -= obstacleSpeed;
            if(rect.x == birdX) {
                score++;
                System.out.println(score);
            }

        }
        if(!obstacles.isEmpty()) if(obstacles.getLast().x <= 1080) generateObstacles();
    }

    /**
     * Paints the components in the game window.
     * @param g The Graphics object to paint.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background image
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Draw the bird image at the specified position and size
        g.drawImage(birdImage, birdX, Math.min(birdY, getHeight() - 45), 80, 45, this);

        // Draw obstacles
        g.setColor(Color.GREEN); // Set color for obstacles
        for (Rectangle rect : obstacles) {
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
    }

    /**
     * calls the jump() method when spacebar is pressed
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !jumping) {
            jumping = true;
            jump();
        }
    }

    /**
     * method for redrawing the score window whenever it is called.
     */
    private void increaseScore() {
        // Update the score label text
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                Component[] subComponents = ((JPanel) component).getComponents();
                for (Component subComponent : subComponents) {
                    if (subComponent instanceof JLabel) {
                        ((JLabel) subComponent).setText("Score: " + score/2);
                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     *  Changes the bird's coordinates according to the jump and gravity parameter.
     *  makes sure that the bird can jump again and again and that the bird doesn't get out of the upper bound of
     *  the window.
     */
    private void jump() {
        Timer timer = new Timer(10, new ActionListener() {
            int elevation = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (elevation < maxElevation) {
                    int newBirdY = birdY - jumpVelocity;

                    // Check if the new position breaches window boundaries
                    if (newBirdY >= 0 && newBirdY <= getHeight() - 45) {
                        birdY = newBirdY;
                    }

                    elevation += jumpVelocity;
                    repaint();
                } else {
                    ((Timer) e.getSource()).stop();
                    jumping = false;
                }
            }
        });
        timer.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    /**
     * Opens the game window and starts the game.
     * takes care of redrawing of score and obstacles
     */
    public void openWindow() {
        JFrame frame = new JFrame("Flappy Bird");
        frame.setSize(1080, 720);
        frame.setResizable(false);

        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        Timer gameTimer = new Timer(25, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(checkCollisions()) lost = true;
                if(lost)gameOver();
                increaseScore();
                moveObstacles();
                repaint();
            }
        });
        gameTimer.start();
    }

    /**
     * Main method to start the Flappy Bird game.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        FlappyBird backgroundExample = new FlappyBird();
        backgroundExample.openWindow();
    }
}
