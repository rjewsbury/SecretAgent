
//Player Roles
isKernel :- kernel(K) & player(N) & N = K.
isScheduler :- scheduler(S) & player(N) & N = S.

//Player Identities
isVirus :- role(R) & R = 0.
isRogue :- role(R) & R = 1.
isAntiVirus :- role(R) & R = 2.

!identity.

//If agent is an AntiVirus
+!identity
	: isAntiVirus
	<- .print("I am an AntiVirus");
	   !play.
	
//If agent is a Rogue
+!identity
	: isRogue
	<- .print("I am a Rogue");
	   !play.
	
//If agent is a Rogue
+!identity
	: isVirus
	<- .print("I am a Virus");
	   !play.	   
	   
	// Methods each agent can do //
+!drawCards
	: isKernel
	<-	drawThree;
		!passCards;
		!play.

+!discardCard
	: isKernel 
	<-	discardVirus.
	
+!discardCard
	: isKernel
	<-	discardAntiVirus.
		
+!passCards
	: isKernel
	<-	passCards.


	//Logic to play when player is Virus
+!play
	: isVirus
	<- !play.

	
	
	//Logic to play when player is Rogue
+!play
	: isRogue
	<- !play.
	

	// Logic to play when player is Virus //	
//If Kernel
+!play
	: isKernel & isVirus
	<- 	//?schedulerCandidate(X);
		//electScheduler(X);
		!play.

//If Scheduler
+!play
	: isScheduler & isVirus
	<- 	discardVirus;
		playAntiVirus;
		!play.

	
	// Logic to play when player is Rogue //
	
//If Kernel
+!play
	: isKernel & isRogue
	<- 	//?schedulerCandidate(X);
		//electScheduler(X);
		!play.

//If Scheduler
+!play
	: isScheduler & isRogue
	<- 	discardVirus;
		playAntiVirus;
		!play.

		
		// Logic to play when player is AntiVirus //

//If Kernel
+!play
	: isKernel & isAntiVirus
	<- 	//?schedulerCandidate(X);
		//electScheduler(X);
		drawThree;
		!play.
		
		//If Scheduler 
//If has 1 all
+!play
	: isScheduler & isAntiVirus
	<- 	discardVirus;
		playAntiVirus;
		!play.

		//If has 2 of same
+!play
	: isScheduler & isAntiVirus
	<- 	discardAntiVirus;
		playAntiVirus;
		!play.
		
+!play
	: isScheduler & isAntiVirus
	<- 	discardVirus;
		playVirus;
		!play.
		
