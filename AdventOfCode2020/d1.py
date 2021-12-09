import bisect


def load_expense_report(path):
    with open(path) as fp:
        lines = fp.readlines()
        return [ int(item) for item in lines]


def bin_search(a, x, lo=0):
    i = bisect.bisect_left(a, x, lo=0)
    if i != len(a) and a[i] == x:
        return i
    return -1


def problem1(report, total=2020):
    for item in report:
        other = total - item
        idx = bin_search(report, other)
        if idx != -1:
            return item, other, item * other
    raise ValueError('No solution found')


def problem2(report):
    for idx, item in enumerate(report):
        rem_total = 2020 - item
        try:
            x, y, _ = problem1(report, rem_total)
            return x, y, item, x * y * item
        except ValueError:
            pass


if __name__ == '__main__':
    report = load_expense_report('d1_input1_large.txt')
    report.sort()
    x, y, mul = problem1(report)
    print(f'{x} * {y} = {mul}')

    x, y, z, mul = problem2(report)
    print(f'{x} * {y} * {z} = {mul}')
