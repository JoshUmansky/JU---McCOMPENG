#include <px4_platform_common/px4_config.h>
#include <px4_platform_common/log.h>

#include <drivers/drv_hrt.h>
#include <uORB/Publication.hpp>
#include <uORB/topics/test_motor.h>
#include <uORB/topics/rc_channels.h>
#include <uORB/topics/led_control.h>
#include <uORB/topics/debug_value.h>



#define DC_MOTOR 0
#define ANGLE_MOTOR 1


extern "C" __EXPORT int hello_world_main(int argc, char *argv[]);

 int hello_world_main(int argc, char *argv[])
 {
 	debug_value_s debug_data;
	int debug_handle = orb_subscribe(ORB_ID(debug_value));
	orb_set_interval(debug_handle, 500);
	
	test_motor_s test_motor;
	rc_channels_s rc_channels;
	double motor_value = 0; // a number between 0 to 1

	uORB::Publication<test_motor_s> test_motor_pub(ORB_ID(test_motor));

	int rc_combined_handle = orb_subscribe(ORB_ID(rc_channels));
	orb_set_interval(rc_combined_handle, 200);
	float motor_speed = 0;
	
	PX4_INFO("Motor speed is %f", (double)(motor_speed));
	test_motor.timestamp = hrt_absolute_time();
	test_motor.motor_number = DC_MOTOR;
	test_motor.value = 0.5f;
	test_motor.action = test_motor_s::ACTION_RUN;
	test_motor.driver_instance = 0;
	test_motor.timeout_ms = 0;
	
	test_motor_pub.publish(test_motor);


	 while(1)
	 {
		orb_copy(ORB_ID(debug_value), debug_handle, &debug_data);
		orb_copy(ORB_ID(rc_channels), rc_combined_handle, &rc_channels);
		
		if(motor_value > 1.0 || motor_value < 0)
		 break;

		//float motor_speed = (rc_channels.channels[2] + 1.0f)/2;
		float turn = (-rc_channels.channels[0] + 1.0f)/2;
		float distance = debug_data.value;
		int direction = debug_data.ind-1; // -1=left, 0=straight, 1=right
	
		//turn = (float)((-direction)+1.0f)/2;		
		
		if(distance < 50 && distance > 15){
			motor_speed = (rc_channels.channels[2]/5 + 1.0f)/2;
			turn = (float)((-direction)+1.0f)/2;
		} else if(distance < 15){
			motor_speed = 0.5f;
			turn = (float)((-direction)+1.0f)/2;
		} else{
			motor_speed = (rc_channels.channels[2] + 1.0f)/2;
		}
		
		
		
		PX4_INFO("Motor speed is %f", (double)(motor_speed));
		test_motor.timestamp = hrt_absolute_time();
		test_motor.motor_number = DC_MOTOR;
		test_motor.value = motor_speed;
		test_motor.action = test_motor_s::ACTION_RUN;
		test_motor.driver_instance = 0;
		test_motor.timeout_ms = 0;

		test_motor_pub.publish(test_motor);

		//sleep(10);

		// Turn
		PX4_INFO("The motor will be stopped");
		test_motor.timestamp = hrt_absolute_time();
		test_motor.motor_number = ANGLE_MOTOR;
		test_motor.value = turn;
		test_motor.driver_instance = 0;
		test_motor.timeout_ms = 0;

		test_motor_pub.publish(test_motor);
	 }

	PX4_INFO("The motor will be stopped");
	test_motor.timestamp = hrt_absolute_time();
	test_motor.motor_number = DC_MOTOR;
	test_motor.value = 0.5;
	test_motor.driver_instance = 0;
	test_motor.timeout_ms = 0;

	test_motor_pub.publish(test_motor);

	return 0;
}