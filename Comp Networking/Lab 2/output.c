
/*
 * 
 * Simulation_Run of A Single Server Queueing System
 * 
 * Copyright (C) 2014 Terence D. Todd Hamilton, Ontario, CANADA,
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

/******************************************************************************/

#include <stdio.h>
#include "simparameters.h"
#include "main.h"
#include "output.h"

/******************************************************************************/

/*
 * This function outputs a progress message to the screen to indicate this are
 * working.
 */
double summarized_value;
double sum_1 = 0;
double sum_2 = 0;
double sum_3 = 0;

void
output_progress_msg_to_screen(Simulation_Run_Ptr simulation_run)
{
  double percentage_done;
  Simulation_Run_Data_Ptr data;

  data = (Simulation_Run_Data_Ptr) simulation_run_data(simulation_run);

  data->blip_counter++;

  if((data->blip_counter >= BLIPRATE)
     ||
     (data->number_of_packets_processed >= RUNLENGTH)) {

    data->blip_counter = 0;

    percentage_done =
      100 * (double) data->number_of_packets_processed/RUNLENGTH;

    /*printf("%3.0f%% ", percentage_done);

    printf("Successfully Xmtted Pkts  = %ld (Arrived Pkts = %ld) \r",
	   data->number_of_packets_processed, data->arrival_count);*/

    fflush(stdout);
  }

}

/*
 * When a simulation_run run is completed, this function outputs various
 * collected statistics on the screen.
 */
void reset_sums() {
  sum_1 = 0;
  sum_2 = 0;
  sum_3 = 0;
}

void
output_results(Simulation_Run_Ptr simulation_run, int p12)
{
  double xmtted_fraction;
  Simulation_Run_Data_Ptr data;

  data = (Simulation_Run_Data_Ptr) simulation_run_data(simulation_run);

  //printf("\n");
  //printf("Random Seed = %d \n", data->random_seed);
  //printf("Packet arrival count = %ld \n", data->arrival_count);

  xmtted_fraction = (double) data->number_of_packets_processed /
    data->arrival_count;

  //printf("Transmitted packet count  = %ld (Service Fraction = %.5f)\n",
	// data->number_of_packets_processed, xmtted_fraction);

  //printf("Arrival rate = %.3f packets/second \n", (double) PACKET_ARRIVAL_RATE);

  //printf("Mean Delay (msec) = %.2f \n",
	// 1e3*data->accumulated_delay/data->number_of_packets_processed);

  // Link 1 data
  //printf("Transmitted packet count 1 = %ld (Service Fraction = %.5f)\n",
  //data->number_of_packets_processed_1, xmtted_fraction);

  //printf("Arrival rate = %.3f packets/second \n", (double) PACKET_ARRIVAL_RATE_1);
  double mean_1 = (1e3*data->accumulated_delay_1)/data->number_of_packets_processed_1;
  //printf("Mean Delay 1 (msec) = %f \n", mean_1);

  // Link 2 data
  //printf("Transmitted packet count 2  = %ld (Service Fraction = %.5f)\n",
  //data->number_of_packets_processed_2, xmtted_fraction);

  //printf("Arrival rate 2 = %.3f packets/second \n", (double) PACKET_ARRIVAL_RATE_2_3);

  //printf("Total delay 2 = %f\n", data->accumulated_delay_2);
  double mean_2 = 1e3*((double)data->accumulated_delay_2)/((double)data->number_of_packets_processed_2);
  //printf("Mean Delay 2 (msec) = %f \n", mean_2);

  // Link 3 data
  //printf("Transmitted packet count 3 = %ld (Service Fraction = %.5f)\n",
  //data->number_of_packets_processed_3, xmtted_fraction);

  //printf("Arrival rate 3 = %.3f packets/second \n", (double) PACKET_ARRIVAL_RATE_2_3);

  //printf("Total delay 3 = %f\n", data->accumulated_delay_3);
  double mean_3 = 1e3*data->accumulated_delay_3/data->number_of_packets_processed_3;
  //printf("Mean Delay 3 (msec) = %f \n", mean_3);

    /* ********Part 3 Data collection & output**********
    printf("Percentage over 20ms %.2f \n", 100*data->number_of_delay_greater_then_20/data->number_of_packets_processed);
    summarized_value += (100*data->number_of_delay_greater_then_20/data->number_of_packets_processed);
*/
    summarized_value += 1e3*data->accumulated_delay/data->number_of_packets_processed;
    sum_1 += mean_1;
    sum_2 += mean_2;
    sum_3 += mean_3;
//printf("Done seed:%d\n", data->random_seed);
if(data->random_seed == 234234)
{
   //Part3 printf("Average percentage over 20ms %.6f \n", summarized_value / 8);
    //printf("Average mean service time %0.2f", summarized_value/8);
    printf("%d, %f, %f, %f\n", p12, sum_1/8, sum_2/8, sum_3/8);
}
    //printf("\n");
}



