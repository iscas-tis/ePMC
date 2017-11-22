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

package epmc.dd.sylvan;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.options.Category;
import epmc.options.OptionTypeInteger;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationSylvan implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-sylvan";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
                .setIdentifier(OptionsDDSylvan.DD_SYLVAN_CATEGORY)
                .setParent(OptionsDD.DD_CATEGORY)
                .build();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDSylvan.IDENTIFIER, LibraryDDSylvan.class);
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
        .setIdentifier(OptionsDDSylvan.DD_SYLVAN_WORKERS)
        .setType(typeInteger).setDefault("1")
        .setCommandLine().setGui()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
        .setIdentifier(OptionsDDSylvan.DD_SYLVAN_INIT_CACHE_SIZE)
        .setType(typeInteger).setDefault("262144")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
        .setIdentifier(OptionsDDSylvan.DD_SYLVAN_INIT_NODES)
        .setType(typeInteger).setDefault("1000000")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDSylvan.OPTIONS_DD_SYLVAN)
        .setIdentifier(OptionsDDSylvan.DD_SYLVAN_CACHE_GRANULARITY)
        .setType(typeInteger).setDefault("4")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
    }
}
