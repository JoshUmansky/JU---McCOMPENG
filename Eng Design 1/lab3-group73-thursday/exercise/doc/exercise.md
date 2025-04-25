### Exercise

After the changes to __experiment 4__ have been applied, support the following spec. 

We assume only the first ten alphabet keys from the PS2 keyboard ('A' to 'J') and space are monitored; consequently, if a PS/2 key not listed above has been pressed, you should take no action. As clarified next, your design must identify the most significant key pressed in the current _run_. 

- after power up (or when asynchronous reset is de-asserted), we start with _run_ 0; each time the space key is pressed, the _run_ is incremented; assume the number of runs never exceeds decimal 99;

- an alphabet key is more significant than another alphabet key if the corresponding character comes after it in alphabet order; for example, key 'B' is more significant than key 'A', key 'C' is more significant than both keys 'A' and 'B', and so on, key 'J' is more significant than any of the other nine keys that are monitored;

- display two messages: one gives the _run_ number, and the other one gives the most significant key (MSK) that has been pressed at least once in the current _run_; note, when a new _run_ starts, the history of all the previously pressed keys is cleared; note also, the _run_ value is displayed in binary coded decimal (BCD) format;

The location of the two messages on the screen does not matter so long as they are visible, and the message for the __run__ is above the message for MSK. 

The examples below clarify the functionality.

_Example 1_ - After power-up, we press alphabet keys in the following order: 'A', 'D', 'A, 'C', 'B', and 'C'. Then the two messages are:

`RUN 0`

`MSK D`

Note that it makes no difference that keys 'A' and 'C' have been pressed multiple times.

_Example 2_ - After the sequence of keys from _example 1_, we press the space key and then the following keys: 'A', 'B', 'C', 'B', 'C', 'B', and 'A'. Then the two messages are:

`RUN 1`

`MSK C`

Note that the history of the keys pressed in _example 1_ has been cleared after pressing space, and hence, the sequence of keys from _example 2_ is independent of what happened in _example 1_.

_Example 3_ - After the sequence of keys from _example 1_ and _example 2_ assume you press space nine times and then the following keys: 'F', 'G', 'F', 'I', 'E', 'B', 'C', 'A', 'D', and 'G'. Then the two messages are:

`RUN 10`

`MSK I`

The above example illustrates that the _run_ is displayed in BCD format.

As clarified through the above examples, the character to be displayed after `MSK` is determined each time a new key is pressed. Note also that when a new _run_ starts, no character will be displayed after `MSK` (i.e., display space) because no alphabet key has been pressed in the new _run_.

When testing the design on the board, assume that at most one key is pressed in each frame (one entire period of V\_SYNC). In simulation, if you choose to use it while troubleshooting, to make it faster, it is recommended that you schedule multiple PS/2 key events in `board_events.txt` before the end of the first vertical blanking period, so only one full frame needs to be simulated to produce a `.ppm` file in the `exercise/data` sub-folder.

In your report, you __MUST__ discuss your resource usage in terms of registers. You should relate your estimate to the register count from the compilation report in Quartus.

__HINT__ - Think of a ten-bit register that gets cleared each time the space key has been pressed, and each of its individual bits is set when the corresponding key (from the ten monitored keys) has been pressed. This register can control a priority encoder whose output encodes an offset (from the address of character 'A') that is used to look up the address of the character to be displayed after MSK. In addition to learning about video interfaces, it is essential to start developing a hardware-centric mindset when tackling circuit challenges, as in this take-home exercise.

Submit your sources, and in your report, write approximately half a page (but not more than a full page) that describes your reasoning. Your sources should follow the directory structure from the in-lab experiments (already set up for you in the `exercise` folder); note that your report (in `.pdf`, `.txt` or `.md` format) should be included in the `exercise/doc` sub-folder.

Your submission is due 16 hours before your next lab session. Late submissions will be penalized.
