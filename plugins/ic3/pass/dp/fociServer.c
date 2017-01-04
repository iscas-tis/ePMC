/*
BLAST is a tool for software model checking.
This file is part of BLAST.

Copyright (c) 2002-2007, The BLAST Team.
All rights reserved. 

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of the authors nor their organizations 
   may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHORS ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

(This is the Modified BSD License, see also
 http://www.opensource.org/licenses/bsd-license.php)

The BLAST Team consists of
        Dirk Beyer (SFU), Thomas A. Henzinger (EPFL),
        Ranjit Jhala (UCSD), and Rupak Majumdar (UCLA).

BLAST web page:
        http://mtc.epfl.ch/blast/

Bug reports:
        Dirk Beyer:      firstname.lastname@sfu.ca or
        Rupak Majumdar:  firstname@cs.ucla.edu or
        Ranjit Jhala:    lastname@cs.ucla.edu
 */

#include <sys/types.h> 
#include <stdio.h>    
#include <unistd.h>   
#include <sys/wait.h> 
#include <stdlib.h>   
#include <sys/stat.h> 
#include <pthread.h>
#include <semaphore.h>
#include <errno.h>
#include <signal.h>
#include <fcntl.h>
#include <string.h>

//NOTE
//the internal structure needs some factorisation.
//a lot of code is copied between the (non)symmetric parts
char* pipes[4];

int create_named_pipes(){
	//creates unique filenames
	pid_t pid = getpid();
	char buffer[32];
	sprintf(buffer, "foci_in_%d",(int)pid);
	pipes[0] = malloc(strlen(buffer)*sizeof(char));
	strncpy(pipes[0], buffer, 32);
	sprintf(buffer, "foci_out_%d",(int)pid);
	pipes[1] = malloc(strlen(buffer)*sizeof(char));
	strncpy(pipes[1], buffer, 32);
	sprintf(buffer, "foci_sym_in_%d",(int)pid);
	pipes[2] = malloc(strlen(buffer)*sizeof(char));
	strncpy(pipes[2], buffer, 32);
	sprintf(buffer, "foci_sym_out_%d",(int)pid);
	pipes[3] = malloc(strlen(buffer)*sizeof(char));
	strncpy(pipes[3], buffer, 32);
	int err = 0;
	int i;
	for( i = 0; i < 4; ++i ){
		//fprintf(stderr, "creating named pipe: %s\n",pipes[i]);
		int e = mkfifo(pipes[i], 0600);
		err |= e;
		if( e != 0 ){
			perror("creating pipes");
		}
	}
	return err;
}

sem_t producer;
sem_t consumer;
sem_t producer_sym;
sem_t consumer_sym;
sem_t fd_ready;
sem_t fd_ready_sym;

pid_t foci_child = 0;
pid_t foci_child_sym = 0;

int counter = 0;//count the number of process created

int foci_pipe[2];
int foci_pipe_sym[2];

int foci_err_fd;
int foci_err_fd_sym;


pthread_t fkiller;
pthread_t fkiller_sym;
pthread_t fmaker;
pthread_t fmaker_sym;

int init_semaphores(){
	int err = 0;
	int e = sem_init( &producer, 0, 0);
	err |= e;
	if( e != 0 ){
		perror("creating the semaphores");
	}
	e = sem_init( &consumer, 0, 0);
	err |= e;
	if( e != 0 ){
		perror("creating the semaphores");
	}
	e = sem_init( &producer_sym, 0, 0);
	err |= e;
	if( e != 0 ){
		perror("creating the semaphores");
	}
	e = sem_init( &consumer_sym, 0, 0);
	err |= e;
	if( e != 0 ){
		perror("creating the semaphores");
	}
	e = sem_init( &fd_ready, 0, 0);
	err |= e;
	if( e != 0 ){
		perror("creating the semaphores");
	}
	e = sem_init( &fd_ready_sym, 0, 0);
	err |= e;
	if( e != 0 ){
		perror("creating the semaphores");
	}
	return err;
}

void clean_files(){
	int i;
	for(i = 0; i < 4; ++i){
		remove(pipes[i]);
	}
}

void destroy_semaphores(){
	sem_destroy(&producer);
	sem_destroy(&producer_sym);
	sem_destroy(&consumer);
	sem_destroy(&consumer_sym);
	sem_destroy(&fd_ready);
	sem_destroy(&fd_ready_sym);
}

/*do not takes care of error, just clean*/
void terminate(){
	destroy_semaphores();
	clean_files();
	//kill the child processes
	if( foci_child > 0 ){
		kill(foci_child, SIGKILL);
	}
	if( foci_child_sym > 0 ){
		kill(foci_child_sym, SIGKILL);
	}
	printf("fociServer stopped (%d queries)\n", counter-2);
}

void echo(char* s){
	FILE* f = fopen(s, "w");
	if( f == NULL ){
		perror("unable to open the file (echo)");
	}
	fclose(f);
}

