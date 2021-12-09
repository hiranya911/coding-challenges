import d2
import spring_droid


class SpringDroidCLI(d2.InputDevice, d2.OutputDevice):

    def __init__(self, inputs):
        self.inputs = inputs
        self.index = 0

    def read(self):
        val = self.inputs[self.index]
        self.index += 1
        return ord(val)

    def write(self, value):
        try:
            print(chr(value), end='')
        except ValueError:
            print('Output:', value)


class SimpleIOManager(d2.IOManager):

    def __init__(self, device):
        self.device = device

    def get_input_device(self):
        return self.device

    def get_output_device(self):
        return self.device


inputs = [
    'NOT A T\n'
    'NOT C J\n'
    'OR T J\n'
    'AND D J\n'
    'WALK\n'
]

inputs = [
    'NOT A J\n' # if not A
    
    'NOT C T\n'
    'AND D T\n'
    'AND H T \n'
    'OR T J\n' # if not C and D and H

    'NOT B T\n'
    'AND D T\n'
    'OR T J\n' # if not B and D
    
    'RUN\n'
]

if len(inputs) > 15:
    raise Exception('Program too long')
droid = SpringDroidCLI(''.join(inputs))
io = SimpleIOManager(droid)
d2.run_program(spring_droid.INPUT, io)

