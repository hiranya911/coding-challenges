def split_layers(image, width, height):
    pixels_per_layer = width * height
    layers = []
    for i in range(0, len(image), pixels_per_layer):
        layers.append(image[i:i+pixels_per_layer])
    return layers


def count_digit(layer, digit):
    return len([d for d in layer if d == digit])


def parse_image(s):
    return [int(ch) for ch in s]


def checksum(image, width, height):
    layers = split_layers(image, width, height)
    zero_counts = [count_digit(layer, 0) for layer in layers]
    min_index = zero_counts.index(min(zero_counts))
    fewest_0s = layers[min_index]
    count_1s = count_digit(fewest_0s, 1)
    count_2s = count_digit(fewest_0s, 2)
    return count_1s * count_2s


def get_color(pixels):
    for p in pixels:
        if p == 2:
            continue
        return p


def decode(image, width, height):
    layers = split_layers(image, width, height)
    result = []
    for i in range(0, width*height):
        pixels = [layer[i] for layer in layers]
        result.append(get_color(pixels))
    return result


def print_decoded(image, width, height):
    for i in range(0, width*height):
        digit = image[i]
        if digit == 0:
            print(' ', end='')
        else:
            print('X', end='')
        if (i + 1) % width == 0:
            print()
        


with open('rover_image.txt') as fp:
    content = fp.read()
content = content.strip()
image = parse_image(content)
#print(checksum(image, 25, 6))
result = decode(image, 25, 6)
print_decoded(result, 25, 6)

