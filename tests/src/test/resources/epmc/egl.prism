// Randomised protocol for signing contracts taken from:
// S. Even, O. Goldreich, and A. Lempel.
// A randomized protocol for signing contracts.
// Communications of the ACM, 28(6):637 647, 1985.

//-----------------------------------------------------------------------------------------

dtmc // Model is a DTMC (no nondeterminism)

// CONSTANTS:

// N: number of pairs of secrets each party sends
const int N;
// L: number of bits in each secret (fix at 2)
const int L=2;

// FORMULAE:

// B knows a pair of secrets
formula kB = ( (a1_0=L  & a2_0=L)
             | (a1_1=L  & a2_1=L)
             | (a1_2=L  & a2_2=L)
             | (a1_3=L  & a2_3=L)
             | (a1_4=L  & a2_4=L)
             | (a1_5=L  & a2_5=L)
             | (a1_6=L  & a2_6=L)
             | (a1_7=L  & a2_7=L)
             | (a1_8=L  & a2_8=L)
             | (a1_9=L  & a2_9=L));
// A knows a pair of secrets
formula kA = ( (b1_0=L  & b2_0=L)
             | (b1_1=L  & b2_1=L)
             | (b1_2=L  & b2_2=L)
             | (b1_3=L  & b2_3=L)
             | (b1_4=L  & b2_4=L)
             | (b1_5=L  & b2_5=L)
             | (b1_6=L  & b2_6=L)
             | (b1_7=L  & b2_7=L)
             | (b1_8=L  & b2_8=L)
             | (b1_9=L  & b2_9=L));

//-----------------------------------------------------------------------------------------

// Scheduler: used to order the sending of messages between parties A and B

