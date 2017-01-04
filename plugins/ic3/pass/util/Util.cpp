/******************************** CPPFile *****************************

* FileName [Util.cpp]

* PackageName [util]

* Synopsis [The file containing various utility routines.]

* SeeAlso []

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2007 by Saarland University. All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/


#include "Util.h"
#include "Database.h"
#include <stdarg.h>

bool approxEquality(double a, double b, double precision) {
	return a == b || ( precision * fabs(a + b)  > fabs(b - a) );
}

namespace util {

/*********************************************************************/
//prints a message and exits.
/*********************************************************************/
__attribute__ ((noreturn)) void Error(const char *format,...)
{
  va_list argp;
  va_start(argp,format);
  vprintf(format,argp);
  va_end(argp);
  exit(-1);
}

__attribute__ ((noreturn)) void Error(std::string message)
{
  printf("%s",message.c_str());
  exit(-1);
}

/*********************************************************************/
//prints a message. the first argument is the verbosity level of the
//message.
/*********************************************************************/
void Message(const char *format,...)
{
    va_list argp;
    va_start(argp,format);
    vprintf(format,argp);
    va_end(argp);
    fflush(stdout);
}

void Message(std::string message)
{
  Message(message.c_str());
}

/*********************************************************************/
//trim a std::string - remove intial and trailing whitespace
/*********************************************************************/
std::string TrimString(const std::string &s)
{
  std::string::size_type a = s.find_first_not_of(" ");
  std::string::size_type b = s.find_last_of(" ");
  if((a == std::string::npos) && (b == std::string::npos)) return s;
  else if(a == std::string::npos) return s.substr(0,b);
  else if(b == std::string::npos) return s.substr(a,s.length() - a);
  else return s.substr(a, b - a);
}

/*********************************************************************/
//generate new temporary variable
/*********************************************************************/
std::string NewTempVar()
{
  static unsigned tempVarCounter = 0;
  ++tempVarCounter;
  char x[50];
  snprintf(x,50,"temp_var_%d",tempVarCounter);
  return x;
}

/*********************************************************************/
//check if the argument std::string is a temporary variable
/*********************************************************************/
bool IsTempVar(const std::string &arg)
{
  const char *ptr = arg.c_str();
  return (strstr(ptr,"temp_var_") == ptr);
}


static char buffer[33];

/*********************************************************************/
//convert an int a std::string
/*********************************************************************/
std::string intToString(int i) {
  snprintf(buffer,33,"%d",i);
  return std::string(buffer);
}

/*********************************************************************/
//convert a std::string to an int
/*********************************************************************/
int stringToInt(const std::string& s) {
	const char* c = s.c_str();
  	return atoi(c);
}


/*********************************************************************/
//convert a string to an int
/*********************************************************************/
double stringToDouble(const std::string& s) {
	const char* c = s.c_str();
  	return atof(c);
}


/*********************************************************************/
//convert a float a string
/*********************************************************************/
std::string floatToString(float f) {
  snprintf(buffer,33,"%G",f);
  return buffer;
}

/*********************************************************************/
//convert a float a string
/*********************************************************************/
std::string doubleToString(double d) {
  snprintf(buffer,33,"%G",d);
  return buffer;
}

/*********************************************************************/
// remove file extension from file name
/*********************************************************************/
std::string RemoveExtension(std::string s) {
  if (s.size() == 0) {
    return "";
  }

  size_t pos = s.find_last_of('.');
  if (pos != std::string::npos) {
    return std::string(s.begin(), s.begin() + pos);
  } else {
    return s;
  }
}

} //end of util

/*********************************************************************/
//end of Util.cpp
/*********************************************************************/
