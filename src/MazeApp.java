import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MazeApp extends JFrame {

    private MazeVisualizer visualizer;
    private JLabel statusLabel;
    private JSlider speedSlider;
    private JButton solveBtn;

    public MazeApp() {
        setTitle("Pathfinding Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        visualizer = new MazeVisualizer(this);
        add(visualizer, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBackground(new Color(50, 50, 50));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        controlPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setOpaque(false);

        JButton generateBtn = new JButton("Generate New Maze");
        styleButton(generateBtn);
        generateBtn.addActionListener(e -> visualizer.resetAndGenerate());

        solveBtn = new JButton("Solve (A*)");
        styleButton(solveBtn);
        solveBtn.setEnabled(false);
        solveBtn.addActionListener(e -> visualizer.startSolver());

        JLabel sliderLabel = new JLabel("Speed:");
        sliderLabel.setForeground(Color.WHITE);

        speedSlider = new JSlider(1, 60, 20);
        speedSlider.setOpaque(false);
        speedSlider.setInverted(true);
        speedSlider.addChangeListener(e -> visualizer.setDelay(speedSlider.getValue()));

        buttonsPanel.add(generateBtn);
        buttonsPanel.add(solveBtn);
        buttonsPanel.add(sliderLabel);
        buttonsPanel.add(speedSlider);

        controlPanel.add(buttonsPanel, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);

        visualizer.resetAndGenerate();
    }

    private void styleButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));

        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }

    public void updateStatus(String text) {
        statusLabel.setText(text);
    }

    public void enableSolveButton(boolean enable) {
        if (solveBtn != null) {
            solveBtn.setEnabled(enable);
            if(enable) {
                solveBtn.setBackground(new Color(70, 130, 180));
            } else {
                solveBtn.setBackground(new Color(100, 100, 100));
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MazeApp().setVisible(true));
    }
}

class MazeVisualizer extends JPanel {

    private final int ROWS = 41;
    private final int COLS = 61;
    private final int CELL_SIZE = 18;

    private char[][] grid;
    private Timer timer;
    private MazeApp parent;

    private Stack<Point> stack = new Stack<>();
    private Point currentGen;
    private boolean generating = false;

    private boolean solving = false;
    private boolean finished = false;
    private PriorityQueue<Node> openSet;
    private Set<Point> closedSet;
    private Map<Point, Node> nodeTracker;
    private List<Point> finalPath;
    private Point startPoint;
    private Point endPoint;

    private class Node {
        Point p;
        Node parent;
        int g, f;
        public Node(Point p, Node parent, int g, int h) {
            this.p = p;
            this.parent = parent;
            this.g = g;
            this.f = g + h;
        }
    }

    public MazeVisualizer(MazeApp parent) {
        this.parent = parent;
        this.setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        this.setBackground(Color.BLACK);

        grid = new char[ROWS][COLS];

        timer = new Timer(20, e -> {
            if (generating) stepGeneration();
            else if (solving) stepSolver();
            repaint();
        });
    }

    public void setDelay(int delay) {
        timer.setDelay(delay);
    }

    public void resetAndGenerate() {
        generating = true;
        solving = false;
        finished = false;
        parent.enableSolveButton(false);
        parent.updateStatus("Generating Maze (Recursive Backtracker)...");

        for (int r = 0; r < ROWS; r++) {
            Arrays.fill(grid[r], '#');
        }

        stack.clear();
        currentGen = new Point(1, 1);
        grid[1][1] = '.';
        stack.push(currentGen);

        timer.start();
    }

    public void startSolver() {
        if (generating) return;

        solving = true;
        finished = false;
        parent.updateStatus("Solving Maze (A* Search)...");

        startPoint = new Point(1, 1);
        endPoint = new Point(COLS - 2, ROWS - 2);

        if (grid[endPoint.y][endPoint.x] == '#') grid[endPoint.y][endPoint.x] = '.';

        openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        closedSet = new HashSet<>();
        nodeTracker = new HashMap<>();
        finalPath = new ArrayList<>();

        Node startNode = new Node(startPoint, null, 0, heuristic(startPoint, endPoint));
        openSet.add(startNode);
        nodeTracker.put(startPoint, startNode);

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
            parent.updateStatus("Maze Generated. Ready to Solve.");
            parent.enableSolveButton(true);
        }
    }

    private void stepSolver() {
        if (openSet.isEmpty()) {
            solving = false;
            parent.updateStatus("No Solution Found!");
            return;
        }

        Node current = openSet.poll();
        closedSet.add(current.p);

        if (current.p.equals(endPoint)) {
            solving = false;
            finished = true;
            reconstructPath(current);
            parent.updateStatus("Goal Reached! Path Cost: " + current.g);
            return;
        }

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] dir : dirs) {
            Point neighbor = new Point(current.p.x + dir[0], current.p.y + dir[1]);

            if (isValid(neighbor) && !closedSet.contains(neighbor)) {
                int newG = current.g + 1;
                if (!nodeTracker.containsKey(neighbor) || newG < nodeTracker.get(neighbor).g) {
                    Node n = new Node(neighbor, current, newG, heuristic(neighbor, endPoint));
                    if (nodeTracker.containsKey(neighbor)) openSet.remove(nodeTracker.get(neighbor));
                    openSet.add(n);
                    nodeTracker.put(neighbor, n);
                }
            }
        }

        if (closedSet.size() % 5 == 0) {
            parent.updateStatus("Searching... Nodes Visited: " + closedSet.size());
        }
    }

    private boolean isValid(Point p) {
        return p.x >= 0 && p.x < COLS && p.y >= 0 && p.y < ROWS && grid[p.y][p.x] != '#';
    }

    private int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private void reconstructPath(Node current) {
        while (current != null) {
            finalPath.add(current.p);
            current = current.parent;
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

        if (solving || finished || (!generating && closedSet != null && !closedSet.isEmpty())) {
            g.setColor(new Color(255, 100, 100, 150));
            for (Point p : closedSet) g.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

            g.setColor(new Color(100, 255, 100, 150));
            for (Node n : openSet) g.fillRect(n.p.x * CELL_SIZE, n.p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

            if (finished && !finalPath.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(4));
                g2.setColor(new Color(0, 120, 255));

                for (int i = 0; i < finalPath.size()-1; i++) {
                    Point p1 = finalPath.get(i);
                    Point p2 = finalPath.get(i+1);
                    g2.drawLine(
                            p1.x * CELL_SIZE + CELL_SIZE/2, p1.y * CELL_SIZE + CELL_SIZE/2,
                            p2.x * CELL_SIZE + CELL_SIZE/2, p2.y * CELL_SIZE + CELL_SIZE/2
                    );
                }
            }

            if (startPoint != null) {
                g.setColor(Color.ORANGE);
                g.fillOval(startPoint.x * CELL_SIZE + 2, startPoint.y * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
            }
            if (endPoint != null) {
                g.setColor(Color.RED);
                g.fillOval(endPoint.x * CELL_SIZE + 2, endPoint.y * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
            }
        }
    }
}