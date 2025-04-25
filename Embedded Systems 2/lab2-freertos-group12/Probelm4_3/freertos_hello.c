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
#include "event_groups.h"

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
void producer_sem(void* pvParameters);
void consumer1_sem(void* pvParameters);
void consumer2_sem(void* pvParameters);


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

TaskHandle_t input;
int main(void)
{
	BaseType_t status;

	/* Init board hardware. */
	BOARD_InitBootPins();
	BOARD_InitBootClocks();
	BOARD_InitDebugConsole();

	EventGroupHandle_t event_group = xEventGroupCreate();

	status = xTaskCreate(producer_sem, "producer", 200, (void*)event_group, 3, NULL);
	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	status = xTaskCreate(consumer1_sem, "consumer", 200, (void*)event_group, 2, NULL);
	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	status = xTaskCreate(consumer2_sem, "consumer", 200, (void*)event_group, 2, NULL);
	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	vTaskStartScheduler();
	while (1);
}

int counter = 0;

void producer_sem(void* pvParameters)
{
	EventGroupHandle_t event_group = (EventGroupHandle_t)pvParameters;
	EventBits_t bits;

	xEventGroupSetBits(event_group, 0x3);

	while(1)
	{
		xEventGroupWaitBits(event_group,
							0x3,
							pdTRUE,
							pdTRUE,
							portMAX_DELAY);

		xEventGroupSetBits(event_group, 0xC);

		counter++;

		vTaskDelay(1000 / portTICK_PERIOD_MS);
	}
}

void consumer1_sem(void* pvParameters)
{
	EventGroupHandle_t event_group = (EventGroupHandle_t)pvParameters;
	EventBits_t bits;

	while(1)
	{
		xEventGroupWaitBits(event_group,
									1 << 3,
									pdTRUE,
									pdFALSE,
									portMAX_DELAY);

		PRINTF("Received Value = %d\r\n", counter);

		xEventGroupSetBits(event_group, 1 << 1);
	}
}

void consumer2_sem(void* pvParameters)
{
	EventGroupHandle_t event_group = (EventGroupHandle_t)pvParameters;
	EventBits_t bits;


	BaseType_t status;

	while(1)
	{
		xEventGroupWaitBits(event_group,
									1 << 2,
									pdTRUE,
									pdFALSE,
									portMAX_DELAY);

		PRINTF("Received Value = %d\r\n", counter);

		xEventGroupSetBits(event_group, 1 << 0);
	}
}
