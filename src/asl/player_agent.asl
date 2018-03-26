//Ending the game
virusWin :- (virusPlayed(V) & V = 6) | virusRevealed(X).
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
	: player(X)
	<-	.print("I am player ",X);
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
	<-  !waitForever.
	
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
	: not isKernel & not isScheduler & voteComplete & not cardPlayed & not scheduler(S)
	<-	wait;
		!play.
		
+!play
	: not isKernel & not isScheduler & voteComplete & not cardPlayed & scheduler(S)
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
	<-	.print("Asking identity");
		.broadcast(achieve, revealIdentity(S));
		addMessage('ARE YOU A VIRUS?');
		+askedIdentity;
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
		!discardCard;
		passCards;
		!waitForScheduler;
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
		wait.
	   
+!revealIdentity(X)
	: player(P) & P=X & isVirus
	<-  .print("Revealing Identity: VIRUS");
		addMessage('YEAAAA BOIIIIIIIIII');
		.broadcast(tell, virusRevealed(P));
		wait.
	
+!revealIdentity(X)
	: player(P) & P=X & not isVirus
	<-  .print("Revealing Identity: NOT VIRUS");
		addMessage('Im not the Virus!');
		.broadcast(tell, notVirus(P)).
	
+!revealIdentity(X)
	: player(P) & not(P=X)
	<- wait.
	
+!waitForResponse(S)
	: notVirus(S) | virusRevealed(V)
	<- wait.
	
+!waitForResponse(S)
	: true
	<- 	wait;
		!waitForResponse(S).
		
+!electScheduler
	: isKernel
	<-  ?schedulerCandidate(X);
		.print("Electing Player ",X);
		addMessage('Electing ',X);
		electScheduler(X).


//remove the beliefs from the previous round
+!vote
	: interpretedVote(P)
	<-	-interpretedVote(P);
		!vote.
		
+!vote
	: toldHeldAntiVirus(A) & toldHeldVirus(V)
	<-	?player(ID);
		.broadcast(untell, heldAntiVirus(ID, A));
		.broadcast(untell, heldVirus(ID, V));
		//we need to remember what numbers we told everybody
		//so we can un-tell them once the round ends
		-toldHeldAntiVirus(A);
		-toldHeldVirus(V);
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
		!interpretVotes;
		deleteMessage.
		
//if this agent has never seen that player vote before, create a trust belief
+!interpretVotes
	: vote(P, X) & not trust(P, Y)
	<-	+trust(P, 0);
		!interpretVotes.

//if this player voted the same thing as another player, trust them a bit more
+!interpretVotes
	: vote(P, Y) & trust(P, T) & not interpretedVote(P)
	<-	?interpretVote(P, D);
		-trust(P, T);
		+trust(P, D);
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
	<-	//do something with the information about the card played
		!interpretCardPlayed;
		wait.
		
//change the trust value based on what card was played
+!interpretCardPlayed
	: scheduler(S) & kernel(K) & trust(S, TS) & trust(K, TK)
	<-	-trust(S, TS);
		-trust(K, TK);
		?interpretCard(S, DS);
		+trust(S, DS);
		?interpretCard(K, DK);
		+trust(K, DK).

//if it's already been decided how to discard, then discard the card.
//keep the belief long enough to decide what to tell people,
//then we don't need it
+!discardCard
	: discard(D) & D = 'virus'
	<-	.print("discarding a virus");
		!broadcastHand;
		-discard(D);
		discardVirus.

+!discardCard
	: discard(D) & D = 'antivirus'
	<-	.print("discarding an antivirus");
		!broadcastHand;
		-discard(D);
		discardAntiVirus.	

//decides how to discard a card.
//if there's no choice to be made, automatically decides
+!discardCard
	: heldVirus(V) & heldAntiVirus(A) & V = 0 & A > 0
	<-	+discard('antivirus');	//discard an antivirus
		!discardCard.
	
+!discardCard
	: heldVirus(V) & heldAntiVirus(A) & V > 0 & A = 0
	<- 	+discard('virus');		//discard a virus
		!discardCard.
	
//if there is a choice to be made, defer to the belief base
//discardDecision == 0 means discard a virus
//discardDecision == 1 means discard an antivirus
+!discardCard
	: heldVirus(V) & heldAntiVirus(A) & V > 0 & A > 0
	<-	?discardDecision(X);
		+discard(X);
		!discardCard.

//agents have the option to lie to other agents when telling them what cards
//they got. to do this, the decision on which beliefs to send has to go through
//the custom belief base
+!broadcastHand
	: player(ID)
	<-	?handBroadcastDecision(A, V);
		.broadcast(tell, heldAntiVirus(ID, A));
		.broadcast(tell, heldVirus(ID, V));
		//we need to remember what numbers we told everybody
		//so we can un-tell them once the round ends
		+toldHeldAntiVirus(A);
		+toldHeldVirus(V);
		!addHandMessage(A, V).

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
		addMessage('Shooting ',X);
		deleteAgent(X)
		!askIdentity(X).
		
+!useAbility
	: true
	<- wait.
	
//these methods are just for the aesthetics of broadcasting their hand
+!addHandMessage(A, V)
	: player(ID) & kernel(K) & K=ID & V=0
	<-	addMessage('You wont believe this');
		wait;	//build anticipation
		addMessage('I got ', A, ' Anti!').
		
+!addHandMessage(A, V)
	: player(ID) & kernel(K) & K=ID & A=0
	<-	addMessage('You wont believe this');
		wait;	//build anticipation
		addMessage('I got ', V, ' Virus!').
		
+!addHandMessage(A, V)
	: player(ID) & kernel(K) & K=ID & A > 0 & V > 0
	<-	addMessage('I got ', A, ' A and ', V, ' V').
	
+!addHandMessage(A, V)
	: player(ID) & scheduler(S) & S=ID & A=0
	<-	addMessage('He gave me ',V,' Virus').
	
+!addHandMessage(A, V)
	: player(ID) & scheduler(S) & S=ID & V=0
	<-	addMessage('He gave me ',A,' Anti').
	
+!addHandMessage(A, V)
	: player(ID) & scheduler(S) & S=ID & A>0 & V>0
	<-	addMessage('He gave me ',V,' and ',A).
