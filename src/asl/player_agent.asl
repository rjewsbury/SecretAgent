
!play.

//If agent is an AntiVirus
+!play
	: role(R) & R = 2
	<- .print("I am AntiVirus").
	
//If agent is a Rogue
+!play
	: role(R) & R = 1
	<- .print("I am Rogue").
	
//If agent is a Rogue
+!play
	: role(R) & R = 0
	<- .print("I am Virus").

+!foobar
