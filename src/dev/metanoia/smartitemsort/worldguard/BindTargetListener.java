package dev.metanoia.smartitemsort.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.metanoia.smartitemsort.BindDropTargetEvent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.bukkit.event.EventPriority.HIGHEST;



public final class BindTargetListener implements Listener {

    private final SmartItemSortWorldGuard plugin;

    public BindTargetListener(final SmartItemSortWorldGuard plugin) {
        this.plugin = plugin;

        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        final Plugin dependentPlugin = pluginManager.getPlugin("WorldGuard");

        if (dependentPlugin instanceof final WorldGuardPlugin worldGuardPlugin) {
            info(() -> String.format("Found WorldGuard %s", worldGuardPlugin.getDescription().getVersion()));
        } else {
            error(() -> "Could not find WorldGuard plugin");
        }
    }


    @EventHandler(priority = HIGHEST, ignoreCancelled = true)
    public void onBindDropTarget(final BindDropTargetEvent e) {
        final Block source = e.getSource();
        final ItemFrame target = e.getTarget();

        if (!isPermittedTarget(source, target)) {
            debug(() -> String.format("Canceled targeting of %s from %s", target, source.getLocation()));
            e.setCancelled(true);
        }
    }


    private boolean isPermittedTarget(final Block srcBlock, final ItemFrame target) {
        final WorldGuard wg = WorldGuard.getInstance();
        if (wg == null) {
            debug(() -> "Could not find WorldGuard plugin. WorldGuard regions not enforced.");
            return true;
        }

        final World world = srcBlock.getWorld();
        final RegionManager regionManager = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            debug(() -> "Could not find region manager. WorldGuard regions not enforced.");
            return true;
        }

        final BlockVector3 srcBlockVector3 = BlockVector3.at(srcBlock.getX(), srcBlock.getY(), srcBlock.getZ());
        final ApplicableRegionSet applicableSrcRegionSet = regionManager.getApplicableRegions(srcBlockVector3);

        // see if the target is in the same claim as the source block. We are optimistic and provide the claim
        // from the source block as a hint in the search for the target's claim (as it should be most of the time).
        final Block targetBlock = target.getLocation().getBlock().getRelative(target.getAttachedFace());
        final BlockVector3 targetBlockVector3 = BlockVector3.at(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
        final ApplicableRegionSet applicableTargetRegionSet = regionManager.getApplicableRegions(targetBlockVector3);

        if (!equalRegions(applicableSrcRegionSet, applicableTargetRegionSet)) {
            debug(() -> String.format("Target cannot be bound to source. Source regions are %s. Target regions are %s.", getRegionNames(applicableSrcRegionSet), getRegionNames(applicableTargetRegionSet)));
            return false;
        }

        trace(() -> "Source and target are in the same regions.");
        return true;
    }


    private List<String> getRegionNames(final ApplicableRegionSet regionSet) {
        ArrayList<String> nameList = new ArrayList<>();

        if (regionSet.size() == 0) {
            nameList.add("NONE");
            return nameList;
        }

        for (ProtectedRegion protectedRegion : regionSet) {
            nameList.add(protectedRegion.getId());
        }

        return nameList;
    }


    private boolean equalRegions(final ApplicableRegionSet left, final ApplicableRegionSet right) {
        return left.getRegions().equals(right.getRegions());
    }


    private void debug(Supplier<String> message) { this.plugin.debug(message); }
    private void error(Supplier<String> message) { this.plugin.error(message); }
    private void info(Supplier<String> message) { this.plugin.info(message); }
    private void trace(Supplier<String> message) { this.plugin.trace(message); }

}
