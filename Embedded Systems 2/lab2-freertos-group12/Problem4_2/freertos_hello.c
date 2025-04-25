/*
 * Copyright (c) 2015, Freescale Semiconductor, Inc.
 * Copyright 2016-2017 NXP
 * All rights reserved.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/* FreeRTOS kernel includes. */
#include "FreeRTOS.h"
#include "task.h"
#include "queue.h"
#include "timers.h"
#include "semphr.h"
#include "event_groups.h"

#include "ctype.h"

/* Freescale includes. */
#include "fsl_device_registers.h"
#include "fsl_debug_console.h"
#include "pin_mux.h"
#include "clock_config.h"
#include "board.h"

/*******************************************************************************
 * Definitions
 ******************************************************************************/

/* Task priorities. */
#define hello_task_PRIORITY (configMAX_PRIORITIES - 1)
/*******************************************************************************
 * Prototypes
 ******************************************************************************/
void producer_event(void* pvParameters);
void consumer_event(void* pvParameters);

void u_consume(void* pvParameters);
void d_consume(void* pvParameters);
void l_consume(void* pvParameters);
void r_consume(void* pvParameters);


/*******************************************************************************
 * Code
 ******************************************************************************/
/*!
 * @brief Application entry point.
 */
#define MAX_LENGTH 30

char* str = "4DS";
char my_string[] = "";

char pass_string[MAX_LENGTH];
char rec_string[MAX_LENGTH];
int StrLen;
//int counter = 0;
char global_char;

TaskHandle_t input;
int main(void)
{
	BaseType_t status;

	/* Init board hardware. */
	BOARD_InitBootPins();
	BOARD_InitBootClocks();
	BOARD_InitDebugConsole();

	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t*) malloc(4 * sizeof(SemaphoreHandle_t));
	semaphores[0] = xSemaphoreCreateBinary(); // Up
	semaphores[1] = xSemaphoreCreateBinary(); // Down
	semaphores[2] = xSemaphoreCreateBinary(); // Left
	semaphores[3] = xSemaphoreCreateBinary(); // Right

	status = xTaskCreate(producer_event, "producer", 200, (void*)semaphores, 1, NULL);

	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	status = xTaskCreate(u_consume, "u_consumer", 200, (void*)semaphores, 2, NULL);

	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	status = xTaskCreate(d_consume, "d_consumer", 200, (void*)semaphores, 2, NULL);

	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	status = xTaskCreate(l_consume, "l_consumer", 200, (void*)semaphores, 2, NULL);

	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	status = xTaskCreate(r_consume, "r_consumer", 200, (void*)semaphores, 2, NULL);

	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	vTaskStartScheduler();
	while (1);
}

#define LEFT_BIT 	(1 << 0)
#define RIGHT_BIT 	(1 << 1)
#define UP_BIT 		(1 << 2)
#define DOWN_BIT 	(1 << 3)

void producer_event(void* pvParameters)
{
	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t*)pvParameters;

	BaseType_t status;
	char c;
	while(1)
	{
		scanf("%c", &c);
		switch(c)
		{
			case 'a':
				xSemaphoreGive(semaphores[2]);
				break;
			case 's':
				xSemaphoreGive(semaphores[1]);
				break;
			case 'd':
				xSemaphoreGive(semaphores[3]);
				break;
			case 'w':
				xSemaphoreGive(semaphores[0]);
				break;
		}
	}
}

void u_consume(void* pvParameters)
{
	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t*)pvParameters;
	SemaphoreHandle_t sem = semaphores[0];
	BaseType_t status;

	while(1){
		status = xSemaphoreTake(sem, portMAX_DELAY);
		PRINTF("Up\r\n");
	}
}

void d_consume(void* pvParameters)
{
	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t*)pvParameters;
	SemaphoreHandle_t sem = semaphores[1];
	BaseType_t status;

	while(1){
		status = xSemaphoreTake(sem, portMAX_DELAY);
		PRINTF("Down\r\n");
	}
}

void l_consume(void* pvParameters)
{
	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t*)pvParameters;
	SemaphoreHandle_t sem = semaphores[2];
	BaseType_t status;

	while(1){
		status = xSemaphoreTake(sem, portMAX_DELAY);
		PRINTF("Left\r\n");
	}
}

void r_consume(void* pvParameters)
{
	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t*)pvParameters;
	SemaphoreHandle_t sem = semaphores[3];
	BaseType_t status;

	while(1){
		status = xSemaphoreTake(sem, portMAX_DELAY);
		PRINTF("Right\r\n");
	}
}
