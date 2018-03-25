//Ending the game
virusWin :- (virusPlayed(V) & V = 6) | (scheduler(S) & virusRevealed(X) & S=X).
antiVirusWin :- (antiVirusPlayed(A) & A = 5) | (virusRevealed(V) & dead(X) & V=X).

//Player Elected Position
isKernel :- kernel(K) & player(N) & N = K.
isScheduler :- scheduler(S) & player(N) & N = S.

//Player Identities
isVirus :- role(R) & R = 0.
isRogue :- role(R) & R = 1.
isAntiVirus :- role(R) & R = 2.

//Dead
isDead :- player(X) & dead(Y) & X = Y.

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
	   
+!waitForever
	:true
	<-	wait;
		!waitForever.
	   
//Win conditions
//with a time stepped environment, every agent needs to pick an action
//for the game to continue. this allows everybody else to catch up
+!play
	: virusWin
	<-	.print("Virus team wins!");
		!waitForever.
	
+!play
	: antiVirusWin
	<-  .print("AntiVirus team wins!");
		!waitForever.

+!play
	: isDead
	<-  wait;
		!play.
	
//if no scheduler has been elected, elect a scheduler
+!play
	: isKernel & not voteComplete & not electedScheduler(S)
	<- 	!electScheduler;
		!play.

+!play
	: not voteComplete
	<-	!vote;
		!waitForVote;
		!play.

+!play
	: not isKernel & not isScheduler & voteComplete & not cardPlayed
	<-	!waitForScheduler;
		//make beliefs based on what card was played
		!play.

//wait for the round to end
+!play
	: not isKernel & not isScheduler & voteComplete & cardPlayed
	<-	wait;
		!play.
		
+!play
	: isKernel & voteComplete & scheduler(S)
		& not askedIdentity & virusPlayed(V) & V >= 3
	<-	!askIdentity(S);
		!play.
		
+!askIdentity(S)
	: true
	<-	.broadcast(achieve, revealIdentity(S));
		addMessage('ARE YOU A VIRUS?');
		.print("Asking identity");
		+askedIdentity;
		!waitForResponse(S).
		
+!waitForResponse(S)
	: notVirus(S) | virusElected
	<- wait.
	
+!waitForResponse(S)
	: true
	<- 	wait;
		!waitForResponse(S).
		
//if the vote passed, draw 3 cards
+!play
	: isKernel & voteComplete & scheduler(S)
		& (askedIdentity | (virusPlayed(V) & V < 3))
	<-	.print("Vote passed. drawing three cards");
			//once we've started playing the round, we dont need to remember
			//that we asked for an identity
		-askedIdentity;
		deleteMessage;
		drawThree;
		.print("Discarding a card");
		!discardCard;
		.print("Passing cards");
		passCards;
		.print("Waiting for scheduler");
		!waitForScheduler;
		.print("Using ability");
		!useAbility;
		.print("Passing kernel");
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

+!play
	: true
	<- .print("I DONT KNOW HOW TO PLAY").
		
-!play
	:true
	<-	.print("FAILED TO PLAY");
		wait;
		!play.
	   
+!revealIdentity(X)
	: player(P) & P=X & isVirus
	<-  .print("Revealing Identity: VIRUS");
		addMessage('YE DOG');
		.broadcast(tell, virusRevealed(P));
		wait.
	
+!revealIdentity(X)
	: player(P) & P=X & not isVirus
	<-  .print("Revealing Identity: NOT VIRUS");
		addMessage('NO U');
		.broadcast(tell, notVirus(P)).
	
+!revealIdentity(X)
	: player(P) & not(P=X)
	<- wait.
	
		
+!electScheduler
	: isKernel
	<-  ?schedulerCandidate(X);
		.print("Electing Player ",X);
		addMessage('Electing ',X);
		electScheduler(X).

//if this agent still believes he's interpreted votes from last round
//remove the beliefs
+!vote
	: interpretedVote(P)
	<-	-interpretedVote(P);
		!vote.

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
	<-	addMessage('NEIN!');
		.print("Voting No");
		voteNo.
	
+!submitVote(Y)
	: Y = 1
	<-	addMessage('JA!');
		.print("Voting Yes");
		voteYes.
	
+!vote
	: true
	<- .print("I DONT KNOW HOW TO VOTE").

+!waitForVote
	: not voteComplete
	<-	wait;
		!waitForVote.
	
+!waitForVote
	: voteComplete
	<-	//do something with the information about votes
		!interpretVotes
		deleteMessage.
		
//if this agent has never seen that player vote before, create a trust belief
+!interpretVotes
	: vote(P, X) & not trust(P, Y)
	<-	+trust(P, 0);
		!interpretVotes.

//if this player voted the same thing as another player, trust them a bit more
+!interpretVotes
	: player(ID) & vote(ID, X)
		& vote(P, Y) & trust(P, T) & not interpretedVote(P)
		& X = Y
	<-	-trust(P, T);
		+trust(P, T+1);
		+interpretedVote(P);
		!interpretVotes.

+!interpretVotes
	: player(ID) & vote(ID, X)
		& vote(P, Y) & trust(P, T) & not interpretedVote(P)
		& not(X = Y)
	<-	-trust(P, T);
		+trust(P, T-1);
		+interpretedVote(P);
		!interpretVotes.

+!interpretVotes
	: not ( vote(P, Y) & not interpretedVote(P) )
	<-	wait.

//wait for the card to be played
+!waitForScheduler
	: not cardPlayed
	<-	wait;
		!waitForScheduler.

//once a card is played, update trust
+!waitForScheduler
	: cardPlayed
	<-	//do something with the information about the card playes
		!interpretCardPlayed;
		wait.
		
//change the trust value based on what card was played
+!interpretCardPlayed
	: scheduler(S) & trust(S, T) & virusPlayed
	<-	-trust(S, T);
		+trust(S, T-5).
		
+!interpretCardPlayed
	: scheduler(S) & trust(S, T) & antiVirusPlayed
	<-	-trust(S, T);
		+trust(S, T+5).

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
	<-	.print("Playing Antivirus");
		playAntiVirus.	

+!playCard
	: isScheduler & heldVirus(V) & V > 0
	<- 	.print("Playing Virus");
		playVirus.
	
+!useAbility
	: hasBullet
	<-	?deleteDecision(X);
		.print("Using Bullet on ", X);
		deleteAgent(X)
		!askIdentity(X).
		
+!useAbility
	: true
	<- wait.
