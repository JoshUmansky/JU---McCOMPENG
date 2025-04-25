
/*
 * Simulation_Run of the ALOHA Protocol
 * 
 * Copyright (C) 2014 Terence D. Todd Hamilton, Ontario, CANADA
 * todd@mcmaster.ca
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

/*******************************************************************************/

#include <stdio.h>
#include "simparameters.h"
#include "main.h"
#include "output.h"

/*******************************************************************************/

void
output_blip_to_screen(Simulation_Run_Ptr simulation_run)
{
  double percentagedone;
  Simulation_Run_Data_Ptr data;

  data = (Simulation_Run_Data_Ptr) simulation_run_data(simulation_run);

  data->blip_counter++;

  if((data->blip_counter >= BLIPRATE)
     ||
     (data->number_of_packets_processed >= RUNLENGTH)) {

    data->blip_counter = 0;

    percentagedone =
      100 * (double) data->number_of_packets_processed/RUNLENGTH;

    printf("%3.0f%% ", percentagedone);

    printf("Successfully Xmtted Pkts  = %ld (Arrived Pkts = %ld) \r", 
	   data->number_of_packets_processed, 
	   data->arrival_count);

    fflush(stdout);
  }
}

/**********************************************************************/

void output_results(Simulation_Run_Ptr this_simulation_run)
{
  int i;
  double xmtted_fraction;
  Simulation_Run_Data_Ptr sim_data;

  sim_data = (Simulation_Run_Data_Ptr) simulation_run_data(this_simulation_run);

  printf("\n");
  printf("Random Seed = %d \n", sim_data->random_seed);
  printf("Pkt Arrivals = %ld \n", sim_data->arrival_count);

  xmtted_fraction = (double) sim_data->number_of_packets_processed /
    sim_data->arrival_count;

  printf("Xmtted Pkts  = %ld (Service Fraction = %.5f)\n",
	 sim_data->number_of_packets_processed, xmtted_fraction);

  printf("Mean Delay   = %.1f \n",
	 sim_data->accumulated_delay/sim_data->number_of_packets_processed);

  printf("Mean collisions per packet = %.3f\n",
	 (double) sim_data->number_of_collisions / 
	 sim_data->number_of_packets_processed);

  for(i=0; i<sim_data->num_stations; i++) {

    printf("Station %2i Mean Delay = %8.1f \n", i,
	   (sim_data->stations+i)->accumulated_delay / 
	   (sim_data->stations+i)->packet_count);
  }
  printf("\n\n");
}

void output_results_mod(Simulation_Run_Ptr sim_run)
{
  double xmtted_fraction;
  Simulation_Run_Data_Ptr sim_data;

  sim_data = (Simulation_Run_Data_Ptr) simulation_run_data(sim_run);

  double stations_delay = 0;

  for (int i = 0; i < sim_data->num_stations; i++) { // Part 4
    if (i == 0) // skip station #1 which has no backoff
      continue;
    
    stations_delay += (sim_data->stations+i)->accumulated_delay/(sim_data->stations+i)->packet_count;
  }
  stations_delay /= (sim_data->num_stations - 1); // average delay of stations with backoff
  //double mean_delay = stations_delay; // Part 4

  double now = simulation_run_get_time(sim_run);

  double single_station_delay = sim_data->stations->accumulated_delay/sim_data->stations->packet_count;

  double mean_delay = sim_data->accumulated_delay/sim_data->number_of_packets_processed; // Part 2

  // Parts 3 & 4
  double throughput = sim_data->number_of_packets_processed / (now / sim_data->mean_duration);
  double throughput_1 = sim_data->stations->packet_count / (now / sim_data->mean_duration); // throughput of no backoff channel

  sim_data->throughput+=throughput;
  sim_data->throughput_1+=throughput_1;
  
  //sim_data->total_delay+= mean_delay; // part 2
  sim_data->total_delay += stations_delay;
  sim_data->total_delay_station += single_station_delay;

  sim_data->accumulations += 1;

  if(sim_data->random_seed == 400408809) {
    //printf("Accumulated_probability: %f\nRuns: %d\n", sim_data->accumulated_probability, sim_data->accumulations);
    sim_data->total_delay = sim_data->total_delay / sim_data->accumulations;
    sim_data->total_delay_station = sim_data->total_delay_station / sim_data->accumulations;
    
    sim_data->throughput /= sim_data->accumulations;
    
    //printf("%f, %f, %f\n", sim_data->arrival_rate, sim_data->total_delay_station ,sim_data->total_delay); // Part 4
    //printf("%f, %f\n", sim_data->arrival_rate, sim_data->throughput); // Parts 3
    printf("%f, %f\n", sim_data->arrival_rate, sim_data->total_delay); // Parts 2
  }
}

