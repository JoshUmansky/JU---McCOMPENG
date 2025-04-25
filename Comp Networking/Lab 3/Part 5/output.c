
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

#include <stdio.h>
#include "simparameters.h"
#include "main.h"
#include "output.h"

/*******************************************************************************/

void output_progress_msg_to_screen(Simulation_Run_Ptr this_simulation_run)
{
  double percentagedone;
  Simulation_Run_Data_Ptr sim_data;

  sim_data = (Simulation_Run_Data_Ptr) simulation_run_data(this_simulation_run);

  sim_data->blip_counter++;

  if((sim_data->blip_counter >= BLIPRATE)
     ||
     (sim_data->number_of_calls_processed >= RUNLENGTH))
    {
      sim_data->blip_counter = 0;

      percentagedone =
	100 * (double) sim_data->number_of_calls_processed/RUNLENGTH;

      printf("%3.0f%% ", percentagedone);

      printf("Call Count = %ld \r", sim_data->number_of_calls_processed);

      fflush(stdout);
    }
}

/*******************************************************************************/

void output_results(Simulation_Run_Ptr this_simulation_run)
{
  Simulation_Run_Data_Ptr sim_data;

  sim_data = (Simulation_Run_Data_Ptr) simulation_run_data(this_simulation_run);

  printf("\n");

  printf("random seed = %d \n", sim_data->random_seed);
  printf("call process count = %ld \n", sim_data->number_of_calls_processed);
  printf("number of calls waited = %f \n", sim_data->accumulated_wait_calls);
  printf("calls under t = %f \n", sim_data->number_calls_under_t);
  printf("wait_time total = %f \n", sim_data->accumulated_wait_time);

  double prob_wait = (double) (sim_data->accumulated_wait_calls / sim_data->number_of_calls_processed);
  double average_wait_time = (double) (sim_data->accumulated_wait_time / sim_data->number_of_calls_processed);
  double w_prob = (double) ((sim_data->number_calls_under_t + (sim_data->number_of_calls_processed - sim_data->accumulated_wait_calls))
    / sim_data->number_of_calls_processed);
  /*int lambda = Call_ARRIVALRATE;
  int N = NUMBER_OF_CHANNELS;
  int A = auto_A;//Must be greater then Number_of_channels
  int t = wait_t;
  printf("%i, %i, %i, %i \n", lambda, N, A, t);*/
  printf("Pw = %f \n", prob_wait);
  printf("Tw = %f \n", average_wait_time);
  printf("Wt = %f \n", w_prob);
  //printf("Blocking probability = %.5f (Service fraction = %.5f)\n",	 1-xmtted_fraction, xmtted_fraction);

  printf("\n");
}

void output_results_mod(Simulation_Run_Ptr sim_run)
{
  double xmtted_fraction;
  Simulation_Run_Data_Ptr sim_data;

  sim_data = (Simulation_Run_Data_Ptr) simulation_run_data(sim_run);

  xmtted_fraction = (double) (sim_data->blocked_call_count)/sim_data->call_arrival_count;
  //printf("xmtted: %f\n", xmtted_fraction);
  sim_data->accumulated_probability += xmtted_fraction;
  sim_data->accumulations += 1;

  if(sim_data->random_seed == 400408809) {
    //printf("Accumulated_probability: %f\nRuns: %d\n", sim_data->accumulated_probability, sim_data->accumulations);
    sim_data->accumulated_probability = sim_data->accumulated_probability / sim_data->accumulations;

    sim_data->results[sim_data->A-1][sim_data->num_channels-1] = sim_data->accumulated_probability;
    /*if(sim_data->A == 1)
      printf("%d, %f\n", sim_data->num_channels, sim_data->accumulated_probability);
    else
      printf("%f\n", sim_data->accumulated_probability);*/
  }
}

void output_final_results(Simulation_Run_Ptr sim_run)
{
  Simulation_Run_Data_Ptr sim_data;
  sim_data = (Simulation_Run_Data_Ptr) simulation_run_data(sim_run);

  for (int A = 0; A < sim_data->A; A++) {
    printf("%d", A+1);
    for (int trunks = 0; trunks < sim_data->num_channels; trunks++) {
      printf(", %f", sim_data->results[A][trunks]);
    }
    printf("\n");
  }
}


