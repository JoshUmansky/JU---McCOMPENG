import socket
import hashlib
import argparse
import os

# Constants
CMD_FIELD_LEN = 1
FILE_SIZE_FIELD_LEN = 8

SERVER_HOSTNAME = 'localhost'
UDP_PORT = 30000
TCP_PORT = 30001
MSG_ENCODING = "utf-8"
RECV_BUFFER_SIZE = 1024
FILE_NOT_FOUND_MSG = "Error: Requested file is not available!"

COMMANDS = { "SD": 1,
             "GET": 2,
             "PUT": 3,
             "RLIST": 4,
             "BYE": 5,
             "SCAN": 6 }

class Client:
    def __init__(self):
        self.client_udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) #Create client socket
        self.client_udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        self.client_udp_socket.settimeout(5)
        self.client_tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_udp_address = (SERVER_HOSTNAME, UDP_PORT)
        self.server_tcp_address = (SERVER_HOSTNAME, TCP_PORT)

        #self.connect_to_server()
        self.run()

    def connect_to_server(self, address):
        print(f"Connecting to {address}")
        try:
            self.client_tcp_socket.connect(self.server_tcp_address)
        except Exception as msg:
            print(msg)
            exit()

    def scan(self):
        try:
            scan_str = "SERVICE DISCOVERY"
            self.client_udp_socket.sendto(scan_str.encode(MSG_ENCODING), ('0.0.0.0',UDP_PORT))
            respones_d, address = self.client_udp_socket.recvfrom(RECV_BUFFER_SIZE)
            ip, port = address
            print_str = f"{respones_d.decode(MSG_ENCODING)} found at {ip}/{port}"
            print(print_str)
        except TimeoutError:
            print("Scan timed out.")

    def run(self):
        #TODO tell user if socket is not connected when requesting something of the server
        #TODO implement bye to close server connection, change errors to disconnect server and not crash

        try:
            while True:
                msg = input("Enter command: ")

                args = msg.split(" ")
                command = args[0].lower()

                if (command == "connect"):
                    if (not (isinstance(args[1], int) and isinstance(args[2], int)) or len(args) < 3):
                        print("To connect please give a valid address and port in the format\tconnect <ip> <port>")
                        continue
                    self.connect_to_server((args[1], args[2]))
                elif (command == "scan"):
                    print("Scanning for file sharing service ...")
                    self.scan()
                elif (command == "get"):
                    put_field = COMMANDS["GET"].to_bytes(CMD_FIELD_LEN, byteorder='big')
                    filename = args[1]

                    if not filename:
                        print("No file provided")
                        continue

                    filename_field = filename.encode(MSG_ENCODING)
                    pkt = put_field + filename_field

                    self.client_tcp_socket.sendall(pkt)

                    file_size_bytes = self.socket_recv_size(FILE_SIZE_FIELD_LEN)
                    if (len(file_size_bytes) == 0):
                        self.client_tcp_socket.close()
                        return
                    
                    file_size = int.from_bytes(file_size_bytes, byteorder='big')
                    print(f"File size {file_size}")

                    # receive the file
                    recvd_bytes_total = bytearray()
                    try:
                        while len(recvd_bytes_total) < file_size:
                            recvd_bytes_total += self.client_tcp_socket.recv(RECV_BUFFER_SIZE)
                        
                        print(f"Received {len(recvd_bytes_total)} bytes. Creating file: {filename}")
                        
                        with open(filename, 'w') as f:
                            f.write(recvd_bytes_total.decode(MSG_ENCODING))

                    except KeyboardInterrupt:
                        print()
                        exit(1)
                    except socket.error:
                        self.client_tcp_socket.close()
                        self.client_udp_socket.close()
                elif (command == "put"):
                    put_field = COMMANDS["PUT"].to_bytes(CMD_FIELD_LEN, byteorder='big')

                    filename = args[1]
                    filename_field = filename.encode(MSG_ENCODING)
                    pkt = put_field + filename_field

                    if not filename:
                        print("No file provided")
                        continue

                    try:
                        file = open(filename, 'r').read()
                    except FileNotFoundError:
                        print(FILE_NOT_FOUND_MSG)
                        continue

                    self.client_tcp_socket.sendall(pkt)

                    self.client_tcp_socket.recv(RECV_BUFFER_SIZE)

                    file_bytes = file.encode(MSG_ENCODING)
                    file_size_bytes = len(file_bytes)
                    file_size_field = file_size_bytes.to_bytes(FILE_SIZE_FIELD_LEN, byteorder='big')
                    pkt = file_size_field + file_bytes

                    try:
                        self.client_tcp_socket.sendall(pkt)
                        print(f"Sending file: {filename}")
                    except socket.error:
                        print("Closing tcp connection")
                        self.client_tcp_socket.close()
                        return
                elif (command == "rlist"):
                    cmd_field = COMMANDS["RLIST"].to_bytes(CMD_FIELD_LEN, byteorder='big')
                    self.client_tcp_socket.sendall(cmd_field)
                    
                    print("-"*10 + "\nServer Files\n" + "-"*10)
                    dir_bytes = self.client_tcp_socket.recv(RECV_BUFFER_SIZE)
                    directory = dir_bytes.decode(MSG_ENCODING)
                    print(directory)
                elif (command == "llist"):
                    print("-"*10 + "\nLocal Files\n" + "-"*10)
                    for f in sorted(os.listdir()):
                        print(f)
                    print()
                elif (command == "bye"):
                    cmd_field = COMMANDS["BYE"].to_bytes(CMD_FIELD_LEN, byteorder='big')
                    self.client_tcp_socket.sendall(cmd_field)
                    self.client_tcp_socket.close()
        finally:
            print("Closing connection")
            self.client_udp_socket.close()

    def socket_recv_size(self, length):
        bytes = self.client_tcp_socket.recv(length)
        if len(bytes) < length:
            self.client_tcp_socket.close()
            exit()
        return(bytes)

if __name__ == "__main__": #Argument handling to run as either a server or a client
    parser = argparse.ArgumentParser(description="Run as either a server or a client")
    
    Client()