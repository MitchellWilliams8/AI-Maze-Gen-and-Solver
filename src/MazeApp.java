import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class MazeApp extends JFrame {

    public MazeApp() {
        setTitle("Pathfinding Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        MazeVisualizer visualizer = new MazeVisualizer();
        add(visualizer, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeApp().setVisible(true));
    }
}

class MazeVisualizer extends JPanel {
    private final int ROWS = 40;
    private final int COLS = 60;
    private final int CELL_SIZE = 20;

    private char[][] grid;

    public MazeVisualizer() {
        this.setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        this.setBackground(Color.BLACK);

        grid = new char[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            Arrays.fill(grid[r], '#');
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                g.setColor(grid[r][c] == '#' ? new Color(30,30,30) : Color.WHITE);
                g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(new Color(40,40,40));
                g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }
}