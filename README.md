# ntrip_load

Dumb load test for caster.

Need openjdk java >21  (>22 ideally)

$ java ./src/main/java/com/test/NtripLoadTest.java

will launch multiple base + multiple client on a targetted caster ( constant set to 127.0.0.1 in the file )
 -> Password set to letmein  for mountpoint
 -> password set to centipede:letmein  for client

Mountpoint are named  "TESTxx" with xx a number between 0 and max_number_of_mountpoint-1.


A "demo server" is also present  

$ java ./src/main/java/com/test/NtripServer.java 



Data created by the mountpoint are 1500 char + some.  It contains "iteration: " + a incremental number + "-" repeated. 

You can read a mountpoint with:

$ str2str -in ntrip://centipede:letmein@localhost:2101/TEST123 | sed "s/-//g"

so that you can just see the "iteration" number message (to check if some message are not lost, or delivered late)


 if you want to manually create a mountpoint, you can use 

$ java ./src/main/java/com/test/NtripProducer.java 

which will listen on TCP 5015 and will send the message  "iteration: " + a incremental number + "-" repeated.

so that you can use: 

str2str -in tcpcli://localhost:5015 -out ntrips://:letmein@localhost:2101/MYTEST


