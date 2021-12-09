def find_loop_size(public_key, subject_number=7):
    loop_size = 0
    value = 1
    while True:
        loop_size += 1
        value *= subject_number
        value = value % 20201227
        if value == public_key:
            return loop_size


def transform(subject_number, loop_size):
    value = 1
    for _ in range(loop_size):
        value *= subject_number
        value = value % 20201227
    return value


if __name__ == '__main__':
    card_pk, door_pk = 3418282, 8719412
    # card_pk, door_pk = 5764801, 17807724
    card_loop_size = find_loop_size(card_pk)
    print(transform(door_pk, card_loop_size))

