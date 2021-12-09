import functools


class RoadMap:

    def __init__(self, path):
        with open(path) as fp:
            self.rows = [ line.strip() for line in fp.readlines() ]
            self.offet = len(self.rows[0])

    def lookup(self, row, col):
        curr = self.rows[row]
        return curr[col % self.offet]

    def traverse(self, right, down):
        row, col = 0, 0
        trees = 0
        while row + down < len(self.rows):
            row += down
            col += right
            curr = self.lookup(row, col)
            if curr == '#':
                trees += 1
        return trees

    def __repr__(self):
        return '\n'.join(self.rows)


if __name__ == '__main__':
    rm = RoadMap('d3_input1_large.txt')
    slopes = [ (1, 1), (3, 1), (5, 1), (7, 1), (1, 2)]
    results = []
    for r, d in slopes:
        trees = rm.traverse(right=r, down=d)
        print(f'({r}, {d}): Trees = {trees}')
        results.append(trees)

    prod = functools.reduce(lambda x, y: x*y, results)
    print()
    print(f'Final product = {prod}')
