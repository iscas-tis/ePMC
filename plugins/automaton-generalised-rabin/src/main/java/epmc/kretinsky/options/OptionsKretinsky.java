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

package epmc.kretinsky.options;

public final class OptionsKretinsky {
    public final static String KRETINSKY_OPTIONS = "KretinskyOptions";
    public final static String KRETINSKY_DISABLE_UNUSED_SLAVES = "kretinsky-disable-unused-slaves";
    public final static String KRETINSKY_GFFG_OPTIMISATION = "kretinsky-gffg-optimisation";
    public final static String KRETINSKY_OPTIMISE_MOJMIR = "kretinsky-optimise-mojmir";
    public final static String KRETINSKY_PREPROCESS_SLAVES = "kretinsky-preprocess-slaves";
    public static final String KRETINSKY_LTLFILT_CMD = "kretinsky-ltlfilt-cmd";

    private OptionsKretinsky() {
    }
}
