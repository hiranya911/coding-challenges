def parse_input(path):
  rules = []
  your_ticket = None
  nearby_tickets = []
  with open(path) as fp:
    lines = fp.readlines()
  state = 'rules'
  for line in lines:
    line = line.strip()
    if line == '':
      continue

    if state == 'rules':
      if line == 'your ticket:':
        state = 'your_ticket'
      else:
        rules.append(Rule(line))
    elif state == 'your_ticket':
      if line == 'nearby tickets:':
        state = 'nearby_tickets'
      else:
        your_ticket = [int(val) for val in line.split(',')]
    elif state == 'nearby_tickets':
      nearby_tickets.append([int(val) for val in line.split(',')])
    else:
      raise ValueError('Parse error')

  return rules, your_ticket, nearby_tickets


class RangeCondition:
  def __init__(self, text) -> None:
    lo, hi = text.split('-')
    self.lo = int(lo)
    self.hi = int(hi)

  def check(self, value):
    return value >= self.lo and value <= self.hi

  def __repr__(self) -> str:
      return f'[{self.lo}, {self.hi}]'


class Rule:

  def __init__(self, text) -> None:
    colon = text.index(':')
    self.key = text[:colon]
    self.conditions = [RangeCondition(cond) for cond in text[colon+2:].split(' or ')]

  def check(self, value):
    return any(cond.check(value) for cond in self.conditions)

  def check_all(self, values):
    return all(self.check(value) for value in values)

  def __repr__(self) -> str:
      return f'<Key {self.key}>'


class CSP:

  def __init__(self, variables, domains) -> None:
    self.variables = variables
    self.domains = domains

  def consistent(self, assignment):
    if len(set(assignment.values())) < len(assignment):
      return False
    if len(assignment) == len(self.variables):
      return True
    return True

  def backtracking_search(self, assignment=dict()):
    if len(assignment) == len(self.variables):
      return assignment

    unassigned = [v for v in self.variables if v not in assignment]
    first = unassigned[0]
    for value in self.domains[first]:
      local_assignment = dict(assignment)
      local_assignment[first] = value
      if self.consistent(local_assignment):
        result = self.backtracking_search(local_assignment)
        if result is not None:
          return result
    return None


def check_ticket(ticket, rules):
  for value in ticket:
    if not any(rule.check(value) for rule in rules):
      return value
  return 0


def is_valid_ticket(ticket, rules):
  for value in ticket:
    if not any(rule.check(value) for rule in rules):
      return False
  return True


def scanning_error_rate(tickets, rules):
  rate = 0
  for ticket in tickets:
    rate += check_ticket(ticket, rules)

  return rate


if __name__ == '__main__':
  rules, your_ticket, nearby_tickets = parse_input('rules_large.txt')
  print(f'Total tickets = {len(nearby_tickets)}')
  print(f'Scanning error rate = {scanning_error_rate(nearby_tickets, rules)}')

  valid_tickets = [ticket for ticket in nearby_tickets if is_valid_ticket(ticket, rules)]
  print(f'Valid tickets = {len(valid_tickets)}')

  fields = []
  for i in range(len(valid_tickets[0])):
    fields.append([t[i] for t in valid_tickets])
  print(f'No of fields = {len(fields)}')
  print(f'No of rules = {len(rules)}')

  domains = {}
  for rule in rules:
    domains[rule] = [ idx for idx, values in enumerate(fields) if rule.check_all(values) ]
  rules = sorted(rules, key=lambda r: len(domains[r]))

  print()
  csp = CSP(rules, domains)
  assignment = csp.backtracking_search()

  inverted = {v: k for k, v in assignment.items()}
  for i in range(len(inverted)):
    print(f'{i+1}: {inverted[i]}')

  result = 1
  for rule in rules:
    if rule.key.startswith('departure '):
      idx = assignment[rule]
      result *= your_ticket[idx]

  print(f'Departure values result = {result}')
