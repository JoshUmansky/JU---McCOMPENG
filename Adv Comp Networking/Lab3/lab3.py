import socket
import argparse
import sys
import os
from threading import Thread

# Constants
CMD_FIELD_LEN = 1
FILE_SIZE_FIELD_LEN = 8

SERVER_HOSTNAME = '0.0.0.0'
UDP_PORT = 30000
TCP_PORT = 30001
WIFI_ADAPTER_IP = '192.168.2.54' 
WIFI_ADAPTER_IP = '172.23.145.239' #wsl
MSG_ENCODING = "utf-8"
RECV_BUFFER_SIZE = 1024
MAX_CONNECTION_BACKLOG = 10
DIRECTORY = "./"
FILE_NOT_FOUND_MSG = "Error: Requested file is not available!"

COMMANDS = { "SD": 1,
             "GET": 2,
             "PUT": 3,
             "RLIST": 4,
             "BYE": 5,
             "SCAN": 6 }

class Server:
    UDP_ADDRESS = (SERVER_HOSTNAME, UDP_PORT)
    TCP_ADDRESS = (SERVER_HOSTNAME, TCP_PORT)

    def __init__(self):
        self.create_udp_socket() #creates UDP socket for service discovery
        self.create_tcp_socket() #Create TCP socket for file transfers
        self.client_threads = []
        udp_thread = Thread(target=self.udp_loop)
        udp_thread.start()
        self.get_connections()

    def create_udp_socket(self): #Function that handles the creation of a UDP socket
        try:
            self.server_udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self.server_udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) #Sets socket options to the socket level, can bind to addresses in use
            self.server_udp_socket.bind(Server.UDP_ADDRESS)
            print(f"Listening for service discovery message on SDP port {UDP_PORT}")
        except Exception as msg:
            print(msg)
            sys.exit(1)
    
    def create_tcp_socket(self): #Function handling the tcp socket creation 
        try:
            self.server_tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_tcp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_tcp_socket.bind(Server.TCP_ADDRESS)
            self.server_tcp_socket.listen(MAX_CONNECTION_BACKLOG) #Listens/waits for incoming connections
            print(f"Listening for file sharing connections on port {TCP_PORT}")
        except Exception as msg:
            print(msg)
            sys.exit(1)

    def udp_loop(self): #Continuously listens for incoming UDP messages on the socket
        try:
            while True:
                data, addr = self.server_udp_socket.recvfrom(RECV_BUFFER_SIZE) #Listens on udp socket for messages
                data_str = data.decode(MSG_ENCODING) #Converts byte object to string
                print("received data: " + data_str)
                if("SERVICE DISCOVERY" in data_str.upper()): #If the string is observed from the decoded message
                    response = f"Josh-Shanzeb-Johnny Sharing Service"
                    self.server_udp_socket.sendto(response.encode(MSG_ENCODING), addr) #Send the response back to the sender
        except Exception as msg:
            print(msg)
        except KeyboardInterrupt:
            print()
        finally:
            self.server_udp_socket.close()
            sys.exit(1)

    def get_connections(self):
        try:
            while True:
                client = self.server_tcp_socket.accept()
                tcp_thread = Thread(target=self.connection_handler, args=(client,))
                tcp_thread.daemon = True # Makes the thread exit when the main thread exits
                self.client_threads.append(tcp_thread)
                tcp_thread.start()

                self.client_threads = [t for t in self.client_threads if t.is_alive()]
                #self.connection_handler(self.server_tcp_socket.accept()) #Waits for client connection to accept then goes to handler
        except Exception as msg:
            print(msg)
        except KeyboardInterrupt:
            print()
        finally:
            self.server_udp_socket.close()
            self.server_tcp_socket.close()
            sys.exit(1)

    def connection_handler(self, client):
        connection, address_port = client
        print("-"*72)
        s_string = "s" if len(self.client_threads) > 1 else ""
        print(f"Connection received from {address_port}. Currently {len(self.client_threads)} threaded client{s_string}")

        while True:
            cmd = int.from_bytes(connection.recv(CMD_FIELD_LEN), byteorder='big') #Receives 1 byte of data and converts it to integer index for cmnd
            
            print()
            if (cmd == COMMANDS["GET"]):
                filename_bytes = connection.recv(RECV_BUFFER_SIZE)
                filename = filename_bytes.decode(MSG_ENCODING) #Filename the client is requesting

                print_str = f"| get command (1 byte) | {filename} |"
                print("-"*len(print_str))
                print(print_str)
                print("-"*len(print_str))

                try:
                    with open(DIRECTORY+filename, 'rb') as f:
                        file = f.read() #Attempt to open the file in read mode
                except FileNotFoundError: #Exception if file is not found
                    print(FILE_NOT_FOUND_MSG)
                    connection.close()
                    return
                
                file_size_bytes = len(file) #Convert to bytes
                file_size_field = file_size_bytes.to_bytes(FILE_SIZE_FIELD_LEN, byteorder='big' ) #File size to bytes
                pkt = file_size_field + file

                try:
                    connection.sendall(pkt) #Sending packet through tcp
                    print(f"Sending file: {filename}")
                except socket.error:
                    print("Closing client connection ...")
                    connection.close()
                    return
            elif (cmd == COMMANDS["PUT"]):
                filename_bytes = connection.recv(RECV_BUFFER_SIZE)
                filename = filename_bytes.decode(MSG_ENCODING) #Receive filename from client

                print_str = f"| put command (1 byte) | {filename} |"
                print("-"*len(print_str))
                print(print_str)
                print("-"*len(print_str))

                ack = "ack"
                ack_bytes = ack.encode(MSG_ENCODING) 
                connection.sendall(ack_bytes) #Send ack message back to client to confirm filename was received
                
                file_size_bytes = self.socket_recv_size(connection, FILE_SIZE_FIELD_LEN)
                if (len(file_size_bytes) == 0):
                    return

                file_size = int.from_bytes(file_size_bytes, byteorder='big') #Size of the file is received from client
                print(f"File size {file_size}")

                # receive the file
                recvd_bytes_total = bytearray() 
                try:
                    while len(recvd_bytes_total) < file_size: #using the file size as a constraint to ensure entire file is received
                        recvd_bytes_total += connection.recv(RECV_BUFFER_SIZE)

                    print(f"Received {len(recvd_bytes_total)} bytes. Creating file: {filename}")
                    
                    with open(filename, 'wb') as f:
                        f.write(recvd_bytes_total) #Writes the received conent from the bytearray
                except KeyboardInterrupt:
                    print()
                    exit(1)
                except socket.error:
                    self.server_tcp_socket.close()
            elif (cmd == COMMANDS["RLIST"]):
                print_str = f"| rlist command (1 byte) |"
                print("-"*len(print_str))
                print(print_str)
                print("-"*len(print_str))

                output_str = ""
                for f in sorted(os.listdir()): #List of all files in directory
                    output_str += f + "\n" #Iterate through each file and print the name

                output_bytes = output_str.encode(MSG_ENCODING) #Turn the strings into bytes to send back to client
                connection.sendall(output_bytes)
            elif (cmd == COMMANDS["BYE"]):
                print(f"Closing client connection from {address_port}")
                connection.close() #Closing connection between client and server
                self.client_threads = [t for t in self.client_threads if t.is_alive()] # Clean thread list
                print("Connection closed")
                return

    def socket_recv_size(self, connection, length): #If the received bytes is less than the expected length than close connection
        bytes = connection.recv(length)
        if len(bytes) < length:
            connection.close()
            exit()
        return(bytes)

