import heapq


class Node(object):

    def __init__(self, state, parent, cost=0):
        self.state = state
        self.parent = parent
        self.cost = cost

    def __lt__(self, other):
        return self.cost < other.cost


def node_to_path(node):
    path = []
    while node:
        path.append(node)
        node = node.parent
    path.reverse()
    return path


def astar(init, goal_test, successors):
    frontier = [Node(init, None)]
    explored = {init: 0}
    while frontier:
        current_node = heapq.heappop(frontier)
        current_state = current_node.state
        if goal_test(current_state):
            return current_node

        for child in successors(current_state):
            new_cost = current_node.cost + 1
            if child not in explored or explored[child] > new_cost:
                explored[child] = new_cost
                heapq.heappush(frontier, Node(child, current_node, new_cost))

    return None


    
