class Eris(object):

    def __init__(self):
        self.cells = {}
        self.rows = 0
        self.levels = 1

    def add_row(self, row):
        for col, ch in enumerate(row):
            self.cells[(col, self.rows, 0)] = ch
        self.rows += 1

    def string_hash(self):
        temp = []
        for y in range(5):
            for x in range(5):
                temp.append(self.cells[(x, y, 0)])
        return ''.join(temp)

    def evolve(self):
        temp = {}
        for y in range(5):
            for x in range(5):
                current = self.cells[(x, y, 0)]
                adjacent = self.count_adjacent(x, y)
                if current == '#' and adjacent != 1:
                    current = '.'
                elif current == '.' and (adjacent == 1 or adjacent == 2):
                    current = '#'
                temp[(x, y, 0)] = current
        self.cells = temp

    def evolve_recursive(self):
        temp = {}
        for z in range(0 - self.levels, self.levels + 1):
            for y in range(5):
                for x in range(5):
                    if x == 2 and y == 2:
                        continue
                    current = self.cells.get((x, y, z), '.')
                    adjacent = self.count_adjacent_recursive(x, y, z)
                    if current == '#' and adjacent != 1:
                        current = '.'
                    elif current == '.' and (adjacent == 1 or adjacent == 2):
                        current = '#'
                    temp[(x, y, z)] = current
        self.cells = temp
        self.levels += 1

    def print_levels(self):
        minz = min(z for _, _, z in self.cells)
        maxz = max(z for _, _, z in self.cells)
        for z in range(minz, maxz + 1):
            print('Level:', z)
            for y in range(5):
                for x in range(5):
                    print(self.cells.get((x, y, z), '.'), end='')
                print()
            print()

    def count_adjacent_recursive(self, x, y, z):
        steps = [(0,1), (0,-1), (1,0), (-1,0)]
        nbrs = []
        for xd, yd in steps:
            xnew, ynew = x + xd, y + yd
            if xnew < 0:
                nbrs.append((1, 2, z-1))
            elif xnew > 4:
                nbrs.append((3, 2, z-1))
            elif ynew < 0:
                nbrs.append((2, 1, z-1))
            elif ynew > 4:
                nbrs.append((2, 3, z-1))
            elif xnew == 2 and ynew == 2:
                if x == 1:
                    nbrs += [(0, i, z+1) for i in range(5)]
                elif x == 3:
                    nbrs += [(4, i, z+1) for i in range(5)]
                elif y == 1:
                    nbrs += [(i, 0, z+1) for i in range(5)]
                elif y == 3:
                    nbrs += [(i, 4, z+1) for i in range(5)]
                else:
                    raise Exception()
            else:
                nbrs.append((xnew, ynew, z))

        return sum(1 for nbr in nbrs if self.cells.get(nbr) == '#')

    def count_bugs(self):
        total = 0
        for z in range(0 - self.levels, self.levels + 1):
            for y in range(5):
                for x in range(5):
                    if self.cells.get((x, y, z)) == '#':
                        total += 1
        return total

    def count_adjacent(self, x, y):
        steps = [(0,1), (0,-1), (1,0), (-1,0)]
        nbrs = [(x+xd, y+yd) for xd, yd in steps]
        values = [
            1
            for xnew, ynew in nbrs
            if self.cells.get((xnew, ynew, 0)) == '#'
        ]
        return sum(values)

    def print_map(self):
        for y in range(5):
            for x in range(5):
                val = self.cells[(x, y, 0)]
                print(val, end='')
            print()

    def bio_diversity(self):
        return sum(2**(5*key[1] + key[0]) for key, val in self.cells.items() if val == '#')


def load_eris(path):
    with open(path) as fp:
        lines = fp.readlines()

    eris = Eris()
    for line in lines:
        eris.add_row(line.strip())
    return eris


def evolve_until_repeat(eris):
    seen = {eris.string_hash()}
    while True:
        eris.evolve()
        h = eris.string_hash()
        if h in seen:
            eris.print_map()
            break
        seen.add(h)
    print(eris.bio_diversity())

    
small = 'eris_small.txt'
prod = 'eris_prod.txt'

eris = load_eris(prod)
for i in range(200):
    eris.evolve_recursive()
print(eris.count_bugs())
