def parse_inputs(path):
    with open(path) as fp:
        adapters = [int(line) for line in fp.readlines()]
        adapters.sort()
        return adapters


def pick_adapter_chain(adapters):
    current = 0
    dist = {1: 0, 2: 0, 3: 0}
    for item in adapters:
        diff = item - current
        dist[diff] += 1
        current = item

    dist[3] += 1
    return dist


cache = {}


def backtracking_search(adapters, start_idx):
    if start_idx in cache:
        return cache[start_idx]

    if start_idx == len(adapters) - 1:
        return 1

    current = adapters[start_idx]
    candidates = []
    idx = start_idx + 1
    while idx < len(adapters) and adapters[idx] <= current + 3:
        candidates.append(idx)
        idx += 1

    total = 0
    for candidate in candidates:
        total += backtracking_search(adapters, candidate)
    return total


if __name__ == '__main__':
    adapters = parse_inputs('data/d10_input1_large.txt')
    dist = pick_adapter_chain(adapters)
    summary = dist[1] * dist[3]
    print(f'Jolt summary = {summary}')

    # Start from 0 for the search
    adapters = [0] + adapters
    idx = len(adapters) - 2
    while idx >= 0:
        paths = backtracking_search(adapters, idx)
        cache[idx] = paths
        idx -= 1

    print(f'Paths = {cache[0]}')
