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
	   
//Win conditions
+!play
	: virusWin
	<- .print("Virus team wins!").
	
+!play
	: antiVirusWin
	<- .print("AntiVirus team wins!").
	
//if no scheduler has been elected, elect a scheduler
+!play
	: isKernel & not voteComplete & not electedScheduler(S)
	<- 	!electScheduler;
		!play.

+!play
	: not voteComplete
	<-	!vote;
		.print("Voted. waiting for complete vote");
		!waitForVote;
		!play.

+!play
	: not isKernel & not isScheduler & voteComplete
	<-	wait;
		!play.

//if the vote passed, draw 3 cards
+!play
	: isKernel & voteComplete & scheduler(S)
	<-	.print("Vote passed. drawing three cards");
		drawThree;
		.print("Discarding a card");
		!discardCard;
		.print("Passing cards");
		passCards;
		.print("Waiting for scheduler");
		!waitForScheduler;
		.print("Passing kernel")
		passKernel;
		!play.

+!play
	: isScheduler & voteComplete & ( heldVirus(V) | heldAntiVirus(A))
	<-	!discardCard;
		!play.
		
+!play
	: isScheduler & voteComplete & not ( heldVirus(V) | heldAntiVirus(A))
	<-	wait;
		!play.
		
//if the vote failed, pass kernel
+!play
	: isKernel & voteComplete & not scheduler(S)
	<-	.print("Vote Failed. Passing Kernel.");
		wait;
		passKernel;
		!play.
		
-!play
	:true
	<-	.print("FAILED TO PLAY");
		wait;
		!play.
	   
+!electScheduler
	: isKernel
	<-  ?schedulerCandidate(X);
		.print("Electing ",X);
		electScheduler(X);
		.print("Successfully chose candidate");
		!play.

//waits for the kernel to elect a scheduler
+!vote
	: not electedScheduler(S)
	<-	wait;
		!vote.

+!vote
	: electedScheduler(S) & player(ID) & S = ID
	<-	.print("Voting yes");
		voteYes.

+!vote
	: electedScheduler(S) & player(ID) & not ( S = ID )
	<-	.print("Voting yes");
		voteYes.
	
+!vote
	: true
	<- .print("HOW DID I GET HERE?!?!?!").

+!waitForVote
	: not voteComplete
	<-	wait;
		!waitForVote.
	
+!waitForVote
	: voteComplete
	<-	//do something with the information about votes
		.print("Done waiting for votes!").
		
+!waitForScheduler
	: not cardPlayed
	<-	wait;
		!waitForScheduler.
	
+!waitForScheduler
	: cardPlayed
	<-	//do something with the information about votes
		.print("Done waiting for Scheduler!").
	   
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
	<- discardAntiVirus.

+!discardCard
	: isKernel & isVirus & heldVirus(V) & V > 0
	<- discardVirus.

		
//If Scheduler 
//If has 1 of each type
+!discardCard
	: isScheduler & isVirus & heldVirus(V) & heldAntiVirus(A) & V = A
	<- discardVirus;
	   !playCard.

//If only has AntiVirus cards
+!discardCard
	: isScheduler & isVirus & heldVirus(V) & V = 0
	<- discardAntiVirus;
	   !playCard.
	   
//If only has Virus cards
+!discardCard
	: isScheduler & isVirus & heldVirus(V) & V = 2
	<- discardVirus;
	   !playCard.
		

	
// ----------------------------------
// Logic to play when player is ROGUE
// ----------------------------------
+!discardCard
	: isKernel & isRogue & heldAntiVirus(A) & A = 0
	<- discardVirus.
	
+!discardCard
	: isKernel & isRogue  & heldAntiVirus(A) & A > 0
	<- discardAntiVirus.
	
//If has 1 all
+!discardCard
	: isScheduler & isRogue & heldVirus(V) & heldAntiVirus(A) & V = A
	<- discardAntiVirus;
	   !playCard.

//If only has 2 AntiVirus cards
+!discardCard
	: isScheduler & isRogue  & heldAntiVirus(A) & A = 2
	<- discardAntiVirus;
	   !playCard.
	   
//If only has Virus cards
+!discardCard
	: isScheduler & isRogue  & heldVirus(V) & V = 2
	<- discardVirus;
	   !playCard.
		

		
// ----------------------------------
// Logic to play when player is ANTIVIRUS
// ----------------------------------

+!discardCard
	: isKernel & isAntiVirus & heldVirus(V) & V > 0
	<- discardVirus.
	   
+!discardCard
	: isKernel & isAntiVirus & heldAntiVirus(A) & A = 3
	<- discardAntiVirus.
	
//If has 1 of each
+!discardCard
	: isScheduler & isAntiVirus & heldVirus(V) & heldAntiVirus(A) & V = A
	<- discardVirus;
	   !playCard.

//If only has Virus cards
+!discardCard
	: isScheduler & isAntiVirus & heldVirus(V) & V = 2
	<- discardVirus;
	   !playCard.
	   
//If only has AntiVirus cards
+!discardCard
	: isScheduler & isAntiVirus & heldAntiVirus(A) & A = 2
	<- discardAntiVirus;
	   !playCard.
