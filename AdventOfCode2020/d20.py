class Tile:

    def __init__(self, idx, rows):
        self.idx = idx
        self.rows = rows

    def make_copy(self):
        rows = []
        for row in self.rows:
            rows.append([val for val in row])
        return Tile(self.idx, rows)

    def flip_horizontal(self):
        for row in self.rows:
            row.reverse()

    def flip_vertical(self):
        self.rows.reverse()

    def rotate_right(self):
        rotated = []
        for col in range(len(self.rows[0])):
            new_row = []
            for row in range(len(self.rows) - 1, -1, -1):
                new_row.append(self.rows[row][col])
            rotated.append(new_row)
        self.rows = rotated

    def dump(self):
        print(f'Tile: {self.idx}')
        for row in self.rows:
            print(''.join(row))

    def remove_borders(self):
        rows = []
        for row in self.rows[1:-1]:
            rows.append([item for item in row[1:-1]])
        return Tile(self.idx, rows)

    def concat_left(self, other):
        rows = []
        for idx in range(len(self.rows)):
            left, right = self.rows[idx], other.rows[idx]
            rows.append(left+right)
        return Tile(0, rows)

    def concat_down(self, other):
        return Tile(0, self.rows + other.rows)

    def __repr__(self):
        return f'{self.idx}'

    @property
    def top(self):
        return self.rows[0]

    @property
    def bottom(self):
        return self.rows[-1]

    @property
    def left(self):
        return [row[0] for row in self.rows]

    @property
    def right(self):
        return [row[-1] for row in self.rows]


def create_tile(rows):
    header = rows[0]
    _, idx = header.split()
    rows = [list(line) for line in rows[1:]]
    return Tile(int(idx[:-1]), rows)


def parse_tiles(path):
    tiles = []
    with open(path) as fp:
        buffer = []
        for line in fp.readlines():
            line = line.strip()
            if line:
                buffer.append(line)
            else:
                tiles.append(create_tile(buffer))
                buffer = []

        if buffer:
            tiles.append(create_tile(buffer))
    return tiles


def fit_up(src, target):
    for _ in range(4):
        if src.top == target.bottom:
            return True
        target.rotate_right()

    target.flip_horizontal()
    for _ in range(4):
        if src.top == target.bottom:
            return True
        target.rotate_right()

    return False


def fit_down(src, target):
    for _ in range(4):
        if src.bottom == target.top:
            return True
        target.rotate_right()

    target.flip_horizontal()
    for _ in range(4):
        if src.bottom == target.top:
            return True
        target.rotate_right()
    return False


def fit_left(src, target):
    for _ in range(4):
        if src.left == target.right:
            return True
        target.rotate_right()

    target.flip_vertical()
    for _ in range(4):
        if src.left == target.right:
            return True
        target.rotate_right()
    return False


def fit_right(src, target):
    for _ in range(4):
        if src.right == target.left:
            return True
        target.rotate_right()

    target.flip_vertical()
    for _ in range(4):
        if src.right == target.left:
            return True
        target.rotate_right()
    return False


def neighbors(pos):
    x, y = pos
    diff = [(0,1), (0,-1), (1,0), (-1,0)]
    return [(x+dx, y+dy) for dx, dy in diff]


def check_neighbors(pos, tile, grid):
    up, down, right, left = neighbors(pos)
    if up in grid and tile.top != grid[up].bottom:
        return False
    if down in grid and tile.bottom != grid[down].top:
        return False
    if left in grid and tile.left != grid[left].right:
        return False
    if right in grid and tile.right != grid[right].left:
        return False
    return True


def dump_grid(grid):
    min_x = min(x for x,_ in grid.keys())
    max_x = max(x for x,_ in grid.keys())
    min_y = min(y for _,y in grid.keys())
    max_y = max(y for _,y in grid.keys())
    for y in range(max_y, min_y - 1, - 1):
        for x in range(min_x, max_x + 1):
            print(grid.get((x,y), 'XXXX'), end='  ')
        print()

    return grid[(min_x,min_y)].idx * grid[(min_x,max_y)].idx * grid[(max_x,min_y)].idx * grid[(max_x,max_y)].idx


def print_image(grid):
    min_x = min(x for x,_ in grid.keys())
    max_x = max(x for x,_ in grid.keys())
    min_y = min(y for _,y in grid.keys())
    max_y = max(y for _,y in grid.keys())
    rows = []
    for y in range(max_y, min_y - 1, - 1):
        start = None
        for x in range(min_x, max_x + 1):
            if not start:
                start = grid[(x,y)].remove_borders()
            else:
                start = start.concat_left(grid[(x,y)].remove_borders())
        rows.append(start)

    start = rows[0]
    for row in rows[1:]:
        start = start.concat_down(row)
    start.dump()
    return start


