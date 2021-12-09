class Ruleset:
    def __init__(self):
        self.rules = {}

    def add_rule(self, line):
        if line.endswith(' no other bags.'):
            return

        segments = line.split()
        outer = f'{segments[0]} {segments[1]}'
        rem = ' '.join(segments[4:])
        inner_segments = rem.split(',')

        inner_values = []
        for inner_segment in inner_segments:
            segments = inner_segment.split()
            inner_values.append((f'{segments[1]} {segments[2]}', int(segments[0])))
        self.rules[outer] = inner_values

    def contained_in(self, node):
        results = []
        for outer, inner_values in self.rules.items():
            for inner, count in inner_values:
                if inner == node.state:
                    results.append(Node(outer, node))
        return results

    def inner_containers(self, node):
        if node.state in self.rules:
            inner_vals = self.rules[node.state]
            return [ Node(inner, node, count) for inner, count in inner_vals ]
        return []


class Node:
    def __init__(self, state, parent, cost = 0):
        self.state = state
        self.parent = parent
        self.cost = cost

    def __repr__(self):
        return self.state


def visit(initial, successors):
    visited = set()
    frontier = [ initial ]
    while frontier:
        current = frontier.pop()
        for child in successors(current):
            if child.state in visited:
                continue
            visited.add(child.state)
            frontier.append(child)
    return visited


def node_cost(initial, successors):
    cost = 1
    for child in successors(initial):
        cost += child.cost * node_cost(child, successors)
    return cost


def build_ruleset(path):
    with open(path) as fp:
        lines = fp.readlines()
        rs = Ruleset()
        for line in lines:
            rs.add_rule(line.strip())
        return rs


if __name__ == "__main__":
    rs = build_ruleset('d7_input1_large.txt')
    containers = visit(Node('shiny gold', None), rs.contained_in)
    print(f'Containers = {len(containers)}')

    # Subtract itself
    total_bags = node_cost(Node('shiny gold', None), rs.inner_containers) - 1
    print(f'Total bags within = {total_bags}')
