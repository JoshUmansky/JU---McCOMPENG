#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>

//Defines
#define PAGE_SIZE 256
#define NUM_PAGES 256
#define TLB_SIZE 16
#define PHYSICAL_MEMORY_SIZE NUM_PAGES * PAGE_SIZE

//Indexing for circular queues
int pageFrame = 0;
int TLBFrame = 0;


typedef struct{
    int pageNumber;
    int frameNumber;
} TLBentry;

//Structures for memory emulation
int pageTable[NUM_PAGES];
TLBentry TLB[TLB_SIZE];
unsigned char physicalMemory[PHYSICAL_MEMORY_SIZE];

//Statistics
int totalAddresses = 0;
int pageFaults = 0;
int TLBhits = 0;

//Search
int search_TLB(int pageNumber){
    for(int i = 0; i < TLB_SIZE; i++){
        if(TLB[i].pageNumber == pageNumber){
            return TLB[i].frameNumber; //found
        }
    }
    return -1; //not found
}

//Add
void TLB_Add(int pageNumber, int frameNumber){
    TLB[TLBFrame].pageNumber = pageNumber;
    TLB[TLBFrame].frameNumber = frameNumber;
    TLBFrame = (TLBFrame + 1) % TLB_SIZE; //place at end of TLB, circular queue
    return;
}

//Update
void TLB_Update(int pageNumber, int frameNumber){
    for(int i = 0; i < TLB_SIZE; i++){
        if(TLB[i].pageNumber == pageNumber){
            TLB[i].frameNumber = frameNumber;
            return;
        }
    }
}

//Page Fault Handler
int HandlePageFault(int pageNumber, signed char *backing_store){
    if(pageFrame >= NUM_PAGES){
        pageFrame = pageFrame % NUM_PAGES; //circle queue
    }
    if(pageTable[pageFrame] != -1){ //invalidates TLB and pageTable
        TLB_Update(pageTable[pageFrame], -1);
        pageTable[pageFrame] = -1;
    }
    
    memcpy(&physicalMemory[pageFrame * PAGE_SIZE], &backing_store[pageNumber * PAGE_SIZE], PAGE_SIZE); //copy from backing store to physical memory
    pageTable[pageNumber] = pageFrame;
    pageFrame++;
    return pageFrame - 1; //Save the resource of an extra variable, simply return the pageFrame used
}


int main(void){
    FILE *addressFile;
    int logicalAddress;

    //Open files
    addressFile = fopen("addresses.txt", "r");
    if(addressFile == NULL){
        printf("Error: Unable to open file\n"); //Error Handling
        exit(1);
    }
    int bs_fd = open("BACKING_STORE.bin", O_RDONLY);
    if(bs_fd < 0){
        printf("Error: Unable to open file\n"); //Error Handling
        exit(1);
    }

    //Map backing store to memory
    signed char *backing_store = mmap(0, PHYSICAL_MEMORY_SIZE, PROT_READ, MAP_PRIVATE, bs_fd, 0);
    if(backing_store == MAP_FAILED){
        printf("Error: Unable to map file\n");
        exit(1);
    }

    //Initialize pageTable and TLB
    for(int i = 0; i < NUM_PAGES; i++){
        pageTable[i] = -1;
    }
    for(int i = 0; i < TLB_SIZE; i++){
        TLB[i].pageNumber = -1;
        TLB[i].frameNumber = -1;
    }
    
    //Main loop
    while(fscanf(addressFile, "%d", &logicalAddress) != EOF){
        int pageNumber = logicalAddress >> 8;
        int offset = logicalAddress & 255;
        int frameNumber;
        totalAddresses++;
        
        frameNumber = search_TLB(pageNumber);
        if(frameNumber != -1){
            TLBhits++;
        }
        else{//tlb miss
            frameNumber = pageTable[pageNumber];
            if(frameNumber == -1){
                pageFaults++;
                frameNumber = HandlePageFault(pageNumber, backing_store);
            }
            TLB_Add(pageNumber, frameNumber);
        }

        int physicalAddress = (frameNumber << 8) | offset;
        signed char value = physicalMemory[frameNumber * PAGE_SIZE + offset];
        printf("Virtual address: %d Physical address: %d Value: %d\n", logicalAddress, physicalAddress, value);        
    }
    printf("Total Addresses: %d\n", totalAddresses);
    printf("Page Faults: %d\n", pageFaults);
    printf("TLB Hits: %d\n", TLBhits);

    munmap(backing_store, PHYSICAL_MEMORY_SIZE);
    close(bs_fd);
    fclose(addressFile);
    return 0;
}
