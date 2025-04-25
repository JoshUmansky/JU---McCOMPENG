from getpass import getpass
import socket
import argparse
import sys
import csv
import hashlib
import ast

######################
#lab 2 server class
######################

class Server:
    HOSTNAME = '127.0.0.1'
    PORT = 50000

    RECV_BUFFER_SIZE = 1024
    MAX_CONNECTION_BACKLOG = 10
    
    MSG_ENCODING = "utf-8"

    SOCKET_ADDRESS = (HOSTNAME, PORT)

    
    def __init__(self):
        self.create_listen_socket()
        self.students = {}
        self.getDatabase()
        self.process_connections_forever()
    
    def getDatabase(self):
        try:
            with open('course_grades.csv','r') as file:
                reader = csv.reader(file)

                for row in reader:
                    student_ids = row[0]
                    studentPass = row[1]
                    studentGrades = row[4:]

                    self.students[student_ids] = {
                        "password:": studentPass,
                        "grades": studentGrades
                    }
        except FileNotFoundError:
            print("Error: course_grades.csv not found")
            sys.exit(1)


    def hashCred(self, student_id, password):
        newHash = hashlib.sha256()
        newHash.update(student_id.encode("utf-8"))  
        newHash.update(password.encode("utf-8"))
        return newHash.digest()

    def create_listen_socket(self):
        try:
            # Create an IPv4 TCP socket.
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

            # Set socket layer socket options. This allows us to reuse
            # the socket without waiting for any timeouts.
            self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

            # Bind socket to socket address, i.e., IP address and port.
            self.socket.bind(Server.SOCKET_ADDRESS)

            # Set socket to listen state.
            self.socket.listen(Server.MAX_CONNECTION_BACKLOG)
            print("Listening on port {} ...".format(Server.PORT))
        except Exception as msg:
            print(msg)
            sys.exit(1)

    def process_connections_forever(self):
        try:
            while True:
                # Block while waiting for accepting incoming
                # connections. When one is accepted, pass the new
                # (cloned) socket reference to the connection handler
                # function.
                self.connection_handler(self.socket.accept())
        except Exception as msg:
            print(msg)
        except KeyboardInterrupt:
            print()
        finally:
            self.socket.close()
            sys.exit(1)

    def connection_handler(self, client):
        connection, address_port = client
        print("-" * 72)
        print("Connection received from {}.".format(address_port))

        while True:
            try:
                # Receive the command from the client
                recvd_bytes = connection.recv(Server.RECV_BUFFER_SIZE)
                if len(recvd_bytes) == 0:
                    print("Closing client connection ... ")
                    connection.close()
                    break
                
                recvd_str = recvd_bytes.decode(Server.MSG_ENCODING)
                print("Received command: " + recvd_str)
                command = recvd_str

                if command == "GG":
                    # Receive authentication details
                    recvd_bytes = connection.recv(Server.RECV_BUFFER_SIZE)
                    if len(recvd_bytes) == 0:
                        print("Closing client connection ... ")
                        connection.close()
                        break
                    
                    recvd_str = recvd_bytes.decode(Server.MSG_ENCODING)
                    print("Received authentication details: " + recvd_str)
                    studentID, receivedHash = recvd_str.split(",")
                    receivedHash = ast.literal_eval(receivedHash)
                    
                    if studentID in self.students:
                        storedPswrd = self.students[studentID]["password:"]
                        hashExpected = self.hashCred(studentID, storedPswrd)
                        print("Expected Hash:", hashExpected)
                        if(hashExpected == receivedHash):
                            print("Authentication Successful")
                            connection.sendall("Authentication Successful".encode(Server.MSG_ENCODING))
                            print(f"student {studentID} authenticated")

                            # Process the command after successful authentication
                            grades = self.students[studentID]["grades"]
                            gradeLabels = ["Midterm", "Lab 1", "Lab 2", "Lab 3", "Lab 4"]
                            grades = [f"{gradeLabels[i]}: {grades[i]}" for i in range(len(grades))]
                            connection.sendall(str(grades).encode(Server.MSG_ENCODING))
                        else:
                            connection.sendall("Authentication Failed".encode(Server.MSG_ENCODING))                        
                            connection.close()
                            return
                    else:
                        connection.sendall("Authentication Failed: wrong ID".encode(Server.MSG_ENCODING))
                        connection.close()
                        return

                elif command == "GMA":
                    midGrades = [int(s["grades"][0]) for s in self.students.values() if s["grades"][0].isdigit()] 
                    average = sum(midGrades)/len(midGrades)                                                      
                    connection.sendall(f"Midterm Average: {average}".encode(Server.MSG_ENCODING))
                    
                elif command.startswith("GL"):
                    labIndex = int(command[2])
                    labGrades = [int(s["grades"][labIndex]) for s in self.students.values() if s["grades"][labIndex].isdigit()] 
                    average = sum(labGrades)/len(labGrades)
                    connection.sendall(f"Lab {labIndex} Average: {average}".encode(Server.MSG_ENCODING))

            except KeyboardInterrupt:
                print()
                print("Closing client connection ... ")
                connection.close()                
                break

if __name__ == "__main__":
    Server()