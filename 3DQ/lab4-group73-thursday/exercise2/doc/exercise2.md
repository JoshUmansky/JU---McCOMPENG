### Exercise 2

Modify the built-in self-test (BIST) engine from __experiment 4__ as follows. To verify all the 2<sup>18</sup> (or 256k) locations of the external SRAM, two sessions of writes and reads will be performed: the first one for the 2<sup>17</sup> (or 128k) **even** locations and the second one for the 128k **odd** locations, as explained below.

In the first session, the 128k **even** locations will be verified by first writing the value of the 16 least significant bits of the address (as done in the lab). While writing the data during the first session, the address lines must change in _increasing_ order. Then, the same 128k even locations will be read to verify their content. However, when reading the data during the first session, the address lines must change in the _decreasing_ order.

After the first session, the BIST engine will perform the same action for the **odd** locations during the second session. As for the even locations, when writing data in the odd locations, the address lines must change in _increasing_ order. Then, the 128k odd locations will be read and compared against the expected values to verify their content. Again, as for the even locations, when reading during the second session, the address lines must change in _decreasing_ order.

It is important to note that in each of the two sessions, every location must be written exactly once and read and checked exactly once.

Submit your sources, and in your report, write approximately half a page (but not more than a full page) that describes your reasoning. Your sources should follow the directory structure from the in-lab experiments (already set up for you in the `exercise2` folder); note, your report (in `.pdf`, `.txt` or `.md` format) should be included in the `exercise2/doc` sub-folder. Note also that although this lab is focused on simulation, your design must still pass compilation in Quartus before you simulate it and write the report.

Your submission is due 16 hours before your next lab session. Late submissions will be penalized.

