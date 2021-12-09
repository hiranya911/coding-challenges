def deal_into_new_stack(stack):
    return stack[::-1]


def deal_new(total, pos):
    # Y = (-1X - 1) % TOTAL
    return total - 1 - pos


def reverse_deal_new(total, a, b):
    return -a, total - 1 - b


def cut(stack, n):
    return stack[n:] + stack[0:n]


def cut2(total, pos, n):
    # Y = (1X - N) % TOTAL
    return (pos - n) % total


def reverse_cut(total, a, b, n):
    return a, (b + n) % total


def deal_with_increment(stack, n):
    total = len(stack)
    result = [0 for i in range(total)]
    for i, v in enumerate(stack):
        result[(i * n) % total] = v
    return result


def deal_inc(total, pos, n):
    # Y = (NX + 0) % TOTAL
    return (pos * n) % total


def reverse_deal_inc(total, a, b, n):
    # Y = NX % TOTAL
    # X = Y * MODINV(N, TOTAL) ??
    # Fermat's little theorem
    z = pow(n, total - 2, total) # mod inverse for prime total
    return a*z % total, b*z % total


def shuffle(deck, steps):
    for step in steps:
        if step == 'deal into new stack':
            deck = deal_into_new_stack(deck)
        elif step.startswith('cut '):
            n = int(step.split(' ')[-1])
            deck = cut(deck, n)
        elif step.startswith('deal with increment '):
            n = int(step.split(' ')[-1])
            deck = deal_with_increment(deck, n)
        else:
            raise Exception()

    return deck


def shuffle2(total, pos, steps):
    for step in steps:
        if step == 'deal into new stack':
            pos = deal_new(total, pos)
        elif step.startswith('cut '):
            n = int(step.split(' ')[-1])
            pos = cut2(total, pos, n)
        elif step.startswith('deal with increment '):
            n = int(step.split(' ')[-1])
            pos = deal_inc(total, pos, n)
        else:
            raise Exception()

    return pos


def reverse_shuffle(steps, total, a=1, b=0):
    for step in steps[::-1]:
        if step == 'deal into new stack':
            a, b = reverse_deal_new(total, a, b)
        elif step.startswith('cut '):
            n = int(step.split(' ')[-1])
            a, b = reverse_cut(total, a, b, n)
        elif step.startswith('deal with increment '):
            n = int(step.split(' ')[-1])
            a, b = reverse_deal_inc(total, a, b, n)
        else:
            raise Exception()

    return a, b


def repeated_composition(a, b, m, total):
    # f(x) = ax + b
    # Apply f(x) on itself m times
    if m == 0:
        return 1, 0
    elif m % 2 == 0:
        # g(x) = f(f(x)) = aax + ab + b
        # Apply g(x) on itsemf m / 2 times
        return repeated_composition(a*a % total, (a*b + b) % total, m // 2, total)
    else:
        # g(x) = f(x) applied on itself m - 1 times
        # Suppose g(x)= cx + d
        # f(g(x)) = a(cx + d) + b = acx + ad + b
        c, d = repeated_composition(a, b, m - 1, total)
        return a*c % total, (a*d + b) % total


def load_instructions(path):
    with open(path) as fp:
        lines = fp.readlines()

    return [l.strip() for l in lines]


small = 'cards_small.txt'
medium = 'cards_medium.txt'
prod = 'cards_prod.txt'

steps = load_instructions(prod)
pos = shuffle2(10007, 2019, steps)
print(pos)

TOTAL = 119315717514047
M = 101741582076661

reverse_func = reverse_shuffle(steps, TOTAL)
a, b = reverse_func
ra, rb = repeated_composition(a, b, M, TOTAL)
print('2020:', (ra * 2020 + rb) % TOTAL)

