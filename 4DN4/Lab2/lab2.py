import socket
import hashlib
import argparse
import sys
import csv
import ast

# Constants
SERVER_HOSTNAME = 'localhost'
SERVER_PORT = 50000
MSG_ENCODING = "utf-8"
RECV_BUFFER_SIZE = 1024
MAX_CONNECTION_BACKLOG = 10

class Server:
    SOCKET_ADDRESS = (SERVER_HOSTNAME, SERVER_PORT)
    
    def __init__(self):
        self.create_listen_socket()
        self.students = {}
        self.getDatabase()
        self.process_connections_forever()
    
    def getDatabase(self): #This function was created to read the course_grades.csv file and store the information in a dictionary
        try:
            print("Reading course_grades.csv ...")
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
                    print(row)
            
        except FileNotFoundError:
            print("Error: course_grades.csv not found")
            sys.exit(1)

    def hashCred(self, student_id, password): #This function was created to hash the student_id and password
        newHash = hashlib.sha256()
        newHash.update(student_id.encode("utf-8"))  
        newHash.update(password.encode("utf-8"))
        return newHash.digest()

    def create_listen_socket(self): #This function was created to create the socket and bind it to the server address
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.socket.bind(Server.SOCKET_ADDRESS)
            self.socket.listen(MAX_CONNECTION_BACKLOG)
            print(f"Listening on port {SERVER_PORT} ...")
        except Exception as msg:
            print(msg)
            sys.exit(1)

    def process_connections_forever(self): #This function was created to handle the connections and the commands that are sent by the client
        try:
            while True:
                self.connection_handler(self.socket.accept()) #This line was changed to pass the client socket to the connection_handler function
        except Exception as msg: #This line was added to catch any exceptions that may occur
            print(msg)
        except KeyboardInterrupt: #This line was added to catch the KeyboardInterrupt exception
            print()
        finally: #This line was added to close the socket when the program is terminated
            self.socket.close()
            sys.exit(1)

    def connection_handler(self, client): #This function was created to handle the commands that are sent by the client
        connection, address_port = client
        print("-" * 72)

        print(f"Connection received from {address_port}.")
        while True:
            try:
                recvd_bytes = connection.recv(RECV_BUFFER_SIZE) #Accepts the command from the client
                if len(recvd_bytes) == 0:
                    print("Closing client connection ... ")
                    connection.close()
                    break                

                recvd_str = recvd_bytes.decode(MSG_ENCODING) #Decodes the command from the client
                print(f"Received command: {recvd_str}")
                command = recvd_str 

                if command == "GG":
                    recvd_bytes = connection.recv(RECV_BUFFER_SIZE) #Retrieve student ID and password from client
                    if len(recvd_bytes) == 0:
                        print("Closing client connection ... ")
                        connection.close()
                        break
                    recvd_str = recvd_bytes.decode(MSG_ENCODING) #Decodes the student ID and password from the client
                    print(f"Received (Student ID, Hashed Password): {recvd_str}")
                    studentID, receivedHash = recvd_str.split(",")
                    receivedHash = ast.literal_eval(receivedHash) 
                    #This was used to convert the string to a byte array
                    #this was one of the major mistakes orignally, as we required the recieved information to be a byte array
                    #and not a string
                    
                    if studentID in self.students: #Loop through the dictonary to find the student that matches the login information
                        storedPswrd = self.students[studentID]["password:"] #Store password of student we are looking to see if matches the hashed password
                        hashExpected = self.hashCred(studentID, storedPswrd) #Hash the student ID and password
                        if hashExpected == receivedHash: #Compare the hashed password to the received hashed password, if they match, send back grades
                            print("Authentication Successful")
                            connection.sendall("Authentication Successful".encode(MSG_ENCODING))
                            grades = self.students[studentID]["grades"]
                            gradeLabels = ["Midterm", "Lab 1", "Lab 2", "Lab 3", "Lab 4"]
                            grades = [f"{gradeLabels[i]}: {grades[i]}" for i in range(len(grades))]
                            connection.sendall(str(grades).encode(MSG_ENCODING))
                        else: #Else, send back that the authentication failed for a incorrect password
                            print("Authentication Failed: wrong password")
                            connection.sendall("Authentication Failed".encode(MSG_ENCODING))                        
                            connection.close()
                            return
                    else: #Else, send back that the authentication failed for a incorrect student ID
                        print("Athentication Failed: wrong ID")
                        connection.sendall("Authentication Failed: wrong ID".encode(MSG_ENCODING))
                        connection.close()
                        return
                elif command == "GMA": #GMA does not require login info, return the average of the midterm grades
                    midGrades = [int(s["grades"][0]) for s in self.students.values() if s["grades"][0].isdigit()] #this line posed a problem as grades were being stored as strings and not integers
                    average = sum(midGrades)/len(midGrades)                                                       #On top of this, we only pulled values that had a studentID that was a digit
                    connection.sendall(f"Midterm Average: {average}".encode(MSG_ENCODING))                        #as both the header nad "Averages" student were being read originally
                elif command.startswith("GL"): #GLx does not require login info, return the average of the lab grades if x index is in range, otherwise return error
                    labIndex = int(command[2])
                    try:
                        labGrades = [int(s["grades"][labIndex]) for s in self.students.values() if s["grades"][labIndex].isdigit()] 
                        average = sum(labGrades)/len(labGrades)
                        connection.sendall(f"Lab {labIndex} Average: {average}".encode(MSG_ENCODING))
                    except IndexError:
                        connection.sendall(f"Lab {labIndex} does not exist".encode(MSG_ENCODING))
                elif command.lower() == 'exit': #Exit the connection
                    print("Closing client connection ... ")
                    connection.close()
                    break
                else: #If the command is invalid, send back invalid command (Error handling)
                    connection.sendall("Invalid command".encode(MSG_ENCODING))
            except Exception as msg: #This line was added to catch any exceptions that may occur
                print(msg)
                connection.close()
                break
            except KeyboardInterrupt: #This line was added to catch the KeyboardInterrupt exception
                print("Closing client connection ... ")
                connection.close()                
                break

