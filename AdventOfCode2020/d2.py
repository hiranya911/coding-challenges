class PasswordPolicy:

    def __init__(self, config):
        segments = config.split(' ')
        bounds = segments[0].split('-')
        self.low = int(bounds[0])
        self.high = int(bounds[1])
        self.char = segments[1]

    def verify(self, password):
        count = 0
        for ch in password:
            if ch == self.char:
                count += 1
        return count >= self.low and count <= self.high


class NewPasswordPolicy(PasswordPolicy):

    def verify(self, password):
        ch1 = password[self.low - 1]
        ch2 = password[self.high - 1]
        if ch1 == self.char and ch2 != self.char:
            return True
        if ch1 != self.char and ch2 == self.char:
            return True
        return False


def load_password_file(path):
    items = []
    with open(path) as fp:
        lines = fp.readlines()
        return [ line.strip().split(': ') for line in lines ]


def problem1(inputs, policy_cls=PasswordPolicy):
    count = 0
    for config, password in inputs:
        if policy_cls(config).verify(password):
            count += 1
    return count


if __name__ == '__main__':
    inputs = load_password_file('d2_input1_large.txt')
    valid_passwords = problem1(inputs)
    print(f'[Old] Valid passwords = {valid_passwords}')

    valid_passwords = problem1(inputs, policy_cls=NewPasswordPolicy)
    print(f'[New] Valid passwords = {valid_passwords}')
