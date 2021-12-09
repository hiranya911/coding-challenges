import itertools
import threading

import d2


class FixedInputDevice(d2.InputDevice):

    def __init__(self, value):
        self.value = value

    def read(self):
        return self.value

    def __str__(self):
        return f'Fixed[{self.value}]'

    def __repr__(self):
        return str(self)


class FixedOutputDevice(d2.OutputDevice):

    def write(self, value):
        self.value = value


class PipedInputOutputDevice(d2.InputDevice, d2.OutputDevice):

    def __init__(self):
        self.ready = threading.Condition()
        self.value = []

    def read(self):
        with self.ready:
            while not self.value:
                self.ready.wait()
            value = self.value.pop()
            self.ready.notify()
            return value

    def write(self, value):
        with self.ready:
            while self.value:
                self.ready.wait()
            self.value.append(value)
            self.ready.notify()


class Amplifier(threading.Thread):

    def __init__(self, prog, iomanager):
        super().__init__()
        self.prog = [p for p in prog]
        self.iomanager = iomanager

    def run(self):
        d2.run_program(self.prog, self.iomanager)


class AmpIOManager(d2.IOManager):

    def __init__(self, phase_setting, in_pipe, out_pipe):
        self.initial = FixedInputDevice(phase_setting)
        self.second = in_pipe
        self.output = out_pipe
        self.count = 0

    def get_input_device(self):
        self.count += 1
        if self.count == 1:
            return self.initial
        return self.second

    def get_output_device(self):
        return self.output
        

def run_pipeline(prog, phases):
    pipes = [PipedInputOutputDevice() for _ in range(0, len(phases))]
    amps = []
    for i in range(0, len(phases)):
        iomanager = AmpIOManager(phases[i], pipes[i], pipes[(i+1)%len(phases)])
        amps.append(Amplifier(prog, iomanager))

    pipes[-1].write(0)
    for amp in amps:
        amp.start()
    for amp in amps:
        amp.join()
    return pipes[-1].read()


def find_max_setting(prog, amps=5, feedback=False):
    start = 0
    if feedback:
        start = 5
    phases = [i for i in range(start, start + amps)]
    perms = [perm for perm in itertools.permutations(phases)]
    return max([run_pipeline(prog, perm) for perm in perms])


sample1 = [3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0]
sample2 = [3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0]
sample3 = [3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5]

prod = [
    3,8,1001,8,10,8,105,1,0,0,21,34,51,76,101,126,207,288,369,450,99999,3,9,102,4,9,9,1001,9,2,9,4,9,99,3,9,1001,9,2,9,1002,9,3,9,101,3,9,9,4,9,99,3,9,102,5,9,9,1001,9,2,9,102,2,9,9,101,3,9,9,1002,9,2,9,4,9,99,3,9,101,5,9,9,102,5,9,9,1001,9,2,9,102,3,9,9,1001,9,3,9,4,9,99,3,9,101,2,9,9,1002,9,5,9,1001,9,5,9,1002,9,4,9,101,5,9,9,4,9,99,3,9,1002,9,2,9,4,9,3,9,102,2,9,9,4,9,3,9,1001,9,1,9,4,9,3,9,102,2,9,9,4,9,3,9,1001,9,1,9,4,9,3,9,101,1,9,9,4,9,3,9,1001,9,2,9,4,9,3,9,1002,9,2,9,4,9,3,9,1001,9,1,9,4,9,3,9,1001,9,1,9,4,9,99,3,9,102,2,9,9,4,9,3,9,101,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,1002,9,2,9,4,9,3,9,1001,9,2,9,4,9,3,9,101,1,9,9,4,9,3,9,1001,9,2,9,4,9,3,9,102,2,9,9,4,9,3,9,101,1,9,9,4,9,3,9,102,2,9,9,4,9,99,3,9,101,2,9,9,4,9,3,9,101,2,9,9,4,9,3,9,101,1,9,9,4,9,3,9,1001,9,2,9,4,9,3,9,101,2,9,9,4,9,3,9,101,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,1001,9,2,9,4,9,3,9,1001,9,1,9,4,9,99,3,9,1001,9,2,9,4,9,3,9,101,1,9,9,4,9,3,9,102,2,9,9,4,9,3,9,1001,9,1,9,4,9,3,9,101,1,9,9,4,9,3,9,101,2,9,9,4,9,3,9,1001,9,1,9,4,9,3,9,101,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,99,3,9,1001,9,2,9,4,9,3,9,102,2,9,9,4,9,3,9,1002,9,2,9,4,9,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,1001,9,1,9,4,9,3,9,101,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,99
]

print(find_max_setting(prod))
print(find_max_setting(prod, feedback=True))
