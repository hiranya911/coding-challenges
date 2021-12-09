def is_increasing(digits):
    for i in range(1, len(digits)):
        if digits[i] < digits[i-1]:
            return False
    return True


def has_double(digits):
    group = 1
    last = digits[0]
    for i in range(1, len(digits)):
        if digits[i] == last:
            group += 1
        elif group == 2:
            return True
        else:
            group = 1
        last = digits[i]

    if group == 2:
        return True
    return False


def is_password_candidate(number):
    digits = [int(n) for n in str(number)]
    return is_increasing(digits) and has_double(digits)


def count_passwords(a, b):
    count = 0
    for number in range(a, b+1):
        if is_password_candidate(number):
            count += 1
    return count


print(is_password_candidate(112233))
print(is_password_candidate(123444))
print(is_password_candidate(111122))
print(count_passwords(235741, 706948))
