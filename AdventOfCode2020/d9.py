class Xmas:

  def __init__(self, window):
    self.window = window
    self.numbers = [None] * window
    self.sums = {}
    self.index = 0

  def check(self, num):
    curr = self.index % self.window
    self.index += 1
    old = self.numbers[curr]
    if old is not None:
      if not self.is_valid_sum(num):
        return False
      del self.sums[old]

    self.numbers[curr] = num
    for k, v in self.sums.items():
      v.add(k + num)
    self.sums[num] = set()
    return True

  def is_valid_sum(self, num):
    for sums in self.sums.values():
      if num in sums:
        return True
    return False


def parse_numbers(path):
  with open(path) as fp:
    return [ int(line) for line in fp.readlines() ]


def find_weakness(seq, num):
  start, end = 0, 1
  total = seq[start] + seq[end]
  while total != num:
    if total < num:
      end += 1
      total += seq[end]
    else:
      total -= seq[start]
      start += 1
  return seq[start:end+1]


if __name__ == "__main__":
  xmas = Xmas(window=25)
  numbers = parse_numbers('large.txt')
  broken = -1
  for num in numbers:
    if not xmas.check(num):
      print(f'Broken sequence at {num}')
      broken = num
      break

  seq = find_weakness(numbers, broken)
  weakness = min(seq) + max(seq)
  print(f'Weakness = {weakness}')
