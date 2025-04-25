
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

  int start_operators = 1;
  int max_operators = 10;

  double max_patience = 10.0;
  double start_patience = 0.0;

  double A_start = 1.0;
  double A_max = 20.0;
  double A_i = 1.0;

  double patience_i = 1.0;
  double mean_call_duration = 3;
  double arrival_rate = 3;

  int row = 0;
  //int num_col = (int)((max_patience-start_patience)/patience_i + 1);
  int num_col = (int)((A_max-A_start)/A_i + 1);
  //int num_rows = (int)(max_operators-start_operators + 1);
  int num_rows = (int)((max_patience-start_patience)/patience_i + 1);

  data.results = (double**)malloc(num_rows * sizeof(double*));  
  for (int i = 0; i < num_rows; ++i) {
      data.results[i] = (double*)malloc(num_col * sizeof(double));  
  }

  data.delay_results = (double**)malloc(num_rows * sizeof(double*));  
  for (int i = 0; i < num_rows; ++i) {
      data.delay_results[i] = (double*)malloc(num_col * sizeof(double));  
  }

  /* 
   * Loop for each random number generator seed, doing a separate
   * simulation_run run for each.
   */

  //for(int num_operators = start_operators; num_operators <= max_operators; num_operators += 1) {
  for(double patience = start_patience; patience <= max_patience; patience += patience_i) {
    int col = 0;
    for(int A = A_start; A <= A_max; A += A_i) {
    //for(double patience = start_patience; patience <= max_patience; patience += patience_i) {
      // change this depending on which youre graphing
      //double patience = 1.0;
      int num_operators = 5;

      data.accumulations = 0;
      data.accumulated_leave_prob = 0;
      data.accumulated_delay = 0.0;
      data.A = A;
      j = 0;

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
        data.number_of_calls_dropped = 0;
        data.accumulated_call_time = 0.0;
        data.random_seed = random_seed;
        data.N = num_operators;
        data.w = patience;
        data.arrival_rate = arrival_rate;
        data.avg_call_duration = mean_call_duration;

        /* Create the channels. */
        data.channels = (Channel_Ptr *) xcalloc((int) data.N,
                  sizeof(Channel_Ptr));

        /* Initialize the channels. */
        for (i=0; i<data.N; i++) {
          *(data.channels+i) = server_new(); 
        }

        data.queue = fifoqueue_new();

        /* Set the random number generator seed. */
        random_generator_initialize((unsigned) random_seed);

        /* Schedule the initial call arrival. */
        schedule_call_arrival_event(simulation_run,
          simulation_run_get_time(simulation_run) +
          exponential_generator((double) 1/data.arrival_rate));
        
        /* Execute events until we are finished. */
        while(data.number_of_calls_processed < RUNLENGTH) {
          simulation_run_execute_event(simulation_run);
        }

        // Check the queue for people who are still waiting but should have left
        while(fifoqueue_size(data.queue) > 0) {

          Call_Ptr next_call = (Call_Ptr) fifoqueue_get(data.queue);
          double now = simulation_run_get_time(simulation_run);
          double wait_time = now-next_call->arrive_time;
          TRACE(printf("Time: %f\tPatience: %f\tTime Waiting: %f\n", now, next_call->patience, wait_time);)
          TRACE(printf("%d", next_call->patience < wait_time);)
          if (next_call->patience < wait_time) { // If the time since arrival is within the allowed wait time, send the call through, otherwise drop it and move on
            // take statistics for dropped call
            TRACE(printf("Dropped a call\n");)
            data.number_of_calls_dropped += 1;
          }
          xfree((void*) next_call);
        }
        
        /* Print out some results. */
        //output_results(simulation_run);
        output_results_mod(simulation_run);

        /* Clean up memory. */
        cleanup(simulation_run);
      }
      data.results[row][col] = data.accumulated_leave_prob/data.accumulations;
      data.delay_results[row][col] = data.accumulated_delay/data.accumulations;
      col += 1;
    }
    row += 1;
  }
  //output_all_results(simulation_run);
  /*double patience = start_patience;
  printf("-");
  for (int col = 0; col < num_col; col++) {
    printf(", %f", patience);
    patience += patience_i;
  }*/
  double A = A_start;
  printf("-");
  for (int col = 0; col < num_col; col++) {
    printf(", %f", A);
    A += A_i;
  }

  printf("\n");

  /*for (int N = 0; N < num_rows; N++) {
    printf("%d", start_operators + N);
    for (int W = 0; W < num_col; W++) {
      printf(", %.15f", data.results[N][W]);
    }
    printf("\n");
  }*/

  for (int N = 0; N < num_rows; N++) {
    printf("%d", start_patience + N * patience_i);
    for (int W = 0; W < num_col; W++) {
      printf(", %.15f", data.results[N][W]);
    }
    printf("\n");
  }

  printf("\nDelay stuff\n");

  /*patience = start_patience;
  printf("-");
  for (int col = 0; col < num_col; col++) {
    printf(", %f", patience);
    patience += patience_i;
  }*/

  A = A_start;
  printf("-");
  for (int col = 0; col < num_col; col++) {
    printf(", %f", A);
    A += A_i;
  }

  printf("\n");

  /*for (int N = 0; N < num_rows; N++) {
    printf("%d", start_operators + N);
    for (int W = 0; W < num_col; W++) {
      printf(", %.15f", data.delay_results[N][W]);
    }
    printf("\n");
  }*/

  for (int N = 0; N < num_rows; N++) {
    printf("%d", start_patience + N * patience_i);
    for (int W = 0; W < num_col; W++) {
      printf(", %.15f", data.delay_results[N][W]);
    }
    printf("\n");
  }

  /* Pause before finishing. */
  //getchar();
  return 0;
}












