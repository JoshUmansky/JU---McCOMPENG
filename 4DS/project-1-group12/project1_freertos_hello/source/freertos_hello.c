#include "FreeRTOS.h"

/* Freescale includes. */
#include "fsl_device_registers.h"
#include "clock_config.h"
#include "board.h"

#include "Motor_Control_Component.h"
#include "RC_Receiver_Component.h"
#include "Terminal_Component.h"
#include "LED_Component.h"
#include "Accelerometer_Component.h"

/*
 Channel 3 Speed
1000 - 2000

1000 - 1050 - 0 LED: Blue Mode 0
1050 - 1367 - 33 LED: Green Mode 1
1368 - 1684 - 66 LED: Orange/Yellow Mode 2
1686 - 2000 - 100 LED: Red Mode 3

Channel 1 turn
Left - 1000 Right - 2000

-180 - 180

 */

int main(void)
{
    /* Init board hardware. */
    BOARD_InitBootClocks();
    BOARD_InitBootPins();
//
	setupMotorComponent();
	setupLEDComponent();
	setupRCReceiverComponent();
//    setupTerminalComponent();

//    setupAccelerometerComponent();

    vTaskStartScheduler();

    while(1)
    {}
}
