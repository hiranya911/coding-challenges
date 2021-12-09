import d2


UP = 0
RIGHT = 1
DOWN = 2
LEFT = 3


class CleaningRobot(d2.InputDevice, d2.OutputDevice):

    def __init__(self, inputs):
        self.inputs = inputs
        self.last = None

    def read(self):
        return self.inputs.pop(0)

    def write(self, value):
        self.last = value


class Camera(d2.OutputDevice):

    def __init__(self):
        self.cells = {}
        self.x = 0
        self.y = 0

    def write(self, value):
        print(chr(value), end='')
        if value != 10:
            self.cells[(self.x, self.y)] = value
            self.x += 1
        else:
            self.x = 0
            self.y += 1

    def find_intersections(self):
        marker = ord('#')
        scaffolding = [key for key in self.cells.keys() if self.cells[key] == marker]

        def is_intersection(pos):
            nbrs = neighbors(pos)
            return all(self.cells.get(n) == marker for n in nbrs)

        return [s for s in scaffolding if is_intersection(s)]

    def find_robot(self):
        for key, value in self.cells.items():
            if chr(value) in ('<', '>', '^', 'v'):
                return key
        raise Exception('Robot missing')

    def simple_walk(self):
        marker = ord('#')
        robot = self.find_robot()
        visited = {robot}
        path = [robot]
        direction = UP
        scaff = [s for s in self.cells.keys() if self.cells[s] == marker]
        last = None
        while len(visited) < len(scaff) + 1:
            nbrs = [n for n in neighbors(robot) if self.cells.get(n) == marker]
            if len(nbrs) == 1:
                last = robot
                robot = nbrs[0]
            elif len(nbrs) == 2 or len(nbrs) == 3:
                other = [n for n in nbrs if n != last]
                last = robot
                robot = other[0]
            else:
                xdiff = robot[0] - last[0]
                ydiff = robot[1] - last[1]
                other = [n for n in nbrs if n[0] - robot[0] == xdiff and n[1] - robot[1] == ydiff]
                last = robot
                robot = other[0]
            visited.add(robot)
            path.append(robot)

        return path

    
def rotate(current, new):
    commands = []
    while current != new:
        current = (current + 1) % 4
        commands.append('R')
    if len(commands) == 3:
        commands = ['L']
    return commands


def move_to(direction, src, dst):
    if src == dst:
        return [], direction

    commands = []
    if not is_adjacent(src, dst):
        raise Exception()


    xdiff = src[0] - dst[0]
    ydiff = src[1] - dst[1]
    next_dir = direction
    if xdiff == 1:
        next_dir = LEFT
    elif xdiff == -1:
        next_dir = RIGHT
    elif ydiff == 1:
        next_dir = UP
    elif ydiff == -1:
        next_dir = DOWN
    else:
        raise Exception()

    temp = rotate(direction, next_dir)
    commands = concat_commands(commands, temp)
    commands = concat_commands(commands, [1])
    return commands, next_dir
    
            
def concat_commands(cmd1, cmd2):
    rotations = ('L', 'R')
    if cmd1 and cmd2 and cmd1[-1] not in rotations and cmd2[0] not in rotations:
        cmd1[-1] += cmd2[0]
        cmd1 += cmd2[1:]
    else:
        cmd1 += cmd2
    return cmd1


def generate_commands(path, direction=UP):
    commands = []
    for pos in range(1, len(path)):
        temp, direction = move_to(direction, path[pos-1], path[pos])
        commands = concat_commands(commands, temp)
    return commands


def is_adjacent(p1, p2):
    xdiff = abs(p1[0] - p2[0])
    ydiff = abs(p1[1] - p2[1])
    if xdiff == 1 and ydiff == 0:
        return True
    if xdiff == 0 and ydiff == 1:
        return True
    return False
    

def neighbors(pos):
    return [
        (pos[0] + 1, pos[1]),
        (pos[0] - 1, pos[1]),
        (pos[0], pos[1] + 1),
        (pos[0], pos[1] - 1),
    ]


def print_maze(cells):
    max_x = max(p[0] for p in cells.keys())
    max_y = max(p[1] for p in cells.keys())
    for row in range(max_y + 1):
        for col in range(max_x + 1):
            print(chr(cells[(col, row)]), end='')
        print()
        

def encode_routines(path, routines):
    pos = 0
    seq = []
    output = []
    while pos < len(path):
        seq.append(path[pos])
        for k, v in routines.items():
            if v == seq:
                output.append(k)
                seq = []
        pos += 1
    return output


def encode_commands(commands):
    output = []
    for cmd in commands:
        s = str(cmd)
        if output:
            output.append(ord(','))
        for ch in s:
            output.append(ord(ch))
    output.append(ord('\n'))
    return output


def longest_repeated_prefix(commands):
    print(commands)
    prefix = [commands[0]]
    longest = []
    while True:
        l = len(prefix)
        found = False
        for i in range(l, len(commands)):
            current = commands[i: i+l]
            if prefix == current and l > len(longest):
                found = True
                longest = [p for p in prefix]
                break

        if not found:
            while len(encode_commands(longest)) > 20:
                longest = longest[0:-1]
            return longest
        prefix.append(commands[l])


