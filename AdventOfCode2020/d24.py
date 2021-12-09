import collections


class HexGrid:

    def __init__(self):
        self.tiles = {}
        self.current = (0, 0)

    def east(self):
        col, row = self.current
        return (col+1, row)

    def west(self):
        col, row = self.current
        return (col-1, row)

    def south_east(self):
        col, row = self.current
        row += 1
        if row % 2 == 0:
            col += 1
        return (col, row)

    def south_west(self):
        col, row = self.current
        row += 1
        if row % 2 != 0:
            col -= 1
        return (col, row)

    def north_east(self):
        col, row = self.current
        row -= 1
        if row % 2 == 0:
            col += 1
        return (col, row)

    def north_west(self):
        col, row = self.current
        row -= 1
        if row % 2 != 0:
            col -= 1
        return (col, row)

    def flip(self, pos=None):
        if pos is None:
            pos = self.current
        if pos not in self.tiles:
            self.tiles[pos] = 'W'
        if self.tiles[pos] == 'W':
            self.tiles[pos] = 'B'
        else:
            self.tiles[pos] = 'W'

    def move_and_flip(self, line):
        self.current = (0, 0)
        steps = parse_instructions(line)
        for step in steps:
            if step == 'e':
                self.current = self.east()
            elif step == 'se':
                self.current = self.south_east()
            elif step == 'sw':
                self.current = self.south_west()
            elif step == 'w':
                self.current = self.west()
            elif step == 'nw':
                self.current = self.north_west()
            elif step == 'ne':
                self.current = self.north_east()
            else:
                raise ValueError(step)
        self.flip()

    def color(self, pos):
        return self.tiles.get(pos, 'W')

    def neighbors(self, pos):
        self.current = pos
        return [
            self.east(),
            self.south_east(),
            self.south_west(),
            self.west(),
            self.north_west(),
            self.north_east(),
        ]

    @property
    def black_tiles(self):
        return sum(1 for v in self.tiles.values() if v == 'B')

    def tick(self):
        black_tiles = [k for k, v in self.tiles.items() if v == 'B']
        index = {}
        flip = []
        for black_tile in black_tiles:
            nbrs = self.neighbors(black_tile)
            black_nbrs = [n for n in nbrs if self.color(n) == 'B']
            white_nbrs = [n for n in nbrs if self.color(n) == 'W']
            if len(black_nbrs) == 0 or len(black_nbrs) > 2:
                flip.append(black_tile)
            for white_nbr in white_nbrs:
                if white_nbr not in index:
                    index[white_nbr] = set()
                index[white_nbr].add(black_tile)

        for white_tile, black_nbrs in index.items():
            if len(black_nbrs) == 2:
                flip.append(white_tile)

        for tile in flip:
            self.flip(tile)


def parse_instructions(line):
    chars = collections.deque(list(line))
    steps = []
    while chars:
        ch = chars.popleft()
        if ch == 'e' or ch == 'w':
            steps.append(ch)
        elif ch == 'n' or ch == 's':
            step = (ch + chars.popleft())
            steps.append(step)
        else:
            raise ValueError('Unsupported')
    return steps


def get_input(path):
    with open(path) as fp:
        return [line.strip() for line in fp.readlines()]


if __name__ == '__main__':
    grid = HexGrid()
    lines = get_input('data/d24_input1_large.txt')
    for line in lines:
        grid.move_and_flip(line)
    print('Tiles flipped:', len(grid.tiles))
    print('Black tiles:', grid.black_tiles)

    for _ in range(100):
        grid.tick()
    print('Black tiles:', grid.black_tiles)
