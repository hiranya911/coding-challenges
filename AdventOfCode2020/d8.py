class Instruction:

    @staticmethod
    def parse(line):
        opcode, arg = line.strip().split()
        if opcode == 'acc':
            return Acc(int(arg))
        if opcode == 'jmp':
            return Jmp(int(arg))
        if opcode == 'nop':
            return Nop(int(arg))
        raise ValueError(f'Unrecognized instruction {line}')

    def run(self, console):
        pass


class Acc(Instruction):

    def __init__(self, arg):
        self.arg = arg

    def run(self, console):
        console.accumulator += self.arg
        console.pc += 1


class Nop(Instruction):

    def __init__(self, arg):
        self.arg = arg

    def run(self, console):
        console.pc += 1

    def flip(self):
        return Jmp(self.arg)


class Jmp(Instruction):

    def __init__(self, offset):
        self.offset = offset

    def run(self, console):
        console.pc += self.offset

    def flip(self):
        return Nop(self.offset)


class GameConsole:

    def execute(self, instructions):
        self.pc = 0
        self.accumulator = 0
        executed = set()
        while True:
            if self.pc == len(instructions):
                return True
            if self.pc in executed:
                return False
            executed.add(self.pc)
            current = instructions[self.pc]
            current.run(self)
        return True


if __name__ == '__main__':
    with open('data/d8_input1_large.txt') as fp:
        lines = fp.readlines()

    instructions = [ Instruction.parse(line) for line in lines ]
    gc = GameConsole()
    gc.execute(instructions)
    print(f'Acc = {gc.accumulator}')

    for idx, inst in enumerate(instructions):
        if not isinstance(inst, (Jmp, Nop)):
            continue
        fixed = instructions[:idx] + [inst.flip()] + instructions[idx+1:]
        if gc.execute(fixed):
            print('Program terminated successfully')
            print(f'Acc = {gc.accumulator}')
            break
