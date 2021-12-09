import collections
import heapq


class Node(object):

    def __init__(self, curr, item, steps, prev):
        self.curr = curr
        self.prev = prev
        self.item = item
        self.steps = steps

    @property
    def keys(self):
        keys = []
        if self.item.islower():
            keys.append(self.item)
        if self.prev:
            keys += self.prev.keys
        return keys

    @property
    def distance(self):
        distance = self.steps
        if self.prev:
            distance += self.prev.distance
        return distance

    def __eq__(self, other):
        return self.curr == other.curr
    

class Maze(object):

    def __init__(self):
        self.cells = {}
        self.rows = 0
        self.pos = None
        self.keys = []
        self.key_count = 0
        self.divided = False

    def add_row(self, row):
        for col, ch in enumerate(row):
            self.cells[(col, self.rows)] = ch
            if ch == '@':
                self.pos = (col, self.rows)
            elif ch.islower():
                self.key_count += 1
        self.rows += 1

    def divide(self):
        x, y = self.pos
        max_x = max(p[0] for p in self.cells)
        max_y = max(p[1] for p in self.cells)
        for i in range(max_x + 1):
            self.cells[i, y] = '#'
        for i in range(max_y + 1):
            self.cells[x, i] = '#'
        self.divided = True

    def collect_keys2(self):
        frontier = (self.pos, )
        if self.divided:
            x, y = self.pos
            frontier = (
                (x-1, y-1),
                (x-1, y+1),
                (x+1, y-1),
                (x+1, y+1),
            )
            
        q = [(0, frontier, frozenset())]
        visited = [set(), set(), set(), set()]
        while q:
            dist, frontier, keys = heapq.heappop(q)
            if len(keys) == self.key_count:
                return dist

            for i, pos in enumerate(frontier):
                state = (pos[0], pos[1], keys)
                if state in visited[i]:
                    continue

                visited[i].add(state)
                reach = self._find_reachable(pos, keys)
                for (cell, ndist) in reach:
                    nkey = frozenset([self.cells[cell]])
                    nfrontier = frontier[0:i] + (cell, ) + frontier[i+1:]
                    heapq.heappush(q, (dist + ndist, nfrontier, nkey | keys))

    def _find_reachable(self, pos, keys):
        queue = collections.deque([(pos, 0)])
        reach = []
        visited = set()
        # BFS to find the closest reachable keys
        while queue:
            cell, dist = queue.popleft()
            obj = self.cells[cell]
            if obj.islower() and obj not in keys:
                reach.append((cell, dist))
                continue

            nbrs = self._next(cell)
            for nbr in nbrs:
                if nbr in visited:
                    continue
                visited.add(nbr)
                
                obj = self.cells[nbr]
                if not obj.isupper() or obj.lower() in keys:
                    queue.append((nbr, dist + 1))
                    
        return reach

    def _next(self, curr):
        x, y = curr
        nbrs = [
            (x, y + 1),
            (x, y - 1),
            (x + 1, y),
            (x - 1, y),
        ]
        return [n for n in nbrs if n in self.cells and self.cells[n] != '#']

    def print(self):
        cols = max(k[0] for k in self.cells.keys())
        for row in range(self.rows):
            for col in range(cols + 1):
                print(self.cells[(col, row)], end='')
            print()


def build_maze(path):
    with open(path) as fp:
        lines = fp.readlines()

    maze = Maze()
    for line in lines:
        maze.add_row(line.strip())

    return maze


sample1 = 'maze_small.txt'
sample2 = 'maze_medium.txt'
sample3 = 'maze_medium2.txt'

prod = 'maze_prod.txt'

maze = build_maze(prod)
maze.divide()
maze.print()
print(maze.collect_keys2())