def remove_matches(commands, prefix):
    start = 0
    while start < len(commands):
        curr = commands[start:start+len(prefix)]
        if curr == prefix:
            commands = commands[0:start] + commands[start+len(prefix):]
        else:
            start += 1
    return commands
        
        
class CameraIOManager(d2.IOManager):

    def __init__(self):
        self.camera = Camera()

    def get_output_device(self):
        return self.camera


class RobotIOManager(d2.IOManager):

    def __init__(self, robot):
        self.robot = robot

    def get_input_device(self):
        return self.robot

    def get_output_device(self):
        return self.robot


PROG = [
    1,330,331,332,109,4078,1102,1,1182,15,1102,1,1477,24,1002,0,1,570,1006,570,36,1001,571,0,0,1001,570,-1,570,1001,24,1,24,1106,0,18,1008,571,0,571,1001,15,1,15,1008,15,1477,570,1006,570,14,21101,0,58,0,1105,1,786,1006,332,62,99,21102,1,333,1,21102,1,73,0,1106,0,579,1101,0,0,572,1101,0,0,573,3,574,101,1,573,573,1007,574,65,570,1005,570,151,107,67,574,570,1005,570,151,1001,574,-64,574,1002,574,-1,574,1001,572,1,572,1007,572,11,570,1006,570,165,101,1182,572,127,1001,574,0,0,3,574,101,1,573,573,1008,574,10,570,1005,570,189,1008,574,44,570,1006,570,158,1105,1,81,21101,0,340,1,1106,0,177,21102,1,477,1,1106,0,177,21101,514,0,1,21102,1,176,0,1105,1,579,99,21102,184,1,0,1106,0,579,4,574,104,10,99,1007,573,22,570,1006,570,165,102,1,572,1182,21102,375,1,1,21101,211,0,0,1106,0,579,21101,1182,11,1,21101,0,222,0,1106,0,979,21102,388,1,1,21102,233,1,0,1105,1,579,21101,1182,22,1,21101,0,244,0,1106,0,979,21102,1,401,1,21102,255,1,0,1105,1,579,21101,1182,33,1,21102,266,1,0,1105,1,979,21101,0,414,1,21101,277,0,0,1106,0,579,3,575,1008,575,89,570,1008,575,121,575,1,575,570,575,3,574,1008,574,10,570,1006,570,291,104,10,21101,0,1182,1,21102,1,313,0,1105,1,622,1005,575,327,1102,1,1,575,21101,327,0,0,1105,1,786,4,438,99,0,1,1,6,77,97,105,110,58,10,33,10,69,120,112,101,99,116,101,100,32,102,117,110,99,116,105,111,110,32,110,97,109,101,32,98,117,116,32,103,111,116,58,32,0,12,70,117,110,99,116,105,111,110,32,65,58,10,12,70,117,110,99,116,105,111,110,32,66,58,10,12,70,117,110,99,116,105,111,110,32,67,58,10,23,67,111,110,116,105,110,117,111,117,115,32,118,105,100,101,111,32,102,101,101,100,63,10,0,37,10,69,120,112,101,99,116,101,100,32,82,44,32,76,44,32,111,114,32,100,105,115,116,97,110,99,101,32,98,117,116,32,103,111,116,58,32,36,10,69,120,112,101,99,116,101,100,32,99,111,109,109,97,32,111,114,32,110,101,119,108,105,110,101,32,98,117,116,32,103,111,116,58,32,43,10,68,101,102,105,110,105,116,105,111,110,115,32,109,97,121,32,98,101,32,97,116,32,109,111,115,116,32,50,48,32,99,104,97,114,97,99,116,101,114,115,33,10,94,62,118,60,0,1,0,-1,-1,0,1,0,0,0,0,0,0,1,46,50,0,109,4,2102,1,-3,586,21002,0,1,-1,22101,1,-3,-3,21102,0,1,-2,2208,-2,-1,570,1005,570,617,2201,-3,-2,609,4,0,21201,-2,1,-2,1106,0,597,109,-4,2106,0,0,109,5,1201,-4,0,630,20102,1,0,-2,22101,1,-4,-4,21101,0,0,-3,2208,-3,-2,570,1005,570,781,2201,-4,-3,653,20102,1,0,-1,1208,-1,-4,570,1005,570,709,1208,-1,-5,570,1005,570,734,1207,-1,0,570,1005,570,759,1206,-1,774,1001,578,562,684,1,0,576,576,1001,578,566,692,1,0,577,577,21101,0,702,0,1105,1,786,21201,-1,-1,-1,1106,0,676,1001,578,1,578,1008,578,4,570,1006,570,724,1001,578,-4,578,21101,0,731,0,1106,0,786,1106,0,774,1001,578,-1,578,1008,578,-1,570,1006,570,749,1001,578,4,578,21102,756,1,0,1105,1,786,1106,0,774,21202,-1,-11,1,22101,1182,1,1,21102,1,774,0,1106,0,622,21201,-3,1,-3,1105,1,640,109,-5,2106,0,0,109,7,1005,575,802,20101,0,576,-6,21002,577,1,-5,1106,0,814,21102,1,0,-1,21101,0,0,-5,21102,0,1,-6,20208,-6,576,-2,208,-5,577,570,22002,570,-2,-2,21202,-5,51,-3,22201,-6,-3,-3,22101,1477,-3,-3,1201,-3,0,843,1005,0,863,21202,-2,42,-4,22101,46,-4,-4,1206,-2,924,21101,1,0,-1,1106,0,924,1205,-2,873,21101,0,35,-4,1106,0,924,2102,1,-3,878,1008,0,1,570,1006,570,916,1001,374,1,374,1202,-3,1,895,1101,0,2,0,2102,1,-3,902,1001,438,0,438,2202,-6,-5,570,1,570,374,570,1,570,438,438,1001,578,558,921,21002,0,1,-4,1006,575,959,204,-4,22101,1,-6,-6,1208,-6,51,570,1006,570,814,104,10,22101,1,-5,-5,1208,-5,51,570,1006,570,810,104,10,1206,-1,974,99,1206,-1,974,1102,1,1,575,21101,973,0,0,1106,0,786,99,109,-7,2105,1,0,109,6,21102,0,1,-4,21101,0,0,-3,203,-2,22101,1,-3,-3,21208,-2,82,-1,1205,-1,1030,21208,-2,76,-1,1205,-1,1037,21207,-2,48,-1,1205,-1,1124,22107,57,-2,-1,1205,-1,1124,21201,-2,-48,-2,1106,0,1041,21102,1,-4,-2,1105,1,1041,21101,0,-5,-2,21201,-4,1,-4,21207,-4,11,-1,1206,-1,1138,2201,-5,-4,1059,1202,-2,1,0,203,-2,22101,1,-3,-3,21207,-2,48,-1,1205,-1,1107,22107,57,-2,-1,1205,-1,1107,21201,-2,-48,-2,2201,-5,-4,1090,20102,10,0,-1,22201,-2,-1,-2,2201,-5,-4,1103,1202,-2,1,0,1106,0,1060,21208,-2,10,-1,1205,-1,1162,21208,-2,44,-1,1206,-1,1131,1105,1,989,21102,1,439,1,1105,1,1150,21101,477,0,1,1105,1,1150,21101,0,514,1,21102,1,1149,0,1106,0,579,99,21102,1,1157,0,1106,0,579,204,-2,104,10,99,21207,-3,22,-1,1206,-1,1138,2102,1,-5,1176,2101,0,-4,0,109,-6,2105,1,0,44,7,44,1,5,1,44,1,5,1,44,1,5,1,44,1,5,1,44,1,5,1,40,11,40,1,3,1,42,9,42,1,3,1,44,9,42,1,1,1,3,1,1,1,42,1,1,1,3,1,1,1,42,1,1,1,3,1,1,1,42,1,1,1,3,9,36,1,1,1,5,1,5,1,28,11,5,1,5,1,28,1,7,1,7,1,5,1,26,11,7,1,5,1,26,1,1,1,15,1,5,1,26,1,1,1,15,7,26,1,1,1,38,9,1,1,1,1,38,1,7,1,1,1,1,1,24,7,7,1,3,9,24,1,5,1,7,1,3,1,3,1,1,1,26,1,5,1,7,1,1,9,26,1,5,1,7,1,1,1,1,1,3,1,28,1,5,1,7,1,1,1,1,1,3,1,28,1,5,1,7,1,1,1,1,1,3,1,28,9,5,1,1,1,1,1,3,13,22,1,1,1,5,1,1,1,1,1,15,1,22,1,1,1,5,1,1,1,1,13,3,1,22,1,1,1,5,1,1,1,13,1,3,1,22,9,1,1,13,1,3,1,24,1,7,1,13,1,3,1,24,1,7,1,13,1,3,1,24,1,7,1,13,1,3,1,24,9,9,9,46,1,50,9,50,1,50,1,50,1,50,1,50,1,50,1,50,1,50,1,50,1,50,9,4
]

io = CameraIOManager()
d2.run_program(PROG, io)

intersections = io.camera.find_intersections()
calib = sum(i[0] * i[1] for i in intersections)
print('Alignment parameter:', calib)

path = io.camera.simple_walk()
commands = generate_commands(path)

routines = {
    'A': ['L', 8, 'R', 10, 'L', 8, 'R', 8],
    'B': ['L', 12, 'R', 8, 'R', 8],
    'C': ['L', 8, 'R', 6, 'R', 6, 'R', 10, 'L', 8],
}
main_routine = encode_routines(commands, routines)

inputs = encode_commands(main_routine)
inputs += encode_commands(routines['A'])
inputs += encode_commands(routines['B'])
inputs += encode_commands(routines['C'])
inputs += encode_commands(['n'])

PROG[0] = 2
robot = CleaningRobot(inputs)
io = RobotIOManager(robot)
#d2.run_program(PROG, io)
#print('Output:', robot.last)
