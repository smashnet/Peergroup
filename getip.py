"""
A simple python server that returns your IPv4/IPv6 address
"""

from time import sleep
import socket

#If you want to host this yourself, change these to the external IPs of your server
IP4 = '37.120.160.33'
IP6 = '2a03:4000:6:3007::1'
PORT = 17533
SLEEPTIME = 0.01
ls4 = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
if(socket.has_ipv6):
  ls6 = socket.socket(socket.AF_INET6,socket.SOCK_STREAM)

def main():
  print("Listening on port (IPv4): " + str(PORT))
  ls4.bind((IP4, PORT))
  ls4.settimeout(0.01)
  if(socket.has_ipv6):
    print("Has IPv6!")
    print("Listening on port (IPv6): " + str(PORT))
    ls6.bind((IP6, PORT))
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
    print("Incoming connection: " + str(ip) + ":" + str(port))
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
      print("Incoming connection: [" + str(ip) + "]:" + str(port))
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
