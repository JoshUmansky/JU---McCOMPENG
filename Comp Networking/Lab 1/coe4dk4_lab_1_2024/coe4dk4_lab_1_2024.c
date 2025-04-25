
/*
 *
 * Simulation of Single Server Queueing System
 * 
 * Copyright (C) 2014 Terence D. Todd Hamilton, Ontario, CANADA,
 * todd@mcmaster.ca
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY dor FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

/*******************************************************************************/

#include <stdio.h>
#include "simlib.h"

/*******************************************************************************/

/*
 * Simulation Parameters
 */

//#define RANDOM_SEED 5259140
#define NUMBER_TO_SERVE 1e6

#define SERVICE_TIME 10
//#define ARRIVAL_RATE 0.1

#define BLIP_RATE 10000

#define MAX_QUEUE_SIZE 200

char SYS_TYPE[3] = {'M','D', 1};

/*******************************************************************************/

/*
 * main() uses various simulation parameters and creates a clock variable to
 * simulate real time. A loop repeatedly determines if the next event to occur
 * is a customer arrival or customer departure. In either case the state of the
 * system is updated and statistics are collected before the next
 * iteration. When it finally reaches NUMBER_TO_SERVE customers, the program
 * outputs some statistics such as mean delay.
 */

int main()
{
  double ARRIVAL_RATE = 0.01;
  int random_seed;
  
  printf("Data will be in format: ARRIVAL_RATE, mean delay\n");
  while (ARRIVAL_RATE * SERVICE_TIME < 4) { // Loop to vary ARRIVAL_RATE
      double average_val = 0;
      double average_rejections_frac = 0;
      int iterations = 0;
      
      for (random_seed = 0; random_seed < 10; random_seed++) {
          iterations++;
          double clock = 0; /* Clock keeps track of simulation time. */

          /* System state variables. */
          int number_in_system = 0;
          double next_arrival_time = 0;
          double next_departure_time = 0;
       
          /* Data collection variables. */
          long int total_served = 0;
          long int total_arrived = 0;
          long int total_rejections = 0;

          double total_busy_time = 0;
          double integral_of_n = 0;
          double last_event_time = 0;

          /* Set the seed of the random number generator. */
          random_generator_initialize(random_seed + 400234265);

          /* Process customers until we are finished. */
          while (total_served < NUMBER_TO_SERVE) {

              /* Test if the next event is a customer arrival or departure. */
              if (number_in_system == 0 || next_arrival_time < next_departure_time) {

                  /*
                    * A new arrival is occurring.
                    */

                  total_arrived++;

                  clock = next_arrival_time;
                  if(SYS_TYPE[0] == 'M')
                    next_arrival_time = clock + exponential_generator((double)1 / ARRIVAL_RATE);
                  else if(SYS_TYPE[0] == 'D')
                    next_arrival_time = clock + 1/ARRIVAL_RATE;
                 
                  if(number_in_system - 1 >= MAX_QUEUE_SIZE) {// If there's already a max queue, turns away the customer
                    total_rejections++;
                    continue;
                  }

                  /* Update our statistics. */
                  integral_of_n += number_in_system * (clock - last_event_time);
                  last_event_time = clock;

                  number_in_system++;
                  

                  /* If this customer has arrived to an empty system, start its
                service right away. */
                  if (number_in_system == 1) {
                    if(SYS_TYPE[1] == 'M')
                      next_departure_time = clock + exponential_generator((double)SERVICE_TIME);
                    else if(SYS_TYPE[1] == 'D')
                      next_departure_time = clock + SERVICE_TIME;
                  }
              }
              else {

                  /*
                    * A customer departure is occuring.
                    */

                  clock = next_departure_time;

                  /* Update our statistics. */
                  integral_of_n += number_in_system * (clock - last_event_time);
                  last_event_time = clock;

                  number_in_system--;
                  total_served++;
                  total_busy_time += SERVICE_TIME; // This doesnt really represent accurate values if it is M/M/1

                  /*
                    * If there are other customers waiting, start one in service
                    * right away.
                    */

                  if (number_in_system > 0) {
                    if(SYS_TYPE[1] == 'M')
                      next_departure_time = clock + exponential_generator((double)SERVICE_TIME);
                    else if(SYS_TYPE[1] == 'D')
                      next_departure_time = clock + SERVICE_TIME;
                  }
                  /*
                    * Every so often, print an activity message to show we are active.
                    */

                    /*if (total_served % BLIP_RATE == 0)
                      printf("Customers served = %ld (Total arrived = %ld)\r",
                        total_served, total_arrived);*/
              }
          }
          /* Output final results. */
          //printf("\nUtilization = %f\n", total_busy_time/clock);
          //printf("Fraction served = %f\n", (double) total_served/total_arrived);
          //printf("Mean number in system = %f\n", integral_of_n/clock);
          //printf("Mean delay = %f\n", integral_of_n/total_served);

          /* Halt the program before exiting. */
          //printf("Hit Enter to finish ... \n");
          //getchar(); 
          
          average_val += integral_of_n/total_served;
          average_rejections_frac += (double)total_rejections/total_arrived;
      }
      average_val = average_val/iterations;
      average_rejections_frac = average_rejections_frac/iterations;
      
      //printf("%f %f\n", ARRIVAL_RATE, average_rejections_frac); // rejeciton rate
      printf("%i %f %f\n", random_seed + 400234265, ARRIVAL_RATE, average_val); // mean delay
      ARRIVAL_RATE += 0.01;
    }
  

  return 0;

}






