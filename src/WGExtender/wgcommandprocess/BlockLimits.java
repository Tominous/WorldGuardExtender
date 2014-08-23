/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package WGExtender.wgcommandprocess;

import java.math.BigInteger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import WGExtender.Config;
import WGExtender.WGExtender;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class BlockLimits {

	public BlockLimits() {
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			vaultperms = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class).getProvider();
		}
	}

	private Object vaultperms;

	public ProcessedClaimInfo processClaimInfo(Config config, Player player) {
		Selection psel = WGExtender.getInstance().getWorldEdit().getSelection(player);
		ProcessedClaimInfo info = new ProcessedClaimInfo();
		if (psel == null) {
			return info;
		}
		Vector min = psel.getNativeMinimumPoint();
		Vector max = psel.getNativeMaximumPoint();
		BigInteger size = BigInteger.ONE;
		size = size.multiply(BigInteger.valueOf(max.getBlockX()).subtract(BigInteger.valueOf(min.getBlockX())));
		size = size.multiply(BigInteger.valueOf(max.getBlockZ()).subtract(BigInteger.valueOf(min.getBlockZ())));
		size = size.multiply(BigInteger.valueOf(max.getBlockY()).subtract(BigInteger.valueOf(min.getBlockY())));
		if (size.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
			info.disallow();
			info.setInfo(size, BigInteger.valueOf(Integer.MAX_VALUE));
			return info;
		}
		if (player.hasPermission("worldguard.region.unlimited")) {
			return info;
		}
		if (config.blocklimitsenabled) {
			String[] pgroups = vaultperms != null ? ((Permission) vaultperms).getPlayerGroups(player) : WGExtender.getInstance().getWorldGuard().getGroups(player);
			if (pgroups.length == 0) {
				return info;
			}
			int maxblocks = 0;
			for (String pgroup : pgroups) {
				pgroup = pgroup.toLowerCase();
				if (config.blocklimits.containsKey(pgroup)) {
					maxblocks = Math.max(maxblocks, config.blocklimits.get(pgroup));
				}
			}
			BigInteger maxblocksi = BigInteger.valueOf(maxblocks);
			if (size.compareTo(maxblocksi) > 0) {
				info.disallow();
				info.setInfo(size, maxblocksi);
				return info;
			}
		}
		return info;
	}

	protected static class ProcessedClaimInfo {

		private boolean claimAllowed = true;
		private BigInteger size;
		private BigInteger maxsize;

		public void disallow() {
			claimAllowed = false;
		}

		public boolean isClaimAllowed() {
			return claimAllowed;
		}

		public void setInfo(BigInteger claimed, BigInteger max) {
			size = claimed;
			maxsize = max;
		}

		public String getClaimedSize() {
			return size.toString();
		}

		public String getMaxSize() {
			return maxsize.toString();
		}

	}

}
