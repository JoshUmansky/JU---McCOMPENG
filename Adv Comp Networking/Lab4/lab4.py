import socket
import argparse
import sys
import threading
from threading import Thread
import msvcrt

SERVER_HOSTNAME = '0.0.0.0'
TCP_CDRP_PORT = 30001
MSG_ENCODING = 'utf-8'
RECV_BUFFER_SIZE = 1024
MAX_CONNECTION_BACKLOG = 10

MULTICAST_TTL = 1  # Time-to-live for multicast packets
MULTICAST_IFACE = '127.0.0.1'  # Interface for multicast (use '0.0.0.0' for all interfaces)


COMMANDS = { "getdir": 1,
             "makeroom": 2,
             "deleteroom": 3,
             "bye": 4,
             "chat": 5,
             }


class Server:
    TCP_ADDRESS = (SERVER_HOSTNAME, TCP_CDRP_PORT)

    def __init__(self):
        self.client_threads = []
        self.chat_directory = {} #{room name: (ip, port)}
        self.create_tcp_socket()
        self.get_connections()        
    
    def create_tcp_socket(self):
        try:
            self.server_tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_tcp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_tcp_socket.bind(Server.TCP_ADDRESS)
            self.server_tcp_socket.listen(MAX_CONNECTION_BACKLOG) #Listens/waits for incoming connections
            print(f"Chat Room Directory Server listening on port {TCP_CDRP_PORT}")
        except Exception as msg:
            print(msg)
            sys.exit(1)
    
    def get_connections(self):
        try:
            while True:
                client = self.server_tcp_socket.accept()
                tcp_thread = Thread(target=self.connection_handler, args=(client,))
                tcp_thread.daemon = True # Makes the thread exit when the main thread exits
                self.client_threads.append(tcp_thread)
                tcp_thread.start()
                print(f"New connection from {client[1]}")


                self.client_threads = [t for t in self.client_threads if t.is_alive()]
        except Exception as msg:
            print(msg)
        except KeyboardInterrupt:
            print()
        finally:
            self.server_tcp_socket.close()
            sys.exit(1)
    
    def connection_handler(self, client):
        connection, address_port = client
        try:
            while True:
                cmd = int.from_bytes(connection.recv(1), byteorder='big') 

                if(cmd == COMMANDS["getdir"]):
                    if not self.chat_directory:
                        print(f"No chat rooms are available right now")
                    output = "Chat Rooms:\n"

                    for name,(ip,port) in self.chat_directory.items():
                        output += f"- {name} at {ip}:{port}\n"
                    outputBytes = output.encode(MSG_ENCODING)
                    connection.sendall(outputBytes)

                elif cmd == COMMANDS["makeroom"]:
                    data_byte = connection.recv(RECV_BUFFER_SIZE)
                    data = data_byte.decode(MSG_ENCODING).strip()
                    data_field = data.split()

                    chat_name, room_address, room_port = data_field
                    if any((room_address, room_port) == val for val in self.chat_directory.values()):
                        connection.sendall("Error: IP/PORT already in use".encode(MSG_ENCODING))
                        continue

                    # Add the chatroom to the directory
                    self.chat_directory[chat_name] = (room_address, room_port)
                    connection.sendall(f"Room '{chat_name}' created, on {room_address} with port {room_port}".encode(MSG_ENCODING))

                elif (cmd == COMMANDS["deleteroom"]):
                    room_name = connection.recv(RECV_BUFFER_SIZE).decode(MSG_ENCODING)
                    if room_name in self.chat_directory:
                        del self.chat_directory[room_name]
                        connection.sendall(f"Room '{room_name}' deleted.".encode(MSG_ENCODING))
                    else:
                        connection.sendall("Error: Room not found.".encode(MSG_ENCODING))

                elif cmd == COMMANDS["bye"]:
                    connection.sendall("Goodbye.".encode(MSG_ENCODING))
                    break

                elif cmd == COMMANDS["chat"]:
                    try:
                        room_name = connection.recv(RECV_BUFFER_SIZE).decode(MSG_ENCODING).strip()
                        if room_name in self.chat_directory:
                            ip, port = self.chat_directory[room_name]
                            connection.sendall(f"{ip} {port}".encode(MSG_ENCODING))
                        else:
                            connection.sendall("Error: Room not found.".encode(MSG_ENCODING))
                    except Exception as e:
                        print(f"Error in chat command: {e}")
                        connection.sendall("Error: Failed to process 'chat' command.".encode(MSG_ENCODING))

                else:
                    connection.sendall("Unknown command.".encode(MSG_ENCODING))
        except Exception as e:
            print(f"Exception handling client {address_port}: {e}")
        finally:
            print(f"Closing connection from {address_port}")
            connection.close()


