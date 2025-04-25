#include "LED_Component.h"

QueueHandle_t led_queue;

void setupLEDComponent()
{
	setupLEDPins();

	setupLEDs();

    /*************** LED Task ***************/
	//Create LED Queue
    BaseType_t status;
	led_queue = xQueueCreate(1, sizeof(int));

	if (led_queue == NULL)
	{
	PRINTF("Queue creation failed!.\r\n");
	while (1);
	}

	
	//Create LED Tass
    status = xTaskCreate(ledTask, "Led Task", 200, NULL, 2, NULL);
	if (status != pdPASS)
	{
	PRINTF("Task creation failed!.\r\n");
	printf("End of led setup");
	while (1);
	}
}

void setupLEDPins()
{
	//Configure LED pins
    CLOCK_EnableClock(kCLOCK_PortC);
    CLOCK_EnableClock(kCLOCK_PortD);

    // BLUE
	PORT_SetPinMux(PORTC, 8U, kPORT_MuxAlt3);
    // GREEN
    PORT_SetPinMux(PORTC, 9U, kPORT_MuxAlt3);
    // RED
    PORT_SetPinMux(PORTD, 1U, kPORT_MuxAlt4);
}

void setupLEDs()
{
	//Initialize PWM for the LEDs
    ftm_config_t ftmInfo;
	ftm_chnl_pwm_signal_param_t ftmParam[3];

	ftmParam[0].chnlNumber = kFTM_Chnl_1;
	ftmParam[1].chnlNumber = kFTM_Chnl_4;
	ftmParam[2].chnlNumber = kFTM_Chnl_5;
	for (int i = 0; i < 3; i++) {
		ftmParam[i].level = kFTM_HighTrue;
		ftmParam[i].dutyCyclePercent = 0;
		ftmParam[i].firstEdgeDelayPercent = 0U;
		ftmParam[i].enableComplementary = false;
		ftmParam[i].enableDeadtime = false;
	}

	FTM_GetDefaultConfig(&ftmInfo);

	FTM_Init(FTM3, &ftmInfo);
	FTM_SetupPwm(FTM3, &ftmParam, 3U, kFTM_EdgeAlignedPwm, 5000U, CLOCK_GetFreq(kCLOCK_BusClk));
	FTM_StartTimer(FTM3, kFTM_SystemClock);
}

void ledTask(void* pvParameters)
{
	//LED task implementation
	int mode = 0;
	BaseType_t status;
	while(1){

		status = xQueueReceive(led_queue, (void *)&mode, portMAX_DELAY);
		//printf("dequeued!\n");
		int red = 0;
		int blue = 0;
		int green = 0;


		if(mode == 0){ //Green
			green = 100;
			red = 0;
			blue = 0;
		} else if(mode == 1){ //Yellow
			green = 100;
			red = 100;
			blue = 0;
		} else if(mode == 2){ //Red
			red = 100;
			green = 0;
			blue = 0;
		}

		FTM_UpdatePwmDutycycle(FTM3, FTM_RED_CHANNEL, kFTM_EdgeAlignedPwm, red);
		FTM_UpdatePwmDutycycle(FTM3, FTM_BLUE_CHANNEL, kFTM_EdgeAlignedPwm, blue);
		FTM_UpdatePwmDutycycle(FTM3, FTM_GREEN_CHANNEL, kFTM_EdgeAlignedPwm, green);
		FTM_SetSoftwareTrigger(FTM3, true);
	}
}
