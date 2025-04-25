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
static void hello_task(void *pvParameters);
static void hello_task2(void *pvParameters);
static void input_task(void *pvParameters);
void producer_queue(void* pvParameters);
void consumer_queue(void* pvParameters);
void producer_sem(void* pvParameters);
void consumer1_sem(void* pvParameters);
void consumer2_sem(void* pvParameters);
void vTaskDelete(TaskHandle_t pxTask);

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

	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t*) malloc(3 * sizeof(
	SemaphoreHandle_t));
	semaphores[0] = xSemaphoreCreateBinary(); //Producer semaphore
	semaphores[1] = xSemaphoreCreateBinary(); //Consumer semaphore
	semaphores[2] = xSemaphoreCreateCounting(2, 2);

	QueueHandle_t queue1 = xQueueCreate(1, sizeof(char));
	if (queue1 == NULL)
	{
	PRINTF("Queue creation failed!.\r\n");
	while (1);
	}
	status = xTaskCreate(producer_sem, "producer", 200, (void*)semaphores, 2, NULL);
	if (status != pdPASS)
	{
	PRINTF("Task creation failed!.\r\n");
	while (1);
	}
	status = xTaskCreate(consumer1_sem, "consumer", 200, (void*)semaphores, 2, NULL);
	if (status != pdPASS)
	{
	PRINTF("Task creation failed!.\r\n");
	while (1);
	}
	status = xTaskCreate(consumer2_sem, "consumer", 200, (void*)semaphores, 2, NULL);
	if (status != pdPASS)
	{
	PRINTF("Task creation failed!.\r\n");
	while (1);
	}
	vTaskStartScheduler();
	while (1)
	{}
}

/*!
 * @brief Task responsible for printing of "Hello world." message.
 */
void hello_task(void *pvParameters)
{
	while(1)
	{
		PRINTF("Hello World\r\n");
		vTaskDelay(1000 / portTICK_PERIOD_MS);
	}
}

void hello_task2(void *pvParameters)
{
	while(1)
	{
		PRINTF("Hello %s.\r\n", (char*) pvParameters);
		vTaskDelay(1000 / portTICK_PERIOD_MS);
	}
}
void input_task(void *pvParameters)
{
	while(1)
	{
		PRINTF("Hello, please enter a string: ");
		SCANF("%s", my_string);
		vTaskDelay(1000 / portTICK_PERIOD_MS);
		vTaskDelete(NULL);

	}
}
void producer_queue(void* pvParameters)
{
	QueueHandle_t queue1 = (QueueHandle_t)pvParameters;
	BaseType_t status;
	int counter = 0;

	PRINTF("ENTER INPUT STRING\r\n");
	scanf("%s", pass_string);
	StrLen = strlen(pass_string);

	while(counter <= StrLen)
	{
		if(counter <= StrLen){
			status = xQueueSendToBack(queue1, (void*) &pass_string[counter], portMAX_DELAY);
			counter++;
			if (status != pdPASS)
			{
			PRINTF("Queue Send failed!.\r\n");
			while (1);
			}
			vTaskDelay(1000 / portTICK_PERIOD_MS);
		}

	}
	vTaskDelete(NULL);
}

void consumer_queue(void* pvParameters)
{
	QueueHandle_t queue1 = (QueueHandle_t)pvParameters;
	BaseType_t status;
	int counter = 0;
	char c;
	while(1)
	{
		status = xQueueReceive(queue1, (void *) &c, portMAX_DELAY);
		rec_string[counter] = c;
		counter++;
		if (status != pdPASS)
		{
			PRINTF("Queue Receive failed!.\r\n");
			while (1);
		}
		if(counter == StrLen+1){
			//rec_string[counter] = '\0';
			while(1){
				PRINTF("%s\r\n", rec_string);
				vTaskDelay(1000 / portTICK_PERIOD_MS);
			}
		}
		//PRINTF("Received Value = %s\r\n", received_string);
	}
}

void producer_sem(void* pvParameters)
{
	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t*)pvParameters;
	SemaphoreHandle_t producer1_semaphore = semaphores[0];
	SemaphoreHandle_t producer2_semaphore = semaphores[1];
	SemaphoreHandle_t consumer_semaphore = semaphores[2];
	BaseType_t status1, status2;
	int counter = 0;

	PRINTF("ENTER INPUT STRING\r\n"); //scanf doesnt work w spaces
	scanf("%[^\n]", pass_string);
	StrLen = strlen(pass_string);

	while(counter <= StrLen)
		{
		status1 = xSemaphoreTake(consumer_semaphore, portMAX_DELAY);
		status2 = xSemaphoreTake(consumer_semaphore, portMAX_DELAY);
		if (status1 != pdPASS || status2 != pdPASS)
		{
			PRINTF("Failed to acquire consumer_semaphore\r\n");
			while (1);
		}
		//Before is setup, after is get string
		global_char = pass_string[counter];
		counter++;

		//this means string is ready
		xSemaphoreGive(producer1_semaphore);
		xSemaphoreGive(producer2_semaphore);

		while(counter == StrLen+1)

		vTaskDelay(1000 / portTICK_PERIOD_MS);
		}
}

void consumer1_sem(void* pvParameters)
{
	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t)pvParameters;
	SemaphoreHandle_t producer1_semaphore = semaphores[0];
	SemaphoreHandle_t consumer_semaphore = semaphores[2];
	//PRINTF("Strlen %d\n", StrLen);
	char print_string[StrLen];
	int counter = 0;
	BaseType_t status;
	while(1)
	{
		status = xSemaphoreTake(producer1_semaphore, portMAX_DELAY);

		if (status != pdPASS)
		{
			PRINTF("Failed to acquire producer1_semaphore\r\n");
			while (1);
		}

		print_string[counter] = global_char;
		counter++;

		if(counter == StrLen+1){
			//rec_string[counter] = '\0';
			while(1){
				PRINTF("%s\r", print_string);
				vTaskDelay(1000 / portTICK_PERIOD_MS);
			}
		}

		xSemaphoreGive(consumer_semaphore);
	}
}
void consumer2_sem(void* pvParameters)
{
	SemaphoreHandle_t* semaphores = (SemaphoreHandle_t*)pvParameters;
	SemaphoreHandle_t producer2_semaphore = semaphores[1];
	SemaphoreHandle_t consumer_semaphore = semaphores[2];
	BaseType_t status;
	char print_string[StrLen];
	int counter = 0;
	while(1)
	{
		status = xSemaphoreTake(producer2_semaphore, portMAX_DELAY);
		if (status != pdPASS)
		{
			PRINTF("Failed to acquire producer2_semaphore\r\n");
			while (1);
		}
		print_string[counter] = toupper(global_char);
		counter++;
		if(counter == StrLen+1){
		//rec_string[counter] = '\0';
				while(1){
					PRINTF("%s\r", print_string);
					vTaskDelay(1000 / portTICK_PERIOD_MS);
				}
			}
		xSemaphoreGive(consumer_semaphore);

	}
}
