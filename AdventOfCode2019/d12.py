import numpy as np


class Moon(object):

    def __init__(self, pos):
        self.pos = pos
        self.vel = [0, 0, 0]

    @classmethod
    def from_string(cls, s):
        s = s[1:-1]
        coords = s.split(', ')
        pos = [int(coord.split('=')[1]) for coord in coords]
        return cls(pos)

    @property
    def potential_energy(self):
        return sum([abs(p) for p in self.pos])

    @property
    def kinetic_energy(self):
        return sum([abs(v) for v in self.vel])

    @property
    def total_energy(self):
        return self.potential_energy * self.kinetic_energy

    def apply_gravity(self, other):
        for i in range(0, len(self.pos)):
            if self.pos[i] < other.pos[i]:
                self.vel[i] += 1
                other.vel[i] -= 1
            elif self.pos[i] > other.pos[i]:
                self.vel[i] -= 1
                other.vel[i] += 1

    def axis(self, idx):
        return (self.pos[idx], self.vel[idx])

    def apply_velocity(self):
        for i in range(0, len(self.pos)):
            self.pos[i] += self.vel[i]

    def pos_to_str(self):
        return f'<x={self.pos[0]}, y={self.pos[1]}, z={self.pos[2]}>'

    def vel_to_str(self):
        return f'<x={self.vel[0]}, y={self.vel[1]}, z={self.vel[2]}>'

    def __str__(self):
        return f'pos={self.pos_to_str()}, vel={self.vel_to_str()}'

    def __hash__(self):
        signature = tuple(self.pos) + tuple(self.vel)
        return hash(signature)

    def to_array(self):
        return self.pos + self.vel


def simulate(moons, steps):
    for step in range(0, steps):
        for i in range(0, len(moons)):
            for j in range(i+1, len(moons)):
                m1, m2 = moons[i], moons[j]
                m1.apply_gravity(m2)

        for moon in moons:
            moon.apply_velocity()

    return sum([moon.total_energy for moon in moons])


def simulate_step(moons):
    for i in range(0, len(moons)):
        for j in range(i+1, len(moons)):
            m1, m2 = moons[i], moons[j]
            m1.apply_gravity(m2)

    for moon in moons:
        moon.apply_velocity()


def hash_moons(moons):
    hs = tuple(hash(moon) for moon in moons)
    return hash(hs)


def total_energy(moons):
    return sum(moon.total_energy for moon in moons)


def scan_moons(path):
    with open(path) as fp:
        lines = fp.readlines()
    return [Moon.from_string(s.strip()) for s in lines]


def axis_state(moons, axis):
    tuples = [m.axis(axis) for m in moons]
    result = tuples[0]
    for i in range(1, len(tuples)):
        result = result + tuples[i]
    return result


def gcd(a, b):
    return b if a == 0 else gcd(b % a, a)


# https://dev.to/jbristow/advent-of-code-2019-solution-megathread-day-12-the-n-body-problem-5h3b
def simulate_until_reset(moons):
    periods = []
    global_period = 1
    for axis in (0, 1, 2):
        steps = 0
        states = set()
        while True:
            state = axis_state(moons, axis)
            if state in states:
                periods.append(steps)
                global_period = global_period * steps // gcd(global_period, steps)
                print(f'Found period {steps}')
                break
            states.add(state)
            simulate_step(moons)
            steps += 1

    print(f'Global period: {global_period}')
    return periods



sample = 'moons_small.txt'
prod = 'moons_prod.txt'

moons = scan_moons(prod)
#energy = simulate(moons, 1000)
#print(f'Total energy: {energy}')

simulate_until_reset(moons)
