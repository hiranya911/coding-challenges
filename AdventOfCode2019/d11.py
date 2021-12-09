import d2


class Position(object):

    def __init__(self, x=0, y=0):
        self.x = x
        self.y = y

    def move(self, direction):
        x, y = self.x, self.y
        if direction == 0:
            y += 1
        elif direction == 1:
            x -= 1
        elif direction == 2:
            y -= 1
        else:
            x += 1
        return Position(x, y)
        
    def __eq__(self, other):
        return self.x == other.x and self.y == other.y

    def __hash__(self):
        return hash(self.x) + 31 * hash(self.y)

    def __str__(self):
        return f'({self.x}, {self.y})'

    def __repr__(self):
        return str(self)


class Robot(d2.InputDevice, d2.OutputDevice):

    def __init__(self):
        self.grid = {}
        self.current = Position()
        self.grid[self.current] = 1
        self.output_mode = 0
        self.direction = 0 # 0: Up, 1: Left, 2: Down, 3: Right
        self.painted = set()

    def read(self):
        return self.grid.get(self.current, 0)

    def write(self, value):
        if self.output_mode == 0:
            # Paint
            self.grid[self.current] = value
            self.output_mode = 1
            self.painted.add(self.current)
        else:
            # Move
            self._move(value)
            self.output_mode = 0

    def print_panel(self):
        min_x = min(p.x for p in self.grid.keys())
        max_x = max(p.x for p in self.grid.keys())
        min_y = min(p.y for p in self.grid.keys())
        max_y = max(p.y for p in self.grid.keys())

        for y in range(max_y, min_y - 1, -1):
            for x in range(min_x, max_x + 1):
                color = self.grid.get(Position(x, y))
                if color == 1:
                    print('*', end='')
                else:
                    print(' ', end='')
            print()
            

    def _move(self, turn):
        if turn == 0:
            self.direction = (self.direction + 1) % 4
        else:
            self.direction = (self.direction - 1) % 4
        self.current = self.current.move(self.direction)


class RobotIOManager(d2.IOManager):

    def __init__(self):
        self.robot = Robot()

    def get_input_device(self):
        return self.robot

    def get_output_device(self):
        return self.robot
        

def paint_hull(program):
    io = RobotIOManager()
    d2.run_program(program, io)
    print('Painted tiles:', len(io.robot.painted))
    print()
    io.robot.print_panel()


PAINT = [
    3,8,1005,8,299,1106,0,11,0,0,0,104,1,104,0,3,8,102,-1,8,10,101,1,10,10,4,10,108,1,8,10,4,10,102,1,8,28,1006,0,85,1,106,14,10,3,8,102,-1,8,10,1001,10,1,10,4,10,1008,8,0,10,4,10,101,0,8,58,1,1109,15,10,3,8,1002,8,-1,10,1001,10,1,10,4,10,1008,8,0,10,4,10,1002,8,1,84,3,8,102,-1,8,10,1001,10,1,10,4,10,108,1,8,10,4,10,1002,8,1,105,1006,0,48,3,8,1002,8,-1,10,1001,10,1,10,4,10,108,0,8,10,4,10,102,1,8,130,1006,0,46,1,1001,17,10,3,8,1002,8,-1,10,101,1,10,10,4,10,1008,8,0,10,4,10,1002,8,1,160,2,109,20,10,3,8,102,-1,8,10,1001,10,1,10,4,10,108,0,8,10,4,10,1002,8,1,185,3,8,102,-1,8,10,1001,10,1,10,4,10,108,1,8,10,4,10,1001,8,0,207,1006,0,89,2,1002,6,10,1,1007,0,10,3,8,102,-1,8,10,101,1,10,10,4,10,1008,8,1,10,4,10,101,0,8,241,2,4,14,10,3,8,102,-1,8,10,101,1,10,10,4,10,1008,8,1,10,4,10,101,0,8,267,1,1107,8,10,1,109,16,10,2,1107,4,10,101,1,9,9,1007,9,1003,10,1005,10,15,99,109,621,104,0,104,1,21101,0,387239486208,1,21102,316,1,0,1106,0,420,21101,0,936994976664,1,21102,327,1,0,1105,1,420,3,10,104,0,104,1,3,10,104,0,104,0,3,10,104,0,104,1,3,10,104,0,104,1,3,10,104,0,104,0,3,10,104,0,104,1,21102,1,29192457307,1,21102,1,374,0,1106,0,420,21101,0,3450965211,1,21101,0,385,0,1106,0,420,3,10,104,0,104,0,3,10,104,0,104,0,21102,1,837901103972,1,21101,408,0,0,1106,0,420,21102,867965752164,1,1,21101,0,419,0,1105,1,420,99,109,2,22102,1,-1,1,21102,40,1,2,21102,451,1,3,21102,1,441,0,1106,0,484,109,-2,2106,0,0,0,1,0,0,1,109,2,3,10,204,-1,1001,446,447,462,4,0,1001,446,1,446,108,4,446,10,1006,10,478,1102,0,1,446,109,-2,2105,1,0,0,109,4,1201,-1,0,483,1207,-3,0,10,1006,10,501,21101,0,0,-3,22101,0,-3,1,22102,1,-2,2,21101,1,0,3,21101,520,0,0,1106,0,525,109,-4,2106,0,0,109,5,1207,-3,1,10,1006,10,548,2207,-4,-2,10,1006,10,548,21201,-4,0,-4,1105,1,616,22101,0,-4,1,21201,-3,-1,2,21202,-2,2,3,21101,0,567,0,1106,0,525,22101,0,1,-4,21101,1,0,-1,2207,-4,-2,10,1006,10,586,21102,1,0,-1,22202,-2,-1,-2,2107,0,-3,10,1006,10,608,21202,-1,1,1,21102,608,1,0,106,0,483,21202,-2,-1,-2,22201,-4,-2,-4,109,-5,2105,1,0
]

paint_hull(PAINT)