module scheduler
	
	// b: which bit of the secret a party should send next
	b : [1..L];
	// n: which secret a party should send next
	n : [0..max(N-1,1)];
	// phase: current phase of the protocol
	//        1   = sending messages of the form OT(.,.,.,.)
	//        2&3 = sending secrets 1..N and N+1..2N, respectively
	//        4   = finished
	phase : [1..5];
	// party: which party moves next
	party : [1..2];
	
	// FIRST PHASE:
	
	// A sends a message, B will go next
	[receiveB] phase=1 & party=1 -> (party'=2);
	// B sends a message, move onto next message and go back to A
	[receiveA] phase=1 & party=2 & n<N-1 -> (party'=1) & (n'=n+1);
	// B sends final (Nth) message, move to next phase
	[receiveA] phase=1 & party=2 & n=N-1 -> (party'=1) & (phase'=2) & (n'=0); 
	
	// SECOND AND THIRD PHASES (interleaved for A and B):
	
	// A sends bth bit of nth secret (for  n=1..N-1), move to next secret
	[receiveB] phase=2 & party=1 & n<N-1-> (n'=n+1);
	// A sends bth bit of Nth secret, move to next phase (N+1..2N)
	[receiveB] phase=2 & party=1 & n=N-1 -> (phase'=3) & (n'=0); 
	// A sends bth bit of (N+n)th secret (for  n=1..N-1), move to next secret
	[receiveB] phase=3 & party=1 & n<N-1-> (n'=n+1);
	// A sends bth bit of last (2Nth) secret, B will go next
	[receiveB] phase=3 & party=1 & n=N-1 -> (phase'=2) & (party'=2) & (n'=0);
	
	// B sends bth bit of nth secret (for  n=1..N-1), move to next secret
	[receiveA] phase=2 & party=2 & n<N-1 -> (n'=n+1);
	// B sends bth bit of Nth secret, move to next phase (N+1..2N)
	[receiveA] phase=2 & party=2 & n=N-1 -> (phase'=3) & (n'=0);
	// B sends bth bit of (N+n)th secret (for  n=1..N-1), move to next secret
	[receiveA] phase=3 & party=2 & n<N-1 -> (n'=n+1);
	// B sends bth bit of last (2Nth) secret, increment b and go back to A
	[receiveA] phase=3 & party=2 & n=N-1 & b<L -> (phase'=2) & (party'=1) & (n'=0) & (b'=b+1);
	// B sends final (Lth) bit of last (2Nth) secret, protocol is now finished
	[receiveA] phase=3 & party=2 & n=N-1 & b=L -> (phase'=4);
	
	// FINISHED (loop)
	[] phase=4 -> (phase'=4);
	
endmodule

//-----------------------------------------------------------------------------------------

// Party A

module partyA
	
	// How many bits of each of B's secrets A currently knows
	// Secrets are in pairs and thus divided into two sets.
	// b1_i stores the value (number of bits known) for the ith secret
	// of the first set of secrets. b2_i stores the value
	// for the ith secret of the second set of secrets.
	// (Technical note: Keep pairs of secrets together
	//  to give a more structured model and hence smaller MTBDD)
	b1_0 : [0..L]; b2_0 : [0..L];
	b1_1 : [0..L]; b2_1 : [0..L];
	b1_2 : [0..L]; b2_2 : [0..L];
	b1_3 : [0..L]; b2_3 : [0..L];
	b1_4 : [0..L]; b2_4 : [0..L];
	b1_5 : [0..L]; b2_5 : [0..L];
	b1_6 : [0..L]; b2_6 : [0..L];
	b1_7 : [0..L]; b2_7 : [0..L];
	b1_8 : [0..L]; b2_8 : [0..L];
	b1_9 : [0..L]; b2_9 : [0..L];
	
	// A receives either secret n-1 or N+(n-1) with equal probability
	// (using Oblivious Transfer (OT) protocol)
	// Note: get full secret here, i.e. all L bits
	[receiveA] phase=1 & n=0  -> 0.5 : (b1_0'=L) + 0.5 : (b2_0'=L);
	[receiveA] phase=1 & n=1  -> 0.5 : (b1_1'=L) + 0.5 : (b2_1'=L);
	[receiveA] phase=1 & n=2  -> 0.5 : (b1_2'=L) + 0.5 : (b2_2'=L);
	[receiveA] phase=1 & n=3  -> 0.5 : (b1_3'=L) + 0.5 : (b2_3'=L);
	[receiveA] phase=1 & n=4  -> 0.5 : (b1_4'=L) + 0.5 : (b2_4'=L);
	[receiveA] phase=1 & n=5  -> 0.5 : (b1_5'=L) + 0.5 : (b2_5'=L);
	[receiveA] phase=1 & n=6  -> 0.5 : (b1_6'=L) + 0.5 : (b2_6'=L);
	[receiveA] phase=1 & n=7  -> 0.5 : (b1_7'=L) + 0.5 : (b2_7'=L);
	[receiveA] phase=1 & n=8  -> 0.5 : (b1_8'=L) + 0.5 : (b2_8'=L);
	[receiveA] phase=1 & n=9  -> 0.5 : (b1_9'=L) + 0.5 : (b2_9'=L);
	// A receives single bit for secrets 0..N-1 (when scheduler module in phase 2)
	[receiveA] phase=2 & n=0  -> (b1_0'=min(b1_0+1,L));
	[receiveA] phase=2 & n=1  -> (b1_1'=min(b1_1+1,L));
	[receiveA] phase=2 & n=2  -> (b1_2'=min(b1_2+1,L));
	[receiveA] phase=2 & n=3  -> (b1_3'=min(b1_3+1,L));
	[receiveA] phase=2 & n=4  -> (b1_4'=min(b1_4+1,L));
	[receiveA] phase=2 & n=5  -> (b1_5'=min(b1_5+1,L));
	[receiveA] phase=2 & n=6  -> (b1_6'=min(b1_6+1,L));
	[receiveA] phase=2 & n=7  -> (b1_7'=min(b1_7+1,L));
	[receiveA] phase=2 & n=8  -> (b1_8'=min(b1_8+1,L));
	[receiveA] phase=2 & n=9  -> (b1_9'=min(b1_9+1,L));
	// A receives single bits for secrets N..2N-1 (when scheduler module in phase 3)
	[receiveA] phase=3 & n=0  -> (b2_0'=min(b2_0+1,L));
	[receiveA] phase=3 & n=1  -> (b2_1'=min(b2_1+1,L));
	[receiveA] phase=3 & n=2  -> (b2_2'=min(b2_2+1,L));
	[receiveA] phase=3 & n=3  -> (b2_3'=min(b2_3+1,L));
	[receiveA] phase=3 & n=4  -> (b2_4'=min(b2_4+1,L));
	[receiveA] phase=3 & n=5  -> (b2_5'=min(b2_5+1,L));
	[receiveA] phase=3 & n=6  -> (b2_6'=min(b2_6+1,L));
	[receiveA] phase=3 & n=7  -> (b2_7'=min(b2_7+1,L));
	[receiveA] phase=3 & n=8  -> (b2_8'=min(b2_8+1,L));
	[receiveA] phase=3 & n=9  -> (b2_9'=min(b2_9+1,L));

endmodule

//-----------------------------------------------------------------------------------------

// Construct module for party B through renaming

module partyB=partyA[b1_0=a1_0 ,b1_1=a1_1 ,b1_2=a1_2 ,b1_3=a1_3 ,b1_4=a1_4 ,b1_5=a1_5 ,b1_6=a1_6 ,b1_7=a1_7 ,b1_8=a1_8 ,b1_9=a1_9,
                     b2_0=a2_0 ,b2_1=a2_1 ,b2_2=a2_2 ,b2_3=a2_3 ,b2_4=a2_4 ,b2_5=a2_5 ,b2_6=a2_6 ,b2_7=a2_7 ,b2_8=a2_8 ,b2_9=a2_9,
                     receiveA=receiveB] 
endmodule
