/*
 * led_driver.c
 *
 *  Created on: Jan 16, 2025
 *      Author: umanskyj
 */

#include "led_driver.h"

void Init(GPIO_Struct* gpio, char c){
	gpio->GPIOx_PDOR = 0x00000000;
	gpio->GPIOx_PCOR = 0x00000000;
	//gpio->GPIOx_PDDR = 0x00000302; //0000 0011 0000 0010 enabling pins 1,8,9 as output
	gpio->GPIOx_PDIR = 0x00000000;
	gpio->GPIOx_PSOR = 0x00000000;
	gpio->GPIOx_PTOR = 0x00000000;
	if(c == 'c'){
		gpio->GPIOx_PDDR = 0x00000300;//0000 0011 0000 0000
	}
	else if(c == 'd'){
		gpio->GPIOx_PDDR = 0x00000002;
	}
}

void Clear(GPIO_Struct* gpio){
	//update PCOR
	gpio->GPIOx_PCOR = 0x11111111;
}

void Toggle(GPIO_Struct* gpio, uint32_t mask){
	//update PTOR
	gpio->GPIOx_PTOR = mask;

}


