
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
#include "trace.h"
#include "main.h"
#include "output.h"
#include "packet_transmission.h"

/******************************************************************************/

/*
 * This function will schedule the end of a packet transmission at a time given
 * by event_time. At that time the function "end_packet_transmission" (defined
 * in packet_transmissionl.c) is executed. A packet object is attached to the
 * event and is recovered in end_packet_transmission.c.
 */

long
schedule_end_packet_transmission_event(Simulation_Run_Ptr simulation_run,
				       double event_time,
				       Server_Ptr link)
{
  Event event;

  event.description = "Packet Xmt End";
  event.function = end_packet_transmission_event;
  event.attachment = (void *) link;

  return simulation_run_schedule_event(simulation_run, event, event_time);
}

/******************************************************************************/

/*
 * This is the event function which is executed when the end of a packet
 * transmission event occurs. It updates its collected data then checks to see
 * if there are other packets waiting in the fifo queue. If that is the case it
 * starts the transmission of the next packet.
 */

void
end_packet_transmission_event(Simulation_Run_Ptr simulation_run, void * link)
{
  Simulation_Run_Data_Ptr data;
  Packet_Ptr this_packet, next_packet;

  data = (Simulation_Run_Data_Ptr) simulation_run_data(simulation_run);


  TRACE(printf("End Of Packet from link:%d.\n", (link==data->link1) ? 1 : ((link==data->link2) ? 2 : 3)););

  /* 
   * Packet transmission is finished. Take the packet off the data link.
   */

  this_packet = (Packet_Ptr) server_get(link);
  TRACE(printf("Packet source:%d, destination:%d\n", this_packet->source_id, this_packet->destination_id);)

  if(this_packet->destination_id == 2 || this_packet->destination_id == 3){ // transfers from server 1 to the next two
    this_packet->service_time = get_packet_transmission_time_multi(simulation_run, data->link2);
    if(this_packet->destination_id==2) { // determine which link it is going to
      link = data->link2;
      this_packet->destination_id=0;
    } else if(this_packet->destination_id==3) {
      link = data->link3;
      this_packet->destination_id=0;
    }

    this_packet->service_time = get_packet_transmission_time_multi(simulation_run, link); // make new service_time

    if(link == data->link2){ // dictates whether queued or go through server
      TRACE(printf("Transferring packet to link:2.\n"););

      if(server_state(data->link2) == BUSY) {
        TRACE(printf("Queueing in buffer 2. New buffer size:%d\n",fifoqueue_size(data->buffer3)+1);)
        fifoqueue_put(data->buffer2, (void*) this_packet);
      } else {
        start_transmission_on_link(simulation_run, this_packet, data->link2);
      }
    } else {//if(link == data->link3){
      TRACE(printf("Transferring packet to link:3.\n"););

      if(server_state(data->link3) == BUSY) {
        TRACE(printf("Queueing in buffer 3. New buffer size:%d\n",fifoqueue_size(data->buffer3)+1);)
        fifoqueue_put(data->buffer3, (void*) this_packet);
      } else {
        start_transmission_on_link(simulation_run, this_packet, data->link3);
      }
    }

    if(fifoqueue_size(data->buffer1) > 0) {
      TRACE(printf("Taking from buffer 1. New buffer size:%d\n",fifoqueue_size(data->buffer1)+1);)
      next_packet = (Packet_Ptr) fifoqueue_get(data->buffer1);
      start_transmission_on_link(simulation_run, next_packet, data->link1);
    }
    
    return;
  }

  /* Collect statistics. */
  double time = simulation_run_get_time(simulation_run);
  data->number_of_packets_processed++;
  data->accumulated_delay += time - this_packet->arrive_time;
  if(this_packet->source_id == 1){
    data->number_of_packets_processed_1++;
    data->accumulated_delay_1 += time - this_packet->arrive_time;
  } else if(this_packet->source_id == 2){
    data->number_of_packets_processed_2++;
    data->accumulated_delay_2 += time - this_packet->arrive_time;
  } else {
    data->number_of_packets_processed_3++;
    data->accumulated_delay_3 += time - this_packet->arrive_time;
  }
    /* Part 3 Data collection
    if((simulation_run_get_time(simulation_run) -
    this_packet->arrive_time)*1e3 >= 20)
    {
        data->number_of_delay_greater_then_20 += 1;
    }
    */
  /* Output activity blip every so often. */
  output_progress_msg_to_screen(simulation_run);

  /* 
   * See if there is are packets waiting in the buffer. If so, take the next one
   * out and transmit it immediately.
  */

  /*if(fifoqueue_size(data->buffer1) > 0) {
    next_packet = (Packet_Ptr) fifoqueue_get(data->buffer1);
    start_transmission_on_link(simulation_run, next_packet, link);
  }*/
  if(link == data->link2) {
    if(fifoqueue_size(data->buffer2) > 0) {
      next_packet = (Packet_Ptr) fifoqueue_get(data->buffer2);
      start_transmission_on_link(simulation_run, next_packet, link);
    }
  } else if(link == data->link3) {
    if(fifoqueue_size(data->buffer3) > 0) {
      next_packet = (Packet_Ptr) fifoqueue_get(data->buffer3);
      start_transmission_on_link(simulation_run, next_packet, link);
    }
  }

  /* This packet is done ... give the memory back. */
  xfree((void *) this_packet);
}

/*
 * This function ititiates the transmission of the packet passed to the
 * function. This is done by placing the packet in the server. The packet
 * transmission end event for this packet is then scheduled.
 */

void
start_transmission_on_link(Simulation_Run_Ptr simulation_run, 
			   Packet_Ptr this_packet,
			   Server_Ptr link)
{
  Simulation_Run_Data_Ptr data;
  data = (Simulation_Run_Data_Ptr) simulation_run_data(simulation_run);

  TRACE(printf("Start Of Packet on link:%d.\n", (link==data->link1) ? 1 : ((link==data->link2) ? 2 : 3));)
  TRACE(printf("Packet source:%d.\n", this_packet->source_id);)
  server_put(link, (void*) this_packet);
  this_packet->status = XMTTING;

  /* Schedule the end of packet transmission event. */
  schedule_end_packet_transmission_event(simulation_run,
  simulation_run_get_time(simulation_run) + this_packet->service_time,
  (void *) link);
}

/*
 * Get a packet transmission time. For now it is a fixed value defined in
 * simparameters.h
 */

double
get_packet_transmission_time(void)
{
  return ((double) PACKET_XMT_TIME);
}

double
get_packet_transmission_time_multi(Simulation_Run_Ptr simulation_run, Server_Ptr link)
{
  double PACKET_TIME = 0;
  Simulation_Run_Data_Ptr data;
  data = (Simulation_Run_Data_Ptr) simulation_run_data(simulation_run);

  if(link == data->link1)
    PACKET_TIME = (double) PACKET_XMT_TIME_1;
  else
    PACKET_TIME = (double) PACKET_XMT_TIME_2_3;
  return (PACKET_TIME);
}


