import search


class PlutoMaze(object):

    def __init__(self):
        self.cells = {}
        self.rows = 0
        self.consumed = set()

    def add_row(self, row):
        for col, ch in enumerate(row):
            self.cells[(col, self.rows)] = ch
        self.rows += 1

    @property
    def start(self):
        aa = [k for k, v in self.portals.items() if v == 'AA']
        return aa[0]

    @property
    def end(self):
        zz = [k for k, v in self.portals.items() if v == 'ZZ']
        return zz[0]

    def navigate(self, recurse=False):
        init = (self.start, 0)
        goal = self.end

        def goal_test(state):
            return state[0] == goal and state[1] == 0

        successors = self._find_reachable
        if recurse:
            successors = self._find_recursive_reachable

        result = search.astar(init, goal_test, successors)
        if result:
            return result.cost

        raise Exception('No result found')

    def _find_recursive_reachable(self, state):
        x, y = state[0]
        z = state[1]
        
        steps = [(0,1), (0,-1), (1,0), (-1,0)]
        nbrs = [(x+a, y+b) for a, b in steps]
        reachable = [(nbr, z) for nbr in nbrs if self.cells.get(nbr) == '.']

        pos = (x, y)
        portal = self.portals.get(pos)
        if portal and portal not in ('AA', 'ZZ'):
            dest = [k for k, v in self.portals.items() if v == portal and k != pos][0]
            if self.is_outer(pos):
                if z > 0:
                    reachable += [(dest, z - 1)]
            else:
                reachable += [(dest, z + 1)]

        return reachable
                
        raise Exception('No result found')

    def _find_reachable(self, state):
        x, y = state[0]
        steps = [(0,1), (0,-1), (1,0), (-1,0)]
        nbrs = [(x+a, y+b) for a, b in steps]
        reachable = [(nbr, 0) for nbr in nbrs if self.cells.get(nbr) == '.']

        pos = (x, y)
        portal = self.portals.get(pos)
        if portal and portal not in ('AA', 'ZZ'):
            dest = [k for k, v in self.portals.items() if v == portal and k != pos][0]
            reachable += [(dest, 0)]
        return reachable

    def is_outer(self, portal):
        x, y = portal
        if x == 2 or y == 2:
            return True

        max_x = max(x for x, _ in self.cells)
        if x == max_x - 2:
            return True
        
        max_y = max(y for _, y in self.cells)
        if y == max_y - 2:
            return True
        return False

    def _find_portals(self):
        self.portals = {}
        open_spaces = [k for k, v in self.cells.items() if v == '.']
        steps = [(0,1), (0,-1), (1,0), (-1,0)]
        for x, y in open_spaces:
            nbrs = [(x+a, y+b) for a, b in steps]
            for i, nbr in enumerate(nbrs):
                ch1 = self.cells.get(nbr)
                if ch1.isupper():
                    nx, ny = nbr[0] + steps[i][0], nbr[1] + steps[i][1]
                    ch2 = self.cells.get((nx, ny))
                    name = ch1 + ch2
                    if i == 1 or i == 3:
                        name = name[::-1]
                    self.portals[(x,y)] = name
                    break

    def finalize(self):
        self._find_portals()
        print(f'{len(self.portals)} portals detected')
        print()



def build_maze(path):
    with open(path) as fp:
        lines = fp.readlines()

    maze = PlutoMaze()
    for line in lines:
        maze.add_row(line.rstrip())
    maze.finalize()
    return maze


small = 'pluto_small.txt'
medium = 'pluto_medium.txt'
large = 'pluto_large.txt'
prod = 'pluto_prod.txt'

maze = build_maze(prod)
print(maze.navigate(recurse=False))
