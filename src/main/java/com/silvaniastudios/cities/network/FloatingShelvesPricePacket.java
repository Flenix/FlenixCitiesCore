package com.silvaniastudios.cities.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.silvaniastudios.cities.core.CityConfig;
import com.silvaniastudios.cities.core.FlenixCities_Core;
import com.silvaniastudios.cities.econ.EconUtils;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class FloatingShelvesPricePacket implements IMessage {
	
	public static String ownerName;
	public static double buyPrice1;
	public static double sellPrice1;
	public static double buyPrice2;
	public static double sellPrice2;
	public static double buyPrice3;
	public static double sellPrice3;
	public static double buyPrice4;
	public static double sellPrice4;
	
	public EconUtils econ = new EconUtils();
	
	public FloatingShelvesPricePacket() {}
	
	public FloatingShelvesPricePacket(String owner, double b1, double s1, double b2, double s2, double b3, double s3, double b4, double s4) {
		ownerName = owner;
		buyPrice1 = b1;
		sellPrice1 = s1;
		buyPrice2 = b2;
		sellPrice2 = s2;
		buyPrice3 = b3;
		sellPrice3 = s3;
		buyPrice4 = b4;
		sellPrice4 = s4;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		ownerName = ByteBufUtils.readUTF8String(buf);
		buyPrice1 = econ.parseDouble(ByteBufUtils.readUTF8String(buf));
		sellPrice1 = econ.parseDouble(ByteBufUtils.readUTF8String(buf));
		buyPrice2 = econ.parseDouble(ByteBufUtils.readUTF8String(buf));
		sellPrice2 = econ.parseDouble(ByteBufUtils.readUTF8String(buf));
		buyPrice3 = econ.parseDouble(ByteBufUtils.readUTF8String(buf));
		sellPrice3 = econ.parseDouble(ByteBufUtils.readUTF8String(buf));
		buyPrice4 = econ.parseDouble(ByteBufUtils.readUTF8String(buf));
		sellPrice4 = econ.parseDouble(ByteBufUtils.readUTF8String(buf));
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, ownerName);
		ByteBufUtils.writeUTF8String(buf, "" + buyPrice1);
		ByteBufUtils.writeUTF8String(buf, "" + sellPrice1);
		ByteBufUtils.writeUTF8String(buf, "" + buyPrice2);
		ByteBufUtils.writeUTF8String(buf, "" + sellPrice2);
		ByteBufUtils.writeUTF8String(buf, "" + buyPrice3);
		ByteBufUtils.writeUTF8String(buf, "" + sellPrice3);
		ByteBufUtils.writeUTF8String(buf, "" + buyPrice4);
		ByteBufUtils.writeUTF8String(buf, "" + sellPrice4);
	}
	
	public static class Handler implements IMessageHandler<FloatingShelvesPricePacket, IMessage> {

		@Override
		public IMessage onMessage(FloatingShelvesPricePacket message, MessageContext ctx) {
			if (CityConfig.debugMode) {
				System.out.println(String.format("Packet recieved! 9 bits of data? %s %s %s %s %s etc", message.ownerName, message.buyPrice1, message.sellPrice1, message.buyPrice2, message.sellPrice2));
			}
			return null;
		}
	}
}