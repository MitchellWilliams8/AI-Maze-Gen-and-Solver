import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class MazeApp extends JFrame {
    private MazeVisualizer visualizer;

    public MazeApp() {
        setTitle("Pathfinding Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        visualizer = new MazeVisualizer();
        add(visualizer, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeApp().setVisible(true));
    }
}

class MazeVisualizer extends JPanel {
    private final int ROWS = 41;
    private final int COLS = 61;
    private final int CELL_SIZE = 18;

    private char[][] grid;
    private Timer timer;

    private Stack<Point> stack = new Stack<>();
    private Point currentGen;
    private boolean generating = true;

    public MazeVisualizer() {
        this.setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        this.setBackground(Color.BLACK);
        grid = new char[ROWS][COLS];

        for (int r = 0; r < ROWS; r++) Arrays.fill(grid[r], '#');
        currentGen = new Point(1, 1);
        grid[1][1] = '.';
        stack.push(currentGen);

        timer = new Timer(20, e -> {
            if (generating) stepGeneration();
            repaint();
        });
        timer.start();
    }

    private void stepGeneration() {
        int[][] directions = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
        List<int[]> unvisited = new ArrayList<>();

        for (int[] dir : directions) {
            int nx = currentGen.x + dir[0];
            int ny = currentGen.y + dir[1];
            if (nx > 0 && nx < COLS && ny > 0 && ny < ROWS && grid[ny][nx] == '#') {
                unvisited.add(dir);
            }
        }

        if (!unvisited.isEmpty()) {
            stack.push(currentGen);
            int[] dir = unvisited.get((int) (Math.random() * unvisited.size()));
            grid[currentGen.y + dir[1]/2][currentGen.x + dir[0]/2] = '.';
            currentGen = new Point(currentGen.x + dir[0], currentGen.y + dir[1]);
            grid[currentGen.y][currentGen.x] = '.';
        } else if (!stack.isEmpty()) {
            currentGen = stack.pop();
        } else {
            generating = false;
            currentGen = null;
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
        if (generating && currentGen != null) {
            g.setColor(Color.MAGENTA);
            g.fillRect(currentGen.x * CELL_SIZE, currentGen.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }
}