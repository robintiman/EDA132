package model;

import control.EstimatorInterface;

import java.util.Random;

public class Estimator implements EstimatorInterface {
    private int rows, cols, head;
    private int[] truePos;
    private double[][] T, O;
    private static final int NORTH = 0;
    private static final int WEST = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 3;
    private static final int FIRSTCORNER = 3, FIRSTWALL = 5, FIRSTNOWALL = 8;
//    private static final int NOTHING = -1, FOUND = 1;

    public Estimator(int rows, int cols, int head) {
        this.rows = rows;
        this.cols = cols;
        this.head = head;
        truePos = new int[2];
        int S = rows * cols * head;
        T = new double[S][S];
        O = new double[S][S];
        fillTransitionMatrix();
        int[] reading = sensor();
//        fillObservationMatrix();

        for (int i = 0; i < T.length; i++) {
            for (int j = 0; j < T[i].length; j++) {
                System.out.print(T[i][j] + " ");
            }
            System.out.println();
        }
//        state = new State();
    }

    public int getNumRows() {
        return rows;
    }

    public int getNumCols() {
        return cols;
    }

    public int getNumHead() {
        return head;
    }

    public void update() {

    }

    public int[] getCurrentTruePosition() {
        return truePos;
    }

    public int[] getCurrentReading() {
        return new int[0];
    }

    public double getCurrentProb(int x, int y) {
        return 0;
    }

    public double getOrXY(int rX, int rY, int x, int y) {
        return 0;
    }

    public double getTProb(int x, int y, int h, int nX, int nY, int nH) {
        return 0;
    }

    // -------- PRIVATE METHODS --------

    //  The sensor reports
// - the true location L with probability 0.1
// - any of the n_Ls ∈ {3, 5, 8} existing surrounding fields L_s with probability 0.05 each.
// - any of the n_Ls2 ∈ {5, 6, 7, 9, 11, 16} existing “secondary” surrounding fields L_s2 with probability 0.025 each
// - "nothing" with probability 1.0 - 0.1 - n_Ls*0.05 - n_Ls2*0.025
    private int[] sensor() {
        int truepos = convertToLinearPos(truePos[0], truePos[1]);
        int[] walls = encounteringWalls(truepos, -1);
        int n_Ls, n_Ls2;
        int nbrOfSurroundingWalls = walls[1];
        if (nbrOfSurroundingWalls == 0) {
            // This is hard-coded for now and assumes a 4x4 grid.
            n_Ls = FIRSTCORNER;
            n_Ls2 = 5;
        } else if (nbrOfSurroundingWalls == 1) {
            n_Ls = FIRSTWALL;
            n_Ls2 = 6;
        } else {
            n_Ls = FIRSTNOWALL;
            n_Ls2 = 7;
        }
        double probForSurrField = 0.05 * n_Ls;
        double probFor2SurrField = 0.025 * n_Ls2;
        double probForTruePos = 0.1;
        double probForNothing = 1.0 - probForSurrField - probFor2SurrField - probForTruePos;
        Random rand = new Random();
        double reading = rand.nextDouble();
        if (reading <= probForTruePos) {
            return truePos;
        } else if (reading <= probForTruePos + probForSurrField) {
            // Return some pos in the directly surrounding field.
            return null;
        } else if (reading <= probForTruePos + probForSurrField + probFor2SurrField) {
            // Return some pos in the second surrounding field.

            return null;
        } else {
            // Return nothing (null)
            return null;
        }

    }

//    private int[] pickFromSurroundingField(int level, int n) {
//        int x = truePos[0];
//        int y = truePos[1];
//        Random rand = new Random();
//        int step = 3;
//        if (level == 1) {
//            // First surrounding field
//            if (n == FIRSTCORNER) {
//                step = 2;
//                if (x == 0) {
//                    x += rand.nextInt(step);
//                } else {
//                    x -= rand.nextInt(step);
//                }
//                if (y == 0) {
//                    y += rand.nextInt(step);
//                } else {
//                    y -= rand.nextInt(step);
//                }
//            } else if (n == FIRSTWALL) {
//                step = 2;
//                if (x == 0) {
//                    x += rand.nextInt(step);
//                } else if (x == cols) {
//                    x -= rand.nextInt(step);
//                } else {
//                    x += rand.nextInt(step + 1) - 1;
//                }
//                if (y == 0) {
//                    x += rand.nextInt(step);
//                } else if (x == cols) {
//                    x -= rand.nextInt(step);
//                } else {
//                    x += rand.nextInt(step + 1) - 1;
//                }
//            }
//        } else {
//            // Second surrounding field
//
//        }
//    }

