//Ending the game
virusWin :- (virusPlayed(V) & V = 6) | virusElected.
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
		
		
+!play
	: isKernel & voteComplete & scheduler(S)
		& not askedIdentity & virusPlayed(V) & V >= 3
	<-	.broadcast(achieve, revealIdentity);
		addMessage('R U HITLER');
		+askedIdentity;
		wait;
		!play.
		
//if the vote passed, draw 3 cards
+!play
	: isKernel & voteComplete & scheduler(S)
		& (askedIdentity | (virusPlayed(V) & V < 3))
	<-	.print("Vote passed. drawing three cards");
		-askedIdentity;
		deleteMessage;
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
	   
+!revealIdentity
	: isScheduler & isVirus
	<-  addMessage('YE DOG');
		.broadcast(tell, virusElected);
		wait.
	
+!revealIdentity
	: isScheduler & not isVirus & player(X)
	<-  addMessage('NO U');
		.broadcast(tell, notVirus(X));
		wait;
		deleteMessage.
	
+!revealIdentity
	: not isScheduler
	<- wait.
	
		
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

//Decides how to vote based on beliefs about S and K
+!vote
	: electedScheduler(S)
	<-	?voteDecision(X);
		!submitVote(X).
		
+!submitVote(Y)
	: Y = 0
	<- voteNo.
	
+!submitVote(Y)
	: Y = 1
	<- voteYes.
	
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
