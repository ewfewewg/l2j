/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Mobius
 */
public class RequestEvaluate extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		readInt(); // target Id
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Player target = (Player) player.getTarget();
		if (target == null)
		{
			player.sendPacket(SystemMessageId.SELECT_TARGET);
			return;
		}
		
		if (!(player.getTarget() instanceof Player))
		{
			player.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		if (player.getLevel() < 10)
		{
			player.sendPacket(SystemMessageId.ONLY_CHARACTERS_OF_LEVEL_10_OR_ABOVE_ARE_AUTHORIZED_TO_MAKE_RECOMMENDATIONS);
			return;
		}
		
		if (player.getTarget() == player)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}
		
		if (player.getRecomLeft() <= 0)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_MAKE_FURTHER_RECOMMENDATIONS_AT_THIS_TIME_YOU_WILL_RECEIVE_MORE_RECOMMENDATION_CREDITS_EACH_DAY_AT_1_P_M);
			return;
		}
		
		if (target.getRecomHave() >= 255)
		{
			player.sendPacket(SystemMessageId.YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION);
			return;
		}
		
		if (!Config.ALT_RECOMMEND && !player.canRecom(target))
		{
			player.sendPacket(SystemMessageId.THAT_CHARACTER_HAS_ALREADY_BEEN_RECOMMENDED);
			return;
		}
		
		player.giveRecom(target);
		
		SystemMessage sm = null;
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_S1_YOU_ARE_AUTHORIZED_TO_MAKE_S2_MORE_RECOMMENDATIONS);
		sm.addPcName(target);
		sm.addInt(player.getRecomLeft());
		player.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_S1);
		sm.addPcName(player);
		target.sendPacket(sm);
		
		player.updateUserInfo();
		target.broadcastUserInfo();
	}
}
