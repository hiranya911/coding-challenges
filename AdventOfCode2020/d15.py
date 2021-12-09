class MemoryGame:

  def __init__(self, numbers) -> None:
    self.history = {}
    self.initial = numbers
    for idx, num in enumerate(numbers):
        self.history[num] = idx
    self.last = None
    self.idx = 0

  def next(self):
    if self.idx < len(self.initial):
        self.last = self.initial[self.idx]
        self.idx += 1
        return self.last

    result = self._next_from_history()
    self.history[self.last] = self.idx - 1
    self.last = result
    self.idx += 1
    return result

  def _next_from_history(self):
    prev = self.idx - 1
    last_index = self.history.get(self.last, prev)
    return prev - last_index


if __name__ == '__main__':
    starters = [0,13,1,8,6,15]
    game = MemoryGame(starters)
    for _ in range(2020):
        val = game.next()
    print(f'2020th = {val}')
