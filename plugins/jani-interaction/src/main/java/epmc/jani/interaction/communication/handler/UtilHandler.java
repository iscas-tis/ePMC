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

package epmc.jani.interaction.communication.handler;

import java.util.Map;

import epmc.jani.interaction.communication.Backend;

public final class UtilHandler {
    public static void addIntegratedHandlers(Backend backend, Map<String, Handler> handlers) {
        assert backend != null;
        assert handlers != null;
        handlers.put(HandlerAuthenticate.TYPE, new HandlerAuthenticate(backend));
        handlers.put(HandlerClose.TYPE, new HandlerClose(backend));
        handlers.put(HandlerQueryAnalysisEngines.TYPE, new HandlerQueryAnalysisEngines(backend));
        handlers.put(HandlerStartAnalysis.TYPE, new HandlerStartAnalysis(backend));
        handlers.put(HandlerStopAnalysis.TYPE, new HandlerStopAnalysis(backend));
        handlers.put(HandlerUpdateServerParameters.TYPE, new HandlerUpdateServerParameters(backend));
    }

    private UtilHandler() {
    }
}
