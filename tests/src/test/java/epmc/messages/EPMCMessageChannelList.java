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

package epmc.messages;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import epmc.messages.Message;
import epmc.messages.MessageInstance;

public final class EPMCMessageChannelList implements EPMCMessageChannel {
    public static class LogEntry {
        private final long time;
        private final Message key;
        private final List<String> arguments = new ArrayList<>();
        private final List<String> publicArguments
        = Collections.unmodifiableList(arguments);

        LogEntry(long time, Message key, String... arguments) {
            assert time >= 0;
            assert key != null;
            assert arguments != null;
            for (String argument : arguments) {
                assert argument != null;
            }
            this.time = time;
            this.key = key;
            this.arguments.addAll(Arrays.asList(arguments));
        }

        LogEntry(long time, Message key, List<String> arguments) {
            assert time >= 0;
            assert key != null;
            assert arguments != null;
            for (String argument : arguments) {
                assert argument != null;
            }
            this.time = time;
            this.key = key;
            this.arguments.addAll(arguments);
        }

        public long getTime() {
            return time;
        }

        public Message getKey() {
            return key;
        }

        public List<String> getArguments() {
            return publicArguments;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(time);
            builder.append(" ");
            builder.append(key);
            builder.append(" ");
            int argNr = 0;
            for (String string : arguments) {
                builder.append(string);
                if (argNr < arguments.size() - 1) {
                    builder.append(" ");
                }
            }
            return builder.toString();
        }
    }

    boolean started;
    private final List<LogEntry> list = new ArrayList<>();
    private final List<LogEntry> publicList = Collections.unmodifiableList(list);

    public EPMCMessageChannelList() throws RemoteException {
        super();
    }

    public List<LogEntry> getList() {
        return publicList;
    }

    public void clear() {
        list.clear();
    }

    @Override
    public void setTimeStarted(long time) {
        assert !started;
        started = true;
    }

    @Override
    public void send(MessageInstance instance) {
        list.add(new LogEntry(instance.getTime(), instance.getMessage(), instance.getParameters()));
    }
}
