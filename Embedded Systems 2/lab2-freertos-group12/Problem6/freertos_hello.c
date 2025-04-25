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

/* Freescale includes. */
#include "fsl_device_registers.h"
#include "fsl_debug_console.h"
#include "pin_mux.h"
#include "clock_config.h"
#include "board.h"
#include "semphr.h"

#include "timers.h"

/*******************************************************************************
 * Definitions
 ******************************************************************************/
SemaphoreHandle_t semaphore;

/* Task priorities. */
#define hello_task_PRIORITY (configMAX_PRIORITIES - 1)
/*******************************************************************************
 * Prototypes
 ******************************************************************************/
static void hello_task(void *pvParameters);

/*******************************************************************************
 * Code
 ******************************************************************************/
/*!
 * @brief Application entry point.
 */
void timerCallbackFunction(TimerHandle_t timer_handle)
{
	PRINTF("Hello from the single shot timer callback.\r\n");
}

void timerCallbackFunction2(TimerHandle_t timer_handle)
{
	xSemaphoreGive(semaphore);

//	static int counter = 0;
//	PRINTF("Hello from the periodic timer callback. Counter = %d\r\n", counter);
//	counter++;
//
//	if(counter >= 10)
//		xTimerStop(timer_handle, 0);
}

void timer_task(void *pvParameters)
{
	while(1) {
		xSemaphoreTake(semaphore, portMAX_DELAY);
		PRINTF("Yup, the timer and semaphore worked\n");
	}
}

int main(void)
{
	BaseType_t status;
	/* Init board hardware. */
	BOARD_InitBootPins();
	BOARD_InitBootClocks();
	BOARD_InitDebugConsole();

	semaphore = xSemaphoreCreateBinary();

	status = xTaskCreate(hello_task, "Hello_task", 200, NULL, 2, NULL);
	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	status = xTaskCreate(timer_task, "timer_task", 200, NULL, 3, NULL);
	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

	TimerHandle_t timer_handle = xTimerCreate("Single Shot timer",
												5000 / portTICK_PERIOD_MS,
												pdFALSE,
												NULL,
												timerCallbackFunction);

	status = xTimerStart(timer_handle, 0);
	if(status != pdPASS)
	{
		PRINTF("Couldn’t start the timer!.\r\n");
		while (1);
	}

	TimerHandle_t timer_handle2 = xTimerCreate("Periodic timer",
												1000 / portTICK_PERIOD_MS,
												pdTRUE,
												NULL,
												timerCallbackFunction2);

	status = xTimerStart(timer_handle2, 0);

	if(status != pdPASS)
	{
		PRINTF("Couldn’t start the timer!.\r\n");
		while (1);
	}

	vTaskStartScheduler();
	while (1)
	{}
}

/*!
 * @brief Task responsible for printing of "Hello world." message.
 */
static void hello_task(void *pvParameters)
{
    for (;;)
    {
        PRINTF("Hello world.\r\n");
        vTaskSuspend(NULL);
    }
}
