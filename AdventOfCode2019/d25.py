import d2
import explore_ship


class Drone(d2.InputDevice, d2.OutputDevice):

    def __init__(self):
        self.value = ''
        self.idx = 0
        

    def read(self):
        if self.idx == len(self.value):
            self.value = input('> ')
            self.value += '\n'
            self.idx = 0

        ch = self.value[self.idx]
        self.idx += 1
        return ord(ch)

    def write(self, value):
        print(chr(value), end='')



class DroneIOManager(d2.IOManager):

    def __init__(self):
        self.drone = Drone()

    def get_input_device(self):
        return self.drone

    def get_output_device(self):
        return self.drone


def explore():
    io = DroneIOManager()
    d2.run_program(explore_ship.INPUT, io)



explore()
