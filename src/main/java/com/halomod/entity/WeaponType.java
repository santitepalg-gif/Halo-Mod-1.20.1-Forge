package com.halomod.entity;

/** Armas disponibles. Los valores salen de los items de tu addon (use_duration, damage) y son ajustables. */
public enum WeaponType {
    //            id               daño  vel   cooldown(ticks)  r     g     b
    PLASMA_RIFLE("plasma_rifle",    6f,  2.2f, 3,               0.3f, 0.9f, 0.4f),
    PLASMA_PISTOL("plasma_pistol",  7f,  1.8f, 8,               0.3f, 0.9f, 0.4f),
    ASSAULT_CANNON("assault_cannon", 5f, 2.6f, 2,               0.5f, 1.0f, 0.3f);

    public final String id;
    public final float damage;
    public final float velocity;
    public final int cooldown;
    public final float r, g, b;

    WeaponType(String id, float damage, float velocity, int cooldown, float r, float g, float b) {
        this.id = id;
        this.damage = damage;
        this.velocity = velocity;
        this.cooldown = cooldown;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
