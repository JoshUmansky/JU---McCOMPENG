
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

#include <math.h>
#include <stdio.h>
#include "packet_duration.h"
#include "packet_transmission.h"
#include "packet_arrival.h"

/*******************************************************************************/

long int
schedule_packet_arrival_event(Simulation_Run_Ptr simulation_run,
			      Time event_time)
{
  Event event;

  event.description = "Packet Arrival";
  event.function = packet_arrival_event;
  event.attachment = NULL;

  return simulation_run_schedule_event(simulation_run, event, event_time);
}

/*******************************************************************************/

void
packet_arrival_event(Simulation_Run_Ptr simulation_run, void* dummy_ptr) 
{
  int random_station_id;
  Station_Ptr station;
  Packet_Ptr new_packet;
  Buffer_Ptr stn_buffer;
  Time now;
  Simulation_Run_Data_Ptr data;

  now = simulation_run_get_time(simulation_run);

  data = (Simulation_Run_Data_Ptr) simulation_run_data(simulation_run);
  data->arrival_count++;

  /* Randomly pick the station that this packet is arriving to. Note
     that randomly splitting a Poisson process creates multiple
     independent Poisson processes.*/
  random_station_id = (int) floor(uniform_generator()*data->num_stations);
  station = data->stations + random_station_id;

  new_packet = (Packet_Ptr) xmalloc(sizeof(Packet));
  new_packet->arrive_time = now;
  new_packet->service_time = get_packet_duration(simulation_run);
  new_packet->status = WAITING;
  new_packet->collision_count = 0;
  new_packet->station_id = random_station_id;

  /* Put the packet in the buffer at that station. */
  stn_buffer = station->buffer;
  fifoqueue_put(stn_buffer, (void *) new_packet);

  /* If this is the only packet at the station, transmit it (i.e., the
     ALOHA protocol). It stays in the queue either way. */
  if(fifoqueue_size(stn_buffer) == 1) {
    /* Transmit the packet. */
    schedule_transmission_start_event(simulation_run, now, (void *) new_packet);
  }

  /* Schedule the next packet arrival. */
  schedule_packet_arrival_event(simulation_run, 
		now + exponential_generator((double) 1/data->arrival_rate));
}



