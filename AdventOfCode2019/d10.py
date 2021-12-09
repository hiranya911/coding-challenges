import math


class Polar(object):

    def __init__(self, ref, target):
        self.target = target
        self.distance = ref.distance(target)
        self.rotation = ref.rotation(target)

    def __str__(self):
        return f'{self.target}: ({self.distance}, {self.rotation})'

    def __repr__(self):
        return str(self)

    def __eq__(self, other):
        return self.rotation == other.rotation and self.distance == other.distance

    def __lt__(self, other):
        if self.rotation == other.rotation:
            return self.distance < other.distance
        return self.rotation > other.rotation


class Asteroid(object):

    def __init__(self, x, y):
        self.x = x
        self.y = y

    def __str__(self):
        return f'({self.x}, {self.y})'

    def __repr__(self):
        return self.__str__()

    def __hash__(self):
        return hash(self.x) + 31 * hash(self.y)

    def __eq__(self, other):
        return self.x == other.x and self.y == other.y

    def distance(self, other):
        y_diff = self.y - other.y
        x_diff = self.x - other.x
        return math.sqrt(y_diff * y_diff + x_diff * x_diff)

    def rotation(self, other):
        return math.atan2(other.x - self.x, other.y - self.y) - math.pi


class NewLine(object):

    def __init__(self, p1, p2):
        self.p1 = p1
        self.p2 = p2
        self.points = {p1, p2}

    def add(self, other):
        if collinear(self.p1, other.p1, other.p2) and collinear(self.p2, other.p1, other.p2):
            self.points = self.points.union(other.points)
            return True
        return False

    def visible_count(self, p):
        if p not in self.points:
            return 0
        elif len(self.points) == 2:
            return 1
        elif in_between(p, self.points):
            return 2
        else:
            return 1

    def __repr__(self):
        return str(self)

    def __str__(self):
        return f'{self.p1}---{self.p2} ({self.slope})'

    @property
    def slope(self):
        if self.p1.y == self.p2.y:
            return math.pi / 2
        ratio = (self.p1.x - self.p2.x) / (self.p1.y - self.p2.y)
        return math.atan(ratio)


def do_thing_2(asteroids):
    src_2_targets = {}
    for source in asteroids:
        rotations = {}
        for target in asteroids:
            if source == target:
                continue
            plr = Polar(source, target)
            targets = rotations.get(plr.rotation, [])
            targets.append(plr)
            rotations[plr.rotation] = targets
        src_2_targets[source] = rotations
    return src_2_targets
        
    

def do_thing(asteroids):
    print(f'No of asteroids: {len(asteroids)}')
    lines = {}
    for i in range(0, len(asteroids)):
        temp = []
        for j in range(0, len(asteroids)):
            if i == j:
                continue
            line = NewLine(asteroids[i], asteroids[j])
            merged = False
            for other in temp:
                if other.add(line):
                    merged = True
                    break

            if not merged:
                temp.append(line)
        lines[asteroids[i]] = temp

    print(f'No of lines: {len(lines)}')
    visibility = {}
    for p in lines:
        visibility[p] = sum([line.visible_count(p) for line in lines[p]])
    return visibility, lines
                

def parse_map(file_name):
    with open(file_name) as fp:
        lines = fp.readlines()

    result = []
    for row, line in enumerate(lines):
        for col, char in enumerate(line):
            if char == '#':
                result.append(Asteroid(col, row))
    return result


def in_between(ref, points):
    copy = [p for p in points]
    if copy[0].x != copy[1].x:
        copy = sorted(copy, key=lambda p: p.x)
        idx = copy.index(ref)
        return idx > 0 and idx < len(points) - 1
    copy = sorted(copy, key=lambda p: p.y)
    idx = copy.index(ref)
    return idx > 0 and idx < len(points) - 1


def collinear(p1, p2, p3):
    area = (p1.x - p2.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p2.y)
    return area == 0


def firing_order(source, lines):
    lines_of_fire = lines[source]
    polars = []
    for rotations in lines_of_fire.values():
        polars += rotations

    result = sorted(polars)
    count = 0
    order = []
    while result:
        destroyed = []
        last = None
        for plr in result:
            if last and last.rotation == plr.rotation:
                continue
            count += 1
            order.append(plr)
            destroyed.append(plr)
            last = plr

        for plr in destroyed:
            result.remove(plr)
    return order


asteroids = parse_map('ast_map_prod.txt')
src_2_targets = do_thing_2(asteroids)
max_key = max(src_2_targets.keys(), key=lambda k: len(src_2_targets[k]))
max_val = src_2_targets[max_key]
print(f'{max_key}: {len(max_val)}')

order = firing_order(max_key, src_2_targets)
print(f'200: {order[199].target}')
