class PocketDimension:

  def __init__(self):
    self.points = {}
    self.four_d = False

  def plot_layer(self, layer_z=0, layer_w=0):
    layer = [(x, y, z, w) for x, y, z, w in self.points.keys() if z == layer_z and w == layer_w]
    min_x = min(x for x, _, _ in layer)
    max_x = max(x for x, _, _ in layer)
    min_y = min(y for _, y, _ in layer)
    max_y = max(y for _, y, _ in layer)
    for y in range(min_y, max_y + 1):
      for x in range(min_x, max_x + 1):
        point = self.get_point((x, y, layer_z))
        if point == 1:
          print('#', end='')
        else:
          print('.', end='')
      print()

  def plot_all_layers(self):
    min_z = min(z for _, _, z, _ in self.points.keys())
    max_z = max(z for _, _, z, _ in self.points.keys())
    for z in range(min_z, max_z + 1):
      print(f'z={z}')
      self.plot_layer(layer_z=z)
      print()

  def get_point(self, point):
    return self.points.get(point, 0)

  def get_active_cubes(self):
    return len(self.points)

  def evolve(self):
    to_deactivate = []
    to_activate = {}
    for point, value in self.points.items():
      if value == 0:
        continue

      nbrs = self.find_neighbors(point)
      active_count = sum(self.get_point(nbr) for nbr in nbrs)
      if active_count != 2 and active_count != 3:
        to_deactivate.append(point)
      for nbr in nbrs:
        if self.get_point(nbr) == 0:
          active_nbrs = to_activate.get(nbr, set())
          active_nbrs.add(point)
          to_activate[nbr] = active_nbrs

    for point in to_deactivate:
      del self.points[point]
    for point, nbrs in to_activate.items():
      if len(nbrs) == 3:
        self.points[point] = 1

  def find_neighbors(self, point):
    x, y, z, w = point
    nbrs = []
    dw_items = [0]
    if self.four_d:
      dw_items += [-1, 1]
    for dx in (-1, 0, 1):
      for dy in (-1, 0, 1):
        for dz in (-1, 0, 1):
          for dw in dw_items:
            nbrs.append((x+dx, y+dy, z+dz, w+dw))

    nbrs.remove(point)
    return nbrs


  @staticmethod
  def load_from_file(path):
    dim = PocketDimension()
    x, y, z, w = 0, 0, 0, 0
    with open(path) as fp:
      for line in fp.readlines():
        for ch in line.strip():
          if ch == '#':
            dim.points[(x, y, z, w)] = 1
          x += 1
        x = 0
        y += 1
    return dim


if __name__ == '__main__':
  dim = PocketDimension.load_from_file('pocket_large.txt')
  dim.four_d = True
  for _ in range(6):
    dim.evolve()
  print(f'Active cubed = {dim.get_active_cubes()}')
