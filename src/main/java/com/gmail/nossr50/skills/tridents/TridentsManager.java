package com.gmail.nossr50.skills.tridents;

import com.gmail.nossr50.config.AdvancedConfig;
import com.gmail.nossr50.datatypes.interactions.NotificationType;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.datatypes.skills.ToolType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.runnables.skills.BleedTimerTask;
import com.gmail.nossr50.skills.SkillManager;
import com.gmail.nossr50.skills.archery.Archery;
import com.gmail.nossr50.util.EventUtils;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.NotificationManager;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.random.RandomChanceUtil;
import com.gmail.nossr50.util.skills.RankUtils;
import com.gmail.nossr50.util.skills.SkillActivationType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TridentsManager extends SkillManager  {

    public TridentsManager(McMMOPlayer mcMMOPlayer) {
        super(mcMMOPlayer, PrimarySkillType.TRIDENTS);
    }


    /* Boolean checks */
    public boolean canActivateAbility() {
        return mcMMOPlayer.getToolPreparationMode(ToolType.TRIDENT) && Permissions.berserk(getPlayer());
    }

    public boolean canDaze(LivingEntity target) {
        if(!RankUtils.hasUnlockedSubskill(getPlayer(), SubSkillType.TRIDENTS_BLIND))
            return false;

        return target instanceof Player && Permissions.isSubSkillEnabled(getPlayer(), SubSkillType.TRIDENTS_BLIND);
    }

    public boolean canDeflect() {
        if(!RankUtils.hasUnlockedSubskill(getPlayer(), SubSkillType.TRIDENTS_ARROW_DEFLECT))
            return false;

        Player player = getPlayer();

        return ItemUtils.isUnarmed(player.getInventory().getItemInMainHand()) && Permissions.isSubSkillEnabled(getPlayer(), SubSkillType.UNARMED_ARROW_DEFLECT);
    }

    public boolean canDisarm(LivingEntity target) {
        if(!RankUtils.hasUnlockedSubskill(getPlayer(), SubSkillType.TRIDENTS_DISARM))
            return false;

        return target instanceof Player && ((Player) target).getInventory().getItemInMainHand().getType() != Material.AIR && Permissions.isSubSkillEnabled(getPlayer(), SubSkillType.UNARMED_DISARM);
    }

    public boolean canUseRupture() {
        return Permissions.isSubSkillEnabled(getPlayer(), SubSkillType.TRIDENTS_RUPTURE) && RankUtils.hasUnlockedSubskill(getPlayer(), SubSkillType.TRIDENTS_RUPTURE);
    }

    /**
     * Handle the effects of the Daze ability
     *
     * @param defender The {@link Player} being affected by the ability
     */
    public double daze(Player defender) {
        if (!RandomChanceUtil.isActivationSuccessful(SkillActivationType.RANDOM_LINEAR_100_SCALE_WITH_CAP, SubSkillType.TRIDENTS_BLIND, getPlayer())) {
            return 0;
        }

        Location dazedLocation = defender.getLocation();
        dazedLocation.setPitch(90 - Misc.getRandom().nextInt(181));

        defender.teleport(dazedLocation);
        defender.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 10, 10));


        if (NotificationManager.doesPlayerUseNotifications(defender)) {
            NotificationManager.sendPlayerInformation(defender, NotificationType.SUBSKILL_MESSAGE, "Combat.Blinded");
        }

        if (mcMMOPlayer.useChatNotifications()) {
            NotificationManager.sendPlayerInformation(getPlayer(), NotificationType.SUBSKILL_MESSAGE, "Combat.TargetBlinded");
        }

        return Tridents.blindBonusDamage;
    }

    /**
     * Check for disarm.
     *
     * @param defender The defending player
     */
    public void disarmCheck(Player defender) {
        if (RandomChanceUtil.isActivationSuccessful(SkillActivationType.RANDOM_LINEAR_100_SCALE_WITH_CAP, SubSkillType.TRIDENTS_DISARM, getPlayer()) && !hasIronGrip(defender)) {
            if (EventUtils.callDisarmEvent(defender).isCancelled()) {
                return;
            }

            if(UserManager.getPlayer(defender) == null)
                return;

            Item item = Misc.dropItem(defender.getLocation(), defender.getInventory().getItemInMainHand());

            if (item != null && AdvancedConfig.getInstance().getDisarmProtected()) {
                item.setMetadata(mcMMO.disarmedItemKey, UserManager.getPlayer(defender).getPlayerMetadata());
            }

            defender.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            NotificationManager.sendPlayerInformation(defender, NotificationType.SUBSKILL_MESSAGE, "Skills.Disarmed");
        }
    }

    /**
     * Check for arrow deflection.
     */
    public boolean deflectCheck() {
        if (RandomChanceUtil.isActivationSuccessful(SkillActivationType.RANDOM_LINEAR_100_SCALE_WITH_CAP, SubSkillType.TRIDENTS_ARROW_DEFLECT, getPlayer())) {
            NotificationManager.sendPlayerInformation(getPlayer(), NotificationType.SUBSKILL_MESSAGE, "Combat.ArrowDeflect");
            return true;
        }

        return false;
    }

    /**
     * Check for Bleed effect.
     *
     * @param target The defending entity
     */
    public void ruptureCheck(LivingEntity target) {
        if (RandomChanceUtil.isActivationSuccessful(SkillActivationType.RANDOM_LINEAR_100_SCALE_WITH_CAP, SubSkillType.TRIDENTS_RUPTURE, getPlayer())) {

            if (target instanceof Player) {
                Player defender = (Player) target;

                //Don't start or add to a bleed if they are blocking
                if(defender.isBlocking())
                    return;

                if (NotificationManager.doesPlayerUseNotifications(defender)) {
                    if(!BleedTimerTask.isBleeding(defender))
                        NotificationManager.sendPlayerInformation(defender, NotificationType.SUBSKILL_MESSAGE, "Tridents.Combat.Bleeding.Started");
                }
            }

            BleedTimerTask.add(target, getPlayer(), getRuptureBleedTicks(), RankUtils.getRank(getPlayer(), SubSkillType.TRIDENTS_RUPTURE), getToolTier(getPlayer().getInventory().getItemInMainHand()));

            if (mcMMOPlayer.useChatNotifications()) {
                NotificationManager.sendPlayerInformation(getPlayer(), NotificationType.SUBSKILL_MESSAGE, "Tridents.Combat.Bleeding");
            }
        }
    }

    public int getRuptureBleedTicks()
    {
        int bleedTicks = 2 * RankUtils.getRank(getPlayer(), SubSkillType.TRIDENTS_RUPTURE);

        if(bleedTicks > Tridents.bleedMaxTicks)
            bleedTicks = Tridents.bleedMaxTicks;

        return bleedTicks;
    }

    public int getToolTier(ItemStack itemStack)
    {
        if(ItemUtils.isDiamondTool(itemStack))
            return 4;
        else if(ItemUtils.isIronTool(itemStack) || ItemUtils.isGoldTool(itemStack))
            return 3;
        else if(ItemUtils.isStoneTool(itemStack))
            return 2;
        else
            return 1;
    }

    /**
     * Check Iron Grip ability success
     *
     * @param defender The defending player
     * @return true if the defender was not disarmed, false otherwise
     */
    private boolean hasIronGrip(Player defender) {
        if (!Misc.isNPCEntityExcludingVillagers(defender)
                && Permissions.isSubSkillEnabled(defender, SubSkillType.UNARMED_IRON_GRIP)
                && RandomChanceUtil.isActivationSuccessful(SkillActivationType.RANDOM_LINEAR_100_SCALE_WITH_CAP, SubSkillType.UNARMED_IRON_GRIP, defender)) {
            NotificationManager.sendPlayerInformation(defender, NotificationType.SUBSKILL_MESSAGE, "Unarmed.Ability.IronGrip.Defender");
            NotificationManager.sendPlayerInformation(getPlayer(), NotificationType.SUBSKILL_MESSAGE, "Unarmed.Ability.IronGrip.Attacker");

            return true;
        }

        return false;
    }
}