########################################################################

class Client:
    def __init__(self): #Client initialization of udp and tcp sockets
        self.client_udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) #Create client socket
        self.client_udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1) #Enables broadcast support, needed for discovering servers
        self.client_udp_socket.bind((WIFI_ADAPTER_IP, 0)) # Must use the correct wifi adapter ip to work between network devices
        self.client_udp_socket.settimeout(5) #5 sec timeout if no response is received
        self.client_tcp_socket = None

        #self.connect_to_server()
        self.run()

    def connect_to_server(self, address):
        print(f"Connecting to {address}")
        try:
            self.client_tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM) #Create new tcp socket for file transfer
            self.client_tcp_socket.connect(address) 
        except Exception as msg:
            print(msg)
            exit()

    def scan(self):
        try:
            scan_str = "SERVICE DISCOVERY"
            self.client_udp_socket.sendto(scan_str.encode(MSG_ENCODING), ('255.255.255.255',UDP_PORT)) #broadcasts service discovery to all servers on local network
            respones_d, address = self.client_udp_socket.recvfrom(RECV_BUFFER_SIZE) #Received name and address of server
            ip, port = address
            print_str = f"{respones_d.decode(MSG_ENCODING)} found at {ip}/{port}"
            print(print_str)
        except TimeoutError:
            print("Scan timed out.")

    def close(self):
        self.client_tcp_socket.close()
        self.client_tcp_socket = None
        print("Closed the connection to the server")

    def run(self):
        try:
            while True:
                msg = input("Enter command: ")

                args = msg.split(" ")
                command = args[0].lower()

                if (command == "get" or command == "put" or command == "rlist" or command == "bye"):
                    if(self.client_tcp_socket == None): #Checks if a tcp socket has been connected to a server
                        print("Not connected to a server")
                        continue

                if (command == "connect"):
                    ### BULLET PROOFING
                    if (self.client_tcp_socket != None): #Checks if client is not already connected to a server
                        print("Already connected to a server")
                        continue

                    if (len(args) < 3):
                        print("Not enough arguments given")
                        continue

                    ip_split = args[1].split(".")
                    if (len(ip_split) != 4):
                        print("Invalid IP given")
                        continue

                    valid = True
                    for i in ip_split:
                        try:
                            int (i)
                        except:
                            print("Invalid IP given")
                            valid = False
                            break

                    if (not valid):
                        continue

                    try:
                        int(args[2])
                    except:
                        print("Invalid port given")
                        continue

                    self.connect_to_server((args[1], int(args[2]))) #Connect to server with the provided IP and port from command

                elif (command == "scan"): #Looking for open servers
                    print("Scanning for file sharing service ...")
                    self.scan()

                elif (command == "get"):
                    if len(args) < 2: #Checks if the filename was also given
                        print("No file provided")
                        continue

                    filename = args[1] 

                    get_field = COMMANDS["GET"].to_bytes(CMD_FIELD_LEN, byteorder='big') #Encodes Get command to byte

                    filename_field = filename.encode(MSG_ENCODING) #Encode filename
                    pkt = get_field + filename_field #Creates packet for server 

                    self.client_tcp_socket.sendall(pkt)

                    file_size_bytes = self.socket_recv_size(FILE_SIZE_FIELD_LEN) #If a file is received
                    if (len(file_size_bytes) == 0):
                        print("No file received, connection closed")
                        self.close()
                        continue
                    
                    file_size = int.from_bytes(file_size_bytes, byteorder='big') #byte of file size from server is turned to a integer value
                    print(f"File size {file_size}")

                    # receive the file
                    recvd_bytes_total = bytearray() #Bytearray for file content
                    try:
                        while len(recvd_bytes_total) < file_size: #File size is used to ensure entire file is received
                            recvd_bytes_total += self.client_tcp_socket.recv(RECV_BUFFER_SIZE)
                        
                        print(f"Received {len(recvd_bytes_total)} bytes. Creating file: {filename}")
                        
                        with open(filename, 'wb') as f:
                            f.write(recvd_bytes_total) #Write/overwrite content into the file requested

                    except KeyboardInterrupt:
                        print()
                        exit(1)
                    except socket.error:
                        print("Socket error occurred")
                        self.close()

                elif (command == "put"):
                    if len(args) < 2:
                        print("No file provided")
                        continue

                    filename = args[1]

                    get_field = COMMANDS["PUT"].to_bytes(CMD_FIELD_LEN, byteorder='big') #Command PUT is set to byte for server to receive

                    filename_field = filename.encode(MSG_ENCODING) #Encode filename client wants to send
                    pkt = get_field + filename_field #Creation of PUT packet

                    try:
                        with open(filename, 'rb') as f:
                            file = f.read()
                    except FileNotFoundError:
                        print(FILE_NOT_FOUND_MSG)
                        continue

                    self.client_tcp_socket.sendall(pkt) #Send the packet
                    self.client_tcp_socket.recv(RECV_BUFFER_SIZE) #Wait for ack

                    file_size_bytes = len(file) #Length of file in bytes
                    file_size_field = file_size_bytes.to_bytes(FILE_SIZE_FIELD_LEN, byteorder='big')
                    pkt = file_size_field + file #packet holding size and data in bytes, big endian

                    try:
                        self.client_tcp_socket.sendall(pkt) #Send the packet
                        print(f"Sending file: {filename}")
                    except socket.error:
                        print("Closing tcp connection")
                        self.close()
                        continue
                    
                elif (command == "rlist"):
                    cmd_field = COMMANDS["RLIST"].to_bytes(CMD_FIELD_LEN, byteorder='big') #Sending the RLIST command to server
                    self.client_tcp_socket.sendall(cmd_field)
                    
                    print("-"*10 + "\nServer Files\n" + "-"*10)
                    dir_bytes = self.client_tcp_socket.recv(RECV_BUFFER_SIZE) #Receiving the file names
                    directory = dir_bytes.decode(MSG_ENCODING) #Decoding and printing
                    print(directory)

                elif (command == "llist"):
                    print("-"*10 + "\nLocal Files\n" + "-"*10)
                    for f in sorted(os.listdir()): #Itterate through directory to view local files
                        print(f)
                    print()

                elif (command == "bye"):
                    cmd_field = COMMANDS["BYE"].to_bytes(CMD_FIELD_LEN, byteorder='big') #BYE command being sent to server to initiate closing
                    self.client_tcp_socket.sendall(cmd_field)
                    self.close()
                    print("Connection closed")
        finally:
            print("Closing connection")
            self.client_udp_socket.close()

    def socket_recv_size(self, length):
        bytes = self.client_tcp_socket.recv(length)
        if len(bytes) < length:
            return []
        return(bytes)

########################################################################

if __name__ == '__main__':
    roles = {'client': Client,'server': Server}
    parser = argparse.ArgumentParser()

    parser.add_argument('-r', '--role',
                        choices=roles, 
                        help='server or client role',
                        required=True, type=str)

    args = parser.parse_args()
    roles[args.role]()

########################################################################