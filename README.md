# AI-Maze-Gen-and-Solver

> A real time Java visualization tool that demonstrates the relationship between procedural content generation and heuristic graph traversal.

## Overview

This application serves as an interactive view of algorithm efficiency and data visualization. It procedurally generates mazes using a randomized depth first strategy and solves them using the A* search algorithm.

## Key Features

* **Procedural Generation:** Implements the **Recursive Backtracker** algorithm to create complex, mazes that are guaranteed to be solvable.
* **Pathfinding Engine:** Visualizes the **A* algorithm in real time, color-coding the *Open Set* (green), *Closed Set* (red), and *Optimal Path* (blue).
* **Control Dashboard:** An interactive GUI allowing users to:
    * Trigger generation and solving phases independently.
    * Adjust execution speed (1ms to 60ms delay) via a slider.
    * View real time metrics (visited nodes, path cost).
* **Rendering:** Uses Java Swing's double buffering to prevent flickering during high speed rendering steps.

## Technical Implementation

### Maze Generation
The maze is generated using a **Recursive Backtracker** algorithm implemented with a Stack<Point>.
* **Logic:** It treats the grid as a graph where nodes are valid coordinates. It moves two steps at a time to preserve walls, carving a path by removing the wall between the current node and the neighbor.

### The Solver
The solver utilizes **A* Search**, a best first search algorithm.
* **Heuristic:** Manhattan Distance (Math.abs(dx) + Math.abs(dy)).
* **Data Structures:**
    * PriorityQueue<Node>: Manages the frontier (Open Set), ordered by F-Cost (G + H).
    * HashSet<Point>: Constant time O(1) lookups for visited nodes (Closed Set).
    * HashMap<Point, Node>: fast retrieval of node costs.

## Usage Guide

1.  **Launch:** Upon opening, the app initializes the grid.
2.  **Generate:** Click **"Generate New Maze"** to watch the Recursive Backtracker carve the layout. Use the slider to speed up or slow down this process.
3.  **Solve:** Once generation is complete, the **"Solve (A*)"** button enables. Click it to watch the AI find the optimal path.
4.  **Analyze:** Watch the "Open Set" (Green) explore. Note how the algorithm prioritizes movement toward the goal (Bottom Right) rather than searching blindly.
