class DockingProgram:

    def __init__(self, version='v1'):
        self.memory = {}
        self.version = version

    def execute(self, instruction):
        instruction.execute(self)


class BitMask:

    def __init__(self, mask_string):
        self.or_mask = int(mask_string.replace('X', '0'), 2)
        self.and_mask = int(mask_string.replace('X', '1'), 2)

    def apply(self, arg):
        return (arg | self.or_mask) & self.and_mask


class BitMaskV2:

    def __init__(self, mask_string):
        self.or_mask = int(mask_string.replace('X', '0'), 2)
        self.and_mask = int(mask_string.replace('0', '1').replace('X', '0'), 2)
        self.floaters = sorted([35 - i for i, ch in enumerate(mask_string) if ch == 'X'])

    def apply(self, arg):
        base_value = (arg | self.or_mask) & self.and_mask
        results = [base_value, base_value + (1 << self.floaters[0])]
        for pos in self.floaters[1:]:
            temp = [val + (1 << pos) for val in results]
            results += temp
        return results


class Instruction:

    def execute(self, program):
        pass

    @staticmethod
    def parse(cmd):
        if cmd.startswith('mask = '):
            return SetBitMask(cmd)
        if cmd.startswith('mem['):
            return SetMem(cmd)
        raise ValueError(f'Unsupported command: {cmd}')


class SetBitMask(Instruction):

    def __init__(self, cmd):
        self.segments = cmd.split()

    def execute(self, program):
        if program.version == 'v2':
            program.bit_mask = BitMaskV2(self.segments[2])
        else:
            program.bit_mask = BitMask(self.segments[2])


class SetMem(Instruction):

    def __init__(self, cmd):
        lhs, _, rhs = cmd.split()
        self.addr = int(lhs[4:-1])
        self.value = int(rhs)

    def execute(self, program):
        if program.version == 'v2':
            addresses = program.bit_mask.apply(self.addr)
            for addr in addresses:
                program.memory[addr] = self.value
        else:
            program.memory[self.addr] = program.bit_mask.apply(self.value)


def parse_instructions(path):
    with open(path) as fp:
        return [Instruction.parse(line.strip()) for line in fp.readlines()]


if __name__ == '__main__':
    instructions = parse_instructions('data/d14_input1_large.txt')
    prog = DockingProgram(version='v2')
    for inst in instructions:
        prog.execute(inst)

    print(f'Sum = {sum(prog.memory.values())}')
