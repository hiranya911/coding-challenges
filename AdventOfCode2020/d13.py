import math


def parse_input(path):
    with open(path) as fp:
        lines = fp.readlines()
        buses = lines[1].split(',')
        return int(lines[0]), [ int(bus) for bus in buses if bus != 'x' ]


def find_earliest(start, buses):
    while True:
        candidates = [bus for bus in buses if start % bus == 0]
        if candidates:
            return candidates[0], start
        start += 1


def parse_bus_ids(path):
    with open(path) as fp:
        lines = fp.readlines()
        buses = []
        for bus in lines[1].split(','):
            if bus != 'x':
                buses.append(int(bus))
            else:
                buses.append(-1)
    return [get_modulo_equaltion((bus, idx)) for idx, bus in enumerate(buses) if bus != -1]


# https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm
def bezout(a, b):
    old_r, r = a, b
    old_s, s = 1, 0
    old_t, t = 0, 1
    while r != 0:
        q = old_r // r
        old_r, r = r, old_r - q * r
        old_s, s = s, old_s - q * s
        old_t, t = t, old_t - q * t
    return old_s, old_t


def get_modulo_equaltion(bus_with_index):
    mod, diff = bus_with_index
    return mod, (-1 * diff) % mod


# https://math.stackexchange.com/questions/2612036/how-to-combine-congruences
# https://en.wikipedia.org/wiki/Chinese_remainder_theorem
def find_magic_time(equations):
    current_mod, current_val = equations[0]
    print(f't = {current_val} mod {current_mod}')
    idx = 1
    while idx < len(equations):
        next_mod, next_val = equations[idx]
        print(f't = {next_val} mod {next_mod}')
        u, v = bezout(next_mod, current_mod)
        temp = current_val * u * next_mod + next_val * v * current_mod
        current_mod *= next_mod
        current_val = temp % current_mod # Solution = Temp Mod N (pick the smallest positive solution)
        idx += 1
    return current_val


if __name__ == '__main__':
    input_file = 'data/d13_input1_large.txt'
    start, buses = parse_input(input_file)
    bus, earliest = find_earliest(start, buses)
    print(f'Bus = {bus}, Wait time = {earliest - start}')
    print(f'Bus x Wait = {bus * (earliest - start)}')

    print()
    buses = parse_bus_ids(input_file)
    print('Magic start time =', find_magic_time(buses))
