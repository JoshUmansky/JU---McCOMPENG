### Take-home exercise

Modify **experiment 5** to support the following behaviour on the green LEDs.

- The green LED 8 is lightened only if at least one of the switches 16 down to 10 is high;

- The green LED 7 is lightened only if all of the switches 16 down to 10 are high;

- The green LED 6 is lightened only if the number of switches from 16 down to 10 that are high is an even number;

- The green LED 5 is lightened only if at least one of the switches 9 down to 3 is low;

- The green LED 4 is lightened only if none of the switches 9 down to 3 is low;

- The green LED 3 is lightened only if the number of switches from 9 down to 3 that are low is an even number;

- The green LED 2 is lightened only if at least two of the switches 2 down to 0 are high;

- The green LED 1 is lightened only if at most two of the switches 2 down to 0 are high;

- The green LED 0 is lightened only if exactly two of the switches 2 down to 0 are high.

The 7-segment displays show the content of an 8-bit binary counter that updates every second, and it counts *up* or *down* from ALL0 (all bits are zero) to ALL1 (all bits are 1), as detailed below.

- The meaning of push-buttons 0, 1 and 2 from the in-lab contribution to **experiment 5** are the same; you will need to add the functionality for push-button 3, as described next; note, when the counter counts *up* after it reaches its highest value ALL1 it will roll over to ALL0 and continue counting *up*; conversely, when it counts *down* after it reaches its lowest value ALL0, it will roll over to ALL1 and continue counting *down*;

- The 7-segment displays will be in one of the two display modes: hexadecimal format (**hex**) or octal format; in the **hex** mode, the two rightmost 7-segment displays will show the value of the counter in hexadecimal format, and all the other 7-segment displays will not be lightened (as if the board is powered off); note, the **hex** mode is the default display mode for the code released for **experiment 5**; in the **oct** mode the three rightmost 7-segment displays will show the value of the counter in octal format, and all the other 7-segment displays will not be lightened;

- Each time after the push-button 3 has been pressed and released, the display mode will change as follows (take note that push-button 3 must be released for the display mode change to take effect): from **hex** mode transition to **oct** mode; from **oct** mode transition to **hex**; if push-button 3 is pressed while the counter is stopped (as controlled by push-button 0) then each time it is released push-button 3 will produce a change of display mode;

- It is assumed that after any push-button has been pressed, no other push-button activity will occur until at least one second has passed after the respective push-button has been released; 

- On power-up assume the counter is active in state ALL0, the counting direction is *up*, and display mode is **hex**.

Submit your sources, and in your report, write approximately half a page (but not more than a full page) describing your reasoning. Your sources should follow the directory structure from the in-lab experiments (already set up for you in the `exercise` folder); note your report (in `.pdf,` `.txt` or `.md` format) should be included in the `exercise/doc` sub-folder.

Your submission is due 16 hours before your next lab session. Late submissions will be penalized.