void* killer(void* ptr){
	while( 1 ) {
		sem_wait( &fd_ready );
		int fd =  foci_err_fd;
		char line[] = "Satisfiable\0";//avoid dynamic memory
		int error = 0;
		if( read(fd, line, 11*sizeof(char)) > 0){
			error = 1;
			//able to read => foci error
			close(fd);
			if(kill(foci_child, SIGKILL) != 0){
				perror("unable to kill foci");
			}
			printf("%s\n",line);
			fflush(stdout);
			//as opening a name pipe can be blocking,
			//this thread should stop until the main thread
			//opens the otherside of the pipe.
			echo(pipes[1]); //make the main thread continue...
		}else{
			//not able to read (fine)
			errno = 0;
		}
		if( error == 0){
			close(fd);
		}
	}
	pthread_exit(0);
}

void* killer_sym(void* ptr){
	while( 1 ) {
		sem_wait( &fd_ready_sym );
		int fd =  foci_err_fd_sym;
		char line[] = "Satisfiable\0";//avoid dynamic memory
		int error = 0;
		if( read(fd, line, 11*sizeof(char)) > 0){
			error = 1;
			//able to read => foci error
			close(fd);
			if(kill(foci_child_sym, SIGKILL) != 0){
				perror("unable to kill foci (symmetric)");
			}
			printf("%s\n",line);
			fflush(stdout);
			//as opening a name pipe can be blocking,
			//this thread should stop until the main thread
			//opens the otherside of the pipe.
			echo(pipes[3]); //make the main thread continue...
		}else{
			//not able to read (fine)
			errno = 0;
		}
		if( error == 0){
			close(fd);
		}
	}
	pthread_exit(0);
}


void* create(void* p){
	while( 1 ){
		++counter;
		if(pipe(foci_pipe) != 0){
			perror("creating the pipe for child-parent communication");
			terminate();
			exit(-1);
		}
		//fork + multithreading => danger
		//but at these point, this thread should be the only one that is not blocked
		//and the only existing thread in the child should be this one.
		foci_child = fork();
		if ( foci_child == 0 ){
			//child part
			dup2(foci_pipe[1], STDERR_FILENO);//redirect err to foci_pipe[1]
			if(close(foci_pipe[1]) != 0){
				perror("closing foci_pipe");
			}
			int fnull = open("/dev/null", O_WRONLY);
			if (fnull == -1){
				perror("opening '/dev/null'");
				terminate();
				exit(-1);
			}
			dup2(fnull, STDOUT_FILENO);//redirect stdout to /dev/null
			if(close(fnull) != 0){
				perror("closing '/dev/null'");
			}
			execlp("foci.opt", "foci.opt", pipes[0], pipes[1], (char*)NULL);
			perror("error during execlp 'foci.opt'");
			terminate();
			exit(666);
		}else if (foci_child == -1){
			//error part
			printf("error creating the %dth process\n", counter);
			terminate();
			exit(0);
		}else{
			//parent part
			foci_err_fd = foci_pipe[0];
			sem_post( &fd_ready );
			if(close(foci_pipe[1]) != 0){//close anyway, only used by the child
				perror("line: __LINE__");
			}
			sem_post(&producer);
			//fprintf(stderr,"foci n° %d created\n", counter);
			sem_wait(&consumer);
			if(waitpid(foci_child, NULL, 0) == -1){
				perror("waitpid failed");
			}
		}
	}
}

void* create_sym(void* p){
	while( 1 ){
		++counter;
		if(pipe(foci_pipe_sym) != 0){
			perror("creating the pipe for child-parent communtcation (symmetric)");
			terminate();
			exit(-1);
		}
		//fork + multithreading => danger
		//but at these point, this thread should be the only one that is not blocked
		//and the only existing thread in the child should be this one.
		foci_child_sym = fork();
		if ( foci_child_sym == 0 ){
			//child part
			dup2(foci_pipe_sym[1], STDERR_FILENO);//redirect err to foci_pipe_sym[1]
			if(close(foci_pipe_sym[1]) != 0){
				perror("closing foci_pipe_sym");
			}
			int fnull = open("/dev/null", O_WRONLY);
			if (fnull == -1){
				perror("opening '/dev/null'");
				terminate();
				exit(-1);
			}
			dup2(fnull, STDOUT_FILENO);//redirect stdout to /dev/null
			if(close(fnull) != 0){
				perror("closing '/dev/null'");
			}
			execlp("foci.opt", "foci.opt", "-s", pipes[2], pipes[3], (char*)NULL);
			perror("error during execlp 'foci.opt -s'");
			terminate();
			exit(666);
		}else if (foci_child_sym == -1){
			//error part
			printf("error creating the %dth process (symmetric)\n", counter);
			terminate();
			exit(0);
		}else{
			//parent part
			foci_err_fd_sym = foci_pipe_sym[0];
			sem_post( &fd_ready_sym );
			if(close(foci_pipe_sym[1]) != 0){//close anyway, only used by the child
				perror("line: __LINE__");
			}
			sem_post(&producer_sym);
			//fprintf(stderr,"foci n° %d created\n", counter);
			sem_wait(&consumer_sym);
			if(waitpid(foci_child_sym, NULL, 0) == -1){
				perror("waitpid failed (symmetric)");
			}
		}
	}
}

