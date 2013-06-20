"""
A simple python server that returns your IPv4/IPv6 address
"""

from time import sleep
import socket

PORT = 49155
SLEEPTIME = 0.01
ls = socket.socket(socket.AF_INET,socket.SOCK_STREAM)

def main():
  print("Listening on port: " + str(PORT))
  ls.bind(('', PORT))
  ls.settimeout(0.01)

  while True:
    listenfornewconnections()
    try:
      sleep(SLEEPTIME)
    except KeyboardInterrupt:
      exit()

def listenfornewconnections():
  try:
    ls.listen(2)
    sock,name = ls.accept()
    sock.settimeout(0.01)
    ip,port = name
    print("Incoming connection: " + str(ip))
    sock.send(ip)
  except socket.timeout:
    pass
  except KeyboardInterrupt:
    exit()

main()
