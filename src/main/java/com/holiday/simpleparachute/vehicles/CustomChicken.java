package com.holiday.simpleparachute.vehicles;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.Level;
import org.bukkit.Location;

public class CustomChicken extends Chicken {

    public CustomChicken(Level world, Location location) {
        super(EntityType.CHICKEN, world);
        this.setPos(location.getX(), location.getY(), location.getZ());
        this.setInvisible(true);
        this.persistentInvisibility = true;
        this.updateInvisibilityStatus();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Añadir un goal personalizado para manejar como un caballo
        this.goalSelector.addGoal(8, new FollowPlayerGoal(this, 1.2)); // Velocidad ajustada
    }

    void remove() {
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damagesource) {
        return null;
    }

    /*
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isVehicle()) {
            // Obtener el jugador que controla el pollo
            Player player = (Player) this.getControllingPassenger();
            if (player != null) {

                System.out.println("Travel  ");
                // Ajustar la rotación del pollo para que siga al jugador
                this.setYRot(player.getYRot());
                this.setXRot(player.getXRot() * 0.5F);

                // Calcular la dirección hacia donde mira el jugador
                Vec3 lookDirection = player.getLookAngle().normalize(); // Normalizamos para mantener dirección

                // Ajustar la velocidad y dirección del movimiento
                this.setDeltaMovement(lookDirection.scale(1.0)); // Cambiar 1.0 por otro valor para ajustar velocidad

                // Ajustar el flap (aleteo) para simular vuelo
                this.flapping = 1.0F;
                this.flap += this.flapping * 2.0F;

                return;
            }
        }

        // Comportamiento estándar si no está siendo montado
        super.travel(travelVector);
    }

     */
}