/** read from in and put in out
 *  stops when reading EOF or '\n'
 */
int forward_query(FILE* in, FILE* out){
	char c = (char) fgetc(in);
	while(c != EOF && c != '\n'){
		if(putc(c, out) == EOF){
			perror("forwarding query to foci");
		}
		c = (char) fgetc(in);
	}
	//put a line return
	putc('\n', out);
	return 0;
}

/** read from in and put in out
 *  stops when reading EOF
 */
int forward_answer(FILE* in, FILE* out){
	char c = (char) fgetc(in);
	while(c != EOF){
		if(putc(c, out) == EOF){
			perror("forwarding answer from foci");
		}
		c = (char) fgetc(in);
	}
	//put a line return (not needed)
	putc('^', out);
	return 0;
}

int read_from_stdin(){
	int again = 1;
	while(again){
		char const type = fgetc(stdin);
		if (type == 'n'){
			sem_wait(&producer);//wait foci to be created
			FILE* out = fopen(pipes[0], "w");
			if(out == NULL){
				perror("unable to open the pipe to send foci query");
				return -1;
			}
			forward_query(stdin, out);
			fflush(out);
			if(fclose(out) != 0){
				perror("closing pipe to foci input file");
			}
			//perror("trying to open foci_out");
			FILE* in = fopen(pipes[1], "r");
			if(in == NULL){
				perror("unable to open the pipe to read foci answer");
				return -1;
			}
			forward_answer(in, stdout);
			fflush(stdout);
			if(fclose(in) != 0){
				perror("closing pipe form foci output file");
			}
			sem_post(&consumer);
		}else if (type == 's'){
			sem_wait(&producer_sym);//wait foci to be created
			FILE* out = fopen(pipes[2], "w");
			if(out == NULL){
				perror("unable to open the pipe to send foci query (symmetric)");
				return -1;
			}
			forward_query(stdin, out);
			fflush(out);
			if(fclose(out) != 0){
				perror("closing pipe to foci input file (symmetric)");
			}
			//perror("trying to open foci_sym_out");
			FILE* in = fopen(pipes[3], "r");
			if(in == NULL){
				perror("unable to open the pipe to read foci answer (symmetric)");
				return -1;
			}
			forward_answer(in, stdout);
			fflush(stdout);
			if(fclose(in) != 0){
				perror("closing pipe form foci output file");
			}
			sem_post(&consumer_sym);
		}else if(type == 'e') {
			return -1;
		}
		else 
		{
			//fprintf(stderr, "Unkown query");
			return -1;
		}
	}
	return 0;
}

void signal_handler(int sig){
	terminate();
	fprintf(stderr, "RECEIVED SIGNAL %d, exiting\n", sig);
	exit(0);
}

void setup_signal_hanlder(){
	signal(SIGQUIT, signal_handler);
	signal(SIGABRT, signal_handler);
	signal(SIGALRM, signal_handler);
	signal(SIGHUP, signal_handler);
	signal(SIGINT, signal_handler);
	signal(SIGTERM, signal_handler);
	signal(SIGSEGV, signal_handler);
}

int main(int argc, char* argv[]){
	printf("This program uses Foci, Copyright  2003 Cadence Berkeley Laboratories,\n") ;
	printf("Cadence Design Systems. All rights reserved.\n^");
	fflush(stdout);
	if(create_named_pipes() != 0){
		clean_files();
		return -1;
	}
	if(init_semaphores() != 0){
		clean_files();
		destroy_semaphores();
		return -1;
	}
	setup_signal_hanlder();
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	if(pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM) != 0){
		perror("unable to set thread scope");
	}
	if(pthread_create(&fkiller, &attr, &killer, NULL) != 0){
		fprintf( stderr, "failed to create killer thread. \n");
		terminate();
		exit(-1);
	}
	if(pthread_create(&fkiller_sym, &attr, &killer_sym, NULL) != 0){
		fprintf( stderr, "failed to create killer_sym thread. \n");
		terminate();
		exit(-1);
	}
	if(pthread_create(&fmaker, NULL, &create, NULL) != 0){
		fprintf( stderr, "failed to create maker thread. \n");
		terminate();
		exit(-1);
	}
	if(pthread_create(&fmaker_sym, NULL, &create_sym, NULL) != 0){
		fprintf( stderr, "failed to create maker_sym thread. \n");
		terminate();
		exit(-1);
	}
	read_from_stdin();
	terminate();
	return 0;
}
