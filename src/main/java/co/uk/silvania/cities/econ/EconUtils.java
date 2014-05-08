package co.uk.silvania.cities.econ;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import co.uk.silvania.cities.core.CityConfig;
import co.uk.silvania.cities.core.ClientPacketHandler;
import co.uk.silvania.cities.core.CoreItems;
import co.uk.silvania.cities.core.NBTConfig;
import co.uk.silvania.cities.econ.money.ItemCoin;
import co.uk.silvania.cities.econ.money.ItemNote;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class EconUtils {
	
	
	/*
	 * Methods available in this class:
	 * parseDouble(string) - Turn a string to a double
	 * parseInt(string) - Turn a string to an int
	 * moveIntToBalance - Turn an int into a double, used for balance stuff
	 * 
	 * reqClientInventoryBalance() - Fires off and receives a response packet, giving you the client's correct inventory balance. Uses packets to avoid item ghosts.
	 * reqClientBankBalance() - Tells the client how much is in the players bank balance.
	 * Both of these are CLIENT-side, for use in things like GUI and on-screen rendering. Please don't call them server-side, you will crash.
	 * 
	 * giveChange(d paid, d cost, player) - Calculates the change owed to a player. First double is the amount the player gave, second is the value.
	 * getAllInventoryCash(player) - Calculates the total amount of cash in the players inventory.
	 * removeAllPlayerCash(player) - Takes all the cash from the players inventory
	 * findCashInInventory(player, d value) - Find's a particular coin or note in the players inventory. If found, it will remove it.
	 * chargePlayerAnywhere - Charges a player with both physical money and card. Will try physical first, then fallback to debit card before failing.
	 * findCoinsInInventory - Charge a player using only coins
	 * findNotesInInventory - Charge a player using only notes
	 * getBalance(player, world) - Gets the player's current bank balance. Does not count inventory.
	 * TODO payBalanceViaCard(d cost, player, playerOwner, world) - Opens the GUI for a card transaction to send money from the player to the shop owner
	 */
		
	//Use to convert things like a string to a double, usable by the economy.
	//VERY useful for example in the ATM, keying in values from the buttons.
	public static double parseDouble(String s) {
		try { 
			return Double.parseDouble(s);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}
	
	//This one is used to turn the PIN from a string to an int when changing it in the ATM.
	public static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}
	
	public static double moveIntToBalance(int i) {
		double newBalance = i;
		return newBalance;
	}
	
	public static double reqClientInventoryBalance() {
		return ClientPacketHandler.invBalance;
	}
	
	public static double reqClientBankBalance() {
		return ClientPacketHandler.initBal;
	}
	
	//Send info to the client with players bank balance.
	public static void triggerServerBankBalancePacket(EntityPlayer player, World world) {
        ByteArrayOutputStream bt = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bt);
        try {
        	out.writeUTF("InitBalance");
        	out.writeDouble(getBalance(player, world));
        	
        	Packet250CustomPayload packet = new Packet250CustomPayload("FCitiesPackets", bt.toByteArray());
        	
        	Player par1Player = (Player)player;
        	
        	PacketDispatcher.sendPacketToPlayer(packet, par1Player);
        }
        catch (IOException ex) {
        	System.out.println("Packet Failed!");
        }
	}
	
	//Send info to the client with players inventory balance.
	public static void triggerServerInventoryBalancePacket(EntityPlayer player, World world) {
        ByteArrayOutputStream bt = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bt);
        try {
        	out.writeUTF("InventoryBalance");
        	out.writeDouble(getInventoryCash(player));
        	
        	Packet250CustomPayload packet = new Packet250CustomPayload("FCitiesPackets", bt.toByteArray());
        	
        	Player par1Player = (Player)player;
        	
        	PacketDispatcher.sendPacketToPlayer(packet, par1Player);
        }
        catch (IOException ex) {
        	System.out.println("Packet Failed!");
        }
	}
	
	//This takes the amount paid against the total cost, and pays the player the correct change.
	//Also used by the ATM withdrawl.
	
	/*public static void giveChange(double paid, double cost, EntityPlayer entityPlayer) {
		World world = entityPlayer.worldObj;
		double change = parseDouble(formatBalance(paid - cost));
		double toBank = change;
		
		ItemStack dollar100 = new ItemStack(CoreItems.note10000);
		ItemStack dollar50 = new ItemStack(CoreItems.note5000);
		ItemStack dollar20 = new ItemStack(CoreItems.note2000);
		ItemStack dollar10 = new ItemStack(CoreItems.note1000);
		ItemStack dollar5 = new ItemStack(CoreItems.note500);
		ItemStack dollar2 = new ItemStack(CoreItems.note200);
		ItemStack dollar1 = new ItemStack(CoreItems.note100);
		
		ItemStack cent50 = new ItemStack(CoreItems.coin50);
		ItemStack cent25 = new ItemStack(CoreItems.coin25);
		ItemStack cent10 = new ItemStack(CoreItems.coin10);
		ItemStack cent5 = new ItemStack(CoreItems.coin5);
		ItemStack cent2 = new ItemStack(CoreItems.coin2);
		ItemStack cent1 = new ItemStack(CoreItems.coin1);
		
		while (change >= 100) {
			entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.note10000));
			change = change - 100;
		}
		while (change >= 50) {
			entityPlayer.inventory.addItemStackToInventory(dollar50);
			change = change - 50;
		} 
		while (change >= 20) {
			entityPlayer.inventory.addItemStackToInventory(dollar20);
			change = change - 20;
		}
		while (change >= 10) {
			entityPlayer.inventory.addItemStackToInventory(dollar10);
			change = change - 10;
		}
		while (change >= 5) {
			entityPlayer.inventory.addItemStackToInventory(dollar5);
			change = change - 5;
		}
		while (change >= 2) {
			entityPlayer.inventory.addItemStackToInventory(dollar2);
			change = change - 2;
		}
		while (change >= 1) {
			entityPlayer.inventory.addItemStackToInventory(dollar1);
			change = change - 1;
		}
		while (change >= 0.5) {
			entityPlayer.inventory.addItemStackToInventory(cent50);
			change = change - 0.5;
		}
		while (change >= 0.25) {
			entityPlayer.inventory.addItemStackToInventory(cent25);
			change = change - 0.25;
		}
		while (change >= 0.1) {
			entityPlayer.inventory.addItemStackToInventory(cent10);
			change = change - 0.1;
		}
		while (change >= 0.05) {
			entityPlayer.inventory.addItemStackToInventory(cent5);
			change = change - 0.05;
		}
		while (change >= 0.02) {
			entityPlayer.inventory.addItemStackToInventory(cent2);
			change = change - 0.02;
		}
		while (change > 0) {
			entityPlayer.inventory.addItemStackToInventory(cent1);
			change = change - 0.01;
		}
	}*/
	
	//New WIP version which checks inventory space before giving it, and sends the remainder to the bank account.
	public static void giveChange(double paid, double cost, EntityPlayer entityPlayer) {
		World world = entityPlayer.worldObj;
		double change = parseDouble(formatBalance(paid - cost));
		double toBank = 0;	
		
		while (change >= 100) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.note10000))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.note10000));
				System.out.println("100 to inventory :)");
			} else {
				System.out.println("Sending 100 to bank");
				toBank = toBank + 100;
			}
			change = change - 100;
		}
		
		while (change >= 50) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.note5000))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.note5000));
				System.out.println("50 to inventory :)");
			} else {
				System.out.println("50 to bank.");
				toBank = toBank + 50;
			}
			change = change - 50;
		} 
		
		while (change >= 20) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.note2000))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.note2000));
				System.out.println("20 to inventory :)");
			} else {
				System.out.println("20 to bank.");
				toBank = toBank + 20;
			}
			change = change - 20;
		}
		
		while (change >= 10) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.note1000))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.note1000));
				System.out.println("10 to inventory :)");
			} else {
				System.out.println("10 to bank.");
				toBank = toBank + 10;
			}
			change = change - 10;
		}
		
		while (change >= 5) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.note500))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.note500));
				System.out.println("5 to inventory :)");
			} else {
				System.out.println("5 to bank.");
				toBank = toBank + 5;
			}
			change = change - 5;
		}
		
		while (change >= 2) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.note200))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.note200));
				System.out.println("2 to inventory :)");
			} else {
				System.out.println("2 to bank.");
				toBank = toBank + 2;
			}
			change = change - 2;
		}
		
		while (change >= 1) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.note100))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.note100));
				System.out.println("1 to inventory :)");
			} else {
				System.out.println("1 to bank.");
				toBank = toBank + 1;
			}
			change = change - 1;
	
		//Coins
		}
		while (change >= 0.5) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.coin50))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.coin50));
				System.out.println("0.50 to inventory :)");
			} else {
				System.out.println("0.50 to bank.");
				toBank = toBank + 0.5;
			}
			change = change - 0.5;
		}
		
		while (change >= 0.25) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.coin25))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.coin25));
				System.out.println("0.25 to inventory :)");
			} else {
				System.out.println("0.25 to bank.");
				toBank = toBank + 0.25;
			}
			change = change - 0.25;
		}
		
		while (change >= 0.1) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.coin10))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.coin10));
				System.out.println("0.10 to inventory :)");
			} else {
				System.out.println("0.10 to bank.");
				toBank = toBank + 0.1;
			}
			change = change - 0.1;
		}
		
		while (change >= 0.05) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.coin5))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.coin5));
				System.out.println("0.05 to inventory :)");
			} else {
				System.out.println("0.05 to bank.");
				toBank = toBank + 0.05;
			}
			change = change - 0.05;
		}
		
		while (change >= 0.02) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.coin2))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.coin2));
				System.out.println("0.02 to inventory :)");
			} else {
				System.out.println("0.02 to bank.");
				toBank = toBank + 0.02;
			}
			change = change - 0.02;
		}
		
		while (change > 0) {
			if (inventoryHasSpace(entityPlayer, new ItemStack(CoreItems.coin1))) { 
				entityPlayer.inventory.addItemStackToInventory(new ItemStack(CoreItems.coin1));
				System.out.println("0.01 to inventory :)");
			} else {
				System.out.println("0.01 to bank.");
				toBank = toBank + 0.01;
			}
			change = change - 0.01;
		}
		
		System.out.println("Depositing " + toBank + " to players account.");
		depositToAccount(entityPlayer, world, toBank);
	}
	
	//Counts up all the money in the players inventory.
	//When wallets are added, they will have something like this independently, and will just be added to the final balance.
	//That way, getInventoryCash will return all money both in your wallet and in your general inventory.
	public static double getInventoryCash(EntityPlayer player) {
		double balance = 0;
		for (int i = player.inventory.getSizeInventory() - 1; i >= 0; -- i) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null) {
				double moneyValue = 0;
				if (stack.getItem() instanceof ItemNote) {
					ItemNote note = (ItemNote) stack.getItem();
					if (note.getMoneyValue() >= 0) {
						moneyValue = note.getMoneyValue();
					}
				} else if (stack.getItem() instanceof ItemCoin) {
					ItemCoin coin = (ItemCoin) stack.getItem();
					if (coin.getMoneyValue() >= 0) {
						moneyValue = coin.getMoneyValue();
					}
				}
				int quantity = stack.stackSize;
				double totalValue = moneyValue * quantity;
				
				if (CityConfig.debugMode == true) {
					System.out.println("There is a money stack with value of " + moneyValue  + ". The stack size is " + quantity + " with a total value of " + totalValue);
				}
				balance = balance + totalValue;
			}
		}
		return balance;
	}
	
	public static void removeAllPlayerCash(EntityPlayer player) {
		if (CityConfig.debugMode == true) {
			System.out.println("Beginning loop to remove all player cash");
		}
		for (int i = player.inventory.getSizeInventory() - 1; i >= 0; -- i) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null) {
				if (stack.getItem() instanceof ItemNote || stack.getItem() instanceof ItemCoin) {
					if (CityConfig.debugMode) {
						System.out.println("Found note, Removing!");
					}
					player.inventory.setInventorySlotContents(i, null);
					((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
				}
			}
		}
	}
	
	/*
	 * Second to the ATM, this is the most complex section of code I've ever written, and it does a lot more than it looks!
	 * 
	 * This paragraph (/essay) will explain what this deceivingly small block of code does.
	 * 
	 * First things first, it checks the player actually has enough money in their inventory. If they don't, it won't go any further.
	 * Next, it begins a loop. This loop repeats the ENTIRE remaining code on each slot of the inventory.
	 * 
	 * If the slot contains either a coin or note, it continues. It checks the amount of that item in the stack, and also gets the value of the singular item.
	 * 
	 * Next, we start a second loop. This is where the magic really happens.
	 * The loop starts at 1, and increases 1 each time it goes around, until it reaches however many are in the item's stack.
	 * 
	 * Each time it goes around, it checks the value of the amount it has counted against the amount requested
	 * For example, say it's the third time around and we're checking $10, it would be $30.
	 * 
	 * When we reach the correct amount (either equal or higher than the originally requested value), we do a few more things:
	 * First, it will either nullify the stack if appropriate, or just reduce by the correct amount.
	 * After that, we run the giveChange method above, in case change is required (eg they paid $30, but only $28 was needed)
	 * We then refresh the inventory, and return true, because we have the correct amount.
	 * 
	 * Now, what happens if the stack doesn't have enough cash?
	 * If the for loop never reaches the critical point where it's equal to value, it won't continue through the if and thus won't return true.
	 * Instead, it skips past it, and saves the value it has counted so far to the currentlyPaid variable.
	 * The first for loop begins again, continuing to the next inventory slot, and starting the whole process again. It keeps doing this until eventually, the value is met.
	 * 
	 * LIKE A BOSS.
	 */
	public static boolean findCashInInventory(EntityPlayer player, double value) {
		if (getInventoryCash(player) >= value) {
			for (int i = player.inventory.getSizeInventory() - 1; i >= 0; -- i) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack != null) {
					if (stack.getItem() instanceof ItemNote || stack.getItem() instanceof ItemCoin) {
						int qty = stack.stackSize;
						double noteValue = 0;
						double coinValue = 0;
						if (stack.getItem() instanceof ItemNote) {
							//These are inside if statements in order to avoid bad casting.
							ItemNote note = (ItemNote) stack.getItem();
							noteValue = note.getMoneyValue();
						}
						if (stack.getItem() instanceof ItemCoin) {
							ItemCoin coin = (ItemCoin) stack.getItem();
							coinValue = coin.getMoneyValue();
						}
						//Here, we add the two values. Only one of the two is ever used, so "moneyValue" is both note and coin value.
						double moneyValue = noteValue + coinValue;
						double currentlyPaid = 0;
						//Second loop, basically checks if the stack's value is high enough one item at a time (as to not overpay)
						for(int x = 1; x <= qty; x++) {
							if (CityConfig.debugMode) {
								System.out.println("Nested Loop! Current stack value is: " + (moneyValue * x) + " - The target is " + value);
							}
							if (currentlyPaid + (moneyValue * x) >= value) {
								System.out.println("This is fired if the moneyValue is higher than the value, allegedly");
								if (x == qty) {
									player.inventory.setInventorySlotContents(i, null);
								} else
									player.inventory.decrStackSize(i, x);
								double paidAmount = moneyValue * x;
								System.out.println("Give change: " + (paidAmount - value));
								depositToAccount(player, player.worldObj, paidAmount-value);
								((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
								//If second loop pays enough, we return; we don't need to do anything else as the balance has been paid.
								return true;
							}
						}
						//If second loop fails, this part is ran.
						currentlyPaid = currentlyPaid + (moneyValue * qty);
						player.inventory.setInventorySlotContents(i, null);
						((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
					}
				}
			}
		}
		//If they don't have enough cash, that's it - they can't buy it.
		//Later, I'll add a secondary option for paying by card here.
		return false;
	}
	
	//Almost identical to findCashInInv, except it returns the value of the change.
	//Useful for player owned shop systems, as change should be taken from the player and NOT generated.
	public static double findCashInInventoryWithChange(EntityPlayer player, double value) {
		double currentlyPaid = 0;
		if (getInventoryCash(player) >= value) {
			for (int i = player.inventory.getSizeInventory() - 1; i >= 0; -- i) {
				if (CityConfig.debugMode) {
					System.out.println("Currently paid: " + currentlyPaid + ", Value: " + value);
				}
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack != null) {
					if (stack.getItem() instanceof ItemNote || stack.getItem() instanceof ItemCoin) {
						int qty = stack.stackSize;
						double noteValue = 0;
						double coinValue = 0;
						if (stack.getItem() instanceof ItemNote) {
							ItemNote note = (ItemNote) stack.getItem();
							noteValue = note.getMoneyValue();
						}
						if (stack.getItem() instanceof ItemCoin) {
							ItemCoin coin = (ItemCoin) stack.getItem();
							coinValue = coin.getMoneyValue();
						}
						double moneyValue = noteValue + coinValue; //How much
						double stackValue = qty * (parseDouble(formatBalance(moneyValue)));
						currentlyPaid = currentlyPaid + stackValue;
						player.inventory.setInventorySlotContents(i, null);
						if (currentlyPaid >= value) {
							if (CityConfig.debugMode) {
								System.out.println("Giving change, SHOULD be " + (value - currentlyPaid));
							}
							giveChange(currentlyPaid, value, player);
							((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
							return 0;
						}
							
						/*for(int x = 1; x <= qty; x++) {
							if (CityConfig.debugMode) {
								System.out.println("Nested Loop! Current stack value is: " + (moneyValue * x) + " - The target is " + value);
							}
							currentlyPaid = currentlyPaid + (moneyValue * x);
							if (currentlyPaid >= value) {
								System.out.println("This is fired if the moneyValue is higher than the value, allegedly");
								if (x == qty) {
									player.inventory.setInventorySlotContents(i, null);
								} else
									player.inventory.decrStackSize(i, x);
								System.out.println("Give change: " + (currentlyPaid - value));
								giveChange(currentlyPaid, value, player);
								((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
								return currentlyPaid;
							}*/
					}
				}
			}
		}
		return 0;
	}
	
	//Attempt to give change from an inventory, such as a shop's cash register.
	public static boolean giveChangeFromInventory(EntityPlayer player, World world, int x, int y, int z, double value) {
		return false;
	}
	
	public static boolean chargePlayerAnywhere(EntityPlayer player, double value) {
		World world = player.worldObj;
		if (findCashInInventory(player, value) == false) {
			double invBalance = getInventoryCash(player);
			double cardBalance = getBalance(player, world);
			
			double totalBalance = invBalance + cardBalance;
			if (player.inventory.hasItem(CoreItems.debitCardNew.itemID)) {
				if (invBalance < value) {
					if (totalBalance >= value) {
						double payAmount = value - invBalance;
						if (payBalanceByCard(player, payAmount)) {
							removeAllPlayerCash(player);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static boolean findCoinsInInventory(EntityPlayer player, double value) {
		if (getInventoryCash(player) >= value) {
			for (int i = player.inventory.getSizeInventory() - 1; i >= 0; -- i) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack != null) {
					if (stack.getItem() instanceof ItemCoin) {
						int qty = stack.stackSize;
						double noteValue = 0;
						double coinValue = 0;
						ItemCoin coin = (ItemCoin) stack.getItem();
						coinValue = coin.getMoneyValue();
						double currentlyPaid = 0;
						for(int x = 1; x <= qty; x++) {
							if (CityConfig.debugMode) {
								System.out.println("Nested Loop! Current stack value is: " + (coinValue * x) + " - The target is " + value);
							}
							if (currentlyPaid + (coinValue * x) >= value) {
								System.out.println("This is fired if the moneyValue is higher than the value, allegedly");
								if (x == qty) {
									player.inventory.setInventorySlotContents(i, null);
								} else
									player.inventory.decrStackSize(i, x);
								double paidAmount = coinValue * x;
								System.out.println("Give change: " + (paidAmount - value));
								depositToAccount(player, player.worldObj, (paidAmount-value));
								((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
								return true;
							}
						}
						currentlyPaid = currentlyPaid + (coinValue * qty);
						player.inventory.setInventorySlotContents(i, null);
						((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
					}
				}
			}
		}
		return false;
	}
	
	public static boolean findNotesInInventory(EntityPlayer player, double value) {
		if (getInventoryCash(player) >= value) {
			for (int i = player.inventory.getSizeInventory() - 1; i >= 0; -- i) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack != null) {
					if (stack.getItem() instanceof ItemNote) {
						int qty = stack.stackSize;
						double noteValue = 0;
						ItemNote note = (ItemNote) stack.getItem();
						noteValue = note.getMoneyValue();
						double currentlyPaid = 0;
						for(int x = 1; x <= qty; x++) {
							if (CityConfig.debugMode) {
								System.out.println("Nested Loop! Current stack value is: " + (noteValue * x) + " - The target is " + value);
							}
							if (currentlyPaid + (noteValue * x) >= value) {
								System.out.println("This is fired if the moneyValue is higher than the value, allegedly");
								if (x == qty) {
									player.inventory.setInventorySlotContents(i, null);
								} else
									player.inventory.decrStackSize(i, x);
								double paidAmount = noteValue * x;
								System.out.println("Give change: " + (paidAmount - value));
								depositToAccount(player, player.worldObj, (paidAmount-value));
								((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
								return true;
							}
						}
						currentlyPaid = currentlyPaid + (noteValue * qty);
						player.inventory.setInventorySlotContents(i, null);
						((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
					}
				}
			}
		}
		return false;
	}
	
	public static boolean payBalanceByCard(EntityPlayer player, double value) {
		World world = player.worldObj;
		double cardBalance = getBalance(player, world);
		if (value <= cardBalance) {
			//They can pay by card!
			//Open GUI
			//Check PIN

	        ByteArrayOutputStream bt = new ByteArrayOutputStream();
	        DataOutputStream out = new DataOutputStream(bt);
			
			String victimPlayer = DebitCardItem.checkCardOwner(player);
	        NBTTagCompound nbt = NBTConfig.getTagCompoundInFile(NBTConfig.getWorldConfig(world));
			double currentBalance = 0;
	        if (nbt.hasKey(player.username)) {
	            NBTTagCompound playernbt = nbt.getCompoundTag(player.username);
	            if (playernbt.hasKey("Balance")) {
	                currentBalance = playernbt.getDouble("Balance");
	            }
	            double modifiedBalance = currentBalance - value;
	            playernbt.setDouble("Balance", modifiedBalance);
	            nbt.setCompoundTag(player.username, playernbt);
	        } else {
	            NBTTagCompound playernbt = new NBTTagCompound();
	            if (playernbt.hasKey("Balance")) {
	                currentBalance = playernbt.getDouble("Balance");
	            }
	            double modifiedBalance = currentBalance - value;
	            playernbt.setDouble("Balance", modifiedBalance);
	            nbt.setCompoundTag(player.username, playernbt);
	        }
	        NBTTagCompound playernbt = nbt.getCompoundTag(player.username);
	        NBTConfig.saveConfig(nbt, NBTConfig.getWorldConfig(world));
			return true;
		}
		return false;
	}
	
	//Used for printing the balance ONLY. Do NOT use anywhere money values are actually altered!
	//Simply rounds any double to two decimal places, stops bugs where doubles add micro factions.
	public static String formatBalance(double bal) {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		String str = nf.format(bal);
		str = str.replace(",", "");
		
		return str;
	}

	//Quick n' easy method of getting the players balance.
	public static double getBalance(EntityPlayer player, World world) {
        ByteArrayOutputStream bt = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bt);
		
		String victimPlayer = DebitCardItem.checkCardOwner(player);
        NBTTagCompound nbt = NBTConfig.getTagCompoundInFile(NBTConfig.getWorldConfig(world));
        double balance = 0;
        if (nbt.hasKey(victimPlayer)) {
            NBTTagCompound playernbt = nbt.getCompoundTag(victimPlayer);
            if (playernbt.hasKey("Balance")) {
                balance = playernbt.getDouble("Balance");
            }
        }
        return balance;
	}
	
	//Checks if the inventory has room for the specified itemstack. Returns false either if all slots are full (with no matching types),
	//Or if there's an available slot but it doesn't have room to accomodate.
	//Returns true if there's an empty slot, or a partially filled slot of the same type which has enough free space to add to it.
	//I am aware that the addItemStackToInventory method already does this check, but you may want to use this anyway.
	//For example, you don't want to charge a player and then not give them their stuff,
	//and likewise, you don't want to give them the stuff and find you can't charge them.
	//Consider this a "safe check" - it finds the answer without altering the inventory in any way.
	public static boolean inventoryHasSpace(EntityPlayer player, ItemStack item) {
		for (int x = 35; x >= 0; --x) {
			ItemStack slot = player.inventory.getStackInSlot(x);
			if (slot != null) {
				if (slot.getItem().equals(item.getItem())) {
					int max = slot.getMaxStackSize();
					int slotSize = slot.stackSize;
					int itemSize = item.stackSize;
					
					if ((itemSize + slotSize) <= max) {
						System.out.println("Unfilled compatable stack found; adding to it.");
						return true;
					}	
				}
			} else {
				System.out.println("Emtpy slot found. How useful! ID: " + x);
				return true;
			}
		}
		return false;
	}
	
	//Finds out if the player has a debit card that they own in their inventory.
	public static boolean hasOwnCard(EntityPlayer player) {
		for (int i = player.inventory.getSizeInventory() - 1; i >= 0; -- i) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null) {
				if (stack.getItem() == CoreItems.debitCardNew) {
					if (player.username.equals(stack.stackTagCompound.getString("playerName"))) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static void depositToAccount(EntityPlayer player, World world, double deposit) {
		NBTTagCompound nbt = NBTConfig.getTagCompoundInFile(NBTConfig.getWorldConfig(world));
		double currentBalance = 0;
        if (nbt.hasKey(player.username)) {
            NBTTagCompound playernbt = nbt.getCompoundTag(player.username);
            if (playernbt.hasKey("Balance")) {
                currentBalance = playernbt.getDouble("Balance");
            }
            double modifiedBalance = currentBalance + deposit;
            playernbt.setDouble("Balance", modifiedBalance);
            nbt.setCompoundTag(player.username, playernbt);
        } else {
            NBTTagCompound playernbt = new NBTTagCompound();
            if (playernbt.hasKey("Balance")) {
                currentBalance = playernbt.getDouble("Balance");
            }
            double modifiedBalance = currentBalance + deposit;
            playernbt.setDouble("Balance", modifiedBalance);
            nbt.setCompoundTag(player.username, playernbt);
        }
        NBTTagCompound playernbt = nbt.getCompoundTag(player.username);
        NBTConfig.saveConfig(nbt, NBTConfig.getWorldConfig(world));
        player.addChatMessage(EnumChatFormatting.GOLD + "" + EnumChatFormatting.GREEN + " was sent to your bank account. Your current total balance is $" + EnumChatFormatting.GOLD + EconUtils.formatBalance(EconUtils.getBalance(player, player.worldObj)));
	}
}
