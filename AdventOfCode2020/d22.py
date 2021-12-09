import collections


def parse_input(path):
    all_decks = []
    with open(path) as fp:
        deck = []
        for line in fp.readlines():
            line = line.strip()
            if not line:
                continue
            if line.startswith('Player '):
                if deck:
                    all_decks.append(collections.deque(deck))
                    deck = []
            else:
                deck.append(int(line))

        if deck:
            all_decks.append(collections.deque(deck))
    return all_decks


def get_score(winner):
    return sum(x * y for x, y in zip(winner, [i for i in range(len(winner), 0, -1)]))


def combat(player1, player2):
    round = 1
    while True:
        if not player1 or not player2:
            print()
            print('== Post-game results ==')
            print('Player 1:', player1)
            print('Player 2:', player2)
            break

        print(f'-- Round {round} --')
        print('Player 1:', player1)
        print('Player 2:', player2)
        p1 = player1.popleft()
        p2 = player2.popleft()
        print('Player 1 plays:', p1)
        print('Player 2 plays:', p2)
        if p1 > p2:
            print('Player 1 wins the round!')
            print()
            player1.append(p1)
            player1.append(p2)
        else:
            print('Player 2 wins the round!')
            print()
            player2.append(p2)
            player2.append(p1)
        round += 1

    winner = player1 if player1 else player2
    return get_score(winner)


GAME_ID = 0


def recursive_combat(player1, player2):
    global GAME_ID
    round = 1
    p1_history = set()
    p2_history = set()
    GAME_ID += 1
    game = GAME_ID
    while True:
        if not player1:
            print(f'The winner of game {game} is player 2!')
            return 2, get_score(player2)
        if not player2:
            print(f'The winner of game {game} is player 1!')
            return 1, get_score(player1)

        print(f'-- Round {round} (Game {game}) --')
        print('Player 1:', player1)
        print('Player 2:', player2)

        p1_hash = str(player1)
        p2_hash = str(player2)
        if p1_hash in p1_history or p2_hash in p2_history:
            print(f'(TERM) The winner of game {game} is player 1!')
            return 1, p1_hash

        p1_history.add(p1_hash)
        p2_history.add(p2_hash)

        p1 = player1.popleft()
        p2 = player2.popleft()
        print('Player 1 plays:', p1)
        print('Player 2 plays:', p2)
        if len(player1) >= p1 and len(player2) >= p2:
            print('Playing a sub-game to determine the winner...')
            player1_copy = collections.deque(list(player1)[:p1])
            player2_copy = collections.deque(list(player2)[:p2])
            winner, _ = recursive_combat(player1_copy, player2_copy)
            if winner == 1:
                player1.append(p1)
                player1.append(p2)
            else:
                player2.append(p2)
                player2.append(p1)
        elif p1 > p2:
            print(f'Player 1 wins the round {round} of game {game}!')
            print()
            player1.append(p1)
            player1.append(p2)
        else:
            print(f'Player 2 wins the round {round} of game {game}!')
            print()
            player2.append(p2)
            player2.append(p1)
        round += 1


if __name__ == '__main__':
    player1, player2 = parse_input('data/d22_input1_large.txt')
    winner, score = recursive_combat(player1, player2)
    print(f'Winning score: {score}')