class Client:
    def __init__(self): #Client initialization of udp and tcp sockets
        self.client_tcp_socket = None
        self.chatName = "Default"
        self.run()

    def connect_to_server(self, address):
        print(f"Connecting to {address}")
        try:
            self.client_tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM) #Create new tcp socket for file transfer
            self.client_tcp_socket.connect(address) 
        except Exception as msg:
            print(msg)
            exit()

    def close(self):
        self.client_tcp_socket.close()
        self.client_tcp_socket = None
        print("Closed the connection to the server") 

    def chat_mode(self, room_name, multicast_ip, multicast_port):
        print(f"Entering chat mode for room '{room_name}' (Ctrl+a to exit).")
        print(f"Chatting on {multicast_ip}:{multicast_port}")

        # Create a UDP socket for multicast
        udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

        # Bind to the multicast port
        udp_socket.bind(('', int(multicast_port)))

        # Join the multicast group
        mreq = socket.inet_aton(multicast_ip) + socket.inet_aton(MULTICAST_IFACE)
        udp_socket.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)     

        # Start a thread to listen for incoming messages
        def listen_for_messages():
            while True:
                try:
                    data, _ = udp_socket.recvfrom(RECV_BUFFER_SIZE)                    
                    print(f"\n{data.decode(MSG_ENCODING)}")

                except Exception as e:
                    if "10038" in str(e):
                        return
                    print(f"Error receiving message: {e}")
                    break

        listener_thread = threading.Thread(target=listen_for_messages, daemon=True)
        listener_thread.start()

        # Handle user input in chat mode
        try:
            while True: 
                # Prompt the user for input
                message = input("> ").strip()  # Wait for the user to press Enter                    

                if message == "\x01":  # Ctrl+a to exit
                    print("Exiting chat mode.")
                    break

                # Send the message to the multicast group
                udp_socket.sendto(f"{self.chatName}: {message}".encode(MSG_ENCODING),
                                (multicast_ip, int(multicast_port)))
        finally:
            # Leave the multicast group and close the socket
            
            udp_socket.setsockopt(socket.IPPROTO_IP, socket.IP_DROP_MEMBERSHIP, mreq)
            udp_socket.close()
            listener_thread.join()
            

    def run(self):
        try:
            while True:
                msg = input("Enter Command: ")
                args = msg.split()
                if not args:
                    continue
                command = args[0].lower()
                
                if (command == "getdir" or command == "makeroom" or command == "deleteroom" or command == "bye"):
                                    if(self.client_tcp_socket == None): #Checks if a tcp socket has been connected to a server
                                        print("Not connected to a server")
                                        continue

                if (command == "connect"):
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
                    
                elif command == "name":
                    if(len(args) < 2):
                        print("Invalid number of arguments: name <your name>")
                        continue
                    name = args[1:]
                    self.chatName = "".join(name)
                    print(f"Name set to '{self.chatName}'")
                elif command == "getdir":
                    self.client_tcp_socket.sendall(COMMANDS["getdir"].to_bytes(1, byteorder='big'))
                    data = self.client_tcp_socket.recv(RECV_BUFFER_SIZE)
                    print(data.decode(MSG_ENCODING))
                elif command == "makeroom":
                    if(len(args) < 4):
                        print("Invalid number of arguments: makeroom <room name> <ip> <port>")
                        continue
                    room_name = args[1]
                    room_ip = args[2]
                    room_port = args[3]

                    self.client_tcp_socket.sendall(COMMANDS["makeroom"].to_bytes(1, byteorder='big'))
                    data = f"{room_name} {room_ip} {room_port}".encode(MSG_ENCODING)
                    self.client_tcp_socket.sendall(data)
                    data = self.client_tcp_socket.recv(RECV_BUFFER_SIZE)
                    print(data.decode(MSG_ENCODING))

                elif command == "deleteroom":
                    if(len(args) < 2):
                        print("Invalid number of arguments: deleteroom <room name>")
                        continue
                    room_name = args[1]
                    self.client_tcp_socket.sendall(COMMANDS["deleteroom"].to_bytes(1, byteorder='big'))
                    data = room_name.encode(MSG_ENCODING)
                    self.client_tcp_socket.sendall(data)
                    data = self.client_tcp_socket.recv(RECV_BUFFER_SIZE)
                    print(data.decode(MSG_ENCODING))
                elif command == "bye":
                    self.client_tcp_socket.sendall(COMMANDS["bye"].to_bytes(1, byteorder='big'))
                    data = self.client_tcp_socket.recv(RECV_BUFFER_SIZE)
                    print(data.decode(MSG_ENCODING))
                    break
                elif command == "chat":
                    if len(args) < 2:
                        print("Invalid number of arguments: chat <room name>")
                        continue
                    room_name = args[1]
                    try:
                        self.client_tcp_socket.sendall(COMMANDS["chat"].to_bytes(1, byteorder='big'))                        
                        self.client_tcp_socket.sendall(room_name.encode(MSG_ENCODING))
                        response = self.client_tcp_socket.recv(RECV_BUFFER_SIZE).decode(MSG_ENCODING)

                        if response.startswith("Error"):
                            print(response)
                        else:
                            # Server should return multicast IP and port (e.g., "224.0.0.1 5000")
                            multicast_ip, multicast_port = response.split()
                            self.chat_mode(room_name, multicast_ip, multicast_port)
                    except Exception as e:
                        print(f"Error in chat mode: {e}")
        finally:
            self.close()

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
