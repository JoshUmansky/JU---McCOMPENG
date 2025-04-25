# Compiler
CC = gcc

# Compiler Flags
CFLAGS = -Wall -Werror -g

# Source files
SRC = coe4dk4_lab_1_2024.c simlib.c

# Object files
OBJ = coe4dk4_lab_1_2024.o simlib.o

# Header files
HEADERS = simlib.h trace.h

# Output executable name
OUT = lab1

# Default rule (first rule is executed by default)
all: $(OUT)

# Rule to create the executable
$(OUT): $(OBJ)
	$(CC) $(CFLAGS) -o $(OUT) $(OBJ)

# Rule to create object files from source files
%.o: %.c $(HEADERS)
	$(CC) $(CFLAGS) -c $<

# Clean up generated files
clean:
	rm -f $(OBJ) $(OUT)