#include <stdio.h>
#include <stdlib.h>
#include <limits.h>

__attribute__ ((visibility("default")))
FILE *get_stdout() {
    return stdout;
}

__attribute__ ((visibility("default")))
FILE *get_stderr() {
    return stderr;
}

__attribute__ ((visibility("default")))
FILE *get_stdin() {
    return stdout;
}

__attribute__ ((visibility("default")))
int get_eof() {
    return EOF;
}

__attribute__ ((visibility("default")))
int get_seek_set() {
    return SEEK_SET;
}

__attribute__ ((visibility("default")))
int get_seek_cur() {
    return SEEK_CUR;
}

__attribute__ ((visibility("default")))
int get_seek_end() {
    return SEEK_END;
}

__attribute__ ((visibility("default")))
char *read_stream_to_string(FILE *stream) {
    long lastPosition = ftell(stream);
    if (fseek(stream, 0, SEEK_END) != 0) {
        return NULL;
    }
    long size = ftell(stream);
    rewind(stream);
    if (size >= INT_MAX) {
        return NULL;
    }
    char *result = malloc(sizeof(char) * (size + 1));
    int ch = fgetc(stream);
    int pos = 0;
    while (ch != EOF) {
        result[pos] = (char) ch;
        pos++;
        ch = fgetc(stream);
    }
    result[size] = (char) 0;
    fseek(stream, lastPosition, SEEK_SET);
    return result;
}
