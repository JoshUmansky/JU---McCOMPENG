#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define TOTAL_CYLINDERS 300
#define REQUEST_SIZE 20

//Disk Scheduling Algorithms Function Definitions
void fcfs(int requests[], int head);
void sstf(int requests[], int head);
void scan(int requests[], int head, int direction);
void cscan(int requests[], int head, int direction);
void look(int requests[], int head, int direction);
void clook(int requests[], int head, int direction);

// Function to compare two integers for qsort
int compare(const void *a, const void *b);


int main(int argc, char *argv[]) {
    //Command line arguments in the format <initial_head_position> <direction>
    //Direction can be either LEFT or RIGHT later converted to -1 or 1 for ease of use in function calls
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <initial_head_position> <direction>\n", argv[0]);
        return 1;
    }
    
    //Reading the request from the file
    FILE *file = fopen("request.bin", "r");
    if (!file) {
        perror("Error opening file");
        return 1;
    }

    
    int requests[REQUEST_SIZE];
    int initial_head_position = atoi(argv[1]);

    //Converting direction for simplicity
    int direction = (strcmp(argv[2], "LEFT") == 0) ? -1 : 1;
    //-1 for left, 1 for right
    
    //Fill the requests array with data from the file
    for(int i = 0; i < REQUEST_SIZE; i++) {
        fread(&requests[i], sizeof(int), 1, file);
    }
    fclose(file);

    //Initial prints for output
    printf("Total requests = %d\n", REQUEST_SIZE);
    printf("Initial Head Position: %d\n", initial_head_position);
    printf("Direction of Head: %s\n", (direction == -1) ? "LEFT" : "RIGHT");

    /*Function calls for all methods of disk scheduling algorithms (scan, cscan, look, clook using sorted_requests)
    Qsort algorithm used from C Standard Library with a compare method*/
    fcfs(requests, initial_head_position);
    sstf(requests, initial_head_position);

    //Sort the requests for SCAN and C-SCAN algorithms
    int sorted_requests[REQUEST_SIZE];
    memcpy(sorted_requests, requests, sizeof(requests));
    qsort(sorted_requests, REQUEST_SIZE, sizeof(int), compare); //Part of C Standard Library

    scan(sorted_requests, initial_head_position, direction);
    cscan(sorted_requests, initial_head_position, direction);
    look(sorted_requests, initial_head_position, direction);
    clook(sorted_requests, initial_head_position, direction);
    
    return 0;

}

// Function to compare two integers for qsort
int compare(const void *a, const void *b) {
    return (*(int*)a - *(int*)b);
}

// Disk Scheduling Algorithms Implementation

// First-Come, First-Served (FCFS) Disk Scheduling Algorithm
void fcfs(int requests[], int head) {
    int total_head_movement = 0;
    int current_position = head;

    printf("\nFCFS DISK SCHEDULING ALGORITHM:\n");
    for (int i = 0; i < REQUEST_SIZE; i++) {
        total_head_movement += abs(current_position - requests[i]);
        current_position = requests[i];
        printf("%d, ", requests[i]);
    }
    printf("\nFCFS - Total head movements = %d\n", total_head_movement);
}

// Shortest Seek Time First (SSTF) Disk Scheduling Algorithm
void sstf(int requests[], int head) {
    int total_head_movement = 0;
    int current_position = head;
    int completed[REQUEST_SIZE] = {0};
    int min_index, min_distance; //Tracking the minimum distance and index of the request, service the closest request

    printf("\nSSTF DISK SCHEDULING ALGORITHM:\n");
    for (int i = 0; i < REQUEST_SIZE; i++) {
        min_distance = TOTAL_CYLINDERS; //Maximum possible distance
        min_index = -1; //Resetting the index for each iteration

        for (int j = 0; j < REQUEST_SIZE; j++) {
            if (!completed[j]) { // If the request is not completed
                int distance = abs(current_position - requests[j]);
                if (distance < min_distance) { // Find the closest request
                    min_distance = distance;
                    min_index = j;
                }
            }
        }

        if (min_index != -1) { //Valid request found
            total_head_movement += min_distance; //Add to overall head movement
            current_position = requests[min_index];
            completed[min_index] = 1; // Mark the request as completed
            printf("%d, ", requests[min_index]);
        }
    }
    printf("\nSSTF - Total head movements = %d\n", total_head_movement);
}

// SCAN Disk Scheduling Algorithm
//Algorithm scans the disk in one direction and services all requests until it reaches the end of the disk, then reverses direction
//and services all requests in the opposite direction.
void scan(int requests[], int head, int direction) {
    int total_head_movement = 0;
    int current_position = head;
    int start_index = 0;

    printf("\nSCAN DISK SCHEDULING ALGORITHM:\n\n");

    if (direction == 1) { // Moving right
        // Find start index for requests to the right of the head
        for (int i = 0; i < REQUEST_SIZE; i++) {
            if (requests[i] >= head) {
                start_index = i;
                break;
            }
        }
        //Begin Processing
        for (int i = start_index; i < REQUEST_SIZE; i++) {
            total_head_movement += abs(current_position - requests[i]);
            current_position = requests[i];
            printf("%d, ", requests[i]);
            
        }
        //End of disk
        total_head_movement += (TOTAL_CYLINDERS - 1 - current_position);
        current_position = TOTAL_CYLINDERS - 1;
        printf("%d, ", current_position);

        //Reverse direction
        for (int i = start_index - 1; i >= 0; i--) {
            total_head_movement += abs(current_position - requests[i]);
            current_position = requests[i];
            printf("%d, ", requests[i]);
        }
    //Start direction was left, reversed process to right
    } else {
        for (int i = REQUEST_SIZE - 1; i >= 0; i--) {
            if (requests[i] <= head) {
                start_index = i;
                break;
            }
        }
        for (int i = start_index; i >= 0; i--) {
            total_head_movement += abs(current_position - requests[i]);
            current_position = requests[i];
            printf("%d, ", requests[i]);
        }
        total_head_movement += current_position;
        current_position = 0;
        printf("%d, ", current_position);

        for (int i = start_index + 1; i < REQUEST_SIZE; i++) {
            total_head_movement += abs(current_position - requests[i]);
            current_position = requests[i];
            printf("%d, ", requests[i]);
        }
    }
    printf("\n\nSCAN - Total head movements = %d\n", total_head_movement);
}

