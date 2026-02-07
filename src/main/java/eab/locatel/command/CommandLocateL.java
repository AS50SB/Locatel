package eab.locatel.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.*;

public class CommandLocateL extends CommandBase {
    
    @Override
    public String getName() {
        return "locatel";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "/locatel <structure|biome> <id>";
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /locatel <structure|biome> <id>"));
            return;
        }
        
        String type = args[0].toLowerCase();
        String id = args[1];
        
        if (type.equals("structure")) {
            String vanillaId = mapStructureToVanillaId(id);
            try {
                String command = "locate " + vanillaId;
                server.commandManager.executeCommand(sender, command);
            } catch (Exception e) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error: " + e.getMessage()));
            }
        } else if (type.equals("biome")) {
            locateBiome(id, server, sender);
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown type: " + type));
        }
    }
    
    private String mapStructureToVanillaId(String structureId) {
        String id = structureId.toLowerCase();
        
        if (id.startsWith("village_")) {
            return "Village";
        }
        
        switch (id) {
            case "village":
                return "Village";
            case "mineshaft":
                return "Mineshaft";
            case "stronghold":
                return "Stronghold";
            case "temple":
            case "desert_pyramid":
            case "jungle_pyramid":
            case "igloo":
            case "witch_hut":
                return "Temple";
            case "monument":
            case "ocean_monument":
                return "Monument";
            case "mansion":
            case "woodland_mansion":
                return "Mansion";
            case "fortress":
            case "nether_fortress":
                return "Fortress";
            case "endcity":
                return "EndCity";
            default:
                return structureId;
        }
    }
    
    private void locateBiome(String biomeId, MinecraftServer server, ICommandSender sender) {
        World world = sender.getEntityWorld();
        BlockPos startPos = sender.getPosition();
        
        Biome targetBiome = getBiomeByName(biomeId);
        if (targetBiome == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown biome: " + biomeId));
            return;
        }
        
        int maxRadius = 5000;
        int step = 32;
        
        BlockPos foundPos = null;
        double minDistance = Double.MAX_VALUE;
        
        for (int radius = step; radius <= maxRadius; radius += step) {
            for (int side = 0; side < 4; side++) {
                for (int offset = -radius; offset <= radius; offset += step) {
                    int x = 0;
                    int z = 0;
                    
                    switch (side) {
                        case 0:
                            x = offset;
                            z = -radius;
                            break;
                        case 1:
                            x = radius;
                            z = offset;
                            break;
                        case 2:
                            x = offset;
                            z = radius;
                            break;
                        case 3:
                            x = -radius;
                            z = offset;
                            break;
                    }
                    
                    BlockPos checkPos = startPos.add(x, 0, z);
                    
                    if (world.isBlockLoaded(checkPos)) {
                        Biome biome = world.getBiome(checkPos);
                        if (biome == targetBiome) {
                            double distance = startPos.distanceSq(checkPos);
                            if (distance < minDistance) {
                                minDistance = distance;
                                foundPos = checkPos;
                            }
                        }
                    }
                }
            }
            
            if (foundPos != null) {
                int distance = (int)Math.sqrt(minDistance);
                sender.sendMessage(new TextComponentString(
                    TextFormatting.GREEN + "Found biome " + biomeId + 
                    " at " + TextFormatting.AQUA + foundPos.getX() + " " + foundPos.getY() + " " + foundPos.getZ() +
                    TextFormatting.GREEN + " (" + distance + " blocks away)"
                ));
                return;
            }
        }
        
        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not find biome " + biomeId + " within 5000 blocks"));
    }
    
    private Biome getBiomeByName(String biomeName) {
        String searchName = biomeName.toLowerCase();
        
        if (searchName.startsWith("village_")) {
            searchName = searchName.substring(8);
        }
        
        Map<String, String> biomeMap = new HashMap<>();
        biomeMap.put("plains", "Plains");
        biomeMap.put("desert", "Desert");
        biomeMap.put("mountains", "Extreme Hills");
        biomeMap.put("forest", "Forest");
        biomeMap.put("taiga", "Taiga");
        biomeMap.put("swamp", "Swampland");
        biomeMap.put("river", "River");
        biomeMap.put("ocean", "Ocean");
        biomeMap.put("savanna", "Savanna");
        biomeMap.put("jungle", "Jungle");
        biomeMap.put("badlands", "Mesa");
        biomeMap.put("mesa", "Mesa");
        biomeMap.put("mushroom_fields", "MushroomIsland");
        biomeMap.put("ice_spikes", "Ice Plains Spikes");
        
        String mappedName = biomeMap.get(searchName);
        if (mappedName != null) {
            searchName = mappedName;
        } else {
            searchName = biomeName;
        }
        
        for (Biome biome : Biome.REGISTRY) {
            if (biome != null) {
                String name = biome.getBiomeName();
                if (name != null && name.equalsIgnoreCase(searchName)) {
                    return biome;
                }
            }
        }
        return null;
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, 
                                          String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "structure", "biome");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("structure")) {
                List<String> structures = Arrays.asList(
                    "village", "village_plains", "village_desert", "village_savanna", "village_taiga",
                    "mineshaft", "stronghold", 
                    "temple", "desert_pyramid", "jungle_pyramid", "igloo", "witch_hut",
                    "monument", "ocean_monument",
                    "mansion", "woodland_mansion",
                    "fortress", "nether_fortress",
                    "endcity"
                );
                return getListOfStringsMatchingLastWord(args, structures);
            } else if (args[0].equalsIgnoreCase("biome")) {
                List<String> biomes = Arrays.asList(
                    "plains", "desert", "mountains", "forest", "taiga", "swamp",
                    "river", "ocean", "savanna", "jungle", "badlands", "mesa",
                    "mushroom_fields", "ice_spikes"
                );
                return getListOfStringsMatchingLastWord(args, biomes);
            }
        }
        return Collections.emptyList();
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}