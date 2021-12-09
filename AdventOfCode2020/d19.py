import regex


def parse_rules(path):
    rules = {}
    inputs = []
    with open(path) as fp:
        lines = fp.readlines()
        for line in lines:
            line = line.strip()
            if ':' in line:
                key, val = line.strip().split(': ')
                if val.startswith('"'):
                    val = val.strip('"')
                rules[int(key)] = val
            else:
                inputs.append(line)

    return rules, inputs


def is_resolved(key, rule, resolved):
    for segment in rule.split():
        if segment == '|' or segment == 'a' or segment == 'b':
            continue
        if int(segment) in resolved:
            continue

        return False
    return True


def resolve_rules(rules):
    unresolved = dict(rules)
    resolved = {}
    while unresolved:
        new_resolved = []
        for key, val in unresolved.items():
            if is_resolved(key, val, resolved):
                resolved[key] = val
                new_resolved.append(key)

        for key in new_resolved:
            del unresolved[key]

    return resolved


def expand_rule(resolved, idx, modified=False):
    rule = resolved[idx]
    if len(rule) == 1:
        return rule
    result = ''
    if modified:
        if idx == 8:
            return f'({expand_rule(resolved, 42, modified)}+)'
        if idx == 11:
            res = f'({expand_rule(resolved, 42, modified)}(?R)?{expand_rule(resolved, 31, modified)})'
            return res

    sectional = False
    for segment in rule.split():
        if segment == '|':
            result += '|'
            sectional = True
        else:
            try:
                result += (expand_rule(resolved, int(segment), modified))
            except Exception as ex:
                print(idx, ':', rule, modified)
                raise ex

    if sectional:
        return f'({result})'
    return result


def count_matches(lines, resolved, modified=False):
    pattern = regex.compile(expand_rule(resolved, 0, modified))
    total = 0
    for line in lines:
        m = pattern.match(line)
        if m:
            start, end = m.span()
            print(start, end, len(line))
            if len(line) == (end - start):
                total += 1
    return total


def check_string(original_text, resolved, modified=False):
    sections = resolved[0].split(' | ')
    for section in sections:
        text = original_text
        subrules = section.split()
        found = True
        for sr in subrules:
            p = regex.compile(f'^{expand_rule(resolved, int(sr), modified)}')
            print(p)
            m = p.match(text)
            if m is not None:
                text = text[m.end():]
            else:
                found = False
                break
        if found and not text:
            return True
    return False


def resolve_enumerate(idx, rules, resolved=dict()):
    rule = rules[idx]
    if len(rule) == 1:
        resolved[idx] = [rule]

    if idx in resolved:
        return resolved[idx]

    full_results = []
    for section in rule.split(' | '):
        results = ['']
        for sr in section.split():
            temp = resolve_enumerate(int(sr), rules, resolved)
            temp_results = []
            for r in results:
                for t in temp:
                    temp_results.append(f'{r}{t}')
            results = temp_results
        full_results += results

    resolved[idx] = full_results
    return full_results


def match_8(line, eight):
    for option in eight:
        if line.startswith(option):
            return True
    return False


def match_11(line, eleven):
    for option in eleven:
        h1, h2 = option[:len(option)//2], option[len(option)//2:]
        if line.startswith(h1) and line.endswith(h2):
            return True
    return False


def check_11(line, eleven):
    len11 = len(eleven[0])
    if not line or len(line) % len11 != 0:
        return False
    while line:
        if match_11(line, eleven):
            line = line[len11//2:-len11//2]
        else:
            return False
    return True


def parse_modified(line, eight, eleven):
    len8 = len(eight[0])
    len11 = len(eleven[0])

    if match_8(line, eight):
        line = line[len8:]
    else:
        return False

    while True:
        if check_11(line, eleven):
            return True

        if match_8(line, eight):
            line = line[len8:]
        else:
            return False

    return False


if __name__ == '__main__':
    rules, inputs = parse_rules('data/d19_input1_large.txt')
    resolved = set(resolve_enumerate(0, rules))
    print(sum(1 for line in inputs if line in resolved))

    eight = resolve_enumerate(8, rules)
    eleven = resolve_enumerate(11, rules)
    print(sum(1 for line in inputs if parse_modified(line, eight, eleven)))
