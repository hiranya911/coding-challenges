import numpy as np

BASE = [0, 1, 0, -1]

# (N % N*4) // P

class FactorGenerator(object):

    def __init__(self, pos):
        self.pos = pos
        self.index = 0
        self.repeats = 0

    def __next__(self):
        value = BASE[self.index]
        self.repeats += 1
        if self.repeats == self.pos:
            self.repeats = 0
            self.index = (self.index + 1) % len(BASE)
        return value


MULTIPLIER_CACHE = {}
MULTS = None


def calculate_multipliers(inputs):
    global MULTS
    if MULTS is not None:
        return MULTS

    print('Calculating multipliers...')
    rows = []
    for i in range(0, len(inputs)):
        gen = FactorGenerator(i+1)
        next(gen)
        row = [next(gen) for _ in range(len(inputs))]
        rows.append(row)
    MULTS = np.array(rows)
    print('Done...')
    return MULTS
    
    
def calculate_phase(inputs):
    output = []
    mults = calculate_multipliers(inputs)
    digits = np.sum(inputs * mults, axis=1)
    return np.array([abs(i) % 10 for i in digits])


def list_to_str(lst):
    return ''.join([str(i) for i in lst])


def fft(inputs, phases):
    for i in range(phases):
        inputs = calculate_phase(inputs)
        print(f'Finished phase {i+1}')
    return inputs

# https://work.njae.me.uk/2019/12/20/advent-of-code-2019-day-16/
def fast_fft(inputs, phases, start):
    inputs = inputs[start:]
    for _ in range(phases):
        output = []
        total = 0
        for pos in range(len(inputs) - 1, -1, -1):
            total += inputs[pos]
            val = total % 10
            output.append(val)

        inputs = output[::-1]
    return inputs


def str_to_list(s):
    return [int(ch) for ch in s]
    

sample = '80871224585914546619083218645595'
prod = '59731816011884092945351508129673371014862103878684944826017645844741545300230138932831133873839512146713127268759974246245502075014905070039532876129205215417851534077861438833829150700128859789264910166202535524896960863759734991379392200570075995540154404564759515739872348617947354357737896622983395480822393561314056840468397927687908512181180566958267371679145705350771757054349846320639601111983284494477902984330803048219450650034662420834263425046219982608792077128250835515865313986075722145069152768623913680721193045475863879571787112159970381407518157406924221437152946039000886837781446203456224983154446561285113664381711600293030463013'

offset = int(prod[0:7])

prod = prod * 10000
lst = str_to_list(prod)
output = fast_fft(lst, 100, offset)
print(output[0:8])
