
isKernel :- kernel(K) & player(N) & N = K.
isScheduler :- scheduler(S) & player(N) & N = S.

//Roles
isVirus :- role(R) & R = 0.
isRogue :- role(R) & R = 1.
isAntiVirus :- role(R) & R = 2.



!identity.

//If agent is an AntiVirus
+!identity
	: isAntiVirus
	<- .print("I am an AntiVirus").
	   //!play.
	
//If agent is a Rogue
+!identity
	: isRogue
	<- .print("I am a Rogue").
	   //!play.
	
//If agent is a Rogue
+!identity
	: isVirus
	<- .print("I am a Virus").
	   //!play.
	   
//Kernel Player Logic	   
	   
	   //Logic to when player is Kernel
+!play
	: isKernel
	<- ?schedulerCandidate(X);
		electScheduler(X);
		!play.
	
//Scheduler Player Logic
		
	//Logic to play when player is Scheduler and Virus
+!play
	: isScheduler & isVirus
	<- !play.

	//Logic to play when player is Scheduler and is Rogue
+!play
	: isScheduler & isRogue
	<- !play.
	
	//Logic to play when player is Scheduler and AntiVirus
+!play
	: isScheduler & isAntiVirus
	<- !play.
	
//Regular Player Logic
	
	//Logic to play when player is a Virus
+!play
	: isVirus
	<- !play.
	
	//Logic to play when player is a Rogue
+!play
	: isRogue
	<- !play.	
	
	//Logic to play when player is an AntiVirus
+!play
	: isAntiVirus
	<- !play.
