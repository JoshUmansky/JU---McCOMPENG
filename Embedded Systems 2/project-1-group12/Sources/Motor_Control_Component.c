#include "Motor_Control_Component.h"

QueueHandle_t motor_queue;
QueueHandle_t angle_queue;

void setupMotorComponent()
{
	printf("Start of motor controller");
	setupMotorPins();

	setupDCMotor();
	setupServo();

    /*************** Motor Task ***************/
	BaseType_t status;
	//Create Motor Queue
	motor_queue = xQueueCreate(1, sizeof(int));

	if (motor_queue == NULL)
	{
	PRINTF("Queue creation failed!.\r\n");
	while (1);
	}

	//Create Motor Task
	status = xTaskCreate(motorTask, "Motor Task", 200, NULL, 2, NULL);
	printf("task made");
	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}

    /*************** Position Task ***************/
	//Create Angle Queue
	angle_queue = xQueueCreate(1, sizeof(int));
	if (angle_queue == NULL)
	{
		PRINTF("Queue creation failed!.\r\n");
		while (1);
	}

	//Create Position Task
	status = xTaskCreate(positionTask, "Angle Task", 200, NULL, 2, NULL);

	if (status != pdPASS)
	{
		PRINTF("Task creation failed!.\r\n");
		while (1);
	}
	printf("End of motor setup");
}

void setupMotorPins()
{
	CLOCK_EnableClock(kCLOCK_PortA);
    CLOCK_EnableClock(kCLOCK_PortC);
	PORT_SetPinMux(PORTC, 1U, kPORT_MuxAlt4);
    PORT_SetPinMux(PORTA, 6U, kPORT_MuxAlt3);
}

void setupDCMotor()
{
	//Initialize PWM for DC motor
	ftm_config_t ftmInfo;
	ftm_chnl_pwm_signal_param_t ftmParam;
	ftm_pwm_level_select_t pwmLevel = kFTM_HighTrue;
	ftmParam.chnlNumber = FTM_CHANNEL_DC_MOTOR;
	ftmParam.level = pwmLevel;
	ftmParam.dutyCyclePercent = 7;
	ftmParam.firstEdgeDelayPercent = 0U;
	ftmParam.enableComplementary = false;
	ftmParam.enableDeadtime = false;
	FTM_GetDefaultConfig(&ftmInfo);
	ftmInfo.prescale = kFTM_Prescale_Divide_128;
	FTM_Init(FTM_MOTORS, &ftmInfo);
	FTM_SetupPwm(FTM_MOTORS, &ftmParam, 1U, kFTM_EdgeAlignedPwm, 50U, CLOCK_GetFreq(
	kCLOCK_BusClk));
	FTM_StartTimer(FTM_MOTORS, kFTM_SystemClock);
}

void setupServo()
{
	//Initialize PWM for Servo motor
	ftm_config_t ftmInfo;
	ftm_chnl_pwm_signal_param_t ftmParam;
	ftm_pwm_level_select_t pwmLevel = kFTM_HighTrue;
	ftmParam.chnlNumber = FTM_CHANNEL_SERVO;
	ftmParam.level = pwmLevel;
	ftmParam.dutyCyclePercent = 7;
	ftmParam.firstEdgeDelayPercent = 0U;
	ftmParam.enableComplementary = false;
	ftmParam.enableDeadtime = false;
	FTM_GetDefaultConfig(&ftmInfo);
	ftmInfo.prescale = kFTM_Prescale_Divide_128;
	FTM_Init(FTM_MOTORS, &ftmInfo);
	FTM_SetupPwm(FTM_MOTORS, &ftmParam, 1U, kFTM_EdgeAlignedPwm, 50U, CLOCK_GetFreq(
	kCLOCK_BusClk));
	FTM_StartTimer(FTM_MOTORS, kFTM_SystemClock);
}

void updatePWM_dutyCycle(ftm_chnl_t channel, float dutyCycle)
{
	uint32_t cnv, cnvFirstEdge = 0, mod;

	/* The CHANNEL_COUNT macro returns -1 if it cannot match the FTM instance */
	assert(-1 != FSL_FEATURE_FTM_CHANNEL_COUNTn(FTM_MOTORS));

	mod = FTM_MOTORS->MOD;
	if(dutyCycle == 0U)
	{
		/* Signal stays low */
		cnv = 0;
	}
	else
	{
		cnv = mod * dutyCycle;
		/* For 100% duty cycle */
		if (cnv >= mod)
		{
			cnv = mod + 1U;
		}
	}

	FTM_MOTORS->CONTROLS[channel].CnV = cnv;
}

void motorTask(void* pvParameters)
{
	int dcinput = 0;
	int speed = 0;
	BaseType_t status;
	float dutyCycle_DC;
	//Motor task implementation
	while(1){
		status = xQueueReceive(motor_queue, (void *)&speed, portMAX_DELAY);
		if(status != pdPASS){
			PRINTF("QUEUE receive failed!\r\n");
			while(1);
		}
		dutyCycle_DC = speed * 0.025f/100.0f + 0.0615;
		updatePWM_dutyCycle(FTM_CHANNEL_DC_MOTOR, dutyCycle_DC);
		FTM_SetSoftwareTrigger(FTM_MOTORS, true);
	}
	
}

void positionTask(void* pvParameters)
{
	//Position task implementation
	int angle = 0;
	BaseType_t status;
	float dutyCycle_Servo;
	//Motor task implementation
	while(1){
		status = xQueueReceive(angle_queue, (void *)&angle, portMAX_DELAY);
		if(status != pdPASS){
			PRINTF("QUEUE receive failed!\r\n");
			while(1);
		}

		dutyCycle_Servo = (((angle + 90)/180.0f) * 0.05f) + 0.05;
		updatePWM_dutyCycle(FTM_CHANNEL_SERVO, dutyCycle_Servo);
		FTM_SetSoftwareTrigger(FTM_MOTORS, true);
	}
}
