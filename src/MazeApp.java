import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;
import java.util.List;

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
    private final int ROWS = 41;
    private final int COLS = 61;
    private final int CELL_SIZE = 18;

    private char[][] grid;

    private Stack<Point> stack = new Stack<>();
    private Point currentGen;
    private boolean generating = true;

    private boolean solving = false;
    private boolean finished = false;
    private PriorityQueue<Node> openSet;
    private Set<Point> closedSet;
    private Map<Point, Node> nodeTracker;
    private List<Point> finalPath;
    private Point endPoint;

    private class Node {
        Point p;
        Node parent;
        int g, f;
        public Node(Point p, Node parent, int g, int h) {
            this.p = p; this.parent = parent; this.g = g; this.f = g + h;
        }
    }

    public MazeVisualizer() {
        this.setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        this.setBackground(Color.BLACK);
        grid = new char[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) Arrays.fill(grid[r], '#');

        currentGen = new Point(1, 1);
        grid[1][1] = '.';
        stack.push(currentGen);

        Timer timer = new Timer(20, e -> {
            if (generating) stepGeneration();
            else if (solving) stepSolver();
            repaint();
        });
        timer.start();
    }

    private void startSolver() {
        solving = true;
        Point startPoint = new Point(1, 1);
        endPoint = new Point(COLS - 2, ROWS - 2);
        if (grid[endPoint.y][endPoint.x] == '#') grid[endPoint.y][endPoint.x] = '.';

        openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        closedSet = new HashSet<>();
        nodeTracker = new HashMap<>();
        finalPath = new ArrayList<>();

        Node startNode = new Node(startPoint, null, 0, heuristic(startPoint, endPoint));
        openSet.add(startNode);
        nodeTracker.put(startPoint, startNode);
    }

    private void stepGeneration() {
        int[][] directions = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
        List<int[]> unvisited = new ArrayList<>();
        for (int[] dir : directions) {
            int nx = currentGen.x + dir[0], ny = currentGen.y + dir[1];
            if (nx > 0 && nx < COLS && ny > 0 && ny < ROWS && grid[ny][nx] == '#') unvisited.add(dir);
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
            startSolver();
        }
    }

    private void stepSolver() {
        if (openSet.isEmpty()) { solving = false; return; }
        Node current = openSet.poll();
        closedSet.add(current.p);

        if (current.p.equals(endPoint)) {
            solving = false; finished = true; reconstructPath(current); return;
        }

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] dir : dirs) {
            Point neighbor = new Point(current.p.x + dir[0], current.p.y + dir[1]);
            if (neighbor.x >= 0 && neighbor.x < COLS && neighbor.y >= 0 && neighbor.y < ROWS && grid[neighbor.y][neighbor.x] != '#' && !closedSet.contains(neighbor)) {
                int newG = current.g + 1;
                if (!nodeTracker.containsKey(neighbor) || newG < nodeTracker.get(neighbor).g) {
                    Node n = new Node(neighbor, current, newG, heuristic(neighbor, endPoint));
                    if (nodeTracker.containsKey(neighbor)) openSet.remove(nodeTracker.get(neighbor));
                    openSet.add(n);
                    nodeTracker.put(neighbor, n);
                }
            }
        }
    }

    private int heuristic(Point a, Point b) { return Math.abs(a.x - b.x) + Math.abs(a.y - b.y); }
    private void reconstructPath(Node current) { while(current != null) { finalPath.add(current.p); current = current.parent; } }

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
            g.setColor(Color.MAGENTA); g.fillRect(currentGen.x * CELL_SIZE, currentGen.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
        if (solving || finished) {
            g.setColor(new Color(255, 100, 100)); for (Point p : closedSet) g.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            g.setColor(Color.GREEN); for (Node n : openSet) g.fillRect(n.p.x * CELL_SIZE, n.p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            if (finished) { g.setColor(Color.BLUE); for (Point p : finalPath) g.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE); }
        }
    }
}