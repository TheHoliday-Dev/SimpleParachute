package com.holiday.simpleparachute;

import com.holiday.simpleparachute.commands.MainCommand;
import com.holiday.simpleparachute.vehicles.CustomChicken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.bukkit.*;

import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftBlockDisplay;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicReference;

public class Main extends JavaPlugin implements Listener {
    ItemStack parachuteItem;
    static Main instance;

    @Override
    public void onEnable() {
        // Registrar eventos
        instance = this;
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        createParachuteItem();

        getCommand("parachute").setExecutor(new MainCommand());
    }

    public static Main getInstance() {
        return instance;
    }

    public ItemStack getParachuteItem() {
        return parachuteItem;
    }

    void createParachuteItem() {
        ItemStack itemStack = new ItemStack(Material.GREEN_DYE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§aParachute");
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(new NamespacedKey(this, "parachute"), PersistentDataType.BOOLEAN, true);
        itemStack.setItemMeta(itemMeta);
        this.parachuteItem = itemStack;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return; // Ignorar clics con la otra mano

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != parachuteItem.getType())
            return; // Verificar que tiene un diamante
        ItemMeta itemMeta = player.getInventory().getItemInMainHand().getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        boolean isEnabled = container.get(new NamespacedKey(this, "parachute"), PersistentDataType.BOOLEAN);
        if (!isEnabled) return;
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_ELYTRA, 1.5f, 1.5f);
        // Crear un rectángulo verde en la cabeza del jugador con cuerdas
        createGreenRectangleWithRopes(player);
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        // Verifica que el bloque colocado sea una alfombra
        if (!event.getBlockPlaced().getType().toString().endsWith("_CARPET")) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();
        Location blockLocation = event.getBlockPlaced().getLocation();

        // Busca ítems dropeados cerca de la ubicación del bloque colocado
        for (org.bukkit.entity.Item nearbyItem : world.getEntitiesByClass(org.bukkit.entity.Item.class)) {
            if (nearbyItem.getItemStack().getType() != parachuteItem.getType())
                continue; // Verifica que sea un paracaídas
            if (nearbyItem.getLocation().distance(blockLocation) > 5)
                continue; // Verifica que esté dentro de un radio de 3 bloques

            // Cuenta las alfombras cerca del ítem dropeado
            int carpetCount = 0;
            int radius = 5;
            Location center = blockLocation.clone();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location checkLocation = blockLocation.clone().add(x, y, z);
                        Material blockType = world.getBlockAt(checkLocation).getType();

                        // Verificar si es una alfombra
                        if (blockType.toString().endsWith("_CARPET")) {
                            carpetCount++;
                        }
                    }
                }
            }

            if (carpetCount >= 6) {

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Location checkLocation = blockLocation.clone().add(x, y, z);

                            // Verificar si está dentro del radio esférico
                            if (center.distance(checkLocation) <= radius) {
                                Material blockType = world.getBlockAt(checkLocation).getType();
                                // Verificar si el bloque es una alfombra
                                if (blockType.toString().endsWith("_CARPET")) {
                                    world.getBlockAt(checkLocation).setType(Material.AIR); // Transformar en aire
                                }
                            }
                        }
                    }
                }

                // Cambia el valor del paracaídas a true
                ItemStack itemStack = nearbyItem.getItemStack();
                ItemMeta itemMeta = itemStack.getItemMeta();
                PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                container.set(new NamespacedKey(this, "parachute"), PersistentDataType.BOOLEAN, true);
                itemStack.setItemMeta(itemMeta);

                // Notifica al jugador
                player.sendMessage(ChatColor.GREEN + "¡Your parachute has been reactivated!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                break; // Solo afecta al primer paracaídas cercano encontrado
            }
        }
    }

    private void createGreenRectangleWithRopes(Player player) {
        // Obtener la ubicación de la cabeza del jugador
        reloadConfig();

        Location headLocation = player.getEyeLocation().clone().add(0, 0.5, 0); // Ajuste hacia arriba

        // Crear un BlockDisplay para el rectángulo verde
        BlockDisplay rectangleDisplay = (BlockDisplay) player.getWorld().spawn(headLocation, BlockDisplay.class);
        rectangleDisplay.setBlock(Material.GREEN_CONCRETE.createBlockData());

        float x = (float) getConfig().getDouble("x");
        float y = (float) getConfig().getDouble("y");
        float z = (float) getConfig().getDouble("z");

        float x2 = (float) getConfig().getDouble("x2");
        float y2 = (float) getConfig().getDouble("y2");
        float z2 = (float) getConfig().getDouble("z2");

        rectangleDisplay.setTransformation(new Transformation(
                new Vector3f(x, y, z), // Offset inicial (derecha y arriba)
                new Quaternionf(0, 0, 0, 1), // Rotación
                new Vector3f(x2, y2, z2), // Escalado
                new Quaternionf(0, 0, 0, 1) // Rotación final
        ));

        CraftWorld craftWorld = (CraftWorld) player.getWorld();
        Level level = craftWorld.getHandle(); // Convierte el mundo de Bukkit a NMS

        CustomChicken customChicken = new CustomChicken(level, player.getLocation());
        level.addFreshEntity(customChicken);
        customChicken.getBukkitEntity().setPassenger(player);

        AtomicReference<BukkitTask> taskRef = new AtomicReference<>();

        // Hacer que todo siga al jugador
        taskRef.set(Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!player.isOnline() || rectangleDisplay.isDead() || customChicken.getBukkitEntity().isOnGround()) {
                rectangleDisplay.remove();
                customChicken.remove(Entity.RemovalReason.KILLED);
                ItemStack itemStack = parachuteItem.clone();
                ItemMeta itemMeta = itemStack.getItemMeta();
                PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
                persistentDataContainer.set(new NamespacedKey(this, "parachute"), PersistentDataType.BOOLEAN, false);
                itemStack.setItemMeta(itemMeta);
                Location dropLocation = player.getLocation();
                player.getWorld().dropItemNaturally(dropLocation, itemStack);
                BukkitTask task = taskRef.get();
                if (task != null) {
                    task.cancel();
                }
                return;
            }

            Vector direction = player.getLocation().getDirection().normalize(); // Dirección del jugador
            Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize(); // Vector perpendicular en el plano XZ
            Vector offsetVector = perpendicular.clone().multiply(1.0).subtract(direction.clone().multiply(0.4)); // Retrocede 0.5 bloques
            Location targetRectangleLocation = player.getLocation().clone()
                    .add(offsetVector)
                    .add(0, 2.2, 0);
            CraftBlockDisplay craftBlockDisplay = (CraftBlockDisplay) rectangleDisplay;
            addPassenger(player, craftBlockDisplay);
        }, 0L, 1L)); // Ejecutar cada tick
    }

    public void addPassenger(org.bukkit.entity.Player player, org.bukkit.entity.BlockDisplay rectangleDisplay) {
        // Convierte a las instancias NMS
        CraftBlockDisplay craftBlockDisplay = (CraftBlockDisplay) rectangleDisplay;
        Display.BlockDisplay blockDisplay = craftBlockDisplay.getHandle();

        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer playerNet = craftPlayer.getHandle();

        // Usa startRiding para montar al BlockDisplay como pasajero del jugador
        blockDisplay.startRiding(playerNet, true);
    }
}
