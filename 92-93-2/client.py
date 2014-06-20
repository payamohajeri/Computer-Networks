import socket
import time
import select

UDP_IP = "127.0.0.1"
UDP_PORT = 6027
UDP_PORT_2 = 10017
#Window Size = 5

clientName = raw_input("Please enter client name in 7 characters: ")
while (len(clientName) != 7):
	print "Your client name must be 7 character"
	clientName = raw_input("Please enter client name in 7 characters: ")

data=""
firstCount = 0
secondCount = 5

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock2 = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock2.bind((UDP_IP, UDP_PORT_2))
sock2.setblocking(0)

f=open("input.txt","r", 0)

packets = ['','','','','']

while True:
	for i in range(firstCount, secondCount, 1):
		num=i%256
		index = i%5
		now = time.time()
		data=f.read(255)
		Message = clientName + chr(num) + chr(len(data)) + data
		packets[index] = Message
		sock.sendto(Message, (UDP_IP, UDP_PORT))
	later = time.time()
	while( float(later - now) < 5) :
		later = time.time()
		ready = select.select([sock2],[],[],0.1)
		if ready[0] :
			data2, addr = sock2.recvfrom(1024)
			seqNum = ord(data2[3:len(data2)])
			if(seqNum>=firstCount and seqNum<secondCount):
				for i in range(seqNum, secondCount, 1):
					sock.sendto(packets[i%5], (UDP_IP, UDP_PORT))

	firstCount += 5
	secondCount += 5
