class Ferry:

    def __init__(self):
        self.dir = 'E'
        self.pos = [0, 0]

    def navigate(self, command):
        if command[0] in ('L', 'R'):
            self.rotate(command)
        else:
            self.move(command)

    def move(self, command):
        direction, size = command[0], int(command[1:])
        if direction == 'F':
            direction = self.dir

        idx = 0
        if direction == 'N' or direction == 'S':
            idx = 1
        if direction == 'S' or direction == 'W':
            size *= -1

        self.pos[idx] += size


    def rotate(self, command):
        direction, size = command[0], int(command[1:])
        turns = size // 90
        if direction == 'L':
            turns *= -1
        options = ('N', 'E', 'S', 'W')
        current_idx = options.index(self.dir)
        next_idx = (current_idx + turns) % 4
        self.dir = options[next_idx]

    def distance(self, init=(0, 0)):
        x_abs = abs(self.pos[0] - init[0])
        y_abs = abs(self.pos[1] - init[1])
        return x_abs + y_abs


class Waypoint(Ferry):

    def __init__(self):
        super().__init__()
        self.waypoint = [10, 1]

    def move(self, command):
        direction, size = command[0], int(command[1:])
        if direction == 'F':
            self.pos[0] += (size * self.waypoint[0])
            self.pos[1] += (size * self.waypoint[1])
            return

        idx = 0
        if direction == 'N' or direction == 'S':
            idx = 1
        if direction == 'S' or direction == 'W':
            size *= -1

        self.waypoint[idx] += size

    def rotate(self, command):
        direction, size = command[0], int(command[1:])
        if direction == 'R':
            size = 360 - size
        theta = (size // 90) % 4
        sine = (0, 1, 0, -1)
        cosine = (1, 0, -1, 0)
        x = self.waypoint[0] * cosine[theta] - self.waypoint[1] * sine[theta]
        y = self.waypoint[0] * sine[theta] + self.waypoint[1] * cosine[theta]
        self.waypoint = [x, y]


def parse_commands(path):
    with open(path) as fp:
        return [ line.strip() for line in fp.readlines() ]


if __name__ == '__main__':
    commands = parse_commands('data/d12_input1_large.txt')
    ferry = Waypoint()
    for cmd in commands:
        ferry.navigate(cmd)
    print(f'Distance = {ferry.distance()}')
