/*
 * Copyright (C) 2011-2012 Keyle
 *
 * This file is part of MyPet
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyPet. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.util;


import de.Keyle.MyPet.skill.MyPetSkillTree;
import de.Keyle.MyPet.util.configuration.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class MyPetSkillTreeConfigLoader
{
    private static Map<String, MyPetSkillTree> SkillTrees = new HashMap<String, MyPetSkillTree>();
    private static Map<String, String> Inheritances = new HashMap<String, String>();
    private static List<String> lSkillTrees = new ArrayList<String>();

    private static YamlConfiguration MWConfig = null;

    public static void setConfig(YamlConfiguration newMWConfig)
    {
        MWConfig = newMWConfig;
    }

    public static void loadSkillTrees()
    {
        if (MWConfig == null)
        {
            return;
        }
        SkillTrees.clear();
        Inheritances.clear();
        ConfigurationSection sec = MWConfig.getConfig().getConfigurationSection("skilltrees");
        if (sec == null)
        {
            return;
        }
        Set<String> SkillTree = sec.getKeys(false);
        if (SkillTree.size() > 0)
        {
            for (String ST : SkillTree)
            {
                String inherit = MWConfig.getConfig().getString("skilltrees." + ST + ".inherit", "%#_DeFaUlT_#%");
                if (!inherit.equals("%#_DeFaUlT_#%"))
                {
                    Inheritances.put(ST, inherit);
                }
                Set<String> Level = MWConfig.getConfig().getConfigurationSection("skilltrees." + ST).getKeys(false);
                if (Level.size() > 0)
                {
                    MyPetSkillTree MWST = new MyPetSkillTree(ST);
                    for (String Lv : Level)
                    {
                        if (MyPetUtil.isInt(Lv))
                        {
                            MWST.addLevel(Integer.parseInt(Lv), MWConfig.getConfig().getStringList("skilltrees." + ST + "." + Lv));
                        }
                    }
                    SkillTrees.put(ST, MWST);
                    lSkillTrees.add(ST);
                }
                else if (Level.size() == 0)
                {
                    MyPetSkillTree MWST = new MyPetSkillTree(ST);
                    SkillTrees.put(ST, MWST);
                    lSkillTrees.add(ST);
                }
            }
        }
    }

    public static MyPetSkillTree getSkillTree(String Name)
    {
        if (SkillTrees.containsKey(Name))
        {

            MyPetSkillTree MWST = new MyPetSkillTree(SkillTrees.get(Name).getName());

            if (SkillTrees.get(Name).getLevels() != null)
            {
                for (int level : SkillTrees.get(Name).getLevels())
                {
                    MWST.addSkillToLevel(level, SkillTrees.get(Name).getSkills(level));
                }
            }

            if (Inheritances.containsKey(Name))
            {
                String NextInheritance = Inheritances.get(Name);
                while (!NextInheritance.isEmpty())
                {
                    if (SkillTrees.containsKey(Name))
                    {
                        MyPetSkillTree nextMWST = SkillTrees.get(NextInheritance);
                        if (nextMWST.getLevels() != null)
                        {
                            for (int level : nextMWST.getLevels())
                            {
                                MWST.addSkillToLevel(level, nextMWST.getSkills(level));
                            }
                            if (Inheritances.containsKey(NextInheritance))
                            {
                                NextInheritance = getInheritance(NextInheritance);
                            }
                            else
                            {
                                NextInheritance = "";
                            }
                        }
                    }
                }
            }
            return MWST;
        }
        return null;
    }

    public static String getInheritance(String Name)
    {
        if (Inheritances.containsKey(Name))
        {
            return Inheritances.get(Name);
        }
        return null;
    }

    public static String[] getSkillTreeNames()
    {
        if (lSkillTrees.size() > 0)
        {
            String[] TN = new String[lSkillTrees.size()];
            for (int i = 0 ; i < lSkillTrees.size() ; i++)
            {
                TN[i] = lSkillTrees.get(i);
            }
            return TN;
        }
        return new String[0];
    }

    public static boolean existSkillTree(String Name)
    {
        return SkillTrees.containsKey(Name);
    }
}