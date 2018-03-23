//Ending the game
virusWin :- virusPlayed(V) & V = 6.
antiVirusWin :- antiVirusPlayed(A) & A = 5.

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
	   
	   
	   //Things all Agents will do
+!play
	: virusWin
	<- .print("Virus team wins!").
	
+!play
	: antiVirusWin
	<- .print("AntiVirus team wins!").
	   
+!play
	: isKernel
	<- 	//?schedulerCandidate(X);
		//electScheduler(X);
		.print(R);
		!drawCards.
		
+!play
	: not isKernel
	<- !play.
	   
+!electScheduler
	: isKernel
	<-  //?schedulerCandidate(X);
		//electScheduler(X);
		!play.
		
+!drawCards
	: isKernel
	<- drawThree;
	   !discardCard.
	   
+!playCard
	: isScheduler & heldAntiVirus(A) & A > 0
	<- playAntiVirus.	

+!playCard
	: isScheduler & heldVirus(V) & V > 0
	<- playVirus.
	
	
	
	// 
	//Logic to play when player is VIRUS
	//	
	
//If Kernel
+!play
	: isKernel & isRogue
	<- !discardCard.

+!discardCard
	: isKernel & isVirus & heldVirus(V) & V = 0
	<- discardAntiVirus;
	   passCards;
	   !play.
	
+!discardCard
	: isKernel & isVirus & heldVirus(V) & V > 0
	<- discardVirus;
	   passCards;
	   !play.

		
		//If Scheduler 
//If has 1 all	
+!play
	: isScheduler & isVirus
	<- !discardCard.

+!discardCard
	: isScheduler & isVirus & heldVirus(V) & heldAntiVirus(A) & V = A
	<- discardVirus;
	   !playCard;
	   !play.

//If only has AntiVirus cards
+!discardCard
	: isScheduler & isVirus & heldVirus(V) & V = 0
	<- discardAntiVirus;
	   !playCard;
	   !play.
	   
//If only has Virus cards
+!discardCard
	: isScheduler & isVirus & heldVirus(V) & V = 2
	<- discardVirus;
	   !playCard;
	   !play.
		

	
	// 
	//Logic to play when player is ROGUE
	//	
	
//If Kernel
+!play
	: isKernel & isRogue
	<- !discardCard.

+!discardCard
	: isKernel & isRogue & heldAntiVirus(A) & A = 0
	<- discardVirus;
	   passCards;
	   !play.
	
+!discardCard
	: isKernel & isRogue  & heldAntiVirus(A) & A > 1
	<- discardAntiVirus;
	   passCards;
	   !play.
	
		
		// If Scheduler //
+!play
	: isScheduler & isRogue
	<- !discardCard.
	
//If has 1 all
+!discardCard
	: isScheduler & isRogue & heldVirus(V) & heldAntiVirus(A) & V = A
	<- discardAntiVirus;
	   !playCard;
	   !play.

//If only has 2 AntiVirus cards
+!discardCard
	: isScheduler & isRogue  & heldAntiVirus(A) & A = 2
	<- discardAntiVirus;
	   !playCard;
	   !play.
	   
//If only has Virus cards
+!discardCard
	: isScheduler & isRogue  & heldVirus(V) & V = 2
	<- discardVirus;
	   !playCard;
	   !play.
		

		
		// Logic to play when player is ANTIVIRUS //

//If Kernel
+!play
	: isKernel & isAntiVirus
	<- 	//?schedulerCandidate(X);
		//electScheduler(X);
		!drawCards.

+!discardCard
	: isKernel & heldVirus(V) & V > 0
	<- discardVirus;
	   passCards;
	   !play.
	   
+!discardCard
	: isKernel & heldAntiVirus(A) & A = 3
	<- discardAntiVirus;
	   passCards;
	   !play.
		
		//If Scheduler 
+!play
	: isScheduler & isAntiVirus
	<- !discardCard.
	
//If has 1 all
+!discardCard
	: isScheduler & heldVirus(V) & heldAntiVirus(A) & V = A
	<- discardVirus;
	   !playCard;
	   !play.

//If only has Virus cards
+!discardCard
	: isScheduler & heldVirus(V) & V = 2
	<- discardVirus;
		.print("Have 2 Virus cards");
	   !playCard;
	   !play.
	   
//If only has AntiVirus cards
+!discardCard
	: isScheduler & heldAntiVirus(A) & A = 2
	<- discardAntiVirus;
	   !playCard;
	   !play.
	   
-!play
	:true
	<- !play.
