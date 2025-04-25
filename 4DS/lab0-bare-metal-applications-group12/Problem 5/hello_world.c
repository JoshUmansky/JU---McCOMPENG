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
#include <stdio.h>
#include <stdlib.h>

/*******************************************************************************
 * Definitions
 ******************************************************************************/


/*******************************************************************************
 * Prototypes
 ******************************************************************************/

/*******************************************************************************
 * Code
 ******************************************************************************/
void pwm_setup()
{
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

/*!
 * @brief Main function
 */
int main(void)
{
    char ch;
    char** input = "";

    /* Init board hardware. */
    BOARD_InitBootPins();
    BOARD_InitBootClocks();
    BOARD_InitDebugConsole();

    pwm_setup();

    printf("Enter html color value: \n");

    int red, green, blue;
    scanf("%2x%2x%2x", &red, &green, &blue);

    int red_val = (red*100)/255;
    int green_val = (green*100)/255;
    int blue_val = (blue*100)/255;

    //PRINTF("hello world.\r\n");
    int duty_cycle=0;
    FTM_UpdatePwmDutycycle(FTM3, kFTM_Chnl_1, kFTM_EdgeAlignedPwm, red_val);
    FTM_UpdatePwmDutycycle(FTM3, kFTM_Chnl_4, kFTM_EdgeAlignedPwm, blue_val);
    FTM_UpdatePwmDutycycle(FTM3, kFTM_Chnl_5, kFTM_EdgeAlignedPwm, green_val);
    FTM_SetSoftwareTrigger(FTM3, true);

    while (1)
    {

    }
}