class Client:
    def __init__(self):
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM) #Create client socket
        self.server_address = (SERVER_HOSTNAME, SERVER_PORT)
        print(f"Connecting to {self.server_address}")
        self.client_socket.connect(self.server_address) #Connect to server
        self.run()

    def run(self):
        try:
            while True:
                command = input("Enter command (GG, GMA, GLx where x is lab number, or 'exit' to quit): ")
                if command.startswith("GL"):                                               #All used to print the command as per the requirements
                    print("Fetching lab average for lab", command[2])                       #
                if command == "GMA":                                                       #
                    print("Fetching midterm average")                
                if command.lower() == 'exit':                                               
                    #send command for server to close connection
                    self.client_socket.sendall(command.encode(MSG_ENCODING))
                    break
                self.client_socket.sendall(command.encode(MSG_ENCODING)) #send command to server
                if command.lower() == 'gg': #If the command is gg, send the student ID and password to the server as well
                    student_id = input("Enter student ID: ")
                    password = input("Enter password: ")
                    print(f"Id Number: {student_id}, Password: {password} received")
                    hashed_password = hashlib.sha256((student_id + password).encode(MSG_ENCODING)).digest()
                    print("Hashed Password: ", hashed_password)
                    auth_message = f"{student_id},{hashed_password}"
                    self.client_socket.sendall(auth_message.encode(MSG_ENCODING))
                    response = self.client_socket.recv(RECV_BUFFER_SIZE).decode(MSG_ENCODING) #Receive the response from the server
                    print(f"Server response: {response}")
                    if "Authentication Successful" in response:
                        response = self.client_socket.recv(RECV_BUFFER_SIZE).decode(MSG_ENCODING) #If the authentication is successful, receive the grades from the server
                        print(f"Server response: {response}")
                else:
                    response = self.client_socket.recv(RECV_BUFFER_SIZE).decode(MSG_ENCODING) #Receive the response from the server
                    print(f"Server response: {response}")
        finally:
            print("Closing connection")
            self.client_socket.close()

if __name__ == "__main__": #Argument handling to run as either a server or a client
    parser = argparse.ArgumentParser(description="Run as either a server or a client")
    parser.add_argument("role", choices=["server", "client"], help="Choose whether to run as server or client")
    args = parser.parse_args()
    
    if args.role == "server":
        Server()
    elif args.role == "client":
        Client()
