class GroupResponse:

    def __init__(self, lines):
        self.lines = lines
        self.chars = [ set(line) for line in lines ]

    def get_yes_count(self):
        yes = set.union(*self.chars)
        return len(yes)

    def get_common_yes_count(self):
        yes = set.intersection(*self.chars)
        return len(yes)


def parse_file(path):
    groups = []
    with open(path) as fp:
        lines = fp.readlines()
        buffer = []
        for line in lines:
            line = line.strip()
            if line == '':
                groups.append(GroupResponse(buffer))
                buffer = []
            else:
                buffer.append(line)
    if buffer:
        groups.append(GroupResponse(buffer))
    return groups


if __name__ == "__main__":
    groups = parse_file('d6_input1_small.txt')
    sum_of_yes = sum(group.get_yes_count() for group in groups)
    print(f'Sum of yes (union): {sum_of_yes}')

    sum_of_yes = sum(group.get_common_yes_count() for group in groups)
    print(f'Sum of yes (intersect): {sum_of_yes}')
