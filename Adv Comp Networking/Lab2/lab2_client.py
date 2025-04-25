import socket
import hashlib

SERVER_HOSTNAME = 'localhost'
SERVER_PORT = 50000
MSG_ENCODING = "utf-8"
RECV_BUFFER_SIZE = 1024

def hashCred(student_id, password):
    newHash = hashlib.sha256()
    newHash.update(student_id.encode(MSG_ENCODING))
    newHash.update(password.encode(MSG_ENCODING))
    return newHash.digest()

def main():
    # Create a TCP/IP socket
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Connect the socket to the server
    server_address = (SERVER_HOSTNAME, SERVER_PORT)
    print(f"Connecting to {server_address}")
    client_socket.connect(server_address)

    try:
        while True:
            # Send command
            command = input("Enter command (GG, GMA, GLx where x is lab number, or 'exit' to quit): ")
            if command.lower() == 'exit':
                break
            client_socket.sendall(command.encode(MSG_ENCODING))
            if command.lower() == 'gg':
                # Send authentication details
                student_id = input("Enter student ID: ")
                password = input("Enter password: ")
                hashed_password = hashCred(student_id, password)
                auth_message = f"{student_id},{hashed_password}"
                client_socket.sendall(auth_message.encode(MSG_ENCODING))

                # Receive authentication response
                response = client_socket.recv(RECV_BUFFER_SIZE).decode(MSG_ENCODING)
                print(f"Server response: {response}")

                if "Authentication Successful" in response:
                    # Receive command response
                    response = client_socket.recv(RECV_BUFFER_SIZE).decode(MSG_ENCODING)
                    print(f"Server response: {response}")
            else:
                # Receive command response
                response = client_socket.recv(RECV_BUFFER_SIZE).decode(MSG_ENCODING)
                print(f"Server response: {response}")

    finally:
        print("Closing connection")
        client_socket.close()

if __name__ == "__main__":
    main()