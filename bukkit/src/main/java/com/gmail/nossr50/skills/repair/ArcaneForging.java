package com.gmail.nossr50.skills.repair;

import com.gmail.nossr50.core.config.skills.AdvancedConfig;

public class ArcaneForging {

    public static boolean arcaneForgingDowngrades  = AdvancedConfig.getInstance().getArcaneForgingDowngradeEnabled();
    public static boolean arcaneForgingEnchantLoss = AdvancedConfig.getInstance().getArcaneForgingEnchantLossEnabled();
}
