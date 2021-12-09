import collections


def pick_three(cups, current):
    new_current = current
    to_remove = []
    for i in (1, 2, 3):
        idx = (current + i) % len(cups)
        if idx < current:
            new_current -= 1
        to_remove.append(cups[idx])
    for cup in to_remove:
        cups.remove(cup)
    return cups, to_remove, new_current


def move(cups, current=0):
    len_cups = len(cups)
    current_cup = cups[current]

    cups, removed, new_current = pick_three(cups, current)
    #print_pickup(removed)

    destination_cup = (current_cup - 1) % len_cups
    while destination_cup in removed:
        destination_cup = (destination_cup - 1) % len_cups
    #print('destination:', destination_cup + 1)

    index = cups.index(destination_cup)
    cups.insert(index+1, removed[0])
    cups.insert(index+2, removed[1])
    cups.insert(index+3, removed[2])

    if index < new_current:
        new_current += 3

    return cups, (new_current+1) % len_cups


def print_cups(cups, current):
    print('cups: ', end='')
    for idx, cup in enumerate(cups):
        if idx == current:
            print(f'({cup+1})', end=' ')
        else:
            print(cup+1, end=' ')
    print()


def print_pickup(cups):
    print('pick up: ', end='')
    for cup in cups:
        print(cup+1, end=' ')
    print()


def get_result(cups):
    idx = (cups.index(0) + 1) % len(cups)
    result = []
    while cups[idx] != 0:
        result.append(str(cups[idx]+1))
        idx = (idx+1) % len(cups)
    return ''.join(result)


class Cup:

    def __init__(self, label):
        self.label = label


class CupCircle:

    def __init__(self, cups):
        self.cups = cups
        self.count = len(cups)
        self.current = cups[0]
        for idx, cup in enumerate(cups[:-1]):
            cup.next = cups[idx+1]
        cups[-1].next = cups[0]
        self.label_index = {cup.label: cup for cup in cups}

    def move(self):
        # Remove 3
        one = self.current.next
        two = one.next
        three = two.next
        self.current.next = three.next
        removed = (one.label, two.label, three.label)

        # Find destination
        dest = self.current.label - 1
        if dest == 0:
            dest = self.count
        while dest in removed:
            dest -= 1
            if dest == 0:
                dest = self.count
        dest_cup = self.label_index[dest]

        # Add 3
        temp = dest_cup.next
        dest_cup.next = one
        three.next = temp

        self.current = self.current.next


    def signature(self):
        node = self.label_index[1].next
        result = []
        while node.label != 1:
            result.append(str(node.label))
            node = node.next
        return ''.join(result)


if __name__ == '__main__':
    start = '792845136'
    cups = [Cup(int(ch)) for ch in start] + [Cup(i) for i in range(10, 1000001)]
    print(f'Total cups = {len(cups)}')

    cc = CupCircle(cups)
    for _ in range(10000000):
        cc.move()

    one = cc.label_index[1]
    r1, r2 = one.next.label, one.next.next.label
    print(r1, r2, r1*r2)
    #print(cc.signature())