    /**
     * Fills the O matrix
     * @param reading True if the sensor found something, False otherwise.
     */
    private void fillObservationMatrix(boolean reading) {
        int pos, realPos;
        double prob;
        for (int i = 0; i < O[0].length; i += head) {
            pos = i / head;
            realPos = convertToLinearPos(truePos[0], truePos[1]);
//            if (reading) {
//                if (pos == realPos) {
//                    prob = 0.1;
//                } else if () {
//
//                }
//
//            } else {
//
//            }
//            O[i][i] = prob;
        }
    }

    private int getNbrOfSurroundingFields(int pos) {
        // We only want the number of walls so heading doesn't matter here.
        int[] walls = encounteringWalls(pos, -1);
        return 0;

    }

    private boolean inDirectlySurroundingFields(int pos, int realPos) {
        int diff = Math.abs(pos - realPos);
        return true;
    }

    /**
     * Converts given coordinates into a linear position of the room matrix.
     * @param x, y coordinates
     * @return The corresponding linear position.
     */
    private int convertToLinearPos(int x, int y) {
        return x * cols + y;
    }

    private void fillTransitionMatrix() {
        // Each row represents a state and each column represents a state.
        double prob = 0;
        int pos = 0, nextPos = 0;
        int dir = 0, nextDir = 0; // NORTH = 0, WEST = 1, SOUTH = 2, EAST = 3
        for (int i = 0; i < T[0].length; i++) {
            // Gets the current position and heading
            System.out.print(T[0].length);
            pos = i / head;
            dir = i % head;
            for (int j = 0; j < T[0].length; j++) {
                // Iterates through and adds transition probabilities for every other state.
                nextPos = j / head;
                nextDir = j % head;
                if (pos == 0 && nextPos == 1) {
                    int c = 1+1;
                }
                // What is the probability that the next position and direction will be nextPos and nextDir?
                if (nextStateIsPossible(pos, nextPos, nextDir)) {
                    if (dir == nextDir) {
                        prob = 0.7;
                    } else {
                        int[] walls = encounteringWalls(pos, dir);
                        boolean wallEncountered = walls[0] == 1;
                        if (wallEncountered) {
                            prob = 1.0 / (head - walls[1]);
                        } else {
                            prob = 0.3 / (head - walls[1] - 1);
                        }
                    }
                } else {
                    prob = 0;
                }
                T[i][j] = prob;
            }
        }
    }

    private boolean nextStateIsPossible(int p, int np, int nh) {
        int diff = p - np;
        if (p / cols != np / cols) {
            // p and np are on different rows.
            if (diff < 0) {
                return nh == SOUTH && diff == -4;
            } else {
                return nh == NORTH && diff == 4;
            }
        } else {
            // p and np are on the same row.
            if (diff < 0) {
                return nh == EAST && diff == -1;
            } else {
                return nh == WEST && diff == 1;
            }
        }
    }


    private int[] encounteringWalls(int p, int h) {
        int wallEncountered = 0; // 0 if false, 1 otherwise
        int walls = 0;
        int x = (p-1) / cols;
        if (p - cols < 0) {
            walls++; // NORTH
            if (h == NORTH) wallEncountered = 1;
        }
        if ((p / cols != (p - 1) / cols) || p - 1 < 0) {
            walls++; // WEST
            if (h == WEST) wallEncountered = 1;
        }
        if (p + cols > cols * rows) {
            walls++; // SOUTH
            if (h == SOUTH) wallEncountered = 1;
        }
        if (p / cols != (p + 1) / cols) {
            walls++; // EAST
            if (h == EAST) wallEncountered = 1;
        }
        int [] ret = {wallEncountered, walls};
        return ret;
    }
}