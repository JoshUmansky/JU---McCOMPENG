
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
#include <string.h>

/*******************************************************************************/

/*
 * Simulation Parameters
 */

//#define RANDOM_SEED 5259140
#define NUMBER_TO_SERVE 1e4

#define SERVICE_TIME 10
//#define ARRIVAL_RATE 0.1

#define BLIP_RATE 10000

#define MAX_QUEUE_SIZE 200

int* generate_packet_size() { // assigns uniform distributed number between 0 and 4
  int *a = (int *)malloc(sizeof(int));  // Dynamically allocates an int

  double r = (double) rand()/(double) RAND_MAX;
  *a = (int) (5.0*r);

  return a;
}

void clear_queue(Fifoqueue_Ptr queue) {
  while (fifoqueue_size(queue) > 0) {
    free(fifoqueue_get(queue));
  }
  free(queue);
}

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
  int part = 3;
  char sub_part = 'b';
  char var_to_adjust[] = "token_max"; // for part 2

  if (part == 1) {
    int bucket_size = 3;
    int start_bucket_size = 1;
    int end_bucket_size = 10;
    
    double ARRIVAL_RATE = 100; // 100 packets/sec

    int DRIP_RATE = 1000;
    double start_DRIP_RATE = 1;
    double end_DRIP_RATE = 1000;

    double drip_time = 1.0/DRIP_RATE;

    int random_seed;

    //int variable = (sub_part == 'a' ? bucket_size : DRIP_RATE);;
    int var_start = (sub_part == 'a' ? start_bucket_size : start_DRIP_RATE);
    int var_end = (sub_part == 'a' ? end_bucket_size : end_DRIP_RATE);
    int num_iter = 100;
    int var_delta = (var_end - var_start) / (num_iter-1);
    
    printf("Data will be in format: %s, average drop rate (percent), mean output data rate (packets/second)\n", (part=='a'?"bucket size":"R"));
    for (int i = 0, variable = var_start; i <= num_iter; i++, variable += var_delta) { // Loop to vary bucket size
        if (sub_part == 'a') {
          bucket_size = variable;
        } else if (sub_part == 'b') {
          drip_time = 1.0/variable;
        }
        
        double average_val = 0;
        double average_rejections_frac = 0;
        double mean_output_data_rate = 0;
        int iterations = 0;
        
        for (random_seed = 0; random_seed < 10; random_seed++) {
            iterations++;
            double clock = 0; /* Clock keeps track of simulation time. */

            /* System state variables. */
            int number_in_bucket = 0;
            double next_arrival_time = 0;
            double next_departure_time = 0; // remove this
            double next_clock_tick = drip_time; // not sure if this should start at 0 or drip_time, since idk if a packet can be sent the same moment it arrives
        
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
                if (next_arrival_time < next_clock_tick) {
                    /* A new arrival is occurring. */

                    total_arrived++;

                    clock = next_arrival_time;
                    
                    next_arrival_time = clock + exponential_generator((double)1 / ARRIVAL_RATE);
                  
                    if(number_in_bucket >= bucket_size) {// If there's already a full bucket, drop the packet
                      total_rejections++;
                      continue;
                    }

                    /* Update our statistics. */
                    integral_of_n += number_in_bucket * (clock - last_event_time);
                    last_event_time = clock;

                    number_in_bucket++;
                }
                else {
                    /* A packet departure is occuring. */

                    clock = next_clock_tick;
                    next_clock_tick = clock + drip_time;

                    if (number_in_bucket > 0) {
                      /* Update our statistics. */
                      integral_of_n += number_in_bucket * (clock - last_event_time);
                      last_event_time = clock;

                      number_in_bucket--;
                      total_served++;
                      total_busy_time += drip_time;
                    }

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
            mean_output_data_rate += (double)total_served/clock;
        }
        average_val = average_val/iterations;
        average_rejections_frac = average_rejections_frac/iterations;
        mean_output_data_rate = mean_output_data_rate/iterations;
        
        //printf("%f %f\n", ARRIVAL_RATE, average_rejections_frac); // rejeciton rate
        printf("%i %f %f\n", variable, average_rejections_frac, mean_output_data_rate); // mean delay
      }
  }
  else if (part == 2) {
    double counter_reset = 2E5; // 2E5 is basically 0% loss with ARR = 100, TICK = 1, DRIP = 1E6, bucekt_size = 2
    double counter = counter_reset;

    int bucket_size = 2;
    int packet_sizes[5] = {500, 1000, 1500, 2000, 2500};
    
    double ARRIVAL_RATE = 100; // 100 packets/sec
    double DRIP_RATE = 1E4;

    double TICK_RATE = 1.0; // bytes per/sec
    double start_TICK_RATE = 1000;
    double end_TICK_RATE = 4000;

    double tick_time = 1.0/TICK_RATE;
    double drip_time = 1.0/DRIP_RATE;

    int random_seed;
    
    double var_start = (strcmp(var_to_adjust, "counter") == 0 ? counter_reset : (strcmp(var_to_adjust, "tick_time") == 0 ? tick_time : DRIP_RATE));
    double var_end = var_start*100;
    int num_iter = 100;
    double var_delta = (var_end - var_start) / (num_iter-1);
    double variable = var_start;

    printf("Data will be in format: %s, average drop rate (percent), mean output data rate (packets/second)\n", var_to_adjust);
    for (int i = 0; i < num_iter; i++, variable += var_delta) { // Loop to vary bucket size
      // Set the variable which is varying here
      if (strcmp(var_to_adjust, "counter") == 0) {
        counter_reset = variable;
        counter = counter_reset;
      } else if (strcmp(var_to_adjust, "tick_time") == 0)
        tick_time = variable;
      else if (strcmp(var_to_adjust, "drip_rate") == 0) {
        DRIP_RATE = variable;
        drip_time = 1.0/DRIP_RATE;
      }

      double average_val = 0;
      double average_rejections_frac = 0;
      double average_output_bit_rate = 0;
      double mean_output_data_rate = 0;
      int iterations = 0;
      
      for (random_seed = 0; random_seed < 4; random_seed++) {
        //printf("random)seed %i, counter_reset: %i\n", random_seed, counter_reset);
        iterations++;
        double clock = 0; /* Clock keeps track of simulation time. */

        /* System state variables. */
        int number_in_bucket = 0;
        double next_arrival_time = 0;
        double next_clock_tick = tick_time;
        double next_drop_time = drip_time;

        /* Data collection variables. */
        long int total_served = 0;
        long int total_arrived = 0;
        long int total_rejections = 0;
        long int total_bytes = 0;

        int no_bytes_left = 0;
        int current_packet = 0;
        /* Set the seed of the random number generator. */
        random_generator_initialize(random_seed + 400234265);
        // initialize bucket queue
        Fifoqueue_Ptr bucket_queue = fifoqueue_new(); 

        /* Process customers until we are finished. */

        while (total_served < NUMBER_TO_SERVE) {
            if (next_arrival_time < next_drop_time && next_arrival_time < next_clock_tick) { // next action is an arrival
              //printf("arrival\n");
              clock = next_arrival_time;
              next_arrival_time = clock + exponential_generator((double)1 / ARRIVAL_RATE);

              total_arrived++;

              if (fifoqueue_size(bucket_queue) >= bucket_size) { // Reject packet
                total_rejections++;
              } else { // Add randomly sized packet to queue
                fifoqueue_put(bucket_queue, (void*)generate_packet_size());
              }

            } else if (next_drop_time <= next_clock_tick) { // next action is a drop
              //printf("drip\n");
              clock = next_drop_time;
              next_drop_time = clock + drip_time;
              
              if (current_packet) {
                  current_packet--;
                  counter--;

                  total_bytes++;
                  
                  if (current_packet == 0)
                    total_served++;
              } else if (fifoqueue_size(bucket_queue) > 0) {
                //printf("\n%i\n",packet_sizes[*(int*)fifoqueue_see_front(bucket_queue)]);
                if (packet_sizes[*(int*)fifoqueue_see_front(bucket_queue)] <= counter) {
                  //printf("Drip\n");
                  int* packet = (int*)fifoqueue_get(bucket_queue);
                  int packet_size = packet_sizes[*packet];
                  current_packet = packet_size;
                  
                  free(packet);
                } else {
                  no_bytes_left = 1;
                  next_drop_time = clock + drip_time*((int)((next_clock_tick - clock)/drip_time) + 1); // calc to not drip until counter reset
                }
              }
            } else { // next action is a byte count reset
              //printf("\ncounter reset\n");
              //printf("Clock reset\n");
              clock = next_clock_tick;
              next_clock_tick = clock + tick_time;

              counter = counter_reset;
              no_bytes_left = 0;
            }

            //if (total_served % 1 == 0)
            //  printf("Customers served = %ld (Total arrived = %ld)\tCurrent Packet = %i (counter = %i)\r", total_served, total_arrived, current_packet, counter);
        }
        clear_queue(bucket_queue);
        average_rejections_frac += (double)total_rejections/total_arrived;
        mean_output_data_rate += (double)total_served/clock;
        average_output_bit_rate += (double)total_bytes/clock;
      }
      average_val = average_val/iterations;
      average_rejections_frac = average_rejections_frac/iterations;
      mean_output_data_rate = mean_output_data_rate/iterations;
      average_output_bit_rate = average_output_bit_rate/iterations;
      
      //printf("%f %f\n", ARRIVAL_RATE, average_rejections_frac); // rejeciton rate

      printf("%f %f %f\n", variable, average_rejections_frac, average_output_bit_rate); // mean delay
    }
  }
  else if (part == 3) {
    double ARRIVAL_RATE = 100;    

    // Token queue
    double TOKEN_RATE = 150000;
    double TOKEN_PERIOD = 1.0/TOKEN_RATE;
    int max_tokens = 2500;
    int num_tokens = 0;

    // packet queue
    double DRIP_RATE = 1E6;
    double DRIP_PERIOD = 1.0/DRIP_RATE;
    int max_packets = 4;
    int num_packets = 0;

    int packet_sizes[5] = {500, 1000, 1500, 2000, 2500};

    int random_seed;
    
    double var_start = (strcmp(var_to_adjust, "token_max") == 0 ? max_tokens : max_packets);
    double var_end = var_start*100;
    int num_iter = 100;
    double var_delta = (var_end - var_start + 1) / (num_iter);
    double variable = var_start;

    printf("Data will be in format: %s, average drop rate (percent), mean output data rate (%s/second)\n", var_to_adjust, sub_part == 'a' ? "pkts" : "bits");
    for (int i = 0; i <= num_iter; i++, variable += var_delta) { // Loop to vary bucket size
      // Set the variable which is varying here
      if (strcmp(var_to_adjust, "token_max") == 0) {
        max_tokens = variable;
      } else {
        max_packets = variable;
      }

      double average_val = 0;
      double average_rejections_frac = 0;
      double average_output_bit_rate = 0;
      double mean_output_data_rate = 0;
      int iterations = 0;
      
      for (random_seed = 0; random_seed < 4; random_seed++) {
        //printf("random)seed %i, counter_reset: %i\n", random_seed, counter_reset);
        iterations++;
        double clock = 0; /* Clock keeps track of simulation time. */

        /* System state variables. */
        num_tokens = 0;
        num_packets = 0;
        double next_arrival_time = 0;
        double next_token_time = TOKEN_PERIOD;
        double next_drop_time = DRIP_PERIOD;

        /* Data collection variables. */
        long int total_served = 0;
        long int total_arrived = 0;
        long int total_rejections = 0;
        long int total_bits = 0;
        int current_packet = 0;

        /* Set the seed of the random number generator. */
        random_generator_initialize(random_seed + 400234265);
        // initialize bucket queue
        Fifoqueue_Ptr bucket_queue = fifoqueue_new();

        /* Process customers until we are finished. */
        while (total_served < NUMBER_TO_SERVE) {
          //printf("Times: packet %f token %f drop %f\n", next_arrival_time, next_token_time, next_drop_time);
          if (next_arrival_time < next_token_time && next_arrival_time < next_drop_time) { // next action is an arrival
            //printf("arrival\n");
            clock = next_arrival_time;
            next_arrival_time = clock + exponential_generator((double)1 / ARRIVAL_RATE);

            total_arrived++;

            if (num_packets >= max_packets) { // Reject packet
              total_rejections++;
            } else { // Add randomly sized packet to queue
              if (sub_part == 'b') {
                fifoqueue_put(bucket_queue, (void*)generate_packet_size()); // Part b
                //printf("Add to queue: %d\n",fifoqueue_size(bucket_queue));
              }
              num_packets++;
            }

          } else if (next_token_time <= next_drop_time) { // next action is a token arrival
            //printf("\nToken\n");
            clock = next_token_time;
            next_token_time = clock + TOKEN_PERIOD;
            
            if (num_tokens < max_tokens)
              num_tokens++;
                          
          } else { // next action is a drop
            //printf("\nDrip\n");
            //printf("NExt drip %f\n", next_drop_time);
            //printf("%d\n", current_packet);
            clock = next_drop_time;
            next_drop_time = clock + DRIP_PERIOD;

            if (sub_part == 'a' && num_packets > 0 && num_tokens > 0) {
              num_packets--;
              num_tokens--;
              total_served++;
            } else if (sub_part == 'b') {
              if (current_packet > 0) {
                current_packet--;
                num_tokens--;
                total_bits++;

                if (current_packet == 0)
                  total_served++;
              } else if (num_packets > 0) {
                if(packet_sizes[*(int*)fifoqueue_see_front(bucket_queue)] <= num_tokens) {
                  int* packet = (int*)fifoqueue_get(bucket_queue);
                  int packet_size = packet_sizes[*packet];
                  current_packet = packet_size;
                  free(packet);
                  num_packets--;
                }
              }
            }

            if (sub_part == 'a') {
              if (num_tokens == 0)
                next_drop_time = clock + DRIP_PERIOD*((int)((next_token_time - clock)/DRIP_PERIOD) + 1); // calc to not drip until a new token available
            } else if (sub_part == 'b') {
              if (num_packets > 0) {
                if (num_tokens < packet_sizes[*(int*)fifoqueue_see_front(bucket_queue)]) {
                  int diff = packet_sizes[*(int*)fifoqueue_see_front(bucket_queue)] - num_tokens;
                  next_drop_time = clock + DRIP_PERIOD*((int)((next_token_time + diff*TOKEN_PERIOD - clock)/DRIP_PERIOD));
                }
              }
            }

          //if (total_served % 1 == 0)
            //printf("Customers served = %ld (Total arrived = %ld)\r", total_served, total_arrived);
          } 
        }
        clear_queue(bucket_queue);
        average_rejections_frac += (double)total_rejections/total_arrived;
        mean_output_data_rate += (double)total_served/clock;
        average_output_bit_rate += (double)total_bits/clock;
      }
      average_val = average_val/iterations;
      average_rejections_frac = average_rejections_frac/iterations;
      mean_output_data_rate = mean_output_data_rate/iterations;
      average_output_bit_rate = average_output_bit_rate/iterations;
      
      //printf("%f %f\n", ARRIVAL_RATE, average_rejections_frac); // rejeciton rate

      printf("%f %f %f\n", variable, average_rejections_frac, sub_part == 'a' ? mean_output_data_rate : average_output_bit_rate); // mean delay
    }

  }

  return 0;

}






