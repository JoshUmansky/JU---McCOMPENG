/*
 * Copyright (c) 2013 - 2015, Freescale Semiconductor, Inc.
 * Copyright 2016-2017 NXP
 * All rights reserved.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

#include "fsl_device_registers.h"
#include "fsl_debug_console.h"
#include "pin_mux.h"
#include "clock_config.h"
#include "board.h"

/*******************************************************************************
 * Definitions

 ******************************************************************************/
#define MEM_LOC(x) *((int*)x)
#define ARBITRARY_LOCATION MEM_LOC(0x20001004)
#define Loc1 *((char*)0x20001000)
#define Loc2 *((int*)0x20001001)
#define Loc3 *((short int*)0x20001005)
#define Loc4 *((int*)0x20001007)


void testFunction()
{
	int *ptr_location = (int*)0x20001000;
	*ptr_location = 11;

	ARBITRARY_LOCATION = 12;
	int x = ARBITRARY_LOCATION;
}

typedef struct __attribute__((__packed__))
{
	int location_1;
	char location_2;
	int location_3;
}ARBITRARY_MODULE;

#define MODULE ((ARBITRARY_MODULE*)0x20001000)

void testFunction2()
{
struct ARBITRARY_MODULE* module = (struct ARBITRARY_MODULE*)0x20001000;
MODULE->location_1 = 0xAAAAAAAA;
MODULE->location_2 = 0xBB;
MODULE->location_3 = 0xCCCCCCCC;
}


void Problem1(){
	Loc1 = 0xAC;
	Loc2 = 0xAABBCCDD;
	Loc3 = 0xABCD;
	Loc4 = 0xAABBCCDD;

}

/*******************************************************************************
 * Prototypes
 ******************************************************************************/

/*******************************************************************************
 * Code
 ******************************************************************************/
/*!
 * @brief Main function
 */
int main(void)
{
    char ch;

    /* Init board hardware. */
    BOARD_InitBootPins();
    BOARD_InitBootClocks();
    BOARD_InitDebugConsole();

    PRINTF("hello world.\r\n");
    //Problem1();
    testFunction2();
    while (1)
    {
        ch = GETCHAR();
        PUTCHAR(ch);
    }
}
