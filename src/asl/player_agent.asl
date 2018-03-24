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

//initially assume everyone is a scheduler candidate
schedulerCandidate(0).
schedulerCandidate(1).
schedulerCandidate(2).
schedulerCandidate(3).
schedulerCandidate(4).
schedulerCandidate(5).

!identity.

// DERIVED BELIEFS
//when someone is elected Kernel, they're no longer a scheduler candidate
+kernel(K) : true <- -schedulerCandidate(K).
//when someone is ex Kernel, they're no longer a scheduler candidate
+exKernel(K) : true <- -schedulerCandidate(K).
//when someone is ex Scheduler, they're no longer a scheduler candidate
+exScheduler(S) : true <- -schedulerCandidate(S).

//when agents lose these roles, they become eligible again
-kernel(K) : true <- +schedulerCandidate(K).
-exKernel(K) : true <- +schedulerCandidate(K).
-exScheduler(S) : true <- +schedulerCandidate(S).

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
		!drawThree;
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
		voteNo.
	
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
	
+!drawCards
	: isKernel
	<- drawThree;
	   !discardCard.
	   
+!passKernel
	: isKernel
	<- passKernel.
	   
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
	   !passKernel;
	   !play.

+!discardCard
	: isKernel & isVirus & heldVirus(V) & V > 0
	<- discardVirus;
	   passCards;
	   !passKernel;
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
	   !passKernel;
	   !play.
	
+!discardCard
	: isKernel & isRogue  & heldAntiVirus(A) & A > 1
	<- discardAntiVirus;
	   passCards;
	   !passKernel;
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
	   !passKernel;
	   !play.
	   
+!discardCard
	: isKernel & heldAntiVirus(A) & A = 3
	<- discardAntiVirus;
	   passCards;
	   !passKernel;
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
