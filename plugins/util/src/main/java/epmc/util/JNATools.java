/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.util;

import java.util.HashMap;
import java.util.Map;

import javax.management.RuntimeErrorException;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

/**
 * Several static auxiliary methods for Java Native access (JNA).
 * Most functions just call standard c functions. In case wrappers for these
 * functions will be implemented in future versions of JNA, these functions
 * should be removed from this class and calls to them replaced by according
 * functions of JNA. However, currently this seems not to be the case, and they
 * seem also not to be present in the Platform extension. In addition, this
 * class also contains some functionality not found as standard c functions.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JNATools {
    /**
     * Class to which the native utility library is mapped.
     * 
     * @author Ernst Moritz Hahn
     */
    private final static class Utils {
        /** part of filename of native utility library */
        private final static String UTIL_LIBRARY_NAME = "utils";

        /** c function fgetc */
        static native int fgetc(Pointer stream);

        /** c function tmpfile */
        static native Pointer tmpfile();

        /** c function fclose */
        static native int fclose(Pointer stream);

        /** c function rewind */
        static native void rewind (Pointer stream);

        /** c function ftell */
        static native NativeLong ftell(Pointer stream);

        /** c function fseek */
        static native int fseek(Pointer stream, NativeLong offset, int whence);

        /** c function free */
        static native void free(Pointer ptr);

        /** utility function to obtain pointer to stdout */
        static native Pointer get_stdout();

        /** utility function to obtain pointer to stderr */
        static native Pointer get_stderr();

        /** utility function to obtain pointer to stdin */
        static native Pointer get_stdin();

        /** utility function to obtain value of EOF */
        static native int get_eof();

        /** utility function to obtain value of SEEK_SET */
        static native int get_seek_set();

        /** utility function to obtain value of SEEK_CUR */
        static native int get_seek_cur();

        /** utility function to obtain value of SEEK_END */
        static native int get_seek_end();

        /** utility function to read FILE* to char* */
        static native Pointer read_stream_to_string(Pointer stream);

        /** indicator whether utility library was loaded successfully */
        private final static boolean loaded =
                JNATools.registerLibrary(Utils.class, UTIL_LIBRARY_NAME);
    }

    /** string by which JNA indicates that a library could not be loaded */
    private final static String CANNOT_LOAD = "Unable to load library";

    /**
     * Register a native library to an given class using JNA.
     * In contrast to the JNA methods, this method will return {@code true} if
     * the library was loaded successfully but will return {@code false} if the
     * library was not found for the running operating system. In case the
     * library could not be loaded for other reasons, a runtime error will be
     * thrown. None of the arguments to this function may be {@code null}.
     * 
     * @param handler class to which to map the library to.
     * @param libraryName base name of the native library
     * @return {@code true} if loading the library was successful
     */
    public static boolean registerLibrary(Class<?> handler, String libraryName) {
        assert handler != null;
        assert libraryName != null;
        boolean ok = true;
        try {
            Map<String,Object> options = new HashMap<>();
            /* The following lines fix a problem occurring otherwise in Linux.
             * Library.OPTION_OPEN_FLAGS must *not* be set on Windows, though,
             * because it breaks things there.
             */
            if (Platform.isLinux()) {
                options.put(Library.OPTION_OPEN_FLAGS, 2);
            }
            // following line is needed for native files from plugins
            options.put(Library.OPTION_CLASSLOADER, handler.getClassLoader());
            NativeLibrary library = NativeLibrary.getInstance(libraryName, options);
            Native.register(handler, library);
        } catch (UnsatisfiedLinkError e) {
            // throw EPMCException if library not found to allow to show a
            // nice message, but rethrow unchecked exception otherwise
            boolean failedLoad = e.getMessage().substring(0,
                    CANNOT_LOAD.length()).equals(CANNOT_LOAD);
            if (!failedLoad) {
                throw new RuntimeErrorException(e);
            }
            ok = false;
        }
        return ok;
    }

    /**
     * Obtain stdout FILE* stream.
     * 
     * @return stdout FILE* stream
     */
    public static Pointer getStdout() {
        return Utils.get_stdout();
    }

    /**
     * Obtain stderr FILE* stream.
     * 
     * @return stderr FILE* stream
     */
    public static Pointer getStderr() {
        return Utils.get_stderr();
    }

    /**
     * Obtain stdin FILE* stream.
     * 
     * @return stdin FILE* stream
     */
    public static Pointer getStdin() {
        return Utils.get_stdin();
    }

    /**
     * Call c function {@code tmpfile}.
     * @see <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/tmpfile.html">http://pubs.opengroup.org/onlinepubs/009695399/functions/tmpfile.html</a>
     * 
     * @return return value of c call
     */
    public static Pointer tmpfile() {
        return Utils.tmpfile();
    }

    /**
     * Call c function {@code fclose}.
     * @see <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/fclose.html">http://pubs.opengroup.org/onlinepubs/009695399/functions/fclose.html</a>
     * 
     * @param stream
     * @return return value of c call
     */
    public static int fclose(Pointer stream) {
        assert stream != null;
        return Utils.fclose(stream);
    }

    /**
     * Call c function {@code rewind}.
     * @see <a href="http://pubs.opengroup.org/onlinepubs/9699919799/functions/rewind.html">http://pubs.opengroup.org/onlinepubs/9699919799/functions/rewind.html</a>
     * 
     * @param stream parameter of c call
     */
    public static void rewind(Pointer stream) {
        assert stream != null;
        Utils.rewind(stream);
    }

    /**
     * Call c function {@code fgetc}.
     * @see <a href="http://pubs.opengroup.org/onlinepubs/9699919799/functions/fgetc.html">http://pubs.opengroup.org/onlinepubs/9699919799/functions/fgetc.html</a>
     * 
     * @param stream parameter of c call
     * @return return value of c call
     */
    public static int fgetc(Pointer stream) {
        assert stream != null;
        return Utils.fgetc(stream);
    }

    /**
     * Call c function {@code ftell}.
     * @see <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/ftell.html">http://pubs.opengroup.org/onlinepubs/009695399/functions/ftell.html</a>
     * 
     * @param stream parameter of c call
     * @return return value of c call
     */
    public static NativeLong ftell(Pointer stream) {
        assert stream != null;
        return Utils.ftell(stream);
    }

    /**
     * Call c function {@code fseek}.
     * @see <a href="http://pubs.opengroup.org/onlinepubs/9699919799/functions/fseek.html">http://pubs.opengroup.org/onlinepubs/9699919799/functions/fseek.html</a>
     * 
     * @param stream parameter of c call
     * @param offset parameter of c call
     * @param whence parameter of c call
     * @return return value of c call
     */
    public static int fseek(Pointer stream, NativeLong offset, int whence) {
        assert stream != null;
        assert offset != null;
        return Utils.fseek(stream, offset, whence);
    }

    /**
     * Call c function {@code free}.
     * @see <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/free.html">http://pubs.opengroup.org/onlinepubs/009695399/functions/free.html</a>
     * 
     * @param pointer parameter of c call
     */
    public static void free(Pointer pointer) {
        Utils.free(pointer);
    }

    /**
     * Obtain integer constant EOF.
     * 
     * @return integer constant EOF
     */
    public static int get_eof() {
        return Utils.get_eof();
    }

    /**
     * Obtain integer constant SEEK_SET
     * 
     * @return integer constant SEEK_SET
     */
    public static int get_seek_set() {
        return Utils.get_seek_set();
    }

    /**
     * Obtain integer constant SEEK_CUR
     * 
     * @return integer constant SEEK_CUR
     */
    public static int get_seek_cur() {
        return Utils.get_seek_cur();
    }

    /**
     * Obtain integer constant SEEK_END
     * 
     * @return integer constant SEEK_END
     */
    public static int get_seek_end() {
        return Utils.get_seek_end();
    }

    /**
     * Read complete FILE* to byte array.
     * The parameter must not be {@code null}.
     * 
     * @param stream FILE* to read from
     * @return byte array read from FILE*
     */
    public static byte[] readStreamToByteArray(Pointer stream) {
        assert stream != null;
        NativeLong lastPosition = ftell(stream);
        if (Utils.fseek(stream, new NativeLong(0), get_seek_end()) != 0) {
            return null;
        }
        long size = ftell(stream).longValue();
        Utils.rewind(stream);
        if (size >= Integer.MAX_VALUE) {
            return null;
        }
        byte[] result = new byte[(int) size];
        int eof = get_eof();
        int ch = fgetc(stream);
        int pos = 0;
        /* not the most efficient way (many native calls), but suffices for
         * the current use cases of the function */
        while (ch != eof) {
            result[pos] = (byte) ch;
            pos++;
            ch = fgetc(stream);
        }
        fseek(stream, lastPosition, get_seek_set());
        return result;
    }

    /**
     * Read complete FILE* to {@link String}.
     * The parameter must not be {@code null}.
     * 
     * @param stream FILE* to read
     * @return string constructed from FILE*
     */
    public static String readStreamToString(Pointer stream) {
        assert stream != null;
        Pointer cString = Utils.read_stream_to_string(stream);
        String result = cString.getString(0);
        free(cString);
        return result;
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private JNATools() {
    }
}
