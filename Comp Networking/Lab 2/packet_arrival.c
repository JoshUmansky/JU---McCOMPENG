
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

#include <math.h>
#include <stdio.h>
#include "main.h"
#include "packet_transmission.h"
#include "packet_arrival.h"
#include "trace.h"

/******************************************************************************/

/*
 * This function will schedule a packet arrival at a time given by
 * event_time. At that time the function "packet_arrival" (located in
 * packet_arrival.c) is executed. An object can be attached to the event and
 * can be recovered in packet_arrival.c.
 */

long int
schedule_packet_arrival_event(Simulation_Run_Ptr simulation_run,
			      double event_time, Server_Ptr link)
{
  Event event;

  event.description = "Packet Arrival";
  event.function = packet_arrival_event;
  event.attachment = (void *) link;
  //event.link = link;

  return simulation_run_schedule_event(simulation_run, event, event_time);
}

/******************************************************************************/

/*
 * This is the event function which is executed when a packet arrival event
 * occurs. It creates a new packet object and places it in either the fifo
 * queue if the server is busy. Otherwise it starts the transmission of the
 * packet. It then schedules the next packet arrival event.
 */

void
packet_arrival_event(Simulation_Run_Ptr simulation_run, void * ptr)
{
  Simulation_Run_Data_Ptr data;
  Packet_Ptr new_packet;

  Server_Ptr link = (Server_Ptr)ptr;

  data = (Simulation_Run_Data_Ptr) simulation_run_data(simulation_run);
  data->arrival_count++;

  new_packet = (Packet_Ptr) xmalloc(sizeof(Packet));
  new_packet->arrive_time = simulation_run_get_time(simulation_run);
  new_packet->service_time = get_packet_transmission_time_multi(simulation_run, link);
  new_packet->status = WAITING;
  
  /* 
   * Start transmission if the data link is free. Otherwise put the packet into
   * the buffer.
   */
  // part 5 code
  if(link == data->link1){ // send 
    new_packet->source_id = 1; // indicates it came from first server
    int chance = rand()%100 ;
    TRACE(printf("Chance roll, %d\%, p12=%d\n",chance,data->p12);)
    if(chance < data->p12) //
      new_packet->destination_id = 2;
    else
      new_packet->destination_id = 3; 

    if(server_state(data->link1) == BUSY) {
      TRACE(printf("Queueing in buffer 1. New buffer size:%d\n",fifoqueue_size(data->buffer3)+1);)
      fifoqueue_put(data->buffer1, (void*) new_packet);
    } else {
      start_transmission_on_link(simulation_run, new_packet, data->link1);
    }
  } else if(link == data->link2){
    new_packet->source_id = 2;
    new_packet->destination_id = 0;
    if(server_state(data->link2) == BUSY) {
      TRACE(printf("Queueing in buffer 2. New buffer size:%d\n",fifoqueue_size(data->buffer3)+1);)
      fifoqueue_put(data->buffer2, (void*) new_packet);
    } else {
      start_transmission_on_link(simulation_run, new_packet, data->link2);
    }
  } else {
    new_packet->source_id = 3;
    new_packet->destination_id = 0;
    if(server_state(data->link3) == BUSY) {
      TRACE(printf("Queueing in buffer 3. New buffer size:%d\n",fifoqueue_size(data->buffer3)+1);)
      fifoqueue_put(data->buffer3, (void*) new_packet);
    } else {
      start_transmission_on_link(simulation_run, new_packet, data->link3);
    }
  }

  /*if(server_state(data->link1) == BUSY && server_state(data->link2)==BUSY) {
    fifoqueue_put(data->buffer, (void*) new_packet);
  } else if (server_state(data->link1) != BUSY && server_state(data->link2) == BUSY) {
    start_transmission_on_link(simulation_run, new_packet, data->link1);
  }
    else if(server_state(data->link1) == BUSY && server_state(data->link2) != BUSY)
    {
        start_transmission_on_link(simulation_run, new_packet, data->link2);
    }
    else
    {
        //if both open, start with link1
        start_transmission_on_link(simulation_run, new_packet, data->link1);
    }*/

  /* 
   * Schedule the next packet arrival. Independent, exponentially distributed
   * interarrival times gives us Poisson process arrivals.
   */
  int ARRIVAL_RATE = 0;
  if(link == NULL) {
    ARRIVAL_RATE = PACKET_ARRIVAL_RATE;
  } else if(link == data->link1){
    ARRIVAL_RATE = PACKET_ARRIVAL_RATE_1;
  } else {
    ARRIVAL_RATE = PACKET_ARRIVAL_RATE_2_3;
  }

  schedule_packet_arrival_event(simulation_run,
			simulation_run_get_time(simulation_run) +
			exponential_generator((double) 1/ARRIVAL_RATE), link);
}



