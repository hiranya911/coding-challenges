import threading
import time
import sys

import d2
import nic_driver


ROUTER = {}
LAST_SEND = {'val': time.time()}


def send(addr, x, y):
    LAST_SEND['val'] = time.time()
    dest = ROUTER.get(addr, [])
    dest.append(x)
    dest.append(y)
    ROUTER[addr] = dest


class NetworkInterface(d2.InputDevice, d2.OutputDevice):

    def __init__(self, addr):
        self.addr = addr
        self.outgoing = []
        queue = ROUTER.get(addr, [])
        ROUTER[addr] = [addr] + queue

    def read(self):
        queue = ROUTER[self.addr]
        if queue:
            return queue.pop(0)
        
        if LAST_SEND.get('exit') is True:
            raise Exception('Halting...')
        time.sleep(0.1)
        return -1

    def write(self, value):
        self.outgoing.append(value)
        if len(self.outgoing) == 3:
            addr, x, y = self.outgoing
            send(addr, x, y)
            self.outgoing = []


class SimpleIOManager(d2.IOManager):

    def __init__(self, nic):
        self.nic = nic

    def get_input_device(self):
        return self.nic

    def get_output_device(self):
        return self.nic


class NAT(threading.Thread):

    def __init__(self):
        threading.Thread.__init__(self)
        self.x = None
        self.y = None
        self.read_x = True
        self.seen = set()

    def run(self):
        while True:
            self._receive()
            if self._is_idle():
                print(f'Sending wakeup to 0: {self.x} {self.y}')
                if self.y in self.seen:
                    print(f'-------- REPEATED SEND {self.y} --------')
                    LAST_SEND['exit'] = True
                    break
                self.seen.add(self.y)
                send(0, self.x, self.y)
                
    def _receive(self):
        queue = ROUTER.get(255, [])
        if not queue:
            time.sleep(0.1)
            return

        value = queue.pop(0)
        if self.read_x:
            self.x = value
            self.read_x = False
        else:
            if self.y is None:
                print(f'Received first packet at NAT: {self.x} {value}')
            self.y = value
            self.read_x = True

    def _is_idle(self):
        return time.time() - LAST_SEND['val'] > 2 and all(len(q) == 0 for q in ROUTER.values())


def bootup(addr):
    nic = NetworkInterface(addr)
    io = SimpleIOManager(nic)
    try:
        d2.run_program(nic_driver.INPUT, io)
    except Exception as ex:
        if LAST_SEND.get('exit') is not True:
            raise ex


def init_network():
    machines = []
    for i in range(50):
        t = threading.Thread(target=bootup, args=(i,))
        machines.append(t)
    machines.append(NAT())

    for m in machines:
        m.start()

    for m in machines:
        m.join()


init_network()

