import sys


class Instruction(object):

    def __init__(self, prog):
        self.prog = prog

    def get_parameter(self, i):
        return self.prog.get_parameter(i)


class Add(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        p1 = self.get_parameter(1)
        p2 = self.get_parameter(2)
        p3 = self.prog.get_addr(3)
        self.prog.set(p3, p1 + p2)
        self.prog.pc += 4
        return True


class Multiply(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        p1 = self.get_parameter(1)
        p2 = self.get_parameter(2)
        p3 = self.prog.get_addr(3)
        self.prog.set(p3, p1 * p2)
        self.prog.pc += 4
        return True


class Halt(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        self.prog.pc += 1
        return False


class InputDevice(object):

    def read(self):
        raise NotImplementedError()


class CommandlineInputDevice(InputDevice):

    def read(self):
        return int(input('Input: '))


class IOManager(object):

    def get_input_device(self):
        return CommandlineInputDevice()

    def get_output_device(self):
        return CommandlineOutputDevice()
    

class Input(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        device = self.prog.iomanager.get_input_device()
        value = device.read()
        p1 = self.prog.get_addr(1)
        self.prog.set(p1, value)
        self.prog.pc += 2
        return True


class OutputDevice(object):

    def write(self, value):
        raise NotImplementedError()


class CommandlineOutputDevice(OutputDevice):

    def write(self, value):
        print(f'Output: {value}')
    
    
class Output(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        p1 = self.get_parameter(1)
        device = self.prog.iomanager.get_output_device()
        device.write(p1)
        self.prog.pc += 2
        return True
    

class JumpIfTrue(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        p1 = self.get_parameter(1)
        p2 = self.get_parameter(2)
        if p1 != 0:
            self.prog.pc = p2
        else:
            self.prog.pc += 3
        return True


class JumpIfFalse(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        p1 = self.get_parameter(1)
        p2 = self.get_parameter(2)
        if p1 == 0:
            self.prog.pc = p2
        else:
            self.prog.pc += 3
        return True


class LessThan(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        p1 = self.get_parameter(1)
        p2 = self.get_parameter(2)
        p3 = self.prog.get_addr(3)
        if p1 < p2:
            self.prog.set(p3, 1)
        else:
            self.prog.set(p3, 0)
        self.prog.pc += 4
        return True


class Equals(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        p1 = self.get_parameter(1)
        p2 = self.get_parameter(2)
        p3 = self.prog.get_addr(3)
        if p1 == p2:
            self.prog.set(p3, 1)
        else:
            self.prog.set(p3, 0)
        self.prog.pc += 4
        return True


class AdjustRelativeBase(Instruction):

    def __init__(self, prog):
        super().__init__(prog)

    def run(self):
        p1 = self.get_parameter(1)
        self.prog.rbase += p1
        self.prog.pc += 2
        return True


class Program(object):

    def __init__(self, prog, iomanager=IOManager()):
        self.prog = [p for p in prog]
        self.pc = 0
        self.rbase = 0
        self.iomanager = iomanager
        self.extra_mem = {}

    @property
    def current(self):
        return self.prog[self.pc]

    def get(self, addr):
        if addr < len(self.prog):
            return self.prog[addr]
        return self.extra_mem.get(addr, 0)

    def set(self, addr, value):
        if addr < len(self.prog):
            self.prog[addr] = value
        else:
            self.extra_mem[addr] = value

    def get_parameter(self, i):
        value = self.prog[self.pc + i]
        mode = self._get_parameter_mode(i)
        if mode == 0:
            return self.get(value)
        elif mode == 2:
            return self.get(self.rbase + value)
        return value

    def get_addr(self, i):
        value = self.prog[self.pc + i]
        mode = self._get_parameter_mode(i)
        if mode == 2:
            return self.rbase + value
        return value
    
    def _get_parameter_mode(self, i):
        pos = 10 ** (i+1)
        return self.current // pos % 10
        
    def execute(self):
        while True:
            inst = self.decode()
            if not inst.run():
                return    

    def decode(self):
        opcode = self.current
        ones = opcode % 10
        tens = opcode // 10 % 10
        opcode = ones + tens * 10
        if opcode == 1:
            return Add(self)
        elif opcode == 2:
            return Multiply(self)
        elif opcode == 3:
            return Input(self)
        elif opcode == 4:
            return Output(self)
        elif opcode == 5:
            return JumpIfTrue(self)
        elif opcode == 6:
            return JumpIfFalse(self)
        elif opcode == 7:
            return LessThan(self)
        elif opcode == 8:
            return Equals(self)
        elif opcode == 9:
            return AdjustRelativeBase(self)
        elif opcode == 99:
            return Halt(self)
        else:
            raise Exception('Invalid opcode')


def run_program(prog, iomanager=IOManager()):
    p = Program(prog, iomanager)
    p.execute()
    return p.prog


def grav_assist_prog(prog, noun, verb):
    copy = [i for i in prog]
    copy[1], copy[2] = noun, verb
    return run_program(copy)[0]


if __name__ == '__main__':
    grav_assist = [
        1,0,0,3,1,1,2,3,1,3,4,3,1,5,0,3,2,1,9,19,1,19,5,23,2,6,23,27,1,6,27,31,2,31,9,35,1,35,
        6,39,1,10,39,43,2,9,43,47,1,5,47,51,2,51,6,55,1,5,55,59,2,13,59,63,1,63,5,67,2,67,13,71,
        1,71,9,75,1,75,6,79,2,79,6,83,1,83,5,87,2,87,9,91,2,9,91,95,1,5,95,99,2,99,13,103,1,103,
        5,107,1,2,107,111,1,111,5,0,99,2,14,0,0,
    ]

    print('First run output:', grav_assist_prog(grav_assist, 12, 2))
    print()

    answer = 19690720
    for noun in range(0, 100):
        for verb in range(0, 100):
            if grav_assist_prog(grav_assist, noun, verb) == answer:
                print(f'Noun: {noun}; Verb: {verb}')
                print(f'Final answer: {100 * noun + verb}')
                sys.exit(0)
