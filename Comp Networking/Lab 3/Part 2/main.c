
/*
 * 
 * Call Blocking in Circuit Switched Networks
 * 
 * Copyright (C) 2014 Terence D. Todd
 * Hamilton, Ontario, CANADA
 * todd@mcmaster.ca
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 */

/*******************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "output.h"
#include "trace.h"
#include "simparameters.h"
#include "cleanup.h"
#include "call_arrival.h"
#include "main.h"

/*******************************************************************************/

int main(void)
{
  int i;
  int j=0;

  Simulation_Run_Ptr simulation_run;
  Simulation_Run_Data data; /* Simulation_Run_Data is defined in main.h. */

  /* 
   * Get the list of random number generator seeds defined in simparameters.h.
   */

  unsigned RANDOM_SEEDS[] = {RANDOM_SEED_LIST, 0};
  unsigned random_seed;

  int num_channels;
  int A;

  int max_A = 10;
  int max_channels = 20;


  data.results = (double**)malloc(max_A * sizeof(double*));  

  for (int i = 0; i < max_A; ++i) {
      data.results[i] = (double*)malloc(max_channels * sizeof(double));  
  }

  /* 
   * Loop for each random number generator seed, doing a separate
   * simulation_run run for each.
   */

  for(A = 1; A <= max_A; A++) {

    printf("A = %d\n", A);
    printf("Trunks, Blocking Probability\n");

    for(num_channels = 1; num_channels <= max_channels; num_channels += 1) {
      
      j = 0;
      data.accumulated_probability = 0;
      data.accumulations = 0;

      while ((random_seed = RANDOM_SEEDS[j++]) != 0) {

        /* Create a new simulation_run. This gives a clock and eventlist. */
        simulation_run = simulation_run_new();

        /* Add our data definitions to the simulation_run. */
        simulation_run_set_data(simulation_run, (void *) & data);

        /* Initialize our simulation_run data variables. */
        data.blip_counter = 0;
        data.call_arrival_count = 0;
        data.calls_processed = 0;
        data.blocked_call_count = 0;
        data.number_of_calls_processed = 0;
        data.accumulated_call_time = 0.0;
        data.random_seed = random_seed;
        data.A = A;
        data.arrival_rate = (int) Call_ARRIVALRATE;
        data.num_channels = num_channels;

        /* Create the channels. */
        data.channels = (Channel_Ptr *) xcalloc(num_channels,
                  sizeof(Channel_Ptr));

        /* Initialize the channels. */
        for (i=0; i<num_channels; i++) {
          *(data.channels+i) = server_new(); 
        }

        /* Set the random number generator seed. */
        random_generator_initialize((unsigned) random_seed);

        /* Schedule the initial call arrival. */
        schedule_call_arrival_event(simulation_run,
          simulation_run_get_time(simulation_run) +
          exponential_generator((double) 1/Call_ARRIVALRATE));
        
        /* Execute events until we are finished. */
        while(data.number_of_calls_processed < RUNLENGTH) {
          simulation_run_execute_event(simulation_run);
        }
        
        /* Print out some results. */
        //output_results(simulation_run);
        output_results_mod(simulation_run);

        /* Clean up memory. */
        cleanup(simulation_run);
      }
    }
  }

  output_final_results(simulation_run);

  /* Pause before finishing. */
  //getchar();
  return 0;
}