class State:

    def __init__(self, grid, tiles):
        print(len(grid))
        self.grid = grid
        self.tiles = tiles

    @property
    def width(self):
        min_x = min(x for x, _ in self.grid.keys())
        max_x = max(x for x, _ in self.grid.keys())
        return max_x - min_x + 1

    @property
    def height(self):
        min_y = min(y for _, y in self.grid.keys())
        max_y = max(y for _, y in self.grid.keys())
        return max_y - min_y + 1

    def is_solved(self):
        return len(self.tiles) == 0

    def up_successors(self, src_pos, src, target_pos):
        candidates = []
        for tile in self.tiles:
            if fit_up(src, tile) and check_neighbors(target_pos, tile, self.grid):
                candidates.append(tile)
        return candidates

    def down_successors(self, src_pos, src, target_pos):
        candidates = []
        for tile in self.tiles:
            if fit_down(src, tile) and check_neighbors(target_pos, tile, self.grid):
                candidates.append(tile)
        return candidates

    def left_successors(self, src_pos, src, target_pos):
        candidates = []
        for tile in self.tiles:
            if fit_left(src, tile) and check_neighbors(target_pos, tile, self.grid):
                candidates.append(tile)
        return candidates

    def right_successors(self, src_pos, src, target_pos):
        candidates = []
        for tile in self.tiles:
            if fit_right(src, tile) and check_neighbors(target_pos, tile, self.grid):
                candidates.append(tile)
        return candidates

    def new_state(self, candidate, target_pos):
        grid = dict(self.grid)
        tiles = list(self.tiles)
        grid[target_pos] = candidate.make_copy() # Make sure grid tiles don't change
        tiles.remove(candidate)
        return State(grid, tiles)

    def _find_successors(self, src_pos, src):
        result = []
        up, down, right, left = neighbors(src_pos)
        if up not in self.grid:
            result += [self.new_state(c, up) for c in self.up_successors(src_pos, src, up)]
        if down not in self.grid:
            result += [self.new_state(c, down) for c in self.down_successors(src_pos, src, down)]
        if left not in self.grid:
            result += [self.new_state(c, left) for c in self.left_successors(src_pos, src, left)]
        if right not in self.grid:
            result += [self.new_state(c, right) for c in self.right_successors(src_pos, src, right)]

        return [ r for r in result if r.height <= 12 and r.width <= 12 ]

    def successors(self):
        candidates = []
        for pos, src in self.grid.items():
            candidates += self._find_successors(pos, src)
        return candidates

    def __hash__(self):
        items = [(k[0], k[1], v.idx) for k, v in self.grid.items()]
        return hash(tuple(sorted(items)))

    def __eq__(self, other):
        return self.grid == other.grid


class Node:

    def __init__(self, state, parent):
        self.state = state
        self.parent = parent


def dfs(initial):
    frontier = []
    frontier.append(Node(initial, None))
    explored = {initial}
    while frontier:
        current = frontier.pop()
        current_state = current.state
        if current_state.is_solved():
            return current

        for child in current_state.successors():
            if child in explored:
                continue
            explored.add(child)
            frontier.append(Node(child, current))
    return None


def _do_detect(tile):
    pattern = [
        (18,-1),
        (5, 0), (6, 0), (11, 0), (12, 0), (17, 0), (18, 0), (19, 0),
        (1, 1), (4, 1), (7, 1), (10, 1), (13, 1), (16, 1)
    ]
    sea_monsters = set()
    rows = tile.rows
    for row in range(1, len(rows) - 1):
        for col in range(len(rows[0]) - 19):
            if rows[row][col] == '#':
                candidate = [(col+dx, row+dy) for dx, dy in pattern]
                if all(rows[y][x] == '#' for x, y in candidate):
                    sea_monsters.add((col, row))
                    for c in candidate:
                        sea_monsters.add(c)
    return sea_monsters


def detect_sea_monsters(tile):
    rows = tile.rows
    hashes = set()
    for row in range(len(rows)):
        for col in range(len(rows[0])):
            if rows[row][col] == '#':
                hashes.add((row, col))
    print(f'Total hashes = {len(hashes)}')

    monsters = None
    for _ in range(4):
        monsters = _do_detect(tile)
        if not monsters:
            tile.rotate_right()
        else:
            break

    if not monsters:
        tile.flip_horizontal()
        for _ in range(4):
            monsters = _do_detect(tile)
            if not monsters:
                tile.rotate_right()
            else:
                break

    print('Roughness =', len(hashes) - len(monsters))


if __name__ == '__main__':
    tiles = parse_tiles('data/d20_input1_large.txt')
    print(f'Total tiles = {len(tiles)}')

    grid = {(0,0): tiles.pop().make_copy()}
    initial = State(grid, tiles)
    result = dfs(initial)
    res = dump_grid(result.state.grid)

    print()
    print('Result =', res)

    tile = print_image(result.state.grid)
    detect_sea_monsters(tile)
