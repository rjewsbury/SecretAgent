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

//if you're the scheduler, you should be passed 2 cards.
+!play
	: isScheduler & voteComplete & ( heldVirus(V) | heldAntiVirus(A))
	<-	!discardCard;	//pick a card to discard
		!playCard;		//play the one remaining card
		!play.

//if you havn't been handed any cards yet, wait
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
		.print("Electing Player ",X);
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

//decides how to discard a card.
//if there's no choice to be made, automatically decides
+!discardCard
	: heldVirus(V) & heldAntiVirus(A) & V = 0 & A > 0
	<- discardAntiVirus.
	
+!discardCard
	: heldVirus(V) & heldAntiVirus(A) & V > 0 & A = 0
	<- discardVirus.
	
//if there is a choice to be made, defer to the belief base
//discardDecision == 0 means discard a virus
//discardDecision == 1 means discard an antivirus
+!discardCard
	: heldVirus(V) & heldAntiVirus(A) & V > 0 & A > 0
	<-	?discardDecision(X);
		!discardCard(X).

+!discardCard(X)
	: X = 0
	<-	discardVirus.

+!discardCard(X)
	: X = 1
	<-	discardAntiVirus.

//only the scheduler can play cards, and there should be no decision to make
+!playCard
	: isScheduler & heldAntiVirus(A) & A > 0
	<- playAntiVirus.	

+!playCard
	: isScheduler & heldVirus(V) & V > 0
	<- playVirus.
