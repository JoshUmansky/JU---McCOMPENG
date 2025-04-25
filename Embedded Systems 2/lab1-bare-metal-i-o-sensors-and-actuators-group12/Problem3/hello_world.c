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
#include "fsl_ftm.h"
#include "fsl_uart.h"


/*******************************************************************************
 * Definitions
 ******************************************************************************/
#define FTM_MOTOR FTM0
#define FTM_CHANNEL_DC_MOTOR kFTM_Chnl_0
#define FTM_CHANNEL_SERVO_MOTOR kFTM_Chnl_3
#define TARGET_UART UART4

void setupPWM_Motor(){
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
	FTM_Init(FTM_MOTOR, &ftmInfo);
	FTM_SetupPwm(FTM_MOTOR, &ftmParam, 1U, kFTM_EdgeAlignedPwm, 50U, CLOCK_GetFreq(
	kCLOCK_BusClk));
	FTM_StartTimer(FTM_MOTOR, kFTM_SystemClock);
}
void setupPWM_Servo(){
	ftm_config_t ftmInfo;
	ftm_chnl_pwm_signal_param_t ftmParam;
	ftm_pwm_level_select_t pwmLevel = kFTM_HighTrue;
	ftmParam.chnlNumber = FTM_CHANNEL_SERVO_MOTOR;
	ftmParam.level = pwmLevel;
	ftmParam.dutyCyclePercent = 7;
	ftmParam.firstEdgeDelayPercent = 0U;
	ftmParam.enableComplementary = false;
	ftmParam.enableDeadtime = false;
	FTM_GetDefaultConfig(&ftmInfo);
	ftmInfo.prescale = kFTM_Prescale_Divide_128;
	FTM_Init(FTM_MOTOR, &ftmInfo);
	FTM_SetupPwm(FTM_MOTOR, &ftmParam, 1U, kFTM_EdgeAlignedPwm, 50U, CLOCK_GetFreq(
	kCLOCK_BusClk));
	FTM_StartTimer(FTM_MOTOR, kFTM_SystemClock);
}
void updatePWM_dutyCycle(ftm_chnl_t channel, float dutyCycle){
	uint32_t cnv, cnvFirstEdge = 0, mod;
	/* The CHANNEL_COUNT macro returns -1 if it cannot match the FTM instance */
	assert(-1 != FSL_FEATURE_FTM_CHANNEL_COUNTn(FTM_MOTOR));
	mod = FTM_MOTOR->MOD;
	if (dutyCycle == 0U){
		/* Signal stays low */
		cnv = 0;
	}
	else{
		cnv = mod * dutyCycle;
		/* For 100% duty cycle */
		if (cnv >= mod){
			cnv = mod + 1U;
			}
	}
	FTM_MOTOR->CONTROLS[channel].CnV = cnv;
}

/*******************************************************************************
 * Prototypes
 ******************************************************************************/
volatile char ch;
volatile int new_char = 0;
/*******************************************************************************
 * Code
 ******************************************************************************/
//**** CUSTOM CODE ****//
void setupUART()
{
    uart_config_t config;
    UART_GetDefaultConfig(&config);
    config.baudRate_Bps = 57600;
    config.enableTx = true;
    config.enableRx = true;
    config.enableRxRTS = true;
    config.enableTxCTS = true;
    UART_Init(TARGET_UART, &config, CLOCK_GetFreq(kCLOCK_BusClk));

    UART_EnableInterrupts(TARGET_UART, kUART_RxDataRegFullInterruptEnable);
    EnableIRQ(UART4_RX_TX_IRQn);
}

void UART4_RX_TX_IRQHandler()
{
	UART_GetStatusFlags(TARGET_UART);
	ch = UART_ReadByte(TARGET_UART);
	new_char = 1;
}

