def fuel(mass):
    return (mass // 3) - 2

def adjusted_fuel(mass):
    total_fuel = fuel(mass)
    f3 = fuel(total_fuel)
    while f3 >= 0:
        total_fuel += f3
        f3 = fuel(f3)
    return total_fuel


if __name__ == '__main__':
    total_fuel = 0
    with open('d1-input.txt') as fp:
        lines = fp.readlines()
        for line in lines:
            mass = int(line.strip())
            total_fuel += adjusted_fuel(mass)

    print(f'Total fuel for modules: {total_fuel}')





