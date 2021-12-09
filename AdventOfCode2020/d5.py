def decode(enc):
    return int(enc, 2)


def parse_address(line):
    row, col = line[:7], line[7:]
    row = row.replace('F', '0').replace('B', '1')
    col = col.replace('L', '0').replace('R', '1')
    return row, col


def parse_seatmap_file(path):
    with open(path) as fp:
        lines = fp.readlines()
        return [ parse_address(line.strip()) for line in lines ]


def get_seat_id(row_enc, col_enc):
    row = decode(row_enc)
    col = decode(col_enc)
    return row * 8 + col


def find_missing(seats):
    seats.sort()
    for idx, seat_id in enumerate(seats):
        if seats[idx+1] != seat_id + 1:
            return seat_id + 1
    raise ValueError('No solution')


if __name__ == "__main__":
    inputs = parse_seatmap_file('d5_input1_large.txt')
    seats = [ get_seat_id(row, col) for row, col in inputs ]
    max_seat_id = max(seats)
    print(f'Max seat ID: {max_seat_id}')

    missing = find_missing(seats)
    print(f'My seat ID: {missing}')