int get_uart_num() {
    char rxbuff[3];  // Read 3 characters at a time
    int input_num[4];
    int last = 0;

    while (1) {
        UART_ReadBlocking(TARGET_UART, rxbuff, 3);

        // Check for the end character (carriage return)
        if (rxbuff[0] == 13)  // Assuming '\r' is the end signal
            break;


        PRINTF("%c", rxbuff[0]);  // Print the first byte

        // Assuming the input is numeric and we want to store each digit
        if (last < 4) {
            input_num[last] = rxbuff[0] - '0';  // Convert char digit to integer
            last++;
        }


        //PRINTF("%d", last);
    }

    PRINTF("!\n");

    // Convert the input_num array to an integer
    int num = 0;
    for (int i = 0; i < last; i++) {
        num = num * 10 + input_num[i];
    }
    PRINTF("%d", num);
    PRINTF("\n");
    return num;
}

/*!
 * @brief Main function
 */
int main(void)
{
	//char ch = 'c';
	char txbuff [] = "Helo World\r\n";
	char rxbuff;
	char input_num[4] = "";
	int servoinput;
	int dcinput;
	float dutyCycle_DC, dutyCycle_Servo;

	BOARD_InitBootPins();
	BOARD_InitBootClocks();
	BOARD_InitDebugConsole();

	setupPWM_Motor();
	setupPWM_Servo();
	setupUART();

	/******* Delay *******/
	for(volatile int i = 0U; i < 1000000; i++)
	__asm("NOP");
	PRINTF("%s", txbuff);
	UART_WriteBlocking(TARGET_UART, txbuff, sizeof(txbuff) - 1);

	while(1){
		char input[8];
		int i =0;
		printf("DC Input (-100 to 100)");
		while(1) {
		        if(new_char) {
		            new_char = 0;
		            if (i < sizeof(input) - 1) {
		                input[i++] = ch;
		            }
		            printf("input[%d] = %c\n", i-1, input[i-1]);
		            if(ch == 13) {  // Newline character for 'Enter'
		                break;
		            }
		        }
		    }

		    input[i] = '\0';  // Null-terminate the string to avoid undefined behavior
		    float dutyCycle_DC = atoi(input) * 0.025f / 100.0f + 0.0615;
		    updatePWM_dutyCycle(FTM_CHANNEL_DC_MOTOR, dutyCycle_DC);
		    FTM_SetSoftwareTrigger(FTM_MOTOR, true);

		    i = 0;  // Reset i for servo input
		    printf("Servo Input (-90 to 90): ");
		    while(1) {
		        if(new_char) {
		            new_char = 0;
		            if (i < sizeof(input) - 1) {
		                input[i++] = ch;
		            }
		            printf("input[%d] = %c\n", i-1, input[i-1]);
		            if(ch == 13) {  // Newline character for 'Enter'
		                break;
		            }
		        }
		    }

		    input[i] = '\0';  // Null-terminate the string
		    int servoInput = atoi(input);
		    float dutyCycle_Servo = (((servoInput + 90) / 180.0f) * 0.05f) + 0.05;
		    updatePWM_dutyCycle(FTM_CHANNEL_SERVO_MOTOR, dutyCycle_Servo);
		    FTM_SetSoftwareTrigger(FTM_MOTOR, true);

		/*while(1){
			printf("DC Input (-100 to 100)");
			while(1){
				dcinput = get_uart_num();
				break;
			}

			printf("Servo Input (-90 to 90)");
			while(1){
				servoinput = get_uart_num();
				break;
			}
			dutyCycle_DC = dcinput * 0.025f/100.0f + 0.0615;
			PRINTF("DC = %.6f", dutyCycle_DC);
			dutyCycle_Servo = (((servoinput + 90)/180.0f) * 0.05f) + 0.05;
			PRINTF("Servo = %.6f", dutyCycle_Servo);

			updatePWM_dutyCycle(FTM_CHANNEL_DC_MOTOR, dutyCycle_DC);
			PRINTF("Updated DC");
			updatePWM_dutyCycle(FTM_CHANNEL_SERVO_MOTOR, dutyCycle_Servo);
			PRINTF("Updated Servo");
			FTM_SetSoftwareTrigger(FTM_MOTOR, true);
		}*/
	}


}
