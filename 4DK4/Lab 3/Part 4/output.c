
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

      printf("A = %.1f, N = %d  W = %f   %3.0f%% ", sim_data->A, sim_data->N, sim_data->w ,percentagedone);

      printf("Call Count = %ld \r", sim_data->number_of_calls_processed);

      fflush(stdout);
    }
}

/*******************************************************************************/

void output_results(Simulation_Run_Ptr this_simulation_run)
{
  double xmtted_fraction;
  Simulation_Run_Data_Ptr sim_data;

  sim_data = (Simulation_Run_Data_Ptr) simulation_run_data(this_simulation_run);

  printf("\n");

  printf("random seed = %d \n", sim_data->random_seed);
  printf("call arrival count = %ld \n", sim_data->call_arrival_count);
  printf("dropped call count = %ld \n", sim_data->number_of_calls_dropped);

  xmtted_fraction = (double) (sim_data->number_of_calls_dropped)/sim_data->call_arrival_count;

  printf("Leave probability = %.5f (Service fraction = %.5f)\n",
	 xmtted_fraction, 1-xmtted_fraction);

  printf("\n");
}

void output_results_mod(Simulation_Run_Ptr this_simulation_run)
{
  Simulation_Run_Data_Ptr sim_data = (Simulation_Run_Data_Ptr) simulation_run_data(this_simulation_run);

  double xmtted_fraction = (double) (sim_data->number_of_calls_dropped)/sim_data->call_arrival_count;
  sim_data->accumulated_leave_prob += xmtted_fraction;
  sim_data->accumulated_delay += sim_data->accumulated_call_time/sim_data->number_of_calls_processed;
  sim_data->accumulations += 1;
}

void output_all_results(Simulation_Run_Ptr sim_run)
{
  Simulation_Run_Data_Ptr sim_data;
  sim_data = (Simulation_Run_Data_Ptr) simulation_run_data(sim_run);
  for (int N = 0; N < sim_data->N; N++) {
    printf("%d", N+1);
    for (int W = 0; W < sim_data->N; W++) {
      printf(", %f", sim_data->results[N][W]);
    }
    printf("\n");
  }
}



