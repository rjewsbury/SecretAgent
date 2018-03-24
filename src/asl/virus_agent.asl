//Ending the game
virusWin :- virusPlayed(V) & V = 6.
antiVirusWin :- antiVirusPlayed(A) & A = 5.

//Player Elected Position
isKernel :- kernel(K) & player(N) & N = K.
isScheduler :- scheduler(S) & player(N) & N = S.

//Player Identities
isVirus :- role(R) & R = 0.
isRogue :- role(R) & R = 1.
isAntiVirus :- role(R) & R = 2.

!identity.

//states their roles to the terminal before playing
+!identity
	: isAntiVirus
	<- .print("I am an AntiVirus");
	   !play.

+!identity
	: isRogue
	<- .print("I am a Rogue");
	   !play.

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
		?player(ID);
		.print(ID);
		!drawCards.

+!play
	: isScheduler
	<- !discardCard.

+!play
	: not isKernel
	<-	wait;
		!play.

-!play
	:true
	<-	.print("FAILED TO PLAY");
		wait;
		!play.
	   
+!electScheduler
	: isKernel
	<-  //?schedulerCandidate(X);
		//electScheduler(X);
		.print("ELECT SCHEDULER NOT IMPLEMENTED");
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
	
	
	
// ----------------------------------
// Logic to play when player is VIRUS
// ----------------------------------
	
//If Kernel
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
//If has 1 of each type
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
		

	
// ----------------------------------
// Logic to play when player is ROGUE
// ----------------------------------
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
		

		
// ----------------------------------
// Logic to play when player is ANTIVIRUS
// ----------------------------------

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
	
//If has 1 of each
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
