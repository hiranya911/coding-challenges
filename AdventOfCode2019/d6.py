class SpaceObject(object):

    def __init__(self, name, parent):
        self.name = name
        self.parent = parent

    @property
    def height(self):
        counter = 0
        parent = self.parent
        while parent:
            counter += 1
            parent = parent.parent
        return counter

    @property
    def ancestors(self):
        anc = []
        parent = self.parent
        while parent:
            anc.append(parent)
            parent = parent.parent
        return anc

    def distance(self, other):
        if other == self.parent:
            return 0
        return self.parent.distance(other) + 1


class OrbitalMap(object):

    def __init__(self):
        self.com = SpaceObject('COM', None)
        self.objects = {'COM': self.com}

    def add_orbit(self, parent, child):
        parent_obj = self.objects.get(parent)
        if not parent_obj:
            parent_obj = SpaceObject(parent, None)
            self.objects[parent] = parent_obj

        child_obj = self.objects.get(child)
        if not child_obj:
            child_obj = SpaceObject(child, parent_obj)
            self.objects[child] = child_obj
        else:
            child_obj.parent = parent_obj

    def get_object(self, name):
        return self.objects[name]

    def common_ancestor(self, name1, name2):
        obj1 = self.get_object(name1)
        obj2 = self.get_object(name2)
        anc1, anc2 = obj1.ancestors, obj2.ancestors
        anc1, anc2 = anc1[::-1], anc2[::-1]
        for i in range(0, len(anc1)):
            if anc1[i].name == anc2[i].name:
                continue
            else:
                return anc1[i-1]
        raise Exception('No common ancestor')

    def transfer_distance(self, name1='YOU', name2='SAN'):
        common = self.common_ancestor('YOU', 'SAN')
        print(f'Common ancestor: {common.name}')

        d1 = self.get_object(name1).distance(common)
        d2 = self.get_object(name2).distance(common)
        return d1 + d2

    def get_total_orbits(self):
        return sum([obj.height for obj in self.objects.values()])

# A)B
#A -> None, B -> A
# C)A
# C -> None, A -> C

def construct_map(lines):
    omap = OrbitalMap()
    for line in lines:
        line = line.strip()
        segments = line.split(')')
        omap.add_orbit(segments[0], segments[1])
    return omap


def parse_file(name):
    with open(name) as fp:
        lines = fp.readlines()
    return lines


sample = [
    "COM)B", "B)C", "C)D", "D)E", "E)F", "B)G", "G)H", "D)I",
    "E)J", "J)K", "K)L", "K)YOU", "I)SAN"
]

lines = parse_file('orbits.txt')
print(f'Number of lines: {len(lines)}')
m = construct_map(lines)
print(f'Total orbits: {m.get_total_orbits()}')
print(f'Total transfers: {m.transfer_distance()}')




