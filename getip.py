"""
A simple python server that returns your IPv4/IPv6 address
"""

from time import sleep
import socket

PORT = 8000
SLEEPTIME = 0.01
ls4 = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
if(socket.has_ipv6):
  ls6 = socket.socket(socket.AF_INET6,socket.SOCK_STREAM)

def main():
  print("Listening on port (IPv4): " + str(PORT))
  ls4.bind(('', PORT))
  ls4.settimeout(0.01)
  if(socket.has_ipv6):
    print("Has IPv6!")
    print("Listening on port (IPv6): " + str(PORT+1))
    ls6.bind(('', PORT+1))
    ls6.settimeout(0.01)

  while True:
    listenfornewconnections()
    try:
      sleep(SLEEPTIME)
    except KeyboardInterrupt:
      cleanup()
      exit()

def listenfornewconnections():
  try:
    ls4.listen(2)
    sock,name = ls4.accept()
    sock.settimeout(0.01)
    ip, port = name
    print("Incoming connection: " + str(ip))
    sock.send(ip)
    sock.close()
  except socket.timeout:
    pass
  except KeyboardInterrupt:
    cleanup()
    exit()
  if(socket.has_ipv6):
    try:
      ls6.listen(2)
      sock,name = ls6.accept()
      sock.settimeout(0.01)
      ip, port, a, b = name
      print("Incoming connection: " + str(ip))
      sock.send(ip)
      sock.close()
    except socket.timeout:
      pass
    except KeyboardInterrupt:
      cleanup()
      exit()

def cleanup():
  ls4.close()
  if(socket.has_ipv6):
    ls6.close()

main()