// C-SCAN Disk Scheduling Algorithm
//Similar to Scan, but circes around the disc instead of reversing direction
void cscan(int requests[], int head, int direction) {
    int total_head_movement = 0;
    int current_position = head;

    printf("\nC-SCAN DISK SCHEDULING ALGORITHM:\n\n");
    if (direction == 1) { // Moving right
        //Process requests
        for (int i = 0; i < REQUEST_SIZE; i++) {
            if (requests[i] >= current_position) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                printf("%d, ", requests[i]);
                
            }
        }
        //Once end of disk is reached, jump to the beginning
        total_head_movement += (TOTAL_CYLINDERS - current_position - 1);
        current_position = 0;
        total_head_movement += TOTAL_CYLINDERS - 1;

        //Continue processing requests from the beginning of the disk to the head
        for (int i = 0; i < REQUEST_SIZE; i++) {
            if (requests[i] < head) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                printf("%d, ", requests[i]);
            }
        }
    } else { // Moving left, reverse process of right
        for (int i = REQUEST_SIZE - 1; i >= 0; i--) {
            if (requests[i] <= current_position) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                printf("%d, ", requests[i]);
            }
        }
        total_head_movement += current_position;
        current_position = TOTAL_CYLINDERS - 1;
        total_head_movement += current_position;

        for (int i = REQUEST_SIZE - 1; i >= 0; i--) {
            if (requests[i] > head) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                printf("%d, ", requests[i]);
            }
        }
    }
    printf("\n\nC-SCAN - Total head movements = %d\n", total_head_movement);
}

// LOOK Disk Scheduling Algorithm
//Similar to scan, but does not go to the end of the disk, only services requests in the direction of the head, ultimately reversing direction
//Like a Look right, Look left movement (or vica versa if the start direction is the other way)
void look(int requests[], int head, int direction) {
    int total_head_movement = 0;
    int current_position = head;
    int completed[REQUEST_SIZE] = {0};
    int i;

    printf("\nLOOK DISK SCHEDULING ALGORITHM:\n\n");
    if (direction == 1) { // Moving right
        for (i = 0; i < REQUEST_SIZE; i++) { //Service requests in the right direction
            if (requests[i] >= current_position && !completed[i]) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                completed[i] = 1;
                printf("%d, ", requests[i]);
            }
        }
        for (i = REQUEST_SIZE - 1; i >= 0; i--) { //Service requests in the left direction
            if (!completed[i]) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                completed[i] = 1;
                printf("%d, ", requests[i]);
            }
        }
    } else { // Moving left
        for (i = REQUEST_SIZE - 1; i >= 0; i--) { //Service requests in the left direction
            if (requests[i] <= current_position && !completed[i]) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                completed[i] = 1;
                printf("%d, ", requests[i]);
            }
        }
        for (i = 0; i < REQUEST_SIZE; i++) { //Service requests in the right direction
            if (!completed[i]) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                completed[i] = 1;
                printf("%d, ", requests[i]);
            }
        }
    }
    printf("\n\nLOOK - Total head movements = %d\n", total_head_movement);
}

// C-LOOK Disk Scheduling Algorithm
//Similar to C-SCAN, but does not go to the end of the disk, only services requests in the direction of the head, ultimately jumping to the beginning of the disk
void clook(int requests[], int head, int direction) {
    int total_head_movement = 0;
    int current_position = head;

    printf("\nC-LOOK DISK SCHEDULING ALGORITHM:\n\n");

    if (direction == 1) { // Moving right
        for (int i = 0; i < REQUEST_SIZE; i++) {
            if (requests[i] >= current_position) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                printf("%d", requests[i]);
                if (i < REQUEST_SIZE - 1) {
                    printf(", ");
                }
            }
        }
        //Once end of disk is reached, jump to the beginning
        for (int i = 0; i < REQUEST_SIZE; i++) {
            if (requests[i] < head) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                printf(", %d", requests[i]);
            }
        }
    } else { // Moving left
        for (int i = REQUEST_SIZE - 1; i >= 0; i--) {
            if (requests[i] <= current_position) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                printf("%d", requests[i]);
                if (i > 0) {
                    printf(", ");
                }
            }
        }
        for (int i = REQUEST_SIZE - 1; i >= 0; i--) {
            if (requests[i] > head) {
                total_head_movement += abs(current_position - requests[i]);
                current_position = requests[i];
                printf(", %d", requests[i]);
            }
        }
    }
    printf("\n\nC-LOOK - Total head movements = %d\n", total_head_movement);
}

