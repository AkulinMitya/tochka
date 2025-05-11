import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class run2 {
    private static final char[] KEYS_CHAR = new char[26];
    private static final char[] DOORS_CHAR = new char[26];


    static {
        for (int i = 0; i < 26; i++) {
            KEYS_CHAR[i] = (char)('a' + i);
            DOORS_CHAR[i] = (char)('A' + i);
        }
    }


    // Чтение данных из стандартного ввода
    private static char[][] getInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;


        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            lines.add(line);
        }


        char[][] maze = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            maze[i] = lines.get(i).toCharArray();
        }


        return maze;
    }

    private static class Point {
        int r, c;
        Point(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    private static class State {
        int[] robots;
        int keyMask;
        State(int[] r, int mask) {
            robots = Arrays.copyOf(r, 4);
            keyMask = mask;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof State)) return false;
            State s = (State) o;
            return Arrays.equals(robots, s.robots) && keyMask == s.keyMask;
        }
        @Override
        public int hashCode() {
            return Arrays.hashCode(robots) * 31 + keyMask;
        }
    }

    private static List<int[]> findReachableKeys(char[][] map, int sr, int sc, int keyMask, Map<Character, Integer> keyToBit, int n, int m) {
        List<int[]> result = new ArrayList<>();
        Queue<int[]> queue = new ArrayDeque<>();
        boolean[][] visited = new boolean[n][m];
        queue.add(new int[]{sr, sc, 0});
        visited[sr][sc] = true;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int r = cur[0], c = cur[1], steps = cur[2];
            char cell = map[r][c];
            if (cell >= 'a' && cell <= 'z') {
                int bit = keyToBit.get(cell);
                int keyBit = 1 << bit;
                if ((keyMask & keyBit) == 0) {
                    result.add(new int[]{cell, bit, steps});
                    continue;
                }
            }
            for (int[] d : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nr >= n || nc < 0 || nc >= m) continue;
                if (visited[nr][nc]) continue;
                char ch = map[nr][nc];
                if (ch == '#') continue;
                if (ch >= 'A' && ch <= 'Z') {
                    int doorBit = 1 << (ch - 'A');
                    if ((keyMask & doorBit) == 0) continue;
                }
                visited[nr][nc] = true;
                queue.add(new int[]{nr, nc, steps + 1});
            }
        }
        return result;
    }

    public static int min_steps_to_collect_all_keys(char[][] data) {
        int n = data.length;
        int m = data[0].length;

        List<Point> starts = new ArrayList<>();
        Map<Character, Point> keyPositions = new HashMap<>();
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < m; ++j) {
                char c = data[i][j];
                if (c == '@') {
                    starts.add(new Point(i, j));
                } else if (c >= 'a' && c <= 'z') {
                    keyPositions.put(c, new Point(i, j));
                }
            }

        List<Character> sortedKeys = new ArrayList<>(keyPositions.keySet());
        Collections.sort(sortedKeys);
        Map<Character, Integer> keyToBit = new HashMap<>();
        for (int i = 0; i < sortedKeys.size(); i++) {
            keyToBit.put(sortedKeys.get(i), i);
        }

        int allKeysMask = (1 << sortedKeys.size()) - 1;

        Map<State, Integer> dist = new HashMap<>();
        PriorityQueue<State> queue = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        int[] startPositions = new int[4];
        for (int i = 0; i < 4; i++) {
            Point p = (i < starts.size()) ? starts.get(i) : new Point(-1, -1);
            startPositions[i] = p.r * m + p.c;
        }

        State start = new State(startPositions, 0);
        dist.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            State cur = queue.poll();
            int d = dist.get(cur);

            if (cur.keyMask == allKeysMask)
                return d;

            for (int ri = 0; ri < 4; ri++) {
                int pos = cur.robots[ri];
                if (pos < 0) continue;
                int r = pos / m;
                int c = pos % m;

                List<int[]> reachable = findReachableKeys(data, r, c, cur.keyMask, keyToBit, n, m);

                for (int[] keyInfo : reachable) {
                    char keyChar = (char) keyInfo[0];
                    int bit = keyInfo[1];
                    int steps = keyInfo[2];
                    int keyBit = 1 << bit;
                    if ((cur.keyMask & keyBit) != 0) continue;

                    int[] newRobots = Arrays.copyOf(cur.robots, 4);
                    Point dest = keyPositions.get(keyChar);
                    newRobots[ri] = dest.r * m + dest.c;

                    State next = new State(newRobots, cur.keyMask | keyBit);
                    int newDist = d + steps;

                    if (!dist.containsKey(next) || dist.get(next) > newDist) {
                        dist.put(next, newDist);
                        queue.add(next);
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    public static void main(String[] args) throws IOException {
        char[][] data = getInput();
        int result = min_steps_to_collect_all_keys(data);

        if (result == Integer.MAX_VALUE) {
            System.out.println("No solution found");
        } else {
            System.out.println(result);
        }
    }
}
