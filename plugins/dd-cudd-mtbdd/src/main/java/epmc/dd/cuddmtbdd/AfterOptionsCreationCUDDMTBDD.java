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

package epmc.dd.cuddmtbdd;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeInteger;
import epmc.options.OptionTypeLong;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationCUDDMTBDD implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-cuddmtbdd";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsDDCUDDMTBDD.OPTIONS_DD_CUDD_MTBDD)
                .setIdentifier(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_CATEGORY)
                .setParent(OptionsDD.DD_CATEGORY)
                .build();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_MT_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDCUDDMTBDD.IDENTIFIER, LibraryDDCUDDMTBDD.class);
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDCUDDMTBDD.OPTIONS_DD_CUDD_MTBDD)
        .setIdentifier(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_INIT_CACHE_SIZE)
        .setType(typeInteger).setDefault("0")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        OptionTypeLong typeLong = OptionTypeLong.getTypeLong();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsDDCUDDMTBDD.OPTIONS_DD_CUDD_MTBDD)
        .setIdentifier(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_MAX_MEMORY)
        .setType(typeLong).setDefault("0")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDDMTBDD.OPTIONS_DD_CUDD_MTBDD)
        .setIdentifier(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_UNIQUE_SLOTS)
        .setType(typeInteger).setDefault("256")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDDMTBDD.OPTIONS_DD_CUDD_MTBDD)
        .setIdentifier(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_MAX_CACHE_HARD)
        .setType(typeInteger).setDefault("0")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDDMTBDD.OPTIONS_DD_CUDD_MTBDD)
        .setIdentifier(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_MIN_HIT)
        .setType(typeInteger).setDefault("30")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDDMTBDD.OPTIONS_DD_CUDD_MTBDD)
        .setIdentifier(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_GARBAGE_COLLECT)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDDMTBDD.OPTIONS_DD_CUDD_MTBDD)
        .setIdentifier(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_LOOSE_UP_TO)
        .setType(typeInteger).setDefault("0")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
    }
}
