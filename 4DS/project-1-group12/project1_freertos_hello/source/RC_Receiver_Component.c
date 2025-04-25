#include "RC_Receiver_Component.h"
#include "fsl_uart.h"
SemaphoreHandle_t rc_hold_semaphore;
TaskHandle_t rc_task_handle;

typedef struct {
	uint16_t header;
	uint16_t ch1;
	uint16_t ch2;
	uint16_t ch3;
	uint16_t ch4;
	uint16_t ch5;
	uint16_t ch6;
	uint16_t ch7;
	uint16_t ch8;
} RC_Values;



void setupRCReceiverComponent()
{
	BaseType_t status;

	setupRCPins();

	setupUART_RC();

    /*************** RC Task ***************/
	//Create RC Semaphore
	status = xTaskCreate(rcTask, "RC Task", 200, NULL, 2, NULL);
	if (status != pdPASS)
	{
	PRINTF("Task creation failed!.\r\n");
	while (1);
	}
}

void setupRCPins()
{
	//Configure RC pins
	CLOCK_EnableClock(kCLOCK_PortC);

	PORT_SetPinMux(PORTC, 3u, kPORT_MuxAlt3);
}

void setupUART_RC()
{
	//setup UART for RC receiver
	uart_config_t config;
	UART_GetDefaultConfig(&config);

	config.baudRate_Bps = 115200;
	config.enableTx = false;
	config.enableRx = true;

	UART_Init(RC_UART, &config, CLOCK_GetFreq(kCLOCK_CoreSysClk));
}

void rcTask(void* pvParameters)
{

	RC_Values rc_values;
	uint8_t* ptr = (uint8_t*) &rc_values;
	BaseType_t status;
	int angle = 0;
	int mode = 0;
	int speed = 0;
	int direction = 1;
	//RC task implementation
	while (1)
		{
			UART_ReadBlocking(RC_UART, ptr, 1);
			//printf("After fist readblock");
			if(*ptr != 0x20)
				continue;


			UART_ReadBlocking(RC_UART, &ptr[1], sizeof(rc_values) - 1);
			//printf("After second readblock");
			//printf(rc_values.header);
			if(rc_values.header == 0x4020)
			{
				/*printf("Channel 1 = %d\t", rc_values.ch1);
				printf("Channel 2 = %d\t", rc_values.ch2);
				printf("Channel 3 = %d\t", rc_values.ch3);
				printf("Channel 4 = %d\t", rc_values.ch4);
				printf("Channel 5 = %d\t", rc_values.ch5);
				printf("Channel 6 = %d\t", rc_values.ch6);
				printf("Channel 7 = %d\t", rc_values.ch7);
				printf("Channel 8 = %d\r\n", rc_values.ch8);*/
				if(rc_values.ch5 == 1000)
					{direction = 1;}
				if(rc_values.ch5 == 2000)
					{direction = -1;}

				if(rc_values.ch6 == 1000){ //max 100% speed 0-100
					mode = 2;
					speed = direction * (rc_values.ch3 - 1000) / 10;
				} else if(rc_values.ch6 == 1500){ //max 75% speed 0-75
					mode = 1;
					speed = direction * (rc_values.ch3 - 1000) * 75 / 1000;
				} else if(rc_values.ch6 == 2000){ //max 50% speed 0-50
					mode = 0;
					speed = direction * (rc_values.ch3 - 1000) * 50 / 1000;
				}

				angle = (-180 + 0.36*(rc_values.ch1 - 1000)) / 2; //generates a value between -90 and 90

				printf("Speed: %d\n", speed);
				printf("Mode: %d\n", mode);
				printf("Angle: %d\n", angle);
				//printf("Before first queue");
				status = xQueueSendToBack(motor_queue, (void*) &speed, portMAX_DELAY);
				//printf("Before second queue");
				status = xQueueSendToBack(angle_queue, (void*) &angle, portMAX_DELAY);
//				printf("Before third queue\n");
				status = xQueueSendToBack(led_queue, (void*) &mode, portMAX_DELAY);
				//printf("enqueued!\n");
//				printf("After third queue\n");
			}
		}
}


