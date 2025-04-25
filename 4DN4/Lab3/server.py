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
MSG_ENCODING = "utf-8"
RECV_BUFFER_SIZE = 1024
MAX_CONNECTION_BACKLOG = 10
DIRECTORY = "/home/user/school/4DN4/4DN4/Lab3/"
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
        self.create_udp_socket()
        self.create_tcp_socket()
        udp_thread = Thread(target=self.udp_loop)
        udp_thread.start()
        self.get_connections()

    def create_udp_socket(self):
        try:
            self.udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self.udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.udp_socket.bind(Server.UDP_ADDRESS)
            print(f"Listening for service discovery message on SDP port {UDP_PORT}")
        except Exception as msg:
            print(msg)
            sys.exit(1)
    
    def create_tcp_socket(self):
        try:
            self.tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.tcp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.tcp_socket.bind(Server.TCP_ADDRESS)
            self.tcp_socket.listen(MAX_CONNECTION_BACKLOG)
            print(f"Listening for file sharing connections on port {TCP_PORT}")
        except Exception as msg:
            print(msg)
            sys.exit(1)

    def udp_loop(self):
        try:
            while True:
                data, addr = self.udp_socket.recvfrom(RECV_BUFFER_SIZE)
                data_str = data.decode(MSG_ENCODING)
                print("received data: " + data_str)
                if("SERVICE DISCOVERY" in data_str.upper()):
                    response = f"Josh-Shanzeb-Johnny Sharing Service"
                    self.udp_socket.sendto(response.encode(MSG_ENCODING), addr)
        except Exception as msg:
            print(msg)
        except KeyboardInterrupt:
            print()
        finally:
            self.udp_socket.close()
            self.tcp_socket.close()
            sys.exit(1)

    def get_connections(self):
        try:
            while True:
                self.connection_handler(self.tcp_socket.accept())
        except Exception as msg:
            print(msg)
        except KeyboardInterrupt:
            print()
        finally:
            self.udp_socket.close()
            self.tcp_socket.close()
            sys.exit(1)

    def connection_handler(self, client):
        connection, address_port = client
        print("-"*72)
        print(f"Connection received from {address_port}.")

        while True:
            cmd = int.from_bytes(connection.recv(CMD_FIELD_LEN), byteorder='big')
            print()
            if (cmd == COMMANDS["GET"]):
                print("GET COMMAND RECEIVED")

                filename_bytes = connection.recv(RECV_BUFFER_SIZE)
                filename = filename_bytes.decode(MSG_ENCODING)
                try:
                    file = open(DIRECTORY+filename, 'r').read()
                except FileNotFoundError:
                    print(FILE_NOT_FOUND_MSG)
                    connection.close()
                    return
                
                file_bytes = file.encode(MSG_ENCODING)
                file_size_bytes = len(file_bytes)
                file_size_field = file_size_bytes.to_bytes(FILE_SIZE_FIELD_LEN, byteorder='big' )
                pkt = file_size_field + file_bytes

                try:
                    connection.sendall(pkt)
                    print(f"Sending file: {filename}")
                except socket.error:
                    print("Closing client connection ...")
                    connection.close()
                    return
            elif (cmd == COMMANDS["PUT"]):
                print("PUT COMMAND RECEIVED")
                filename_bytes = connection.recv(RECV_BUFFER_SIZE)
                filename = filename_bytes.decode(MSG_ENCODING)

                ack = "ack"
                ack_bytes = ack.encode(MSG_ENCODING)
                connection.sendall(ack_bytes)
                
                file_size_bytes = self.socket_recv_size(connection, FILE_SIZE_FIELD_LEN)
                if (len(file_size_bytes) == 0):
                    return

                file_size = int.from_bytes(file_size_bytes, byteorder='big')
                print(f"File size {file_size}")

                # receive the file
                recvd_bytes_total = bytearray()
                try:
                    while len(recvd_bytes_total) < file_size:
                        recvd_bytes_total += connection.recv(RECV_BUFFER_SIZE)

                    print(f"Received {len(recvd_bytes_total)} bytes. Creating file: {filename}")
                    
                    #TODO replace 1.py with filename
                    #temp
                    filename = DIRECTORY+"1.py"#filename
                    with open(filename, 'w') as f:
                        f.write(recvd_bytes_total.decode(MSG_ENCODING))
                except KeyboardInterrupt:
                    print()
                    exit(1)
                except socket.error:
                    self.tcp_socket.close()
            elif (cmd == COMMANDS["RLIST"]):
                output_str = ""
                for f in sorted(os.listdir()):
                    output_str += f + "\n"

                output_bytes = output_str.encode(MSG_ENCODING)
                connection.sendall(output_bytes)
            elif (cmd == COMMANDS["BYE"]):
                print(f"Closing client connection from {address_port}")
                connection.close()

    def socket_recv_size(self, connection, length):
        bytes = connection.recv(length)
        if len(bytes) < length:
            connection.close()
            exit()
        return(bytes)
    

if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    Server()
    