import functools


class Food:

    def __init__(self, ingredients, allergens):
        self.ingredients = ingredients
        self.allergens = allergens

    def get_ruleset(self):
        return {allergen: set(self.ingredients) for allergen in self.allergens}

    @staticmethod
    def create(line):
        sep = line.index('(')
        section1, section2 = line, ''
        if sep != -1:
            section1 = line[:sep].strip()
            section2 = line[sep+10:-1]
        ingredients = set(section1.split())
        allergens = set()
        if section2:
            allergens = section2.split(', ')
        return Food(ingredients, allergens)

    def __repr__(self):
        return f'{self.ingredients} --> {self.allergens}'


def parse_input(path):
    with open(path) as fp:
        return [Food.create(line.strip()) for line in fp.readlines()]


def combine_rules(food):
    global_rules = {}
    for f in food:
        rules = f.get_ruleset()
        for key, val in rules.items():
            if key not in global_rules:
                global_rules[key] = val
            else:
                global_rules[key] = global_rules[key].intersection(val)
    return global_rules


def find_non_allergic(global_rules, all_ingredients):
    allergic_ingredients = functools.reduce(lambda x, y: x.union(y), [v for v in global_rules.values()])
    non_allergic = all_ingredients - allergic_ingredients
    print('Allergic ingredients:', allergic_ingredients)
    print('Non-allergic ingredients:', non_allergic)
    return non_allergic


def count_safe(food, non_allergic):
    total = 0
    for f in food:
        for i in f.ingredients:
            if i in non_allergic:
                total += 1
    return total


def reduce_rules(global_rules):
    while True:
        # Mark
        marked = []
        for k, v in global_rules.items():
            if len(v) == 1:
                marked.append((k, list(v)[0]))

        # Sweep
        found = False
        for allergen, ingredient in marked:
            for k, v in global_rules.items():
                if k != allergen and ingredient in v:
                    v.remove(ingredient)
                    found = True

        if not found:
            break


def canonical_dangerous_ingredients(global_rules):
    reduce_rules(global_rules)
    ingredients = []
    for k in sorted(global_rules.keys()):
        ingredients.append(global_rules[k].pop())
    return ','.join(ingredients)


if __name__ == '__main__':
    food = parse_input('data/d21_input1_large.txt')
    all_ingredients = functools.reduce(lambda x, y: x.union(y), [f.ingredients for f in food])
    print('All ingredients:', all_ingredients)

    global_rules = combine_rules(food)
    non_allergic = find_non_allergic(global_rules, all_ingredients)
    print('Non allergic occurances:', count_safe(food, non_allergic))

    print(canonical_dangerous_ingredients(global_rules))
