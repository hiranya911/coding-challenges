import d2


class DroneSystem(d2.InputDevice, d2.OutputDevice):

    def __init__(self, coordinates):
        self.coordinates = [i for coord in coordinates for i in coord]
        self.current = 0
        self.outputs = {}

    def read(self):
        value = self.coordinates[self.current]
        self.current += 1
        return value

    def write(self, value):
        idx = self.current
        addr = (self.coordinates[idx-2], self.coordinates[idx-1])
        self.outputs[addr] = value


class DroneIOManager(d2.IOManager):

    def __init__(self, drone):
        self.drone = drone

    def get_input_device(self):
        return self.drone

    def get_output_device(self):
        return self.drone


PROG = [
    109,424,203,1,21102,11,1,0,1106,0,282,21101,0,18,0,1106,0,259,1201,1,0,221,203,1,21102,1,31,0,1106,0,282,21101,0,38,0,1106,0,259,20102,1,23,2,21202,1,1,3,21101,1,0,1,21101,0,57,0,1105,1,303,2101,0,1,222,20101,0,221,3,21001,221,0,2,21102,1,259,1,21101,0,80,0,1105,1,225,21101,185,0,2,21102,91,1,0,1106,0,303,1202,1,1,223,21001,222,0,4,21102,259,1,3,21101,225,0,2,21102,1,225,1,21101,0,118,0,1106,0,225,20102,1,222,3,21102,1,131,2,21101,133,0,0,1106,0,303,21202,1,-1,1,22001,223,1,1,21101,148,0,0,1105,1,259,2101,0,1,223,21002,221,1,4,21002,222,1,3,21101,0,16,2,1001,132,-2,224,1002,224,2,224,1001,224,3,224,1002,132,-1,132,1,224,132,224,21001,224,1,1,21101,0,195,0,106,0,109,20207,1,223,2,20101,0,23,1,21102,1,-1,3,21101,0,214,0,1105,1,303,22101,1,1,1,204,1,99,0,0,0,0,109,5,1201,-4,0,249,22101,0,-3,1,22101,0,-2,2,21201,-1,0,3,21101,0,250,0,1106,0,225,21201,1,0,-4,109,-5,2106,0,0,109,3,22107,0,-2,-1,21202,-1,2,-1,21201,-1,-1,-1,22202,-1,-2,-2,109,-3,2106,0,0,109,3,21207,-2,0,-1,1206,-1,294,104,0,99,22102,1,-2,-2,109,-3,2105,1,0,109,5,22207,-3,-4,-1,1206,-1,346,22201,-4,-3,-4,21202,-3,-1,-1,22201,-4,-1,2,21202,2,-1,-1,22201,-4,-1,1,21201,-2,0,3,21101,343,0,0,1106,0,303,1105,1,415,22207,-2,-3,-1,1206,-1,387,22201,-3,-2,-3,21202,-2,-1,-1,22201,-3,-1,3,21202,3,-1,-1,22201,-3,-1,2,22101,0,-4,1,21102,384,1,0,1106,0,303,1105,1,415,21202,-4,-1,-4,22201,-4,-3,-4,22202,-3,-2,-2,22202,-2,-4,-4,22202,-3,-2,-3,21202,-4,-1,-2,22201,-3,-2,1,21201,1,0,-4,109,-5,2106,0,0
]


def map_tractor_beam(max_x=50, max_y=50):
    coords = [(x,y) for x in range(max_x) for y in range(max_y)]
    drone = DroneSystem(coords)
    io = DroneIOManager(drone)
    for _ in range(len(coords)):
        d2.run_program(PROG, io)
    return drone.outputs


COUNT = 0

def is_effective(x, y):
    global COUNT
    COUNT += 1
    coords = [(x,y)]
    drone = DroneSystem(coords)
    d2.run_program(PROG, DroneIOManager(drone))
    return drone.outputs[(x,y)]


def find_start(y, last):
    x = last
    while x - last < 20:
        if is_effective(x, y):
            return (x, y)
        x += 1
    return -1, -1


def find_end(x, y):
    increment = 100
    while True:
        x += increment
        if not is_effective(x, y):
            if increment == 1:
                return (x - 1, y)
            x -= increment
            increment //= 10


def can_enclose(x, y, size=100):
    y_max = y + size - 1
    x_min = x - size + 1
    total = 0
    for row in range(y_max, y - 1, -1):
        if not is_effective(x_min, row) or not is_effective(x, row):
            return False

    return True


def check_row(y):
    total = 0
    for x in range(0, 1000):
        if is_effective(x, y):
            print(x, y)
            total += 1
    return total
            
            
def find_min_distance():
    y = 0
    last = 0
    size = 100
    while True:
        start_x, _ = find_start(y, last)
        if start_x < 0:
            y += 1
            continue

        last = start_x
        print(last)
        if y < 99:
            y += 1
            continue
        
        end_x, _= find_end(start_x, y)
        if end_x - start_x + 1 < size:
            y += 1
            continue

        if can_enclose(end_x, y, size):
            x_result = end_x - size + 1
            print('Found', x_result, y)
            print(x_result * 10000 + y)
            break
        
        y += 1


def fast_find_end(lend, ly, y):
    diff = y - ly
    end = None
    for x in range(lend, lend + diff + 2):
        if is_effective(x, y):
            end = x
        elif end is not None:
            return end
    return -1


def find_min_distance2():
    lend, ly = 0, 0
    y = 0
    while True:
        y += 1
        end = fast_find_end(lend, ly, y)
        if end == -1:
            continue

        lend = end
        ly = y

        if end < 99:
            continue

        if can_enclose(end, y):
            xr = end - 100 + 1
            print('Done:', xr * 10000 + y)
            return
        

def draw_map():
    cells = map_tractor_beam()
    for y in range(50):
        print(y, end='')
        for x in range(50):
            val = cells[(x, y)]
            if val == 0:
                print('.', end='')
            else:
                print('#', end='')
        print()


find_min_distance2()
print(COUNT)
