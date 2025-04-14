import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ArkanoidGame extends JPanel implements MouseMotionListener, ActionListener {

    private final int BALL_SIZE = 10;
    private final int PADDLE_WIDTH = 100;
    private final int PADDLE_HEIGHT = 20;
    private final int BLOCK_WIDTH = 60;
    private final int BLOCK_HEIGHT = 20;
    private final int ROWS = 3;
    private final int COLS = 5;

    private int ballX = 250;
    private int ballY = 300;
    private int ballVelX = 2;
    private int ballVelY = 2;

    private int paddleX = 200;
    private final int paddleY = 550;

    private boolean isGameOver = false;
    private boolean isVictory = false;

    private Rectangle[][] blocks;
    private Timer timer;

    private JButton restartButton;

    public ArkanoidGame() {
        setBackground(Color.BLACK);
        setLayout(null); // para usar botón con coordenadas
        addMouseMotionListener(this);
        setFocusable(true);
        initializeBlocks();

        timer = new Timer(5, this);
        timer.start();

        restartButton = new JButton("Reiniciar");
        restartButton.setBounds(190, 300, 120, 40);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> resetGame());
        this.add(restartButton);
    }

    private void initializeBlocks() {
        blocks = new Rectangle[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int x = j * (BLOCK_WIDTH + 10) + 30;
                int y = i * (BLOCK_HEIGHT + 5) + 30;
                blocks[i][j] = new Rectangle(x, y, BLOCK_WIDTH, BLOCK_HEIGHT);
            }
        }
    }

    private void resetGame() {
        ballX = 250;
        ballY = 300;
        ballVelX = 2;
        ballVelY = 2;
        paddleX = 200;
        isGameOver = false;
        isVictory = false;
        restartButton.setVisible(false);
        initializeBlocks();
        timer.start();
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        paddleX = e.getX() - PADDLE_WIDTH / 2;
        paddleX = Math.max(0, Math.min(paddleX, getWidth() - PADDLE_WIDTH));
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver || isVictory) return;

        ballX += ballVelX;
        ballY += ballVelY;

        // Rebote con paredes
        if (ballX <= 0 || ballX >= getWidth() - BALL_SIZE) ballVelX *= -1;
        if (ballY <= 0) ballVelY *= -1;

        // Rebote con la paleta
        Rectangle ballRect = new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE);
        Rectangle paddleRect = new Rectangle(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        if (ballRect.intersects(paddleRect)) {
            ballVelY = -Math.abs(ballVelY);
            ballY = paddleY - BALL_SIZE;
        }

        // Game Over si pasa la paleta
        if (ballY >= paddleY + PADDLE_HEIGHT) {
            isGameOver = true;
            timer.stop();
            restartButton.setVisible(true);
        }

        // Colisión con bloques
        outerLoop:
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Rectangle block = blocks[i][j];
                if (block != null && block.intersects(ballRect)) {
                    blocks[i][j] = null;
                    ballVelY *= -1;
                    break outerLoop;
                }
            }
        }

        // Verificar victoria
        if (checkVictory()) {
            isVictory = true;
            timer.stop();
            repaint();

            // Reiniciar automáticamente tras 3 segundos
            new Timer(3000, ev -> resetGame()).start();
        }

        repaint();
    }

    private boolean checkVictory() {
        for (Rectangle[] row : blocks) {
            for (Rectangle block : row) {
                if (block != null) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Bolita
        g.setColor(Color.RED);
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Paleta
        g.setColor(Color.BLUE);
        g.fillRect(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Bloques
        g.setColor(Color.GREEN);
        for (Rectangle[] row : blocks) {
            for (Rectangle block : row) {
                if (block != null) {
                    g.fillRect(block.x, block.y, BLOCK_WIDTH, BLOCK_HEIGHT);
                }
            }
        }

        // Mensaje de Game Over
        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String msg = "Game Over";
            int msgWidth = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2 - 30);
        }

        // Mensaje de Victoria
        if (isVictory) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String msg = "¡Ganaste!";
            int msgWidth = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2 - 30);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Arkanoid Game");
        ArkanoidGame game = new ArkanoidGame();
        frame.add(game);
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
