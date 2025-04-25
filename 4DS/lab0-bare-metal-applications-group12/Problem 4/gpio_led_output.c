	/*
 * Copyright (c) 2015, Freescale Semiconductor, Inc.
 * Copyright 2016-2017 NXP
 * All rights reserved.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

#include "pin_mux.h"
#include "clock_config.h"
#include "board.h"
#include "fsl_debug_console.h"
#include "fsl_gpio.h"
#include "led_driver.h"

/*******************************************************************************
 * Definitions
 ******************************************************************************/
#define BOARD_LED_GPIO     GPIOD
#define BOARD_LED_GPIO_PIN 13

#define BOARD_LED_GPIOC     GPIOC
#define BOARD_LED_GPIOD     GPIOD
#define BOARD_LED_GPIO_PIN_RED 1
#define BOARD_LED_GPIO_PIN_BLUE 8
#define BOARD_LED_GPIO_PIN_GREEN 9



/*******************************************************************************
 * Prototypes
 ******************************************************************************/
/*!
 * @brief delay a while.
 */
void delay(void);

/*******************************************************************************
 * Variables
 ******************************************************************************/

/*******************************************************************************
 * Code
 ******************************************************************************/
void delay(void)
{
    volatile uint32_t i = 0;
    for (i = 0; i < 3200000; ++i)
    {
        __asm("NOP"); /* delay */
    }
}

/*!
 * @brief Main function
 */
int main(void)
{
    /* Define the init structure for the output LED pin*/

	gpio_pin_config_t led_config = {
        kGPIO_DigitalOutput,
        0,
    };

    /* Board pin, clock, debug console init */
    BOARD_InitBootPins();
    BOARD_InitBootClocks();
    BOARD_InitDebugConsole();

    /* Print a note to terminal. */
    PRINTF("\r\n GPIO Driver example\r\n");
    PRINTF("\r\n The LED is blinking.\r\n");

    /* Init output LED GPIO. */
    GPIO_PinInit(BOARD_LED_GPIO, BOARD_LED_GPIO_PIN, &led_config);
    GPIO_PinInit(BOARD_LED_GPIOC, BOARD_LED_GPIO_PIN_BLUE, &led_config);
    GPIO_PinInit(BOARD_LED_GPIOC, BOARD_LED_GPIO_PIN_GREEN, &led_config);
    GPIO_PinInit(BOARD_LED_GPIOD, BOARD_LED_GPIO_PIN_RED, &led_config);
    //(struct ARBITRARY_MODULE*)0x20001000;
    GPIO_Struct *gpioc = (GPIO_Struct*)0x400FF080;
    GPIO_Struct *gpiod = (GPIO_Struct*)0x400FF0C0;
    char d ='d';
    char c = 'c';
    Init(gpioc, c);
    Init(gpiod, d);

    while (1)
    {
        delay();
        //Experiment 3
        //GPIO_PortToggle(BOARD_LED_GPIO, 1u << BOARD_LED_GPIO_PIN);
        //Problem 3
        /*GPIO_PortToggle(BOARD_LED_GPIOC, 1u << BOARD_LED_GPIO_PIN_BLUE);
        delay();
        GPIO_PortToggle(BOARD_LED_GPIOC, 1u << BOARD_LED_GPIO_PIN_BLUE);
        delay();
        GPIO_PortToggle(BOARD_LED_GPIOC, 1u << BOARD_LED_GPIO_PIN_GREEN);
        delay();
        GPIO_PortToggle(BOARD_LED_GPIOC, 1u << BOARD_LED_GPIO_PIN_GREEN);
        delay();
        GPIO_PortToggle(BOARD_LED_GPIOD, 1u << BOARD_LED_GPIO_PIN_RED);
        delay();
        GPIO_PortToggle(BOARD_LED_GPIOD, 1u << BOARD_LED_GPIO_PIN_RED);*/

        Toggle(gpioc, 1u << 8);
        delay();
        Toggle(gpioc, 1u << 8);
        delay();
        Toggle(gpioc, 1u << 9);
		delay();
		Toggle(gpioc, 1u << 9);
		delay();
		Toggle(gpiod, 1u << 1);
		delay();
		Toggle(gpiod, 1u << 1);
		delay();

    }
}
