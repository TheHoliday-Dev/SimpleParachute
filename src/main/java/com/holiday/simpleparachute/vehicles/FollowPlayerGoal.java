package com.holiday.simpleparachute.vehicles;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FollowPlayerGoal extends Goal {
    private final Chicken chicken;
    private final double speed;

    public FollowPlayerGoal(Chicken chicken, double speed) {
        this.chicken = chicken;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Player nearestPlayer = this.chicken.level().getNearestPlayer(this.chicken, 10);
        return nearestPlayer != null && !nearestPlayer.isSpectator();
    }

    public void tick() {
        Player nearestPlayer = this.chicken.level().getNearestPlayer(this.chicken, 10);
        if (nearestPlayer != null) {
            // Obtén la ubicación actual del jugador y su dirección
            org.bukkit.Location eyeLocation = nearestPlayer.getBukkitEntity().getEyeLocation();
            org.bukkit.util.Vector direction = eyeLocation.getDirection();

            org.bukkit.util.Vector velocity = direction.multiply(0.2); // Velocidad moderada

            // Ajustar movimiento del pollo
            Vec3 deltaMovement = new Vec3(velocity.getX(), 0.0, velocity.getZ()); // Mantener Y en 0.0
            this.chicken.setDeltaMovement(deltaMovement);

            // Multiplica la dirección por una distancia fija (1 bloque en este caso)
            org.bukkit.util.Vector targetVector = direction.multiply(2).add(eyeLocation.toVector());
            this.chicken.travel(new Vec3(targetVector.getX(), targetVector.getY(), targetVector.getZ()));
            this.chicken.getNavigation().moveTo(
                    targetVector.getX(),
                    targetVector.getY(),
                    targetVector.getZ(),
                    this.speed
            );
        }
    }
}
