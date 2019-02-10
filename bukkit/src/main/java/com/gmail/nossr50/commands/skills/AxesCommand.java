package com.gmail.nossr50.commands.skills;

import com.gmail.nossr50.core.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.core.datatypes.skills.SubSkillType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.skills.axes.Axes;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.TextComponentFactory;
import com.gmail.nossr50.core.data.UserManager;
import com.gmail.nossr50.util.skills.RankUtils;
import com.gmail.nossr50.util.skills.SkillActivationType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AxesCommand extends SkillCommand {
    private String critChance;
    private String critChanceLucky;
    private double axeMasteryDamage;
    private double impactDamage;
    private String skullSplitterLength;
    private String skullSplitterLengthEndurance;

    private boolean canSkullSplitter;
    private boolean canCritical;
    private boolean canAxeMastery;
    private boolean canImpact;
    private boolean canGreaterImpact;

    public AxesCommand() {
        super(PrimarySkillType.AXES);
    }

    @Override
    protected void dataCalculations(Player player, float skillValue) {
        // ARMOR IMPACT
        if (canImpact) {
            impactDamage = UserManager.getPlayer(player).getAxesManager().getImpactDurabilityDamage();
        }

        // AXE MASTERY
        if (canAxeMastery) {
            axeMasteryDamage = Axes.getAxeMasteryBonusDamage(player);
        }
        
        // CRITICAL HIT
        if (canCritical) {
            String[] criticalHitStrings = getAbilityDisplayValues(SkillActivationType.RANDOM_LINEAR_100_SCALE_WITH_CAP, player, SubSkillType.AXES_CRITICAL_STRIKES);
            critChance = criticalHitStrings[0];
            critChanceLucky = criticalHitStrings[1];
        }
        
        // SKULL SPLITTER
        if (canSkullSplitter) {
            String[] skullSplitterStrings = calculateLengthDisplayValues(player, skillValue);
            skullSplitterLength = skullSplitterStrings[0];
            skullSplitterLengthEndurance = skullSplitterStrings[1];
        }
    }

    @Override
    protected void permissionsCheck(Player player) {
        canSkullSplitter = Permissions.skullSplitter(player) && RankUtils.hasUnlockedSubskill(player, SubSkillType.AXES_SKULL_SPLITTER);
        canCritical = canUseSubskill(player, SubSkillType.AXES_CRITICAL_STRIKES);
        canAxeMastery = canUseSubskill(player, SubSkillType.AXES_AXE_MASTERY);
        canImpact = canUseSubskill(player, SubSkillType.AXES_ARMOR_IMPACT);
        canGreaterImpact = canUseSubskill(player, SubSkillType.AXES_GREATER_IMPACT);
    }

    @Override
    protected List<String> statsDisplay(Player player, float skillValue, boolean hasEndurance, boolean isLucky) {
        List<String> messages = new ArrayList<String>();

        if (canImpact) {
            messages.add(LocaleLoader.getString("Ability.Generic.Template", LocaleLoader.getString("Axes.Ability.Bonus.2"), LocaleLoader.getString("Axes.Ability.Bonus.3", impactDamage)));
        }
        
        if (canAxeMastery) {
            messages.add(LocaleLoader.getString("Ability.Generic.Template", LocaleLoader.getString("Axes.Ability.Bonus.0"), LocaleLoader.getString("Axes.Ability.Bonus.1", axeMasteryDamage)));
        }

        if (canCritical) {
            messages.add(getStatMessage(SubSkillType.AXES_CRITICAL_STRIKES, critChance)
                    + (isLucky ? LocaleLoader.getString("Perks.Lucky.Bonus", critChanceLucky) : ""));
        }
        
        if (canGreaterImpact) {
            messages.add(LocaleLoader.getString("Ability.Generic.Template", LocaleLoader.getString("Axes.Ability.Bonus.4"), LocaleLoader.getString("Axes.Ability.Bonus.5", Axes.greaterImpactBonusDamage)));
        }

        if (canSkullSplitter) {
            messages.add(getStatMessage(SubSkillType.AXES_SKULL_SPLITTER, skullSplitterLength)
                    + (hasEndurance ? LocaleLoader.getString("Perks.ActivationTime.Bonus", skullSplitterLengthEndurance) : ""));
        }

        return messages;
    }

    @Override
    protected List<TextComponent> getTextComponents(Player player) {
        List<TextComponent> textComponents = new ArrayList<>();

        TextComponentFactory.getSubSkillTextComponents(player, textComponents, PrimarySkillType.AXES);

        return textComponents;
    }
}
