#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define BufferSize 10


#define OFFSET_MASK 0x00000FFF
#define PAGES 8
#define OFFSET_BITS 12
#define PAGE_SIZE 4096

int main()
{
    FILE *fptr = fopen("labaddr.txt", "r");
    char buff[BufferSize];
    int page_table[PAGES] = {6,4,3,7,0,1,2,5};

    if (fptr == NULL)
    {
        printf("Error opening file\n");
        exit(1);
    }

    while (fgets(buff, BufferSize, fptr) != NULL)
    {
        buff[strcspn(buff, "\n")] = 0; //Remove newline character to match the lab doc
        
        int address = atoi(buff);
        int page_number = address >> OFFSET_BITS;
        int page_offset = address & OFFSET_MASK;
        int physical_address = (page_table[page_number] << OFFSET_BITS) | page_offset;

        printf("Virtual addr is %s:", buff);
        printf(", Page number: %d", page_number);
        printf(", Offset: %d", page_offset);
        printf(", Physical addr is %d\n", physical_address);
    }

    fclose(fptr);

    return 0;
}