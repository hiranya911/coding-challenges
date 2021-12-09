def tokenize(text):
    tokens = []
    number = ''
    for ch in text:
        if ch == ' ':
            continue

        if ch.isdigit():
            number += ch
            continue

        if number:
            tokens.append(int(number))
            number = ''

        if ch in ('+', '*', '(', ')'):
            tokens.append(ch)
        else:
            raise ValueError(f'Syntax error: {ch}')

    if number:
        tokens.append(int(number))

    return tokens


class Number:

    def __init__(self, value):
        self.value = value

    def evaluate(self):
        return self.value


class Add:

    def __init__(self, lhs, rhs):
        if not lhs or not rhs:
            raise ValueError(f'Parse error: {lhs} {rhs}')
        self.lhs = lhs
        self.rhs = rhs

    def evaluate(self):
        return self.lhs.evaluate() + self.rhs.evaluate()


class Multiply:

    def __init__(self, lhs, rhs):
        if not lhs or not rhs:
            raise ValueError(f'Parse error: {lhs} {rhs}')
        self.lhs = lhs
        self.rhs = rhs

    def evaluate(self):
        return self.lhs.evaluate() * self.rhs.evaluate()


def next_expression(tokens):
    current = tokens.pop(0)
    if isinstance(current, int):
        return Number(current)
    elif current == '(':
        return parse(tokens)

    raise ValueError('Failed to extract expression')


def parse(tokens):
    tree = None
    while tokens:
        if tree is None:
            lhs = next_expression(tokens)
        else:
            lhs = tree

        operator = tokens.pop(0)
        if operator == '+':
            rhs = next_expression(tokens)
            tree = Add(lhs, rhs)
        elif operator == '*':
            rhs = next_expression(tokens)
            tree = Multiply(lhs, rhs)
        elif operator == ')':
            return tree
        else:
            raise ValueError('Unknown operator')

    return tree


def next_expression2(tokens):
    tree = None
    while tokens:
        if isinstance(tokens[0], int):
            tree = Number(tokens.pop(0))
        elif tokens[0] == '+':
            tokens.pop(0)
            lhs = tree
            rhs = next_expression2(tokens)
            tree = Add(lhs, rhs)
        elif tokens[0] == '(':
            tokens.pop(0)
            tree = parse2(tokens)
        else:
            break

    return tree


def parse2(tokens):
    tree = None
    while tokens:
        if tree is None:
            tree = next_expression2(tokens)

        lhs = tree
        if not tokens:
            break
        current = tokens.pop(0)
        if current == '*':
            rhs = next_expression2(tokens)
            tree = Multiply(lhs, rhs)
        elif current == ')':
            return tree
        else:
            raise ValueError('Unknown operator', current)
    return tree


def evaluate(expr):
    return parse(tokenize(expr)).evaluate()


def evaluate2(expr):
    return parse2(tokenize(expr)).evaluate()


if __name__ == '__main__':
    # print(evaluate2('((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2'))
    with open('data/d18_input_large.txt') as fp:
        lines = fp.readlines()
        total = 0
        for idx, line in enumerate(lines):
            #print(idx + 1)
            total += evaluate2(line.strip())

        print(f'Total = {total}')
