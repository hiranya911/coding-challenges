import re


HAIR_COLOR = re.compile('#[0-9a-f]{6}')


REQUIRED_FIELDS = [
    'byr', 'iyr', 'eyr', 'hgt', 'hcl', 'ecl', 'pid'
]


def validate_byr(value):
    return len(value) == 4 and int(value) >= 1920 and int(value) <=2002


def validate_iyr(value):
    return len(value) == 4 and int(value) >= 2010 and int(value) <= 2020


def validate_eyr(value):
    return len(value) == 4 and int(value) >= 2020 and int(value) <= 2030


def validate_hgt(value):
    if len(value) == 5 and value.endswith('cm'):
        num = int(value[:3])
        return num >= 150 and num <= 193
    elif len(value) == 4 and value.endswith('in'):
        num = int(value[:2])
        return num >= 59 and num <= 76
    return False


def validate_hcl(value):
    return HAIR_COLOR.match(value) is not None


def validate_ecl(value):
    return value in ('amb', 'blu', 'brn', 'gry', 'grn', 'hzl', 'oth')


def validate_pid(value):
    return len(value) == 9 and value.isdigit()


RULES = {
    'byr': validate_byr,
    'iyr': validate_iyr,
    'eyr': validate_eyr,
    'hgt': validate_hgt,
    'hcl': validate_hcl,
    'ecl': validate_ecl,
    'pid': validate_pid,
}


class TravelDocument:

    def __init__(self, fields):
        self.fields = fields

    def is_valid(self):
        for key, rule in RULES.items():
            if key not in self.fields:
                return False
            if not rule(self.fields[key]):
                return False
        return True

    @staticmethod
    def create(lines):
        entries = {}
        for line in lines:
            fields = line.split()
            for field in fields:
                key, value = field.split(':')
                entries[key] = value
        return TravelDocument(entries)


def parse_batch_file(path):
    docs = []
    with open(path) as fp:
        lines = fp.readlines()
        buffer = []
        for line in lines:
            line = line.strip()
            if line == '':
                if buffer:
                    docs.append(TravelDocument.create(buffer))
                    buffer = []
            else:
                buffer.append(line)
        if buffer:
            docs.append(TravelDocument.create(buffer))
    return docs


if __name__ == '__main__':
    docs = parse_batch_file('d4_input1_large.txt')
    print(f'Parsed {len(docs)} from batch file')
    valid_docs = sum(1 for doc in docs if doc.is_valid())
    print(f'Valid passports: {valid_docs}')
