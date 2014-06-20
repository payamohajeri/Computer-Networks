import socket
from random import randint
from threading import Thread

UDP_IP = "127.0.0.1"
UDP_PORT = 6027
UDP_PORT_2 = 10017

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock2 = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((UDP_IP, UDP_PORT))
clients = {}
f=open("log.txt","w", 0)

while True:
	#listening .... 
	data, addr = sock.recvfrom(1024)
	if(not(clients.has_key(addr[0]))):
		clients[addr[0]] = {addr[1]:0}
	elif(not(clients[addr[0]].has_key(addr[1]))):
		clients[addr[0]][addr[1]] = 0
	seq = clients[addr[0]][addr[1]]
	#parsing ...
	clientName = data[0:7]
	seqNum = ord(data[7])
	dataSize = ord(data[8])
	message = data[9:len(data)]
	#drop ...
	if((seq!=seqNum-1) and not(seqNum -1 < 0)):
		#print "Real lost"
		sock2.sendto("NAK" + chr(seq+1), (addr[0], UDP_PORT_2))
		continue
	if (randint(0,100)%4 == 0):
		#print "random lost"
		sock2.sendto("NAK" + chr(seqNum), (addr[0], UDP_PORT_2))
		continue
	clients[addr[0]][addr[1]] = seqNum
	f.write("[ " + clientName + " " + str(seqNum) + " ] " + " : " + message + "\n")
	f.flush()
f.close()
