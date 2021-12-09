import functools
import time


def neighbors(seat_map, row, col):
    result = []
    if row > 0:
        result.append((row-1, col))
        if col > 0:
            result.append((row-1, col-1))
        if col+1 < seat_map.cols:
            result.append((row-1, col+1))
    if row+1 < seat_map.rows:
        result.append((row+1, col))
        if col > 0:
            result.append((row+1, col-1))
        if col+1 < seat_map.cols:
            result.append((row+1, col+1))
    if col > 0:
        result.append((row, col-1))
    if col+1 < seat_map.cols:
        result.append((row, col+1))
    return result


@functools.lru_cache(maxsize=None)
def visible_neighbors(seat_map, row, col):
    gradients = [(0, 1), (0, -1), (1, 0), (-1, 0), (1, 1), (1, -1), (-1, 1), (-1, -1)]
    result = []
    for row_diff, col_diff in gradients:
        multiplier = 1
        while True:
            row_to_check = row + row_diff * multiplier
            if row_to_check < 0 or row_to_check == seat_map.rows:
                break
            col_to_check = col + col_diff * multiplier
            if col_to_check < 0 or col_to_check == seat_map.cols:
                break
            if seat_map.seats[row_to_check][col_to_check] != '.':
                result.append((row_to_check, col_to_check))
                break
            multiplier += 1
    return result


def parse_map(path):
    with open(path) as fp:
        return [ list(line.strip()) for line in fp.readlines() ]


class SeatMap:

    def __init__(self, seats, neighbors_func=neighbors):
        self.seats = seats
        self.rows = len(seats)
        self.cols = len(seats[0])
        self.neighbors_func = neighbors_func

    def tick(self):
        updated = list([list(row) for row in self.seats])
        changes = 0
        for row in range(self.rows):
            for col in range(self.cols):
                if self.should_occupy(row, col):
                    updated[row][col] = '#'
                    changes += 1
                elif self.should_vacate(row, col):
                    updated[row][col] = 'L'
                    changes += 1
        self.seats = updated
        return changes

    def get_occupied_seat_count(self):
        return sum(1 for row in range(self.rows) for col in range(self.cols) if self.is_taken(row, col))

    def should_occupy(self, row, col):
        return self.is_empty(row, col) and self.occupied_neighbors(row, col) == 0

    def should_vacate(self, row, col):
        return self.is_taken(row, col) and self.occupied_neighbors(row, col) >= 4

    def is_taken(self, row, col):
        return self.seats[row][col] == '#'

    def is_empty(self, row, col):
        return self.seats[row][col] == 'L'

    def occupied_neighbors(self, row, col):
        nbrs = self.neighbors_func(self, row, col)
        return sum(1 for row, col in nbrs if self.is_taken(row, col))

    @staticmethod
    def create(path):
        with open(path) as fp:
            seats = [ list(line.strip()) for line in fp.readlines() ]
            return SeatMap(seats)

    def __repr__(self):
        return '\n'.join([''.join(row) for row in self.seats])


class NewSeatMap(SeatMap):

    def __init__(self, seats):
        super().__init__(seats, neighbors_func=visible_neighbors)

    def should_vacate(self, row, col):
        return self.is_taken(row, col) and self.occupied_neighbors(row, col) >= 5


if __name__ == '__main__':
    seat_map = NewSeatMap(parse_map('data/d11_input1_large.txt'))
    start = time.time()
    while True:
        if seat_map.tick() == 0:
            break
    print(seat_map.get_occupied_seat_count())
    print(f'Elapsed time = {time.time() - start}')
