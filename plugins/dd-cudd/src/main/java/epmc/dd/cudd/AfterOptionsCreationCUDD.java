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

package epmc.dd.cudd;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeInteger;
import epmc.options.OptionTypeIntegerNonNegativeOrDefault;
import epmc.options.OptionTypeLong;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationCUDD implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-cudd";
    private final static String CUDD_REORDER_SAME = "cudd-reorder-same";
    private final static String CUDD_REORDER_NONE = "cudd-reorder-none";
    private final static String CUDD_REORDER_RANDOM = "cudd-reorder-random";
    private final static String CUDD_REORDER_RANDOM_PIVOT = "cudd-reorder-random-pivot";
    private final static String CUDD_REORDER_SIFT = "cudd-reorder-sift";
    private final static String CUDD_REORDER_SIFT_CONVERGE = "cudd-reorder-sift-converge";
    private final static String CUDD_REORDER_SYMM_SIFT = "cudd-reorder-symm-sift";
    private final static String CUDD_REORDER_SYMM_SIFT_CONV = "cudd-reorder-symm-sift-conv";
    private final static String CUDD_REORDER_WINDOW2 = "cudd-reorder-window2";
    private final static String CUDD_REORDER_WINDOW3 = "cudd-reorder-window3";
    private final static String CUDD_REORDER_WINDOW4 = "cudd-reorder-window4";
    private final static String CUDD_REORDER_WINDOW2_CONV = "cudd-reorder-window2-conv";
    private final static String CUDD_REORDER_WINDOW3_CONV = "cudd-reorder-window3-conv";
    private final static String CUDD_REORDER_WINDOW4_CONV = "cudd-reorder-window4-conv";
    private final static String CUDD_REORDER_GROUP_SIFT = "cudd-reorder-group-sift";
    private final static String CUDD_REORDER_GROUP_SIFT_CONV = "cudd-reorder-group-sift-conv";
    private final static String CUDD_REORDER_ANNEALING = "cudd-reorder-annealing";
    private final static String CUDD_REORDER_GENETIC = "cudd-reorder-genetic";
    private final static String CUDD_REORDER_LINEAR = "cudd-reorder-linear";
    private final static String CUDD_REORDER_LINEAR_CONVERGE = "cudd-reorder-linear-converge";
    private final static String CUDD_REORDER_LAZY_SIFT = "cudd-reorder-lazy-sift";
    private final static String CUDD_REORDER_EXACT = "cudd-reorder-exact";
    private final static Map<String,Integer> REORDER_MAP;
    static {
        Map<String,Integer> reorderMap = new LinkedHashMap<>();
        reorderMap.put(CUDD_REORDER_SAME, LibraryDDCUDD.CUDD_REORDER_SAME);
        reorderMap.put(CUDD_REORDER_NONE, LibraryDDCUDD.CUDD_REORDER_NONE);
        reorderMap.put(CUDD_REORDER_RANDOM, LibraryDDCUDD.CUDD_REORDER_RANDOM);
        reorderMap.put(CUDD_REORDER_RANDOM_PIVOT, LibraryDDCUDD.CUDD_REORDER_RANDOM_PIVOT);
        reorderMap.put(CUDD_REORDER_SIFT, LibraryDDCUDD.CUDD_REORDER_SIFT);
        reorderMap.put(CUDD_REORDER_SIFT_CONVERGE, LibraryDDCUDD.CUDD_REORDER_SIFT_CONVERGE);
        reorderMap.put(CUDD_REORDER_SYMM_SIFT, LibraryDDCUDD.CUDD_REORDER_SYMM_SIFT);
        reorderMap.put(CUDD_REORDER_SYMM_SIFT_CONV, LibraryDDCUDD.CUDD_REORDER_SYMM_SIFT_CONV);
        reorderMap.put(CUDD_REORDER_WINDOW2, LibraryDDCUDD.CUDD_REORDER_WINDOW2);
        reorderMap.put(CUDD_REORDER_WINDOW3, LibraryDDCUDD.CUDD_REORDER_WINDOW3);
        reorderMap.put(CUDD_REORDER_WINDOW4, LibraryDDCUDD.CUDD_REORDER_WINDOW4);
        reorderMap.put(CUDD_REORDER_WINDOW2_CONV, LibraryDDCUDD.CUDD_REORDER_WINDOW2_CONV);
        reorderMap.put(CUDD_REORDER_WINDOW3_CONV, LibraryDDCUDD.CUDD_REORDER_WINDOW3_CONV);
        reorderMap.put(CUDD_REORDER_WINDOW4_CONV, LibraryDDCUDD.CUDD_REORDER_WINDOW4_CONV);
        reorderMap.put(CUDD_REORDER_GROUP_SIFT, LibraryDDCUDD.CUDD_REORDER_GROUP_SIFT);
        reorderMap.put(CUDD_REORDER_GROUP_SIFT_CONV, LibraryDDCUDD.CUDD_REORDER_GROUP_SIFT_CONV);
        reorderMap.put(CUDD_REORDER_ANNEALING, LibraryDDCUDD.CUDD_REORDER_ANNEALING);
        reorderMap.put(CUDD_REORDER_GENETIC, LibraryDDCUDD.CUDD_REORDER_GENETIC);
        reorderMap.put(CUDD_REORDER_LINEAR, LibraryDDCUDD.CUDD_REORDER_LINEAR);
        reorderMap.put(CUDD_REORDER_LINEAR_CONVERGE, LibraryDDCUDD.CUDD_REORDER_LINEAR_CONVERGE);
        reorderMap.put(CUDD_REORDER_LAZY_SIFT, LibraryDDCUDD.CUDD_REORDER_LAZY_SIFT);
        reorderMap.put(CUDD_REORDER_EXACT, LibraryDDCUDD.CUDD_REORDER_EXACT);
        REORDER_MAP = Collections.unmodifiableMap(reorderMap);
    }
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
                .setIdentifier(OptionsDDCUDD.DD_CUDD_CATEGORY)
                .setParent(OptionsDD.DD_CATEGORY)
                .build();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        ddLibraryClasses.put(LibraryDDCUDD.IDENTIFIER, LibraryDDCUDD.class);
        OptionTypeEnum typeCUDDSubengine = new OptionTypeEnum(OptionsTypesCUDD.CUDDSubengine.class);
        options.addOption().setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
        .setIdentifier(OptionsDDCUDD.DD_CUDD_SUBENGINE)
        .setType(typeCUDDSubengine).setDefault(OptionsTypesCUDD.CUDDSubengine.MTBDD)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        options.addOption().setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
        .setIdentifier(OptionsDDCUDD.DD_CUDD_INIT_CACHE_SIZE)
        .setType(typeInteger).setDefault(LibraryDDCUDD.CUDD_CACHE_SLOTS)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        OptionTypeLong typeLong = OptionTypeLong.getTypeLong();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        OptionTypeIntegerNonNegativeOrDefault typeIntegerNonNegativeOrDefault = OptionTypeIntegerNonNegativeOrDefault.getTypeIntegerNonNegativeOrDefault();
        options.addOption().setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
        .setIdentifier(OptionsDDCUDD.DD_CUDD_MAX_MEMORY)
        .setType(typeLong).setDefault("34359738368")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
        .setIdentifier(OptionsDDCUDD.DD_CUDD_UNIQUE_SLOTS)
        .setType(typeInteger).setDefault(LibraryDDCUDD.CUDD_UNIQUE_SLOTS)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
        .setIdentifier(OptionsDDCUDD.DD_CUDD_MAX_CACHE_HARD)
        .setType(typeIntegerNonNegativeOrDefault)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
        .setIdentifier(OptionsDDCUDD.DD_CUDD_MIN_HIT)
        .setType(typeIntegerNonNegativeOrDefault)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
        .setIdentifier(OptionsDDCUDD.DD_CUDD_GARBAGE_COLLECT)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
        .setIdentifier(OptionsDDCUDD.DD_CUDD_LOOSE_UP_TO)
        .setType(typeIntegerNonNegativeOrDefault)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        OptionTypeMap<Integer> typeReorderMap = new OptionTypeMap<>(REORDER_MAP);
        options.addOption().setBundleName(OptionsDDCUDD.OPTIONS_DD_CUDD)
        .setIdentifier(OptionsDDCUDD.DD_CUDD_REORDER_HEURISTIC)
        .setType(typeReorderMap)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
    }
}
