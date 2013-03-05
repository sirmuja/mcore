package com.massivecraft.mcore.mixin;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.massivecraft.mcore.PS;
import com.massivecraft.mcore.event.MCorePlayerPSTeleportEvent;
import com.massivecraft.mcore.util.SenderUtil;
import com.massivecraft.mcore.util.Txt;

public class TeleportMixinDefault extends TeleportMixinAbstract
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static TeleportMixinDefault i = new TeleportMixinDefault();
	public static TeleportMixinDefault get() { return i; }
	
	// -------------------------------------------- //
	// CORE LOGIC
	// -------------------------------------------- //
	
	public static void teleportEntity(Entity entity, PS ps) throws TeleporterException
	{
		ps = ps.clone();
		
		// Ensure the ps has a world name
		if (ps.getWorldName() == null)
		{
			ps.setWorldName(entity.getWorld().getName());
		}
		
		Location location = ps.calcLocation();		
		if (location == null) throw new TeleporterException(Txt.parse("<b>Could not calculate the location."));
		
		entity.teleport(location);
		
		Vector velocity = ps.getVelocity();
		if (velocity == null) return;
		
		entity.setVelocity(velocity);
	}
	
	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public void teleport(Player teleportee, PS destinationPs, String destinationDesc, int delaySeconds) throws TeleporterException
	{
		this.sendPreTeleportMessage(teleportee, destinationDesc, delaySeconds);
		if (delaySeconds > 0)
		{
			new ScheduledTeleport(teleportee, destinationPs, destinationDesc, delaySeconds).schedule();
		}
		else
		{
			// Run event
			MCorePlayerPSTeleportEvent event = new MCorePlayerPSTeleportEvent(teleportee, teleportee.getLocation(), destinationPs.clone());
			event.run();
			if (event.isCancelled()) return;
			if (event.getTo() == null) return;
			destinationPs = event.getTo().clone();
			
			teleportEntity(teleportee, destinationPs);
		}
	}
	
	@Override
	public void teleport(String teleporteeId, PS destinationPs, String destinationDesc, int delaySeconds) throws TeleporterException
	{
		validateTeleporteeId(teleporteeId);
		Player teleportee = SenderUtil.getPlayer(teleporteeId);
		this.teleport(teleportee, destinationPs, destinationDesc, delaySeconds);
	}
	
}