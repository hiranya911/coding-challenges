import math

class Compound(object):

    def __init__(self, name, qty):
        self.name = name
        self.qty = qty

    @classmethod
    def from_string(cls, s):
        qty, name = s.split(' ')
        return cls(name, int(qty))


class Formula(object):

    def __init__(self, lhs, rhs):
        self.lhs = lhs
        self.rhs = rhs

    @property
    def base_element(self):
        return len(self.lhs) == 1 and self.lhs[0].name == 'ORE'

    @classmethod
    def from_string(cls, s):
        lhs, rhs = s.split(' => ')
        inputs = [Compound.from_string(s) for s in lhs.split(', ')]
        output = Compound.from_string(rhs)
        return cls(inputs, output)


def formula_for(formulas, result):
    filtered = [f for f in formulas if f.rhs.name == result]
    return filtered[0]
    

def find_ore(formulas):
    inputs = {}
    fuel_formula = formula_for(formulas, 'FUEL')
    for item in fuel_formula.lhs:
        qty = item.qty
        inputs[item.name] = item.qty
    return reduce_to_ore(formulas, inputs)

def reduce_to_ore(formulas, inputs):
    extras = {}
    while len(inputs) > 1 or 'ORE' not in inputs:
        temp = {}
        delete = []
        for k, v in inputs.items():
            if k == 'ORE' or v == 0:
                continue
            reaction = formula_for(formulas, k)
            ratio = v / reaction.rhs.qty # Should take v - extras[k] here
            round_up = math.ceil(ratio)
            for item in reaction.lhs:
                qty = temp.get(item.name, 0)
                qty += item.qty * round_up
                if item.name in extras:
                    pre = extras[item.name]
                    if pre <= qty:
                        qty -= pre
                        del extras[item.name]
                    else:
                        qty = 0
                        extras[item.name] = pre - qty
                temp[item.name] = qty
            extras[reaction.rhs.name] = extras.get(reaction.rhs.name, 0) + reaction.rhs.qty * round_up - v
            delete.append(k)

        delete += [k for k, v in inputs.items() if v == 0]
        for k in delete:
            del inputs[k]

        for k, v in temp.items():
            inputs[k] = inputs.get(k, 0) + v

        if len(inputs) == 0:
            raise Exception()

    return inputs['ORE'], {k: v for k, v in extras.items() if v > 0}


def produce(formulas, name, qty, inputs={}):
    formula = formula_for(formulas, name)
    curr = inputs.get(name, 0)
    ratio = math.ceil(max(qty - curr, 0) / formula.rhs.qty)
    extra = formula.rhs.qty * ratio + curr - qty
    if name != 'ORE':
        inputs[name] = extra

    ore = 0
    for c in formula.lhs:
        if c.name == 'ORE':
            ore += c.qty * ratio
        else:
            ore += produce(formulas, c.name, c.qty * ratio, inputs)
    return ore
    


def parse_formulas(path):
    with open(path) as fp:
        lines = fp.readlines()

    return [Formula.from_string(line.strip()) for line in lines]


formulas = parse_formulas('fuel_prod.txt')
ore_needed = produce(formulas, 'FUEL', 1)
print('ORE per 1 FUEL = ', ore_needed)

total_ore = 1000000000000
fuel_estimate = total_ore // ore_needed
inc = 10000
while True:
    print(f'Checking {fuel_estimate}')
    ore_needed = produce(formulas, 'FUEL', fuel_estimate)
    if ore_needed > total_ore:
        if inc == 1:
            break
        fuel_estimate -= inc
        inc = inc // 10
        print(f'Dropping inc to {inc}')
    else:
        fuel_estimate += inc

print('Max FUEL = ', fuel_estimate - 1)
