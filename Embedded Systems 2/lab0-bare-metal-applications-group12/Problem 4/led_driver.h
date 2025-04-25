/*
 * led_driver.h
 *
 *  Created on: Jan 16, 2025
 *      Author: umanskyj
 */
#include <stdint.h>
#ifndef LED_DRIVER_H_
#define LED_DRIVER_H_


typedef struct{
	int GPIOx_PDOR;
	int GPIOx_PSOR;
	int GPIOx_PCOR;
	int GPIOx_PTOR;
	int GPIOx_PDIR;
	int GPIOx_PDDR;
}GPIO_Struct;

//Helper functions protoypes
void Init(GPIO_Struct*, char);
void Clear(GPIO_Struct*);
void Toggle(GPIO_Struct*, uint32_t mask);


#endif /* LED_DRIVER_H_ */
